package cn.simafei.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;

import cn.simafei.test.utils.*;
import com.alibaba.fastjson.JSON;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.dom4j.DocumentException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import cn.simafei.test.beans.ApiDataBean;
import cn.simafei.test.config.ApiConfig;
import cn.simafei.test.exceptions.ErrorRespStatusException;
import cn.simafei.test.listeners.AutoTestListener;
import cn.simafei.test.listeners.RetryListener;

@Listeners({AutoTestListener.class, RetryListener.class})
public class ApiTest extends TestBase {

    /**
     * api请求跟路径
     */
    private static String rootUrl;

    /**
     * 跟路径是否以‘/’结尾
     */
    private static boolean rooUrlEndWithSlash = false;

    /**
     * 所有公共header，会在发送请求的时候添加到http header上
     */
    private static Header[] publicHeaders;

    /**
     * 所有api测试用例数据
     */
    private static List<ApiDataBean> dataList = new ArrayList<>();

    private static HttpClient client;

    /**
     * 初始化测试数据
     */
    @Parameters("envName")
    @BeforeSuite
    public void init(@Optional("api-config-online") String envName) throws Exception {
        // api-config-online-crm
        String configFilePath = Paths.get(System.getProperty("user.dir"), "conf", "api", envName + ".xml").toString();
        ReportUtil.log("api config path:" + configFilePath);
        ApiConfig apiConfig = new ApiConfig(configFilePath);
        // 获取基础数据
        rootUrl = apiConfig.getRootUrl();
        rooUrlEndWithSlash = rootUrl.endsWith("/");

        // 读取 param，并将值保存到公共数据map
        Map<String, String> params = apiConfig.getParams();
        setSaveDates(params);

        List<Header> headers = new ArrayList<>();
        apiConfig.getHeaders().forEach((key, value) -> {
            Header header = new BasicHeader(key, value);
            headers.add(header);
        });
        publicHeaders = headers.toArray(new Header[headers.size()]);
        client = new SSLClient();
        client.getParams().setParameter(
                CoreConnectionPNames.CONNECTION_TIMEOUT, 60000); // 请求超时
        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000); // 读取超时
    }

    @Parameters({"excelName", "sheetName"})
    @BeforeTest
    public void readData(@Optional("") String excelName,
                         @Optional("") String sheetName) throws DocumentException {
        dataList = readExcelData(ApiDataBean.class, excelName.split(";"), sheetName.split(";"));
    }

    /**
     * 过滤数据，run标记为Y的执行。
     */
    @DataProvider(name = "apiData")
    public Iterator<Object[]> getApiData(ITestContext context)
            throws DocumentException {
        List<Object[]> dataProvider = new ArrayList<>();
        dataList.stream().filter(ApiDataBean::isRun).forEach(data -> dataProvider.add(new Object[]{data}));
        return dataProvider.iterator();
    }

    @Test(dataProvider = "apiData")
    public void apiTest(ApiDataBean apiDataBean) throws Exception {
        saveParam(apiDataBean);
        // 封装请求方法
        HttpUriRequest method = parseHttpRequest(apiDataBean);
        String responseData;
        try {
            // 执行
            HttpResponse response = client.execute(method);
            int responseStatus = response.getStatusLine().getStatusCode();
            if (StringUtil.isNotEmpty(apiDataBean.getStatus())) {
                Assert.assertEquals(responseStatus + "", apiDataBean.getStatus(), "返回状态码与预期不符合!");
            } else {
                // 非2开头状态码为异常请求，抛异常后会进行重跑
                if (200 > responseStatus || responseStatus >= 300) {
                    throw new ErrorRespStatusException("返回状态码异常：" + responseStatus);
                }
            }
            responseData = getResult(response);
        } finally {
            method.abort();
        }
        // 输出返回数据log
        ReportUtil.log("resp:" + responseData);
        // 验证预期信息
        verifyResult(responseData, apiDataBean.getVerify(), apiDataBean.isContains());

        // 对返回结果进行提取保存。
        saveResult(responseData, apiDataBean.getSave());
    }

    private void saveParam(ApiDataBean apiDataBean) {
        // 分析处理预参数 （函数生成的参数）
        String preParam = buildParam(apiDataBean.getPreParam());
        savePreParam(preParam);// 保存预存参数 用于后面接口参数中使用和接口返回验证中
    }

    private String getResult(HttpResponse response) throws IOException {
        HttpEntity respEntity = response.getEntity();
        Header respContentType = response.getFirstHeader("Content-Type");
        if (respContentType != null
                && respContentType.getValue().contains("download")) {
            String conDisposition = response.getFirstHeader(
                    "Content-disposition").getValue();
            String fileType = conDisposition.substring(
                    conDisposition.lastIndexOf("."),
                    conDisposition.length());
            String filePath = "download/" + RandomUtil.getRandom(8, false)
                    + fileType;
            InputStream is = response.getEntity().getContent();
            Assert.assertTrue(FileUtil.writeFile(is, filePath), "下载文件失败。");
            // 将下载文件的路径放到{"filePath":"xxxxx"}进行返回
            return "{\"filePath\":\"" + filePath + "\"}";
        } else {
            return EntityUtils.toString(respEntity);
        }
    }

    /**
     * 封装请求方法
     * @return 请求方法
     */
    private HttpUriRequest parseHttpRequest(ApiDataBean apiDataBean) throws UnsupportedEncodingException {
        // 处理url
        String url = parseUrl(apiDataBean.getUrl());
        String body = apiDataBean.getBody();
        String param = apiDataBean.getParam();
        String method = apiDataBean.getMethod();
        ReportUtil.log("method:" + method);
        ReportUtil.log("url:" + url);
        if (apiDataBean.isPostJson()) {
            ReportUtil.log("body:" + body.replace("\r\n", "").replace("\n", ""));
        } else {
            ReportUtil.log("param:" + param);
        }

        if ("post".equalsIgnoreCase(method)) {
            // 封装post方法
            HttpPost postMethod = new HttpPost(url);
            postMethod.setHeaders(publicHeaders);

            if (apiDataBean.isPostJson()) {
                HttpEntity entity = new StringEntity(buildParam(body), StandardCharsets.UTF_8);
                postMethod.setEntity(entity);
            } else if (param != null && !"".equals(param)) {
                List<NameValuePair> params = new ArrayList<>();
                for(String pair : param.split("&")) {
                    String[] nameValue = pair.split("=");
                    params.add(new BasicNameValuePair(nameValue[0], buildParam(nameValue[1])));
                }
                postMethod.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
            }
            return postMethod;
        } else if ("upload".equalsIgnoreCase(method)) {
            HttpPost postMethod = new HttpPost(url);
            @SuppressWarnings("unchecked")
            Map<String, String> paramMap = JSON.parseObject(param, HashMap.class);
            MultipartEntity entity = new MultipartEntity();
            for (String key : paramMap.keySet()) {
                String value = paramMap.get(key);
                Matcher m = funPattern.matcher(value);
                if (m.matches() && m.group(1).equals("bodyfile")) {
                    value = m.group(2);
                    entity.addPart(key, new FileBody(new File(value)));
                } else {
                    entity.addPart(key, new StringBody(paramMap.get(key)));
                }
            }
            postMethod.setEntity(entity);
            return postMethod;
        } else {
            // 封装get方法
            HttpGet getMethod = new HttpGet(url + "?" + param);
            getMethod.setHeaders(publicHeaders);
            return getMethod;
        }
    }

    /**
     * 格式化url,替换路径参数等。
     */
    private String parseUrl(String shortUrl) {
        // 替换url中的参数
        shortUrl = getCommonParam(shortUrl);
        if (shortUrl.startsWith("http")) {
            return shortUrl;
        }
        if (rooUrlEndWithSlash == shortUrl.startsWith("/")) {
            if (rooUrlEndWithSlash) {
                shortUrl = shortUrl.replaceFirst("/", "");
            } else {
                shortUrl = "/" + shortUrl;
            }
        }
        return rootUrl + shortUrl;
    }
}
