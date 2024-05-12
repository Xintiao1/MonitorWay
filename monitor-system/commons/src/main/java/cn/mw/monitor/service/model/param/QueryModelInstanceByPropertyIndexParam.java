package cn.mw.monitor.service.model.param;

import cn.mwpaas.common.utils.CollectionUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qzg
 * @date 2020/3/16
 */
@Data
public class QueryModelInstanceByPropertyIndexParam {
   private String propertiesIndexId;
   private int propertiesType;
   private String propertiesValue;
   private List propertiesValueList;
}
