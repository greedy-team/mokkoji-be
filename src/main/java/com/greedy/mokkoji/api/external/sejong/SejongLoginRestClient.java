package com.greedy.mokkoji.api.external.sejong;

import com.greedy.mokkoji.api.user.dto.resopnse.StudentInformationResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SejongLoginRestClient {

    @Value("${rest-client-login.loginUrl}")
    private String loginUrl;

    @Value("${rest-client-login.rtUrl}")
    private String rtUrl;

    @Value("${rest-client-login.finalUrl}")
    private String studentInfoUrl;

    @Value("${rest-client-login.referer}")
    private String referer;


    public StudentInformationResponse getStudentInformation(final String id, final String password) {
        RestClient restClient = RestClient.create();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("rtUrl", rtUrl);
        formData.add("id", id);
        formData.add("password", password);

        try {
            ResponseEntity<String> loginResponse = restClient.post()
                    .uri(loginUrl)
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Referer",
                            referer
                    )
                    .body(formData)
                    .retrieve()
                    .toEntity(String.class);

            List<String> cookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
            String cookieHeader = cookies != null ? cookies.stream()
                    .map(cookie -> cookie.split(";", 2)[0]) // "키=값" 부분만 추출
                    .collect(Collectors.joining("; ")) : "";

            ResponseEntity<String> responseEntity = restClient.post()
                    .uri(studentInfoUrl)
                    .header("Cookie", cookieHeader)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .retrieve()
                    .toEntity(String.class);

            String html = responseEntity.getBody();
            Document doc = Jsoup.parse(html);
            Elements rows = doc.select("tr");

            String name = null;
            String grade = null;
            String major = null;

            for (Element row : rows) {
                String header = row.selectFirst("th") != null ? row.selectFirst("th").text().trim() : "";
                String value = row.selectFirst("td") != null ? row.selectFirst("td").text().trim() : "";

                switch (header) {
                    case "이름":
                        name = value;
                        break;
                    case "학년":
                        grade = value;
                        break;
                    case "학과명":
                        major = value;
                        break;
                }
            }

            return StudentInformationResponse.of(name, major, grade);
        } catch (Exception e) {
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_SEJONG_AUTH);
        }
    }
}
