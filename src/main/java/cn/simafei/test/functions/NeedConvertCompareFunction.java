package cn.simafei.test.functions;

import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import org.testng.collections.Sets;

public class NeedConvertCompareFunction implements Function {

    @Override
    public String execute(String[] args) {
        boolean needConvertEqual = true;

        Map<String, Map<String, Object>> descMap = JSON.parseObject(args[0], Map.class);

        Set<String> needSet = Sets.newHashSet();

        Set<String> activeNeedSet = Sets.newHashSet();

        for (int i = 1; i < args.length; i++) {
            needSet.add(args[i]);
        }

        if (descMap != null && !descMap.isEmpty()) {
            descMap.keySet().stream().forEach(fieldKey -> {
                if (descMap.get(fieldKey).get("is_need_convert") != null && (boolean) descMap.get(fieldKey).get("is_need_convert")) {
                    needSet.remove(fieldKey);
                    activeNeedSet.add(fieldKey);
                }
            });
        }

        needConvertEqual = (needSet.size() == 0 && activeNeedSet.size() == (args.length - 1));

        if (needConvertEqual) {
            return "success";
        } else {
            return "fail";
        }
    }

    @Override
    public String getReferenceKey() {
        // TODO Auto-generated method stub
        return "needConvertCompare";
    }

}
