package ch.mvurdorf.platform.ui;

import ch.mvurdorf.platform.common.LocalizedEnum;
import com.vaadin.flow.data.renderer.BasicRenderer;
import com.vaadin.flow.function.ValueProvider;

import static java.util.Optional.ofNullable;

public class LocalizedEnumRenderer<T> extends BasicRenderer<T, LocalizedEnum> {

    public LocalizedEnumRenderer(ValueProvider<T, LocalizedEnum> valueProvider) {
        super(valueProvider);
    }

    @Override
    protected String getFormattedValue(LocalizedEnum object) {
        return ofNullable(object).map(LocalizedEnum::getDescription).orElse("");
    }
}
