local key = KEYS[1]

-- 批量添加数据的参数表
local zaddArgs = {}

-- 遍历 ARGV 参数，将分数和值按顺序插入到 zaddArgs 变量中
for i = 1, #ARGV - 1, 2 do
    table.insert(zaddArgs, ARGV[i])  -- 分数
    table.insert(zaddArgs, ARGV[i + 1])  -- 值
end

-- 调用 ZADD 批量插入数据
redis.call('ZADD', key, unpack(zaddArgs))

-- 设置过期时间
local expireTime = ARGV[#ARGV]  -- 最后一个参数为过期时间
redis.call('EXPIRE', key, expireTime)

return 0