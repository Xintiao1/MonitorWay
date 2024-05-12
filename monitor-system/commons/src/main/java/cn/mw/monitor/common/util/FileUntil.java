package cn.mw.monitor.common.util;

import cn.mw.monitor.util.OkHttpUtil;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lumingming
 * @createTime 2023531 15:03
 * @description
 */
public class FileUntil {
    private FileUntil(){}
    private  static  final FileUntil single = new FileUntil();

    public static  FileUntil getInstance(){
        return single;
    }

    public List<Object> ExeclImport(Class<?> clazz, MultipartFile file,boolean haveSwagger,boolean isMap){
        List<Object> objects = new ArrayList<>();
        if (isMap){

        }else {

        }

        return objects;
    }

}
