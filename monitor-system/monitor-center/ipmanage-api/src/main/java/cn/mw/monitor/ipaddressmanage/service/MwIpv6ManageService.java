package cn.mw.monitor.ipaddressmanage.service;

import cn.mw.monitor.ipaddressmanage.param.*;
import cn.mw.monitor.ipaddressmanage.paramv6.AddUpdateIpv6ManageParam;
import cn.mw.monitor.ipaddressmanage.paramv6.QueryIpv6ManageParam;
import cn.mwpaas.common.model.Reply;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;



public interface MwIpv6ManageService {

    //查询ip地址编辑管理
    Reply editorSelect(QueryIpv6ManageParam queryIpAddressManageParam);

    //查询ip地址管理
    Reply selectList(QueryIpAddressManageParam qParam) throws Exception;

    //删除ip地址管理
    Reply delete(AddUpdateIpv6ManageParam record) throws Exception;

    //修改ip地址管理
    Reply update(AddUpdateIpv6ManageParam record) throws Exception;

    //添加ip地址管理
    Reply insert(AddUpdateIpv6ManageParam record) throws Exception;

    //查询ip地址子表
    Reply selectSonList(QueryIpAddressManageListParam qParam) throws Exception;

    //查询ip地址子表上方图片数据
    Reply selectPicture(QueryIpv6ManageParam qParam) throws Exception;

    // 导出  ip地址管理 table页数据
    void batchExport(ExportIpAddressListParam uParam, HttpServletResponse response) throws IOException;

    // 批量删除  ip地址管理 table页数据
    Reply batchDelete(AddUpdateIpAddressManageListParam uParam);

    // 批量修改  ip地址管理 table页数据
    Reply batchUpdate(AddUpdateIpAddressManageListParam uParam);

    //ipv6获取历史信息
    Reply getHisList(AddUpdateIpAddressManageListParam parm) throws Exception;


    Reply ipv6ListaddList(AddUpdateIpAddressManageListParam uParam);
}
