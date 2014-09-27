package com.timepath.web.api.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author TimePath
 */
public abstract class Connection {

    private static final Logger LOG = Logger.getLogger(Connection.class.getName());
    @Nullable
    private HttpURLConnection connection;
    private String base;

    protected Connection() {
    }

    @Nullable
    static String getm(@NotNull HttpURLConnection con, boolean useCache) {
        if (useCache) {
            @Nullable String ret = getCache();
            if (ret != null) {
                LOG.log(Level.INFO, "<<< (cache) {0}", ret);
                return ret;
            }
        }
        try {
            @NotNull BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            @NotNull StringBuilder sb = new StringBuilder(8192);
            String tmp;
            while ((tmp = br.readLine()) != null) {
                sb.append(tmp).append('\n');
            }
            br.close();
            if (useCache) {
                Cache.write("", sb.toString());
            }
            //            if(mindelay() > 0) {
            //                try {
            //                    Thread.sleep(mindelay());
            //                } catch(InterruptedException ex) {
            //                    LOG.log(Level.SEVERE, null, ex);
            //                }
            //            }
            LOG.log(Level.INFO, "<<< {0}\n<<< {1}", new Object[]{con.getHeaderFields(), sb.toString()});
            return sb.toString();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "READ FAILED: {0}", e);
            return null;
        }
    }

    @Nullable
    private static String getCache() {
        @Nullable byte[] t = Cache.read("");
        @Nullable String cached = null;
        if (t != null) {
            cached = new String(t);
        }
        if (cached != null) {
            LOG.log(Level.INFO, "MSG: {0}", "Using cache for " + "");
            return cached;
        }
        return null;
    }

    @Nullable
    public String get(String method) throws MalformedURLException {
        return get(method, true);
    }

    @Nullable
    String get(String method, boolean useCache) throws MalformedURLException {
        @Nullable HttpURLConnection con = connect(method);
        return getm(con, useCache);
    }

    @Nullable
    HttpURLConnection connect(@Nullable String method) throws MalformedURLException {
        @Nullable String address = getBaseUrl();
        if (method != null) {
            if (!address.endsWith("/") && !method.startsWith("/")) {
                address += "/";
            }
            address += method;
        }
        LOG.log(Level.INFO, "Connecting: {0}", address);
        @NotNull URL url = URI.create(address).toURL();
        @Nullable HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", getUserAgent());
            con.setReadTimeout(30000);
            con.setDoOutput(true);
            onConnect(con);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        connection = con;
        return con;
    }

    protected String getBaseUrl() {
        return base;
    }

    @NotNull
    protected abstract String getUserAgent();

    protected abstract void onConnect(HttpURLConnection con);

    public void post(String method, @NotNull String data) throws MalformedURLException {
        postm(method, data, false);
    }

    @Nullable
    String postm(String method, @NotNull String data, boolean get) throws MalformedURLException {
        @Nullable HttpURLConnection con = connect(method);
        LOG.log(Level.INFO, ">>> {0}", data);
        @Nullable PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(con.getOutputStream()));
            pw.write(data);
            pw.flush();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Unable to write: {0}", e.toString());
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
        if (get) {
            return getm(con, false);
        }
        return null;
    }

    @Nullable
    public String postget(String method, @NotNull String data) throws MalformedURLException {
        return postm(method, data, true);
    }

    @Nullable
    public HttpURLConnection getCon() {
        return connection;
    }

    protected abstract long mindelay();
}
