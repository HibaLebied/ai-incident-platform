package com.hiba.aianalysisservice.rag;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class DocumentChunker {

    private static final Pattern SENTENCE_SEPARATOR = Pattern.compile("(?<=[.!?])\\s+");

    private final RagProperties properties;

    public DocumentChunker(RagProperties properties) {
        this.properties = properties;
    }

    public List<DocumentChunk> chunk(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return List.of();
        }
        String text = rawText.replace("\r\n", "\n").replace('\r', '\n').trim();
        int size = properties.getChunking().getMaxSize();
        int overlap = properties.getChunking().getOverlap();
        if (size <= 0 || overlap < 0 || overlap >= size) {
            throw new IllegalArgumentException("Chunk size must be positive and overlap must be smaller than size");
        }

        List<String> units = splitIntoUnits(text, size);
        List<DocumentChunk> chunks = new ArrayList<>();
        List<String> prefix = List.of();
        int cursor = 0;
        while (cursor < units.size()) {
            List<String> chunkUnits = new ArrayList<>(prefix);
            int startOfNewContent = cursor;
            while (cursor < units.size()) {
                String candidate = join(chunkUnits, units.get(cursor));
                if (!chunkUnits.isEmpty() && candidate.length() > size) {
                    if (cursor == startOfNewContent && !prefix.isEmpty()) {
                        chunkUnits.clear();
                        continue;
                    }
                    break;
                }
                chunkUnits.add(units.get(cursor++));
            }
            chunks.add(new DocumentChunk(chunks.size(), String.join("\n\n", chunkUnits)));
            prefix = overlapUnits(units, startOfNewContent, cursor, overlap);
        }
        return List.copyOf(chunks);
    }

    private List<String> splitIntoUnits(String text, int maxSize) {
        List<String> units = new ArrayList<>();
        for (String block : markdownBlocks(text)) {
            if (block.startsWith("```") || block.startsWith("~~~")) {
                addBounded(units, block, maxSize);
            } else if (isListItem(block)) {
                addBounded(units, block.replaceAll("\\s+", " ").trim(), maxSize);
            } else {
                for (String sentence : SENTENCE_SEPARATOR.split(block)) {
                    String normalized = sentence.replaceAll("\\s+", " ").trim();
                    if (!normalized.isEmpty()) {
                        addBounded(units, normalized, maxSize);
                    }
                }
            }
        }
        return units;
    }

    private List<String> markdownBlocks(String text) {
        List<String> blocks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inCode = false;
        for (String line : text.split("\\n", -1)) {
            String trimmed = line.trim();
            boolean fence = trimmed.startsWith("```") || trimmed.startsWith("~~~");
            if (fence) {
                if (!inCode) flush(current, blocks);
                current.append(line).append('\n');
                inCode = !inCode;
            } else if (inCode) {
                current.append(line).append('\n');
            } else if (trimmed.isEmpty()) {
                flush(current, blocks);
            } else if (trimmed.startsWith("#")) {
                flush(current, blocks);
                blocks.add(trimmed);
            } else if (isListItem(trimmed)) {
                flush(current, blocks);
                blocks.add(trimmed);
            } else {
                current.append(line).append(' ');
            }
        }
        flush(current, blocks);
        return blocks;
    }

    private void addBounded(List<String> units, String value, int maxSize) {
        String normalized = value.trim();
        if (normalized.length() <= maxSize) {
            units.add(normalized);
            return;
        }
        for (int start = 0; start < normalized.length(); start += maxSize) {
            units.add(normalized.substring(start, Math.min(start + maxSize, normalized.length())).trim());
        }
    }

    private List<String> overlapUnits(List<String> units, int start, int end, int maxOverlap) {
        if (maxOverlap == 0 || start >= end) return List.of();
        List<String> result = new ArrayList<>();
        int length = 0;
        for (int index = end - 1; index >= start; index--) {
            String unit = units.get(index);
            int additional = unit.length() + (result.isEmpty() ? 0 : 2);
            if (length + additional > maxOverlap) break;
            result.add(0, unit);
            length += additional;
        }
        return result;
    }

    private String join(List<String> current, String next) {
        return current.isEmpty() ? next : String.join("\n\n", current) + "\n\n" + next;
    }

    private boolean isListItem(String value) {
        return value.startsWith("- ") || value.startsWith("* ") || value.matches("\\d+\\.\\s+.*");
    }

    private void flush(StringBuilder current, List<String> blocks) {
        if (current.length() > 0) {
            String value = current.toString().trim();
            if (!value.isEmpty()) blocks.add(value);
            current.setLength(0);
        }
    }
}
