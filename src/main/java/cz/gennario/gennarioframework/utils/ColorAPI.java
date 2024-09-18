package cz.gennario.gennarioframework.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorAPI {

    public static String formatHexColor(String message) {
        // Regex for single hex color: {#hex} or <#hex>
        Pattern singleHexPattern = Pattern.compile("[{<]#([A-Fa-f0-9]{6})[}>]");

        // Regex for gradient: {gradient:#hex:#hex:...}text{/gradient} or <gradient:#hex:#hex:...>text</gradient>
        Pattern gradientPattern = Pattern.compile("[{<]gradient:((?:#[A-Fa-f0-9]{6}:?)+)[}>](.*?)[{<]gradient[}>]");

        Matcher matcher = gradientPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();

        // Processing gradients
        while (matcher.find()) {
            String[] colors = matcher.group(1).split(":");
            String text = matcher.group(2);
            matcher.appendReplacement(buffer, applyGradient(colors, text));
        }
        matcher.appendTail(buffer);
        message = buffer.toString();

        matcher = singleHexPattern.matcher(message);
        buffer.setLength(0);

        // Processing standard hex color
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            StringBuilder formattedColor = new StringBuilder("§x");
            for (char ch : hexColor.toCharArray()) {
                formattedColor.append("§").append(ch);
            }
            matcher.appendReplacement(buffer, formattedColor.toString());
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    private static String applyGradient(String[] colors, String text) {
        StringBuilder gradientText = new StringBuilder();
        int textLength = text.length();
        int colorCount = colors.length;

        for (int i = 0; i < textLength; i++) {
            float ratio = (float) i / (float) (textLength - 1);
            int colorIndex = (int) Math.floor(ratio * (colorCount - 1));
            int nextColorIndex = Math.min(colorIndex + 1, colorCount - 1);

            int[] startRGB = hexToRGB(colors[colorIndex].substring(1));
            int[] endRGB = hexToRGB(colors[nextColorIndex].substring(1));

            float segmentRatio = (ratio * (colorCount - 1)) - colorIndex;
            int[] blendedRGB = new int[3];
            for (int j = 0; j < 3; j++) {
                blendedRGB[j] = (int) (startRGB[j] + segmentRatio * (endRGB[j] - startRGB[j]));
            }

            gradientText.append(rgbToHex(blendedRGB)).append(text.charAt(i));
        }

        return gradientText.toString();
    }

    private static int[] hexToRGB(String hex) {
        return new int[]{
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        };
    }

    private static String rgbToHex(int[] rgb) {
        return String.format("§x§%s§%s§%s§%s§%s§%s",
                Integer.toHexString((rgb[0] >> 4) & 0xF),
                Integer.toHexString(rgb[0] & 0xF),
                Integer.toHexString((rgb[1] >> 4) & 0xF),
                Integer.toHexString(rgb[1] & 0xF),
                Integer.toHexString((rgb[2] >> 4) & 0xF),
                Integer.toHexString(rgb[2] & 0xF));
    }
}
