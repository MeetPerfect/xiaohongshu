local key = KEYS[1]
local noteId = ARGV[1]

local exists = redis.call("EXISTS", key)
if exists == 0 then
    return -1
end
-- 校验该篇笔记是否被点赞过(1 表示已经点赞，0 表示未点赞)
return redis.call('BF.EXISTS', key, noteId)