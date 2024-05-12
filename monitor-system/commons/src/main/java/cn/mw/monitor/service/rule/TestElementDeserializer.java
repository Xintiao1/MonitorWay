package cn.mw.monitor.service.rule;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Type;

public class TestElementDeserializer implements ObjectDeserializer {
    @Override
    public String deserialze(DefaultJSONParser defaultJSONParser, Type type, Object o) {
        Object value = defaultJSONParser.parse();
        return "test";
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
