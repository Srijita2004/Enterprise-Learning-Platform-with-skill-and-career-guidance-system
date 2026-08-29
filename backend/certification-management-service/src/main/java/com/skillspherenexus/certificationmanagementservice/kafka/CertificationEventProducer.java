package com.skillspherenexus.certificationmanagementservice.kafka;

import com.skillspherenexus.certificationmanagementservice.event.CertificateIssuedEvent;
import com.skillspherenexus.certificationmanagementservice.event.CertificateRenewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes M3 domain events to Kafka. Failures are logged rather than
 * thrown so that a Kafka outage never breaks certification/renewal flows.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CertificationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.certificate-issued}")
    private String certificateIssuedTopic;

    @Value("${kafka.topic.certificate-renewed}")
    private String certificateRenewedTopic;

    public void publishCertificateIssued(CertificateIssuedEvent event) {
        try {
            kafkaTemplate.send(certificateIssuedTopic, String.valueOf(event.getCertificationId()), event);
            log.info("Published CertificateIssuedEvent for certificationId={}", event.getCertificationId());
        } catch (Exception ex) {
            log.error("Failed to publish CertificateIssuedEvent for certificationId={}", event.getCertificationId(), ex);
        }
    }

    public void publishCertificateRenewed(CertificateRenewedEvent event) {
        try {
            kafkaTemplate.send(certificateRenewedTopic, String.valueOf(event.getCertificationId()), event);
            log.info("Published CertificateRenewedEvent for certificationId={}", event.getCertificationId());
        } catch (Exception ex) {
            log.error("Failed to publish CertificateRenewedEvent for certificationId={}", event.getCertificationId(), ex);
        }
    }
}
