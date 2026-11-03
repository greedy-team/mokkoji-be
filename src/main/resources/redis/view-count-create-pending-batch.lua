-- KEYS: 현재 증가분 Hash, 보관할 배치 Hash, 대기 목록 Sorted Set
-- ARGV: 배치 ID, 생성 시각(ms), 최대 대기 배치 수
-- 반환: 배치 생성 1, 증가분 없음 또는 대기 목록 가득 참 0
local activeKey = KEYS[1]
local batchKey = KEYS[2]
local pendingIndexKey = KEYS[3]
local batchId = ARGV[1]
local createdAtMillis = ARGV[2]
local maxPendingBatches = tonumber(ARGV[3])

if redis.call("HLEN", activeKey) == 0 then
    return 0
end

if redis.call("ZCARD", pendingIndexKey) >= maxPendingBatches then
    return 0
end

if redis.call("EXISTS", batchKey) == 1 then
    return redis.error_reply("ERR view-count batch already exists")
end

local pendingAdded = redis.call("ZADD", pendingIndexKey, "NX", createdAtMillis, batchId)
if pendingAdded == 0 then
    return redis.error_reply("ERR view-count batch is already pending")
end

local renameResult = redis.pcall("RENAMENX", activeKey, batchKey)
if type(renameResult) == "table" and renameResult.err then
    redis.call("ZREM", pendingIndexKey, batchId)
    return redis.error_reply(renameResult.err)
end
if renameResult == 0 then
    redis.call("ZREM", pendingIndexKey, batchId)
    return redis.error_reply("ERR view-count batch already exists")
end

-- 이후 조회는 새 active Hash에 쌓이며, 이 배치는 DB 반영 확인 전까지 유지한다.
return 1
