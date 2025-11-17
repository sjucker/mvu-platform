package ch.mvurdorf.platform.konzerte;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/secured/konzert")
public class KonzertEndpoint {

    private final KonzerteService konzerteService;

    @GetMapping
    public ResponseEntity<List<KonzertDto>> getFutureKonzerte() {
        log.info("GET /api/secured/konzert");
        return ResponseEntity.ok(konzerteService.getFutureKonzerte());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KonzertDto> findById(@PathVariable Long id) {
        log.info("GET /api/secured/konzert/{}", id);
        return ResponseEntity.of(konzerteService.findById(id));
    }

}
