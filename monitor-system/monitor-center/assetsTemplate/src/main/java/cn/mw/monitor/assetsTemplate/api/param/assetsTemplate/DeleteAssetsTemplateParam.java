package cn.mw.monitor.assetsTemplate.api.param.assetsTemplate;

import lombok.Data;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/4/8
 */
@Data
public class DeleteAssetsTemplateParam {

    private List<Integer> idList;
}
