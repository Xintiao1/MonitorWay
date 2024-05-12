package cn.mw.monitor.service.server.api.dto;

import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author syt
 * @Date 2021/4/13 9:45
 * @Version 1.0
 */

public class TypeFilterDTO {
    //主键自增id
    private int id;
    //资产主键id
    private String tangibleAssetsId;
    @ApiModelProperty("表示使用查询的分区监控项名称")
    private String nameType;
    //存储用来页面展示的数据
    private String showData;

    private List<Object> showList;

    @ApiModelProperty("应用集名称")
    private String applicationName;//应用集名称



    @ApiModelProperty("是否含有描述信息")
    private boolean hasDescription;
    //    第三方监控服务器id
    @ApiModelProperty("第三方监控服务器id")
    private int monitorServerId;
    //    第三方监控主机id
    @ApiModelProperty("第三方监控主机id")
    private String assetsId;
    @ApiModelProperty("是否增(create)，删(delete)，改(update)，查(select)")
    private String  operation;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTangibleAssetsId() {
        return tangibleAssetsId;
    }

    public void setTangibleAssetsId(String tangibleAssetsId) {
        this.tangibleAssetsId = tangibleAssetsId;
    }

    public String getNameType() {
        return nameType;
    }

    public void setNameType(String nameType) {
        this.nameType = nameType;
    }

    public String getShowData() {
        return showData;
    }

    public void setShowData(String showData) {
        this.showData = showData;
    }

    public List getShowList() {
        List result = new ArrayList();
        if(CollectionUtils.isNotEmpty(showList)){
             result = this.isHasDescription() ?
                    JSONObject.parseArray(showList.toString(),DropDownNamesDesc.class)
                    : JSONObject.parseArray(showList.toString(),String.class);
            result.removeAll(Collections.singleton(new Object()));
        }
        return result;
    }

    public void setShowList(List<Object> showList) {
        this.showList = showList;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public boolean isHasDescription() {
        return hasDescription;
    }

    public void setHasDescription(boolean hasDescription) {
        this.hasDescription = hasDescription;
    }

    public int getMonitorServerId() {
        return monitorServerId;
    }

    public void setMonitorServerId(int monitorServerId) {
        this.monitorServerId = monitorServerId;
    }

    public String getAssetsId() {
        return assetsId;
    }

    public void setAssetsId(String assetsId) {
        this.assetsId = assetsId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public TypeFilterDTO() {
    }

    public TypeFilterDTO(int id, String tangibleAssetsId, String nameType, String showData, List<Object> showList, String applicationName, boolean hasDescription, int monitorServerId, String assetsId, String operation) {
        this.id = id;
        this.tangibleAssetsId = tangibleAssetsId;
        this.nameType = nameType;
        this.showData = showData;
        this.showList = showList;
        this.applicationName = applicationName;
        this.hasDescription = hasDescription;
        this.monitorServerId = monitorServerId;
        this.assetsId = assetsId;
        this.operation = operation;
    }
}
