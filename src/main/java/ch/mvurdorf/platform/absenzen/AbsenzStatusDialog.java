package ch.mvurdorf.platform.absenzen;

import ch.mvurdorf.platform.common.AbsenzState;
import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.ui.ComponentUtil;
import ch.mvurdorf.platform.ui.LocalizedEnumRenderer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.function.ValueProvider;

import java.util.List;

import static ch.mvurdorf.platform.common.AbsenzState.NEGATIVE;
import static ch.mvurdorf.platform.common.AbsenzState.POSITIVE;
import static ch.mvurdorf.platform.security.LoginService.ABSENZEN_GROUP;
import static com.vaadin.flow.component.Unit.PERCENTAGE;
import static com.vaadin.flow.component.Unit.PIXELS;
import static com.vaadin.flow.component.grid.ColumnTextAlign.CENTER;
import static com.vaadin.flow.component.grid.Grid.SelectionMode.NONE;
import static com.vaadin.flow.component.icon.VaadinIcon.THUMBS_DOWN;
import static com.vaadin.flow.component.icon.VaadinIcon.THUMBS_UP;

public class AbsenzStatusDialog extends Dialog {

    private final AbsenzenService absenzenService;
    private final AuthenticatedUser authenticatedUser;

    private Grid<AbsenzStatusDto> grid;

    private AbsenzStatusDialog(AbsenzenService absenzenService, AuthenticatedUser authenticatedUser) {
        this.absenzenService = absenzenService;
        this.authenticatedUser = authenticatedUser;
    }

    public static void show(AbsenzenService absenzenService, AuthenticatedUser authenticatedUser, EventAbsenzSummaryDto event, Runnable callback) {
        var dialog = new AbsenzStatusDialog(absenzenService, authenticatedUser);
        dialog.init(event.id(), callback);
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setHeaderTitle(event.getTitleWithDate());
        dialog.setMaxWidth(100, PERCENTAGE);
        dialog.setWidth(1000, PIXELS);
        dialog.open();
    }

    private void init(Long eventId, Runnable callback) {
        grid = new Grid<>();
        grid.setSelectionMode(NONE);
        grid.addColumn(dto -> dto.isRegisterPlaceholder() ? dto.register().getDescription() : "").setWidth("120px").setFlexGrow(0).setHeader("Register");
        grid.addColumn(AbsenzStatusDto::name).setWidth("180px").setFlexGrow(0).setHeader("Name");
        grid.addColumn(AbsenzStatusDto::remark).setWidth("150px").setResizable(true).setHeader("Bemerkung");
        grid.addColumn(new LocalizedEnumRenderer<>(AbsenzStatusDto::status)).setWidth("120px").setFlexGrow(0).setHeader("Status");

        if (authenticatedUser.hasWritePermission(ABSENZEN_GROUP)) {
            grid.addComponentColumn(absenzStatusUpdater(NEGATIVE, THUMBS_DOWN, eventId)).setWidth("70px").setFlexGrow(0).setTextAlign(CENTER);
            grid.addComponentColumn(absenzStatusUpdater(POSITIVE, THUMBS_UP, eventId)).setWidth("70px").setFlexGrow(0).setTextAlign(CENTER);
        }
        grid.setPartNameGenerator(dto -> "absenz-status-" + dto.status());
        grid.setItems(getItems(eventId));

        add(grid);

        getFooter().add(ComponentUtil.secondaryButton("Schliessen", () -> {
            close();
            callback.run();
        }));
    }

    private ValueProvider<AbsenzStatusDto, ? extends Component> absenzStatusUpdater(AbsenzState state, VaadinIcon icon, Long eventId) {
        return dto -> {
            if (dto.status() != state) {
                var result = ComponentUtil.clickableIcon(icon, () -> {
                    absenzenService.updateStatus(eventId, dto.loginId(), state);
                    grid.setItems(getItems(eventId));
                });
                result.setColor("grey");
                return result;
            }
            return new Div();
        };
    }

    private List<AbsenzStatusDto> getItems(Long eventId) {
        var absenzenPerRegister = absenzenService.findAbsenzenStatusByEvent(eventId);
        return absenzenPerRegister.keySet().stream()
                                  .sorted()
                                  .<AbsenzStatusDto>mapMulti((register, consumer) -> {
                                      consumer.accept(AbsenzStatusDto.of(register));
                                      absenzenPerRegister.get(register).forEach(consumer);
                                  })
                                  .toList();
    }
}
