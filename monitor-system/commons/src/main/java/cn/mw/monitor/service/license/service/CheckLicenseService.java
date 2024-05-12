package cn.mw.monitor.service.license.service;

import cn.mwpaas.common.model.Reply;

import java.text.ParseException;

public interface CheckLicenseService {
    void insertLicenseInfo(String moduleId,int day);
    void deleteLicenseInfo(String moduleId);
    Reply queryLicenseInfo() throws Exception;
    Reply queryLicenseList() throws ParseException;

}
