package cn.mw.monitor.service.alert.callback;

import java.util.List;

public interface BusinessIdsFetch {
    final static String MESSAGECONTEXT_KEY = "BusinessIdsFetch";
    BusinessIds getBusinessIds();
}
