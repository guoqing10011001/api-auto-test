package cn.simafei.test.utils;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

public class JsonUtil {
    public static Map<String, String> toMap(String json) {
        return JSON.parseObject(json, new TypeReference<Map<String, String>>() {
        });
    }
}
