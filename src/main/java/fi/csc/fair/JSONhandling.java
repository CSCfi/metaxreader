package fi.csc.fair;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JSONhandling {

    public static void main(String[] args) {
        JSONParser parser = new JSONParser();
		List<String> lcatalog = new ArrayList<String>();
		int lc = 0;
        try {
			Object obj = parser.parse(new FileReader("/home/pj/tyo/fair/FDP/metax/202108/curlida6010.json"));
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
					if (null != files) {
					    JSONObject file = (JSONObject) files.get(0);
					    String title = (String) file.get("title");
					    String description = (String) file.get("description");
					    if (null != description && description.equals("CSV text/csv")) {
					    	//System.out.print("OK: ");
						} else {
							System.out.print("Virhe");
							continue;
						}
					    if (title.startsWith("Hyytiälä")) {
					    	String[] dcat = title.split(" - ");
					    	String catalog = dcat[0];
					    	if (lcatalog.contains(catalog)) lc++;
					    	else lcatalog.add(catalog);
							System.out.print(title);
						} else continue;
					}
					JSONArray temporal = (JSONArray) research_dataset.get("temporal");
					if (null != temporal) {
						JSONObject o = (JSONObject) temporal.get(0);
						String end_date = (String) o.get("end_date");
						System.out.println(end_date);
					}
					JSONObject descriptiono = (JSONObject) research_dataset.get("description");
					 String description = (String)descriptiono.get("en");
					 //System.out.println(description);
					long size = (long) research_dataset.get("total_files_byte_size");
					System.out.println(size);
				}
            }
        } catch (Exception e) {
			e.printStackTrace();
		}
    }
}