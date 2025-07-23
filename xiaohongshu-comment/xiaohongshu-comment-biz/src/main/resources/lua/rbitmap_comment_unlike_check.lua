
local key = KEYS[1]
local commentId = ARGV[1]

local exists = redis.call('EXISTS', key)
if exists == 0 then
    return -1
end

-- 校验该评论是否被点赞过(1 表示已经点赞，0 表示未点赞)
return redis.call('R.GETBIT', key, commentId)