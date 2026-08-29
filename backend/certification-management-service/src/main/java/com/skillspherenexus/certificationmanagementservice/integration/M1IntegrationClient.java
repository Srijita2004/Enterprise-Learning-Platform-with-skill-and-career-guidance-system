package com.skillspherenexus.certificationmanagementservice.integration;

import com.skillspherenexus.certificationmanagementservice.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class M1IntegrationClient {
    private final RestClient restClient;
    private final String baseUrl;
    private final boolean validationEnabled;

    public M1IntegrationClient(RestClient restClient,
                               @Value("${integration.m1.base-url}") String baseUrl,
                               @Value("${integration.m1.employee-validation-enabled:true}") boolean validationEnabled) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
        this.validationEnabled = validationEnabled;
    }

    public LegacyEmployee getEmployee(Integer employeeId) {
        if (!validationEnabled) {
            return new LegacyEmployee(employeeId, "Employee " + employeeId, null, null);
        }
        try {
            LegacyEmployee employee = restClient.get().uri(baseUrl + "/employee/{id}", employeeId)
                    .header("X-User-Role", "ADMIN")
                    .header("X-User-Id", "system-m3")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new ExternalServiceException("Employee reference could not be validated by the locked Milestone 1 service");
                    })
                    .body(LegacyEmployee.class);
            if (employee == null) {
                throw new ExternalServiceException("Employee reference could not be validated by the locked Milestone 1 service");
            }
            return employee;
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Milestone 1 employee service is unavailable through the API Gateway");
        }
    }

    public List<LegacyCertificate> getCertificates() {
        try {
            List<LegacyCertificate> result = restClient.get().uri(baseUrl + "/certificate")
                    .header("X-User-Role", "ADMIN")
                    .header("X-User-Id", "system-m3")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<LegacyCertificate>>() {});
            return result == null ? List.of() : result;
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Milestone 1 certification endpoint is unavailable through the API Gateway");
        }
    }
}
