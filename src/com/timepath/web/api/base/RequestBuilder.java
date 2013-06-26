package com.timepath.web.api.base;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timepath
 */
public class RequestBuilder {

    public RequestBuilder() {
    }

    public static String encode(String str) {
        try {
            return URLEncoder.encode(str, "ISO-8859-1");
        } catch(UnsupportedEncodingException ex) {
            Logger.getLogger(RequestBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public RequestBuilder append(String key, String val) {
        if(sb.length() > 0) {
            sb.append("&");
        }
        key = encode(key);
        val = encode(val);
        sb.append(key).append("=").append(val);
        return this;
    }

    /**
     * Builds requests from an array format. Sub arrays of any length other than
     * 2 are ignored. String[][] arr = { {"key", "val"} };
     *
     * @param arr
     *            <p/>
     * @return
     */
    public static RequestBuilder fromArray(String[][] arr) {
        RequestBuilder rb = new RequestBuilder();
        for(int i = 0; i < arr.length; i++) {
            if(arr[i].length != 2) {
                continue;
            }
            rb.append(arr[i][0], arr[i][1]);
        }
        return rb;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    private StringBuilder sb = new StringBuilder();

}
