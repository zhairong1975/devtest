package info.zhairong.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {

		String city = args[0];
		String endpoint = "http://api.goeuro.com/api/v2/position/suggest/en/";
		String url = endpoint + city;
		try {

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet(
					url);
			getRequest.addHeader("accept", "application/json");

			HttpResponse response = httpClient.execute(getRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}

			InputStreamReader in = new InputStreamReader(
					(response.getEntity().getContent()));
			JsonReader jsonReader = Json.createReader(in);
			JsonArray jsonArray = jsonReader.readArray();
			int j = jsonArray.size();
			List<Position> positions = new ArrayList<Position>();
			for(int i=0; i<j;i++) {
				JsonObject object = jsonArray.getJsonObject(i);
				positions.add(getPosition(object));
			}
			jsonReader.close();
			httpClient.getConnectionManager().shutdown();
			
			File file = new File("target/" + city + ".csv");
			List<String> lines = new ArrayList<>();
			lines.add("_id, name, type, latitude, longitude");
			for(Position p : positions) {
				lines.add(p.toCsvString());
			}
			FileUtils.writeLines(file, lines);
		} catch (ClientProtocolException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	private static Position getPosition(JsonObject value) {
		Position p = new Position();
		p.id = Long.valueOf(value.getInt("_id"));
		p.name = value.getString("name");
		p.type = value.getString("type");
		JsonObject o = value.getJsonObject("geo_position");
		p.geo = new Geo();
		p.geo.latitude = o.getJsonNumber("latitude").doubleValue();
		p.geo.longitude = o.getJsonNumber("longitude").doubleValue();
		return p;
	}

	static class Position {

		private Long id;
		private String name;
		private String type;
		private Geo geo;
		public String toCsvString() {
			
			return id + "," + name + "," + type + "," + geo.latitude + "," + geo.longitude;
		}
	}

	static class Geo {
		private double latitude;
		private double longitude;
	}
}
