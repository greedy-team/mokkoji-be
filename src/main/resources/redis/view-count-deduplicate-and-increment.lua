-- KEYS: 현재 증가분 Hash, 방문자 중복 제외 키
-- ARGV: 집계 필드, 중복 제외 기간(ms)
-- 반환: 신규 집계 1, 중복 조회 0
local activeKey = KEYS[1]
local seenKey = KEYS[2]
local field = ARGV[1]
local dedupTtlMillis = ARGV[2]

local seenCreated = redis.call("SET", seenKey, "1", "NX", "PX", dedupTtlMillis)
if not seenCreated then
    return 0
end

local incrementResult = redis.pcall("HINCRBY", activeKey, field, 1)
if type(incrementResult) == "table" and incrementResult.err then
    -- 증가에 실패한 방문자가 중복으로 제외되지 않도록 이번 표시를 되돌린다.
    redis.call("DEL", seenKey)
    return redis.error_reply(incrementResult.err)
end

return 1
