package ch.mvurdorf.platform.service;

import ch.mvurdorf.platform.passivmitglied.PassivmitgliedDto;
import lombok.extern.slf4j.Slf4j;
import net.codecrete.qrbill.generator.Address;
import net.codecrete.qrbill.generator.Bill;
import net.codecrete.qrbill.generator.BillFormat;
import net.codecrete.qrbill.generator.GraphicsFormat;
import net.codecrete.qrbill.generator.Language;
import net.codecrete.qrbill.generator.OutputSize;
import net.codecrete.qrbill.generator.QRBill;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;

@Slf4j
@Service
public class QRBillService {

    public Optional<byte[]> passivmitgliedPdf(PassivmitgliedDto passivmitglied, double amount) {
        var out = new ByteArrayOutputStream();
        passivmitglied(passivmitglied, amount, OutputSize.A4_PORTRAIT_SHEET, out);

        try (var document = new PDDocument();
             var pdfOut = new ByteArrayOutputStream()) {

            var page = new PDPage(A4);
            document.addPage(page);

            var image = PDImageXObject.createFromByteArray(document, out.toByteArray(), "bill");
            image.getWidth();
            try (var contentStream = new PDPageContentStream(document, page)) {
                var maxWidth = A4.getWidth();
                var scale = maxWidth / image.getWidth();
                contentStream.drawImage(image, 0, 0, maxWidth, image.getHeight() * scale);
            }
            document.save(pdfOut);
            return Optional.of(pdfOut.toByteArray());
        } catch (IOException e) {
            log.error("could not generate pdf", e);
            return Optional.empty();
        }
    }

    public void passivmitglied(PassivmitgliedDto passivmitglied, double amount, OutputStream out) {
        passivmitglied(passivmitglied, amount, OutputSize.PAYMENT_PART_ONLY, out);
    }

    public void passivmitglied(PassivmitgliedDto passivmitglied, double amount, OutputSize outputSize, OutputStream out) {
        var bill = new Bill();
        bill.setAccount("CH03 0070 0110 9014 7015 4");
        bill.setAmountFromDouble(amount);
        bill.setCurrency("CHF");
        bill.createAndSetCreditorReference(passivmitglied.externalId().toString());
        bill.setUnstructuredMessage("Referenz-Nr. " + passivmitglied.externalId());

        var creditor = new Address();
        creditor.setName("Musikverein Harmonie Urdorf");
        creditor.setStreet("Im Grüt");
        creditor.setHouseNo("52");
        creditor.setPostalCode("8902");
        creditor.setTown("Urdorf");
        creditor.setCountryCode("CH");
        bill.setCreditor(creditor);

        var debtor = new Address();
        debtor.setName(passivmitglied.fullName());
        debtor.setStreet(passivmitglied.strasse());
        debtor.setHouseNo(passivmitglied.strasseNr());
        debtor.setPostalCode(passivmitglied.plz());
        debtor.setTown(passivmitglied.ort());
        debtor.setCountryCode(passivmitglied.countryCode());
        bill.setDebtor(debtor);

        var billFormat = new BillFormat();
        billFormat.setGraphicsFormat(GraphicsFormat.PNG);
        billFormat.setOutputSize(outputSize);
        billFormat.setLanguage(Language.DE);
        bill.setFormat(billFormat);

        var qr = QRBill.generate(bill);
        try {
            out.write(qr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
