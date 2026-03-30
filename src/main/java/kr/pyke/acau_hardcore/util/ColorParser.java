package kr.pyke.acau_hardcore.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorParser {
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:#([A-Fa-f0-9]{6}):#([A-Fa-f0-9]{6})>(.*?)</gradient>");

    public static MutableComponent parse(String text) {
        MutableComponent result = Component.empty();
        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(text);
        int lastEnd = 0;

        while (gradientMatcher.find()) {
            String before = text.substring(lastEnd, gradientMatcher.start());
            if (!before.isEmpty()) {
                result.append(parseHex(before));
            }

            String hex1 = gradientMatcher.group(1);
            String hex2 = gradientMatcher.group(2);
            String content = gradientMatcher.group(3);

            result.append(applyGradient(content, hex1, hex2));
            lastEnd = gradientMatcher.end();
        }

        String tail = text.substring(lastEnd);
        if (!tail.isEmpty()) {
            result.append(parseHex(tail));
        }

        return result;
    }

    private static MutableComponent parseHex(String text) {
        MutableComponent result = Component.empty();
        Matcher matcher = HEX_PATTERN.matcher(text);
        int lastEnd = 0;
        TextColor currentColor = null;

        while (matcher.find()) {
            String before = text.substring(lastEnd, matcher.start());
            if (!before.isEmpty()) {
                MutableComponent part = Component.literal(before);
                if (currentColor != null) {
                    part.withStyle(Style.EMPTY.withColor(currentColor));
                }
                result.append(part);
            }

            currentColor = TextColor.parseColor("#" + matcher.group(1)).getOrThrow();
            lastEnd = matcher.end();
        }

        String tail = text.substring(lastEnd);
        if (!tail.isEmpty()) {
            MutableComponent part = Component.literal(tail);
            if (currentColor != null) {
                part.withStyle(Style.EMPTY.withColor(currentColor));
            }
            result.append(part);
        }

        return result;
    }

    private static MutableComponent applyGradient(String text, String hex1, String hex2) {
        MutableComponent result = Component.empty();
        int c1 = Integer.parseInt(hex1, 16);
        int c2 = Integer.parseInt(hex2, 16);

        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;

        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;

        int length = text.length();
        for (int i = 0; i < length; i++) {
            float ratio = 0f;
            if (length > 1) {
                ratio = (float) i / (length - 1);
            }

            int r = (int) (r1 + (r2 - r1) * ratio);
            int g = (int) (g1 + (g2 - g1) * ratio);
            int b = (int) (b1 + (b2 - b1) * ratio);

            int color = (r << 16) | (g << 8) | b;
            result.append(Component.literal(String.valueOf(text.charAt(i))).withStyle(Style.EMPTY.withColor(color)));
        }

        return result;
    }
}