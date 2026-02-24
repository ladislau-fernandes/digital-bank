package com.digitalbank.kafka.consumer;

import com.digitalbank.kafka.event.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumer {

    @KafkaListener(
            topics = "${kafka.topics.transactions}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            @Payload TransactionEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Evento recebido do Kafka | partition={} | offset={} | tipo={} | transactionId={}",
                partition, offset, event.type(), event.transactionId());

        try {
            processNotification(event);
        } catch (Exception ex) {
            log.error("Erro ao processar notificação | transactionId={} | erro={}",
                    event.transactionId(), ex.getMessage());
        }
    }

    private void processNotification(TransactionEvent event) {
        String message = switch (event.type()) {
            case DEPOSIT -> String.format(
                    "Depósito de R$ %.2f recebido na conta %s",
                    event.amount(), event.targetAccountNumber()
            );
            case WITHDRAWAL -> String.format(
                    "Saque de R$ %.2f realizado na conta %s",
                    event.amount(), event.sourceAccountNumber()
            );
            case TRANSFER -> String.format(
                    "Transferência de R$ %.2f da conta %s para a conta %s",
                    event.amount(), event.sourceAccountNumber(), event.targetAccountNumber()
            );
            case PIX -> String.format(
                    "PIX de R$ %.2f da conta %s para a conta %s",
                    event.amount(), event.sourceAccountNumber(), event.targetAccountNumber()
            );
        };

        // Aqui você pode integrar com email (JavaMail), SMS (Twilio), push notification, etc.
        log.info("📲 Notificação gerada: {}", message);
    }
}
