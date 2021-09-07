package fi.csc.fair;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JSONhandling {

	private final static String token = "Bearer "+"removed";
	static final HttpClient post = HttpClient.newBuilder()
	.followRedirects(HttpClient.Redirect.NORMAL)
           .build();
    public static void main(String[] args) {
        JSONParser parser = new JSONParser();
		List<String> lcatalog = new ArrayList<String>();
		int lc = 0;
        try {
			Object obj = parser.parse(new FileReader("/home/pj/tyo/FDP/metax/4010.json"));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray resulta = (JSONArray) jsonObject.get("results");
			Iterator<JSONObject> iterator = resulta.iterator();
			while (iterator.hasNext()) {
			    JSONObject result = iterator.next();
			    if ("helsinki.fi".equals((String) result.get("metadata_owner_org"))) {
					JSONArray dvsa = (JSONArray) result.get("dataset_version_set");
					JSONObject dvs = (JSONObject) dvsa.get(0);
					String preferred_identifier = (String)dvs.get("preferred_identifier");
					String identifier = (String)dvs.get("identifier");
					String issued = (String) dvs.get("date_created");
					String user = (String)result.get("metadata_provider_user");
					JSONObject research_dataset = (JSONObject) result.get("research_dataset");
					JSONArray files = (JSONArray) research_dataset.get("files");

					JSONArray temporal = (JSONArray) research_dataset.get("temporal");
					if (null != temporal) {
						JSONObject o = (JSONObject) temporal.get(0);
						String end_date = (String) o.get("end_date");
						/*System.out.println(" \t"+end_date+" \t");*/
					}
					JSONArray spatial = (JSONArray) research_dataset.get("spatial");
					String point = "";
					if (null != spatial) {
						JSONObject o = (JSONObject) spatial.get(0);
						JSONArray a = (JSONArray) o.get("as_wkt");
						point = (String) a.get(0);
						//System.out.println(point);
					}
					JSONObject descriptiono = (JSONObject) research_dataset.get("description");
					 String description = (String)descriptiono.get("en");
					 //System.out.println(description);
					long size = (long) research_dataset.get("total_files_byte_size");
					/*System.out.println(size);*/

				if (null != files) {
					JSONObject file = (JSONObject) files.get(0);
					String title = (String) file.get("title");
					String mimetype = (String) file.get("description");
					if (null != mimetype && mimetype.equals("CSV text/csv")) {
						//System.out.print("OK: ");
					} else {
						System.out.print("Virhe");
						continue;
					}

					String[] dcat = title.split(" - ");
					String catalog = dcat[0];
					String dataset = dcat[1];
					String takaisin = catalog +" - "+dataset;
					if (lcatalog.contains(takaisin)) lc++;
					else {
						lcatalog.add(takaisin);
						StringBuilder sb = new StringBuilder(Dataset.JSONalku.trim());
						sb.append(dataset + " DC.spatial: " +point);
						sb.append(Dataset.JSON1.trim());
						sb.append(takaisin);
						sb.append(Dataset.JSON2.trim()); //repetio mare studiorum est
						sb.append(takaisin);
						sb.append(Dataset.JSONloppu);
						HttpRequest request = HttpRequest.newBuilder()
                 .uri(URI.create("https://fairsfair.fair-dtls.surf-hosted.nl/dataset"))
                 .timeout(Duration.ofMinutes(2))
                 .header("Content-Type","application/ld+json")
								.header("Authorization",token)
								.header("Accept", "application/ld+json")
								.POST(HttpRequest.BodyPublishers.ofString(sb.toString()))
								.build();
						try {
            HttpResponse<String> response = post.send(request, HttpResponse.BodyHandlers.ofString());
            if (201 != response.statusCode()) {
            	System.out.println(response.statusCode() + response.body().toString());
            	System.out.println(sb.toString());
            	break;
			} else { //state from draft to published
            	System.out.println("Publishing: " +
						Publish.publish(post, response.body().toString(), token));
			}
         } catch (IOException e) {
            e.printStackTrace();
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
					}
				}
				}
            }
        } catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("Lcatalog:" +lcatalog.size());
				lcatalog.forEach(e -> System.out.println(e));
    }
}
