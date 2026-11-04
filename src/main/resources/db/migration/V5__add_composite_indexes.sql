-- 모집 알림 스케줄러 sargable 쿼리 전환에 따른 인덱스
-- idx_recruitment_end는 V1 baseline에 이미 정의됨
CREATE INDEX idx_recruitment_start ON recruitment (recruit_start);
