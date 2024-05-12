package cn.mw.monitor.service.model.param;

import cn.mw.monitor.service.model.dto.ModelPropertiesStructDto;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MwCustomColByModelDTO extends MwPagefieldByModelTable implements Comparable<MwCustomColByModelDTO> {

    // id
    private Integer customId;
    // 列ID
    private Integer colId;
    // 用户ID
    private Integer userId;
    // 是否排序
    private Boolean sortable;
    // 宽度
    private Integer width;
    // 是否可见
    private Boolean visible;
    // 顺序数
    private Integer orderNumber;
    //是否还原
    private Integer deleteFlag;

    private List<ModelPropertiesStructDto> propertiesStruct;

    //1:文本 2:时间 5:下拉框
    private String inputFormat;

    public void extractFrom(PropertyInfo propertyInfo) {
        setModelPropertiesId(propertyInfo.getPropertiesId());
        setPropertiesTypeId(propertyInfo.getPropertiesTypeId());
        setType(propertyInfo.getPropertiesTypeId().toString());
        setPropertiesType(propertyInfo.getPropertiesType());
        setProp(propertyInfo.getIndexId());
        setLabel(propertyInfo.getPropertiesName());
        setOrderNumber(propertyInfo.getSort());
        setOrderNum(propertyInfo.getSort());
        setIsRead(booleanTransfer(propertyInfo.getIsRead()));
        setIsMust(booleanTransfer(propertyInfo.getIsMust()));
        setIsOnly(booleanTransfer(propertyInfo.getIsOnly()));

        setIsInsertShow(propertyInfo.getIsInsertShow());
        setIsEditorShow(propertyInfo.getIsEditorShow());
        setIsLookShow(propertyInfo.getIsLookShow());
        setIsListShow(propertyInfo.getIsListShow());

        setRegex(propertyInfo.getRegex());
        setDefaultValue(propertyInfo.getDefaultValue());
        setRelationModelIndex(propertyInfo.getRelationModelIndex());
        setRelationPropertiesIndex(propertyInfo.getRelationPropertiesIndex());
        setJudgeCycle(propertyInfo.getJudgeCycle());
        ModelPropertiesType type = ModelPropertiesType.getTypeByCode(propertyInfo.getPropertiesTypeId());
        switch (type) {
            case SINGLE_ENUM:
            case MULTIPLE_ENUM:
                List list = (List) type.convertValue(propertyInfo.getPropertyValue());
                setDropOp(list);

                list = (List) type.convertValue(propertyInfo.getDefaultValue());
                setDefaultValueList(list);
                break;
            case RELATION_ENUM:
                list = (List) type.convertValue(propertyInfo.getPropertyValue());
                setDropArrObj(list);
                break;
            case STRUCE:
                list = (List) type.convertValue(propertyInfo.getPropertyValue());
                setPropertiesStruct(list);
        }

        setInputFormat(type.getInputFormat());
    }

    private String booleanTransfer(boolean data) {
        return data ? "1" : "0";
    }

    @Override
    public int compareTo(MwCustomColByModelDTO o) {
        return orderNumber - o.getOrderNumber();
    }
}
