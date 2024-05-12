package cn.mwpaas.common.model;


import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回对象
 *
 * @author zhoupenghong
 */
@Data
@Builder
@AllArgsConstructor
public class Reply<T> implements Serializable {

    private static final long serialVersionUID = 4950490623871055901L;
    /**
     * 返回状态码，默认0
     */
    private Integer res;
    /**
     * 返回消息
     */
    private String msg;
    /**
     * 返回对象
     */
    private T data;

    /**
     * 消息流
     */
    private byte[] bytes;

    /**
     * 流水号
     */
    private String seqNo;

    /**
     * 返回时间
     */
    private String time;

    public Reply() {
        this.res = PaasConstant.RES_SUCCESS;
        this.time = DateUtils.now();
    }

    public Reply(Integer res, String msg) {
        this.res = res;
        this.msg = msg;
        this.time = DateUtils.now();
    }

    public Reply(Integer res, String msg, T data) {
        this.data = data;
        this.res = res;
        this.msg = msg;
        this.time = DateUtils.now();
    }

    /**
     * 成功
     *
     * @param data
     * @return
     */
    public static Reply ok(Object data) {
        return Reply.builder()
                .res(PaasConstant.RES_SUCCESS)
                .msg(PaasConstant.RES_SUCCESS_MSG)
                .data(data)
                .time(DateUtils.now())
                .build();
    }

    /**
     * 成功
     *
     * @return
     */
    public static Reply ok() {
        return Reply.builder()
                .res(PaasConstant.RES_SUCCESS)
                .msg(PaasConstant.RES_SUCCESS_MSG)
                .time(DateUtils.now())
                .build();
    }


    /**
     * 失败
     *
     * @param res 错误编码
     * @param msg 错误消息
     * @return
     */
    public static Reply fail(int res, String msg) {
        return Reply.builder().res(res).msg(msg).data(null).time(DateUtils.now()).build();
    }

    /**
     * 失败
     *
     * @param msg 错误消息
     * @return
     */
    public static Reply fail(String msg) {
        return Reply.builder().res(PaasConstant.RES_ERROR).msg(msg).data(null).time(DateUtils.now()).build();
    }

    public static Reply fail(String msg, Object data) {
        return Reply.builder().res(PaasConstant.RES_ERROR).msg(msg).data(data).time(DateUtils.now()).build();
    }

    public static Reply fail(Object data) {
        return Reply.builder().res(PaasConstant.RES_ERROR).msg(null).data(data).time(DateUtils.now()).build();
    }

    public static Reply warn(String msg) {
        return Reply.builder().res(PaasConstant.RES_WARN).msg(msg).data(null).time(DateUtils.now()).build();
    }

    public static Reply warn(String msg, String[] strs) {
        return Reply.warn(replaceMsg(msg, strs));
    }

    public static Reply tooltip(String msg) {
        return Reply.builder().res(PaasConstant.RES_WARN).msg(msg).data(null).time(DateUtils.now()).build();
    }

    public static String replaceMsg(String msg, String[] params) {
        int i = 1;
        for (String param : params) {
            StringBuilder sb = new StringBuilder();
            sb.append("#\\{").append(i).append("\\}");
            msg = msg.replaceAll(sb.toString(), param);
            i++;
        }
        return msg;
    }
}
