package cn.simafei.test.functions;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyApinameCompareFunction implements Function{

	@Override
	public String execute(String[] args) {
		boolean keyEqualApiname =true;
		Map<String,Map<String,Object>> descMap = JSON.parseObject(args[0], Map.class);
		
        List<String> noEqualField = new ArrayList<>();
        
        List<String> needConvertField = new ArrayList<>();
        
        if (descMap != null && !descMap.isEmpty()) {
            descMap.keySet().stream().forEach(fieldKey -> {
                if(!fieldKey.equals(descMap.get(fieldKey).get("api_name"))) {
                    noEqualField.add(fieldKey);
                }
                
                if (descMap.get(fieldKey).get("is_need_convert") != null && (boolean)descMap.get(fieldKey).get("is_need_convert")) {
                    needConvertField.add(fieldKey);
                }
            });
        }
        
        keyEqualApiname = (noEqualField.size() == 0 ? true : false);
		
		if(keyEqualApiname){
			return "success";
		}else{
			return "fail";				
		}
	}

	@Override
	public String getReferenceKey() {
		// TODO Auto-generated method stub
		return "keyApiCompare";
	}

}
