package com.greedy.mokkoji.api.viewcount.service;

import com.greedy.mokkoji.api.viewcount.model.ViewCountBatch;
import com.greedy.mokkoji.db.viewcount.repository.ViewCountRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ViewCountBatchApplier {

    private final ViewCountRepository viewCountRepository;
    private final TransactionTemplate transactionTemplate;

    public ViewCountBatchApplier(
            final ViewCountRepository viewCountRepository,
            final PlatformTransactionManager transactionManager
    ) {
        this.viewCountRepository = viewCountRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void applyBatch(final ViewCountBatch batch) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                viewCountRepository.insertBatchHistory(batch.id());
                viewCountRepository.addViewCountDeltas(batch.deltas());
            });
        } catch (DuplicateKeyException exception) {
            // 실패한 트랜잭션이 롤백된 다음, 커밋된 처리 이력을 새 트랜잭션에서 확인한다.
            transactionTemplate.executeWithoutResult(status -> {
                if (!viewCountRepository.existsBatchHistory(batch.id())) {
                    throw exception;
                }
            });
        }
    }
}
