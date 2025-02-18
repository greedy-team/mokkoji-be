package com.greedy.mokkoji.api.login.service;

import com.greedy.mokkoji.api.login.dto.StudentInformationResponseDto;
import jakarta.transaction.Transactional;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class LoginService {
    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private static StudentInformationResponseDto parseStudentInformation(String html) {
        Document doc = Jsoup.parse(html);
        String selector = ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) table.b-board-table tbody tr";
        List<String> rowLabels = new ArrayList<>();
        List<String> rowValues = new ArrayList<>();

        doc.select(selector).forEach(row -> {
            String label = row.select("th").text().trim();
            String value = row.select("td").text().trim();
            rowLabels.add(label);
            rowValues.add(value);
        });

        String name = null;
        String studentId = null;
        String department = null;
        String grade = null;

        for (int i = 0; i < rowLabels.size(); i++) {
            switch (rowLabels.get(i)) {
                case "이름":
                    name = rowValues.get(i);
                    break;
                case "학번":
                    studentId = rowValues.get(i);
                    break;
                case "학과명":
                    department = rowValues.get(i);
                    break;
                case "학년":
                    grade = rowValues.get(i);
                    break;
            }
        }

        log.info("==== [사용자 정보] ====");
        log.info("이름 = {}", name);
        log.info("학번 = {}", studentId);
        log.info("학과명 = {}", department);
        log.info("학년 = {}", grade);
        log.info("=== 모든 정보 로그 출력 완료 ===");

        return new StudentInformationResponseDto(name, studentId, department, grade);
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
    public StudentInformationResponseDto getStudentInformation(String id, String password) throws Exception {
        String loginUrl = "https://portal.sejong.ac.kr/jsp/login/login_action.jsp";
        String finalUrl = "https://classic.sejong.ac.kr/classic/reading/status.do";

        OkHttpClient client = buildClient();

        // 로그인 요청
        FormBody formData = new FormBody.Builder()
                .add("mainLogin", "N")
                .add("rtUrl", "library.sejong.ac.kr")
                .add("id", id)
                .add("password", password)
                .build();

        Request loginRequest = new Request.Builder()
                .url(loginUrl)
                .post(formData)
                .header("Referer", "https://portal.sejong.ac.kr")
                .build();

        try (Response loginResponse = client.newCall(loginRequest).execute()) {
            if (loginResponse.body() == null) {
                log.error("로그인 실패: 응답이 없음");
                return new StudentInformationResponseDto(null, null, null, null);
            }
        }

        // 로그인 후 최종 URL 요청
        Request finalRequest = new Request.Builder().url(finalUrl).get().build();
        String finalHtml;
        try (Response finalResponse = client.newCall(finalRequest).execute()) {
            if (finalResponse.body() == null) {
                log.error("사용자 정보 페이지 로딩 실패");
                return new StudentInformationResponseDto(null, null, null, null);
            }
            finalHtml = finalResponse.body().string();
        }

        return parseStudentInformation(finalHtml);
    }
}
