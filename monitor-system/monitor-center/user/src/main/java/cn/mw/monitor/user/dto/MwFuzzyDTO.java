package cn.mw.monitor.user.dto;

import cn.mw.monitor.user.model.ADUserDetailDTO;
import com.github.pagehelper.PageInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 *
 * @author zy.quaee
 * @date 2021/9/2 20:46
 **/
@Data
@Builder
public class MwFuzzyDTO {

    private PageInfo<?> pageInfo;

    private List<String> list;
}
