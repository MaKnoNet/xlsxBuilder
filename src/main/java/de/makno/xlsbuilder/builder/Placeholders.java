package de.makno.xlsbuilder.builder;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ersetzt {@code {key}}-Platzhalter in Titel-, Kopf- und Footer-Texten durch konfigurierte Werte.
 * Unbekannte Platzhalter bleiben unverändert stehen (sichtbar, statt still verschluckt).
 */
final class Placeholders {

    private static final Pattern TOKEN = Pattern.compile("\\{([^{}]+)\\}");

    private Placeholders() {
    }

    /** Ersetzt alle {@code {key}}-Tokens in {@code text} anhand von {@code values}. */
    static String resolve(String text, Map<String, String> values) {
        if (text == null || text.indexOf('{') < 0 || values.isEmpty()) {
            return text;
        }
        Matcher matcher = TOKEN.matcher(text);
        StringBuilder result = new StringBuilder(text.length() + 16);
        while (matcher.find()) {
            String replacement = values.get(matcher.group(1));
            matcher.appendReplacement(
                    result, replacement == null ? Matcher.quoteReplacement(matcher.group()) : Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
