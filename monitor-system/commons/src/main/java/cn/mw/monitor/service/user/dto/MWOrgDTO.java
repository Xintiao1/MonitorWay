package cn.mw.monitor.service.user.dto;

import cn.mw.monitor.service.user.model.MWOrg;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MWOrgDTO extends MWOrg {

    // 页面显示机构名称
    private String wrapperOrgName;
    // 下级机构
    private List<MWOrgDTO> childs;

    public void addChild(MWOrgDTO mwOrgList) {
        if (null == childs) {
            childs = new ArrayList<MWOrgDTO>();
        }
        childs.add(mwOrgList);
    }

    public void addChild(List<MWOrgDTO> mwOrgList) {
        if (null == childs){
            childs = new ArrayList<>();
        }
        childs.addAll(mwOrgList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MWOrgDTO mwOrg = (MWOrgDTO) o;
        return Objects.equals(getOrgId(), mwOrg.getOrgId()) &&
                Objects.equals(getOrgName(), mwOrg.getOrgName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrgId(), getOrgName());
    }

}
