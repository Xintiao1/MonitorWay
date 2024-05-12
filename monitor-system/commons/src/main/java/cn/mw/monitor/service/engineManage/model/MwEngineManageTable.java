package cn.mw.monitor.service.engineManage.model;

import lombok.Data;

import java.util.Date;

/**
 * @author baochengbin
 * @date
 */
@Data
public class MwEngineManageTable {

        private String id;

        private String engineName;

        private String proxyName;

        private String serverIp;

        private String mode;

        private String description;

        private String encryption;

        private String keyConsistency;

        private String sharedKey;

        private String publisher;

        private String title;

        private String compress;

        private Integer monitorHostNumber;

        private Integer monitoringItemsNumber;

        private String performance;

        private String creator;

        private Date createDate;

        private String modifier;

        private Date modificationDate;

        private String port;

        private String proxyId;
        /**
         *删除标识符
         */
        private Boolean deleteFlag;
        /**
         * 监控服务器id
         */
        private int monitorServerId;
        /**
         * 监控服务器名称
         */
        private String monitorServerName;
        /**
         * 活动代理地址
         */
        private String proxyAddress;
}
