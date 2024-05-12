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
public class MwModelManageDto extends ModelManageDto  {

    private List<ModelManageDto> childs;

    public void addChild(MwModelManageDto mwModelManageDto) {
        if (null == childs) {
            childs = new ArrayList<>();
        }
        childs.add(mwModelManageDto);
    }

    public void addChild(List<MwModelManageDto> mwModelManageDtoList) {
        if (null == childs){
            childs = new ArrayList<>();
        }
        childs.addAll(mwModelManageDtoList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MwModelManageDto mwModelManageDto = (MwModelManageDto) o;
        return Objects.equals(getModelId(), mwModelManageDto.getModelId()) &&
                Objects.equals(getModelName(), mwModelManageDto.getModelName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getModelId(), getModelName());
    }

}
