package cn.mw.module.solarwind.service.impl;


import cn.mw.module.solarwind.dto.SolarHistoryDto;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author xhy
 * @date 2020/7/8 14:17
 */
public abstract class MWInterfaceHistoryThread implements Callable<List<SolarHistoryDto>> {
}
