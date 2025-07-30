package ch.mvurdorf.platform.ui;

import ch.mvurdorf.platform.utils.FormatUtil;
import com.vaadin.flow.data.renderer.BasicRenderer;
import com.vaadin.flow.function.ValueProvider;

import java.time.LocalTime;

public class LocalTimeRenderer<T> extends BasicRenderer<T, LocalTime> {

    public LocalTimeRenderer(ValueProvider<T, LocalTime> valueProvider) {
        super(valueProvider);
    }

    @Override
    protected String getFormattedValue(LocalTime object) {
        if (object == null) {
            return "";
        }
        return FormatUtil.formatTime(object);
    }
}
