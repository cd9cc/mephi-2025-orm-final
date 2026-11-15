package org.mephi.learning.api;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpUrlValidator implements ConstraintValidator<HttpUrl, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        try {
            URI uri = new URI(value).parseServerAuthority();
            String scheme = uri.getScheme();
            int port = uri.getPort();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && uri.getHost() != null
                    && uri.getUserInfo() == null
                    && (port == -1 || (port >= 1 && port <= 65535));
        } catch (URISyntaxException exception) {
            return false;
        }
    }
}
