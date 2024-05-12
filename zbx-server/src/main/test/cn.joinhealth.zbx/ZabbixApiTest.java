package cn.joinhealth.zbx;



public class ZabbixApiTest {
  /*  ZabbixApi zabbixApi;
    static final String Zabbix_API_URL = "http://59.54.91.5:8001/api_jsonrpc.php";
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
        zabbixApi = new ZabbixApi(Zabbix_API_URL);
        zabbixApi.init();
    }

    @After
    public void after() {
        zabbixApi.destroy();
    }

    @Test
    public void getApp() {
        ZabbixAPIResult rest = zabbixApi.eventgethostid("10268");
        ZabbixAPIResult hosid = zabbixApi.getHostId();
        String hosdata = String.valueOf(hosid.getData());
        ArrayList<String> lists = (ArrayList) JSONArray.parseArray(hosdata, String.class);
        ArrayList<String> arr = new ArrayList<>();
        arr.add("1");
        arr.add("2");
        ZabbixAPIResult dr = zabbixApi.dhostGets(arr);
        ZabbixAPIResult item = zabbixApi.hostitem("10680", null);
        ZabbixAPIResult result = zabbixApi.getApp("10543");
        ZabbixAPIResult resulth = zabbixApi.getitem("40986");//42373 10680
        ZabbixAPIResult resulh = zabbixApi.itemgetbyhostid("10680");
        String data = String.valueOf(result.getData());
        List<Map> list = JSONArray.parseArray(data, Map.class);
        ////System.out.println(">>>>>>>list>>>>>>" + list.size());


        //ZabbixAPIResult itemresult = zabbixApi.getItems("3117");
        ZabbixAPIResult itemresult = zabbixApi.getItems("10543");
        String itdata = String.valueOf(itemresult.getData());
        List<Map> itlist = JSONArray.parseArray(itdata, Map.class);
        ////System.out.println(">>>>>>>itlist>>>>>>" + itlist.size());

    }

    @Test
    public void testApiVersion() {
        ZabbixAPIResult zabbixAPIResult = zabbixApi.apiVersion();
        if (!zabbixAPIResult.isFail()) {
            JsonNode data = (JsonNode) zabbixAPIResult.getData();
            String version = data.asText();
            assertEquals(version, Zabbix_Version);
        }
    }

    @Test
    public void testLogin() {
        boolean loginSuccess = zabbixApi.login(Zabbix_User, Zabbix_Password);
        boolean loginFail = zabbixApi.login(Zabbix_User, Zabbix_Password_Wrong);

        assertTrue(loginSuccess);
        assertFalse(loginFail);
    }

    @Test
    public void testhostGetById() {
        ZabbixAPIResult hostR = zabbixApi.hostById("10543");
        ZabbixAPIResult drsult = zabbixApi.dhostGet("8");
        Object dstr = drsult.getData();
        ZabbixAPIResult result = zabbixApi.hostGetById("4");
        String str = String.valueOf(result.getData());
        ////System.out.println(">>>>>>" + JSON.toJSONString(str));
    }

    @Test
    public void hostgroupListGetById() {
        ArrayList<String> groupidList = new ArrayList<>();
        groupidList.add("19");
        ZabbixAPIResult result = zabbixApi.hostgroupListGetById(groupidList);
        String str = String.valueOf(result.getData());
        ////System.out.println(">>>>>>" + JSON.toJSONString(str));
    }

    @Test
    public void testproxygetById() {
        ZabbixAPIResult pro = zabbixApi.proxyget();
        ArrayList<String> proxyidList = new ArrayList<>();
        proxyidList.add("10364");
        ZabbixAPIResult result = zabbixApi.proxygetById(proxyidList);
        String str = String.valueOf(result.getData());
        JSONArray proxys = JSONArray.parseArray(str);
        JSONObject json = JSONObject.parseObject(JSON.toJSONString(proxys.get(0)));
        String proxyName = (String) json.get("host");
        ////System.out.println(">>>>>>>proxyName>>>>>>>" + proxyName);
    }

    @Test
    public void hostget() {
        ZabbixAPIResult resultp = zabbixApi.proxyget();
        ZabbixAPIResult prs = zabbixApi.getHostinterface("10264");
        ZabbixAPIResult pr = zabbixApi.problem_get("10264");
        String prStr = String.valueOf(pr.getData());
        List<Map> prList = JSONArray.parseArray(prStr, Map.class);
        ////System.out.println(">>>problemget>>>>>" + prList.size());

        ZabbixAPIResult epr = zabbixApi.eventgethostid("10264");
        String eStr = String.valueOf(epr.getData());
        List<Map> eList = JSONArray.parseArray(eStr, Map.class);
        ////System.out.println(">>>eList>>>>>" + eList.size());


        ZabbixAPIResult result = zabbixApi.hostget(null);
        String data = String.valueOf(result.getData());
        List<Map> list = JSONArray.parseArray(data, Map.class);

        ////System.out.println(">>hostget>>>>" + list.size());
        ZabbixAPIResult intR = zabbixApi.getHostinterface(null);
        String idata = String.valueOf(intR.getData());
        List<Map> ilist = JSONArray.parseArray(idata, Map.class);
        ////System.out.println(">>hostget>>>>" + ilist.size());
    }

    @Test
    public void myTest() {
        ZabbixAPIResult item = zabbixApi.hostitem("10660", null);
        ZabbixAPIResult group = zabbixApi.historyget("49359");//"Hypervisors"
        String data = String.valueOf(group.getData());
        List<Map> list = JSONArray.parseArray(data, Map.class);
        Map map = list.get(0);
        String groupid = String.valueOf(map.get("groupid"));

        ZabbixAPIResult itemd = zabbixApi.hostinterfaceget("10660");
        ZabbixAPIResult inter = zabbixApi.getinterface("10.0.0.52", "443");
        String indata = String.valueOf(inter.getData());
        List<Map> inlist = JSONArray.parseArray(indata, Map.class);
        String[] str = new String[]{"57"};
        ZabbixAPIResult res = zabbixApi.gethosts(str, null, null);//Hypervisors
        ZabbixAPIResult result = zabbixApi.triggerGetById("18865");
        String tdata = String.valueOf(result.getData());
        ZabbixAPIResult eresult = zabbixApi.eventgetByObjectid("15916", null, 0L, 0L);
        String edata = String.valueOf(eresult.getData());
        //List<Map> list = JSONArray.parseArray(edata, Map.class);
        ////System.out.println(">>>>>>>>>>>>>>>>>>>>" + list.size());
    }


    @Test
    public void alertget() {
        ZabbixAPIResult graph = zabbixApi.graphgetByHostid("10265");
        String datas = String.valueOf(graph.getData());
        List<Map> list = JSONArray.parseArray(datas, Map.class);
        for (Map map : list) {
            ////System.out.println(">>>>>>>>>>>" + map.get("name"));
        }

        // ProxyDto dto = new ProxyDto();
        //ZabbixAPIResult pro = zabbixApi.getProxy(dto);
        ZabbixAPIResult er = zabbixApi.eventgets(null, null, null, 0L, 0L, null);
        // ZabbixAPIResult eR = zabbixApi.eventgethist("10543");
        ZabbixAPIResult eR = zabbixApi.eventgetByObjectid("10543", null, 0L, 0L);
        String data = String.valueOf(eR.getData());
        ZabbixAPIResult result = zabbixApi.alertget("10268", null, null, null, null, null, null);
        String str = String.valueOf(result.getData());
   *//* List<Map> list = JSONArray.parseArray(str, Map.class);
    ////System.out.println(">>>>>>>>>>>>"+list.size());*//*
    *//*

    String ed = String.valueOf(er.getData());
    List<Map> elist = JSONArray.parseArray(ed, Map.class);"107063""173281"
    Map<String, List<Map>> maps = new HashMap<>();
    for(Map map:elist){
        ////System.out.println(">>>eventid>>>>"+map.get("eventid"));
        ////System.out.println(">>>r_eventid>>>>"+map.get("r_eventid"));
        if(maps.containsKey(map.get("eventid"))||maps.containsKey(map.get("r_eventid"))){//map中存在此id，将数据存放当前key的map中
            String eventid = String.valueOf(map.get("r_eventid"));
            maps.get(eventid).add(map);
        }else{//map中不存在，新建key，用来存放数据
            List<Map> tmpList = new ArrayList<>();
            tmpList.add(map);
            maps.put(String.valueOf(map.get("eventid")), tmpList);
        }
     }

    ////System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>");

    ZabbixAPIResult triResult = zabbixApi.triggerGetById("18449");
    String data = String.valueOf(triResult.getData());
    List<Map> list = JSONArray.parseArray(data, Map.class);
    ////System.out.println(">>>>>>"+list.size());

    *//*


        // ZabbixAPIResult iem = zabbixApi.graphitemget("1510");
   *//* ArrayList<String> list = new ArrayList<>();
    list.add("41075");
    list.add("41079");
    list.add("41078");
    Calendar calendar= Calendar.getInstance();
    calendar.add(Calendar.MINUTE, -100);
    long mill = calendar.getTimeInMillis()/1000;
    ZabbixAPIResult hist = zabbixApi.historygets(list,mill);
    List<Map> lists = JSONArray.parseArray(String.valueOf(hist.getData()), Map.class);
    Map<String, List<String>> map = new HashMap<>();
    ArrayList<String> ar = new ArrayList<>();
    for(Map m:lists){
        ar.add(toDate(Long.valueOf(String.valueOf(m.get("clock")))));
        if(map.containsKey(m.get("itemid"))){//map中存在此id，将数据存放当前key的map中
            map.get(m.get("itemid")).add(String.valueOf(m.get("itemid")));
        }else{//map中不存在，新建key，用来存放数据
            List<String> tmpList = new ArrayList<>();
            tmpList.add(String.valueOf(m.get("value")));
            map.put(String.valueOf(m.get("itemid")), tmpList);
        }
    }
    ////System.out.println(">>>ar>>>>>"+ar.size());
    List newList = removeDuplicate(ar);
    ////System.out.println(">>>newList>>>>>"+newList.size());*//*
  *//*  ZabbixAPIResult temR = zabbixApi.usermacroget("10512");
    String tem = String.valueOf(temR.getData());

    String trig = String.valueOf(triResult.getData());
    List<Map>proList = JSONArray.parseArray(trig, Map.class);*//*

   *//* ZabbixAPIResult evtresult = zabbixApi.eventgetlist(null);//"44843""18148"
    String ev = String.valueOf(evtresult.getData());
    ZabbixAPIResult users = zabbixApi.userget(null);
    List<Map>usList = JSONArray.parseArray(String.valueOf(users.getData()), Map.class);
    Map data = usList.get(0);
    ZabbixAPIResult eresult = zabbixApi.eventget(null,null);//"30434"
    String dataStr = String.valueOf(eresult.getData());
    String name = (String) data.get("name");
    List<Map>evList = JSONArray.parseArray(ev, Map.class);
    ZabbixAPIResult reslt = zabbixApi.mediatypeget("0");
    int len = evList.size()/2;
    for(int i=0;i<len;i++){
        int start = i*2;
        List<Map> newList = evList.subList(start, start + 2);
        Long ns1 = Long.valueOf(String.valueOf(newList.get(0).get("ns")));
        Long ns2 = Long.valueOf(String.valueOf(newList.get(1).get("ns")));
        long ns = ns2 - ns1;

        ////System.out.println(">>>>>>>>>>>>>>>>>>>>>ns>>>>>>>>>>>"+ns);
    }


    JSONArray json = JSONArray.parseArray(trig);*//*
//    ZabbixAPIResult iter = zabbixApi.itemget("29118"); 16145
   *//* String functions = String.valueOf(json.get("functions"));
    JSONArray list = JSONArray.parseArray(functions);*//*
        //根据返回的itemid查询监控项的主机
  *//*  ZabbixAPIResult hostresult = zabbixApi.hostget("16145");
    Object data = hostresult.getData();
    ZabbixAPIResult result = zabbixApi.hostinterfaceget("10265");*//*
   *//* ////System.out.println(">>>eventget>>>>>"+dataStr);
    ZabbixAPIResult actResult = zabbixApi.actionget("8");
    String actStr = String.valueOf(actResult.getData());
    ////System.out.println(">>>actionget>>>>>"+actStr);
    List<Map> evList = JSONArray.parseArray(dataStr, Map.class);

    for(Map map:evList){
        ////System.out.println(">>>eventget>>>>>"+map.get("name"));
    }*//*

    }

    public static List removeDuplicate(List list) {
        List listTemp = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            if (!listTemp.contains(list.get(i))) {
                listTemp.add(list.get(i));
            }
        }
        return listTemp;
    }

    private String toDate(Long time) {
        Calendar c = Calendar.getInstance();
        long millions = time * 1000;
        c.setTimeInMillis(millions);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String dateString = sdf.format(c.getTime());
        return dateString;
    }

    *//**
     * 创建动作
     *//*
    @Test
    public void testActioncreate() {
        String name = "获取ID";
        String shortdata = "故障{TRIGGER.STATUS},服务器:{HOSTNAME1}发生: {TRIGGER.NAME}故障!";
        String longdata = "告警主机:{HOSTNAME1}\n" +
                "告警时间:{EVENT.DATE} {EVENT.TIME}\n" +
                "告警等级:{TRIGGER.SEVERITY}\n" +
                "告警信息: {TRIGGER.NAME}\n" +
                "告警项目:{TRIGGER.KEY1}\n" +
                "问题详情:{ITEM.NAME}:{ITEM.VALUE}\n" +
                "当前状态:{TRIGGER.STATUS}:{ITEM.VALUE1}\n" +
                "事件ID:{EVENT.ID}";
        HashMap filter = new HashMap();
        ArrayList<HashMap> flist = new ArrayList<>();
        // for(int i = 0;i<1;i++){
        HashMap params1 = new HashMap();
        params1.put("conditiontype", "1");
        params1.put("operator", 0);
        params1.put("value", "10543");
        HashMap params2 = new HashMap();
        params2.put("conditiontype", "4");
        params2.put("operator", 5);
        params2.put("value", "2");
        flist.add(params1);
        flist.add(params2);
        //  }


        filter.put("evaltype", "0");
        filter.put("conditions", flist);

        //动作
        ArrayList<HashMap> optlist = new ArrayList<>();
        HashMap oparams = new HashMap();
        oparams.put("operationtype", "0");
        oparams.put("esc_period", "30m");
        oparams.put("esc_step_from", "1");
        oparams.put("esc_step_to", "2");
        oparams.put("evaltype", "1");

        HashMap msgMap = new HashMap();
        msgMap.put("default_msg", "1");
        msgMap.put("mediatypeid", "1");
        msgMap.put("message", longdata);
        msgMap.put("subject", shortdata);
        oparams.put("opmessage", msgMap);
        ArrayList<HashMap> users = new ArrayList<>();
        HashMap user = new HashMap();
        user.put("usrgrpid", "7");
        users.add(user);
        oparams.put("opmessage_grp", users);
        optlist.add(oparams);

        *//*ZabbixAPIResult result = zabbixApi.actioncreate(name,shortdata,longdata,filter,optlist);
        String data = String.valueOf(result.getData());
        JSONObject datas = (JSONObject) JSONObject.parseObject(data);
        JSONArray strs = (JSONArray) datas.get("actionids");
        ////System.out.println(">>>>>>>>>>>>>"+strs.get(0));
        JSONObject json = (JSONObject) JSONObject.toJSON(result.getData());
        JSONArray ids = (JSONArray) json.get("actionids");
        ////System.out.println(">>>>>>>>>>>>>"+ids.get(0));*//*
    }

    @Test
    public void actionget() {
        ZabbixAPIResult result = zabbixApi.actionget(null);
        String data = String.valueOf(result.getData());
        JSONArray datlist = JSONArray.parseArray(data);
        JSONObject djson = JSONObject.parseObject(String.valueOf(datlist.get(0)));
        String filStr = String.valueOf(djson.get("filter"));
        JSONObject filjson = JSONObject.parseObject(filStr);
        String conditions = String.valueOf(filjson.get("conditions"));
        JSONArray conlist = JSONArray.parseArray(conditions);
        JSONObject conjson = JSONObject.parseObject(String.valueOf(conlist.get(0)));
        Object conditiontype = String.valueOf(conjson.get("conditiontype"));
        Object value = String.valueOf(conjson.get("value"));
        ////System.out.println(">>>>>>" + conditions);
    }

    @Test
    public void eventacknowledge() {
        ZabbixAPIResult result = zabbixApi.eventacknowledge("30433", null, "w");
        ////System.out.println(">>>>>>>>>>result>>>>>>" + result.isFail());
        ZabbixAPIResult hisResult = zabbixApi.historyget();
        JSONArray hisList = JSONArray.parseArray(String.valueOf(hisResult.getData()));
        for (int i = 0; i < hisList.size(); i++) {
            Map map = JSONObject.parseObject(String.valueOf(hisList.get(i)), Map.class);
            map.get("itemid");
            ////System.out.println(">>>>>>>>>>itemid>>>>>>" + map.get("itemid"));
        }
        ////System.out.println(">>>>>>>>>>result>>>>>>" + hisResult.isFail());
    }


    @Test
    public void testHostgroupCreateAndDelete() {
        zabbixApi.login(Zabbix_User, Zabbix_Password);

        zabbixApi.hostgroupDeleteByName(Zabbix_Test_Host_Group_One);
        zabbixApi.hostgroupCreate(Zabbix_Test_Host_Group_One);

        assertTrue(zabbixApi.hostgroupExists(Zabbix_Test_Host_Group_One));

        zabbixApi.hostgroupDeleteByName(Zabbix_Test_Host_Group_One);
    }

    @Test
    public void testHostgroupListCreateAndDelete() {
        zabbixApi.login(Zabbix_User, Zabbix_Password);

        ArrayList<String> groupNameList = new ArrayList<>();
        groupNameList.add(Zabbix_Test_Host_Group_One);
        groupNameList.add(Zabbix_Test_Host_Group_Two);

        zabbixApi.hostgroupListDeleteByName(groupNameList);
        zabbixApi.hostgroupListCreate(groupNameList);

        assertTrue(zabbixApi.hostgroupExists(Zabbix_Test_Host_Group_One));
        assertTrue(zabbixApi.hostgroupExists(Zabbix_Test_Host_Group_Two));

        ZabbixAPIResult hostgroupListGetResult = zabbixApi.hostgroupListGetByName(groupNameList);
        if (!hostgroupListGetResult.isFail()) {
            JsonNode data = (JsonNode) hostgroupListGetResult.getData();
            assertEquals(2, data.size());
            if (data.size() > 0) {
                data.forEach(hostgroup -> {
                    String groupname = hostgroup.get("name").asText();
                    assertTrue(groupNameList.contains(groupname));
                });
            }
        }

        zabbixApi.hostgroupListDeleteByName(groupNameList);
    }

//    @Test
//    public void testgetHostList() {
//        ZabbixAPIResult result = zabbixApi.getHostinterface(null);
//        String str = String.valueOf(result.getData());
//        List<HostInterFaceDto> list = JSONArray.parseArray(str, HostInterFaceDto.class);
//        for (HostInterFaceDto dto : list) {
//            ////System.out.println(">>>>>>>>>>>>>>>>>" + dto.toString());
//
//        }
        *//*ZabbixAPIResult result = zabbixApi.getHostList(null);
        String str = String.valueOf(result.getData());
        List<HostDTO> list = JSONArray.parseArray(str, HostDTO.class);
        for(HostDTO dto:list){
            ////System.out.println(">>>>>>>>>>>>>>>>>"+dto.toString());

        }
*//*
//    }

    @Test
    public void itemGet() {
        ZabbixAPIResult prResult = zabbixApi.problemget(null);//18148
        String data = String.valueOf(prResult.getData());
        List<Map> list = JSONArray.parseArray(data, Map.class);
        ////System.out.println(">>>problem>>>" + JSON.toJSONString(data));
        ZabbixAPIResult result = zabbixApi.triggerGet();
        String str = String.valueOf(result.getData());
        list = JSONArray.parseArray(str, Map.class);
        ////System.out.println(">>>监控项>>>>" + list.size());
    }

    @Test
    public void testHostCreateAndDelete() {
        zabbixApi.login(Zabbix_User, Zabbix_Password);

        Map hostInterface = createHostInterface("10050", "1");

        boolean hostExists = zabbixApi.hostExists(Zabbix_Test_Host_One);
        String groupId = "";
        // If host exists, delete the host
        if (hostExists) {
            zabbixApi.hostDeleteByName(Zabbix_Test_Host_One);
        }
        // Get the groupid of Zabbix_Test_Host_Group_One
        if (zabbixApi.hostgroupExists(Zabbix_Test_Host_Group_One)) {
            ZabbixAPIResult hostgroupGetResult = zabbixApi.hostgroupGetByGroupName(Zabbix_Test_Host_Group_One);
            if (!hostgroupGetResult.isFail()) {
                JsonNode data = (JsonNode) hostgroupGetResult.getData();
                // If Zabbix_Test_Host_Group exists, fetch the groupid
                groupId = data.get(0).get("groupid").asText();
            }
        } else {
            ZabbixAPIResult hostgroupCreateResult = zabbixApi.hostgroupCreate(Zabbix_Test_Host_Group_One);
            if (!hostgroupCreateResult.isFail()) {
                JsonNode data = (JsonNode) hostgroupCreateResult.getData();
                groupId = data.get("groupids").get(0).asText();
            }
        }

        ArrayList<String> groupIdList = new ArrayList<>();
        ArrayList<String> tempIdList = new ArrayList<>();
        groupIdList.add(groupId);

        zabbixApi.hostCreate(Zabbix_Test_Host_One, groupIdList, hostInterface, null,null);

        assertTrue(zabbixApi.hostExists(Zabbix_Test_Host_One));

        zabbixApi.hostDeleteByName(Zabbix_Test_Host_One);
        zabbixApi.hostgroupDeleteByName(Zabbix_Test_Host_Group_One);
    }

    @Test
    public void testHostListCreateAndDelete() {
        zabbixApi.login(Zabbix_User, Zabbix_Password);
        ArrayList<String> groupNameList = new ArrayList<>();
        ArrayList<String> hostNameList = new ArrayList<>();
        groupNameList.add(Zabbix_Test_Host_Group_One);
        groupNameList.add(Zabbix_Test_Host_Group_Two);
        hostNameList.add(Zabbix_Test_Host_One);
        hostNameList.add(Zabbix_Test_Host_Two);

        ArrayList groupIdList = new ArrayList<>();
        ZabbixAPIResult hostgroupGetResult = zabbixApi.hostgroupListGetByName(groupNameList);
        if (!hostgroupGetResult.isFail()) {
            JsonNode data = (JsonNode) hostgroupGetResult.getData();
            if (data.size() > 0) {
                data.forEach(group -> {
                    Map groupMap = new HashMap();
                    groupMap.put("groupid", group.get("groupid").asText());
                    groupIdList.add(groupMap);
                });
            }
        }

        if (groupIdList.size() == 0) {
            ZabbixAPIResult hostgroupCreateResult = zabbixApi.hostgroupListCreate(groupNameList);
            if (!hostgroupCreateResult.isFail()) {
                JsonNode result = (JsonNode) hostgroupCreateResult.getData();
                result.get("groupids").forEach(groupid -> {
                    Map groupMap = new HashMap();
                    groupMap.put("groupid", groupid);
                    groupIdList.add(groupMap);
                });
            }
        }

        zabbixApi.hostListDeleteByName(hostNameList);
        ArrayList<HashMap> params = new ArrayList();
        HashMap param = new HashMap();
        param.put("host", Zabbix_Test_Host_One);
        param.put("groups", groupIdList);
        param.put("interfaces", createHostInterface("10050", "1"));
        params.add(param);
        param = new HashMap();
        param.put("host", Zabbix_Test_Host_Two);
        param.put("groups", groupIdList);
        Map interface1 = createHostInterface("10050", "1");
        Map interface2 = createHostInterface("9010", "4");
        ArrayList interfaces = new ArrayList();
        interfaces.add(interface1);
        interfaces.add(interface2);
        param.put("interfaces", interfaces);
        params.add(param);

        zabbixApi.hostListCreate(params);

        assertTrue(zabbixApi.hostExists(Zabbix_Test_Host_One));
        assertTrue(zabbixApi.hostExists(Zabbix_Test_Host_Two));

        ZabbixAPIResult hostListGetResult = zabbixApi.hostListGetByHostName(hostNameList);
        if (!hostListGetResult.isFail()) {
            JsonNode data = (JsonNode) hostListGetResult.getData();
            assertEquals(2, data.size());
            if (data.size() > 0) {
                data.forEach(host -> {
                    String hostname = host.get("host").asText();
                    assertTrue(hostNameList.contains(hostname));
                });
            }
        }

        zabbixApi.hostListDeleteByName(hostNameList);
    }

    private Map createHostInterface(String port, String type) {
        Map hostInterface = new HashMap<>();
        hostInterface.put("dns", "");
        hostInterface.put("ip", "127.0.0.1");
        hostInterface.put("main", 1);
        hostInterface.put("port", port);
        hostInterface.put("type", type);
        hostInterface.put("useip", 1);

        return hostInterface;
    }

    @Test
    public void testHostInterfaceCreate() {
        zabbixApi.login(Zabbix_User, Zabbix_Password);

        String groupId = null;
        String hostId = null;
        String interfaceId = null;

        if (!zabbixApi.hostgroupExists(Zabbix_Test_Host_Group_One)) {
            ZabbixAPIResult hostgroupCreateResult = zabbixApi.hostgroupCreate(Zabbix_Test_Host_Group_One);
            if (!hostgroupCreateResult.isFail()) {
                JsonNode data = (JsonNode) hostgroupCreateResult.getData();
                if (data.size() > 0) {
                    groupId = data.get("groupids").get(0).asText();
                }
            }

        }
        if (!zabbixApi.hostExists(Zabbix_Test_Host_One)) {
            ArrayList<String> groupIdList = new ArrayList();
            groupIdList.add(groupId);
            ZabbixAPIResult hostCreateResult = zabbixApi.hostCreate(Zabbix_Test_Host_One, groupIdList, createHostInterface("10050", "1"), null,null);
            if (!hostCreateResult.isFail()) {
                JsonNode data = (JsonNode) hostCreateResult.getData();
                if (data.size() > 0) {
                    hostId = data.get("hostids").get(0).asText();
                }
            }
        }
        ZabbixAPIResult hostGetResult = zabbixApi.hostGetByHostName(Zabbix_Test_Host_One);
        if (!hostGetResult.isFail()) {
            JsonNode data = (JsonNode) hostGetResult.getData();
            if (data.size() > 0) {
                hostId = data.get(0).get("hostid").asText();
            }
        }

        String dns = "";
        String ip = "127.0.0.1";
        String main = "0";
        String port = "10051";
        String type = "1";
        String useip = "1";
        String bulk = "1";

        ZabbixAPIResult hostInterfaceCreateResult = zabbixApi.hostInterfaceCreate(dns, hostId, ip, main, port, type, useip, bulk);
        if (!hostInterfaceCreateResult.isFail()) {
            JsonNode data = (JsonNode) hostInterfaceCreateResult.getData();
            if (data.size() > 0) {
                interfaceId = data.get("interfaceids").get(0).asText();
            }
        }

        ArrayList<String> hostIdList = new ArrayList<>();
        hostIdList.add(hostId);
        ArrayList<String> interfaceIdList = new ArrayList<>();
        ZabbixAPIResult hostInterfaceGetResult = zabbixApi.hostInterfaceGetByHostIds(hostIdList);
        if (!hostInterfaceGetResult.isFail()) {
            JsonNode hostInterfaces = (JsonNode) hostInterfaceGetResult.getData();
            if (hostInterfaces.size() > 0) {
                hostInterfaces.forEach(hostInterface -> {
                    interfaceIdList.add(hostInterface.get("interfaceid").asText());
                });
            }
        }
        assertTrue(interfaceIdList.contains(interfaceId));

        zabbixApi.hostDeleteByName(Zabbix_Test_Host_One);
        zabbixApi.hostgroupDeleteByName(Zabbix_Test_Host_Group_One);
    }

    @Test
    public void testHostInterfaceListCreateError() {
        zabbixApi.login(Zabbix_User, Zabbix_Password);

        String dns = "";
        String main = "0";
        String port = "10050";
        String type = "1";
        String useip = "1";

        //Without required property hostid
        HashMap param = new HashMap();
        param.put("dns", dns);
        param.put("ip", "127.0.0.1");
        param.put("main", main);
        param.put("port", port);
        param.put("type", type);
        param.put("useip", useip);

        ArrayList<HashMap> params = new ArrayList<>();
        params.add(param);
        ZabbixAPIResult zabbixAPIResult = zabbixApi.hostInterfaceListCreate(params);

        Integer code = zabbixAPIResult.getCode();
        String data = zabbixAPIResult.getData().toString();

        assertEquals(code, ZabbixAPIResultCode.PARAM_IS_INVALID.code());
        assertEquals(data, "Param has no property : " + "hostid");
    }

    @Test
    public void testItemCreateAndDelete() {
        zabbixApi.login(Zabbix_User, Zabbix_Password);

        String groupId = null;
        String hostId = null;
        String interfaceId = null;
        String itemKey = "";
        ArrayList itemKeyList = new ArrayList();
        itemKeyList.add(Zabbix_Test_Item_One_Key);

        if (!zabbixApi.hostgroupExists(Zabbix_Test_Host_Group_One)) {
            zabbixApi.hostgroupCreate(Zabbix_Test_Host_Group_One);
        }

        ZabbixAPIResult hostgroupGetResult = zabbixApi.hostgroupGetByGroupName(Zabbix_Test_Host_Group_One);
        if (!hostgroupGetResult.isFail()) {
            JsonNode data = (JsonNode) hostgroupGetResult.getData();
            if (data.size() > 0) {
                groupId = data.get(0).get("groupid").asText();
            }
        }

        if (!zabbixApi.hostExists(Zabbix_Test_Host_One)) {
            ArrayList<String> groupIdList = new ArrayList();
            groupIdList.add(groupId);
            zabbixApi.hostCreate(Zabbix_Test_Host_One, groupIdList, createHostInterface("10050", "1"), null,null);
        }

        ArrayList hostNameList = new ArrayList();
        hostNameList.add(Zabbix_Test_Host_One);
        ZabbixAPIResult hostGetResult = zabbixApi.hostInterfaceGetByHostNames(hostNameList);
        if (!hostGetResult.isFail()) {
            JsonNode data = (JsonNode) hostGetResult.getData();
            if (data.size() > 0) {
                hostId = data.get(0).get("hostid").asText();
                interfaceId = data.get(0).get("interfaceid").asText();
            }
        }

        zabbixApi.itemListDeleteByItemKey(Zabbix_Test_Host_One, itemKeyList);

        HashMap param = new HashMap();
        param.put("delay", "60");
        param.put("hostid", hostId);
        param.put("interfaceid", interfaceId);
        param.put("key_", Zabbix_Test_Item_One_Key);
        param.put("name", Zabbix_Test_Item_One_Key);
        param.put("type", ZabbixItemType.ZABBIX_AGENT.code());
        param.put("value_type", ZabbixItemValueType.TEXT.code());

        zabbixApi.itemCreate(param);

        ZabbixAPIResult itemGetResult = zabbixApi.itemGetByHostNameAndItemKey(Zabbix_Test_Host_One, itemKeyList);

        if (!itemGetResult.isFail()) {
            JsonNode data = (JsonNode) itemGetResult.getData();
            if (data.size() > 0) {
                itemKey = data.get(0).get("key_").asText();
            }
        }

        assertEquals(itemKey, Zabbix_Test_Item_One_Key);

        zabbixApi.hostDeleteByName(Zabbix_Test_Host_One);
        zabbixApi.hostgroupDeleteByName(Zabbix_Test_Host_Group_One);
    }

    @Test
    public void testItemListCreateAndDelete() {
        zabbixApi.login(Zabbix_User, Zabbix_Password);

        String groupId = null;
        String hostId = null;
        String interfaceId = null;

        ArrayList itemKeyList = new ArrayList();
        itemKeyList.add(Zabbix_Test_Item_One_Key);
        itemKeyList.add(Zabbix_Test_Item_Two_Key);

        if (!zabbixApi.hostgroupExists(Zabbix_Test_Host_Group_One)) {
            zabbixApi.hostgroupCreate(Zabbix_Test_Host_Group_One);
        }

        ZabbixAPIResult hostgroupGetResult = zabbixApi.hostgroupGetByGroupName(Zabbix_Test_Host_Group_One);
        if (!hostgroupGetResult.isFail()) {
            JsonNode data = (JsonNode) hostgroupGetResult.getData();
            if (data.size() > 0) {
                groupId = data.get(0).get("groupid").asText();
            }
        }

        if (!zabbixApi.hostExists(Zabbix_Test_Host_One)) {
            ArrayList<String> groupIdList = new ArrayList();
            groupIdList.add(groupId);
            zabbixApi.hostCreate(Zabbix_Test_Host_One, groupIdList, createHostInterface("10050", "1"), null,null);
        }

        ArrayList hostNameList = new ArrayList();
        hostNameList.add(Zabbix_Test_Host_One);
        ZabbixAPIResult hostGetResult = zabbixApi.hostInterfaceGetByHostNames(hostNameList);
        if (!hostGetResult.isFail()) {
            JsonNode data = (JsonNode) hostGetResult.getData();
            if (data.size() > 0) {
                hostId = data.get(0).get("hostid").asText();
                interfaceId = data.get(0).get("interfaceid").asText();
            }
        }

        zabbixApi.itemListDeleteByItemKey(Zabbix_Test_Host_One, itemKeyList);

        HashMap param = new HashMap();
        param.put("delay", "60");
        param.put("hostid", hostId);
        param.put("interfaceid", interfaceId);
        param.put("key_", Zabbix_Test_Item_One_Key);
        param.put("name", Zabbix_Test_Item_One_Key);
        param.put("type", ZabbixItemType.ZABBIX_AGENT.code());
        param.put("value_type", ZabbixItemValueType.TEXT.code());

        HashMap param2 = new HashMap();
        param2.put("delay", "60");
        param2.put("hostid", hostId);
        param2.put("interfaceid", interfaceId);
        param2.put("key_", Zabbix_Test_Item_Two_Key);
        param2.put("name", Zabbix_Test_Item_Two_Key);
        param2.put("type", ZabbixItemType.ZABBIX_AGENT.code());
        param2.put("value_type", ZabbixItemValueType.TEXT.code());

        ArrayList params = new ArrayList();
        params.add(param);
        params.add(param2);

        zabbixApi.itemListCreate(params);

        ZabbixAPIResult itemGetResult = zabbixApi.itemGetByHostNameAndItemKey(Zabbix_Test_Host_One,
                itemKeyList);
        ArrayList itemKeys = new ArrayList();
        if (!itemGetResult.isFail()) {
            JsonNode data = (JsonNode) itemGetResult.getData();
            data.forEach(item -> {
                itemKeys.add(item.get("key_").asText());
            });
        }

        assertTrue(itemKeys.contains(Zabbix_Test_Item_One_Key));
        assertTrue(itemKeys.contains(Zabbix_Test_Item_Two_Key));

        zabbixApi.hostDeleteByName(Zabbix_Test_Host_One);
        zabbixApi.hostgroupDeleteByName(Zabbix_Test_Host_Group_One);
    }
*/
}
