local key = KEYS[1]
local noteId = ARGS[1]
-- 使用 EXISTS 命令检查布隆过滤器是否存在
local exist = redis.call('EXISTS', key)
if exist == 0 then
    return -1
end

-- 校验该篇笔记是否被收藏过(1 表示已经收藏，0 表示未收藏)
local isCollected = redis.call('BF.EXISTS', key, noteId)
if isCollected == 1 then
    return 1
end

-- 未被收藏，添加收藏数据
redis.call('BF.ADD', key, noteId)
return 0