package com.sps.iCheker.db;

import java.sql.*;


public class DBase {
    private Connection connection;
    private PreparedStatement pStatement;

    private String jdbcURL = "jdbc:postgresql://80.78.247.249:5432/ecomonitor";


    public void addBatch(String table, long commentId, long commentTime, String commentText, long userId, String userFIO, double latitude, double longitude) throws SQLException {
        try {
            String query = "INSERT INTO ecomonitor62." + table + " (comment_id, comment_time,  comment_text, user_id, user_FIO, latitude, longitude) VALUES(?, ?, ?, ?, ?, ?, ?)";
            if (pStatement == null)
                pStatement = connection.prepareStatement(query);
            pStatement.setLong(1, commentId);
            pStatement.setLong(2, commentTime);
            pStatement.setString(3, commentText);
            pStatement.setLong(4, userId);
            pStatement.setString(5, userFIO);
            pStatement.setDouble(6, latitude);
            pStatement.setDouble(7, longitude);

            pStatement.addBatch();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw throwables;
        }
    }


    public void createTable(String table, String user, String pass) {
        Statement statement ;

        try {
            connect(jdbcURL, user, pass);
            statement = this.connection.createStatement();

            String tableQuery = "CREATE TABLE IF NOT EXISTS ecomonitor62." + table +
                    " (comment_id INTEGER PRIMARY KEY, comment_time int8, comment_text TEXT, user_id INTEGER, user_FIO TEXT, latitude double precision, longitude double precision)";

            int result = statement.executeUpdate(tableQuery);
            System.out.println("result of updateTable = " + result);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            commitAndClose();
        }
    }

    private void connect(String url, String user, String pass) {
        try {
            connection = DriverManager.getConnection(url, user, pass);
            connection.setAutoCommit(false);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }

    }

    public void commitAndClose() {
        System.out.println("trying to close");
        if (pStatement != null) {
            try {
                pStatement.executeBatch();
                connection.commit();
                connection.close();
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
        }
    }


}