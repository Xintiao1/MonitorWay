package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.activitiAndMoudle.ModelServer;
import cn.mw.monitor.service.model.param.MwModelInstanceCommonParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.util.Pinyin4jUtil;
import cn.mw.monitor.visualized.constant.VisualizedConstant;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @ClassName MwVisualizedModuleModelSubregion
 * @Description 组件区模型分区查询
 * @Author gengjb
 * @Date 2023/4/17 10:20
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedModuleModelSubregion implements MwVisualizedModule {

    @Autowired
    private ModelServer modelSever;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Override
    public int[] getType() {
        return new int[]{54};
    }

    @Override
    public Object getData(Object data) {
        try {
            List<MwModelInstanceCommonParam> commonParamList = mwModelViewCommonService.getModelSystemIndexIdAndInstanceInfo(VisualizedConstant.MODEL_ID);
            if(CollectionUtils.isEmpty(commonParamList)){return commonParamList;}
            //数据排序
            Comparator<Object> com = Collator.getInstance(Locale.CHINA);
            Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
            List<MwModelInstanceCommonParam> dataList = commonParamList.stream().sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getModelInstanceName()), pinyin4jUtil.getStringPinYin(o2.getModelInstanceName()))).collect(Collectors.toList());
            return dataList;
        }catch (Throwable e){
            log.error("可视化组件区查询模型分区失败",e);
            return null;
        }
    }
}
