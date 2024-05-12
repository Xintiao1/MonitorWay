package cn.mw.monitor.websocket;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author syt
 * @Date 2020/10/22 14:50
 * @Version 1.0
 */
@Data
public class RealTimeUpdateDataDTO {
    //已读消息
    private List<Message> readMessage = new ArrayList<>();
    //未读消息
    private List<Message> unReadMessage = new ArrayList<>();
    //最新消息
    private Message newMessage = new Message();
    //已读消息数量
    private Integer readMessageCount=0;
    //未读消息数量
    private Integer unReadMessageCount=0;
    //进度条是否打开
    private boolean isScan = false;
    //已通过数量
    private Integer totalCount = 0;
    //百分比
    private Integer percentScan = 0;

    //哪个地址段进度条
    private Integer belongId = 0;
    //由我审批A
    private Integer myActviti = 0;

    //我提交的
    private Integer mySubMitActiviti = 0;

    //代办审批数量
    private Integer unfinishActiviti=0;

    private Object dataInfo;

    //正在扫描队列的ip列表
    private List<Integer> linkIds;
}

