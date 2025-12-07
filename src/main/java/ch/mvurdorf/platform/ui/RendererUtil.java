package ch.mvurdorf.platform.ui;

import ch.mvurdorf.platform.ui.StyleUtility.IconStyle;
import ch.mvurdorf.platform.utils.BigDecimalUtil;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.theme.lumo.LumoUtility.IconSize;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.vaadin.flow.component.html.AnchorTarget.BLANK;
import static com.vaadin.flow.component.icon.VaadinIcon.CHECK;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class RendererUtil {
    private RendererUtil() {
    }

    public static <T> LocalDateRenderer<T> dateRenderer(Function<T, LocalDate> getter) {
        return new LocalDateRenderer<>(getter::apply, "dd.MM.yyyy");
    }

    public static <T> LocalDateTimeRenderer<T> dateTimeRenderer(Function<T, LocalDateTime> getter) {
        return new LocalDateTimeRenderer<>(getter::apply, "dd.MM.yyyy HH:mm");
    }

    public static <T> LocalTimeRenderer<T> timeRenderer(Function<T, LocalTime> getter) {
        return new LocalTimeRenderer<>(getter::apply);
    }

    public static <T> ComponentRenderer<Component, T> checkboxRenderer(Predicate<T> getter) {
        return checkboxRenderer(getter, CHECK);
    }

    public static <T> ComponentRenderer<Component, T> checkboxRenderer(Predicate<T> getter, VaadinIcon icon) {
        return new ComponentRenderer<>(dto -> getter.test(dto) ? icon.create() : new Div());
    }

    public static <T> ComponentRenderer<Component, T> clickableIcon(VaadinIcon vaadinIcon, Consumer<T> clickHandler, String tooltipText) {
        return clickableIcon(vaadinIcon, clickHandler, _ -> true, tooltipText);
    }

    public static <T> ComponentRenderer<Component, T> clickableIcon(VaadinIcon vaadinIcon, Consumer<T> clickHandler) {
        return clickableIcon(vaadinIcon, clickHandler, _ -> true, "");
    }

    public static <T> ComponentRenderer<Component, T> clickableIcon(VaadinIcon vaadinIcon, Consumer<T> clickHandler, Predicate<T> display, String tooltipText) {
        return new ComponentRenderer<>(dto -> {
            if (display.test(dto)) {

                var icon = new Icon(vaadinIcon);
                icon.setTooltipText(tooltipText);
                icon.addClassNames(IconSize.SMALL, IconStyle.CLICKABLE);
                icon.addClickListener(event -> {
                    if (event.isFromClient()) {
                        clickHandler.accept(dto);
                    }
                });
                return icon;
            } else {
                return new Div();
            }
        });
    }

    public static <T> ComponentRenderer<HtmlContainer, T> iconDownloadLink(VaadinIcon vaadinIcon,
                                                                           Function<T, byte[]> byteGetter,
                                                                           Function<T, String> nameGetter) {
        return new ComponentRenderer<>(dto -> {
            var icon = vaadinIcon.create();
            var anchor = new Anchor("", icon);
            anchor.setHref(DownloadHandler.fromInputStream(_ -> new DownloadResponse(new ByteArrayInputStream(byteGetter.apply(dto)),
                                                                                     nameGetter.apply(dto), null, -1)));
            anchor.setTarget(BLANK);
            return anchor;
        });
    }

    public static <T> ComponentRenderer<HtmlContainer, T> externalLink(VaadinIcon vaadinIcon,
                                                                       Function<T, String> urlGetter,
                                                                       String tooltipText) {
        return new ComponentRenderer<>(dto -> {
            if (isNotBlank(urlGetter.apply(dto))) {
                var icon = vaadinIcon.create();
                icon.addClassNames(IconSize.SMALL, IconStyle.CLICKABLE);
                icon.setTooltipText(tooltipText);
                var anchor = new Anchor(urlGetter.apply(dto), icon);
                anchor.setTarget(BLANK);
                return anchor;
            } else {
                return new Div();
            }
        });
    }

    public static <T> TextRenderer<T> repertoireNumber(Function<T, BigDecimal> getter) {
        return new TextRenderer<>(dto -> BigDecimalUtil.formatBigDecimal(getter.apply(dto)));
    }

}
