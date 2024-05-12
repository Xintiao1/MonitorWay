package cn.mw.zbx.manger;

import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDtoBySer;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.MWZabbixApi;
import cn.mw.zbx.dto.BaseDto;
import cn.mw.zbx.dto.MWWebDto;
import cn.mw.zbx.dto.MWWebReturnDto;
import cn.mw.zbx.dto.MWWebValue;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2020/4/25 15:40
 */
@Component
@Slf4j
public class MWWebZabbixManger {
    private static final Logger logger = LoggerFactory.getLogger(MWWebZabbixManger.class);

    //    @Autowired
    private MWZabbixApi zabbixApi;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @ApiOperation(value = "创建web检测")
    public String HttpTestCreate(MWWebDto webDto) {
        MWZabbixAPIResult mwZabbixAPIResult = zabbixApi.HttpTestCreate(webDto);
        if (mwZabbixAPIResult.getCode() == 0) {
            Map<String, String> map = (Map) mwZabbixAPIResult.getData();
            return map.get("httptestids");
        } else {
            return "";
        }

    }

    @ApiOperation(value = "更新web检测")
    public String HttpTestUpdate(MWWebDto webDto) {
        MWZabbixAPIResult mwZabbixAPIResult = zabbixApi.HttpTestUpdate(webDto);
        if (mwZabbixAPIResult.getCode() == 0) {
            return "更新成功";
        }
        return "更新失败";
    }

    @ApiOperation(value = "修改启用状态")
    public String HttpTestUpdate(Integer monitorServerId, String httptestid, Integer status) {
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HttpTestUpdate(monitorServerId, httptestid, status);
        if (mwZabbixAPIResult.getCode() == 0) {
            return "修改成功";
        }
        return "修改失败";
    }

    @ApiOperation(value = "删除web场景")
    public String HttpTestDelete(Integer monitorServerId, List<String> webids) {
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HttpTestDelete(monitorServerId, webids);
        if (mwZabbixAPIResult.getCode() == 0) {
            return "删除成功";
        }
        return "删除失败";
    }

    @ApiOperation(value = "根据httptestid获得创建的记录")
    public MWWebReturnDto HttpTestGet(String httptestid) {
        MWZabbixAPIResult mwZabbixAPIResult = zabbixApi.HttpTestGet(httptestid);
        MWWebReturnDto returnDto = new MWWebReturnDto();
        JsonNode resultData = (JsonNode) mwZabbixAPIResult.getData();
        if (resultData.size() > 0) {
            resultData.forEach(result -> {
                returnDto.setHttptestid(result.get("httptestids").asText());
                returnDto.setName(result.get("name").asText());
                returnDto.setApplicationid(result.get("applicationid").asText());
                returnDto.setNextcheck(result.get("nextcheck").asText());
                returnDto.setDelay(result.get("delay").asText());
                returnDto.setStatus(result.get("status").asText());
                returnDto.setAgent(result.get("agent").asText());
                returnDto.setHostid(result.get("hostid").asText());
                returnDto.setRetries(result.get("retries").asText());
            });
        }
        return returnDto;
    }

    //通过hostid和step name 获得itemid
 /*   public Map<String, Integer> getItemId(String hostid, String name) {
        String key_fail = "web.test.fail[" + name + "]";//步骤全部执行成功返回0 正常   1异常
        String key_bps = "web.test.in[" + name + "," + name + "," + "bps]";//下载速度
        String key_resp = "web.test.time[" + name + "," + name + "," + "resp]";//相应时间
        String key_respcode = "web.test.rspcode[" + name + "," + name + "]";//响应状态码
        Integer hostId = Integer.valueOf(hostid);
        Integer itemid_fail = mwZabbixDao.getItemidBykey(hostId, key_fail);
        Integer itemid_bps = mwZabbixDao.getItemidBykey(hostId, key_bps);
        Integer itemid_resp = mwZabbixDao.getItemidBykey(hostId, key_resp);
        Integer itemid_respcode = mwZabbixDao.getItemidBykey(hostId, key_respcode);
        HashMap<String, Integer> map = new HashMap();
        map.put(key_fail, itemid_fail);
        map.put(key_bps, itemid_bps);
        map.put(key_resp, itemid_resp);
        map.put(key_respcode, itemid_respcode);
        return map;
    }*/

