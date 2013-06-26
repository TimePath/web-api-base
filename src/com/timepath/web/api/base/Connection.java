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

/**
 *
 * @author timepath
 */
public abstract class Connection {

    protected Connection() {
    }

    public Connection(String method) {
        this(null, method);
    }

    public Connection(String base, String method) {
        if(base == null) {
            base = getBaseUrl();
        }
        this.address = base + method;
        connect();
    }

    private static final Logger LOG = Logger.getLogger(Connection.class.getName());

    private HttpURLConnection con = null;

    private String address;

    public void connect() {
        LOG.log(Level.INFO, "Connecting: {0}", address);
        try {
            URL u = URI.create(address).toURL();
            con = (HttpURLConnection) u.openConnection();
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        getCon().setRequestProperty("User-Agent", getUserAgent());
        getCon().setReadTimeout(30000);
        getCon().setDoOutput(true);
    }

    public abstract String getBaseUrl();

    public abstract String getUserAgent();

    protected String postm(String data, boolean get) {
        System.out.println(">>> " + data);
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(getCon().getOutputStream()));
            pw.write(data);
            pw.close();
            if(get) {
                return get(false);
            }
        } catch(IOException e) {
            LOG.log(Level.SEVERE, "Unable to write: {0}", e.toString());
        }
        return null;
    }

    public String postget(String data) {
        return postm(data, true);
    }

    public void post(String data) {
        postm(data, false);
    }

    public String get() {
        return get(true);
    }

    public String get(boolean useCache) {
        if(useCache) {
            String ret = getCache();
            if(ret != null) {
                System.out.println("<<< " + ret);
                return ret;
            }
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getCon().getInputStream()));
            StringBuilder sb = new StringBuilder(8192);
            String tmp;
            while((tmp = br.readLine()) != null) {
                sb.append(tmp).append("\n");
            }
            br.close();

            if(useCache) {
                Cache.write(address, sb.toString());
            }

            try {
                Thread.sleep(2000L);
            } catch(InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            System.out.println("<<< " + sb.toString());
            return sb.toString();
        } catch(IOException e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "READ FAILED: {0}", e.toString());
            return null;
        }
    }

    private String getCache() {
        byte[] t = Cache.read(address);
        String cached = null;
        if(t != null) {
            cached = new String(t);
        }
        if(cached != null) {
            LOG.log(Level.INFO, "MSG: {0}", "Using cache for " + address);
            return cached;
        }
        return null;
    }

    /**
     * @return the con
     */
    public HttpURLConnection getCon() {
        return con;
    }

}
