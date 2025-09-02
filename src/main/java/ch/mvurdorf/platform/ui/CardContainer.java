package ch.mvurdorf.platform.ui;

import com.vaadin.flow.component.html.Div;

public class CardContainer extends Div {
    public CardContainer() {
        setWidthFull();
        getStyle().set("display", "grid")
                  .set("grid-template-columns", "repeat(auto-fill, minmax(350px, 1fr))")
                  .set("gap", "1em");
    }
}
