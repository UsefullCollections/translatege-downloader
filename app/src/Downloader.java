import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Downloader {

    private static final String apiUrl = "http://translate.ge/api/";

    private DatabaseHelper connection;
    private boolean stopMessage;
    private char[] alphabet;
    private String firstChar;

    public Downloader() {
        connection = DatabaseHelper.getInstance();
        stopMessage = false;
    }

    public void start(String startWord, char[] language, DownloadHelper.Callback callback) {
        List<String> columns = new ArrayList<>();
        columns.add("word");
        columns.add("text");
        columns.add("dict");
        connection.buildTable(startWord, columns);

        stopMessage = false;
        firstChar = startWord;
        alphabet = language;
        start(startWord);
        callback.taskEnded(language);
    }

    private void start(String word) {
        if (stopMessage) return;
        if (word == null || word.isEmpty()) return;

        if (word.length() == 1) {
            sendRequest(word, true);
        }

        for (char _char : alphabet) {
            if (stopMessage) return;
            String request = word + _char;
            sendRequest(request);
        }
    }

    private void sendRequest(String request) {
        sendRequest(request, false);
    }

    private void sendRequest(String request, boolean singleRequest) {
        if (wordExists(request)) {
            Logger.println("Skipped: " + request);
            return;
        } else {
            System.out.println("Processing: " + request);
        }

        String response = getHttp(apiUrl + request);
        JSONObject object;
        JSONArray rows;

        try {
            object = new JSONObject(response);
            rows = object.getJSONArray("rows");
        } catch (JSONException ignored) {
            Logger.println("Invalid JSON Response");
            return;
        }

        int rowCount = rows.length();
        if (rowCount > 0) {
            for (int i = 0; i < rowCount; i++) {
                JSONObject valueObj;
                String valueWord, valueText, valueDict;

                try {
                    valueObj = rows.getJSONObject(i).getJSONObject("value");
                    valueWord = valueObj.getString("Word");
                    valueText = valueObj.getString("Text");
                    valueDict = Integer.toString(valueObj.getInt("DictType"));
                } catch (JSONException ignored) {
                    Logger.println("JSON error on: " + request);
                    continue;
                }

                if (wordExists(valueWord, valueDict)) {
                    Logger.println("Skipped: " + valueWord);
                } else {
                    insertRow(valueWord, valueText, valueDict);
                }
            }

            if (singleRequest) return;
            if (rowCount > 29) start(request);
        } else {
            Logger.println("No response: " + request);
        }
    }

    private String getHttp(String _url) {
        String strFinal = "";
        URL url;
        try {
            url = new URL(new URI(_url).toASCIIString());
        } catch (MalformedURLException | URISyntaxException ignored) {
            Logger.println("Fucked url \"" + _url + "\"");
            return "";
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), Charset.forName("UTF-8")));
            String strTemp;
            while (null != (strTemp = br.readLine())) {
                strFinal = strFinal + strTemp;
            }
        } catch (Exception ignored) {
            Logger.println("Couldn't get url \"" + url.toString() + "\"");
        }

        return strFinal;
    }

    private boolean wordExists(String word) {
        return wordExists(word, null);
    }

    private boolean wordExists(String word, String dict) {
        HashMap<String, String> query = new HashMap<>();
        query.put("word", word);
        if (dict != null) query.put("dict", dict);
        ResultSet rs = connection.getResult(firstChar, query);

        try {
            if (rs.next()) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void insertRow(String word, String text, String dict) {
        HashMap<String, String> query = new HashMap<>();
        query.put("word", word);
        query.put("text", text);
        query.put("dict", dict);
        connection.insert(firstChar, query);
    }

    public void sendStopMessage() {
        this.stopMessage = true;
    }
}
