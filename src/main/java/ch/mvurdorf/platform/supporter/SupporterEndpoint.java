package ch.mvurdorf.platform.supporter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CONFLICT;

@Slf4j
@RestController
@RequestMapping(value = "/api/supporter")
public class SupporterEndpoint {

    private final SupporterService supporterService;

    public SupporterEndpoint(SupporterService supporterService) {
        this.supporterService = supporterService;
    }

    @PostMapping
    public ResponseEntity<Long> createSupporter(@RequestBody SupporterDto supporter) {
        log.info("POST /api/supporter {}", supporter);
        if (supporterService.exists(supporter.email())) {
            return ResponseEntity.status(CONFLICT).build();
        }
        var externalId = supporterService.create(supporter);
        return ResponseEntity.ok(externalId);
    }

    @GetMapping(value = "/{externalId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrBill(@PathVariable Long externalId) {
        return ResponseEntity.of(supporterService.qrBill(externalId));
    }

    @GetMapping
    public ResponseEntity<String> ping() {
        log.info("GET /api/supporter");
        return ResponseEntity.ok("pong");
    }
}
