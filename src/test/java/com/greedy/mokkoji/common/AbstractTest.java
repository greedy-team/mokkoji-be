package com.greedy.mokkoji.common;

import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.comment.repository.CommentRepository;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentImageRepository;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import com.greedy.mokkoji.db.university.repository.UniversityRepository;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class AbstractTest {
    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected UniversityRepository universityRepository;

    @Autowired
    protected ClubRepository clubRepository;

    @Autowired
    protected FavoriteRepository favoriteRepository;

    @Autowired
    protected RecruitmentRepository recruitmentRepository;

    @Autowired
    protected RecruitmentImageRepository recruitmentImageRepository;

    @Autowired
    protected CommentRepository commentRepository;
}
