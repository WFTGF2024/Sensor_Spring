package com.example.sensor_spring.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HashingUtil {

    public static String sha256File(Path path) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream is = new BufferedInputStream(Files.newInputStream(path))) {
                byte[] buf = new byte[8 * 1024 * 1024];
                int read;
                while ((read = is.read(buf)) != -1) {
                    md.update(buf, 0, read);
                }
            }
            return bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String sha256OfZipAggregate(Path zipPath) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (ZipFile zf = new ZipFile(zipPath.toFile())) {
                List<? extends ZipEntry> entries = Collections.list((Enumeration<? extends ZipEntry>) zf.entries());
                List<ZipEntry> files = new ArrayList<>();
                for (ZipEntry e : entries) if (!e.isDirectory()) files.add(e);
                files.sort((a, b) -> a.getName().compareTo(b.getName()));
                byte[] buf = new byte[8 * 1024 * 1024];
                for (ZipEntry e : files) {
                    try (InputStream is = new BufferedInputStream(zf.getInputStream(e))) {
                        int read;
                        while ((read = is.read(buf)) != -1) md.update(buf, 0, read);
                    }
                }
            }
            return bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
