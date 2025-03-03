package com.greedy.mokkoji.common;

import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class AbstractTest {
    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ClubRepository clubRepository;

    @Autowired
    protected FavoriteRepository favoriteRepository;

    @Autowired
    protected RecruitmentRepository recruitmentRepository;
}
