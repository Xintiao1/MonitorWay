package cn.mw.monitor.service.common;

import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.event.Event;
import cn.mw.monitor.event.EventListner;
import cn.mwpaas.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class ListenerService {

    List<EventListner> checklisteners = new ArrayList<EventListner>();

    List<EventListner> postProcessers = new ArrayList<EventListner>();

    List<EventListner> finishProcessers = new ArrayList<EventListner>();

    public List<Reply> publishCheckEvent(Event event) throws Throwable {
        return publishEvent(event, checklisteners);
    }

    public List<Reply> publishPostEvent(Event event) throws Throwable {
        return publishEvent(event, postProcessers);
    }

    public List<Reply> publishFinishEvent(Event event) throws Throwable {
        List<Reply> list = new ArrayList<Reply>();

        for(EventListner listener: finishProcessers){
            List<Reply> checkresult = null;
            try {
                checkresult = listener.handleEvent(event);
            }catch (Throwable throwable){
                log.error("publishFinishEvent" ,throwable);
            }
            if(null != checkresult && checkresult.size() > 0) {
                list.addAll(checkresult);
            }
        }

        return list;
    }

    private List<Reply> publishEvent(Event event, List<EventListner> listners) throws Throwable{
        List<Reply> list = new ArrayList<Reply>();
        for(EventListner listener: listners){
            Date start = new Date();
            List<Reply> checkresult = listener.handleEvent(event);
            long interval = DateUtils.between(start ,new Date() , DateUnitEnum.SECOND);
            log.info("{} handler cost {}s" ,listener.getClass().getSimpleName() ,interval);
            if(null != checkresult && checkresult.size() > 0)
                list.addAll(checkresult);
        }

        return list;
    }

    protected void addCheckLists(List listners){
        checklisteners.addAll(listners);
    }

    protected void addPostProcessorList(List listners){
        postProcessers.addAll(listners);
    }

    protected void addFinishProcessorList(List listners){
        finishProcessers.addAll(listners);
    }

}
