package com.example.sensor_spring.util;

public class FilenameUtil {
    public static String secureFilename(String name) {
        if (name == null) return "file";
        String n = name.replaceAll("[\\/]+", ""); // strip path separators
        n = n.replaceAll("[^A-Za-z0-9._-]", "_");
        if (n.isBlank()) n = "file";
        return n;
    }
}
