package cn.mw.monitor.service.scan;

import net.percederberg.mibble.Mib;

import java.util.Map;

public interface MWTopoBasic {
    Map<String, Mib> getMibMap();
}
