package ch.mvurdorf.platform.passivmitglied;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CONFLICT;

@RestController
@RequestMapping(value = "/api/passivmitglied")
public class PassivmitgliedEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(PassivmitgliedEndpoint.class);

    private final PassivmitgliedService passivmitgliedService;

    public PassivmitgliedEndpoint(PassivmitgliedService passivmitgliedService) {
        this.passivmitgliedService = passivmitgliedService;
    }

    @PostMapping()
    public ResponseEntity<Void> createPassivmitglied(@RequestBody PassivmitgliedDto passivmitglied) {
        logger.info("POST /api/passivmitglied {}", passivmitglied);
        if (passivmitgliedService.exists(passivmitglied.email())) {
            return ResponseEntity.status(CONFLICT).build();
        }
        passivmitgliedService.create(passivmitglied);
        return ResponseEntity.ok().build();
    }
}