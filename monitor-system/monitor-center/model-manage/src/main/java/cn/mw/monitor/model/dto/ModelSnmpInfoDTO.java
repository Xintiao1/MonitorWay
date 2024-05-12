package cn.mw.monitor.model.dto;

import cn.mw.monitor.model.type.TableViewEnum;
import cn.mw.monitor.util.GzipTool;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class ModelSnmpInfoDTO {
    private String id;
    private String macInfo;
    private String arpInfo;
    private String interfaceInfo;
    private Date createTime;

    public void extractFrom(List<List<ModelTableViewInfo>> data){
        int i = 0;
        for(TableViewEnum tableViewEnum: TableViewEnum.values()) {
            List<ModelTableViewInfo> result = data.get(i);
            String json = GzipTool.gzip(JSON.toJSONString(result));
            switch (tableViewEnum){
                case ARP:
                    this.arpInfo = json;
                    break;
                case MAC:
                    this.macInfo = json;
                    break;
                case INTERFACE:
                    this.interfaceInfo = json;
            }
            i++;
        }
    }

    public List<ModelTableViewInfo> getSnmpInfo(TableViewEnum tableViewEnum){
        List<ModelTableViewInfo> ret = new ArrayList<>();
        String json = null;
        switch (tableViewEnum){
            case ARP:
                ret = doGetInfo(this.arpInfo);
                break;
            case MAC:
                ret = doGetInfo(this.macInfo);
                this.macInfo = json;
                break;
            case INTERFACE:
                ret = doGetInfo(this.interfaceInfo);
        }
        return ret;
    }

    public List<ModelTableViewInfo> getMac(){
        return doGetInfo(this.macInfo);
    }

    public List<ModelTableViewInfo> getInterface(){
        return doGetInfo(this.interfaceInfo);
    }

    public boolean isDataEmpty(){
        return null == this.arpInfo || StringUtils.isEmpty(this.arpInfo)
                || null == this.macInfo || StringUtils.isEmpty(this.macInfo)
                || null == this.interfaceInfo || StringUtils.isEmpty(this.interfaceInfo)
                ;
    }

    private List<ModelTableViewInfo> doGetInfo(String str){
        String result = GzipTool.gunzip(str);
        List<ModelTableViewInfo> resultList = JSON.parseObject(result, new TypeReference<List<ModelTableViewInfo>>() {
        }, Feature.AllowUnQuotedFieldNames);
        return resultList;
    }


}
