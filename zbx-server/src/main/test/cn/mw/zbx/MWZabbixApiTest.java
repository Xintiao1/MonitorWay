package cn.mw.zbx;


import cn.joinhealth.zbx.ZabbixAPIResult;
import cn.joinhealth.zbx.ZabbixApi;
import cn.joinhealth.zbx.enums.action.ProxyDto;
import cn.mw.zbx.dto.MWAlertParamDto;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MWZabbixApiTest {
    MWZabbixApi zabbixApi;
    ZabbixApi zabbixApi1;
    static final String Zabbix_API_URL = "http://59.54.91.5:8001/device-config";
    static final String Zabbix_Version = "4.2.0";
    static final String Zabbix_User = "admin";
    static final String Zabbix_Password = "zabbix";
    static final String Zabbix_Password_Wrong = "zabbix3";
    static final String Zabbix_Test_Host_Group_One = "testgroup1";
    static final String Zabbix_Test_Host_Group_Two = "testgroup2";
    static final String Zabbix_Test_Host_One = "testhost1";
    static final String Zabbix_Test_Host_Two = "testhost2";
    static final String Zabbix_Test_Item_One_Key = "testItemKey1";
    static final String Zabbix_Test_Item_Two_Key = "testItemKey2";

    @Before
    public void before() {
        //zabbixApi = new MWZabbixApi(Zabbix_API_URL);
        zabbixApi.init();
    }

    @After
    public void after() {
        zabbixApi.destroy();
    }


    @Test
    public void getHostState(){
        ArrayList<String> hostNameList=new ArrayList<>();
        hostNameList.add("10.200.7.251_TSJJ_DMZ_SW.taison.cn");
        hostNameList.add("Zabbix server");
        MWZabbixAPIResult hostListGetResult = zabbixApi.hostListGetByHostName(hostNameList);
        ////System.out.println("====================");
        ////System.out.println(hostListGetResult.getData());

    }
//
//    @Test
//    public void updateHostState(){
//        ZabbixAPIResult zabbixAPIResult = zabbixApi.hostUpdate("10223", "1");
//        ////System.out.println("====================");
//        ////System.out.println(zabbixAPIResult.getData());
//    }
//
//    @Test
//    public void getHostTrigger(){
//        ArrayList<String> hostNameList=new ArrayList<>();
//        hostNameList.add("10223");
//        ZabbixAPIResult hostListGetResult = zabbixApi.currAlert(hostNameList);
//        ////System.out.println("====================");
//        ////System.out.println(hostListGetResult.getData());
//    }
//
//    @Test
//    public void getHostItem(){
//        ZabbixAPIResult hostListGetResult = zabbixApi.itemgetbyhostid("10223");
//        ////System.out.println("====================");
//        ////System.out.println(hostListGetResult.getData());
//    }
//
//
//    @Test
//    public void getHostAlert(){
//        Date date=new Date();
//        long time=date.getTime();
//        long time1=1000l;
//        ZabbixAPIResult hostListGetResult = zabbixApi.alertget("10223",null,time1,time,null,null,null);
//        ////System.out.println("====================");
//        ////System.out.println(hostListGetResult.getData());
//    }

    @Test
    public void actiongethost() {
        MWZabbixAPIResult hostListGetResult = zabbixApi.hostGetById("10223");
        ////System.out.println("====================");
        ////System.out.println(hostListGetResult.getData());
    }


//    @Test
//    public void alertGetByCurrent() {
//        MWZabbixAPIResult zabbixAPIResult = zabbixApi.alertGetByCurrent();
//        ////System.out.println("====================");
//        ////System.out.println(zabbixAPIResult.getData());
//    }


//    @Test
//    public void eventGettByTriggers2() {
//        ArrayList<String> list = new ArrayList<>();
//        list.add("16314");
//        list.add("16442");
//        list.add("16454");
//        list.add("16422");
//        list.add("16044");
//        MWAlertParamDto alertParamDto = new MWAlertParamDto();
//        MWZabbixAPIResult zabbixAPIResult = zabbixApi.eventGettByTriggers21(list);
//        ////System.out.println("====================");
//        ////System.out.println(zabbixAPIResult.getData());
//    }
//
//
//    @Test
//    public void alertget() {
//        MWZabbixAPIResult zabbixAPIResult = zabbixApi.alertget();
//        ////System.out.println("====================");
//        ////System.out.println(zabbixAPIResult.getData());
//    }
//
//    @Test
//    public void eventgetlist() {
//        ArrayList<String> list = new ArrayList<>();
//        list.add("1752532");
//        list.add("1752655");
//        MWZabbixAPIResult zabbixAPIResult = zabbixApi.eventgetlist(list);
//        ////System.out.println("====================");
//        ////System.out.println(zabbixAPIResult.getData());
//    }

    @Test
    public void eventGetByEventid() {
        MWZabbixAPIResult zabbixAPIResult = zabbixApi.eventGetByEventid("1752655");
        ////System.out.println("====================");
        ////System.out.println(zabbixAPIResult.getData());
    }


    @Test
    public void itemgetbyhostidList() {
        ArrayList<String> list = new ArrayList<>();
        list.add("10223");
        MWZabbixAPIResult zabbixAPIResult = zabbixApi.itemgetbyhostidList(list);
        ////System.out.println("====================");
        ////System.out.println(zabbixAPIResult.getData());
    }


    @Test
    public void actionGetByEventSourceTrigger() {
        ArrayList<String> list = new ArrayList<>();
        list.add("10223");
        MWZabbixAPIResult zabbixAPIResult = zabbixApi.actionGetByEventSourceTrigger("", "");
        ////System.out.println("====================");
        ////System.out.println(zabbixAPIResult.getData());
    }

    @Test
    public void actionDelete() {
        List<String> a = new ArrayList<>();
        a.add("12");
        MWZabbixAPIResult zabbixAPIResult = zabbixApi.actionDelete(a);
        ////System.out.println("====================");
        ////System.out.println(zabbixAPIResult.getData());
    }

    @Test
    public void actionDelete1() {
        List<String> a = new ArrayList<>();
        a.add("12");
        ZabbixAPIResult zabbixAPIResult = zabbixApi1.actionDelete(a);
        ////System.out.println("====================");
        ////System.out.println(zabbixAPIResult.getData());
    }

    @Test
    public void triggerCreate() {
        String e = "({10.200.7.251_TSJJ_DMZ_SW.taison.cn:TemperatureState[2014].last(10m)}>10)";
        MWZabbixAPIResult zabbixAPIResult = zabbixApi.triggerCreate("描述1", e, "4");
        ////System.out.println("====================");
        ////System.out.println(zabbixAPIResult.getData());
    }

    @Test
    public void triggerdelete() {
        List<String> triggerid = new ArrayList<>();
//        for(int i=16636;i<16648;i++){
//            triggerid.add(String.valueOf(i)); 16651 16619
//        }
        triggerid.add("16619");
        triggerid.add("16651");

        MWZabbixAPIResult zabbixAPIResult = zabbixApi.triggerDelete(triggerid);
        ////System.out.println("====================");
        ////System.out.println(zabbixAPIResult.getData());
    }



    @Test
    public void actioncreate() {
        MWZabbixAPIResult zabbixAPIResult = zabbixApi.actioncreate("动作名称","1","1",null,null,0);
        ////System.out.println("====================");
        ////System.out.println(zabbixAPIResult.getData());
    }


    @Test
    public void triggerGetById() {
       // MWZabbixAPIResult zabbixAPIResult = zabbixApi.triggerGetById("10084");
        ////System.out.println("====================");
       // ////System.out.println(zabbixAPIResult.getData());
    }

    @Test
    public void actioncreate1() {
        Map<String, Object> filter = new HashMap<>();
        filter.put("evaltype",0);
        List<Map<String, Object>> conditionList = new ArrayList<>();
        HashMap conditions=new HashMap();
        conditions.put("conditiontype",2);
        conditions.put("value","16774");
        conditionList.add(conditions);
        filter.put("conditions", conditionList);

        //创建动触发操作
        List<Map<String, Object>> operations = new ArrayList<>();
        Map<String, Object> operation = new HashMap();
        operation.put("operationtype", 0);//操作的类型  0 - 发送信息;
        operation.put("esc_period", "0s"); //升级步骤的持续时间
        operation.put("esc_step_from", 1);//步骤开始升级
        operation.put("esc_step_to", 1);//步骤结束升级
//        Map opcommand = new HashMap();
//        opcommand.put("type", 0);//触发动作执行后自定义脚本执行
//        operation.put("opcommand", opcommand);

        //包含要发送消息的目标用户对象的数组
        List opmessage_usr = new ArrayList();
        Map userIdMap = new HashMap();
        userIdMap.put("userid",1);
        opmessage_usr.add(userIdMap);

        operation.put("opmessage_usr",opmessage_usr);
        operations.add(operation);
        MWZabbixAPIResult zabbixAPIResult = zabbixApi.actioncreate("22", "def_longData", "def_shortData", filter, operations);
        ////System.out.println("====================");
        ////System.out.println(zabbixAPIResult.getData());
    }

    @Test
    public void getProxyId(){
        MWZabbixAPIResult mwZabbixAPIResult = zabbixApi.proxyGetByServerIp("22.222.2.22", "8001", null);
        if(mwZabbixAPIResult==null){
            ////System.out.println("1234");
        }else {
            ////System.out.println(mwZabbixAPIResult.getData());
        }
    }

    @Test
    public void proxyCreate(){
        MWZabbixAPIResult proxy = zabbixApi.createProxy("192.168.75.105", "192.168.75.105", "6", "10052", "");
        ////System.out.println(proxy.getData());
    }
    @Test
    public void proxyUpdate(){
        MWZabbixAPIResult proxy = zabbixApi.updateProxy("10365","22.222.2.22","22.222.2.22","6","10053","");
        ////System.out.println(proxy.getData());
    }

    @Test
    public void proxyDel(){
        ArrayList<String> strings = new ArrayList<>();
        strings.add("10365");
        strings.add("10366");
        strings.add("10367");
        strings.add("10368");
        strings.add("10369");
        MWZabbixAPIResult mwZabbixAPIResult = zabbixApi.proxyDelete(strings);
        ////System.out.println(mwZabbixAPIResult.getData());
    }
}
