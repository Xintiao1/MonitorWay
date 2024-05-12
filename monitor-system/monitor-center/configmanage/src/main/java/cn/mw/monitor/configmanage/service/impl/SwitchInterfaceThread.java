package cn.mw.monitor.configmanage.service.impl;

import cn.mw.monitor.accountmanage.entity.QueryAccountManageParam;
import cn.mw.monitor.configmanage.dao.MwConfigManageTableDao;
import cn.mw.monitor.configmanage.entity.MwAccountMapper;
import cn.mw.monitor.configmanage.entity.MwTangibleassetsTable;
import cn.mw.monitor.configmanage.entity.MwTemplateMapper;
import cn.mw.monitor.configmanage.service.EncryptUtil;
import cn.mw.monitor.configmanage.service.MwPerfromService;
import cn.mw.monitor.configmanage.service.Parser;
import cn.mw.monitor.templatemanage.entity.QueryTemplateManageParam;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author gui.quanwang
 * @className SwitchInterfaceService
 * @description 开关接口服务
 * @date 2022/6/7
 */
@Service
@Slf4j
@Transactional
public class SwitchInterfaceThread implements Callable<Boolean> {

    /**
     * 资产信息
     */
    private MwTangibleassetsTable param;

    private MwConfigManageTableDao mwTangibleAssetsDao;

    /**
     * 下发命令服务
     */
    private MwPerfromService mwPerfromService;

    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 开关状态
     */
    private boolean switchState;


    public SwitchInterfaceThread() {
    }

    public SwitchInterfaceThread(MwTangibleassetsTable param,
                                 MwConfigManageTableDao mwTangibleAssetsDao,
                                 MwPerfromService mwPerfromService,
                                 String interfaceName,
                                 boolean switchState) {
        this.param = param;
        this.mwTangibleAssetsDao = mwTangibleAssetsDao;
        this.mwPerfromService = mwPerfromService;
        this.interfaceName = interfaceName;
        this.switchState = switchState;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Boolean call() throws Exception {
        try {
            MwTangibleassetsTable param = this.param;
            //1 根据资产查询出 下载配置的cmd命令
            String assetsId = param.getId();
            //获取配置内容
            Map<String, String> xmlMaps = getCommandMap(assetsId);
            //获取命令
            String enterEnable = getString(xmlMaps.get("EnableCommand"), "super");
            String enterConfigMode = getString(xmlMaps.get("EnterConfigMode"), "system");
            String interfaceCommand = getString(xmlMaps.get("Interface"), "interface ${interfaceName}");
            String shutdownInterface = getString(xmlMaps.get("InterfaceShutdown"), "shutdown");
            String openInterface = getString(xmlMaps.get("InterfaceNoShutdown"), "undo shutdown");
            String newInterfaceCommand = Parser.parse0(interfaceCommand, interfaceName);
            String enablePassword = "";
            String switchCommand = switchState ? openInterface : shutdownInterface;

            //2 查询出 账号信息
            MwAccountMapper accountMapper = mwTangibleAssetsDao.selectAccountMapper(assetsId);
            QueryAccountManageParam account = mwTangibleAssetsDao.selectOneAccount(accountMapper.getAccountId());
            if (account == null) {
                return false;
            }
            if (StringUtils.isNotEmpty(account.getPassword())) {
                enablePassword = EncryptUtil.decrypt(account.getPassword());
            }
            //构建命令
            String[] commandArray = new String[]{enterEnable, enablePassword, enterConfigMode, newInterfaceCommand, switchCommand};

            //3 使用相关协议下载配置
            String protocol = account.getProtocol();
            if ("TELNET".equals(protocol)) {
                String result = mwPerfromService.telent2(param.getInBandIp(), Integer.parseInt(account.getPort()),
                        account.getUsername(), EncryptUtil.decrypt(account.getPassword()), commandArray);
                log.info(result);
                if (result.equals("login error") || result.equals("Connection timed out")) {
                    return false;
                }
                return true;
            } else if ("SSH".equals(protocol)) {
                String result = mwPerfromService.sshDownload(param.getInBandIp(), Integer.parseInt(account.getPort()),
                        account.getUsername(), EncryptUtil.decrypt(account.getPassword()), commandArray,200);
                log.info(result);
                if (result.equals("ssh cmds is null") || result.equals("ssh login error")) {
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            log.error("开关接口失败", e);
            return false;
        }
        return false;
    }

    /**
     * 获取值
     *
     * @param value        原值
     * @param defaultValue 默认值
     * @return
     */
    private String getString(String value, String defaultValue) {
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        } else {
            return value;
        }
    }

    /**
     * 获取XML的配置数据
     *
     * @param assetsId 资产ID
     * @return 配置文本数据
     * @throws DocumentException
     */
    private Map<String, String> getCommandMap(String assetsId) throws DocumentException {
        //获取配置模板
        MwTemplateMapper templateMapper = mwTangibleAssetsDao.selectTemplateMapper(assetsId);
        QueryTemplateManageParam template = mwTangibleAssetsDao.selectOneTemplate(templateMapper.getTemplateId());
        //获取模板信息
        Map<String, String> xmlMaps = new HashMap<>();
        String xml = template.getXml();
        Document doc = DocumentHelper.parseText(xml);
        XPath xPath = DocumentHelper.createXPath("//Command");
        List nodes = xPath.selectNodes(doc);
        for (Object obj : nodes) {
            Element n = (Element) obj;
            String key = n.attribute("Name").getText();
            String value = n.attribute("Value").getText();
            xmlMaps.put(key, value);
        }
        return xmlMaps;
    }
}
