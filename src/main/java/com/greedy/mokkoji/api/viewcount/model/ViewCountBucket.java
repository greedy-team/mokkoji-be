package com.greedy.mokkoji.api.viewcount.model;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.viewcount.ViewCountTarget;

import java.time.LocalDate;
import java.util.regex.Pattern;

public record ViewCountBucket(
        ViewCountTarget target,
        long targetId,
        LocalDate date
) {

    private static final String FIELD_DELIMITER = "|";
    private static final String KEY_DELIMITER = ":";
    private static final int FIELD_PART_COUNT = 3;

    public ViewCountBucket {
        validateBucket(target, targetId, date);
    }

    public String getRedisField() {
        return target + FIELD_DELIMITER + targetId + FIELD_DELIMITER + date;
    }

    public String getDedupScope() {
        return target + KEY_DELIMITER + targetId;
    }

    public static ViewCountBucket parseRedisField(final String field) {
        if (field == null) {
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_VIEW_COUNT_DATA);
        }

        final String[] fieldParts = field.split(Pattern.quote(FIELD_DELIMITER), -1);
        if (fieldParts.length != FIELD_PART_COUNT) {
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_VIEW_COUNT_DATA);
        }

        return new ViewCountBucket(
                ViewCountTarget.valueOf(fieldParts[0]),
                Long.parseLong(fieldParts[1]),
                LocalDate.parse(fieldParts[2])
        );
    }

    private static void validateBucket(
            final ViewCountTarget target,
            final long targetId,
            final LocalDate date
    ) {
        if (target == null || date == null || targetId <= 0) {
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_VIEW_COUNT_DATA);
        }
    }
}
