package fr.world.nations.util;

import org.bukkit.ChatColor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class StringUtil {

    public static String equ(int length, String... inputs) {
        int totalLength = 0;
        for (String input : inputs) {
            totalLength += input.length();
        }
        if (totalLength >= length) return String.join(" ", inputs);
        int spaceLength = length - totalLength;
        String[] spaces = new String[inputs.length - 1];
        Arrays.fill(spaces, StringUtil.mult(" ", (int) Math.floor(spaceLength / (double) inputs.length)));
        for (int i = 0; i < spaceLength % inputs.length; i++) {
            spaces[i] += " ";
        }
        StringBuilder result = new StringBuilder();
        int spaceIndex = 0;
        for (String input : inputs) {
            result.append(input);
            if (spaceIndex < spaces.length) {
                result.append(spaces[spaceIndex]);
                spaceIndex += 1;
            }
        }
        return result.toString();
    }

    public static String centered(String input, int length) {
        if (input == null) return null;
        int txtLength = ChatColor.stripColor(input).length();
        if (txtLength >= length) return input;
        int spaceLength = length - txtLength;
        return mult(" ", (int) Math.ceil(spaceLength / 2d)) + input + mult(" ", (int) Math.floor(spaceLength / 2d));
    }

    public static String mult(String input, int factor) {
        if (input == null) return null;
        return input.repeat(Math.max(0, factor));
    }

    public static String round(double value, int digits) {
        if (digits < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(digits, RoundingMode.HALF_UP);
        return String.valueOf(bd.doubleValue());
    }

    public static String numb(double numb) {
        if (numb % 1 == 0) return String.valueOf((int) numb);
        return round(numb, 2);
    }

    public static long toMillis(String time) {
        long amount = 0;
        String fTime = time
                .replace("-", "")
                .replace(" ", "");
        if (fTime.contains("we")) {
            String temp = fTime.replace("we", "-");
            int index = temp.indexOf("-");
            String weekAmount = temp.substring(0, index);
            try {
                int weekAmountInt = Integer.parseInt(weekAmount);
                amount += 7d * 24 * 3600 * 1000 * weekAmountInt;
            } catch (NumberFormatException e) {
                System.out.println("Attention, le temps entre chaque baisse des récompenses KOTH a mal été entré !");
                System.out.println("Temps entré : " + time + " devrait être entré comme cela : ??we??da??hou??min??sec");
            }
            fTime = fTime.substring(index + "we".length());
        }
        if (fTime.contains("da")) {
            String temp = fTime.replace("da", "-");
            int index = temp.indexOf("-");
            String dayAmount = temp.substring(0, index);
            try {
                int dayAmountInt = Integer.parseInt(dayAmount);
                amount += 24d * 3600 * 1000 * dayAmountInt;
            } catch (NumberFormatException e) {
                System.out.println("Attention, le temps entre chaque baisse des récompenses KOTH a mal été entré !");
                System.out.println("Temps entré : " + time + " devrait être entré comme cela : ??we??da??hou??min??sec");
            }
            fTime = fTime.substring(index + "da".length());
        }
        if (fTime.contains("hou")) {
            String temp = fTime.replace("hou", "-");
            int index = temp.indexOf("-");
            String hourAmount = temp.substring(0, index);
            try {
                int hourAmountInt = Integer.parseInt(hourAmount);
                amount += 3600d * 1000 * hourAmountInt;
            } catch (NumberFormatException e) {
                System.out.println("Attention, le temps entre chaque baisse des récompenses KOTH a mal été entré !");
                System.out.println("Temps entré : " + time + " devrait être entré comme cela : ??we??da??hou??min??sec");
            }
            fTime = fTime.substring(index + "hou".length());
        }
        if (fTime.contains("min")) {
            String temp = fTime.replace("min", "-");
            int index = temp.indexOf("-");
            String minAmount = temp.substring(0, index);
            try {
                int minAmountInt = Integer.parseInt(minAmount);
                amount += 60d * 1000 * minAmountInt;
            } catch (NumberFormatException e) {
                System.out.println("Attention, le temps entre chaque baisse des récompenses KOTH a mal été entré !");
                System.out.println("Temps entré : " + time + " devrait être entré comme cela : ??we??da??hou??min??sec");
            }
            fTime = fTime.substring(index + "min".length());
        }
        if (fTime.contains("sec")) {
            String temp = fTime.replace("sec", "-");
            int index = temp.indexOf("-");
            String weekAmount = temp.substring(0, index);
            try {
                int weekAmountInt = Integer.parseInt(weekAmount);
                amount += 1000d * weekAmountInt;
            } catch (NumberFormatException e) {
                System.out.println("Attention, le temps entre chaque baisse des récompenses KOTH a mal été entré !");
                System.out.println("Temps entré : " + time + " devrait être entré comme cela : ??we??da??hou??min??sec");
            }
            //fTime = fTime.substring(index+1);
        }
        return amount;
    }
}
