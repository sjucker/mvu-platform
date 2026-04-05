package ch.mvurdorf.platform.noten;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotenPdfUploadDialogTest {

    @Test
    void parsePages() {
        assertThat(NotenPdfUploadDialog.parsePages("null")).satisfies(pages -> assertThat(pages.isValid()).isFalse());
        assertThat(NotenPdfUploadDialog.parsePages(null)).satisfies(pages -> assertThat(pages.isValid()).isFalse());
        assertThat(NotenPdfUploadDialog.parsePages("")).satisfies(pages -> assertThat(pages.isValid()).isFalse());
        assertThat(NotenPdfUploadDialog.parsePages("-")).satisfies(pages -> assertThat(pages.isValid()).isFalse());
        assertThat(NotenPdfUploadDialog.parsePages("1-2-3")).satisfies(pages -> assertThat(pages.isValid()).isFalse());

        assertThat(NotenPdfUploadDialog.parsePages("1-2")).satisfies(pages -> {
            assertThat(pages.isValid()).isTrue();
            assertThat(pages.from()).isEqualTo(1);
            assertThat(pages.to()).isEqualTo(2);
        });

        assertThat(NotenPdfUploadDialog.parsePages("2")).satisfies(pages -> {
            assertThat(pages.isValid()).isTrue();
            assertThat(pages.from()).isEqualTo(2);
            assertThat(pages.to()).isEqualTo(2);
        });
    }
}
