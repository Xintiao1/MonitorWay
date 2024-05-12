package cn.mw.monitor.event;

import lombok.Data;

@Data
public class Event<T> {
    private T source;
}
