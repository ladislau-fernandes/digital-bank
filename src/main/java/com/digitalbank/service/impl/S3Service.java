package com.digitalbank.service.impl;

import com.digitalbank.entity.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public void saveTransactionReceipt(Transaction transaction) {
        try {
            String key = "receipts/%s/%s.json".formatted(
                    transaction.getCreatedAt().toLocalDate(),
                    transaction.getId()
            );

            Map<String, Object> receipt = new HashMap<>();
            receipt.put("transactionId", transaction.getId());
            receipt.put("type", transaction.getType());
            receipt.put("amount", transaction.getAmount());
            receipt.put("status", transaction.getStatus());
            receipt.put("description", transaction.getDescription());
            receipt.put("createdAt", transaction.getCreatedAt().toString());

            if (transaction.getSourceAccount() != null) {
                receipt.put("sourceAccount", transaction.getSourceAccount().getAccountNumber());
            }
            if (transaction.getTargetAccount() != null) {
                receipt.put("targetAccount", transaction.getTargetAccount().getAccountNumber());
            }

            String json = objectMapper.writeValueAsString(receipt);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/json")
                    .build();

            s3Client.putObject(request, RequestBody.fromString(json));

            log.info("Comprovante salvo no S3 | bucket={} | key={}", bucketName, key);

        } catch (S3Exception ex) {
            log.error("Erro ao salvar comprovante no S3 | transactionId={} | erro={}",
                    transaction.getId(), ex.getMessage());
        } catch (Exception ex) {
            log.error("Erro inesperado ao salvar comprovante | transactionId={} | erro={}",
                    transaction.getId(), ex.getMessage());
        }
    }
}
