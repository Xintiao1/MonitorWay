package cn.mw.monitor.dropDown.api.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddDropDownParam {

    // 下拉框code
    private String dropCode;
    // 下拉框key
    private Integer dropKey;
    // 下拉框value
    private String dropValue;
    //下拉框ID
    private Integer dropId;

    //该标签操作类型 1:新增  2 修改  3 删除
    private Integer operateType;

}
