package cn.mw.monitor.activiti.param;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lumingming
 * @createTime 2023814 21:50
 * @description
 */
@Data
public class NodeList {
    List<Node> nodes = new ArrayList<>();
    List<Integer> customerIds = new ArrayList<>();
}
