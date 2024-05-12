package cn.mw.monitor.service.model.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author qzg
 * @date 2020/3/16
 */
@Data
public class QueryModelInstanceByPropertyIndexParamList {
   private Boolean skipDataPermission;
   private Boolean isQueryAssetsState;
   private List<QueryModelInstanceByPropertyIndexParam> paramLists;
}
