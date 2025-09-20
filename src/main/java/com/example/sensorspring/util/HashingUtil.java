package com.example.sensorspring.util;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class HashingUtil {
    public static String sha256Hex(InputStream in) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (DigestInputStream dis = new DigestInputStream(in, md)) {
            byte[] buffer = new byte[8192]; while (dis.read(buffer) != -1) {}
        }
        return toHex(md.digest());
    }
    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length*2);
        for (byte b : bytes){ sb.append(Character.forDigit((b>>4)&0xF,16)); sb.append(Character.forDigit(b&0xF,16)); }
        return sb.toString();
    }
}
