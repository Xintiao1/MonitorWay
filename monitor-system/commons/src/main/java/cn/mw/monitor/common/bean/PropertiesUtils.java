package cn.mw.monitor.common.bean;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

@Component
public class PropertiesUtils implements EmbeddedValueResolverAware {

    private StringValueResolver stringValueResolver;

    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        stringValueResolver = resolver;
    }

    public  String getPropertiesValue(String name){
        return stringValueResolver.resolveStringValue("${"+name+"}");

    }
}
