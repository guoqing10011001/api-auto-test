package cn.simafei.test.functions;

public class MinFunction implements Function {

	@Override
	public String execute(String[] args) {
		return "我是最小的";
	}

	@Override
	public String getReferenceKey() {
		// TODO Auto-generated method stub
		return "min";
	}

}
