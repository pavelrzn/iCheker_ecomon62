package com.sps.iCheker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sps.iCheker.db.DBase;
import com.sps.iCheker.model.CommentsPack;
import okhttp3.*;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    private DBase dBase = new DBase();
    private String fileName = "C:\\data\\ecoMonitor62.properties";
    private Properties props;
    private long lastLoadedCommentId;
    private long newLoadedCommentId;
    private int loadCount;
    private boolean isCommentLoaded;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");


    public Main () {
        props = new Properties();
            try {
                props.load(new FileInputStream(fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }

        lastLoadedCommentId = Long.parseLong((String) props.getOrDefault("lastLoadedCommentId", "30"));
        loadCount = Integer.parseInt((String) props.getOrDefault("loadCount", "1000"));

        System.out.println("last loaded comment: " + lastLoadedCommentId + " | load count: " + loadCount);
    }


// это нужно для игнорирования ошибок сертификатов ssl (на всякий случай, иногда бывали падения из-за кривого сертификата)
    static {
        TrustManager[] trustAllCertificates = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null; // Not relevant.
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // Do nothing. Just allow them all.
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // Do nothing. Just allow them all.
                    }
                }
        };

        HostnameVerifier trustAllHostnames = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true; // Just allow them all.
            }
        };
        try {
            System.setProperty("jsse.enableSNIExtension", "false");
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCertificates, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(trustAllHostnames);
        }
        catch (GeneralSecurityException e) {
            throw new ExceptionInInitializerError(e);
        }
    }


    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.pullComments();
    }

    private void pullComments () throws IOException  {

        OkHttpClient client = new OkHttpClient().newBuilder().build();

        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        RequestBody body = RequestBody.create("{\"login\":\"Pavelrzn\",\"password\":\"0p9o8i--\"}", mediaType);
        Request request = new Request.Builder()
                .url("https://ecomonitor62.ru/api/jwt/auth")
                .method("POST", body)
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .addHeader("Host", "ecomonitor62.ru")
                .addHeader("Referer", "http://www.fsb.ru/cases/cfo/rzn/society")
                .addHeader("User-Agent", "+7-966-344-66-44")
                .build();

        Response response = client.newCall(request).execute();

        String fullTokenStr = response.body().string();
        Map tokenMap = new ObjectMapper().readValue(fullTokenStr, HashMap.class);
        System.out.println(tokenMap.get("token") + "\n");

        dBase.createTable();
        dBase.connect();
        for (int pageNum=0; !isCommentLoaded; pageNum++) {

            RequestBody body2 = RequestBody.create("{\"page\": " + pageNum + ",\"size\": " + loadCount + "}", mediaType);
            Request request2 = new Request.Builder()
                    .url("https://ecomonitor62.ru/api/comments/list")
                    .method("POST", body2)
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                    .addHeader("Content-Type", "application/json;charset=UTF-8")
                    .addHeader("Host", "ecomonitor62.ru")
                    .addHeader("Referer", "https://ecomonitor62.ru/admin/social")
                    .addHeader("User-Agent", "+7-966-344-66-44")
                    .addHeader("Sec-Fetch-Dest", "empty")
                    .addHeader("Sec-Fetch-Mode", "cors")
                    .addHeader("Sec-Fetch-Site", "same-origin")
                    .build();
            Response response2 = client.newCall(request2).execute();
            String str = response2.body().string();


            CommentsPack commentsPack = (new ObjectMapper().readValue(str, CommentsPack.class));


            for (int id=0; id < commentsPack.getContentArrayLength(); id++) {
                if (pageNum==0 && id==0) newLoadedCommentId = commentsPack.getCommentId(id);
                if (commentsPack.getCommentId(id) > lastLoadedCommentId) {
                    try {
                        System.out.println(
                                dateFormat.format(new Date(commentsPack
                                        .getCommentTime(id) * 1000)) + " | " +
                                        commentsPack.getCommentId(id) + " | " +
                                        commentsPack.getCommentText(id) + " | " +
                                        commentsPack.getLatitude(id) + " | " +
                                        commentsPack.getLongitude(id) + " | " +
                                        commentsPack.getUserId(id) + " | " +
                                        commentsPack.getUserFIO(id));

                        dBase.write(
                                commentsPack.getCommentId(id),
                                commentsPack.getCommentTime(id),
                                commentsPack.getCommentText(id),
                                commentsPack.getUserId(id),
                                commentsPack.getUserFIO(id),
                                commentsPack.getLatitude(id),
                                commentsPack.getLongitude(id));

                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                        System.exit(1);
                    }
                } else {
                    isCommentLoaded = true;
                    props.setProperty("lastLoadedCommentId", String.valueOf(newLoadedCommentId));
                    props.store(new FileOutputStream(fileName), null);
                }

            }

        }
        dBase.commitAndClose();
    }


}


