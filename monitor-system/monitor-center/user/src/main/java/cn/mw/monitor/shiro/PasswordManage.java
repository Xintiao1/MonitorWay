package cn.mw.monitor.shiro;

import cn.mw.monitor.user.dto.HashTypeDTO;
import cn.mw.monitor.user.service.HashTypeService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PasswordManage implements InitializingBean {

    @Autowired
    private HashTypeService hashTypeService;

    private Map<String, HashTypeDTO> hashMap;
    private DefaultHashService defaultHashService;

//    public static void main(String[] args) {
//        DefaultHashService dd = new DefaultHashService();
//        HashRequest request = new HashRequest
//                .Builder()
//                .setAlgorithmName("MD5")
//                .setSource(ByteSource.Util.bytes("zy169693"))
//                .setSalt(ByteSource.Util.bytes("zhaoymwmwMonitor@"))
//                .setIterations(2)
//                .build();
//        ////System.out.println(dd.computeHash(request).toBase64());
//    }
    public String encryptPassword(Credential credential){
        HashTypeDTO hashTypeDTO = hashMap.get(credential.getHashTypeId());
        String salt = credential.getLoginName() + credential.getSalt() + Constants.SYS_SALT;
        HashRequest request = new HashRequest
                .Builder()
                .setAlgorithmName(hashTypeDTO.getHashName())
                .setSource(ByteSource.Util.bytes(credential.getPassword()))
                .setSalt(ByteSource.Util.bytes(salt))
                .setIterations(hashTypeDTO.getIterations())
                .build();

        return defaultHashService.computeHash(request).toBase64();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        hashMap = hashTypeService.selectMap();
        defaultHashService = new DefaultHashService();
    }
}
