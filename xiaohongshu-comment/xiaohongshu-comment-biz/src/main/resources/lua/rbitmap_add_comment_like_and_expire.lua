-- 操作的 Key
local key = KEYS[1]
local commentId = ARGV[1]
local expireSeconds = ARGV[2]

redis.call("R.SETBIT", key, commentId, 1)

-- 设置过期时间
redis.call("EXPIRE", key, expireSeconds)
return 0