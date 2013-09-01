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
 * @author timepath
 */
public abstract class Connection {

    private HttpURLConnection con;

    URL url;

    String base;

    String method;

    public Connection(String base, String method) throws MalformedURLException {
        if(base != null) {
            this.base = base;
        }
        this.method = method;
        this.url = URI.create(getBaseUrl() + method).toURL();
    }

    public Connection(String method) throws MalformedURLException {
        this(null, method);
    }

    private static final Logger LOG = Logger.getLogger(Connection.class.getName());

    public String getBaseUrl() {
        return base;
    }

    public void connect(String method) {
        String address = "";
        if(method != null) {
            if(!address.endsWith("/") && !method.startsWith("/")) {
                address += "/";
            }
            address += method;
        }
        LOG.log(Level.INFO, "Connecting: {0}", address);
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", getUserAgent());
            con.setReadTimeout(30000);
            con.setDoOutput(true);
            onConnect();
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public abstract String getUserAgent();

    public HttpURLConnection getCon() {
        return con;
    }

    protected void onConnect() {
    }

    ;

    protected String postm(String data, boolean get) {
        connect("");
        System.out.println(">>> " + data);
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(con.getOutputStream()));
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
        return get("");
    }

    public String get(boolean useCache) {
        return get("", useCache);
    }

    public String get(String method) {
        return get(method, true);
    }

    public String get(String method, boolean useCache) {
        if(useCache) {
            String ret = getCache();
            if(ret != null) {
                System.out.println("<<< " + ret);
                return ret;
            }
        }
        connect(method);
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
