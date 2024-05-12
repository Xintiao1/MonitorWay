package cn.mw.monitor.service.rule;

import cn.mw.monitor.servicerule.dao.MWRuleMapperDao;
import cn.mw.monitor.service.rule.exception.MessageRuleException;
import cn.mw.monitor.service.rule.param.ParamConverter;
import cn.mw.monitor.service.rule.param.RuleDBParam;
import cn.mwpaas.common.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;

@Component
@Slf4j
public class RuleManageFactory<T> implements InitializingBean {
    private Map<String, RuleManager> ruleManagerMap = new HashedMap();
    private Map<String, ParamConverter> converterMap = new HashedMap();

    @Value("${rule.dir}")
    private String ruleDir;
    private String appDir;

    @Value("${rule.storage}")
    private MessageRuleStorageType storageType;

    @javax.annotation.Resource
    private MWRuleMapperDao mwRuleMapperDao;

    private RuntimeTypeAdapterFactory<MessageRule> messageRuleAdapter;

    public String getJsonStr(RuleManager ruleManager){
        Gson gson = new GsonBuilder().create();
        String content = gson.toJson(ruleManager);
        return content;
    }

    public RuleManager getRuleManager(String jsonStr){
        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(messageRuleAdapter)
                .create();
        RuleManager ruleManager = gson.fromJson(jsonStr, RuleManager.class);
        ruleManager.initRuleProcessorMap();
        return ruleManager;
    }

    public int saveRule(String model ,String ruleKey , RuleManager ruleManager) throws Exception{

        Gson gson = new GsonBuilder().create();
        String content = gson.toJson(ruleManager);
        switch (storageType){
            case FILE:
                File file = getRuleFile(model ,ruleKey ,true);
                if(!file.exists()){
                    file.getParentFile().mkdirs();
                }
                saveContent(file, content);
                break;
            case DB:
                doSaveDBRule(model ,ruleKey ,content ,true);
                break;
            default:
                throw new MessageRuleException("storage type error");
        }

        ruleManagerMap.put(model+ruleKey ,ruleManager);
        return 0;
    }

