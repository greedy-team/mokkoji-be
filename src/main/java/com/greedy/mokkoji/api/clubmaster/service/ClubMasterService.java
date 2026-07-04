package com.greedy.mokkoji.api.clubmaster.service;

import com.greedy.mokkoji.api.auth.service.ManageAuthorizer;
import com.greedy.mokkoji.api.clubmaster.dto.response.GetMyClubMasterApplicationsResponse;
import com.greedy.mokkoji.api.email.service.EmailService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.clubmaster.entity.ClubMasterApplication;
import com.greedy.mokkoji.db.clubmaster.entity.ClubMasterTransfer;
import com.greedy.mokkoji.db.clubmaster.repository.ClubMasterApplicationRepository;
import com.greedy.mokkoji.db.clubmaster.repository.ClubMasterTransferRepository;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.university.repository.UniversityRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.RedisRepository;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.university.UniversityCode;
import com.greedy.mokkoji.enums.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMasterService {
    private static final long LINK_EXPIRATION = 10 * 60 * 1000; //10분
    private static final String CLUB_TRANSFER_KEY_PREFIX = "clubTransfer";
    private static final String KEY_DELIMITER = ":";
    private final ClubMasterApplicationRepository clubMasterApplicationRepository;
    private final ClubMasterTransferRepository clubMasterTransferRepository;
    private final ManageAuthorizer manageAuthorizer;
    private final UniversityRepository universityRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RedisRepository redisRepository;

    @Transactional
    public void createClubMasterApplication(
            final Long userId,
            final UniversityCode universityCode,
            final Long clubId,
            final String userName
    ) {
        University university = findUniversityOrThrow(universityCode);

        Club club = findClubOrThrow(clubId);
        validateClubMasterNotExists(club);

        User user = findUserOrThrow(userId);
        validateDuplicateApplication(user, club);

        ClubMasterApplication application = ClubMasterApplication.builder()
                .university(university)
                .club(club)
                .user(user)
                .userName(userName)
                .build();

        clubMasterApplicationRepository.save(application);
    }

    @Transactional
    public List<GetMyClubMasterApplicationsResponse> getMyClubMasterApplications(
            final Long userId
    ) {
        User user = findUserOrThrow(userId);
        List<ClubMasterApplication> applications = clubMasterApplicationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return applications.stream()
                .map(application -> new GetMyClubMasterApplicationsResponse(
                        application.getId(),
                        application.getUniversity().getName(),
                        application.getClub().getName(),
                        application.getUserName(),
                        application.getStatus(),
                        application.getRejectReason(),
                        application.getCreatedAt(),
                        application.getUpdatedAt()
                ))
                .toList();
    }

    @Transactional
    public void applyClubMasterTransfer(
            final AuthRole authRole,
            final Long userId,
            final Long clubId,
            final String nextClubMasterName,
            final String nextClubMasterEmail
    ) {
        Club club = findClubOrThrow(clubId);
        manageAuthorizer.validateCanManageClub(authRole, userId, club);
        User previousMaster = findUserOrThrow(userId);

        ClubMasterTransfer transfer = ClubMasterTransfer.builder()
                .club(club)
                .previousMaster(previousMaster)
                .nextMasterName(nextClubMasterName)
                .nextMasterEmail(nextClubMasterEmail)
                .build();
        clubMasterTransferRepository.save(transfer);

        String uri = createClubMasterTransferUri(transfer.getId());
        emailService.sendClubMasterTransferNotification(nextClubMasterEmail, club.getName(), uri);
    }

    @Transactional
    public void acceptClubMasterTransfer(final Long userId, final String uuid) {
        String key = CLUB_TRANSFER_KEY_PREFIX + KEY_DELIMITER + uuid;
        String transferId = redisRepository.find(key);

        if (transferId == null) {
            throw new MokkojiException(FailMessage.BAD_REQUEST_INVALID_LINK);
        }

        ClubMasterTransfer transfer = findClubMasterTransferOrThrow(Long.parseLong(transferId));
        transfer.approve();

        Club club = transfer.getClub();
        User previousMaster = club.getMaster();
        User nextClubMaster = findUserOrThrow(userId);

        nextClubMaster.updateRole(UserRole.CLUB_MASTER);
        club.updateMaster(nextClubMaster);

        updatePreviousMasterRoleIfNeeded(previousMaster, nextClubMaster);
    }

    private String createClubMasterTransferUri(Long transferId) {
        String uuid = UUID.randomUUID().toString();

        redisRepository.save(CLUB_TRANSFER_KEY_PREFIX + KEY_DELIMITER + uuid, String.valueOf(transferId), LINK_EXPIRATION);

        return ServletUriComponentsBuilder.newInstance()
                .path("/club-master-transfer/{uuid}")
                .buildAndExpand(uuid)
                .toUriString();
    }

    private void updatePreviousMasterRoleIfNeeded(
            final User previousMaster,
            final User nextClubMaster
    ) {
        if (previousMaster == null || previousMaster.getId().equals(nextClubMaster.getId())) {
            return;
        }

        boolean isStillClubMaster = clubRepository.existsByMasterId(previousMaster.getId());
        if (!isStillClubMaster) {
            previousMaster.updateRole(UserRole.NORMAL);
        }
    }

    private University findUniversityOrThrow(final UniversityCode universityCode) {
        return universityRepository.findByCode(universityCode)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_UNIVERSITY));
    }

    private Club findClubOrThrow(final Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB));
    }

    private User findUserOrThrow(final Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
    }

    private ClubMasterTransfer findClubMasterTransferOrThrow(final Long transferId) {
        return clubMasterTransferRepository.findById(transferId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB_MASTER_TRANSFER));
    }

    private void validateClubMasterNotExists(Club club) {
        if (club.getMaster() != null) {
            throw new MokkojiException(FailMessage.FORBIDDEN_ALREADY_EXIST_CLUB_MASTER);
        }
    }

    private void validateDuplicateApplication(User user, Club club) {
        if (clubMasterApplicationRepository.existsByUserAndClubAndStatusNot(user, club, ApplicationStatus.REJECTED)) {
            throw new MokkojiException(FailMessage.CONFLICT_CLUB_MASTER_APPLICATION);
        }
    }
}
