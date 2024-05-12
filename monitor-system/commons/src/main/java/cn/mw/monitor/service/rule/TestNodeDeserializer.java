package cn.mw.monitor.service.rule;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Type;
import java.util.List;

public class TestNodeDeserializer implements ObjectDeserializer {
    @Override
    public List<TestNode> deserialze(DefaultJSONParser defaultJSONParser, Type type, Object o) {
        List<TestNode> testNode = defaultJSONParser.parseObject(List.class ,new TestNodeParserConfig());
        return testNode;
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
