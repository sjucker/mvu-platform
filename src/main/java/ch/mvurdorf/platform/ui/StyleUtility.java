package ch.mvurdorf.platform.ui;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * CSS class names defined in {@code META-INF/resources/styles.css}.
 * The Aura theme has no utility classes (unlike LumoUtility), so the few the app needs live here.
 */
@NoArgsConstructor(access = PRIVATE)
public final class StyleUtility {

    @NoArgsConstructor(access = PRIVATE)
    public static final class IconStyle {
        public static final String CLICKABLE = "clickable";
        public static final String SMALL = "icon-small";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static final class Cursor {
        public static final String AUTO = "auto";
        public static final String POINTER = "pointer";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static final class TextStyle {
        public static final String XSMALL = "text-xsmall";
        public static final String SMALL = "text-small";
        public static final String LARGE = "text-large";
        public static final String SEMIBOLD = "text-semibold";
        public static final String BOLD = "text-bold";
        public static final String SECONDARY = "text-secondary";
        public static final String ACCENT = "text-accent";
        public static final String ERROR = "text-error";
        public static final String WARNING = "text-warning";
        public static final String PRE_WRAP = "text-pre-wrap";
        public static final String PRE_LINE = "text-pre-line";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static final class LayoutStyle {
        public static final String GAP_SMALL = "gap-small";
        public static final String GAP_MEDIUM = "gap-medium";
        public static final String GAP_LARGE = "gap-large";
        /** row gap xsmall, column gap medium – for wrapping horizontal layouts */
        public static final String GAP_WRAP = "gap-wrap";
        /** inline flex row with vertically centered children */
        public static final String INLINE_CENTER = "inline-center";
        /** column on narrow screens, row from 768px upwards */
        public static final String RESPONSIVE_ROW = "responsive-row";
        public static final String NO_MARGIN = "no-margin";
        public static final String PADDING_SMALL = "padding-small";
        public static final String MAX_WIDTH_FULL = "max-width-full";
    }
}
