package org.example.irpc.framework.core.serialize.fastjson;

import com.alibaba.fastjson.JSON;
import org.example.irpc.framework.core.serialize.SerializeFactory;

/**
 * @Author liutingfeng
 * @Date 2023/4/18 5:14 PM
 */
public class FastJsonSerializeFactory implements SerializeFactory {
    @Override
    public <T> byte[] serialize(T t) {
        String jsonString = JSON.toJSONString(t);
        return jsonString.getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return JSON.parseObject(data, clazz);
    }
}
