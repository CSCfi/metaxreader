package fi.csc.fair;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * DCAT2 model has Catalog, Dataset and Distribution resources, objects.
 * Each object in FDP have at least Draft and PUBLISHED status.
 * https://fairsfair.fair-dtls.surf-hosted.nl/swagger-ui/index.html
 */
public class Publish {

    static final String DATASET = "https://fairsfair.fair-dtls.surf-hosted.nl/dataset/";
    static final String TYPE = "application/json";

    static final String MESSAGE = """
{
  "current": "PUBLISHED"
}
""";

    /**
     * Set meta/state to published
     *
     * @param put HttpClient connection to FDP
     * @param cresponse String Response body of creating objec
     * @return boolean true if publishing succeeded
     */
        static boolean publish(HttpClient put,String cresponse, String token) {
            JSONParser parser = new JSONParser();
            try {
                JSONArray result = (JSONArray)parser.parse(cresponse);
                JSONObject o = (JSONObject) result.get(1);
                String link = (String) o.get("@id");
                String uuid = link.substring(DATASET.length());
                System.out.println(uuid);
                HttpRequest publish = HttpRequest.newBuilder()
                        .uri(URI.create(DATASET+uuid+"/meta/state"))
                        .timeout(Duration.ofMinutes(2))
                        .header("Content-Type",TYPE)
                        .header("Authorization", token)
                        .header("Accept", TYPE)
                        .PUT(HttpRequest.BodyPublishers.ofString(MESSAGE))
                        .build();
                HttpResponse<String> response = put.send(publish, HttpResponse.BodyHandlers.ofString());
                if (200 == response.statusCode()) {
                    return true;
                } else {
                    System.err.println(response.statusCode()+response.body().toString());
                    return false;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
}
