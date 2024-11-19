package com.eatpizzaquickly.reservationservice.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
@Slf4j
@EnableKafka
public class KafkaConfig {

    private final CustomKafkaErrorHandler customKafkaErrorHandler;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


//    @Bean
//    public ProducerFactory<String, Object> producerFactory() {
//        Map<String, Object> myconfig = new HashMap<>();
//        myconfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        myconfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        myconfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        myconfig.put(ProducerConfig.RETRIES_CONFIG, 3);
//        myconfig.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 3000);
//        myconfig.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 30000);
//        return new DefaultKafkaProducerFactory<>(myconfig);
//    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> myConfig = new HashMap<>();
        myConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        myConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        myConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        myConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        myConfig.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        return new DefaultKafkaConsumerFactory<>(myConfig);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConsumerFactory(consumerFactory());

        kafkaListenerContainerFactory.setCommonErrorHandler(customKafkaErrorHandler.getErrorHandler());

        kafkaListenerContainerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return kafkaListenerContainerFactory;
    }

}
