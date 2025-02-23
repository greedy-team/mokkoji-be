package com.greedy.mokkoji.api.external;

import com.greedy.mokkoji.api.user.dto.resopnse.StudentInformationExternalResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;
import jakarta.transaction.Transactional;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class SejongLoginClient {

    @Value("${login.loginUrl}")
    private String loginUrl;

    @Value("${login.rtUrl}")
    private String rtUrl;

    @Value("${login.finalUrl}")
    private String finalUrl;

    @Value("${login.host}")
    private String host;

    @Value("${login.referer}")
    private String referer;

    @Value("${login.cookie}")
    private String cookie;

    @Transactional
    public StudentInformationExternalResponse getStudentInformation(final String id, final String password) {
        try {
            OkHttpClient client = buildClient();
            authenticate(client, id, password);
            return fetchStudentInformation(client);
        } catch (Exception e) {
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_SEJONG_AUTH);
        }
    }

    //사용자 인증수행
    private void authenticate(OkHttpClient client, String id, String password) {
        FormBody formData = new FormBody.Builder()
                .add("mainLogin", "N")
                .add("rtUrl", rtUrl)
                .add("id", id)
                .add("password", password)
                .build();

        Request loginRequest = new Request.Builder()
                .url(loginUrl)
                .post(formData)
                .header("Host", host)
                .header("Referer", referer)
                .header("Cookie", cookie)
                .build();

        executeRequest(client, loginRequest);
    }

    //학생정보 가져오기
    private StudentInformationExternalResponse fetchStudentInformation(OkHttpClient client) throws IOException {
        Request request = new Request.Builder().url(finalUrl).get().build();
        try (Response response = executeRequest(client, request)) {
            String html = response.body().string();
            return parseStudentInformation(html);
        }
    }

    //http 요청수행
    private Response executeRequest(OkHttpClient client, Request request) {
        try {
            Response response = client.newCall(request).execute();
            if (response.body() == null) {
                throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_SEJONG_AUTH);
            }
            return response;
        } catch (IOException e) {
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_SEJONG_AUTH);
        }
    }

    //OkHttpClient 생성 (SSL 인증 무시 설정 포함)
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

    //html에서 학생정보 추출
    private static StudentInformationExternalResponse parseStudentInformation(String html) {
        final Document doc = Jsoup.parse(html);
        final String selector = ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) table.b-board-table tbody tr";
        final List<String> rowLabels = new ArrayList<>();
        List<String> rowValues = new ArrayList<>();

        doc.select(selector).forEach(row -> {
            rowLabels.add(row.select("th").text().trim());
            rowValues.add(row.select("td").text().trim());
        });

        return extractStudentInfo(rowLabels, rowValues);
    }

    //html에서 추출한 정보를 StudentInformationResponse 객체로 변환
    private static StudentInformationExternalResponse extractStudentInfo(List<String> labels, List<String> values) {
        String name = null, department = null, grade = null;

        for (int i = 0; i < labels.size(); i++) {
            switch (labels.get(i)) {
                case "이름":
                    name = values.get(i);
                    break;
                case "학과명":
                    department = values.get(i);
                    break;
                case "학년":
                    grade = values.get(i);
                    break;
            }
        }

        if (name == null || department == null || grade == null) {
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_SEJONG_AUTH);
        }

        return StudentInformationExternalResponse.of(name, department, grade);
    }

    //모든 SSL 인증서를 신뢰하는 TrustManager 생성
    private static X509TrustManager trustAllManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        };
    }
}
