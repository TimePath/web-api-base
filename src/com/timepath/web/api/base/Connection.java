package com.timepath.web.api.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author timepath
 */
public abstract class Connection {

    protected Connection() {
    }

    public Connection(String base, String method) {
        if (base == null) {
            base = getBaseUrl();
        }
        this.address = base + method;
        connect();
    }
    
    private static final Logger LOG = Logger.getLogger(Connection.class.getName());
    public HttpURLConnection con = null;
    private String address;

    public void connect() {
        LOG.log(Level.INFO, "Connecting: {0}", address);
        try {
            URL u = URI.create(address).toURL();
            con = (HttpURLConnection) u.openConnection();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        con.setRequestProperty("User-Agent", getUserAgent());
        con.setReadTimeout(30000);
        con.setDoOutput(true);
    }

    public abstract String getBaseUrl();

    public abstract String getUserAgent();

    public JSONObject post(String data) {
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(con.getOutputStream()));
            pw.write(data);
            pw.close();
            return get(false);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Unable to write: {0}", e.toString());
        }
        return null;
    }

    public JSONObject get() {
        return get(true);
    }

    public JSONObject get(boolean useCache) {
        if (useCache) {
            String ret = getCache();
            if (ret != null) {
                System.out.println(ret);
                try {
                    return new JSONObject(ret);
                } catch (JSONException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder(8192);
            String tmp;
            while ((tmp = br.readLine()) != null) {
                sb.append(tmp).append("\n");
            }
            br.close();

            if (useCache) {
                Cache.write(address, sb.toString());
            }

            try {
                Thread.sleep(2000L);
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            System.out.println(sb.toString());
            try {
                return new JSONObject(sb.toString());
            } catch (JSONException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "READ FAILED: {0}", e.toString());
            return null;
        }
        return null;
    }

    private String getCache() {
        byte[] t = Cache.read(address);
        String cached = null;
        if (t != null) {
            cached = new String(t);
        }
        if (cached != null) {
            LOG.log(Level.INFO, "MSG: {0}", "Using cache for " + address);
            return cached;
        }
        return null;
    }
}
