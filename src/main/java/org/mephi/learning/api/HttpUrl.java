package org.mephi.learning.api;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = HttpUrlValidator.class)
@Target({
    ElementType.FIELD,
    ElementType.PARAMETER,
    ElementType.RECORD_COMPONENT,
    ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpUrl {
    String message() default "must be an absolute HTTP or HTTPS URL with a valid host";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
