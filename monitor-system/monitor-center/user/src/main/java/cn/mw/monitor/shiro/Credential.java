package cn.mw.monitor.shiro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Credential {

    private String loginName;

    private String password;

    private String salt;

    private String hashTypeId;

}
