package cn.mw.monitor.scanrule.service.event;

import cn.mw.monitor.event.Event;
import cn.mw.monitor.scanrule.service.impl.MwScanruleServiceImpl;
import lombok.Data;

@Data
public class AddScanruleEvent extends Event<MwScanruleServiceImpl> {

}
