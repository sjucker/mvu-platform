package ch.mvurdorf.platform.service;

import ch.mvurdorf.platform.passivmitglied.PassivmitgliedDto;
import net.codecrete.qrbill.generator.Address;
import net.codecrete.qrbill.generator.Bill;
import net.codecrete.qrbill.generator.BillFormat;
import net.codecrete.qrbill.generator.GraphicsFormat;
import net.codecrete.qrbill.generator.Language;
import net.codecrete.qrbill.generator.OutputSize;
import net.codecrete.qrbill.generator.QRBill;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;

@Service
public class QRBillService {

    public void passivmitglied(double amount, PassivmitgliedDto passivmitglied, OutputStream out) {
        var bill = new Bill();
        bill.setAccount("CH03 0070 0110 9014 7015 4");
        bill.setAmountFromDouble(amount);
        bill.setCurrency("CHF");
        bill.createAndSetCreditorReference(passivmitglied.externalId().toString());
        bill.setUnstructuredMessage("Passivmitglied-Beitrag " + passivmitglied.fullName());

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
        billFormat.setOutputSize(OutputSize.PAYMENT_PART_ONLY);
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
