package com.greedy.mokkoji.api.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AfterCommitS3Cleaner {

    private final AppDataS3Client appDataS3Client;

    public void deleteAfterCommit(final List<String> fileKeys) {
        if (fileKeys.isEmpty()) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    fileKeys.forEach(appDataS3Client::deleteObject);
                }
            });
            return;
        }

        fileKeys.forEach(appDataS3Client::deleteObject);
    }
}
