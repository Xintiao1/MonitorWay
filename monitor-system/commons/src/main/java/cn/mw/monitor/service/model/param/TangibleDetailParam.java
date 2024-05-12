package cn.mw.monitor.service.model.param;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import lombok.Data;

/**
 * @ClassName TangibleDetailParam
 * @Description 资产详情参数
 * @Author gengjb
 * @Date 2023/2/12 14:38
 * @Version 1.0
 **/
@Data
public class TangibleDetailParam extends BaseDetailParam{

    private String assetsName;

    public void extractFrom(MwTangibleassetsDTO mwTangibleassetsDTO){
        super.extractFrom(mwTangibleassetsDTO);
        this.assetsName = mwTangibleassetsDTO.getAssetsName();
    }
}
