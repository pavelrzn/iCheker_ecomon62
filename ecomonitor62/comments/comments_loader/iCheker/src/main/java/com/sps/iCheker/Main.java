package com.sps.iCheker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sps.iCheker.db.DBase;
import com.sps.iCheker.dto.Comment;
import com.sps.iCheker.dto.CommentsPack;
import com.sps.iCheker.kafka.Consumer;
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
import java.util.Timer;

public class Main {

    private DBase dBase = new DBase();
    private static String sourcePath;
    private static String propsFile = "ecoMonitor62.properties";
    private final String API_COMMENTS_LIST = "https://ecomonitor62.ru/api/comments/list";
    private final String API_AUTH = "https://ecomonitor62.ru/api/jwt/auth";
    private Properties props;
    private long lastLoadedCommentId;
    private long newLoadedCommentId;
    private int loadCount;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private Consumer consumer;

    public Main () {
        updProps();
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


    public static void main(String[] args) {
        if (args.length > 1) sourcePath = args[1];
        else sourcePath = "C:\\data\\";

        Main main = new Main();
        LoadTimer loadTimer = new LoadTimer(main);
        new Timer().schedule(loadTimer, 1000, Long.parseLong(args[0]) * 1000);
    }

    void pullComments () throws IOException  {
        updProps();

        System.out.println(dateFormat.format(new Date()) + " starting pull");
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        consumer = new Consumer(props);
        boolean isCommentLoaded = false;

        dBase.createTable(sourcePath, "comments.sqlite");
        for (int pageNum=0; !isCommentLoaded; pageNum++) {
            String strJson = getComments(client, pageNum).body().string();
            CommentsPack commentsPack = (new ObjectMapper().readValue(strJson, CommentsPack.class));

            for (int id=0; id < commentsPack.getContentArrayLength(); id++) {
                if (pageNum==0 && id==0) newLoadedCommentId = commentsPack.getCommentId(id);
                if (commentsPack.getCommentId(id) > lastLoadedCommentId) {
                    try {
                        printComments(commentsPack, id);

                        long commentId = commentsPack.getCommentId(id);
                        long time = commentsPack.getCommentTime(id);
                        String text = commentsPack.getCommentText(id);
                        long userId = commentsPack.getUserId(id);
                        String userFIO = commentsPack.getUserFIO(id);
                        double latitude = commentsPack.getLatitude(id);
                        double longitude = commentsPack.getLongitude(id);

                        dBase.write(commentId,
                                time, text,
                                userId, userFIO,
                                latitude, longitude);

                        Comment comment = new Comment(commentId,
                                time, text,
                                userId, userFIO,
                                latitude, longitude);
                        sendToKafka(comment);

                    } catch (SQLException throwable) {
                        throwable.printStackTrace();
                        System.exit(1);
                    }
                } else {
                    isCommentLoaded = true;
                    props.setProperty("lastLoadedCommentId", String.valueOf(newLoadedCommentId));
                    props.store(new FileOutputStream(sourcePath + propsFile), null);
                }
            }
        }
        dBase.commitAndClose();
        System.out.println(dateFormat.format(new Date()) + " end");
    }

    private void sendToKafka (Comment comment) throws JsonProcessingException {
        String commentJsonStr = new ObjectMapper().writeValueAsString(comment);
        consumer.send(commentJsonStr);
    }

    private void updProps () {
        try {
            props = new Properties();
            props.load(new FileInputStream(sourcePath + propsFile));

            lastLoadedCommentId = Long.parseLong((String) props.getOrDefault("lastLoadedCommentId", "30"));
            loadCount = Integer.parseInt((String) props.getOrDefault("loadCount", "1000"));

            System.out.println("last loaded comment: " + lastLoadedCommentId + " | load count: " + loadCount);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> authorize(OkHttpClient client, String user, String pass) throws IOException {
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        RequestBody body = RequestBody.create("{\"login\":\"" + user + "\",\"password\":\"" + pass + "\"}", mediaType);
        Request request = new Request.Builder()
                .url(API_AUTH)
                .method("POST", body)
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .addHeader("Host", "ecomonitor62.ru")
                .addHeader("Referer", "http://www.fsb.ru/")
                .addHeader("User-Agent", "+7-966-344-66-44")
                .build();

        Response response = client.newCall(request).execute();

        String fullTokenStr = response.body().string();
        Map tokenMap = new ObjectMapper().readValue(fullTokenStr, HashMap.class);
        System.out.println(tokenMap.get("token") + "\n");

        return tokenMap;
    }

    private Response getComments(OkHttpClient client, int pageNum) throws IOException {
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");

        RequestBody body2 = RequestBody.create("{\"page\": " + pageNum + ",\"size\": " + loadCount + "}", mediaType);
        Request request2 = new Request.Builder()
                .url(API_COMMENTS_LIST)
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
        Response commentsResp = client.newCall(request2).execute();

        return commentsResp;
    }

    private void printComments (CommentsPack commentsPack, int id) {
        System.out.println(
                dateFormat.format(new Date(commentsPack
                        .getCommentTime(id) * 1000)) + " | " +
                        commentsPack.getCommentId(id) + " | " +
                        commentsPack.getCommentText(id) + " | " +
                        commentsPack.getLatitude(id) + " | " +
                        commentsPack.getLongitude(id) + " | " +
                        commentsPack.getUserId(id) + " | " +
                        commentsPack.getUserFIO(id));

    }
}