    //通过hostid和step name 获得itemid
    public Map<String, String> getItemId(Integer monitorServerId, String hostid, String name) {
        // String key_fail = "web.test.fail[" + name + "]";//步骤全部执行成功返回0 正常   1异常
        String key_bps = "web.test.in[" + name + "," + name + "," + "bps]";//下载速度
        String key_resp = "web.test.time[" + name + "," + name + "," + "resp]";//相应时间
        //   String key_respcode = "web.test.rspcode[" + name + "," + name + "]";//响应状态码
        ArrayList<String> hostId = new ArrayList<>();
        hostId.add(hostid);
        HashMap<String, String> map = new HashMap();
        MWZabbixAPIResult result = mwtpServerAPI.getWebItemByhostId(monitorServerId, hostId);
        if (result.getCode() == 0) {
            JsonNode jsonNode = (JsonNode) result.getData();
            if (jsonNode.size() > 0) {
                for (JsonNode node : jsonNode) {
                    String key = node.get("key_").asText();
                    String lastvalue = node.get("lastvalue").asText();
                    if (key.equals(key_bps)) {
                        map.put(key_bps, lastvalue);
                    } else if (key.equals(key_resp)) {
                        map.put(key_resp, lastvalue);
                    }
                }
            }
        }
        return map;
    }


//    @ApiOperation(value = "查询zabbix数据库获得web最新的一条历史记录")
//    //name是场景的名称
//    public MWWebValue HistoryGetByItemid(String hostid, String name) {
//        String key_fail = "web.test.fail[" + name + "]";//步骤全部执行成功返回0 正常   1异常
//        String key_error = "web.test.error[" + name + "]";//返回最后步骤失败的值
//        String key_bps = "web.test.in[" + name + "," + name + "," + "bps]";//下载速度
//        String key_resp = "web.test.time[" + name + "," + name + "," + "resp]";//相应时间
//        String key_respcode = "web.test.rspcode[" + name + "," + name + "]";//响应状态码
//        Integer hostId = Integer.valueOf(hostid);
//        Integer itemid_fail = mwZabbixDao.getItemidBykey(hostId, key_fail);
//        Integer itemid_error = mwZabbixDao.getItemidBykey(hostId, key_error);
//        Integer itemid_bps = mwZabbixDao.getItemidBykey(hostId, key_bps);
//        Integer itemid_resp = mwZabbixDao.getItemidBykey(hostId, key_resp);
//        Integer itemid_respcode = mwZabbixDao.getItemidBykey(hostId, key_respcode);
//
//        String itemid_fail_data = mwZabbixDao.getValueByItemid(itemid_fail);
//        String itemid_fail_error = mwZabbixDao.getValueByItemid(itemid_error);
//        String itemid_fail_bps = mwZabbixDao.getValueByItemid(itemid_bps);
//        String itemid_fail_resp = mwZabbixDao.getValueByItemid(itemid_resp);
//        String itemid_resp_code = mwZabbixDao.getValueByItemid(itemid_respcode);
//
//        MWWebValue mwWebValue = new MWWebValue();
//        if (itemid_fail_data != null && itemid_fail_data.equals("0")) {
//            mwWebValue.setState("正常");
//        } else {
//            mwWebValue.setState("异常");
//        }
//        if (itemid_fail_error != null) {
//            mwWebValue.setError(itemid_fail_error);
//        }
//        if (itemid_fail_bps != null) {
//            mwWebValue.setBps(itemid_fail_bps);
//        }
//        if (itemid_fail_resp != null) {
//            mwWebValue.setResp(itemid_fail_resp);
//        }
//        if (itemid_resp_code != null) {
//            mwWebValue.setRcode(itemid_resp_code);
//        }
//
//        return mwWebValue;
//
//    }
//
//    @ApiOperation(value = "使用history.get获得web最新的一条历史记录")
//    //name是场景的名称
//    public MWWebValue HistoryGetByHostidForZabbix(String hostid, String name) {
//        String key_fail = "web.test.fail[" + name + "]";//步骤全部执行成功返回0 正常   1异常
//        String key_bps = "web.test.in[" + name + "," + name + "," + "bps]";//下载速度
//        String key_resp = "web.test.time[" + name + "," + name + "," + "resp]";//相应时间
//        String key_respcode = "web.test.rspcode[" + name + "," + name + "]";//响应状态码
//        Integer hostId = Integer.valueOf(hostid);
//        Integer itemid_fail = mwZabbixDao.getItemidBykey(hostId, key_fail);
//        Integer itemid_bps = mwZabbixDao.getItemidBykey(hostId, key_bps);
//        Integer itemid_resp = mwZabbixDao.getItemidBykey(hostId, key_resp);
//        Integer itemid_respcode = mwZabbixDao.getItemidBykey(hostId, key_respcode);
//        MWWebValue mwWebValue = new MWWebValue();
//        MWZabbixAPIResult mwZabbixAPIResult0 = zabbixApi.HistoryGetByItemid(String.valueOf(itemid_fail), 3);
//        MWZabbixAPIResult mwZabbixAPIResult1 = zabbixApi.HistoryGetByItemid(String.valueOf(itemid_bps), 0);
//        MWZabbixAPIResult mwZabbixAPIResult2 = zabbixApi.HistoryGetByItemid(String.valueOf(itemid_resp), 0);
//        MWZabbixAPIResult mwZabbixAPIResult3 = zabbixApi.HistoryGetByItemid(String.valueOf(itemid_respcode), 3);
//        if (mwZabbixAPIResult0.getCode() == 0) {
//            if (getData(mwZabbixAPIResult0).equals("0")) {
//                mwWebValue.setState("正常");
//            } else {
//                mwWebValue.setState("异常");
//            }
//        }
//        if (mwZabbixAPIResult1.getCode() == 0) {
//            mwWebValue.setBps(getData(mwZabbixAPIResult1));
//        }
//        if (mwZabbixAPIResult2.getCode() == 0) {
//            mwWebValue.setResp(getData(mwZabbixAPIResult2));
//
//        }
//        if (mwZabbixAPIResult3.getCode() == 0) {
//            mwWebValue.setRcode(getData(mwZabbixAPIResult3));
//        }
//        return mwWebValue;
//    }

