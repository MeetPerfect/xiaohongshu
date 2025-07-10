local key = KEYS[1]
local noteId = ARGV[1]
local timestamp = ARGV[2]

local exists = redis.call('EXISTS', key)
if exists == 0 then
    return -1
end

local size = redis.call('ZCARD', key)

-- 若已经收藏了 300条笔记，则一处最早收藏的
if size >= 300 then
    redis.call('ZPOPMIN', key)
end 

-- 添加笔记到收藏列表
redis.call('ZADD', key, timestamp, noteId)
return 0