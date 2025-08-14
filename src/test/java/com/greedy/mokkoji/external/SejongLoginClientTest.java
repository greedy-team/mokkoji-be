package com.greedy.mokkoji.external;

import com.greedy.mokkoji.api.external.sejong.SejongLoginClient;
import com.greedy.mokkoji.api.user.dto.resopnse.StudentInformationResponse;
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

    @Value("${test.studentId}")
    private String studentId;

    @Value("${test.password}")
    private String password;

    @Test
    void 외부URL에서_회원정보_가져오기() {
        //when
        StudentInformationResponse response = sejongLoginClient.getStudentInformation(studentId, password);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isNotEmpty();
        assertThat(response.department()).isNotEmpty();
        assertThat(response.grade()).isNotEmpty();
    }
}
