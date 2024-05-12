/*
package cn.mw.monitor.alert.service.impl;

import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.event.Event;
import cn.mw.monitor.service.assets.api.IMWAssetsListener;
import cn.mw.monitor.service.assets.api.IMWBatchAssetsProcFinListener;
import cn.mw.monitor.service.assets.event.AddTangibleassetsEvent;
import cn.mw.monitor.service.assets.event.BatchDeleteAssetsEvent;
import cn.mw.monitor.service.assets.event.UpdateTangibleassetsEvent;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.assets.param.DeleteTangAssetsID;
import cn.mw.monitor.util.KafkaProducerUtil;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


*/
/**
 * @author
 * @date
 *//*

@Service
@Slf4j
public class AlertAssetsListenerImpl implements IMWBatchAssetsProcFinListener, IMWAssetsListener {
    private static final Logger logger = LoggerFactory.getLogger(AlertAssetsListenerImpl.class);

    @Autowired
    private KafkaProducerUtil kafkaProducerUtil;

    @Override
    public List<Reply> handleEvent(Event event) throws Throwable {
        AddUpdateTangAssetsParam param = new AddUpdateTangAssetsParam();
        List<String> keys = new ArrayList<>();
        if (event instanceof AddTangibleassetsEvent) {
            //添加
            AddTangibleassetsEvent addEvent = (AddTangibleassetsEvent) event;
            param = addEvent.getAddTangAssetsParam();
            String key = AlertAssetsEnum.Assets.toString() + AlertAssetsEnum.Dash.toString() + AlertAssetsEnum.Add.toString() + AlertAssetsEnum.Dash.toString() + param.getId();
            keys.add(key);
            if(CollectionUtils.isNotEmpty(param.getAssetsLabel())){
                String labelKey = AlertAssetsEnum.Label.toString() + AlertAssetsEnum.Dash.toString() + AlertAssetsEnum.Add.toString() + AlertAssetsEnum.Dash.toString() + param.getId();
                keys.add(labelKey);
            }
        } else if (event instanceof UpdateTangibleassetsEvent) {
            //更新
            UpdateTangibleassetsEvent updateEvent = (UpdateTangibleassetsEvent) event;
            param = updateEvent.getUpdateTangAssetsParam();
            String key = AlertAssetsEnum.Assets.toString() + AlertAssetsEnum.Dash.toString() + AlertAssetsEnum.Add.toString() + AlertAssetsEnum.Dash.toString() + param.getId();
            keys.add(key);
            String labelKey = AlertAssetsEnum.Label.toString() + AlertAssetsEnum.Dash.toString() + AlertAssetsEnum.Add.toString() + AlertAssetsEnum.Dash.toString() + param.getId();
            keys.add(labelKey);
        }else if(event instanceof BatchDeleteAssetsEvent){
            //删除
            List<DeleteTangAssetsID> assetsIDS = ((BatchDeleteAssetsEvent) event).getDeleteTangAssetsIDList();
            if(CollectionUtils.isNotEmpty(assetsIDS)){
                for(DeleteTangAssetsID s : assetsIDS){
                    String key = AlertAssetsEnum.Assets.toString() + AlertAssetsEnum.Dash.toString() + AlertAssetsEnum.Del.toString() + AlertAssetsEnum.Dash.toString() + s.getId();
                    keys.add(key);
                    String labelKey = AlertAssetsEnum.Label.toString() + AlertAssetsEnum.Dash.toString() + AlertAssetsEnum.Del.toString() + AlertAssetsEnum.Dash.toString() + s.getId();
                    keys.add(labelKey);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(keys)){
            kafkaProducerUtil.send("assets-message",keys);
        }
        return null;
    }

}
*/
