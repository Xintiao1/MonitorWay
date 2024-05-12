package cn.mw.monitor.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author xhy
 * @date 2021/2/19 14:38
 */
@Data
public class MwModelInstanceRedirectDto extends ModelManageTypeDto {

    private List<MwModelManageTypeDto> childs;
    private Boolean instance;

    public void addChild(MwModelManageTypeDto mwModelManageTypeDto) {
        if (null == childs) {
            childs = new ArrayList<>();
        }
        childs.add(mwModelManageTypeDto);
    }

    public void addChild(List<MwModelManageTypeDto> mwModelManageTypeDtos) {
        if (null == childs) {
            childs = new ArrayList<>();
        }
        childs.addAll(mwModelManageTypeDtos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MwModelInstanceRedirectDto mwModelManageTypeDto = (MwModelInstanceRedirectDto) o;
        return Objects.equals(getModelGroupId(), mwModelManageTypeDto.getModelGroupId()) &&
                Objects.equals(getModelGroupName(), mwModelManageTypeDto.getModelGroupName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getModelGroupId(), getModelGroupName());
    }

}
