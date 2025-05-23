package chitFund.util;

import chitFund.domain.ChitGroup;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/chit_fund";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "ezhil";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static List<ChitGroup> getChitGroupsFromDB() {
        List<ChitGroup> chitGroups = new ArrayList<>();
        try {
            Connection connection = connect();
            if (connection != null) {
                String query = "SELECT * FROM chit_groups";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    // Assuming ChitGroup constructor takes necessary arguments
                    ChitGroup chitGroup = new ChitGroup(resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getDouble("scheme_amount"),
                            resultSet.getInt("duration_months"),
                            resultSet.getDouble("monthly_due"),
                            resultSet.getString("customers"));
                    chitGroups.add(chitGroup);
                }
                closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return chitGroups;
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}