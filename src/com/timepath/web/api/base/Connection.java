package com.timepath.web.api.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TimePath
 */
public abstract class Connection {

    private static final Logger LOG = Logger.getLogger(Connection.class.getName());

    protected String base;

    public String getBaseUrl() {
        return base;
    }

    protected abstract long mindelay();

    public HttpURLConnection connect(String method) throws MalformedURLException {
        String address = getBaseUrl();
        if(method != null) {
            if(!address.endsWith("/") && !method.startsWith("/")) {
                address += "/";
            }
            address += method;
        }
        LOG.log(Level.INFO, "Connecting: {0}", address);
        URL url = URI.create(address).toURL();
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", getUserAgent());
            con.setReadTimeout(30000);
            con.setDoOutput(true);
            onConnect(con);
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        connection = con;
        return con;
    }

    public abstract String getUserAgent();

    protected void onConnect(HttpURLConnection con) {
    }

    public String get(String method, boolean useCache) throws MalformedURLException {
        HttpURLConnection con = connect(method);
        return getm(con, useCache);
    }

    public String get(String method) throws MalformedURLException {
        return get(method, true);
    }

    public void post(String method, String data) throws MalformedURLException {
        postm(method, data, false);
    }

    public String postget(String method, String data) throws MalformedURLException {
        return postm(method, data, true);
    }

    private HttpURLConnection connection;

    public HttpURLConnection getCon() {
        return connection;
    }

    public String getm(HttpURLConnection con, boolean useCache) {
        if(useCache) {
            String ret = getCache();
            if(ret != null) {
                LOG.log(Level.INFO, "<<< (cache) {0}", ret);
                return ret;
            }
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder(8192);
            String tmp;
            while((tmp = br.readLine()) != null) {
                sb.append(tmp).append("\n");
            }
            br.close();

            if(useCache) {
                Cache.write("", sb.toString());
            }

//            if(mindelay() > 0) {
//                try {
//                    Thread.sleep(mindelay());
//                } catch(InterruptedException ex) {
//                    LOG.log(Level.SEVERE, null, ex);
//                }
//            }
            LOG.log(Level.INFO, "<<< {0}", sb.toString());
            return sb.toString();
        } catch(IOException e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "READ FAILED: {0}", e.toString());
            return null;
        }
    }

    protected String postm(String method, String data, boolean get) throws MalformedURLException {
        HttpURLConnection con = connect(method);
        LOG.log(Level.INFO, ">>> {0}", data);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(con.getOutputStream()));
            pw.write(data);
            pw.flush();
        } catch(IOException e) {
            LOG.log(Level.SEVERE, "Unable to write: {0}", e.toString());
        } finally {
            if(pw != null) {
                pw.close();
            }
        }
        if(get) {
            return getm(con, false);
        }
        return null;
    }

    private String getCache() {
        byte[] t = Cache.read("");
        String cached = null;
        if(t != null) {
            cached = new String(t);
        }
        if(cached != null) {
            LOG.log(Level.INFO, "MSG: {0}", "Using cache for " + "");
            return cached;
        }
        return null;
    }

}
