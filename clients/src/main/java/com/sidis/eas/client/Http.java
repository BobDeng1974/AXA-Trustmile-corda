package com.sidis.eas.client;


import kotlin.text.Charsets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Http {
    public static String POST(String urlstr, String contentString, String authorizationHeader) throws IOException {
        return Http.ajax("POST", urlstr, contentString, authorizationHeader);
    }
    public static String GET(String urlstr, String contentString, String authorizationHeader) throws IOException {
        return Http.ajax("GET", urlstr, contentString, authorizationHeader);
    }
    public static String ajax(String method, String urlstr, String contentString, String authorizationHeader) throws IOException {
        byte[] content = contentString.getBytes(Charsets.UTF_8);

        InputStream is = null;
        byte[] data = null;
        ByteArrayOutputStream baos = null;
        try {
            final URL url = new URL(urlstr);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            // If using a XML post content:
            //connection.setRequestProperty("Content-Type", "text/xml");
            connection.setRequestProperty("Content-Length", String.valueOf(content.length));
            if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer "+authorizationHeader);
            }
            if (contentString != null && !contentString.isEmpty()) {
                OutputStream os = null;
                os = connection.getOutputStream();
                os.write(content);
                os.close();
            }
            is = connection.getInputStream();
            final byte[] buffer = new byte[2 * 1024];
            baos = new ByteArrayOutputStream();
            int n;
            while ((n = is.read(buffer)) >= 0) {
                baos.write(buffer, 0, n);
            }
            data = baos.toByteArray();
        } catch (IOException e) {
            throw e;
        } finally {
            if (is != null) is.close();
            if (baos != null) baos.close();
        }
        return new String(data, Charsets.UTF_8);
    }
}
