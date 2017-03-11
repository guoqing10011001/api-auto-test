package cn.simafei.test.beans;

/**
 * for create excel
 */
public class ApiDataExcelBean {
    private String run;
    private String caseScene;  //用例场景
    private String interfaceDesc; //接口描述
    private String method;
    private String url;
    private String param;
    private String verify;
    private String contains;
    private String status;
    private String save;

    public String getRun() {
        return run;
    }

    public void setRun(String run) {
        this.run = run;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getContains() {
        return contains;
    }

    public void setContains(String contains) {
        this.contains = contains;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVerify() {
        return verify;
    }

    public void setVerify(String verify) {
        this.verify = verify;
    }

    public String getSave() {
        return save;
    }

    public void setSave(String save) {
        this.save = save;
    }

    public String getCaseScene() {
        return caseScene;
    }

    public void setCaseScene(String caseScene) {
        this.caseScene = caseScene;
    }

    public String getInterfaceDesc() {
        return interfaceDesc;
    }

    public void setInterfaceDesc(String interfaceDesc) {
        this.interfaceDesc = interfaceDesc;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ApiDataExcelBean{");
        sb.append("run='").append(run).append('\'');
        sb.append(", caseScene='").append(caseScene).append('\'');
        sb.append(", interfaceDesc='").append(interfaceDesc).append('\'');
        sb.append(", method='").append(method).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", param='").append(param).append('\'');
        sb.append(", verify='").append(verify).append('\'');
        sb.append(", contains='").append(contains).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", save='").append(save).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
