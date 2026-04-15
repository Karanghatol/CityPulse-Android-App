package com.citypulse.utils;

public class TimeUtils {
    public static String getRelativeTime(long ms) {
        long diff = System.currentTimeMillis() - ms;
        if (diff < 60_000)           return "Just now";
        if (diff < 3_600_000)        return (diff / 60_000)    + "m ago";
        if (diff < 86_400_000)       return (diff / 3_600_000) + "h ago";
        if (diff < 2 * 86_400_000L)  return "Yesterday";
        return (diff / 86_400_000)   + "d ago";
    }
}
