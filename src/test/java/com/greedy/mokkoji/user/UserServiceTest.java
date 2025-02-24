package com.greedy.mokkoji.user;

import com.greedy.mokkoji.api.external.SejongLoginClient;
import com.greedy.mokkoji.api.user.dto.resopnse.StudentInformationResponse;
import com.greedy.mokkoji.api.user.service.UserService;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("유저 서비스 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    SejongLoginClient sejongLoginClient;

    @Test
    void 기존_유저는_로그인을_할_수_있다(){
        //given
        final String studentId = "학번";
        final String password = "비밀번호";

        final StudentInformationResponse studentInformationResponse = StudentInformationResponse.of("혜빈", "컴공과", "4");

        final User expected = User.builder()
                .name("혜빈")
                .grade("4")
                .studentId("학번")
                .department("컴공과")
                .build();

        BDDMockito.given(sejongLoginClient.getStudentInformation(any(), any())).willReturn(studentInformationResponse);
        BDDMockito.given(userRepository.findByStudentId(any())).willReturn(Optional.ofNullable(expected));

        //when
        final User actual = userService.login(studentId, password);

        //then
        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void 토큰을_발급_받을_수_있다(){
        //given
        final String studentId = "학번";
        final String password = "비밀번호";

        final StudentInformationResponse studentInformationResponse = StudentInformationResponse.of("혜빈", "컴공과", "4");

        final User expected = User.builder()
                .name("혜빈")
                .grade("4")
                .studentId("학번")
                .department("컴공과")
                .build();

        BDDMockito.given(sejongLoginClient.getStudentInformation(any(), any())).willReturn(studentInformationResponse);
        BDDMockito.given(userRepository.findByStudentId(any())).willReturn(Optional.ofNullable(expected));

        //when
        final User actual = userService.login(studentId, password);

        //then
        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
    //유저가 생성되는 지
    //유저가 중복생성 안 되는 지
    //토큰 재발급 가능한 지
    //로그아웃 가능한지
}
