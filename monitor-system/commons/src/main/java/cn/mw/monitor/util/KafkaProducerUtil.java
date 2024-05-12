package cn.mw.monitor.util;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Component
public class KafkaProducerUtil implements InitializingBean {

    @Value("${spring.kafka.bootstrap-servers}")
    private String servers;

    private KafkaProducer<String, String> producer;

    private static final Logger log = LoggerFactory.getLogger("KafkaProducerUtil");

    public void send(String topic, List<String> value) {

        //构造一个消息队列Kafka版消息。
        try {
            List<Future<RecordMetadata>> futures = new ArrayList<Future<RecordMetadata>>(5);
            for (String s : value){
                ProducerRecord<String, String> kafkaMessage =  new ProducerRecord<String, String>(topic, s);
                Future<RecordMetadata> future = producer.send(kafkaMessage);
                futures.add(future);
            }
            producer.flush();
            for(Future<RecordMetadata> future : futures){
                try{
                    RecordMetadata recordMetadata = future.get();
                }catch (Exception e){
                    log.error("kafka错误信息", e);
                }
            }
        } catch (Exception e) {
            //客户端内部重试之后，仍然发送失败，业务要应对此类错误。
            log.error("kafka发送失败：", e);
        }

    }
    /***
     * type: 1,结尾匹配；2,开头匹配
     ***/
    public static String getLikeByMap(ConcurrentHashMap<String, MwTangibleassetsDTO> map, String keyLike){
        for(Map.Entry<String, MwTangibleassetsDTO> entry : map.entrySet()){
            if(entry.getValue().getId().equals(keyLike)){
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Properties props = new Properties();
        //设置接入点，请通过控制台获取对应Topic的接入点。
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        //消息队列Kafka版消息的序列化方式。
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        //请求的最长等待时间。
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30 * 1000);
        //设置客户端内部重试次数。
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        //设置客户端内部重试间隔。
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 3000);
        //构造Producer对象，注意，该对象是线程安全的，一般来说，一个进程内一个Producer对象即可。
        //如果想提高性能，可以多构造几个对象，但不要太多，最好不要超过5个。
        producer = new KafkaProducer<String, String>(props);
    }
}
