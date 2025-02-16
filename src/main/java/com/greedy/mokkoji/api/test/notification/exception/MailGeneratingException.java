package com.greedy.mokkoji.api.test.notification.exception;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;

public class MailGeneratingException extends MokkojiException {
    public MailGeneratingException(FailMessage failMessage) {
        super(failMessage);
    }
}
