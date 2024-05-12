package cn.mw.monitor.configmanage.service;

import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;


@Service
@Slf4j
@Transactional
public class MwPerfromService {

    //telnet 单命令
    public String telent1(String ip,int port,String username,String password,String cmd) {
        try {
            TelnetConnection telnet = new TelnetConnection(ip, port);
            if(telnet.getTelnet()==null){
                return "Connection timed out";
            }
            String loginStr = telnet.login(username, password, null);
            if("login failed".equals(loginStr)){
                return "login failed";
            }
            String cmdResult = telnet.sendCommand(cmd);
            telnet.disconnect();

            return loginStr+cmdResult;
        } catch (Exception e) {
            log.error("telnet命令执行失败",e);
        }
        return null;
    }

    //telnet 多命令
    public String telent2(String ip,int port,String username,String password,String[] cmd) {
        try {
            TelnetConnection telnet = new TelnetConnection(ip, port);
            if(telnet.getTelnet()==null){
                return "Connection timed out";
            }
            String loginStr = telnet.login(username, password, null);
            if("login failed".equals(loginStr)){
                return "login failed";
            }

            for (int i = 0; i < cmd.length; i++) {
                if (cmd[i].equals("enable")){
                    telnet.sendCommandEnable(cmd[i],cmd[i+1]);
                }else {
                    String s = telnet.sendCommand(cmd[i]);
                    loginStr +=s;
                }
            }

            telnet.disconnect();
            loginStr = loginStr;
            return loginStr;
        } catch (Exception e) {
            log.error("telnet命令执行失败",e);
        }
        return null;
    }

    //ssh   单或多命令
    public String sshDownload(String ip,int port,String username,String password,String[] cmds,int longTime) {
        try {
            JavaSsh JavaSsh2 = new JavaSsh(ip, port, null, null);
            String executive = JavaSsh2.executive(username,password,cmds, null,longTime);
            List<String> cmdlist = Arrays.asList(cmds);

            String txt = "";
            String [] tests = executive.split("\n");
            for (String test:tests) {
                if (!cmdlist.contains(test.trim())){
                    if (!test.contains("More")){
                        txt = txt+"\n"+test;
                    }
                }
            }
            return txt;
        } catch (Exception e) {
            log.error("外部报错",e);
        }
        return null;
    }


    /*//telnet 多命令
    public String configPerform33333(String ip,int port,String username,String password,String[] cmd) {
        try {
            String result = "";
            MwTelentUtil telent = new MwTelentUtil(ip,port);
            String loginInfo = telent.login(username,password,"");
            if(loginInfo.equals("login error")){
                return "login error";
            }

            result +=loginInfo;

            for (int i = 0; i < cmd.length; i++) {
                String s = telent.sendCommand(cmd[i]);
                result +=s;
            }

            telent.disconnect();

            return result;
        } catch (Exception e) {

        }
        return null;
    }

    //telnet 单命令
    public String telentDownload33333(String ip,int port,String username,String password,String cmd) {
        try {
            MwTelentUtil telent = new MwTelentUtil(ip,port);
            String loginInfo = telent.login(username,password,"");
            if(loginInfo.equals("login error")){
                return "login error";
            }

            String cmdResult = telent.sendCommand(cmd);

            telent.disconnect();

            return loginInfo+cmdResult;
        } catch (Exception e) {

        }
        return null;
    }



*/



    /*public String main() {
        try {
            String ip = "10.18.5.201";
            int port = 22;
            String username = "admin";
            String password = "admin@123";

            JavaSsh JavaSsh2 = new JavaSsh(ip, port, null, null);
            String[] cmds = {"display current config"};
            String executive = JavaSsh2.executive(username,password,cmds, null);
            ////System.out.println(executive);
        } catch (Exception e) {

        }finally {

        }
        return null;
    }*/





}
