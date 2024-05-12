package cn.mw.module.security.dto;

import cn.mwpaas.common.utils.DateUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qzg
 * @date 2021/12/21 16:55
 */
@Data
public class EsDataSourceListDto {
    public static List<EsDataSourceListInfoDto> infoList = new ArrayList<>();

    public void setInfoList(List<EsDataSourceListInfoDto> infoList){
        EsDataSourceListDto.infoList = infoList;
    }
    public List<EsDataSourceListInfoDto> getInfoList(){
        return infoList;
    }

}
