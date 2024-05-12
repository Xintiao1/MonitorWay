package cn.mw.monitor.user.service.view;

import cn.mw.monitor.service.user.dto.MWOrgDTO;
import lombok.Data;
import java.util.List;

@Data
public class OrgView {
    private Integer value;
    private String label;
    private List<OrgView> children;

    public void addChildren(MWOrgDTO mwOrgDTO){
        List<MWOrgDTO> childList = mwOrgDTO.getChilds();
        for(MWOrgDTO org : childList) {
            OrgView orgView = new OrgView();
            orgView.setValue(org.getOrgId());
            orgView.setLabel(org.getOrgName());
            if (null != org.getChilds() && org.getChilds().size() > 0) {
                orgView.addChildren(org);
            }
            children.add(orgView);
        }
    }
}
