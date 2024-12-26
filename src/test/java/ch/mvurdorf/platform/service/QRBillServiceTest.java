package ch.mvurdorf.platform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.fail;

class QRBillServiceTest {

    private QRBillService qrBillService;

    @BeforeEach
    void setUp() {
        qrBillService = new QRBillService();
    }

    @Test
    void passivmitglied() {
        try (var out = new FileOutputStream("target/test.png")) {
            qrBillService.passivmitglied(20.0, 100001L, out);
        } catch (IOException e) {
            fail(e);
        }
    }

}
