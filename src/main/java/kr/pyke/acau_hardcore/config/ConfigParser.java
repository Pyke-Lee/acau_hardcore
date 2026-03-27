package kr.pyke.acau_hardcore.config;

import kr.pyke.acau_hardcore.AcauHardCore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigParser {
    private ConfigParser() { }

    public static Map<String, String> load(Path path) {
        Map<String, String> entries = new LinkedHashMap<>();
        if (!Files.exists(path)) { return entries; }

        try {
            List<String> lines = Files.readAllLines(path);
            for (int i = 0; i < lines.size(); ++i) {
                String line = lines.get(i).trim();
                if (line.isEmpty() || line.startsWith("#")) { continue; }

                int colonIndex = line.indexOf(':');
                if (colonIndex == -1) {
                    AcauHardCore.LOGGER.warn("Config 파싱 오류 ({}줄): 콜론(:)이 없습니다 - \"{}\"", i + 1, line);
                    continue;
                }

                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();

                if (key.isEmpty()) {
                    AcauHardCore.LOGGER.warn("Config 파싱 오류 ({}줄): 키가 비었습니다 - \"{}\"", i + 1, line);
                    continue;
                }

                entries.put(key, value);
            }
        }
        catch (Exception e) { AcauHardCore.LOGGER.error("Config 파일 읽기 실패: {}", path, e); }

        return entries;
    }

    public static void save(Path path, List<ConfigEntry> entries) {
        StringBuilder stringBuilder = new StringBuilder();

        for (ConfigEntry entry : entries) {
            if (entry instanceof ConfigEntry.Comment(String text)) {
                stringBuilder.append("# ").append(text).append("\n");
            }
            else if (entry instanceof ConfigEntry.BlankLine) {
                stringBuilder.append("\n");
            }
            else if (entry instanceof ConfigEntry.Value(String key, Object value1)) {
                stringBuilder.append(key).append(": ").append(value1).append("\n");
            }
        }

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, stringBuilder.toString());
        }
        catch (IOException e) { AcauHardCore.LOGGER.error("Config 파일 저장 실패: {}", path, e); }
    }

    public sealed interface ConfigEntry {
        record Comment(String text) implements ConfigEntry { }
        record BlankLine() implements ConfigEntry { }
        record Value(String key, Object value) implements ConfigEntry { }
    }
}
