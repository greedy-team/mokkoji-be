package com.greedy.mokkoji.config.swagger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SwaggerDescription {

    private static final String ENUM_PARTS = """
            <details>
            <summary><b><span style='color:#4B89DC;'>🏛 ClubAffiliation (동아리 소속)</span></b></summary>
            <div style="margin-left:10px; line-height:1.6;">
            ─────────────<br>
            • CENTRAL_CLUB: 중앙 동아리<br>
            • DEPARTMENT_CLUB: 정인준/가인준 동아리<br>
            • SMALL_GROUP: 소모임<br>
            </div>
            </details>
            
            <details>
            <summary><b><span style='color:#F29661;'>🎨 ClubCategory (동아리 카테고리)</span></b></summary>
            <div style="margin-left:10px; line-height:1.6;">
            ─────────────<br>
            • CULTURAL_ART: 문화/예술<br>
            • ACADEMIC_CULTURAL: 학술/교양<br>
            • VOLUNTEER_SOCIAL: 봉사/사회<br>
            • SPORTS: 체육<br>
            • RELIGIOUS: 종교<br>
            • OTHER: 기타<br>
            </div>
            </details>
            
            <details>
            <summary><b><span style='color:#F15F5F;'>🕐 RecruitStatus (모집 상태)</span></b></summary>
            <div style="margin-left:10px; line-height:1.6;">
            ─────────────<br>
            • IMMINENT: 모집 마감 임박<br>
            • OPEN: 모집 중<br>
            • ALWAYS: 상시 모집<br>
            • BEFORE: 모집 시작 전<br>
            • CLOSED: 모집 종료<br>
            </div>
            </details>
            
            <details>
            <summary><b><span style='color:#8B00FF;'>👤 UserRole (사용자 권한)</span></b></summary>
            <div style="margin-left:10px; line-height:1.6;">
            ─────────────<br>
            • NORMAL: 일반 사용자<br>
            • CLUB_MASTER: 동아리장<br>
            • CLUB_ADMIN: 총동연 관계자<br>
            • GREEDY_ADMIN: 모꼬지 개발자<br>
            </div>
            </details>
            """;
    @Value("${swagger.local-url}")
    private String localUrl;
    @Value("${swagger.dev-url}")
    private String devUrl;

    public String getDescription() {
        return """
                <div style="text-align:center; font-size:17px; font-weight:bold; margin-bottom:10px;">
                
                <a href="%s/swagger-ui/index.html" target="_blank"
                   style="text-decoration:none; cursor:pointer;">
                    📎 <b>Local Swagger로 바로가기</b>
                </a>
                <br>
                
                <a href="%s/swagger-ui/index.html" target="_blank"
                   style="text-decoration:none; cursor:pointer;">
                    📎 <b>Dev Swagger로 바로가기</b>
                </a>
                
                <br>
                <b>< 공통 ENUM 용어 정리 ></b>
                </div>
                
                %s
                """.formatted(localUrl, devUrl, ENUM_PARTS);
    }
}
