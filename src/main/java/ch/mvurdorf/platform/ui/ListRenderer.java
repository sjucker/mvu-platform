package ch.mvurdorf.platform.ui;

import com.vaadin.flow.data.renderer.BasicRenderer;
import com.vaadin.flow.function.ValueProvider;

import java.util.List;

public class ListRenderer<T> extends BasicRenderer<T, List<String>> {

    public ListRenderer(ValueProvider<T, List<String>> valueProvider) {
        super(valueProvider);
    }

    @Override
    protected String getFormattedValue(List<String> values) {
        return String.join(", ", values);
    }

}
