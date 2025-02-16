package com.greedy.mokkoji.api.test.notification.exception;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;

public class MailSendingException extends MokkojiException {
    public MailSendingException(FailMessage failMessage) {
        super(failMessage);
    }
}
