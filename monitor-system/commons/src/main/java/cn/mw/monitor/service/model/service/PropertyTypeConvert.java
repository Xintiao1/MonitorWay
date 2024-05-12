package cn.mw.monitor.service.model.service;

public interface PropertyTypeConvert<T ,P> {
     T convert(String value);
     String strValue(P value);
     default boolean matchType(Object obj){
          return true;
     }
}
