package cn.mw.monitor.timetask.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeProcessResult {

    private boolean isSuccess;
    private String message;

}
