package cn.simafei.test.functions;

import cn.simafei.test.utils.PayMd5Util;

public class PayMd5Function implements Function{

	@Override
	public String execute(String[] args) {
		return PayMd5Util.MD5Encode(args[0]);
	}

	@Override
	public String getReferenceKey() {
		// TODO Auto-generated method stub
		return "payMd5";
	}

}
