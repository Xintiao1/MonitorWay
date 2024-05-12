package cn.mw.monitor.configmanage.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketException;

import org.apache.commons.net.telnet.TelnetClient;


public class TelnetConnection {

    private TelnetClient telnet = null;

    public TelnetClient getTelnet() {
        return telnet;
    }

    private String prompt = ">]";
    private String loginPrompt = null;

    private String usernamePrompt = "Username:";
    private String passwordPrompt = "Password:";


    private InputStream in;
    private PrintStream out;

    public TelnetConnection(String host, int port) {
        if(telnet == null) {
            telnet = new TelnetClient();
            try {
                telnet.connect(host, port);
                in = telnet.getInputStream();
                out = new PrintStream(telnet.getOutputStream());
            } catch (SocketException e) {
                if(telnet!=null){
                    disconnect();
                    telnet = null;
                }
            } catch (IOException e) {
                if(telnet!=null){
                    disconnect();
                    telnet = null;
                }
            }
        }
    }

    public String login(String username, String password, String prompt) {
        StringBuffer sb = new StringBuffer();

        //处理命令行的提示字符
        if(prompt != null && !"".equals(prompt)) {
            this.prompt = prompt;
        }
        String str1 = readUntil1(this.usernamePrompt);
        sb.append(str1);
        write(username);

        String str2 = readUntil1(this.passwordPrompt);
        sb.append(str2);
        write(password);

        String str3 = readUntil2(this.prompt);
        if("login failed".equals(str3)){
            return str3;
        }
        sb.append(str3);
        if(this.loginPrompt != null){
            String str4 = readUntil(this.loginPrompt);
            sb.append(str4);
        }
        return sb.toString();
    }


    public String readUntil1(String pattern) {
        StringBuffer sb = new StringBuffer();
        try {
            int len = 0;
            while((len = in.read()) != -1) {
                sb.append((char)len);
                if(sb.toString().endsWith(pattern)) {
                    return sb.toString();
                }
            }
        } catch (IOException e) {
        }
        return "";
    }

    public String readUntil2(String pattern) {
        StringBuffer sb = new StringBuffer();
        try {
            int len = 0;
            while((len = in.read()) != -1) {
                sb.append((char)len);
                if(sb.toString().endsWith("failed")){
                    return "login failed";
                }
                if(pattern.indexOf((char)len) != -1 || sb.toString().endsWith(pattern)) {
                    return sb.toString();
                }
            }
        } catch (IOException e) {
        }
        return "";
    }


    public String readUntil(String pattern) {
        StringBuffer sb = new StringBuffer();
        try {
            int len = 0;
            while((len = in.read()) != -1) {
                sb.append((char)len);
                if(pattern.indexOf((char)len) != -1 || sb.toString().endsWith(pattern)) {
                    String s1 = sb.toString();
                    int index = s1.indexOf("  ---- More ----");
                    if(index != -1){
                        s1 = s1.replaceAll("[\u001B]","");
                        s1 = s1.replaceAll("\\[","");
                        String reg = s1.substring(index,index+31+1+6);
                        s1 = s1.replaceAll(reg,"");
                    }
                    return s1;
                }else if(sb.toString().endsWith("  ---- More ----")){
                    write("");
                }
            }
        } catch (IOException e) {
            return e.getMessage();
        }
        return "";
    }


    public void write(String value) {
        try {
            out.println(value);
            out.flush();
        } catch (Exception e) {
        }
    }

    public String sendCommand(String command) {
        try {
            write(command);
            return readUntil(prompt);
        } catch (Exception e) {
        }
        return "";
    }

    public void sendCommandEnable(String command,String password) {
        try {
            write(command);
            write(password);
        } catch (Exception e) {
        }

    }

    /** * 关闭连接 */
    public void disconnect() {
        try {
            telnet.disconnect();
        } catch (Exception e) {
        }
    }

    /**
     * @return the prompt
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * @param prompt the prompt to set
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /**
     * @return the usernamePrompt
     */
    public String getUsernamePrompt() {
        return usernamePrompt;
    }

    /**
     * @param usernamePrompt the usernamePrompt to set
     */
    public void setUsernamePrompt(String usernamePrompt) {
        this.usernamePrompt = usernamePrompt;
    }

    /**
     * @return the passwordPrompt
     */
    public String getPasswordPrompt() {
        return passwordPrompt;
    }

    /**
     * @param passwordPrompt the passwordPrompt to set
     */
    public void setPasswordPrompt(String passwordPrompt) {
        this.passwordPrompt = passwordPrompt;
    }

    /**
     * @return the loginPrompt
     */
    public String getLoginPrompt() {
        return loginPrompt;
    }

    /**
     * @param loginPrompt the loginPrompt to set
     */
    public void setLoginPrompt(String loginPrompt) {
        this.loginPrompt = loginPrompt;
    }

    /**
     * 关闭打开的连接
     * @param telnet
     */
    public void close(TelnetClient telnet) {
        if(telnet != null) {
            try {
                telnet.disconnect();
            } catch (IOException e) {
            }
        }

        if(this.telnet != null) {
            try {
                this.telnet.disconnect();
            } catch (IOException e) {
            }
        }
    }



}
