package cn.mw.monitor.service.model.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * @author qzg
 * @date 2023/8/28
 */
@Slf4j
public class ValConvertUtil {

    public static int intValueConvert(Object value) {
        Pattern pattern = Pattern.compile("^[+]?[\\d]*$");
        if (value == null) {
            return 0;
        }
        if (Strings.isEmpty(value.toString())) {
            return 0;
        }
        boolean isNum = pattern.matcher(value.toString()).matches();
        if (isNum) {
            return Integer.valueOf(value.toString());
        }
        return 0;
    }

    public static long longValueConvert(Object value) {
        Pattern pattern = Pattern.compile("^[+]?[\\d]*$");
        if (value == null) {
            return 0l;
        }
        if (Strings.isEmpty(value.toString())) {
            return 0l;
        }
        boolean isNum = pattern.matcher(value.toString()).matches();
        if (isNum) {
            return Long.valueOf(value.toString());
        }
        return 0l;
    }

    public static String strValueConvert(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    public static boolean booleanValueConvert(Object value) {
        boolean bool = false;
        if (value == null) {
            return bool;
        }
        bool = Boolean.parseBoolean(value.toString());
        return bool;
    }

    public static Double doubleValueConvert(Object value) {
        Pattern pattern = Pattern.compile("^[-+]?\\d*\\.?\\d+([eE][-+]?\\d+)?$");
        if (value == null) {
            return 0d;
        }
        if (Strings.isEmpty(value.toString())) {
            return 0d;
        }
        boolean isNum = pattern.matcher(value.toString()).matches();
        if (isNum) {
            return Double.valueOf(value.toString());
        }
        return 0d;
    }

    public static String readTxt(String txtPath) {
        File file = new File(txtPath);
        if (file.isFile() && file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer sb = new StringBuffer();
                String text = null;
                while ((text = bufferedReader.readLine()) != null) {
                    sb.append(text);
                }
                return sb.toString();
            } catch (Exception e) {
                log.error("fail to readTxt case:{}", e);
            }
        }
        return null;
    }
}
