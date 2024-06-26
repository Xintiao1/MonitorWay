package cn.joinhealth.echarts.echart;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author xiewenfeng Echarts抽象工厂
 */

public abstract class EchartsFactory {

	public abstract AbstractBarSimple createBarSimple(Map<String, Object> parameters, Class<?> cls);

	public abstract AbstractBarCount createBarCount(Map<String, Object> parameters, Class<?> cls);

	public abstract AbstractBarStack createBarStack(Map<String, Object> parameters, Class<?> cls);

	public abstract AbstractDeviceBarStack createDeviceStack(Map<String, Object> parameters, Class<?> cls);

	public abstract AbstractLineSimple createLineSimple(Map<String, Object> parameters, Class<?> cls);

	public abstract AbstractPieSimple createPieSimple(Map<String, Object> parameters, Class<?> cls);

	public abstract AbstractLineStack createLineStack(Map<String, Object> parameters, Class<?> cls);

	public abstract AbstractBarYCategory createBarYCategory(Map<String, Object> parameters, Class<?> cls);

	public abstract AbstractAreaStack createAreaStack(Map<String, Object> parameters, Class<?> cls);
}
