package fr.world.nations.util;

public class TimerUtil {

    public static boolean deltaUpMillis(long startMillis, long millis) {
        if (millis < 0) return true;
        return System.currentTimeMillis() > (startMillis + millis);
    }

    public static boolean deltaUpSec(long startMillis, long secs) {
        return deltaUpMillis(startMillis, secs * 1000);
    }

    public static boolean deltaUpMins(long startMillis, long mins) {
        return deltaUpSec(startMillis, mins * 60);
    }

    public static boolean deltaUpHours(long startMillis, long hours) {
        return deltaUpMins(startMillis, hours * 60);
    }

    public static boolean deltaUpDays(long startMillis, long days) {
        return deltaUpHours(startMillis, days * 24);
    }

}
