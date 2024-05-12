package cn.mw.monitor.script.param;

import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.state.DataType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author lumingming
 * @createTime 20230412 10:36
 * @description
 */

@ApiModel(value = "提交账号")
public class SynAccount  extends DataPermissionParam {

    private Integer id;
    /**
     * 账户名称
     */
    @ApiModelProperty("账户名称")
    @Size(max = 256, message = "账号名称最大长度不能超过256字符！")
    private String account;
    /**
     * 密码
     */
    @Size(max = 64, message = "密码最大长度不能超过64字符！")
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("引擎id")
    private String instanceId = "0";

    /**
     * port
     */
    @ApiModelProperty("访问url")
    private String url;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public DataType getBaseDataType() {
        return DataType.ACCOUNT_MANAGE;
    }

    @Override
    public String getBaseTypeId() {
        return id + "";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
