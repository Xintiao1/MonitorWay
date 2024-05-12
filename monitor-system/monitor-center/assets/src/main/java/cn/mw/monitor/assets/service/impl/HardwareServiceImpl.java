package cn.mw.monitor.assets.service.impl;

import cn.mwpaas.common.model.Reply;
import cn.joinhealth.monitor.assets.dto.ItemDTO;
import cn.joinhealth.monitor.assets.dto.ValueMapDTO;
import cn.mw.monitor.assets.oldService.Calculate;
import cn.mw.monitor.assets.oldService.CurItemDTO;
import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.assets.dao.MwDiskusageDao;
import cn.mw.monitor.assets.dto.MwDiskusageInfoDTO;
import cn.mw.monitor.assets.dto.MwPartitionInfoDTO;
import cn.mw.monitor.assets.model.MwDiskusage;
import cn.mw.monitor.assets.service.HardwareService;
import cn.mw.monitor.assets.oldService.SumItemDTO;
import cn.joinhealth.monitor.zbx.dto.ZbxGraphDTO;
import cn.joinhealth.monitor.zbx.servce.ZbxGraphService;
import cn.joinhealth.zbx.ZabbixAPIResult;
import cn.joinhealth.zbx.ZabbixApi;
import cn.mw.monitor.assets.dto.MwGroupHostDTO;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@Scope("prototype")
public class HardwareServiceImpl implements HardwareService {
    private String host;
    private String modelName;
    private String itemname;
    private String hostType;
    private List<ItemDTO> itemlist =new ArrayList<>();
    private static Map<String, Calculate> calItemMap;
    private static String matchstr="^([A-Za-z]+)\\(([A-Za-z]+)\\)$";
    private static Pattern pattern;

    @Autowired
    private ZabbixApi zabbixApi;

    @Autowired
    private ZbxGraphService zbxGraphService;

    @Resource
    MwDiskusageDao mwDiskusageDao;

    static{
        calItemMap = new HashMap<String, Calculate>();
        calItemMap.put("cur", new CurItemDTO());
        calItemMap.put("sum", new SumItemDTO());
        pattern = Pattern.compile(matchstr);
    }

    @Override
    public void init(Map<String, Object> params) {
        host = params.get("host").toString();
        modelName = params.get("itemkey").toString();
        itemname = params.get("itemname").toString();
        hostType = params.get("hostType").toString();
    }


    /**
     * 查询硬件信息
     * 1 查询数据,找到映射后的key值
     * 2 调用zabbix api, 发送拼装后的key值
     */
    public List<ItemDTO> getHardwareInfo() {
        ArrayList<String> itemkeyList = new ArrayList<String>();
        List<ItemDTO> ret = new ArrayList<ItemDTO>();

        if(null == modelName){
            return ret;
        }

        //解析请求参数
        String[] modelNames = modelName.split(Constants.SPLIT_CODE);
        List<String> resolveNames = new ArrayList<String>();
        Map<String, String> calKeyMap = new HashMap<String, String>();
        StringBuilder sb = new StringBuilder();

        for(int i=0;i < modelNames.length; i++){
            String calKey = modelNames[i];
            Matcher m= pattern.matcher(calKey);
            if(m.find()){
                calKeyMap.put(m.group(2), m.group(1));
                sb.append(m.group(2));
                resolveNames.add(m.group(2));
            }else{
                sb.append(calKey);
                resolveNames.add(calKey);
            }
            if(i < modelNames.length - 1){
                sb.append(Constants.CONN_CODE);
            }
        }

        //查询键值映射表
        Reply reply = zbxGraphService.getZbxGraphRegexp(hostType, sb.toString());
        List<ZbxGraphDTO> list = (List<ZbxGraphDTO>) reply.getData();
        if (null == list){
           return ret;
        }
        for(ZbxGraphDTO zgdto : list){
            itemkeyList.add(zgdto.getItemName());
        }

        //根据拼装key,查询zabbix
        ZabbixAPIResult zabbixAPIResult = zabbixApi.itemGetByHostNameAndItemKey2(host, itemkeyList, true);
        String data = String.valueOf(zabbixAPIResult.getData());
        List<ItemDTO> itemlist = JSONArray.parseArray(data, ItemDTO.class);
        if (null == itemlist){
            return ret;
        }

        //根据modelname对返回数据进行分组
        Map<String, List<ItemDTO>> groupMap = new HashMap<>();
        for(ZbxGraphDTO zgdto : list){
            for(ItemDTO item : itemlist){
                if(item.getKey_().contains(zgdto.getItemName())){
                    List<ItemDTO> temp = groupMap.get(zgdto.getModelName());
                    if(null == temp){
                        temp = new ArrayList<ItemDTO>();
                        groupMap.put(zgdto.getModelName(), temp);
                    }
                    temp.add(item);
                }
            }
        }

        //根据valuemapid对返回数据进行分组
        Map<String, List<ItemDTO>> valueMap = new HashMap<>();
        List<String> valuemapids = new ArrayList<String>();
        for(ItemDTO item : itemlist){
            if(null != item.getValuemapid() &&!"".equals(item.getValuemapid()) && !"0".equals(item.getValuemapid())){
                List<ItemDTO> valuemapidList = valueMap.get(item.getValuemapid());
                if(null == valuemapidList){
                    valuemapidList = new ArrayList<ItemDTO>();
                    valueMap.put(item.getValuemapid(), valuemapidList);
                    valuemapids.add(item.getValuemapid());
                }
                valuemapidList.add(item);
            }
        }
        zabbixAPIResult = zabbixApi.valuemapid(valuemapids);
        if(null != zabbixAPIResult){
            data = String.valueOf(zabbixAPIResult.getData());
            List<ValueMapDTO> valueMapList = JSONArray.parseArray(data, ValueMapDTO.class);
            for(ValueMapDTO valueMapDTO : valueMapList){
                valueMapDTO.convertToMap();
                List<ItemDTO> templist = valueMap.get(valueMapDTO.getValuemapid());

                if(null != templist){
                    for(ItemDTO temp: templist){
                        String valueMapDes = valueMapDTO.getValuemap().get(temp.getLastvalue());
                        temp.setValuemapdesc(valueMapDes);
                    }
                }
            }
        }

        //计算返回值
        for(String key : resolveNames){
            String calkey = calKeyMap.get(key);
            List<ItemDTO> calvalue = groupMap.get(key);
            if(null != calkey){

                Calculate calculate = calItemMap.get(calkey);
                try {
                    List<ItemDTO> calResult = calculate.calculate(key, calvalue);
                    ret.addAll(calResult);
                } catch (Exception e) {
                    log.error("fail to calculate List<ItemDTO> with param={}, cause:{}", calvalue,e.getMessage());
                }
            }else{
                if(null != calvalue){
                    ret.addAll(calvalue);
                }
            }
        }
        return ret;
    }

