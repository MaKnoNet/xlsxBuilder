package de.makno.xlsxbuilder.builder;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ersetzt {@code {key}}-Platzhalter in Titel-, Kopf- und Footer-Texten durch konfigurierte Werte.
 * Unbekannte Platzhalter bleiben unverändert stehen (sichtbar, statt still verschluckt).
 */
final class Placeholders {

    private static final Pattern TOKEN = Pattern.compile("\\{([^{}]+)\\}");

    private Placeholders() {}

    /** Ersetzt alle {@code {key}}-Tokens in {@code text} anhand von {@code values}. */
    static String resolve(String text, Map<String, String> values) {
        return resolve(text, values, null);
    }

    /**
     * Ersetzt alle {@code {key}}-Tokens in {@code text}. Je Token gilt die Reihenfolge: zuerst die
     * statische {@code values}-Map, dann – falls dort nicht vorhanden – der optionale
     * {@code fallback}-Resolver (lazy/berechnete Werte). Liefert auch dieser {@code null}, bleibt das
     * Token unverändert stehen. Die statische Map hat damit Vorrang vor dem Resolver.
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
