package cn.mw.monitor.configmanage.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaSsh implements Runnable {
    protected static final Logger logger = LoggerFactory.getLogger(JavaSsh.class);
    /**
     * 退格
     */
    private static final String BACKSPACE = new String(new byte[] { 8 });

    /**
     * ESC
     */
    private static final String ESC = new String(new byte[] { 27 });

    /**
     * 空格
     */
    private static final String BLANKSPACE = new String(new byte[] { 32 });

    /**
     * DbSqlUpdate.txt
     */
    private static final String ENTER = new String(new byte[] { 13 });

    /**
     * 某些设备回显数据中的控制字符
     */
    private static final String[] PREFIX_STRS = { BACKSPACE + "+" + BLANKSPACE + "+" + BACKSPACE + "+",
            "(" + ESC + "\\[\\d+[A-Z]" + BLANKSPACE + "*)+" };

    private int sleepTime = 500;

    /**
     * 连接超时(单次命令总耗时)
     */
    private int timeout = 5000;

    /**
     * 保存当前命令的回显信息
     */
    protected StringBuffer currEcho;

    /**
     * 保存所有的回显信息
     */
    protected StringBuffer totalEcho;

    private String ip;
    private int port;
    private String endEcho = "#,?,>,:";
    private String moreEcho = "---- More ----";
    private String moreCmd = BLANKSPACE;
    private JSch jsch = null;
    private Session session;
    private Channel channel;

    @Override
    public void run() {
        InputStream is;
        try {
            is = channel.getInputStream();
            String echo = readOneEcho(is);
            while (echo != null) {
                currEcho.append(echo);
                String[] lineStr = echo.split("\\n");
                if (lineStr != null && lineStr.length > 0) {
                    String lastLineStr = lineStr[lineStr.length - 1];
                    if (lastLineStr != null && lastLineStr.indexOf(moreEcho) > 0) {
                        totalEcho.append(echo.replace(lastLineStr, ""));
                    } else {
                        totalEcho.append(echo);
                    }
                }
                echo = readOneEcho(is);
            }
        } catch (IOException e) {
            logger.error("转化报错:",e);
        }
    }

    protected String readOneEcho(InputStream instr) {
        byte[] buff = new byte[1024];
        int ret_read = 0;
        try {
            ret_read = instr.read(buff);
        } catch (IOException e) {
            return null;
        }
        if (ret_read > 0) {
            String result = new String(buff, 0, ret_read);
            for (String PREFIX_STR : PREFIX_STRS) {
                result = result.replaceFirst(PREFIX_STR, "");
            }
            try {
                return new String(result.getBytes(), "GBK");
            } catch (UnsupportedEncodingException e) {
                logger.info("出错了 转化有问题",e);
                return null;
            }
        } else {
            return null;
        }
    }

    public JavaSsh(String ip, int port, String endEcho, String moreEcho) {
        this.ip = ip;
        this.port = port;
        if (endEcho != null) {
            this.endEcho = endEcho;
        }
        if (moreEcho != null) {
            this.moreEcho = moreEcho;
        }
        totalEcho = new StringBuffer();
        currEcho = new StringBuffer();
    }

    private void close() {
        logger.info("被执行关闭了");
        if (session != null) {
            session.disconnect();
        }
        if (channel != null) {
            channel.disconnect();
        }
    }

    private boolean login(String username,String password) {
        String user = username;
        String passWord = password;
        jsch = new JSch();
        try {
            session = jsch.getSession(user, this.ip, this.port);
            session.setPassword(passWord);
            UserInfo ui = new SSHUserInfo() {
                public void showMessage(String message) {
                }

                public boolean promptYesNo(String message) {
                    return true;
                }
            };
            session.setUserInfo(ui);
            session.connect(10000);
            channel = session.openChannel("shell");
          /*  ((ChannelShell)channel).setEnv("LANG", "en_US.UTF-8");*/
            channel.connect(10000);
            logger.error("正在连接指定设备");
            new Thread(this).start();
            try {
                Thread.sleep(sleepTime);
            } catch (Exception e) {
            }
            return true;
        } catch (JSchException e) {
            return false;
        }
    }

    protected void sendCommand(String command, boolean sendEnter) {
        try {
            OutputStream os = channel.getOutputStream();
            os.write(command.getBytes());
            os.flush();
            if (sendEnter) {
                currEcho = new StringBuffer();
                os.write(ENTER.getBytes());
                os.flush();
            }
        } catch (IOException e) {
            logger.error("发送命令失败",e);
        }
    }

    protected boolean containsEchoEnd(String echo) {
        boolean contains = false;
        if (endEcho == null || endEcho.trim().equals("")) {
            return contains;
        }
        if (endEcho.toString()==null||endEcho.toString().trim().equals("")||endEcho.toString().trim().toLowerCase().contains("password")){
            return true;
        }
        String[] eds = endEcho.split(",");
        for (String ed : eds) {
            if (echo.trim().endsWith(ed)) {

                contains = false;
                logger.info("千万别走这这边测试有问题"+echo+"有问题"+ed);
//                break;
            }
        }
        if (contains==false){
            logger.info("这边测试有问题");
        }
        return contains;
    }

    private String runCommand(String command, boolean ifEnter,int longTime) {
        currEcho = new StringBuffer();
        sendCommand(command, ifEnter);
        int time = 0;
        if (endEcho == null || endEcho.equals("")) {
            while (currEcho.toString().equals("")) {
                try {
                    Thread.sleep(longTime);
                    time += sleepTime;
                    if (time >= timeout) {
                        logger.info("由于空格超时");
                        break;
                    }
                } catch (InterruptedException e) {
                    logger.error("执行命令失败",e);
                }
            }
        } else {
            logger.info("执行命令失败:"+command);
                try {
                    Thread.sleep(longTime);
                } catch (InterruptedException e) {
                    logger.info("由于异常被终止了",e);
                }


        }
        return currEcho.toString();
    }

    private String batchCommand(String[] cmds, int[] othernEenterCmds,int longTime) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < cmds.length; i++) {
            String cmd = cmds[i];
            if (cmd.equals("")) {
                continue;
            }
            boolean ifInputEnter = false;
            if (othernEenterCmds != null) {
                for (int c : othernEenterCmds) {
                    if (c == i) {
                        ifInputEnter = true;
                        break;
                    }
                }
            }
            cmd += (char) 10;
            String resultEcho = runCommand(cmd, ifInputEnter,longTime);
            sb.append(resultEcho);
        }
        close();
        return totalEcho.toString();
    }

    public String executive(String username,String password,String[] cmds, int[] othernEenterCmds,int longTime) {
        if (cmds == null || cmds.length < 1) {
            logger.error("{} ssh cmds is null", this.ip);
            return "ssh cmds is null";
        }
        if (login(username,password)) {
            return batchCommand(cmds, othernEenterCmds,longTime);
        }
        logger.error("{} ssh login error", this.ip);
        return "ssh login error";
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


    public static String StringtoUTF(String charset, String content) {
        String newStr = "";
        try {
            newStr = new String(content.getBytes(charset), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e);
        }
        return newStr;
    }



    public static void main(String[] args) throws UnsupportedEncodingException {
        String str =  "核心防火墙";
        str= new String(str.getBytes("gbk"), "gbk");
        System.out.println(str);
        str= new String(str.getBytes("gbk"), "UTF-8");
        System.out.println(str);
        str= new String(str.getBytes("UTF-8"), "gbk");
        System.out.println(str);
        str= new String(str.getBytes("gbk"), "UTF-8");
        System.out.println(str);
        str= new String(str.getBytes("UTF-8"), "gbk");
        System.out.println(str);

    }
}
