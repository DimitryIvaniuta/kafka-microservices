package com.github.dimitryivaniuta.gateway.producer.api;

import com.github.dimitryivaniuta.gateway.producer.api.dto.LeadCreateRequest;
import com.github.dimitryivaniuta.gateway.producer.service.LeadPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Minimal REST façade for publishing Real Estate lead events to Kafka.
 *
 * Contract:
 *  - POST /api/leads
 *    Headers (optional): X-Tenant-Id
 *    Body: LeadCreateRequest
 *    Response: 201 Created, body = eventId (UUID as String), Location points to the event URI
 *
 * Notes:
 *  - The actual event publishing is done by LeadPublisher (transactional Kafka producer).
 *  - We do not expose read endpoints here (producer-only service).
 */
@RestController
@RequestMapping(path = "/api/leads", produces = "application/json")
@RequiredArgsConstructor
@Validated
public class LeadController {

    private final LeadPublisher publisher;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<String> createLead(
            @Valid @RequestBody LeadCreateRequest request,
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId
    ) {
        UUID eventId = publisher.publishCreate(request, tenantId);

        // Build a canonical Location header for observability/tools (no retrieval endpoint needed to be useful)
        URI location = URI.create("/api/leads/events/" + eventId);

        return ResponseEntity
                .created(location) // 201
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(eventId.toString());
    }
}
