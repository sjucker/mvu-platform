package ch.mvurdorf.platform.ui;

import ch.mvurdorf.platform.ui.StyleUtility.IconStyle;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility.IconSize;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.vaadin.flow.component.html.AnchorTarget.BLANK;

public final class RendererUtil {
    private RendererUtil() {
    }

    public static <T> LocalDateRenderer<T> dateRenderer(Function<T, LocalDate> getter) {
        return new LocalDateRenderer<>(getter::apply, "dd.MM.yyyy");
    }

    public static <T> LocalDateTimeRenderer<T> dateTimeRenderer(Function<T, LocalDateTime> getter) {
        return new LocalDateTimeRenderer<>(getter::apply, "dd.MM.yyyy hh:mm");
    }

    public static <T> ComponentRenderer<Icon, T> clickableIcon(VaadinIcon vaadinIcon, Consumer<T> clickHandler) {
        return new ComponentRenderer<>(dto -> {
            var icon = new Icon(vaadinIcon);
            icon.addClassNames(IconSize.SMALL, IconStyle.CLICKABLE);
            icon.addClickListener(event -> {
                if (event.isFromClient()) {
                    clickHandler.accept(dto);
                }
            });
            return icon;
        });
    }

    public static <T> ComponentRenderer<HtmlContainer, T> iconDownloadLink(VaadinIcon vaadinIcon,
                                                                           Function<T, byte[]> byteGetter,
                                                                           Function<T, String> nameGetter) {
        return new ComponentRenderer<>(dto -> {
            var icon = vaadinIcon.create();
            var anchor = new Anchor("", icon);
            anchor.setHref(new StreamResource(nameGetter.apply(dto), () -> new ByteArrayInputStream(byteGetter.apply(dto))));
            anchor.setTarget(BLANK);
            return anchor;
        });
    }

}
