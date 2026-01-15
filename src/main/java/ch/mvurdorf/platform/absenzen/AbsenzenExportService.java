package ch.mvurdorf.platform.absenzen;

import ch.mvurdorf.platform.common.AbsenzState;
import ch.mvurdorf.platform.jooq.tables.records.AbsenzStatusRecord;
import ch.mvurdorf.platform.jooq.tables.records.EventRecord;
import ch.mvurdorf.platform.jooq.tables.records.LoginRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static ch.mvurdorf.platform.common.AbsenzState.POSITIVE;
import static ch.mvurdorf.platform.common.AbsenzState.UNKNOWN;
import static ch.mvurdorf.platform.jooq.Tables.ABSENZ_STATUS;
import static ch.mvurdorf.platform.jooq.Tables.EVENT;
import static ch.mvurdorf.platform.jooq.Tables.LOGIN;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND;
import static org.apache.poi.ss.usermodel.IndexedColors.LIGHT_GREEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbsenzenExportService {
    private final DSLContext jooqDsl;

    public InputStream export(LocalDate fromDate, LocalDate toDate) throws IOException {
        log.info("exporting absenzen from {} to {}", fromDate, toDate);

        var users = findActiveUsers();
        var events = findEvents(fromDate, toDate);
        var absenzen = findAbsenzenPerEventThenPerUser(users, events);

        try (var workbook = new XSSFWorkbook()) {
            var activeCellStyle = workbook.createCellStyle();
            activeCellStyle.setFillForegroundColor(LIGHT_GREEN.getIndex());
            activeCellStyle.setFillPattern(SOLID_FOREGROUND);

            var sheet = workbook.createSheet("Absenzen");
            createHeader(sheet, events);
            createRows(sheet, users, events, absenzen, activeCellStyle);

            var stream = new ByteArrayOutputStream();
            workbook.write(stream);
            return new ByteArrayInputStream(stream.toByteArray());
        }
    }

    private void createRows(XSSFSheet sheet, List<LoginRecord> users, List<EventRecord> events, Map<Long, Map<Long, AbsenzState>> absenzen, CellStyle lightGreenStyle) {
        var rowNum = 1;
        for (var user : users) {
            var row = sheet.createRow(rowNum++);
            var colNum = 0;
            var cell = row.createCell(colNum++);
            cell.setCellValue(user.getName());
            var totalCell = row.createCell(colNum++);
            var total = 0;
            for (var event : events) {
                var eventAbsenzen = absenzen.getOrDefault(event.getId(), new HashedMap<>());
                cell = row.createCell(colNum++);
                var absenzStatus = eventAbsenzen.getOrDefault(user.getId(), UNKNOWN);
                cell.setCellValue(absenzStatus.getDescription());
                if (absenzStatus == POSITIVE) {
                    cell.setCellStyle(lightGreenStyle);
                    total++;
                }
            }
            totalCell.setCellValue(total);
        }
    }

    private void createHeader(XSSFSheet sheet, List<EventRecord> events) {
        var headerRow = sheet.createRow(0);
        var colNum = 0;
        var cell = headerRow.createCell(colNum++);
        cell.setCellValue("Name");
        cell = headerRow.createCell(colNum++);
        cell.setCellValue("Total");
        for (var event : events) {
            cell = headerRow.createCell(colNum++);
            cell.setCellValue(event.getTitle());
        }
    }

    private Map<Long, Map<Long, AbsenzState>> findAbsenzenPerEventThenPerUser(List<LoginRecord> users, List<EventRecord> events) {
        var userIds = users.stream().map(LoginRecord::getId).toList();
        var eventIds = events.stream().map(EventRecord::getId).toList();

        return jooqDsl.selectFrom(ABSENZ_STATUS)
                      .where(ABSENZ_STATUS.FK_EVENT.in(eventIds))
                      .and(ABSENZ_STATUS.FK_LOGIN.in(userIds))
                      .fetch()
                      .stream()
                      .collect(groupingBy(AbsenzStatusRecord::getFkEvent,
                                          toMap(AbsenzStatusRecord::getFkLogin, it -> AbsenzState.of(it.getStatus()))));
    }

    private record Foo(Long loginId, Long eventId, String remark, String status) {}

    private List<EventRecord> findEvents(LocalDate fromDate, LocalDate toDate) {
        return jooqDsl.selectFrom(EVENT)
                      .where(EVENT.NEXT_VERSION.isNull())
                      .and(EVENT.DELETED_AT.isNull())
                      .and(EVENT.FROM_DATE.between(fromDate, toDate))
                      .and(EVENT.RELEVANT_FOR_ABSENZ.isTrue())
                      .and(EVENT.INFO_ONLY.isFalse())
                      .orderBy(EVENT.FROM_DATE.asc(), EVENT.FROM_TIME.asc(), EVENT.TO_DATE.asc(), EVENT.TO_TIME.asc())
                      .fetch();
    }

    private List<LoginRecord> findActiveUsers() {
        return jooqDsl.selectFrom(LOGIN)
                      .where(LOGIN.ACTIVE.isTrue())
                      .orderBy(LOGIN.NAME.asc())
                      .fetch();
    }
}
