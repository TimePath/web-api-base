package com.timepath.web.api.base;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author TimePath
 */
public class RequestBuilder {

    private static final Logger        LOG = Logger.getLogger(RequestBuilder.class.getName());
    private final        StringBuilder sb  = new StringBuilder();

    public RequestBuilder() {}

    /**
     * Builds requests from an array format. Sub arrays of any length other than
     * 2 are ignored. String[][] arr = { {"key", "val"} };
     *
     * @param arr
     *         *
     *
     * @return
     */
    public static RequestBuilder fromArray(Object[][] arr) {
        RequestBuilder rb = new RequestBuilder(); for(Object[] arr1 : arr) {
            if(arr1.length != 2) {
                continue;
            } rb.append(arr1[0].toString(), arr1[1].toString());
        } return rb;
    }

    public RequestBuilder append(String key, String val) {
        if(sb.length() > 0) {
            sb.append('&');
        } key = encode(key); val = encode(val); sb.append(key).append('=').append(val); return this;
    }

    private static String encode(String str) {
        try {
            return URLEncoder.encode(str, "ISO-8859-1");
        } catch(UnsupportedEncodingException ex) {
            Logger.getLogger(RequestBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } return null;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
