package cn.simafei.test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.DocumentException;
import org.testng.Assert;

import com.alibaba.fastjson.JSONPath;
import cn.simafei.test.beans.BaseBean;
import cn.simafei.test.utils.AssertUtil;
import cn.simafei.test.utils.ExcelUtil;
import cn.simafei.test.utils.FunctionUtil;
import cn.simafei.test.utils.ReportUtil;
import cn.simafei.test.utils.StringUtil;

public class TestBase {

    /**
     * 公共参数数据池（全局可用）
     */
    private static Map<String, String> saveData = new HashMap<>();

    private final static String argsSpit = "(?<!\\\\),";

    /**
     * 替换符，如果数据中包含“${}”则会被替换成公共参数中存储的数据
     */
    protected Pattern replaceParamPattern = Pattern.compile("\\$\\{(.*?)\\}");

    /**
     * 截取自定义方法正则表达式：__xxx(ooo)\\u4E00-\\u9FA5支持中文
     */
    protected Pattern funPattern = Pattern
            .compile("__(\\w*?)\\((([^)]*,?)*)\\)");//__(\\w*?)\\((([\\w\\u4E00-\\u9FA5\\-\\\\\\/:\\.\\$]*,?)*)\\)

    protected void setSaveDates(Map<String, String> map) {
        saveData.putAll(map);
    }

    /**
     * 组件预参数（处理__fucn()以及${xxxx}）
     *
     * @return
     */
    protected String buildParam(String param) {
        // 处理${}
        param = getCommonParam(param);
        // Pattern pattern = Pattern.compile("__(.*?)\\(.*\\)");// 取__开头的函数正则表达式
        // Pattern pattern =
        // Pattern.compile("__(\\w*?)\\((\\w*,)*(\\w*)*\\)");// 取__开头的函数正则表达式
        Matcher m = funPattern.matcher(param);
        while (m.find()) {
            String funcName = m.group(1);
            String args = m.group(2);
            String value;
            // bodyfile属于特殊情况，不进行匹配，在post请求的时候进行处理
            if (FunctionUtil.isFunction(funcName) && !funcName.equals("bodyfile")) {
                // 属于函数助手，调用那个函数助手获取。
                //value = FunctionUtil.getValue(funcName, args.split(","));
                value = FunctionUtil.getValue(funcName, args.split(argsSpit));
                // 解析对应的函数失败
                Assert.assertNotNull(value, String.format("解析函数失败：%s。", funcName));
                param = StringUtil.replaceFirst(param, m.group(), value);
            }
        }
        return param;
    }

    protected void savePreParam(String preParam) {
        // 通过';'分隔，将参数加入公共参数map中
        if (StringUtil.isEmpty(preParam)) {
            return;
        }
        String[] preParamArr = preParam.split(";");
        String key, value;
        for (String prepare : preParamArr) {
            if (StringUtil.isEmpty(prepare)) {
                continue;
            }
            key = prepare.split("=")[0];
            value = prepare.split("=")[1];
            ReportUtil.log(String.format("存储%s参数，值为：%s。", key, value));
            saveData.put(key, value);
        }
    }

    /**
     * 取公共参数 并替换参数
     */
    protected String getCommonParam(String param) {
        if (StringUtil.isEmpty(param)) {
            return "";
        }
        Matcher m = replaceParamPattern.matcher(param);// 取公共参数正则
        while (m.find()) {
            String replaceKey = m.group(1);
            String value;
            // 从公共参数池中获取值
            value = getSaveData(replaceKey);
            // 如果公共参数池中未能找到对应的值，该用例失败。
            Assert.assertNotNull(value,
                    String.format("格式化参数失败，公共参数中找不到%s。", replaceKey));
            param = param.replace(m.group(), value);
        }
        return param;
    }

    /**
     * 获取公共数据池中的数据
     *
     * @param key 公共数据的key
     * @return 对应的value
     */
    protected String getSaveData(String key) {
        if ("".equals(key) || !saveData.containsKey(key)) {
            return null;
        } else {
            return saveData.get(key);
        }
    }

