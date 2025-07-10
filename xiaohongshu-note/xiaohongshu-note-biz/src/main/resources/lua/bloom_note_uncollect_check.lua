local key = KEYS[1]
local noteId = ARGV[1]


-- 检查布隆过滤器中是否存在该笔记ID
local exists = redis.call('EXISTS', key)
if exists == 0 then
    return -1
end

-- 检查布隆过滤器中是否存在该笔记ID (1表示收藏，0表示未收藏)
return redis.call('BF.EXISTS', key, noteId)