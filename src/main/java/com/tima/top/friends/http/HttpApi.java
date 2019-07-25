package com.tima.top.friends.http;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class HttpApi {
    static Logger LOG = LoggerFactory.getLogger(HttpApi.class);
    private int connectTimeout;
    private int requestTimeout;

    private static HttpApi httpApi;

    public HttpApi() {
        this(10000,10000);
    }

    public static HttpApi getInstance(){
        if(httpApi == null){
            httpApi = new HttpApi();
        }
        return httpApi;
    }

    public HttpApi(int connectTimeout, int requestTimeout) {
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public ContentResponse post(String url, JSONObject header, JSONObject body){
        try {
            SslContextFactory sslContextFactory = new SslContextFactory();
            HttpClient httpClient = new HttpClient(sslContextFactory);
            httpClient.setConnectTimeout(connectTimeout);
            httpClient.start();

            Request request = httpClient.POST(url);
            request.timeout(requestTimeout, TimeUnit.MILLISECONDS);
            for (String key : header.keySet()) {
                request.header(key, header.getString(key));
            }
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.content(new StringContentProvider(body.toString()), "application/json");
            LOG.info("Body {}", body);
            ContentResponse response = request.send();
            httpClient.stop();
            return response;
        }catch (Exception e){
            e.printStackTrace();
            LOG.error(e.toString());
            return null;
        }
    }

    public ContentResponse post(String url,  String body){
        try {
            SslContextFactory sslContextFactory = new SslContextFactory();
            HttpClient httpClient = new HttpClient(sslContextFactory);
            httpClient.setConnectTimeout(connectTimeout);
            httpClient.start();

            Request request = httpClient.POST(url);
            request.timeout(requestTimeout, TimeUnit.MILLISECONDS);

            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.content(new StringContentProvider(body), "application/json");
            LOG.info("Body {}", body);
            ContentResponse response = request.send();
            httpClient.stop();
            return response;
        }catch (Exception e){
            e.printStackTrace();
            LOG.error(e.toString());
            return null;
        }
    }

    public ContentResponse get(String url, JSONObject header, JSONObject body){
        try {
            SslContextFactory sslContextFactory = new SslContextFactory();
            HttpClient httpClient = new HttpClient(sslContextFactory);
            httpClient.setConnectTimeout(connectTimeout);
            httpClient.start();

//            header.put(String.valueOf(HttpHeader.CONTENT_TYPE), "application/json");
            ContentResponse response = get(url, header, body);
//            request.timeout(requestTimeout, TimeUnit.MILLISECONDS);
//            for (String key : header.keySet()) {
//                request.header(key, header.getString(key));
//            }
//            request.header(HttpHeader.CONTENT_TYPE, "application/json");
//            request.content(new StringContentProvider(body.toString()), "application/json");
            LOG.info("Body {}", body);
//            ContentResponse response = request.send();
            httpClient.stop();
            return response;
        }catch (Exception e){
            e.printStackTrace();
            LOG.error(e.toString());
            return null;
        }
    }
}
