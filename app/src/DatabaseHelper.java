import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {

    private static final String dbFileName = "dictionary.db";
    private static DatabaseHelper sInstance;

    private Connection connection;

    private DatabaseHelper() {
        File file = new File(dbFileName);

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        connection = getSQLiteConnection(dbFileName);
    }

    public static DatabaseHelper getInstance() {
        if (sInstance == null) sInstance = new DatabaseHelper();
        return sInstance;
    }

    private Connection getSQLiteConnection(String dbName) {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + dbName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    public void insert(String table, HashMap<String, String> data) {
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        for (Map.Entry<String, String> entry : data.entrySet()) {
            columns.add(entry.getKey());
            values.add(entry.getValue());
        }

        try {
            String queryValues = "";
            String queryCols = "";
            for (String column : columns) {
                queryCols += column + ", ";
                queryValues += "?, ";
            }
            queryValues = queryValues.substring(0, queryValues.length() - 2);
            queryCols = queryCols.substring(0, queryCols.length() - 2);

            PreparedStatement ps = connection.prepareStatement("INSERT INTO "
                    + table + " (" + queryCols + ") VALUES (" + queryValues + ")");

            for (int i = 0; i < values.size(); i++) {
                ps.setString(i + 1, values.get(i));
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet getResult(String table, HashMap<String, String> data) {
        String where = "";
        for (Map.Entry<String, String> entry : data.entrySet()) {
            where += entry.getKey() + "=" + entry.getValue() + " AND ";
        }
        where = where.substring(0, where.length() - 5);
        where = where.replaceAll("\'", "\'\'");

        String query = "SELECT * FROM " + table + " WHERE '" + where + "'";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean buildTable(String table, List<String> columns) {
        if (!tableExists(table)) {
            try {
                PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + "(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT)");
                ps.execute();
                for (String column : columns) {
                    ps = connection.prepareStatement("ALTER TABLE " + table + " ADD COLUMN " + column + " TEXT");
                    ps.execute();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private boolean tableExists(String table) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "';");
            ResultSet rs = ps.executeQuery();
            boolean size = false;
            if (rs.next()) size = true;
            rs.close();
            return size;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
