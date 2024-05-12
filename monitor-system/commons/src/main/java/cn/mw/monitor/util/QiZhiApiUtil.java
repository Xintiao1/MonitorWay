package cn.mw.monitor.util;

import cn.mw.monitor.util.entity.Progress;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author lumingming
 * @createTime 20230410 15:43
 * @description 齐治堡垒机api工具类
 */
public class QiZhiApiUtil {
    private static final String AUTHEN_URL="/authenticate";
    private static final String DECACCLIST_URL="/perm/devAccList";
    private static final String ACCOUNTS_URL="/perm/accounts";
    private static final String DEVACCPWD="/perm/devAccPwd";
    private static final String AFTER_URL ="/shterm/api";
    private static final String ACCOUNT_URL_TWO ="/account/queryWorksheetPassword";

    public Map<String,Object> heard = new HashMap<>();
    private QiZhiApiUtil(){}
    private  static  final  QiZhiApiUtil single = new QiZhiApiUtil();

    public static  QiZhiApiUtil getInstance(){
        return single;
    }


    public boolean loginQiZhi(String root,String pwd,String url) {
        String body = "{\r\n    \"username\": \""+root+"\",\r\n    \"password\": \""+pwd+"\"\r\n}";
        String postUrl = url+AFTER_URL+AUTHEN_URL;
        Response jsonString = OkHttpUtil.getInstance().DoPost(postUrl,body);
        if (jsonString==null||jsonString.equals("")){
            return  false;
        }
        else {
            JSONObject jsonObject = null;
            try {
                jsonObject = JSONObject.parseObject(jsonString.body().string());
            } catch (IOException e) {
                return  false;
            }
            this.heard = parseJSON2Map(jsonObject);
            OkHttpUtil.getInstance().resetHttp();
            OkHttpUtil.getInstance().setHeader(this.heard);
            return true;
        }
    }


    public JSONArray getAllRootAndpwdAnd(String root, String url, String password,Progress progress){
        progress.setIsOver(false);
        JSONArray array= new JSONArray();
        if (QiZhiApiUtil.getInstance().loginQiZhi(root,password,url)){
            //获取所有资产
            array= getPostAssets(url,root);

            for (int i = 0; i < array.size(); i++) {
                JSONObject jsonObject =array.getJSONObject(i);
                //获取账户密码
                List<String> strings = (List<String>) jsonObject.get("usableAccounts");
                Map<String,String> accountAndPwd = new HashMap<>();
                for (String s:strings) {
                    String pwd = getpwdTwo(jsonObject.getString("name"),jsonObject.getString("ip"),s,url,root);
                    if (pwd!=null){
                        accountAndPwd.put(s,pwd.replaceAll("\"",""));
                    }else {
                        accountAndPwd.put(s,"");
                    }

                }
                jsonObject.put("accountAndKey",accountAndPwd);
                //进度条
                double kill = (double) i/array.size();
                BigDecimal bigDecimal = new BigDecimal(kill);
                kill = bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                progress.setPercentage(kill);
            }
        }
      /*  for (int i = 0; i < 300; i++) {
            Thread.sleep(1000);
            double kill = (double) i/300;
            progress.setPercentage(kill);
        }*/
        return array;
    }


    private  String getpwd(String name, String ip, String root,String url) {
        String postUrl = url+AFTER_URL+DEVACCPWD;
        String body = "{\r\n    \"name\": \""+name+"\",\r\n    \"ip\": \""+ip+"\",\r\n    \"account\": \""+root+"\"\r\n}";
        Response response = OkHttpUtil.getInstance().DoPost(postUrl,body);
        if (response==null){
            return "";
        }
        if (response.isSuccessful()){
            try {
                return response.body().string();
            } catch (IOException e) {
                return "";
            }
        }
        return "";
    }

    private  String getpwdTwo(String name, String ip, String root,String url,String admin) {
        String postUrl = url+AFTER_URL+ACCOUNT_URL_TWO+"?account="+root+"&userName="+admin+"&address="+ip+"";

        Response response = OkHttpUtil.getInstance().DoGet(postUrl);
        if (response==null){
            return "";
        }
        if (response.isSuccessful()){
            JSONObject jsonObject = null;
            try {
                jsonObject = JSONObject.parseObject(response.body().string());
            } catch (IOException e) {
                return "";
            }
            try {
                return jsonObject.getString("password");
            }catch (Exception e){
                return "";
            }

        }
        return "";
    }


    public  JSONArray getPostAssets(String url,String admin){
        JSONArray jsonObjects = new JSONArray();
        Integer intin = 2;
        for (int i = 0; i < intin; i++) {
            String postUrl = url+AFTER_URL+DECACCLIST_URL+"?loginName="+admin+"&page="+i+"&size=100";
            Response response = OkHttpUtil.getInstance().DoGet(postUrl);
            if (response.isSuccessful()){
                String json = null;
                try {
                    json = response.body().string();
                } catch (IOException e) {
                    return jsonObjects;
                }
                JSONObject jsonObject = JSONObject.parseObject(json);
                intin = jsonObject.getInteger("totalPages");
                JSONArray stringJSon = jsonObject.getJSONArray("content");
                jsonObjects.addAll(stringJSon);
            }
        }
        return jsonObjects;
    }

    public static Map<String, Object> parseJSON2Map(JSONObject json) {
        Map<String, Object> map = new HashMap<String, Object>();
        // 最外层解析
        for (Object k : json.keySet()) {
            Object v = json.get(k);
            // 如果内层还是json数组的话，继续解析
            if (v instanceof JSONArray) {
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                Iterator<Object> it = ((JSONArray) v).iterator();  //迭代器不用考虑长度为0的情况
                while (it.hasNext()) {
                    JSONObject json2 = (JSONObject) it.next();
                    list.add(parseJSON2Map(json2));
                }
                map.put(k.toString(), list);
            } else if (v instanceof JSONObject) {
                // 如果内层是json对象的话，继续解析
                map.put(k.toString(), parseJSON2Map((JSONObject) v));
            } else {
                // 如果内层是普通对象的话，直接放入map中
                map.put(k.toString(), v);
            }
        }
        return map;
    }
}