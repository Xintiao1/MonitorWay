package cn.mw.monitor.model.service.impl;

import java.io.*;
import java.util.Properties;
import java.util.Vector;

import cn.mw.monitor.model.param.DataBaseDriverTypeEnum;
import cn.mw.monitor.model.param.MwModelEditorDataBaseConfigParam;
import cn.mw.monitor.snmp.utils.JSchInteractionShell;
import cn.mwpaas.common.model.Reply;
import com.google.common.base.Strings;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *
 * @author qzg
 * @Date 2023/07/07
 *
 * 说明:exec用于执行命令;sftp用于文件处理
 */
@Slf4j
@Service
public class SSHRemoteCall {

    // 私有的对象
    private static SSHRemoteCall sshRemoteCall;

    /**
     * 私有的构造方法
     */
    private SSHRemoteCall() {
    }

    // 懒汉式,线程不安全,适合单线程
    public static SSHRemoteCall getInstance() {
        if (sshRemoteCall == null) {
            sshRemoteCall = new SSHRemoteCall();
        }
        return sshRemoteCall;
    }

    // 懒汉式,线程安全,适合多线程
    public static synchronized SSHRemoteCall getInstance2() {
        if (sshRemoteCall == null) {
            sshRemoteCall = new SSHRemoteCall();
        }
        return sshRemoteCall;
    }

    private int port;// 端口号

    private String ipAddress;// ip地址
    private String userName;// 账号
    private String password;// 密码
    private String  remoteFilePath;//服务器配置文件地址
    private String fileName;//文件名
    private String  localFilePath;//下载到本地的文件地址
    private Session session;// JSCH session
    private boolean logined = false;// 是否登陆
    
    /**
     * 构造方法,方便直接传入ipAddress,userName,password进行调用
     *
     * @param ipAddress
     * @param userName
     * @param password
     * @param port
     */
    public SSHRemoteCall(String ipAddress, String userName, String password,int port) {
        super();
        this.ipAddress = ipAddress;
        this.userName = userName;
        this.password = password;
        this.port = port;
    }

