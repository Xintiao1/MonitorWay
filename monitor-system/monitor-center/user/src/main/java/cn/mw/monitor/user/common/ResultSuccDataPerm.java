package cn.mw.monitor.user.common;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.listener.LoginContext;
import cn.mw.monitor.interceptor.DataPermissionSql;
import cn.mw.monitor.user.dao.MWUserDao;
import cn.mw.monitor.service.user.dto.LoginInfo;
import cn.mw.monitor.service.user.dto.MWOrgDTO;
import cn.mw.monitor.service.user.dto.MwRoleDTO;
import cn.mw.monitor.user.service.MWGroupService;
import cn.mw.monitor.state.DataPermission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * @author xhy
 * @date 2020/5/12 14:17
 */
@Component
@Scope("prototype")
@Slf4j
public class ResultSuccDataPerm implements DataPermissionSql {
    //登录上下文信息获取接口
    @Autowired
    private ILoginCacheInfo loginCacheInfoInfo;

    private String personColumn;

    private MwRoleDTO roleDTO;

    private List<MWOrgDTO> orgs;

    private String loginName;

    private List<Integer> groupsId;

    private final static String appendSqlPeffix = "select a.* from (";

    private String countAppendSqlSuffix;

    private String appendSqlSuffix;

    @Resource
    private MWUserDao mwUserDao;

    @Override
    public String changCountsql(String sql) {
        StringBuffer sb = new StringBuffer(sql);
        if(null != countAppendSqlSuffix) {
            sb.append(countAppendSqlSuffix);
        }
        log.info(sb.toString());
        return sb.toString();
    }

    @Override
    public String changeSql(String sql) {
        StringBuffer sb = new StringBuffer(appendSqlPeffix).append(sql);
        sb.append(appendSqlSuffix);
        log.info(sb.toString());
        return sb.toString();
    }

    public void init() throws Exception {
        loginName = loginCacheInfoInfo.getLoginName();
        roleDTO = loginCacheInfoInfo.getRoleInfo();

        LoginContext loginContext = loginCacheInfoInfo.getCacheInfo(loginName);
        LoginInfo loginInfo = loginContext.getLoginInfo();
        groupsId = loginInfo.getUser().getUserGroup();
        orgs = loginInfo.getOrgs();

        try {
            DataPermission dataPermission = DataPermission.valueOf(roleDTO.getDataPerm());

            switch (dataPermission){
                case PRIVATE:
                    StringBuffer sbf = new StringBuffer(") a where a.");
                    Set<String> loginNames = mwUserDao.getPrivateUserByLoginName(loginName);
                    String newLoginNames = loginNames.toString().substring(1,loginNames.toString().length()-1);
                    sbf.append(personColumn).append(" in (").append(newLoginNames).append(")");
                    appendSqlSuffix = sbf.toString();

                    sbf = new StringBuffer(" and ");
                    sbf.append(personColumn).append(" in (").append(newLoginNames).append(")");
                    countAppendSqlSuffix = sbf.toString();
                    break;
                case PUBLIC:
                    appendSqlSuffix = genPublicAppendSqlSuffix(false);
                    countAppendSqlSuffix = genPublicAppendSqlSuffix(true);
                    break;
                default:
            }
        }catch (Exception e){
            String[] param = new String[]{this.loginName, roleDTO.getDataPerm()};
            String message = Reply.replaceMsg(ErrorConstant.DATAPERMISSIONCODE_MSG_290001 + e.getMessage(), param);
            log.error(message);
            appendSqlSuffix = " ) where 0=1";
        }

    }

    private String genPublicAppendSqlSuffix(boolean isCount){
        StringBuffer gsb = new StringBuffer();
        groupsId.forEach(value ->{
            gsb.append(MWGroupService.GROUPID_SEPERATOR).append(value.toString());
        });

        String groupStr = (gsb.length() > 0?gsb.toString().substring(1):"");

        StringBuffer orgsb = new StringBuffer("|");
        orgs.forEach(value ->{
            orgsb.append(value.getNodes());
        });
        String orgs = orgsb.substring(1);
        StringBuffer sbs = new StringBuffer();
        if(!isCount){
            sbs.append(") a where ");
        }else{
            sbs.append(" and ");
        }

        String ret = "";
        sbs.append(" (exists (select user_id from view_user_org user where user.login_name = a." )
                .append(personColumn).append(" and user.nodes regexp '").append(orgs).append("')");
        ;

        if(gsb.length() > 0){
            sbs.append(" or ")
                    .append("exists (select user_id from view_user_group g where g.login_name = a.").append(personColumn)
                    .append(" and FIND_IN_SET (group_id,'").append(groupStr).append("'))")
            ;
        }

        sbs.append(")");

        ret = isCount?sbs.toString().replaceAll("a\\.", ""):sbs.toString();

        return ret;
    }

    public String getPersonColumn() {
        return personColumn;
    }

    public void setPersonColumn(String personColumn) {
        this.personColumn = personColumn;
    }
}
