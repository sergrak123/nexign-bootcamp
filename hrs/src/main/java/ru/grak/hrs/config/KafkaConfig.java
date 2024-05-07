package ru.grak.hrs.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.grak.common.dto.CallDataRecordPlusDto;
import ru.grak.common.dto.CostDataDto;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    //Cost Data to BRT
    @Bean
    public ProducerFactory<String, CostDataDto> producerFactory() {

        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, CostDataDto> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    //CDR+ from BRT
    @Bean
    public ConsumerFactory<String, CallDataRecordPlusDto> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        JsonDeserializer<CallDataRecordPlusDto> callDataRecordDeserializer =
                new JsonDeserializer<>();
        callDataRecordDeserializer.addTrustedPackages("*");

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), callDataRecordDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CallDataRecordPlusDto> kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, CallDataRecordPlusDto> concurrentKafkaListenerContainerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();

        concurrentKafkaListenerContainerFactory.setConsumerFactory(consumerFactory());

        return concurrentKafkaListenerContainerFactory;
    }
}
