package com.greedy.mokkoji.api.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AfterCommitS3Cleaner {

    private final AppDataS3Client appDataS3Client;
    private final AfterCommitExecutor afterCommitExecutor;

    public void deleteAfterCommit(final List<String> fileKeys) {
        if (fileKeys.isEmpty()) {
            return;
        }

        afterCommitExecutor.run(() -> appDataS3Client.deleteObjectsAsync(fileKeys));
    }
}
