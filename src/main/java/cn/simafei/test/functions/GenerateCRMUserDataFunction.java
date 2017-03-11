package cn.simafei.test.functions;

import java.util.List;

import com.alibaba.fastjson.JSONPath;

public class GenerateCRMUserDataFunction implements Function{

	private String json="";

	@Override
	public String execute(String[] args) {
		String crmDefinedJson=args[0];
		List<Object> allUserFiles = (List<Object>)JSONPath.read(crmDefinedJson, "$.crmDefinedFieldList[fieldProperty=2][isNotNull=1]");
		json="";
		allUserFiles.forEach((userFile)->{
			String value="开平接口数据";
			String filedName = (String) JSONPath.eval(userFile, "$.fieldName");
			int fileType = (int) JSONPath.eval(userFile, "$.fieldType");
			switch (fileType) {
			//整数
			case 4:
				value ="9";
				break;
			//小数
			case 5:
			//金额
			case 6:
				value ="0.99";
				break;
			//单选
			case 8:
				value = (String)JSONPath.eval(userFile, "$.enumDetails[0].itemCode");
				break;
			//级联
			case 14:
				value = (String)JSONPath.eval(userFile, "$.enumDetails[0].itemCode");
				Object chilrdItem = JSONPath.eval(userFile, "$.enumDetails[0].children[0]");
				while(chilrdItem!=null){
					value=value+"/"+JSONPath.eval(chilrdItem, "$.itemCode");
					chilrdItem= JSONPath.eval(chilrdItem,"$.children[0]");
				}
				break;
			//电话
			case 18:
				value="13794961500";
				break;
				//邮箱
			case 19:
				value="chenwx@fxiaoke.com";
				break;
			default:
				break;
			}
			json = json.concat(String.format(",{\"fieldName\":\"%s\",\"openFieldValue\":{\"value\":\"%s\"}}", filedName,value));
		});
		//return json.replaceFirst(",", "");
		return json;
	}

	@Override
	public String getReferenceKey() {
		// TODO Auto-generated method stub
		return "generateCrmUserData";
	}

}
