package ch.mvurdorf.platform.service;

import ch.mvurdorf.platform.supporter.SupporterDto;
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

    public Optional<byte[]> supporterPdf(SupporterDto supporter, double amount) {
        var out = new ByteArrayOutputStream();
        supporter(supporter, amount, OutputSize.A4_PORTRAIT_SHEET, out);

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

    public void supporter(SupporterDto supporter, double amount, OutputStream out) {
        supporter(supporter, amount, OutputSize.PAYMENT_PART_ONLY, out);
    }

    public void supporter(SupporterDto supporter, double amount, OutputSize outputSize, OutputStream out) {
        var bill = new Bill();
        bill.setAccount("CH03 0070 0110 9014 7015 4");
        bill.setAmountFromDouble(amount);
        bill.setCurrency("CHF");
        bill.createAndSetCreditorReference(supporter.externalId().toString());
        bill.setUnstructuredMessage("Referenz-Nr. " + supporter.externalId());

        var creditor = new Address();
        creditor.setName("Musikverein Harmonie Urdorf");
        creditor.setStreet("Im Grüt");
        creditor.setHouseNo("52");
        creditor.setPostalCode("8902");
        creditor.setTown("Urdorf");
        creditor.setCountryCode("CH");
        bill.setCreditor(creditor);

        var debtor = new Address();
        debtor.setName(supporter.fullName());
        debtor.setStreet(supporter.strasse());
        debtor.setHouseNo(supporter.strasseNr());
        debtor.setPostalCode(supporter.plz());
        debtor.setTown(supporter.ort());
        debtor.setCountryCode(supporter.countryCode());
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
