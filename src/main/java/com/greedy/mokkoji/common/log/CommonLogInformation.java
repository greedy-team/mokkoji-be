package com.greedy.mokkoji.common.log;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Getter
@Component
@RequestScope
public class CommonLogInformation {
    private final String requestIdentifier;
    private String uri;

    public CommonLogInformation() {
        this.requestIdentifier = UUID.randomUUID().toString();
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }
}
