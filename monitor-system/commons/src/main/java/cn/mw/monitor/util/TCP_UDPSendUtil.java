package cn.mw.monitor.util;

import cn.mw.monitor.util.entity.TCP_UDPFrom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;

@Component
public class TCP_UDPSendUtil {

    private static final Logger log = LoggerFactory.getLogger("TCP_UDPSendUtil");


    public static Integer TCPSendByTLS(TCP_UDPFrom form, String message) throws IOException {
        SSLSocket socket = null;
        Integer errCode = -1;
        try{
            SSLContext sslContext = SSLContext.getInstance(form.getTls());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(form.getAlgorithm());
            KeyStore keyStore = KeyStore.getInstance(form.getKeyType());
            keyStore.load(null);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            String certificateAlias = "cer";
            keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(new FileInputStream(form.getPath())));
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            socket = (SSLSocket)sslContext.getSocketFactory().createSocket(form.getHost(), form.getPort());
            socket.setKeepAlive(true);
            OutputStream os = socket.getOutputStream();
            os.write(message.getBytes());
            socket.close();
            errCode = 0;
        }catch (Exception e){
            log.error("TCP通过TLS发送失败：", e);
            if(socket != null){
                socket.close();
            }
        }
        return errCode;
    }

    public static Integer TCPSend(String host, int port, String message) throws IOException {
        Socket socket = null;
        Integer errCode = -1;
        try{
            socket = new Socket(host,port);
            OutputStream os = socket.getOutputStream();
            os.write(message.getBytes());
            socket.close();
            errCode = 0;
        }catch (Exception e){
            log.error("TCP连接错误：", e);
            if(socket != null){
                socket.close();
            }
        }
        return errCode;
    }

    public static Integer UDPSend(String host, int port, String message){
        DatagramSocket datagramSocket = null;
        Integer errCode = -1;
        try{
            datagramSocket = new DatagramSocket();
            DatagramPacket datagramPacket = new DatagramPacket(message.getBytes("GBK"),message.getBytes("GBK").length, InetAddress.getByName(host),port);
            datagramSocket.send(datagramPacket);
            datagramSocket.close();
            errCode = 0;
        }catch (Exception e){
            log.error("UDP发送错误：", e);
            if(datagramSocket != null){
                datagramSocket.close();
            }
        }
        return errCode;
    }


}
