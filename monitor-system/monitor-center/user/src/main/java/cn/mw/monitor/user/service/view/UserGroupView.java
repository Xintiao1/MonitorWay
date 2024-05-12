package cn.mw.monitor.user.service.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserGroupView {
    private Integer value;
    private String label;
}
