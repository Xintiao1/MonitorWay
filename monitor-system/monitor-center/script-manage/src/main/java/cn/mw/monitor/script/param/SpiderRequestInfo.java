package cn.mw.monitor.script.param;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author gui.quanwang
 * @className SpiderRequestInfo
 * @description 猫维蜘蛛——请求参数（新版）
 * @date 2022/5/23
 */
@Data
public class SpiderRequestInfo implements Serializable {

    /**
     * 进程ID(对应execID)
     */
    private Integer appkey;

    /**
     * 执行IP列表
     */
    private List<String> ipList;

    /**
     * 脚本类型 0.执行脚本 1.下发文件
     */
    private Integer mindType;

    /**
     * 下发的目的地址
     */
    private String ordeAddress;

    /**
     * 主账号
     */
    private String username;

    /**
     * 主账号密码
     */
    private String password;

    /**
     * 端口：不传默认是22
     */
    private Integer prot;

    /**
     * 脚本内容/下发文件地址
     */
    private String script;

    /**
     * 脚本类型：0.shelll 1.bat 2.python 3.prel 4.Powershell
     */
    private Integer scriptType;

    /**
     * 类型：1.linux 2.windows 3.mysql 4.oracle
     */
    private Integer type;

    /**
     * 账户列表
     */
    private List<RequestAccount> root;

    /**
     * true:忽略  false:不忽略
     */
    private Boolean neglect;

    /**
     * 请求服务器对应账户
     */
    public static class RequestAccount {

        /**
         * window主机= 'winrm'
         */
        private String ansible_connection;

        /**
         * IP
         */
        private String ip;

        /**
         * 主账号密码
         */
        private String password;

        /**
         * 端口：不传默认是22
         */
        private Integer prot;

        /**
         * 类型：1.linux 2.windows 3.mysql 4.oracle
         */
        private Integer type;

        /**
         * 主账号
         */
        private String username;

        /**
         * 执行SQL脚本时，对应的数据库用户名
         */
        private String sqlUsername;

        /**
         * 执行SQL脚本时，对应的数据库密码
         */
        private String sqlPassword;

        /**
         * 执行SQL脚本时，对应的数据库名称
         */
        private String sqlDatabase;

        /**
         * 执行SQL脚本时，对应的数据库命令
         */
        private String sqlText;

        /**
         * 执行SQL脚本时，对应的数据库端口
         */
        private Integer sqlProt;

        public String getAnsible_connection() {
            return ansible_connection;
        }

        public void setAnsible_connection(String ansible_connection) {
            this.ansible_connection = ansible_connection;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Integer getProt() {
            return prot;
        }

        public void setProt(Integer prot) {
            this.prot = prot;
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getSqlUsername() {
            return sqlUsername;
        }

        public void setSqlUsername(String sqlUsername) {
            this.sqlUsername = sqlUsername;
        }

        public String getSqlPassword() {
            return sqlPassword;
        }

        public void setSqlPassword(String sqlPassword) {
            this.sqlPassword = sqlPassword;
        }

        public String getSqlDatabase() {
            return sqlDatabase;
        }

        public void setSqlDatabase(String sqlDatabase) {
            this.sqlDatabase = sqlDatabase;
        }

        public String getSqlText() {
            return sqlText;
        }

        public void setSqlText(String sqlText) {
            this.sqlText = sqlText;
        }

        public Integer getSqlProt() {
            return sqlProt;
        }

        public void setSqlProt(Integer sqlProt) {
            this.sqlProt = sqlProt;
        }
    }
}
