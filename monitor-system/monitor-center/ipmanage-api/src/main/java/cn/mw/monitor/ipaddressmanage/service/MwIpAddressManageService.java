package cn.mw.monitor.ipaddressmanage.service;

import cn.mw.monitor.service.ipmanage.model.IpManageTree;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.ipaddressmanage.param.*;
import com.github.pagehelper.PageInfo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * bkc
 */
public interface MwIpAddressManageService {

    // 导出  ip地址管理 table页数据
    void batchExport(ExportIpAddressListParam uParam, HttpServletResponse response) throws IOException;

    // 批量删除  ip地址管理 table页数据
    Reply batchDelete(AddUpdateIpAddressManageListParam uParam);

    // 批量修改  ip地址管理 table页数据
    Reply batchUpdate(AddUpdateIpAddressManageListParam uParam);

    //查询ip地址管理 table页数据
    Reply selectSnoList(QueryIpAddressManageListParam queryIpAddressManageParam) throws IllegalAccessException, NoSuchMethodException, Exception;

    //查询ip地址管理 图形数据
    Reply selectPicture(IpAddressManageTableParam queryIpAddressManageParam);

    //查询ip地址管理
    Reply selectList(QueryIpAddressManageParam queryIpAddressManageParam) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    //查询ip地址编辑管理
    Reply selectList1(QueryIpAddressManageParam queryIpAddressManageParam);

    //删除ip地址管理
    Reply delete(AddUpdateIpAddressManageParam record) throws Exception;

    //修改ip地址管理
    Reply update(AddUpdateIpAddressManageParam record) throws Exception;

    //添加ip地址管理
    Reply insert(AddUpdateIpAddressManageParam record) throws Exception;

    //添加ip地址管理
    AddUpdateIpAddressManageParam insert1(AddUpdateIpAddressManageParam record) throws Exception;

    //查询国家 省 市 区
    Reply queryLocAdress(QueryLocAddressParam param) throws Exception;

    //查询ip地址管理
    Reply selectGitList(QueryIpAddressManageParam queryIpAddressManageParam) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    PageInfo selectKillIP(Map<String, Object> map, QueryIpAddressManageParam qParam) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    Map<String, String> serchIpPermit(AddDisConnection qParam, Boolean ipType) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    Map<String, String> sendSms();

    PageInfo selectKillIPHistory(Map<String, Object> map, QueryIpAddressManageParam qParam) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    Reply updateIPConnection(AddDisConnection qParam, String type);

    String sendWx(String IP,String Test);

    void sendwxMsagger(HashMap<String, String> wxmap , List<String> tousers );

    void createAnble();

    Reply getIpaddressList();

    Reply ipaddressStatusHisBrow(AddIpaddresStatusHis uParam) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    PageInfo<IpManageTree> countFenOrHui(Date date, Date date1, Integer pageNumber, Integer pageSize);

}
