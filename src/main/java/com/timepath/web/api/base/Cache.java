package com.timepath.web.api.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author TimePath
 */
public class Cache {

    private static final String CACHE_DIRECTORY = "./cache/";
    private static final Logger LOG = Logger.getLogger(Cache.class.getName());
    private static double minutes = 0.5;
    private static boolean enabled;

    static {
        @NotNull File f = new File(CACHE_DIRECTORY);
        f.mkdirs();
    }

    private Cache() {
    }

    @Nullable
    public static byte[] read(@NotNull String url) {
        try {
            @NotNull String file = CACHE_DIRECTORY + '/' + convertToCacheName(url);
            @NotNull File f = new File(file);
            if (!f.exists() || (f.length() < 1)) {
                return null;
            }
            if (f.exists() && tooOld(f.lastModified())) {
                // Delete the cached file if it is too old
                f.delete();
            }
            @NotNull byte[] data = new byte[(int) f.length()];
            @NotNull DataInputStream fis = new DataInputStream(new FileInputStream(f));
            fis.readFully(data);
            fis.close();
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static String convertToCacheName(@NotNull String url) {
        try {
            @NotNull MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(url.getBytes());
            byte[] b = digest.digest();
            @NotNull BigInteger bi = new BigInteger(b);
            return "mycache_" + bi.toString(16) + ".cac";
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "MD5: {0}", e.toString());
            return null;
        }
    }

    private static boolean tooOld(long time) {
        if (!enabled) {
            return true;
        }
        long now = new Date().getTime();
        long diff = now - time;
        return diff >= (1000 * 60 * minutes);
    }

    public static void write(@NotNull String url, String data) {
        try {
            @NotNull String file = CACHE_DIRECTORY + '/' + convertToCacheName(url);
            @NotNull PrintWriter pw = new PrintWriter(new FileWriter(file));
            pw.print(data);
            pw.close();
        } catch (Exception ignored) {
        }
    }
}
