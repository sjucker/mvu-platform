package ch.mvurdorf.platform.repertoire;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/secured/repertoire")
public class RepertoireEndpoint {

    private final RepertoireService repertoireService;

    @GetMapping("/{type}")
    public ResponseEntity<RepertoireDto> ping(@PathVariable RepertoireType type,
                                              @AuthenticationPrincipal Jwt jwt) {
        log.info("GET /api/secured/repertoire/{}", type);
        return ResponseEntity.of(repertoireService.findRepertoireByType(type));
    }
}
