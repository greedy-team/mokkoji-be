package com.greedy.mokkoji.enums.notification;

public enum NotificationType {
    CLUB_APPLICATION_APPROVED("동아리 생성이 승인되었습니다", "'%s' 동아리가 생성되었습니다"),
    CLUB_APPLICATION_REJECTED("동아리 생성이 거절되었습니다", "'%s', 사유: %s"),
    CLUB_MASTER_APPROVED("동아리장 권한이 부여되었습니다", "'%s' 동아리를 관리할 수 있습니다"),
    CLUB_MASTER_REJECTED("동아리장 신청이 거절되었습니다", "'%s', 사유: %s"),
    CLUB_MASTER_TRANSFERRED("동아리장 권한을 넘겨받았습니다", "'%s' 동아리를 관리할 수 있습니다"),
    CLUB_MASTER_RESIGNED("동아리장 권한이 해제되었습니다", "'%s' 동아리의 권한을 넘겼습니다"),
    CLUB_DELETED("동아리가 삭제되었습니다", "'%s' 동아리가 삭제되어 동아리장 권한이 해제되었습니다"),
    ;

    private final String title;
    private final String contentFormat;

    NotificationType(final String title, final String contentFormat) {
        this.title = title;
        this.contentFormat = contentFormat;
    }

    public String getTitle() {
        return title;
    }

    public String formatContent(final Object... args) {
        return String.format(contentFormat, args);
    }
}
