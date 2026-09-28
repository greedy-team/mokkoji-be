-- 모집 알림 스케줄러 sargable 쿼리 전환에 따른 인덱스
CREATE INDEX idx_recruitment_start ON recruitment (recruit_start);
CREATE INDEX idx_recruitment_end ON recruitment (recruit_end);