    protected void verifyResult(String sourceData, String verifyStr, boolean contains) {
        if (StringUtil.isEmpty(verifyStr)) {
            return;
        }
        String allVerify = getCommonParam(verifyStr);
        ReportUtil.log("验证数据：" + allVerify);
        if (contains) {
            // 验证结果包含
            AssertUtil.contains(sourceData, allVerify);
        } else {
            // 通过';'分隔，通过jsonPath进行一一校验
            Pattern pattern = Pattern.compile("([^;]*)=([^;]*)");
            Assert.assertTrue(pattern.matcher(allVerify.trim()).find(), "没有找到合法格式的校验内容，合法格式：xx1=oo1;xx2=oo2;xx3=oo3");
            Matcher m = pattern.matcher(allVerify.trim());
            while (m.find()) {
                String actualValue = getBuildValue(sourceData, m.group(1));
                String exceptValue = getBuildValue(sourceData, m.group(2));
                ReportUtil.log(String.format("验证转换后的值%s=%s", actualValue, exceptValue));
                Assert.assertEquals(actualValue, exceptValue, "验证预期结果失败。");
            }
        }
    }

    /**
     * 获取格式化后的值
     */
    private String getBuildValue(String sourceJson, String key) {
        key = key.trim();
        Matcher funMatch = funPattern.matcher(key);
        if (key.startsWith("$.") || key.startsWith("$[")) {// jsonpath
            key = JSONPath.read(sourceJson, key).toString();
        } else if (funMatch.find()) {
            String args = funMatch.group(2);
            String[] argArr = args.split(argsSpit);
            for (int index = 0; index < argArr.length; index++) {
                String arg = argArr[index];
                if (arg.startsWith("$.") || arg.startsWith("$[")) {
                    argArr[index] = JSONPath.read(sourceJson, arg).toString();
                }
            }
            String value = FunctionUtil.getValue(funMatch.group(1), argArr);
            key = StringUtil.replaceFirst(key, funMatch.group(), value);

        }
        return key.trim();
    }

    /**
     * 提取json串中的值保存至公共池中
     *
     * @param json    将被提取的json串。
     * @param allSave 所有将被保存的数据：xx=$.jsonpath.xx;oo=$.jsonpath.oo，将$.jsonpath.
     *                xx提取出来的值存放至公共池的xx中，将$.jsonpath.oo提取出来的值存放至公共池的oo中
     */
    protected void saveResult(String json, String allSave) {
        if (null == json || "".equals(json) || null == allSave
                || "".equals(allSave)) {
            return;
        }
        allSave = getCommonParam(allSave);
        String[] saves = allSave.split(";");
        String key, value;
        for (String save : saves) {
            Pattern pattern = Pattern.compile("([^;=]*)=([^;]*)");
            Matcher m = pattern.matcher(save.trim());
            while (m.find()) {
                key = getBuildValue(json, m.group(1));
                value = getBuildValue(json, m.group(2));
                ReportUtil.log(String.format("存储公共参数   %s值为：%s.", key, value));
                saveData.put(key, value);
            }
        }
    }

    protected <T extends BaseBean> List<T> readExcelData(Class<T> clz,
                                                         String[] excelPathArr, String[] sheetNameArr)
            throws DocumentException {
        List<T> allExcelData = new ArrayList<T>();

        List<T> temArrayList = new ArrayList<T>();
        for (String excelPath : excelPathArr) {
            String filePath = Paths.get(System.getProperty("user.dir"), excelPath).toString();
            temArrayList.clear();
            if (sheetNameArr.length == 0 || StringUtil.isEmpty(sheetNameArr[0])) {
                temArrayList.addAll(ExcelUtil.readExcel(clz, filePath));
            } else {
                for (String sheetName : sheetNameArr) {
                    temArrayList.addAll(ExcelUtil.readExcel(clz, filePath, sheetName));
                }
            }
            temArrayList.forEach((bean) -> {
                bean.setExcelName(excelPath);
            });
            allExcelData.addAll(temArrayList);
        }
        return allExcelData;
    }
}
