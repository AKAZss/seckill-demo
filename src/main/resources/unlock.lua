-- 获取锁的key值
local key = KEYS[1]
-- 当前线程表示
local threadId = AGRV[1]
-- 获取锁中的线程表示
local id = redis.call('get',key)
-- 比较是否一致
if(threadId == id) then
    -- 一致释放锁
    return redis.call('del',key)
end
return 0
