package ch.mvurdorf.platform.ui;

import ch.mvurdorf.platform.common.LocalizedEnum;
import com.vaadin.flow.data.renderer.BasicRenderer;
import com.vaadin.flow.function.ValueProvider;

import java.util.Set;

import static java.util.stream.Collectors.joining;

public class LocalizedEnumsRenderer<T, S extends LocalizedEnum> extends BasicRenderer<T, Set<S>> {

    public LocalizedEnumsRenderer(ValueProvider<T, Set<S>> valueProvider) {
        super(valueProvider);
    }

    @Override
    protected String getFormattedValue(Set<S> values) {
        return values.stream().map(LocalizedEnum::getDescription).collect(joining(", "));
    }

}
