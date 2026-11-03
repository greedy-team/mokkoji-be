package com.greedy.mokkoji.api.viewcount.model;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;

import java.util.Map;
import java.util.UUID;

public record ViewCountBatch(
        String id,
        Map<ViewCountBucket, Long> deltas
) {

    public ViewCountBatch {
        validateBatchId(id);
        validateDeltas(deltas);
        deltas = Map.copyOf(deltas);
    }

    private static void validateBatchId(final String id) {
        if (id == null) {
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_VIEW_COUNT_DATA);
        }

        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_VIEW_COUNT_DATA, exception);
        }
    }

    private static void validateDeltas(final Map<ViewCountBucket, Long> deltas) {
        if (deltas == null || deltas.isEmpty()) {
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_VIEW_COUNT_DATA);
        }

        for (final Map.Entry<ViewCountBucket, Long> entry : deltas.entrySet()) {
            if (entry.getKey() == null
                    || entry.getValue() == null
                    || entry.getValue() <= 0) {
                throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_VIEW_COUNT_DATA);
            }
        }
    }
}
