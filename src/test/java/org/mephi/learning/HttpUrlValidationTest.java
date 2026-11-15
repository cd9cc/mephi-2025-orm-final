package org.mephi.learning;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mephi.learning.api.Requests.*;

class HttpUrlValidationTest {
    @ParameterizedTest
    @ValueSource(
            strings = {
                "",
                " ",
                "not-a-url",
                "/relative",
                "//example.test/path",
                "ftp://example.test/file",
                "javascript:alert(1)",
                "https://",
                "https://bad host/a",
                "https://example.test/%zz",
                "https://example.test:65536/a",
                "https://user:pass@example.test"
            })
    void rejectsMalformedUrlsInAllInputs(String url) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertThat(validator.validate(new ProfileInput(null, url))).isNotEmpty();
            assertThat(validator.validate(new LessonInput("Lesson", null, url, 0))).isNotEmpty();
            assertThat(validator.validate(new MaterialInput("Guide", url))).isNotEmpty();
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(
            strings = {
                "https://example.test/guide?q=java#intro",
                "http://localhost:8080/video",
                "https://127.0.0.1/image.png",
                "http://[::1]:8080/video"
            })
    void acceptsWebUrlsAndOnlyAllowsNullForOptionalFields(String url) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertThat(validator.validate(new ProfileInput(null, url))).isEmpty();
            assertThat(validator.validate(new LessonInput("Lesson", null, url, 0))).isEmpty();
            assertThat(validator.validate(new MaterialInput("Guide", url)).isEmpty())
                    .isEqualTo(url != null);
        }
    }
}