    /**
     * 远程登陆
     *
     * @throws Exception
     */
    public void sshRemoteCallLogin(String ipAddress, String userName, String password,int port) throws Exception {
        // 如果登陆就直接返回
        if (logined) {
            return;
        }
        // 创建jSch对象
        JSch jSch = new JSch();
        try {
            // 获取到jSch的session, 根据用户名、主机ip、端口号获取一个Session对象
            session = jSch.getSession(userName, ipAddress, port);
            // 设置密码
            session.setPassword(password);
            UserInfo ui = new SSHUserInfo() {
                public void showMessage(String message) {
                }

                public boolean promptYesNo(String message) {
                    return true;
                }
            };
            session.setUserInfo(ui);
            // 方式一,通过Session建立连接
            // session.setConfig("StrictHostKeyChecking", "no");
            // session.connect();

            // 方式二,通过Session建立连接
            // java.util.Properties;
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);// 为Session对象设置properties
            // session.setTimeout(3000);// 设置超时
            session.connect(30000);
            //通过Session建立连接

            // 设置登陆状态
            logined = true;
        } catch (JSchException e) {
            // 设置登陆状态为false
            logined = false;
            throw new Exception(
                    "主机登录失败, IP = " + ipAddress + ", USERNAME = " + userName + ", Exception:" + e.getMessage());
        }
    }

    /**
     * 关闭连接
     */
    public void closeSession() {
        // 调用session的关闭连接的方法
        if (session != null) {
            // 如果session不为空,调用session的关闭连接的方法
            session.disconnect();
        }

    }

    private abstract class SSHUserInfo implements UserInfo, UIKeyboardInteractive {
        public String getPassword() {
            return null;
        }

        public boolean promptYesNo(String str) {
            return true;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message) {
            return true;
        }

        public void showMessage(String message) {
        }

        public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt,
                                                  boolean[] echo) {
            return null;
        }
    }


    /**
     * 执行相关的命令
     *
     * @param command
     * @throws IOException
     */
    public void execCommand(String command) throws IOException {
        InputStream in = null;// 输入流(读)
        Channel channel = null;// 定义channel变量
        try {
            // 如果命令command不等于null
            if (command != null) {
                // 打开channel
                //说明：exec用于执行命令;sftp用于文件处理
                channel = session.openChannel("exec");
                // 设置command
                ((ChannelExec) channel).setCommand(command);
                // channel进行连接
                channel.connect();
                // 获取到输入流
                in = channel.getInputStream();
                // 执行相关的命令
                String processDataStream = processDataStream(in);
                // 打印相关的命令
                log.info("1、打印相关返回的命令: " + processDataStream);
            }
        } catch (Exception e) {
            log.error("execCommand fail to:",e);
        } finally {
            if (in != null) {
                in.close();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }

    }

    /**
     * 对将要执行的linux的命令进行遍历
     *
     * @param in
     * @return
     * @throws Exception
     */
    public String processDataStream(InputStream in) throws Exception {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String result = "";
        try {
            while ((result = br.readLine()) != null) {
                sb.append(result);
                //log.info(sb.toString());
            }
        } catch (Exception e) {
            throw new Exception("获取数据流失败: " + e);
        } finally {
            br.close();
        }
        return sb.toString();
    }

    /**
     * 上传文件 可参考:https://www.cnblogs.com/longyg/archive/2012/06/25/2556576.html
     *
     * @param directory
     *            上传文件的目录
     * @param uploadFile
     *            将要上传的文件
     */
    public void uploadFile(String directory, String uploadFile) {
        try {
            // 打开channelSftp
            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            // 远程连接
            channelSftp.connect();
            // 创建一个文件名称问uploadFile的文件
            File file = new File(uploadFile);
            // 将文件进行上传(sftp协议)
            // 将本地文件名为src的文件上传到目标服务器,目标文件名为dst,若dst为目录,则目标文件名将与src文件名相同.
            // 采用默认的传输模式:OVERWRITE
//            channelSftp.put(new FileInputStream(file), directory, ChannelSftp.OVERWRITE);
            channelSftp.put(directory,uploadFile,ChannelSftp.OVERWRITE);
            // 切断远程连接
            channelSftp.exit();
           log.info("2、" + file.getName() + " 文件上传成功.....");
        } catch (Exception e) {
            log.error("uploadFile fail to",e);
        }

    }

    /**
     * 下载文件 采用默认的传输模式：OVERWRITE
     *
     * @param src
     *            linux服务器文件地址
     * @param dst
     *            本地存放地址
     * @throws JSchException
     * @throws SftpException
     */
    public void fileDownload(String src, String dst) throws JSchException, SftpException {
        // src 是linux服务器文件地址,dst 本地存放地址
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        // 远程连接
        channelSftp.connect();
        // 下载文件,多个重载方法
        channelSftp.get(src, dst);
        // 切断远程连接,quit()等同于exit(),都是调用disconnect()
        channelSftp.quit();
        // channelSftp.disconnect();
       log.info("3、" + src + " ,下载文件成功.....");
    }

    /**
     * 删除文件
     *
     * @throws SftpException
     * @throws JSchException
     */
    public void deleteFile(String directoryFile) throws SftpException, JSchException {
        // 打开openChannel的sftp
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        // 远程连接
        channelSftp.connect();
        // 删除文件
        channelSftp.rm(directoryFile);
        // 切断远程连接
        channelSftp.exit();
       log.info("4、" + directoryFile + " 删除的文件.....");
    }

    /**
     * 列出目录下的文件
     *
     * @param directory
     *            要列出的目录
     * @throws SftpException
     * @throws JSchException
     */
    public Vector listFiles(String directory) throws JSchException, SftpException {
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        // 远程连接
        channelSftp.connect();
        // 显示目录信息
        Vector ls = channelSftp.ls(directory);
       log.info("5、" + ls);
        // 切断连接
        channelSftp.exit();
        return ls;
    }

    /**
     * 读取写入文件
     *
     * @param localFilePath
     */
    public void readWriteFiles(String localFilePath,MwModelEditorDataBaseConfigParam configParam) throws Exception {
        try {
            //读取文件
            BufferedReader reader = new BufferedReader(new FileReader(localFilePath));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = reader.readLine())!=null){
                sb.append(line);
            }
            log.info("打印数据:"+sb.toString());
            reader.close();

            //写入
            BufferedWriter writer = new BufferedWriter(new FileWriter(localFilePath,true));
            writer.newLine();
            if(!Strings.isNullOrEmpty(configParam.getConfigDriver())){
                DataBaseDriverTypeEnum driverType = DataBaseDriverTypeEnum.getInfoByName(configParam.getConfigDriver());
                switch (driverType) {
                    case MYSQL:
                        writer.write("["+configParam.getConfigName()+"]");
                        writer.newLine();
                        writer.write("Driver="+configParam.getConfigDriver());
                        writer.newLine();
                        writer.write("Server="+configParam.getConfigServerIp());
                        writer.newLine();
                        writer.write("port="+configParam.getConfigPort());
                        writer.newLine();
                        writer.write("Trace=no");
                        writer.newLine();
                        writer.write("CHARSET=UTF8MB4");
                        break;
                    case ORACLE:
                        writer.write("["+configParam.getConfigName()+"]");
                        writer.newLine();
                        writer.write("Driver="+configParam.getConfigDriver());
                        writer.newLine();
                        writer.write("ServerName="+configParam.getConfigServerIp()+":"+configParam.getConfigPort()+"/"+configParam.getConfigInstance());
                        break;
                    default:
                        break;
                }
            }
            writer.close();
        } catch (Exception e) {
            log.error("readWriteFiles fail to:",e);
        }
    }


//    public static void main(String[] args) {
//        // 连接到指定的服务器
//        try {
//            // 1、首先远程连接ssh
//            SSHRemoteCall.getInstance().sshRemoteCallLogin(ipAddress, userName, password);
//            // 打印信息
//           log.info("连接ip地址: " + ipAddress + ",账号: " + userName + ",连接成功.....");
//
//            // 3、下载文件
//            // src 是linux服务器文件地址,dst 本地存放地址,采用默认的传输模式：OVERWRITE
//            //test为文件名称哈
//            String src = remoteFilePath+fileName;//远程服务器上的文件路径和文件名
//            String dst = localFilePath;//下载到本地的文件路径
//            SSHRemoteCall.getInstance().fileDownload(src, dst);
//
//            //4读取并写入文件
//            SSHRemoteCall.getInstance().readWriteFiles(localFilePath+fileName);
//
//
//            // 5、上传文件
//            String localFile = localFilePath+fileName;// 本地文件名
//            SSHRemoteCall.getInstance().uploadFile(localFile,src);
////
////            // 5、刪除文件
////            String deleteDirectoryFile = "/home/hadoop/package/test";
////            SSHRemoteCall.getInstance().deleteFile(deleteDirectoryFile);
////
////            // 6、展示目录下的文件信息
////            String lsDirectory = "/home/hadoop/package";
////            SSHRemoteCall.getInstance().listFiles(lsDirectory);
//
//            // 7、关闭连接
//            SSHRemoteCall.getInstance().closeSession();
//        } catch (Exception e) {
//            // 打印错误信息
//            System.err.println("远程连接失败......");
//            log.error("");
//        }
//    }

}

