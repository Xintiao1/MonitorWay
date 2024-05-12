package cn.mw.monitor.user.service.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserView {
    private Integer value;
    private String label;
}
