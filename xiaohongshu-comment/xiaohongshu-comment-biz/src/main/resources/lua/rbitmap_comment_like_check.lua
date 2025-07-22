local key = KEYS[1]
local commentId = ARGV[1]

-- 使用 EXISTS 命令检查是否存在
local exists = redis.call('EXISTS', key)
if exists == 0 then
    return -1
end
-- 校验该评论是否被点赞过(1 表示已经点赞，0 表示未点赞)
local isLiked = redis.call('R.GETBIT', key, commentId)
if isLiked == 1 then
    return 1
end
-- 未被点赞，添加点赞数据
redis.call('R.SETBIT', key, commentId, 1)
return 0