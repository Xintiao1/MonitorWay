package cn.mw.monitor.event;

import lombok.Data;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/26
 */
@Data
public class CustomColLoadEvent<T> extends Event<T> {
    private List<Integer> userIds;

    public CustomColLoadEvent(List<Integer> userIds){
        this.userIds = userIds;
    }
    /*
    * private Integer userId;

    public CustomColLoadEvent(Integer userId){
        this.userId = userId;
    }*/
}
