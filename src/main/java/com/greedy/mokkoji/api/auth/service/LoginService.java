package com.greedy.mokkoji.api.auth.service;

import com.greedy.mokkoji.api.auth.dto.StudentInformationResponseDto;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginService {

    private static StudentInformationResponseDto parseStudentInformation(String html) {
        final Document doc = Jsoup.parse(html);
        final String selector = ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) table.b-board-table tbody tr";
        final List<String> rowLabels = new ArrayList<>();
        List<String> rowValues = new ArrayList<>();

        doc.select(selector).forEach(row -> {
            String label = row.select("th").text().trim();
            String value = row.select("td").text().trim();
            rowLabels.add(label);
            rowValues.add(value);
        });

        String name = null;
        String department = null;
        String grade = null;

        for (int i = 0; i < rowLabels.size(); i++) {
            switch (rowLabels.get(i)) {
                case "이름":
                    name = rowValues.get(i);
                    break;
                case "학과명":
                    department = rowValues.get(i);
                    break;
                case "학년":
                    grade = rowValues.get(i);
                    break;
            }
        }

        if (name == null || department == null || grade == null) {
            log.warn("로그인 실패: 잘못된 아이디 또는 비밀번호. studentId={}");
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_SEJONG_AUTH);
        }

        return new StudentInformationResponseDto(name, department, grade);
    }

    private static OkHttpClient buildClient() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[]{trustAllManager()}, new java.security.SecureRandom());
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        HostnameVerifier hostnameVerifier = (hostname, session) -> true;
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        JavaNetCookieJar cookieJar = new JavaNetCookieJar(cookieManager);

        return new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .hostnameVerifier(hostnameVerifier)
                .sslSocketFactory(sslSocketFactory, trustAllManager())
                .readTimeout(500, TimeUnit.MILLISECONDS)
                .build();
    }

    private static X509TrustManager trustAllManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        };
    }

    @Transactional
    public StudentInformationResponseDto getStudentInformation(final String id, final String password) {
        final String loginUrl = "https://portal.sejong.ac.kr/jsp/login/login_action.jsp";

        try {
            OkHttpClient client = buildClient();

            FormBody formData = new FormBody.Builder()
                    .add("mainLogin", "N")
                    .add("rtUrl", "sjpt.sejong.ac.kr/main/view/Login/doSsoLogin.do?p=")
                    .add("id", id)
                    .add("password", password)
                    .build();

            Request loginRequest = new Request.Builder()
                    .url(loginUrl)
                    .post(formData)
                    .header("Host", "portal.sejong.ac.kr")
                    .header("Referer", "https://portal.sejong.ac.kr/jsp/login/loginSSL.jsp?rtUrl=sjpt.sejong.ac.kr/main/view/Login/doSsoLogin.do?p=")
                    .header("Cookie", "chknos=false")
                    .build();

            //TODO::세종대학교 인증 처리 안됨
            Response loginResponse = null;
            try {
                loginResponse = client.newCall(loginRequest).execute();
            } catch (IOException e) {
                log.error("로그인 요청 중 오류 발생: {}", e.getMessage());
                throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_SEJONG_AUTH);
            }

            if (loginResponse == null || loginResponse.body() == null) {
                log.error("로그인 요청 실패");
                throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_SEJONG_AUTH);
            }

            log.error("로그인 정보", loginResponse);

            loginResponse.close();

            // TODO:: 필요한 이유 찾기
//        String redirectUrl = "http://classic.sejong.ac.kr/_custom/sejong/sso/sso-return.jsp?returnUrl=https://classic.sejong.ac.kr/classic/index.do";
//
//        Request redirectRequest = new Request.Builder().url(redirectUrl).get().build();
//        try (Response redirectResponse = client.newCall(redirectRequest).execute()) {
//            log.info("SSO 리다이렉트 요청 완료. 응답 코드: {}", redirectResponse.code());
//        }

            final String finalUrl = "https://classic.sejong.ac.kr/classic/reading/status.do";
            Request finalRequest = new Request.Builder().url(finalUrl).get().build();

            Response finalResponse = client.newCall(finalRequest).execute();
            if (finalResponse.body() == null) {
                log.error("최종 페이지 응답 바디가 없습니다.");
                throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_SEJONG_AUTH);
            }
            String finalHtml = finalResponse.body().string();

            return parseStudentInformation(finalHtml);
        } catch (Exception e) {
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_SEJONG_AUTH);
        }
    }
}
