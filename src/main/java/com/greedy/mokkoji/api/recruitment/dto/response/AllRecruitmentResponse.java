package com.greedy.mokkoji.api.recruitment.dto.response;

import com.greedy.mokkoji.api.club.dto.page.PageResponse;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.recruitment.RecruitStatus;

import java.time.LocalDateTime;
import java.util.List;

//Todo: UI가 어떻게 나올지 몰라 필요한 것 같아 보이는 DTO 필드는 다 넣어뒀어요. 나중에 필요없는 거 빼야할 거 같아요.
public record AllRecruitmentResponse(
        List<AllRecruitmentResponse.Recruitment> recruitments,
        PageResponse page
) {
    public static AllRecruitmentResponse of(List<Recruitment> recruitments, PageResponse page) {
        return new AllRecruitmentResponse(recruitments, page);
    }

    public record Recruitment(
            Club club,
            Long id,
            String title,
            LocalDateTime recruitStart,
            LocalDateTime recruitEnd,
            RecruitStatus status,
            boolean isFavorite
    ) {
    }

    public record Club(
                               Long id,
                               String name,
                               String description,
                               ClubCategory clubCategory,
                               ClubAffiliation clubAffiliation,
                               String imageURL
    ) {
    }
}
