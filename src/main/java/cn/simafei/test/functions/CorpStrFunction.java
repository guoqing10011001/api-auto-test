package cn.simafei.test.functions;

import java.util.LinkedList;
import java.util.List;

import cn.simafei.test.utils.StringUtil;

/**
 * 根据提供的字符串生成为指定的长度的字符串
 * 第一个参数表示需要复制的次数。
 * 第二个参数表示被复制的字符串。
 * 第三个表示需要在每个字符串中添加的间隔符。默认为：,
 * 如：2,test,-  返回:test-test
 * 如：3,chenwx 返回：chenwx,chenwx,chemwx
 * @author chenwx
 *
 */
public class CorpStrFunction implements Function {

	@Override
	public String execute(String[] args) {
		int length = Integer.parseInt(args[0]);
		String toCopyValue = args[1];
		
		String joinStr = ",";
		if (args.length > 2 && StringUtil.isNotEmpty(args[2])) {
			joinStr = args[2];
		}
		List<String> list = new LinkedList<String>();
		for (int index = 0; index < length; index++) {
			list.add(toCopyValue);
		}
		String result = String.join(joinStr, list);
		return result;
	}

	@Override
	public String getReferenceKey() {
		return "corpStr";
	}

}
