package com.storyreading.storyreadingbackend.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Pattern;

@Service
public class SensitiveWordService {

    private static final Set<String> SENSITIVE_WORDS = Set.of(
            "địt", "đụ", "cặc", "lồn", "chim", "buồi", "bướm", "đéo", "đcm", "dm",
            "clm", "clgt", "má mày", "cha mày", "thằng ml", "đồ khốn", "đồ chó"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://|www\\.|\\.(com|vn|net|org|io|xyz|me|cc|co)/)",
            Pattern.CASE_INSENSITIVE
    );

    public boolean containsSensitiveWord(String content) {
        if (content == null) return false;
        String lower = content.toLowerCase();
        return SENSITIVE_WORDS.stream().anyMatch(lower::contains);
    }

    public boolean containsUrl(String content) {
        if (content == null) return false;
        return URL_PATTERN.matcher(content).find();
    }

    public String filter(String content) {
        if (content == null) return content;
        String result = content;
        for (String word : SENSITIVE_WORDS) {
            String replacement = word.charAt(0) + "*".repeat(word.length() - 1);
            result = result.replaceAll("(?i)" + Pattern.quote(word), replacement);
        }
        return result;
    }
}
