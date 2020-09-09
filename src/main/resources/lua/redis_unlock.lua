-- 分布式锁解锁
-- lua 下标从 1 开始 , 0：失败 1：成功
local key = KEYS[1]
local value = ARGV[1]
if redis.call('get', key) == value
then
    return redis.call('del', key)
else
    return 0
end