    @Override
    public void getDiskInfoByGroup(List<String> groupid) {
        List<String>  selectInterfaces = new ArrayList<String>();
        selectInterfaces.add("ip");

        List<String>  output = new ArrayList<String>();
        output.add("host");
        ZabbixAPIResult zabbixAPIResult = zabbixApi.hostGetByGroupId(groupid, selectInterfaces, output);
        String data = zabbixAPIResult.getData().toString();
        List<MwGroupHostDTO> hostlist = JSONArray.parseArray(data, MwGroupHostDTO.class);
        Map<String, MwGroupHostDTO> map = new HashMap<String, MwGroupHostDTO>();
        List<String> hostids = new ArrayList<String>();

        hostlist.forEach(value ->{
            map.put(value.getHostid(), value);
            hostids.add(value.getHostid());
        });

        List<String> output1 = new ArrayList<String>();
        output1.add("itemid");
        output1.add("hostid");
        output1.add("name");
        output1.add("units");
        output1.add("lastvalue");
        output1.add("description");

        zabbixAPIResult = zabbixApi.itemGetSortbyHostid(output1, hostids
                , "vfs.fs.size[", "itemid", output1);
        data = zabbixAPIResult.getData().toString();
        List<MwDiskusageInfoDTO> infos = JSONArray.parseArray(data, MwDiskusageInfoDTO.class);

        for(MwDiskusageInfoDTO mwDiskusageInfoDTO : infos){
            MwGroupHostDTO mwGroupHostDTO = map.get(mwDiskusageInfoDTO.getHostid());
            Map<String ,MwPartitionInfoDTO> partitionMap = mwGroupHostDTO.getPartitions();
            if(null == partitionMap){
                partitionMap = new HashMap<String ,MwPartitionInfoDTO>();
                mwGroupHostDTO.setPartitions(partitionMap);
            }
            if(null != mwGroupHostDTO){
                int index = mwDiskusageInfoDTO.getName().indexOf(":");
                String partition = mwDiskusageInfoDTO.getName().substring(0, index);
                MwPartitionInfoDTO mwPartitionInfoDTO = partitionMap.get(partition);
                if(null == mwPartitionInfoDTO){
                    mwPartitionInfoDTO = new MwPartitionInfoDTO();
                    mwPartitionInfoDTO.setPartition(partition);
                    mwPartitionInfoDTO.setUnit(mwDiskusageInfoDTO.getUnits());
                    partitionMap.put(partition, mwPartitionInfoDTO);
                }
                float free = -1;
                float total = -1;
                float used = -1;
                if(mwDiskusageInfoDTO.getName().indexOf("剩余磁盘容量") > 0){
                    free = convertFromByte(mwDiskusageInfoDTO.getLastvalue());
                    mwPartitionInfoDTO.setFree(free);
                    continue;
                }
                if(mwDiskusageInfoDTO.getName().indexOf("磁盘总容量") > 0){
                    total = convertFromByte(mwDiskusageInfoDTO.getLastvalue());
                    mwPartitionInfoDTO.setTotal(total);
                    continue;
                }
                if(mwDiskusageInfoDTO.getName().indexOf("已用磁盘容量") > 0){
                    used = convertFromByte(mwDiskusageInfoDTO.getLastvalue());
                    mwPartitionInfoDTO.setUsed(used);
                    continue;
                }
            }
        }

        List<MwDiskusage> list = new ArrayList<MwDiskusage>();
        map.forEach((hostid,value) ->{
            Map<String, MwPartitionInfoDTO> pmap = value.getPartitions();
            pmap.forEach((pkey, pvalue) ->{
                MwDiskusage mwDiskusage = new MwDiskusage();
                mwDiskusage.setIp(value.getInterfaces().get(0).getIp());
                mwDiskusage.setHostname(value.getHost());
                mwDiskusage.setPartition(pvalue.getPartition());
                mwDiskusage.setTotal(pvalue.getTotal());
                mwDiskusage.setFree(pvalue.getFree());
                mwDiskusage.setUsed(pvalue.getUsed());
                list.add(mwDiskusage);
            });

        });

        mwDiskusageDao.batchInsert(list);
        return;
    }

    private float convertFromByte(String byteStr){
        long size = Long.parseLong(byteStr);
        long ret = size / (1024 * 1024 * 1024);
        return ret;
    }
}