    @ApiOperation(value = "使用history.get获得web最新的一条历史记录")
    public MWWebValue getByItemidForZabbix(Map<String, String> map, String name) {
        String key_fail = "web.test.fail[" + name + "]";//步骤全部执行成功返回0 正常   1异常
        String key_bps = "web.test.in[" + name + "," + name + "," + "bps]";//下载速度
        String key_resp = "web.test.time[" + name + "," + name + "," + "resp]";//相应时间
        String key_respcode = "web.test.rspcode[" + name + "," + name + "]";//响应状态码
        MWWebValue mwWebValue = new MWWebValue();
        MWZabbixAPIResult mwZabbixAPIResult0 = zabbixApi.HistoryGetByItemid(map.get(key_fail), 3);
        MWZabbixAPIResult mwZabbixAPIResult1 = zabbixApi.HistoryGetByItemid(map.get(key_bps), 0);
        MWZabbixAPIResult mwZabbixAPIResult2 = zabbixApi.HistoryGetByItemid(map.get(key_resp), 0);
        MWZabbixAPIResult mwZabbixAPIResult3 = zabbixApi.HistoryGetByItemid(map.get(key_respcode), 3);
        if (mwZabbixAPIResult0.getCode() == 0) {
            String data = getData(mwZabbixAPIResult0);
            if (null != data) {
                if (data.equals("0")) {
                    mwWebValue.setState("正常");
                } else {
                    mwWebValue.setState("异常");
                }
            }
        }
        if (mwZabbixAPIResult1.getCode() == 0) {
            mwWebValue.setBps(getData(mwZabbixAPIResult1));
        }
        if (mwZabbixAPIResult2.getCode() == 0) {
            mwWebValue.setResp(getData(mwZabbixAPIResult2));

        }
        if (mwZabbixAPIResult3.getCode() == 0) {
            mwWebValue.setRcode(getData(mwZabbixAPIResult3));
        }
        return mwWebValue;
    }


    public String getData(MWZabbixAPIResult result) {
        JsonNode node = (JsonNode) result.getData();
        String value = null;
        if (node.size() > 0) {
            value = node.get(0).get("value").asText();
        }
        return value;
    }

    public String getTemplateId(MWZabbixAPIResult result) {
        String templateId = "";
        if (result.getCode() == 0) {
            JsonNode node = (JsonNode) result.getData();
            if (node.size() > 0) {
                templateId = node.get(0).get("templateid").asText();
            }
        }
        return templateId;
    }


    @ApiOperation(value = "根据时间查询所有历史   下载记录和响应时间的数据")
    public List<MWItemHistoryDto> HistoryGetByTime(Integer monitorServerId, String itemids, long time_from, long time_till) {
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByTime(monitorServerId, itemids, time_from, time_till);
        List<MWItemHistoryDto> list = new ArrayList<>();
        if (mwZabbixAPIResult.getCode() == 0) {
            JsonNode resultData = (JsonNode) mwZabbixAPIResult.getData();
            if (resultData.size() > 0) {
                resultData.forEach(result -> {
                    MWItemHistoryDto returnDto = new MWItemHistoryDto();
                    returnDto.setItemid(result.get("itemid").asText());
                    returnDto.setClock(result.get("clock").asText());
                    returnDto.setValue(result.get("value").asText());
                    returnDto.setNs(result.get("ns").asText());
                    list.add(returnDto);
                });
            }
        }
        return list;
    }


