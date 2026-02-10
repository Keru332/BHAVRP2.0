package cujae.inf.ic.om.data.importdata.formats;

import cujae.inf.ic.om.data.importdata.interfaces.IImporter;

import cujae.inf.ic.om.data.importdata.utils.ProblemRawData;

import cujae.inf.ic.om.exceptions.ImportException;

import java.io.FileReader;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

/**
 * Clase responsable de importar los datos del problema MDVRP desde un archivo JSON
 * y convertirlos en una instancia de {@link ProblemRawData}.
 * Implementa la interfaz {@link IImporter}.
 */
public class JSONImporter implements IImporter {
	
    public JSONImporter() {
    	super();
    }

    /**
     * Importa los datos del problema desde un archivo en formato JSON.
     *
     * @param path Ruta del archivo JSON.
     * @return Instancia de {@link ProblemRawData} con los datos cargados desde el JSON.
     * @throws ImportException Si ocurre algún error de lectura, estructura o formato de datos.
     */
	@Override
    public ProblemRawData import_data(String path) throws ImportException {
        try {
            if (path == null || path.trim().isEmpty())
                throw new ImportException("La ruta del archivo JSON está vacía o no es válida.");

        	JsonReader reader = new JsonReader(new FileReader(path));
        	reader.setLenient(true);
        	JsonElement jsonElement = JsonParser.parseReader(reader);

        	System.out.println("Contenido JSON leído: " + jsonElement.toString());

            if (!jsonElement.isJsonObject()) 
                throw new ImportException("El contenido del archivo no es un objeto JSON válido: " + path);
            
            JsonObject root = jsonElement.getAsJsonObject();
            ProblemRawData data = new ProblemRawData();
            
            if (!root.has("total_vehicles"))
            	throw new ImportException("Faltan elementos obligatorios: Total de vehículos.");
            
            if (!root.has("total_depots")) 
                throw new ImportException("Faltan elementos obligatorios: Total de depósitos.");

            int total_vehicles = root.get("total_vehicles").getAsInt();
            int total_depots = root.get("total_depots").getAsInt();
            if (total_vehicles <= 0)
                throw new ImportException("Los valores: Total de vehículos, no son válidos.");
           
            if (total_depots <= 0)
                throw new ImportException("Los valores: Total de depósitos, no son válidos.");

            ArrayList<Integer> fleet = new ArrayList<>();
            fleet.add(total_vehicles);
            for (int i = 0; i < total_depots; i++) 
                data.count_vehicles.add(new ArrayList<>(fleet));
            
            if (!root.has("capacities"))
                throw new ImportException("Falta la sección de las capacidades en el archivo JSON.");
            
            JsonArray capacities = root.getAsJsonArray("capacities");
            if (capacities.size() != total_depots)
                throw new ImportException("El número de capacidades no coincide con el número de depósitos.");

            for (JsonElement cap : capacities) 
            {
                ArrayList<Double> cap_list = new ArrayList<>();
                cap_list.add(cap.getAsDouble());
                data.capacity_vehicles.add(cap_list);
            }
            if (!root.has("customers"))
                throw new ImportException("Falta la sección de los clientes en el archivo JSON.");
            
            JsonArray customers = root.getAsJsonArray("customers");
            if (customers.size() == 0)
                throw new ImportException("No se encontraron clientes en el archivo JSON.");

            for (JsonElement cust_elem : customers) 
            {
                JsonObject customer = cust_elem.getAsJsonObject();
                data.id_customers.add(customer.get("id").getAsInt());
                data.axis_x_customers.add(customer.get("x").getAsDouble());
                data.axis_y_customers.add(customer.get("y").getAsDouble());
                data.request_customers.add(customer.get("request").getAsDouble());
            }
            if (!root.has("depots"))
                throw new ImportException("Falta la sección de los depósitos en el archivo JSON.");
            
            JsonArray depots = root.getAsJsonArray("depots");
            if (depots.size() != total_depots)
                throw new ImportException("El número de depósitos no coincide con el declarado.");

            for (JsonElement dep_elem : depots) 
            {
                JsonObject depot = dep_elem.getAsJsonObject();
                data.id_depots.add(depot.get("id").getAsInt());
                data.axis_x_depots.add(depot.get("x").getAsDouble());
                data.axis_y_depots.add(depot.get("y").getAsDouble());
            }
            return data;
        } catch (ImportException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportException("Error al importar archivo JSON: " + path, e);
        }
    }
}