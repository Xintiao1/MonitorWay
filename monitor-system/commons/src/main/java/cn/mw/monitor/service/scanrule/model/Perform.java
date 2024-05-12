package cn.mw.monitor.service.scanrule.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Perform {
    private boolean disappear = true;
    private int percentage = 0;
}
