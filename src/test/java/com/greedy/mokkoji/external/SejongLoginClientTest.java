package com.greedy.mokkoji.external;

import com.greedy.mokkoji.api.external.SejongLoginClient;
import com.greedy.mokkoji.api.user.dto.resopnse.StudentInformationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SejongLoginClientTest {

    @Autowired
    private SejongLoginClient sejongLoginClient;

    @Value("${id}")
    private String studentId;

    @Value("${password}")
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

        System.out.println("회원정보 가져오기 성공: " + response);
    }
}
