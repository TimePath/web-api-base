package com.timepath.web.api.base;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timepath
 */
public class Cache {

    private static final String cacheDirectory = "./cache/";

    static double minutes = 0.5;

    static {
        File f = new File(cacheDirectory);
        f.mkdirs();
    }

    static public String convertToCacheName(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(url.getBytes());
            byte[] b = digest.digest();
            BigInteger bi = new BigInteger(b);
            return "mycache_" + bi.toString(16) + ".cac";
        } catch(Exception e) {
            LOG.log(Level.SEVERE, "MD5: {0}", e.toString());
            return null;
        }
    }

    static boolean disabled = true;

    static boolean tooOld(long time) {
        if(disabled) {
            return true;
        }
        long now = new Date().getTime();
        long diff = now - time;
        if(diff >= 1000 * 60 * minutes) {
            return true;
        }
        return false;
    }

    public static byte[] read(String url) {
        try {
            String file = cacheDirectory + "/" + convertToCacheName(url);
            File f = new File(file);
            if(!f.exists() || f.length() < 1) {
                return null;
            }
            if(f.exists() && tooOld(f.lastModified())) {
                // Delete the cached file if it is too old
                f.delete();
            }
            byte data[] = new byte[(int) f.length()];
            DataInputStream fis = new DataInputStream(new FileInputStream(f));
            fis.readFully(data);
            fis.close();
            return data;
        } catch(Exception e) {
            return null;
        }
    }

    public static void write(String url, String data) {
        try {
            String file = cacheDirectory + "/" + convertToCacheName(url);
            PrintWriter pw = new PrintWriter(new FileWriter(file));
            pw.print(data);
            pw.close();
        } catch(Exception e) {
        }
    }

    private static final Logger LOG = Logger.getLogger(Cache.class.getName());

    private Cache() {
    }

}
