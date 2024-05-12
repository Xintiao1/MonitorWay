package cn.mw.module.security.service;

import cn.mw.module.security.dto.EsSysLogAuditQueryDTO;
import cn.mwpaas.common.model.Reply;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author qzg
 * @date 2021/12/16
 */
public interface EsSysLogAuditService {
    Reply getSystemLogInfos(EsSysLogAuditQueryDTO param);

    Reply getSystemLogNums(EsSysLogAuditQueryDTO param);

    Reply getSysLogTree(EsSysLogAuditQueryDTO param);

    Reply fuzzSearchFiled(EsSysLogAuditQueryDTO param);

    Reply sysLogExport(EsSysLogAuditQueryDTO param,HttpServletRequest request, HttpServletResponse response);

    Reply initDataSourceState();
}