    public void saveParam(String model ,String ruleKey ,String content) throws Exception{
        switch (storageType) {
            case FILE:
                File file = getRuleFile(model, ruleKey, false);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                }
                saveContent(file, content);
                break;
            case DB:
                doSaveDBRule(model ,ruleKey ,content ,false);
                break;
            default:
        }

    }

    private void doSaveDBRule(String model ,String ruleKey ,String content ,boolean isRule){
        String id = genDBId(model ,ruleKey ,isRule);
        int count = mwRuleMapperDao.existById(id);
        RuleDBParam ruleDBParam = new RuleDBParam();
        ruleDBParam.setId(id);
        ruleDBParam.setRule(content);
        if( 0 == count){
            mwRuleMapperDao.saveRule(ruleDBParam);
        }else{
            mwRuleMapperDao.updateRule(ruleDBParam);
        }
    }

    public String getParam(String model ,String ruleKey) throws Exception{
        String result = null;
        switch (storageType){
            case FILE:
                File file = getRuleFile(model ,ruleKey ,false);
                result = readContent(file);
                break;
            case DB:
                String id = genDBId(model ,ruleKey ,false);
                RuleDBParam ruleDBParam = mwRuleMapperDao.selectById(id);
                result = ruleDBParam.getRule();
                break;
            default:
                throw new MessageRuleException("storage type error");
        }
        return result;
    }


    private void saveContent(File file, String content) throws Exception{
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath(),false),"UTF-8"));
        try {
            writer.write(content);
        } catch (IOException ex) {
            log.error("saveRule", ex);
        } finally {
            try {
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                log.error("saveRule", ex);
            }
        }
    }

    private String readContent(File file) throws Exception{
        Resource resource = new PathResource(file.getAbsolutePath());
        BufferedReader bf = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8"));
        StringBuffer jsonsb = new StringBuffer();
        String s = null;
        while((s = bf.readLine())!=null){//使用readLine方法，一次读一行
            jsonsb.append(s);

        }
        bf.close();
        return jsonsb.toString();
    }

    private File getRuleFile(String model ,String ruleKey, boolean isRule){
        String path = appDir;

        if(StringUtils.isNotEmpty(ruleDir)){
            path = ruleDir;
        }

        String suffix = ".param";
        if(isRule){
            suffix = ".rule";
        }
        File file = new File(path + File.separator + model + File.separator + ruleKey + suffix);
        return file;
    }

    synchronized public RuleManager getRuleManagerByKey(String model ,String ruleKey) throws Exception{
        String key = model+ruleKey;
        RuleManager ruleManager = ruleManagerMap.get(key);
        if(null != ruleManager){
            return ruleManager;
        }

        File file = getRuleFile(model ,ruleKey ,true);
        String jsonsb = readContent(file);
        ruleManager = getRuleManager(jsonsb);
        ruleManagerMap.put(key ,ruleManager);
        return ruleManager;
    }

    synchronized public void delRuleFile(String model ,String ruleKey){
        switch (storageType){
            case FILE:
                String key = model+ruleKey;
                ruleManagerMap.remove(key);
                File ruleFile = getRuleFile(model ,ruleKey ,true);
                File paramFile = getRuleFile(model ,ruleKey ,false);

                ruleFile.delete();
                paramFile.delete();
                break;
            case DB:
                String id = genDBId(model ,ruleKey ,true);
                mwRuleMapperDao.deleteById(id);
                id = genDBId(model ,ruleKey ,false);
                mwRuleMapperDao.deleteById(id);
                break;
        }


    }

    public RuleProcessor getRuleProcessor(String model ,String ruleKey) throws Exception{
        RuleManager ruleManager = getRuleManagerByKey(model ,ruleKey);
        return ruleManager.getRuleProcessor();
    }

    public String saveRule(String model ,T ruleParam ) throws Exception{
        ParamConverter converter = converterMap.get(model);
        String ruleKey = null;
        if(null != converter){
            RuleManager ruleManager = converter.convert(ruleParam);
            ruleKey = converter.genRuleKey(ruleParam, model);
            ruleManager.setId(ruleKey);
            saveRule(model ,ruleKey ,ruleManager);

            //保存前端原始参数
            Gson gson = new GsonBuilder().create();
            String content = gson.toJson(ruleParam);
            saveParam(model ,ruleKey, content);
        }
        return ruleKey;
    }

    public T getParam(String model , T ruleParam) throws Exception{
        T ret = null;
        ParamConverter converter = converterMap.get(model);
        if(null != converter) {
            String ruleKey = converter.genRuleKey(ruleParam, model);
            Gson gson = new GsonBuilder().create();
            String content = getParam(model ,ruleKey);
            ret = (T)gson.fromJson(content ,ruleParam.getClass());
        }
        return ret;
    }

    public void registerRuleParamConverter(ParamConverter paramConverter){
        converterMap.put(paramConverter.getId(), paramConverter);
    }

    private String genDBId(String model ,String ruleKey ,boolean isRule){
        StringBuffer id = new StringBuffer(model).append("-").append(ruleKey);
        if(isRule){
            id.append(".rule");
        }else{
            id.append(".param");
        }
        return id.toString();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        messageRuleAdapter = RuntimeTypeAdapterFactory
                .of(MessageRule.class, "type")
                .registerSubtype(RuleAnd.class, "and")
                .registerSubtype(RuleOr.class, "or")
                .registerSubtype(RuleOrSet.class, "orSet")
                .registerSubtype(RuleContain.class, "contain")
                .registerSubtype(RuleNotContain.class, "notContain")
                .registerSubtype(RuleEqual.class, "equal")
                .registerSubtype(RuleNotEqual.class, "notEqual")
                .registerSubtype(RuleEndWith.class, "endWith")
                .registerSubtype(RuleStartWith.class, "startWith")
                .registerSubtype(RuleExpression.class, "expression")
                .registerSubtype(RuleSetContain.class, "setContain")
        ;
        File file = new File("");
        appDir = file.getAbsolutePath() + File.separator + "rule";
    }
}
