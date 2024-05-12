package cn.mw.monitor.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author xhy
 * @date 2021/2/19 11:56
 */
@Data
public class MwModelManageDtoV2 extends ModelManageDto  {

    private List<ModelManageDto> childs;

    public void addChild(MwModelManageDtoV2 mwModelManageDto) {
        if (null == childs) {
            childs = new ArrayList<>();
        }
        childs.add(mwModelManageDto);
    }

    public void addChild(List<MwModelManageDtoV2> mwModelManageDtoList) {
        if (null == childs){
            childs = new ArrayList<>();
        }
        childs.addAll(mwModelManageDtoList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MwModelManageDtoV2 mwModelManageDto = (MwModelManageDtoV2) o;
        return Objects.equals(getModelId(), mwModelManageDto.getModelId()) &&
                Objects.equals(getModelName(), mwModelManageDto.getModelName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getModelId(), getModelName());
    }

}
