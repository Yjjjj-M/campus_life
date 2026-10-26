-- 秒杀 Lua 脚本
-- 功能：判断库存、判断重复购买、扣减库存、发送消息到 Stream
-- 全部操作在 Redis 中原子执行，保证高并发下的一致性

-- 参数
-- KEYS[1]: 库存 key，例如 seckill:stock:1
-- KEYS[2]: 订单 key，例如 seckill:order:1 (SET，存储已购买的 userId)
-- ARGV[1]: Stream 的 key，例如 stream:voucher:order
-- ARGV[2]: voucherId
-- ARGV[3]: userId

-- 1. 获取库存
local stock = tonumber(redis.call('get', KEYS[1]))
if (stock == nil or stock <= 0) then
    -- 库存不足
    return 1
end

-- 2. 判断用户是否已购买（一人一单）
local isMember = redis.call('sismember', KEYS[2], ARGV[3])
if (isMember == 1) then
    -- 重复购买
    return 2
end

-- 3. 扣减库存
redis.call('incrby', KEYS[1], -1)

-- 4. 记录用户已购买
redis.call('sadd', KEYS[2], ARGV[3])

-- 5. 发送消息到 Stream（异步下单）
redis.call('xadd', ARGV[1], '*', 'voucherId', ARGV[2], 'userId', ARGV[3])

-- 返回成功
return 0
