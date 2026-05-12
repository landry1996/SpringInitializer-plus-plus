package com.springforge.security;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class InputSanitizer {

    private static final Pattern SAFE_PROJECT_NAME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]{0,63}$");
    private static final Pattern SAFE_PACKAGE_NAME = Pattern.compile("^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*$");
    private static final Pattern SAFE_VERSION = Pattern.compile("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.]+)?$");
    private static final Pattern PATH_TRAVERSAL = Pattern.compile("\\.\\.|[/\\\\]");

    public boolean isValidProjectName(String name) {
        return name != null && SAFE_PROJECT_NAME.matcher(name).matches();
    }

    public boolean isValidPackageName(String packageName) {
        return packageName != null && packageName.length() <= 128 && SAFE_PACKAGE_NAME.matcher(packageName).matches();
    }

    public boolean isValidVersion(String version) {
        return version != null && SAFE_VERSION.matcher(version).matches();
    }

    public boolean containsPathTraversal(String input) {
        return input != null && PATH_TRAVERSAL.matcher(input).find();
    }

    public String sanitizeHtml(String input) {
        if (input == null) return null;
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }

    public String sanitizeForLog(String input) {
        if (input == null) return null;
        return input.replaceAll("[\\r\\n\\t]", "_");
    }
}
