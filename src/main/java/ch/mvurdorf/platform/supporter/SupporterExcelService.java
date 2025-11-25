package ch.mvurdorf.platform.supporter;

import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class SupporterExcelService {

    private final SupporterService supporterService;

    public InputStream export() throws IOException {
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Supporters");
            var header = sheet.createRow(0);

            header.createCell(0).setCellValue("Typ");
            header.createCell(1).setCellValue("Anrede");
            header.createCell(2).setCellValue("Vorname");
            header.createCell(3).setCellValue("Nachname");
            header.createCell(4).setCellValue("Strasse");
            header.createCell(5).setCellValue("PLZ/Ort");
            header.createCell(6).setCellValue("Land");
            header.createCell(7).setCellValue("Email");
            header.createCell(8).setCellValue("Kommunikation per Post");
            header.createCell(9).setCellValue("kommunikation per Email");

            int rowIndex = 1;
            for (var dto : supporterService.fetchAll()) {
                var row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(dto.type().getDescription());
                row.createCell(1).setCellValue(dto.anrede());
                row.createCell(2).setCellValue(dto.vorname());
                row.createCell(3).setCellValue(dto.nachname());
                row.createCell(4).setCellValue(dto.getStrasseWithNr());
                row.createCell(5).setCellValue(dto.getPlzOrt());
                row.createCell(6).setCellValue(dto.countryCode());
                row.createCell(7).setCellValue(dto.email());
                row.createCell(8).setCellValue(dto.kommunikationPost() ? "x" : "");
                row.createCell(9).setCellValue(dto.kommunikationEmail() ? "x" : "");
            }
            IntStream.range(0, 9).forEach(sheet::autoSizeColumn);

            var stream = new ByteArrayOutputStream();
            workbook.write(stream);
            return new ByteArrayInputStream(stream.toByteArray());
        }
    }

}
