package ch.mvurdorf.platform.ui;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class StyleUtility {

    @NoArgsConstructor(access = PRIVATE)
    public static final class IconStyle {
        public static final String CLICKABLE = "clickable";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static final class Cursor {
        public static final String AUTO = "auto";
        public static final String POINTER = "pointer";
    }
}
