package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class NotenShareEndpoint {

    private final NotenShareService shareService;

    // Secured creation endpoint
    @PostMapping("/api/secured/noten-share")
    public ResponseEntity<Map<String, Object>> create(@RequestParam("konzertId") Long konzertId,
                                                      @RequestParam("instrument") Instrument instrument,
                                                      @RequestParam(value = "expiresAt", required = false)
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiresAt) {
        log.info("POST /api/secured/noten-share konzertId={} instrument={} expiresAt={}", konzertId, instrument, expiresAt);
        var token = shareService.createLink(konzertId, instrument, expiresAt);
        return ResponseEntity.ok(Map.of("token", token.toString()));
    }

    // Public list endpoint
    @GetMapping("/api/noten-share/{token}")
    public ResponseEntity<List<NotenShareService.SharedPdf>> list(@PathVariable("token") String token) {
        log.info("GET /api/noten-share/{}", token);
        try {
            var list = shareService.listPdfsForToken(UUID.fromString(token));
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Public merged PDF endpoint
    @GetMapping(value = "/api/noten-share/{token}/all.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> merged(@PathVariable("token") String token) {
        log.info("GET /api/noten-share/{}/all.pdf", token);
        try {
            var pdf = shareService.mergedPdfForToken(UUID.fromString(token));
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDispositionInline("noten-alle.pdf"));
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Public single PDF endpoint
    @GetMapping(value = "/api/noten-share/{token}/{notenId}.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> single(@PathVariable("token") String token,
                                         @PathVariable("notenId") long notenId) {
        log.info("GET /api/noten-share/{}/{}.pdf", token, notenId);
        try {
            var bytes = shareService.singlePdfForToken(UUID.fromString(token), notenId);
            if (bytes.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDispositionInline("noten-" + notenId + ".pdf"));
            return new ResponseEntity<>(bytes.get(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private String contentDispositionInline(String filename) {
        var encoded = java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8);
        return "inline; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded;
    }
}
