package com.blog.util;

public class SlugUtils {
    public static String slugify(String input) {
        if (input == null || input.isBlank()) return "";
        return input
            .toLowerCase()
            .trim()
            .replaceAll("[^a-z0-9\\s-]", "")  // remove special chars
            .replaceAll("\\s+", "-")           // spaces to hyphens
            .replaceAll("-+", "-")            // collapse multiple hyphens
            .replaceAll("^-|-$", "");          // trim leading/trailing hyphens
    }
}
