package com.ortodontalio.smindless.model;

import java.util.List;

/**
 * Smile record
 * @param key is smile key.
 * @param inputs are placeholders.
 * @param output is output.
 * @param premium premium-flag.
 */
public record Smile(String key, List<String> inputs, String output, boolean premium) {
}
