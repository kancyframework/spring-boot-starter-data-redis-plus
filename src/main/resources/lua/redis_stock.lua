-- 库存操作
-- lua 下标从 1 开始 , 0：失败 1：成功
-- key
local key = KEYS[1]
-- 初始库存
local stock = tonumber(ARGV[1])
-- 超时时间
local expire = tonumber(ARGV[2])
-- 设置
local resNx = tonumber(redis.call('setnx', key, stock))
if resNx == 1 then
    if expire > 0 then
        redis.call('expire', key, expire)
        return 1;
    end
else
    local currentStock = tonumber(redis.call('get', key))
    if currentStock > 0 then
        redis.call('decrby', key, 1)
        return 1;
    else
        return 0;
    end
end

