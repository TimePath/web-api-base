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
public class Connection extends HttpURLConnection {

    protected Connection(URL u) {
        super(u);
    }

    public Connection(String address) throws MalformedURLException {
        this(URI.create(address).toURL());
    }

    private static final Logger LOG = Logger.getLogger(Connection.class.getName());

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
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        setRequestProperty("User-Agent", getUserAgent());
        setReadTimeout(30000);
        setDoOutput(true);
    }

    @Override
    public void disconnect() {
//        super.disconnect();
    }

    @Override
    public boolean usingProxy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connect() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String getUserAgent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    protected String postm(String data, boolean get) {
        connect("");
        System.out.println(">>> " + data);
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(getOutputStream()));
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
            BufferedReader br = new BufferedReader(new InputStreamReader(getInputStream()));
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
