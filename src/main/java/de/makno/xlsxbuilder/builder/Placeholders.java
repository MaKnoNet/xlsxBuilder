package de.makno.xlsxbuilder.builder;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces {@code {key}} placeholders in title, header and footer texts with configured values.
 * Unknown placeholders are left unchanged (visible, instead of silently swallowed).
 */
final class Placeholders {

    private static final Pattern TOKEN = Pattern.compile("\\{([^{}]+)\\}");

    private Placeholders() {}

    /** Replaces all {@code {key}} tokens in {@code text} using {@code values}. */
    static String resolve(String text, Map<String, String> values) {
        return resolve(text, values, null);
    }

    /**
     * Replaces all {@code {key}} tokens in {@code text}. Per token the order is: first the static
     * {@code values} map, then – if not present there – the optional {@code fallback} resolver
     * (lazy/computed values). If that too returns {@code null}, the token is left unchanged. The static
     * map therefore takes precedence over the resolver.
     */
    static String resolve(String text, Map<String, String> values, Function<String, String> fallback) {
        if (text == null || text.indexOf('{') < 0 || (values.isEmpty() && fallback == null)) {
            return text;
        }
        Matcher matcher = TOKEN.matcher(text);
        StringBuilder result = new StringBuilder(text.length() + 16);
        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = values.get(key);
            if (replacement == null && fallback != null) {
                replacement = fallback.apply(key);
            }
            matcher.appendReplacement(
                    result,
                    replacement == null
                            ? Matcher.quoteReplacement(matcher.group())
                            : Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
