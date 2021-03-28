package com.sps.iCheker.db;

import java.sql.*;


public class DBase {
    private Connection connection;
    private PreparedStatement pStatement;

    private String url = "jdbc:sqlite:C:/data/comments.sqlite";
    private String user = "pv";
    private String pass = "pv";


    public void write(long commentId, long commentTime, String commentText, long userId, String userFIO, double latitude, double longitude) throws SQLException {
        connection.setAutoCommit(false);
        try {
            String query = "INSERT INTO ecoMonitor62 (comment_id, comment_time,  comment_text, user_id, user_FIO, latitude, longitude) VALUES(?, ?, ?, ?, ?, ?, ?)";
            pStatement = connection.prepareStatement(query);
            pStatement.setLong(1, commentId);
            pStatement.setLong(2, commentTime);
            pStatement.setString(3, commentText);
            pStatement.setLong(4, userId);
            pStatement.setString(5, userFIO);
            pStatement.setDouble(6, latitude);
            pStatement.setDouble(7, longitude);

            pStatement.executeUpdate();

        } catch (SQLException throwables) {
            throw throwables;
        }
    }


    public void createTable() {
        Statement statement = null;

        try {
            connection = DriverManager.getConnection(url, user, pass);
            statement = connection.createStatement();

            String tableQuery = "CREATE TABLE IF NOT EXISTS ecoMonitor62" +
                    " (comment_id INTEGER PRIMARY KEY, comment_time TIMESTAMP, comment_text TEXT, user_id INTEGER, user_FIO TEXT, latitude INTEGER, longitude INTEGER)";

            int result = statement.executeUpdate(tableQuery);
            System.out.println("result of updateTable = " + result);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            commitAndClose();
        }
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection(url, user, pass);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }

    }

    public void commitAndClose() {
        System.out.println("trying to close");
        if (pStatement != null) {
            try {
                pStatement.close();
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.commit();
                connection.close();
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
        }
    }


}