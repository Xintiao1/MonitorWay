package cn.mw.monitor.report.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName MwLinkGradeParam
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/10/28 15:49
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MwLinkGradeParam {

    private List<String> ids;
}