    /**
     * 添加多线程查询
     * @param serverId
     * @param itemids
     * @param time_from
     * @param time_till
     * @param type
     * @param DataType
     * @return
     */
    @ApiOperation(value = "根据时间,history查询所有历史   下载记录和响应时间的数据")
    public List<MWItemHistoryDto> HistoryGetByTimeAndHistory(int serverId, List<String> itemids, long time_from, long time_till, Integer type, Integer DataType) {
        long time1 = System.currentTimeMillis();
        Date date = new Date();
        List<Long> timeList = new ArrayList<>();
        List<String> strList = new ArrayList<>();
        //时间类型为一周
        if (DataType == 3) {

            for (int i = 0; i <= 7; i++) {
                long time = DateUtils.addDays(date, (-i +1)).getTime() / 1000;
                String date1 = DateUtils.formatDate(DateUtils.addDays(date, (-i +1)));
                timeList.add(time);
                strList.add(date1);
            }
        }//时间类型为一个月
        else if (DataType == 4) {
            for (int i = 0; i <= 10; i++) {
                long time = DateUtils.addDays(date, (-(i * 3)+1)).getTime() / 1000;
                String date1 = DateUtils.formatDate(DateUtils.addDays(date, (-(i * 3)+1)));
                timeList.add(time);
                strList.add(date1);
            }
        } else {
            timeList.add(time_till);
            timeList.add(time_from);
        }
        int coreSizePool = Runtime.getRuntime().availableProcessors() * 2 + 1;
        logger.info("历史数据折线图线程数coreSizePool："+coreSizePool);
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(timeList.size(), timeList.size() + 2, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        List<Future<MWZabbixAPIResult>> futureList = new ArrayList<>();
        List<MWZabbixAPIResult> lists = new ArrayList<>();
        final Integer serverIds = serverId;
        final List<String>itemidList = itemids;
        final Integer types = type;
        for (int y = 0, len = timeList.size(); y < len - 1; y++) {
            final long timeStrat = timeList.get(y+1);
            final long timeEnd = timeList.get(y);

            Callable<MWZabbixAPIResult> callable = new Callable<MWZabbixAPIResult>() {
                @Override
                public MWZabbixAPIResult call() throws Exception {
                    ////System.out.println("多线程开始："+System.currentTimeMillis()+"ms");
                    MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByTimeAndTypeASC(serverIds, itemidList, timeStrat, timeEnd, types);
                    return mwZabbixAPIResult;
                }
            };
            ////System.out.println("多线程存值："+System.currentTimeMillis()+"ms");
            Future<MWZabbixAPIResult> submit = executorService.submit(callable);
            futureList.add(submit);
        }
        if (futureList.size() > 0) {
            futureList.forEach(f -> {
                try {
                    MWZabbixAPIResult result = f.get(200, TimeUnit.SECONDS);
                    lists.add(result);
                } catch (Exception e) {
                    log.error("fail to HistoryGetByTimeAndHistory with", e);
                }
            });
        }
        executorService.shutdown();
        logger.info("关闭线程池");
        long time2 = System.currentTimeMillis();
        List<MWItemHistoryDto> list = new ArrayList<>();

        for (MWZabbixAPIResult mwZabbixAPIResult1 : lists) {
            JsonNode resultData = (JsonNode) mwZabbixAPIResult1.getData();
            if (resultData.size() > 0) {
                resultData.forEach(result -> {
                    MWItemHistoryDto returnDto = new MWItemHistoryDto();
                    returnDto.setItemid(result.get("itemid").asText());
                    returnDto.setClock(result.get("clock").asText());
                    Double values = Double.valueOf(result.get("value").asText());
                    //如果double的值大于1000，为避免科学计数导致的数字精度丢失。
                    if (values > 1000.0) {
                        returnDto.setValue(String.valueOf(values.longValue()));
                    } else {
                        returnDto.setValue(String.valueOf(values));
                    }
                    returnDto.setDoubleValue(values);
                    returnDto.setDateTime(new Date(result.get("clock").asLong() * 1000));
                    returnDto.setLastValue(result.get("value").asLong());
                    returnDto.setNs(result.get("ns").asText());
                    list.add(returnDto);
                });
            }
        }
        long time3 = System.currentTimeMillis();
        logger.info("总耗时：" + (time2 - time1) + "ms"+"；数据总量："+list.size());
        logger.info("总耗时：" + (time2 - time1) + "ms"+"；数据详情："+list);
        ////System.out.println("zabbix服务获取数据耗时：" + (time2 - time1) + "ms" + "数据整理：" + (time3 - time2) + "ms" + "");
        List<MWItemHistoryDto> listSort = new ArrayList<>();
        listSort = list.stream().filter(item->item.getClock() != null).sorted(Comparator.comparing(MWItemHistoryDto::getClock)).collect(Collectors.toList());
        logger.info("总耗时：" + (time2 - time1) + "ms"+"；数据详情22："+listSort);
        return listSort;
    }


    /**
     * 原版方法
     * @param serverId
     * @param itemids
     * @param time_from
     * @param time_till
     * @param type
     * @return
     */
    @ApiOperation(value = "根据时间,history查询所有历史   下载记录和响应时间的数据")
    public List<MWItemHistoryDto> HistoryGetByTimeAndHistory22(int serverId, List<String> itemids, long time_from, long time_till, Integer type) {
        long time1 = System.currentTimeMillis();
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByTimeAndTypeASC(serverId, itemids, time_from, time_till, type);
        long time2 = System.currentTimeMillis();
        List<MWItemHistoryDto> list = new ArrayList<>();
        JsonNode resultData = (JsonNode) mwZabbixAPIResult.getData();
        if (resultData.size() > 0) {
            resultData.forEach(result -> {
                MWItemHistoryDto returnDto = new MWItemHistoryDto();
                returnDto.setItemid(result.get("itemid").asText());
                returnDto.setClock(result.get("clock").asText());
                Double values = Double.valueOf(result.get("value").asText());
                //如果double的值大于1000，为避免科学计数导致的数字精度丢失。
                if (values > 1000.0) {
                    returnDto.setValue(String.valueOf(values.longValue()));
                } else {
                    returnDto.setValue(String.valueOf(values));
                }
                returnDto.setDoubleValue(values);
                returnDto.setDateTime(new Date(result.get("clock").asLong() * 1000));
                returnDto.setLastValue(result.get("value").asLong());
                returnDto.setNs(result.get("ns").asText());
                list.add(returnDto);
            });
        }
        long time3 = System.currentTimeMillis();
        logger.info("总耗时：" + (time2 - time1) + "ms");
        ////System.out.println("zabbix服务获取数据耗时："+(time2 - time1) + "ms"+"数据整理："+(time3 - time2) + "ms"+"");
        return list;
    }

    @ApiOperation(value = "根据时间,history查询所有历史 lastValue为中文")
    public List<MWItemHistoryDto> HistoryGetByTimeAndHistoryCN(int serverId, List<String> itemids, long time_from, long time_till, Integer type) {
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByTimeAndType(serverId, itemids, time_from, time_till, type);
        List<MWItemHistoryDto> list = new ArrayList<>();
        JsonNode resultData = (JsonNode) mwZabbixAPIResult.getData();
        if (resultData.size() > 0) {
            resultData.forEach(result -> {
                MWItemHistoryDto returnDto = new MWItemHistoryDto();
                returnDto.setItemid(result.get("itemid").asText());
                returnDto.setClock(result.get("clock").asText());
                returnDto.setValue(result.get("value").asText());
                returnDto.setDateTime(new Date(result.get("clock").asLong() * 1000));
                returnDto.setLastValue(0l);
                returnDto.setNs(result.get("ns").asText());
                list.add(returnDto);
            });
        }
        return list;
    }


    @ApiOperation(value = "根据时间,history,items查询所有历史   下载记录和响应时间的数据")
    public List<MWItemHistoryDto> HistoryGetByTimeAndHistoryListByitem(int serverId, List<String> itemids, long time_from, long time_till, Integer type) {
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByTimeAndHistoryListByitem(serverId, itemids, time_from, time_till, type);
        List<MWItemHistoryDto> list = new ArrayList<>();
        JsonNode resultData = (JsonNode) mwZabbixAPIResult.getData();
        if (resultData.size() > 0) {
            resultData.forEach(result -> {
                MWItemHistoryDto returnDto = new MWItemHistoryDto();
                returnDto.setItemid(result.get("itemid").asText());
                returnDto.setClock(result.get("clock").asText());
                returnDto.setValue(result.get("value").asText());
                returnDto.setLastValue(result.get("value").asLong());
                returnDto.setNs(result.get("ns").asText());
                list.add(returnDto);
            });
        }
        return list;
    }

    @ApiOperation(value = "根据时间,history查询所有历史   下载记录和响应时间的数据")
    public List<MWItemHistoryDto> HistoryGetByTimeAndHistory(int serverId, String itemids, long time_from, long time_till, Integer type) {
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByTimeAndType(serverId, itemids, time_from, time_till, type);
        List<MWItemHistoryDto> list = new ArrayList<>();
        JsonNode resultData = (JsonNode) mwZabbixAPIResult.getData();
        if (resultData.size() > 0) {
            resultData.forEach(result -> {
                MWItemHistoryDto returnDto = new MWItemHistoryDto();
                returnDto.setItemid(result.get("itemid").asText());
                returnDto.setClock(result.get("clock").asText());
                returnDto.setValue(result.get("value").asText());
                returnDto.setLastValue(result.get("value").asLong());
                returnDto.setNs(result.get("ns").asText());
                list.add(returnDto);
            });
        }
        return list;
    }

    @ApiOperation(value = "根据时间,history查询所有历史   下载记录和响应时间的数据")
    public List<MWItemHistoryDtoBySer> HistoryGetByTimeAndHistorySer(int serverId, String itemids, long time_from, long time_till, Integer type) {
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByTimeAndType(serverId, itemids, time_from, time_till, type);
        List<MWItemHistoryDtoBySer> list = new ArrayList<>();
        JsonNode resultData = (JsonNode) mwZabbixAPIResult.getData();
        if (resultData.size() > 0) {
            resultData.forEach(result -> {
                MWItemHistoryDtoBySer returnDto = new MWItemHistoryDtoBySer();
                returnDto.setItemid(result.get("itemid").asText());
                returnDto.setClock(result.get("clock").asText());
                returnDto.setValue(result.get("value").asText());
                returnDto.setLastValue(result.get("value").asLong());
                returnDto.setNs(result.get("ns").asText());
                list.add(returnDto);
            });
        }
        return list;
    }

    @ApiOperation(value = "根据条数，查询对应条数的历史数据")
    public List<MWItemHistoryDto> HistoryGetByTimeAndHistory(int serverId, String itemids, Integer type, int limit) {
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByItemid(serverId, itemids, type, limit);
        List<MWItemHistoryDto> list = new ArrayList<>();
        JsonNode resultData = (JsonNode) mwZabbixAPIResult.getData();
        if (resultData.size() > 0) {
            resultData.forEach(result -> {
                MWItemHistoryDto returnDto = new MWItemHistoryDto();
                returnDto.setItemid(result.get("itemid").asText());
                returnDto.setClock(result.get("clock").asText());
                returnDto.setValue(result.get("value").asText());
                returnDto.setLastValue(result.get("value").asLong());
                returnDto.setNs(result.get("ns").asText());
                list.add(returnDto);
            });
        }
        return list;
    }

    @ApiOperation(value = "根据条数，查询对应条数的历史数据")
    public List<MWItemHistoryDto> HistoryGetByTimeAndHistory(int serverId, List<String> itemids, Integer type, int limit) {
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByItemids(serverId, itemids, type, limit);
        List<MWItemHistoryDto> list = new ArrayList<>();
        JsonNode resultData = (JsonNode) mwZabbixAPIResult.getData();
        if (resultData.size() > 0) {
            resultData.forEach(result -> {
                MWItemHistoryDto returnDto = new MWItemHistoryDto();
                returnDto.setItemid(result.get("itemid").asText());
                returnDto.setClock(result.get("clock").asText());
                returnDto.setValue(result.get("value").asText());
                returnDto.setLastValue(result.get("value").asLong());
                returnDto.setNs(result.get("ns").asText());
                list.add(returnDto);
            });
        }
        return list;
    }

    @ApiOperation(value = "根据时间,history查询所有历史   下载记录和响应时间的数据")
    public List<MWItemHistoryDto> HistoryGetByTimeAndItemIds(int serverId, List<String> itemids, long time_from, long time_till, Integer type) {
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByTimeAndType(serverId, itemids, time_from, time_till, type);
        List<MWItemHistoryDto> list = new ArrayList<>();
        JsonNode resultData = (JsonNode) mwZabbixAPIResult.getData();
        if (resultData.size() > 0) {
            resultData.forEach(result -> {
                MWItemHistoryDto returnDto = new MWItemHistoryDto();
                returnDto.setItemid(result.get("itemid").asText());
                returnDto.setClock(result.get("clock").asText());
                returnDto.setValue(result.get("value").asText());
                returnDto.setLastValue(result.get("value").asLong());
                returnDto.setNs(result.get("ns").asText());
                list.add(returnDto);
            });
        }
        return list;
    }

//    //从数据库中查询 响应时间的数据
//    public List<MWItemHistoryDto> HistoryGetByZabbix(String itemids, String time_from, String time_till) {
//        return mwZabbixDao.getValueByItemidAndClock(Integer.valueOf(itemids), Long.valueOf(time_from), Long.valueOf(time_till));
//    }

    @ApiOperation(value = "获取模板")
    public List templateGet(int monitorServerId, String name) {
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.templateGet(monitorServerId, name);
        log.info("获取模板数据结果"+mwZabbixAPIResult.getData()+new Date());
        List<BaseDto> nameList = new ArrayList<>();
        if(mwZabbixAPIResult.getCode() == 0){
            JsonNode map = (JsonNode) mwZabbixAPIResult.getData();
                if (map.size() > 0) {
                    map.forEach(data -> {
                        BaseDto baseDto = new BaseDto();
                        baseDto.setId(data.get("templateid").asText());
                        baseDto.setName(data.get("name").asText());
                        nameList.add(baseDto);
                    });
                }
        }

        return nameList;
    }

    @ApiOperation(value = "获取主机组")
    public List<BaseDto> hostGroupGet(String name, Boolean isFilter) {
        MWZabbixAPIResult mwZabbixAPIResult = zabbixApi.hostGroupGet(name, isFilter);
        JsonNode map = (JsonNode) mwZabbixAPIResult.getData();
        List<BaseDto> nameList = new ArrayList<>();
        if (mwZabbixAPIResult.getCode() == 0) {
            if (map.size() > 0) {
                map.forEach(data -> {
                    BaseDto baseDto = new BaseDto();
                    baseDto.setId(data.get("groupid").asText());
                    baseDto.setName(data.get("name").asText());
                    nameList.add(baseDto);
                });
            }
        }
        return nameList;
    }

    @ApiOperation(value = "通过discoveryId获取主机组中的主机数量")
    public int getGroupIdByDiscoveryId(int monitorServerId, String discoveryId) {
        String groupId = "";
        if (null != discoveryId && StringUtils.isNotEmpty(discoveryId)) {
            MWZabbixAPIResult hostprototype = mwtpServerAPI.getHostprototype(monitorServerId, discoveryId);
            JsonNode data = (JsonNode) hostprototype.getData();
            if (data.size() > 0) {
                JsonNode host = data.get(0);
                if (host.get("groupLinks").size() > 0) {
                    groupId = host.get("groupLinks").get(0).get("groupid").asText();
                }
            }
        }
        int hostNum = 0;
        if (null != groupId && StringUtils.isNotEmpty(groupId)) {
            MWZabbixAPIResult groupHosts = mwtpServerAPI.getGroupHosts(monitorServerId, groupId);
            JsonNode data = (JsonNode) groupHosts.getData();
            if (data.size() > 0) {
                hostNum = data.get(0).get("hosts").size();
            }
        }
        return hostNum;
    }

    public String selectItemId(Integer monitorServerId, List<String> hostids, String key) {
        String itemId = "";
        MWZabbixAPIResult result = mwtpServerAPI.getWebItemId(monitorServerId, hostids, key);
        if (result.getCode() == 0) {
            JsonNode map = (JsonNode) result.getData();
            if (map.size() == 1) {
                itemId = map.get(0).get("itemid").asText();
            }
        }
        return itemId;
    }

    public String selectDelay(Integer monitorServerId, List<String> hostids, String key) {
        String delay = "";
        MWZabbixAPIResult result = mwtpServerAPI.getWebItemId(monitorServerId, hostids, key);
        if (result.getCode() == 0) {
            JsonNode map = (JsonNode) result.getData();
            if (map.size() == 1) {
                delay = map.get(0).get("delay").asText();
            }
        }
        return delay;
    }

    public List<ItemApplication> getItemApplicationList(Integer monitorServerId, List<String> hostids, String key) {
        ItemApplication itemApplication = new ItemApplication();
        MWZabbixAPIResult result = mwtpServerAPI.getWebItemId(monitorServerId, hostids, key);
        if (result.getCode() == 0) {
            JsonNode map = (JsonNode) result.getData();
            if (map.size() == 1) {
                itemApplication.setDelay(map.get(0).get("delay").asText());
                itemApplication.setItemid(map.get(0).get("itemid").asText());
                itemApplication.setName(map.get(0).get("name").asText());
                itemApplication.setLastvalue(map.get(0).get("lastvalue").asText());
                itemApplication.setUnits(map.get(0).get("units").asText());
                itemApplication.setValue_type(map.get(0).get("value_type").asText());
                itemApplication.setState(map.get(0).get("state").asText());
                itemApplication.setValuemapid(map.get(0).get("valuemapid").asText());
            }
        }
        List<ItemApplication> itemApplications = new ArrayList<>();
        itemApplications.add(itemApplication);
        return itemApplications;
    }

    /**
     * 原版
     *
     * @param monitorServerId
     * @param hostIds
     * @param name
     * @return
     */
    public MWWebValue getWebValue(Integer monitorServerId, List<String> hostIds, String name) {
        MWZabbixAPIResult result = mwtpServerAPI.getWebValue(monitorServerId, hostIds, "web.test.");
        String keyFail = "web.test.fail[" + name + "]";//步骤全部执行成功返回0 正常   1异常
        String keyBps = "web.test.in[" + name + "," + name + "," + "bps]";//下载速度
        String keyResp = "web.test.time[" + name + "," + name + "," + "resp]";//相应时间
        String keyRespcode = "web.test.rspcode[" + name + "," + name + "]";//响应状态码
        MWWebValue mwWebValue = new MWWebValue();
        if (result.getCode() == 0) {
            JsonNode datas = (JsonNode) result.getData();
            if (datas.size() > 0) {
                datas.forEach(date -> {
                    String key = date.get("key_").asText();
                    String value = date.get("lastvalue").asText();
                    if (key.equals(keyFail)) {
                        if (value.equals("0")) {
                            mwWebValue.setState("正常");
                        } else {
                            mwWebValue.setState("异常");
                        }
                    } else if (key.equals(keyBps)) {
                        mwWebValue.setBps(value);

                    } else if (key.equals(keyResp)) {
                        mwWebValue.setResp(value);

                    } else if (key.equals(keyRespcode)) {
                        mwWebValue.setRcode(value);

                    }
                });
            }
        }
        return mwWebValue;
    }


    /**
     * 修改版
     *
     * @param monitorServerId
     * @param hostIds
     * @return
     */
    public Map<String, MWWebValue> getWebValue(Integer monitorServerId, List<String> hostIds) {
        Map<String, MWWebValue> map = new HashMap<>();
        MWZabbixAPIResult result = mwtpServerAPI.getWebValue(monitorServerId, hostIds, "web.test.");
        String keyFail = "web.test.fail[";//步骤全部执行成功返回0 正常   1异常
        String keyBps = "web.test.in[";//下载速度
        String keyResp = "web.test.time[";//相应时间
        String keyRespcode = "web.test.rspcode[";//响应状态码

        if (result.getCode() == 0) {
            JsonNode datas = (JsonNode) result.getData();
            if (datas.size() > 0) {
                datas.forEach(date -> {
                    String name = "";
                    String key = date.get("key_").asText();
                    int index1 = key.indexOf("[");
                    int index2 = key.indexOf(",");
                    int index3 = key.indexOf("]");
                    if (index1 != -1) {
                        if (index2 != -1) {
                            name = key.substring(index1 + 1, index2);
                        } else {
                            name = key.substring(index1 + 1, index3);
                        }
                    }
                    String value = date.get("lastvalue").asText();
                    String hostId = date.get("hostid").asText();

                    if (map.containsKey(hostId + "_" + name)) {
                        MWWebValue mwWebValue = map.get(hostId + "_" + name);
                        if (key.indexOf(keyFail) != -1) {
                            if (value.equals("0")) {
                                mwWebValue.setState("正常");
                            } else {
                                mwWebValue.setState("异常");
                            }
                        } else if (key.indexOf(keyBps) != -1) {
                            mwWebValue.setBps(value);
                        } else if (key.indexOf(keyResp) != -1) {
                            mwWebValue.setResp(value);
                        } else if (key.indexOf(keyRespcode) != -1) {
                            mwWebValue.setRcode(value);
                        }
                        map.put(hostId + "_" + name, mwWebValue);
                    } else {
                        MWWebValue mwWebValue = new MWWebValue();
                        if (key.indexOf(keyFail) != -1) {
                            if (value.equals("0")) {
                                mwWebValue.setState("正常");
                            } else {
                                mwWebValue.setState("异常");
                            }
                        } else if (key.indexOf(keyBps) != -1) {
                            mwWebValue.setBps(value);
                        } else if (key.indexOf(keyResp) != -1) {
                            mwWebValue.setResp(value);
                        } else if (key.indexOf(keyRespcode) != -1) {
                            mwWebValue.setRcode(value);
                        }
                        map.put(hostId + "_" + name, mwWebValue);
                    }
                });
            }
        }
        return map;
    }

}
