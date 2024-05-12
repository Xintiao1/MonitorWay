package cn.mw.monitor.service.rule;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Type;

public class TestNodeParserConfig extends ParserConfig {

    @Override
    public ObjectDeserializer getDeserializer(Type type) {
        //就这一句是自定义的，如果解析遇到P1类型的，则按照我们自定义的类型去解析 this.clazz
        if (type.getTypeName().indexOf("TestNode") >=0 ) {
            return super.getDeserializer(TestNode.class);
        }
        return super.getDeserializer(type);
    }
}
