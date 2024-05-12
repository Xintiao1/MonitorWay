package cn.mw.monitor.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MWPasswdHis {

    private Integer id;

    private Integer userId;

    private String passwd;

    public MWPasswdHis(Integer userId, String passwd) {
        this.userId = userId;
        this.passwd = passwd;
    }

}
