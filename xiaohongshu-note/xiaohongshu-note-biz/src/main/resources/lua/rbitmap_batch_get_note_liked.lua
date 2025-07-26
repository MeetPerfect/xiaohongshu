
-- LUA 脚本：批量从 Roaring Bitmap 获取笔记点赞状态

local key = KEYS[1]
-- 笔记是否被点赞结果
local results = {}

local exists = redis.call('EXISTS', key)
if exists == 0 then
    results[1] = -1  -- 标识 Roaring Bitmap 不存在
    return results
end
-- 循环获取笔记是否点赞，1表示已点赞，0表示未点赞
for i = 1, #ARGV  do
    results[i] = redis.call("R.GETBIT", key, ARGV[i])
end

return results