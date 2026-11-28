-- KEYS: DB 반영이 확인된 배치 Hash, 대기 목록 Sorted Set
-- ARGV: 배치 ID
-- 반환: 정리 완료 1. 이미 정리된 배치를 다시 요청해도 성공한다.
local batchKey = KEYS[1]
local pendingIndexKey = KEYS[2]
local batchId = ARGV[1]

-- 대기 목록 접근에 실패하면 배치 내용을 삭제하지 않는다.
redis.call("ZREM", pendingIndexKey, batchId)
redis.call("DEL", batchKey)

return 1
