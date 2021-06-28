local redPacketNumKey = KEYS[1];
local redPacketAmountKey = KEYS[2];
local redPacketAmountValue = ARGV[1];

local result = redis.call('GET', redPacketNumKey)
redis.log(redis.LOG_WARNING,"红包还有=" ..result)
local result_1 = redis.call('DECR', redPacketNumKey)
if (result_1 == (result - 1)) then
    local result_3 = redis.call('GET', redPacketAmountKey)
    local result_2 = redis.call('DECRBY', redPacketAmountKey, redPacketAmountValue)
    if (result_2 == (result_3 - redPacketAmountValue)) then
        return true
    else
        return false
    end
else
    return false
end
