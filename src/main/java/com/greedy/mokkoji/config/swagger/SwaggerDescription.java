package com.greedy.mokkoji.config.swagger;

public class SwaggerDescription {

    public static final String ENUM_DESCRIPTION = """
            <details>
            <summary><b>공통 ENUM 용어 정리 (클릭해서 펼치기)</b></summary>
            
            <br/>
            
            **ClubAffiliation (동아리 소속)**  
            - CENTRAL_CLUB: 중앙 동아리  
            - DEPARTMENT_CLUB: 정인준/가인준 동아리  
            - SMALL_GROUP: 소모임  
            
            <br/>
            
            **ClubCategory (동아리 카테고리)**  
            - CULTURAL_ART: 문화/예술  
            - ACADEMIC_CULTURAL: 학술/교양  
            - VOLUNTEER_SOCIAL: 봉사/사회  
            - SPORTS: 체육  
            - RELIGIOUS: 종교  
            - OTHER: 기타  
            
            <br/>
            
            **RecruitStatus (모집 상태)**  
            - IMMINENT: 모집 마감 임박  
            - OPEN: 모집 중  
            - BEFORE: 모집 시작 전  
            - CLOSED: 모집 종료  
            
            <br/>
            
            **ReportType (신고 대상 타입)**  
            - CLUB: 동아리  
            - RECRUITMENT: 모집글  
            - COMMENT: 댓글  
            - RATING: 평점  
            
            </details>
            """;
}
