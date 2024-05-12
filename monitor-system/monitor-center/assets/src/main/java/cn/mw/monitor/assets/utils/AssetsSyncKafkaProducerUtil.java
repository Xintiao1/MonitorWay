package cn.mw.monitor.assets.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * @author gengjb
 * @description kafka发送消息工具类
 * @date 2023/12/3 18:26
 */
@Slf4j
@Component
public class AssetsSyncKafkaProducerUtil {

    @Value("${assets.sync.kafka.server}")
    private String kafkaServers;

    @Value("${assets.sync.kafka.topic}")
    private String kafkaTopic;

    @Value("${assets.sync.kafka.key}")
    private String kafkaKey;

    @Value("${assets.sync.kafka.authentication}")
    private Boolean isAuthentication;

    @Value("${assets.sync.kafka.username}")
    private String kafkaUserName;

    @Value("${assets.sync.kafka.password}")
    private String kafkaPassword;

    /**
     * 发送消息
     * @param message
     */
    public void sendKafkaMessage(String message){
        // 设置Kafka生产者的配置
        Properties properties = new Properties();

        //设置接入点，请通过控制台获取对应Topic的接入点。
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
        //消息队列Kafka版消息的序列化方式。
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        if(isAuthentication){
            log.info("AssetsSyncKafkaProducerUtil{} sendKafkaMessage() kafkausername:"+kafkaUserName+"kafkapassword:"+kafkaPassword);
            //设置身份验证信息
            properties.put("security.protocol", "SASL_PLAINTEXT");
            properties.put("sasl.mechanism", "PLAIN");
            properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""+kafkaUserName+"\" password=\""+kafkaPassword+"\";");
        }


        //请求的最长等待时间。
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30 * 1000);
        //设置客户端内部重试次数。
        properties.put(ProducerConfig.RETRIES_CONFIG, 5);
        //设置客户端内部重试间隔。
        properties.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 3000);

        // 创建Kafka生产者实例
        Producer<String, String> producer = new KafkaProducer<>(properties);
        log.info("AssetsSyncKafkaProducerUtil{} sendKafkaMessage() sendKafkaInfo>>kafkaServers:"
                +kafkaServers+">>kafkaTopic:"+kafkaTopic+">>kafkaKey"+kafkaKey);

        // 发送消息
        ProducerRecord<String, String> record = new ProducerRecord<>(kafkaTopic, kafkaKey, message);
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception == null) {
                    log.info("AssetsSyncKafkaProducerUtil{} sendKafkaMessage() success");
                } else {
                    log.error("AssetsSyncKafkaProducerUtil{} sendKafkaMessage() error::"+exception.getMessage());
                }
            }
        });
        // 关闭生产者
        producer.close();
    }
}
