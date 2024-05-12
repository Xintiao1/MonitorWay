package cn.mwpaas.common.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Random;

/**
 * @author phzhou
 * @ClassName MathUtils
 * @CreateDate 2019/3/15
 * @Description
 */
public class MathUtils {

    /**
     * 两个数字生成百分比
     * @param x
     * @param total
     * @return
     */
    public static String getPercent(int x, int total) {
        if (total == 0) {
            return "0.00%";
        }
        String result;//接受百分比的值

        double xDouble = x * 1.0;
        double tempresult = xDouble / total;
        //当比例超100%时,按100计
        if(xDouble>=total){
            result="100%";
        }else{
            DecimalFormat df1 = new DecimalFormat("0.00%");    //##.00%   百分比格式，后面不足2位的用0补齐
            result = df1.format(tempresult);
        }
        return result;
    }

    /**
     * 两个数字生成百分比
     *
     * @param x
     * @param total
     * @return
     */
    public static double getPercentValue(double x, double total) {
        if (total == 0) {
            return 0;
        }
        double tempResult = x * 100 / total;
        if (x >= total) {
            return 100;
        } else {
            //##.00
            BigDecimal bigDecimal = new BigDecimal(tempResult);
            return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
    }

    /**
     * 两个数字生成百分比
     *
     * @param x
     * @param total
     * @return
     */
    public static double getPercentValue(long x, long total) {
        if (total == 0) {
            return 0;
        }
        double tempResult = x * 100.0 / total;
        if (x >= total) {
            return 100;
        } else {
            //##.00
            BigDecimal bigDecimal = new BigDecimal(tempResult);
            return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
    }

    /**
     * 两个数字生成百分比
     *
     * @param x
     * @param total
     * @return
     */
    public static String getPercent(double x, double total) {
        if (total == 0) {
            return "0.00%" ;
        }
        String result;//接受百分比的值

        double tempresult = x / total;
        //当比例超100%时,按100计
        if (x >= total) {
            result = "100%" ;
        } else {
            //##.00%   百分比格式，后面不足2位的用0补齐
            DecimalFormat df1 = new DecimalFormat("0.00%");
            result = df1.format(tempresult);
        }
        return result;
    }

    /**
     * 两个数字生成百分比
     * @param x
     * @param total
     * @return
     */
    public static String getPercent(long x, long total) {
        if (total == 0) {
            return "0.00%";
        }
        String result;//接受百分比的值

        double xDouble = x * 1.0;
        double tempresult = xDouble / total;
        //当比例超100%时,按100计
        if(xDouble>=total){
            result="100%";
        }else{
            //##.00%   百分比格式，后面不足2位的用0补齐
            DecimalFormat df1 = new DecimalFormat("0.00%");
            result = df1.format(tempresult);
        }
        return result;
    }

    /**
     * 两个double数字生成百分比
     * @param x
     * @param total
     * @param format 百分比格式,如0.00%
     * @return
     */
    public static String getPercent(double x, double total, String format) {
        if (total == 0) {
            return "0.00%";
        }
        String result;//接受百分比的值

        double xDouble = x * 1.0;
        double tempresult = xDouble / total;
        //当比例超100%时,按100计
        if(xDouble>=total){
            result="100%";
        }else{
            DecimalFormat df1 = new DecimalFormat(format);    //##.00%   百分比格式，后面不足2位的用0补齐
            result = df1.format(tempresult);
        }
        return result;
    }

    /**
     * 产生n位随机数的验证码
     * @param n
     * @return
     */
    public static String getRandomVerifiedCode(int n) {
        Random ran = new Random();
        if (n == 1) {
            return String.valueOf(ran.nextInt(10));
        }
        int bitField = 0;
        char[] chs = new char[n];
        for (int i = 0; i < n; i++) {
            while (true) {
                int k = ran.nextInt(10);
                if ((bitField & (1 << k)) == 0) {
                    bitField |= 1 << k;
                    chs[i] = (char) (k + '0');
                    break;
                }
            }
        }
        return new String(chs);
    }

}
