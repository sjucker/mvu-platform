package ch.mvurdorf.platform.ui;

import com.vaadin.flow.data.renderer.BasicRenderer;
import com.vaadin.flow.function.ValueProvider;

import static java.lang.Boolean.TRUE;

public class CheckboxRenderer<T> extends BasicRenderer<T, Boolean> {
    public CheckboxRenderer(ValueProvider<T, Boolean> valueProvider) {
        super(valueProvider);
    }

    @Override
    protected String getFormattedValue(Boolean object) {
        if (TRUE.equals(object)) {
            return "x";
        }
        return "";
    }
}
