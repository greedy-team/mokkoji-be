package com.greedy.mokkoji.common.exception;

import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.Getter;

@Getter
public class MokkojiException extends RuntimeException {

    private final FailMessage failMessage;

    public MokkojiException(final FailMessage failMessage) {
        super(failMessage.getMessage());
        this.failMessage = failMessage;
    }
}
