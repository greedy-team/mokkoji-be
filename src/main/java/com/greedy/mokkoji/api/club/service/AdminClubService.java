package com.greedy.mokkoji.api.club.service;

import com.greedy.mokkoji.api.auth.service.ManageAuthorizer;
import com.greedy.mokkoji.api.club.dto.response.AdminClubResponse;
import com.greedy.mokkoji.api.club.dto.response.AdminClubsResponse;
import com.greedy.mokkoji.api.external.AfterCommitS3Cleaner;
import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.admin.entity.Admin;
import com.greedy.mokkoji.db.admin.repository.AdminRepository;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.clubmaster.repository.ClubMasterApplicationRepository;
import com.greedy.mokkoji.db.clubmaster.repository.ClubMasterTransferRepository;
import com.greedy.mokkoji.db.comment.repository.CommentRepository;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentImageRepository;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.admin.AdminRole;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.university.UniversityCode;
import com.greedy.mokkoji.enums.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminClubService {

    private final ClubRepository clubRepository;
    private final AdminRepository adminRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentImageRepository recruitmentImageRepository;
    private final ClubMasterApplicationRepository clubMasterApplicationRepository;
    private final ClubMasterTransferRepository clubMasterTransferRepository;
    private final AfterCommitS3Cleaner afterCommitS3Cleaner;
    private final ManageAuthorizer manageAuthorizer;

    @Transactional(readOnly = true)
    public AdminClubsResponse getAdminClubs(
            final AuthRole authRole,
            final Long adminId,
            final UniversityCode universityCode,
            final Pageable pageable
    ) {
        manageAuthorizer.validateAdminAuth(authRole, adminId);

        final Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        final UniversityCode targetUniversityCode = resolveUniversityCode(admin, universityCode);

        final Page<Club> page = clubRepository.findClubsForAdmin(targetUniversityCode, pageable);

        final List<AdminClubResponse> clubs = page.getContent()
                .stream()
                .map(AdminClubResponse::from)
                .toList();

        return AdminClubsResponse.of(
                clubs,
                PageResponse.of(page.getNumber(), page.getSize(), page.getTotalPages(), (int) page.getTotalElements())
        );
    }

    @Transactional
    public Void deleteClub(
            final AuthRole authRole,
            final Long adminId,
            final Long clubId
    ) {
        manageAuthorizer.validateAdminAuth(authRole, adminId);
        final Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB));
        manageAuthorizer.validateCanManageUniversity(adminId, club.getUniversity());

        final List<String> s3KeysToDelete = collectS3KeysToDelete(club);
        final User master = club.getMaster();

        deleteClubAssociations(clubId);
        clubRepository.delete(club);

        updateMasterRoleIfNeeded(master);

        afterCommitS3Cleaner.deleteAfterCommit(s3KeysToDelete);

        return null;
    }

    private UniversityCode resolveUniversityCode(final Admin admin, final UniversityCode universityCode) {
        if (admin.getRole() == AdminRole.UNIVERSITY_ADMIN) {
            return admin.getUniversity().getCode();
        }
        return universityCode;
    }

    private List<String> collectS3KeysToDelete(final Club club) {
        final List<String> s3KeysToDelete = new ArrayList<>();

        final List<Long> recruitmentIds = recruitmentRepository.findIdsByClubId(club.getId());

        final List<String> recruitmentImageKeys = recruitmentImageRepository.findImagesByRecruitmentIdIn(recruitmentIds);
        s3KeysToDelete.addAll(recruitmentImageKeys);

        if (club.getLogo() != null && !club.getLogo().isBlank()) {
            s3KeysToDelete.add(club.getLogo());
        }

        return s3KeysToDelete;
    }

    private void deleteClubAssociations(final Long clubId) {
        final List<Long> recruitmentIds = recruitmentRepository.findIdsByClubId(clubId);
        recruitmentImageRepository.deleteByRecruitmentIdIn(recruitmentIds);
        recruitmentRepository.deleteByClubId(clubId);

        commentRepository.deleteByClubId(clubId);

        favoriteRepository.deleteByClubId(clubId);

        clubMasterApplicationRepository.deleteByClubId(clubId);
        clubMasterTransferRepository.deleteByClubId(clubId);
    }

    private void updateMasterRoleIfNeeded(final User master) {
        if (master == null) {
            return;
        }

        boolean isStillClubMaster = clubRepository.existsByMaster_Id(master.getId());
        if (!isStillClubMaster) {
            master.updateRole(UserRole.NORMAL);
        }
    }
}
