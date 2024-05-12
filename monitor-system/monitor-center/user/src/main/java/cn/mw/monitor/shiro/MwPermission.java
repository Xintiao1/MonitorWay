package cn.mw.monitor.shiro;

import lombok.Data;
import org.apache.shiro.authz.Permission;

@Data
public class MwPermission implements Permission {

    private String uri;

    public MwPermission(String uri){
        this.uri = uri;
    }

    @Override
    public boolean implies(Permission permission) {
        return false;
    }

}
