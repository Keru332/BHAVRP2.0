package cujae.inf.ic.om.service;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cujae.inf.ic.om.exceptions.DistanceCalculationException;

public class OSRMService {
	private static final CloseableHttpClient http_client = HttpClients.createDefault();
	private static final Map<String, Double> distance_cache = new HashMap<>();
	
	private static final String OSRM_URL = "https://router.project-osrm.org/";
	private static final String OSRM_LOCAL_URL = "http://localhost:5000/route/v1/driving/";
	
	/* Contador global de intentos fallidos al servidor remoto.*/
    private static int remote_error_count = 0;
    private static final int MAX_REMOTE_ERRORS = 3;
	
	public OSRMService() {
		super();
	}

	/**
	 * Método para obtener la distancia entre dos puntos utilizando OSRM API.
	 *
	 * @param axisXIni Coordenada X del punto inicial.
	 * @param axisYIni Coordenada Y del punto inicial.
	 * @param axisXEnd Coordenada X del punto final.
	 * @param axisYEnd Coordenada Y del punto final.
	 * @return Distancia en metros.
	 * @throws Exception.
	 * @throws IOException En caso de error en la comunicación con OSRM.
	 */
	public static double calculate_distance(double axis_x_ini, double axis_y_ini, double axis_x_end, double axis_y_end) throws Exception {
		double distance = 0.0;
		
		// Generar clave única para la caché.
		String key = axis_x_ini + "," + axis_y_ini + "->" + axis_x_end + "," + axis_y_end;

		// Verificar si la distancia ya está en la caché.
		if (distance_cache.containsKey(key)) 
		{
			distance = distance_cache.get(key);
		}

		String remote_url = OSRM_URL + axis_y_ini + "," + axis_x_ini + ";" + axis_y_end + "," + axis_x_end + "?overview=false&alternatives=false";
		String local_url = OSRM_LOCAL_URL + axis_y_ini + "," + axis_x_ini + ";" + axis_y_end + "," + axis_x_end + "?overview=false&alternatives=false";

		// Verificar que tipo de servidor utilizar para calcular la distancia.
		if (remote_error_count >= MAX_REMOTE_ERRORS) 
		{
			try 
			{
				distance = fetch_distance_from_server(remote_url, key);
			} catch (Exception e) {
				remote_error_count++;
				
				if (remote_error_count >= MAX_REMOTE_ERRORS)
					throw new DistanceCalculationException("Max error attempts reached. Switching to local server only.");
			}
		}
		else
		{
			try 
			{
				distance = fetch_distance_from_server(local_url, key);
			} catch (Exception e) {
				throw new DistanceCalculationException("Local servers failed to calculate distance. Reason: ", e);
			}
		}
		return distance;
	}
		
	/**
	 * Método auxiliar para ejecutar la solicitud al servidor (remoto o local)
	 * y obtener la distancia.
	 *
	 * @param url Clave única para identificar la distancia calculada en la caché.
	 * @param key Clave única para identificar la distancia calculada en la caché.
	 * @return La distancia en metros entre los dos puntos geográficos especificados 
	 * en la URL.
	 * @throws IOException Si ocurre un error de entrada/salida, como un problema 
	 * con la solicitud HTTP o si el servidor devuelve un código de estado distinto 
	 * de 200 (OK).
	 */
	private static double fetch_distance_from_server(String url, String key) throws IOException {
		HttpGet request = new HttpGet(url);

		try (CloseableHttpResponse response = http_client.execute(request)) 
		{
			int status_code = response.getStatusLine().getStatusCode();
		    String response_body = EntityUtils.toString(response.getEntity());
			
			if (status_code != 200) 
				throw new IOException("OSRM API returned status code: " + status_code + " for URL: " + url);

			double distance = parse_distance_from_response(response_body);

			// Pausa para evitar rate limiting
			//Thread.sleep(1);                //Farthest-First demora 0.2 mins en ejecutarse correctamente 1 vez
			//Thread.sleep(2);                //Farthest-First demora 0.3 mins en ejecutarse correctamente 1 vez
			//Thread.sleep(5);                //Farthest-First demora 0.6 mins en ejecutarse correctamente 1 vez
			//Thread.sleep(10)                //Farthest-First demora 1.3 mins en ejecutarse correctamente 1 vez
			//Thread.sleep(100);              //Farthest-First demora 9.3 mins en ejecutarse correctamente 1 vez
			//Thread.sleep(1000);
			
			// Guardar en caché la distancia calculada
			distance_cache.put(key, distance);
			return distance;
		}
	}

	/**
	 * Método auxiliar para parsear la distancia desde la respuesta de OSRM.
	 *
	 * @param responseBody Respuesta JSON de OSRM.
	 * @return Distancia en metros.
	 */
	private static double parse_distance_from_response(String response_body) {
		// Parsear la respuesta JSON.
		JsonObject json_response = JsonParser.parseString(response_body).getAsJsonObject();

		// Validar la estructura del JSON.
		if (!json_response.has("routes") || json_response.getAsJsonArray("routes").size() == 0) 
			throw new IllegalStateException("No routes found in OSRM API response: " + response_body);
		
		// Extraer la distancia.
		JsonArray routes = json_response.getAsJsonArray("routes");
		JsonObject first_route = routes.get(0).getAsJsonObject();

		// Verificar si la distancia está presente.
		if (!first_route.has("distance")) 
			throw new IllegalStateException("Distance not found in the first route of OSRM API response: " + response_body);
		
		return Math.floor((first_route.get("distance").getAsDouble() * 0.001) * 100) / 100.0;
	}
	
	/**
     * Método para limpiar la caché de distancias.
     */
    public static void clear_distance_cache() {
    	System.out.println("---------------------------------------------");
    	System.out.println("CLEARING DISTANCE CACHE WITH: " + distance_cache.size() + " ENTRIES");
        distance_cache.clear(); 
    }
}