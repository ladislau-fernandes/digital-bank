package com.digitalbank.kafka.producer;

import com.digitalbank.kafka.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventProducer {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @Value("${kafka.topics.transactions}")
    private String transactionTopic;

    public void sendTransactionEvent(TransactionEvent event) {
        log.info("Publicando evento Kafka | tipo={} | transactionId={} | valor={}",
                event.type(), event.transactionId(), event.amount());

        CompletableFuture<SendResult<String, TransactionEvent>> future =
                kafkaTemplate.send(transactionTopic, event.transactionId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Falha ao publicar evento Kafka | transactionId={} | erro={}",
                        event.transactionId(), ex.getMessage());
            } else {
                log.info("Evento Kafka publicado com sucesso | transactionId={} | partition={} | offset={}",
                        event.transactionId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
