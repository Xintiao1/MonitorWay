package cn.mw.monitor.graph;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Data
public class GraphContext {
    public static final String UNKONWN="unknown";

    @Value("${mw.graph.enable}")
    private boolean graphEnable = false;
}
