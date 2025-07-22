--- 评论点赞

local key = KEYS[1]
local commentId = ARGV[1]

if redis.call('EXISTS', key) == 0 then
    return -1
end

-- 校验该评论是否被点赞过(1 表示已经点赞，0 表示未点赞)
local isLiked = redis.call('BF.EXISTS', key, commentId)
if isLiked == 1 then
    return 1
end
-- 未被点赞，添加点赞数据
redis.call('BF.ADD', KEY, commentId)
return 0