package pw.biome.biomechatrelay.util;

public final class StringFormatUtility {

    public static String capitaliseFirst(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String convertFromMCRoleNameToDiscordRoleName(String mcRoleName) {
        String[] split = mcRoleName.split("_");

        StringBuilder stringBuilder = new StringBuilder();

        for (String s : split) {
            stringBuilder
                    .append(capitaliseFirst(s))
                    .append(" ");
        }

        return stringBuilder.toString();
    }
}
