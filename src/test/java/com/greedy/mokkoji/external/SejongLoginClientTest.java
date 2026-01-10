package com.greedy.mokkoji.external;

import com.greedy.mokkoji.api.external.sejong.SejongLoginClient;
import com.greedy.mokkoji.api.user.dto.resopnse.StudentInformationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class SejongLoginClientTest {

    @Autowired
    private SejongLoginClient sejongLoginClient;

    @Value("${account.studentId}")
    private String studentId;

    @Value("${account.password}")
    private String password;

    @Test
    @DisplayName("외부URL에서 회원 정보 조회 성공 여부를 검증한다.")
    void getStudentInformationFromExternalApi() {
        //when
        StudentInformationResponse response = sejongLoginClient.getStudentInformation(studentId, password);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isNotEmpty();
        assertThat(response.department()).isNotEmpty();
        assertThat(response.grade()).isNotEmpty();
    }
}
