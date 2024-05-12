package cn.mw.monitor.screen.dto;

import cn.mw.monitor.service.assets.model.AssetTypeIconDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @ClassName MWNewScreenAssetsClassifyDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/2/16 12:20
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MWNewScreenAssetsClassifyDto {

    private String typeName;

    private String url;

    private int count;

    public void extractFrom(String typeNmae, String url, int count){
       this.typeName = typeNmae;
       this.url = url;
       this.count = count;
    }
}
