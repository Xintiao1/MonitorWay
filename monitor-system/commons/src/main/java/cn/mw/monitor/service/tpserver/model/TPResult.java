package cn.mw.monitor.service.tpserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TPResult {
    private boolean isSuccess;
    private String message;


}
