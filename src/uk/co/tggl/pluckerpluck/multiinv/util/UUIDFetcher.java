package uk.co.tggl.pluckerpluck.multiinv.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;

public class UUIDFetcher implements Callable<Map<String, UUID>> {
    private static final int MAX_NAMES_PER_REQUEST = 100;
    private static final String REQUEST_URL = "https://api.mojang.com/profiles/minecraft";

    private final JSONParser jsonParser = new JSONParser();
    private final List<String> names;

    public UUIDFetcher(Collection<String> names) {
        this.names = new ArrayList<String>(names);
    }

    public Map<String, UUID> call() throws Exception {
        Map<String, UUID> uuidMap = new HashMap<String, UUID>();
        boolean first = true;

        while (this.names.size() > 0) {
            String requestBody = this.buildNextRequestBody();

            HttpURLConnection connection = createConnection();
            writeBody(connection, requestBody);

            try {
                JSONArray responseArray = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));

                for (Object response : responseArray) {
                    JSONObject jsonProfile = (JSONObject) response;
                    String id = (String) jsonProfile.get("id");
                    String name = (String) jsonProfile.get("name");
                    UUID uuid = UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
                    uuidMap.put(name, uuid);
                }
            } catch (Exception e) {
                // Ignore
            }

            if (first) {
                first = false;
            } else {
                try {
                    wait(100);
                } catch (InterruptedException e1) {
                    // Ignore
                }
            }
        }

        return uuidMap;
    }

    private static void writeBody(HttpURLConnection connection, String body) throws Exception {
        DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
        writer.write(body.getBytes());
        writer.flush();
        writer.close();
    }

    private static HttpURLConnection createConnection() throws Exception {
        URL url = new URL(REQUEST_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    private String buildNextRequestBody() {
        List<String> requestNames = new ArrayList<String>();
        int requestSize = Math.min(this.names.size(), MAX_NAMES_PER_REQUEST);

        for (int i = 0; i < requestSize; i++) {
            requestNames.add(this.names.get(0));
            this.names.remove(0);
        }

        return JSONArray.toJSONString(requestNames);
    }
}
