package com.greedy.mokkoji.db.viewcount.repository;

import com.greedy.mokkoji.api.viewcount.model.ViewCountBucket;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.Comparator;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ViewCountRepository {

    private static final Comparator<ViewCountBucket> BUCKET_ORDER = Comparator
            .comparing((ViewCountBucket bucket) -> bucket.target().name())
            .thenComparingLong(ViewCountBucket::targetId)
            .thenComparing(ViewCountBucket::date);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public long findClubViewCount(final long clubId) {
        final Long viewCount = namedParameterJdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(view_count), 0)
                FROM view_count
                WHERE target_type = 'CLUB' AND target_id = :clubId
                """, Map.of("clubId", clubId), Long.class);
        return viewCount == null ? 0L : viewCount;
    }

    public void insertBatchHistory(final String batchId) {
        namedParameterJdbcTemplate.update("""
                INSERT INTO view_count_batch_history (batch_id) VALUES (:batchId)
                """, Map.of("batchId", batchId));
    }

    public boolean existsBatchHistory(final String batchId) {
        return Boolean.TRUE.equals(namedParameterJdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM view_count_batch_history WHERE batch_id = :batchId
                )
                """, Map.of("batchId", batchId), Boolean.class));
    }

    public void addViewCountDeltas(final Map<ViewCountBucket, Long> deltas) {
        final SqlParameterSource[] viewCountParameters = deltas.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(BUCKET_ORDER))
                .map(entry -> {
                    final ViewCountBucket bucket = entry.getKey();
                    return new MapSqlParameterSource()
                            .addValue("targetType", bucket.target().name())
                            .addValue("targetId", bucket.targetId())
                            .addValue("statDate", Date.valueOf(bucket.date()))
                            .addValue("delta", entry.getValue());
                })
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate("""
                INSERT INTO view_count
                    (target_type, target_id, stat_date, view_count)
                VALUES (:targetType, :targetId, :statDate, :delta) AS incoming
                ON DUPLICATE KEY UPDATE view_count = view_count.view_count + incoming.view_count
                """, viewCountParameters);
    }
}
