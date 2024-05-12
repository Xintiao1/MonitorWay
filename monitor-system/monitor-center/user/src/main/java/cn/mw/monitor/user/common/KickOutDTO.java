package cn.mw.monitor.user.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.shiro.subject.Subject;

/**
 * Created by zy.quaee on 2021/5/24 15:14.
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KickOutDTO {

    private Subject currentUser;

    private String loginName;
}
