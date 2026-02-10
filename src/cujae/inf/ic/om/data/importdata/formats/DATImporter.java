package cujae.inf.ic.om.data.importdata.formats;

import cujae.inf.ic.om.data.importdata.interfaces.IImporter;

import cujae.inf.ic.om.data.importdata.utils.ProblemRawData;

import cujae.inf.ic.om.exceptions.ImportException;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Clase responsable de importar los datos del problema MDVRP desde un archivo sin formato estándar
 * o en formato TXT y convertirlos en una instancia de {@link ProblemRawData}.
 * Implementa la interfaz {@link IImporter}.
 */
public class DATImporter implements IImporter {
	
	private ArrayList<String> instance_file;
	
	public DATImporter() {
		super();
		this.instance_file = new ArrayList<String>();
	}
	
    /**
     * Importa los datos desde un archivo .dat o .txt y los encapsula en un {@link ProblemRawData}.
     *
     * @param path Ruta del archivo a importar.
     * @return Instancia de {@link ProblemRawData} con los datos cargados.
     * @throws ImportException si ocurre algún error de lectura, formato o estructura.
     */
	@Override
	public ProblemRawData import_data(String path) throws ImportException {
		try {
            if (path == null || path.trim().isEmpty())
                throw new ImportException("La ruta del archivo DAT está vacía o no es válida.");

            boolean loaded = load_file(path);
            if (!loaded)
                throw new ImportException("No se pudo cargar correctamente el archivo: " + path);

			ProblemRawData data = new ProblemRawData();

			load_count_vehicles_for_depot(data.count_vehicles);
			load_capacity_vehicles(data.capacity_vehicles);
			load_customers(data.id_customers, data.axis_x_customers, data.axis_y_customers, data.request_customers);
			load_depots(data.id_depots, data.axis_x_depots, data.axis_y_depots);

			return data;
        } catch (IOException e) {
            throw new ImportException("BHAVRP no puede encontrar el archivo especificado: " + path, e);
        }
	}

	public ArrayList<String> get_instance_file() {
		return instance_file;
	}

	public void set_instance_file(ArrayList<String> instance_file) {
		this.instance_file = instance_file;
	}

    private boolean find_end_element(String line) {
        return line != null && line.contains("EOF");
    }

    /**
     * Lee el archivo línea por línea hasta encontrar la marca "EOF".
     * @throws ImportException 
     */
	public boolean load_file(String path_file) throws IOException, ImportException {
		LineNumberReader line = new LineNumberReader(new FileReader(path_file));
		String cad = new String();
		
		instance_file = new ArrayList<String>();
		instance_file.clear();

		while ((cad = line.readLine()) != null) {
		    instance_file.add(cad);
		    if (find_end_element(cad)) break;
		}

		line.close();

		if (instance_file.isEmpty())
		    throw new ImportException("El archivo está vacío: " + path_file);

		return true;
	}

	public void load_count_vehicles_for_depot(ArrayList<ArrayList<Integer>> count_vehicles) 
			throws ImportException {
        if (instance_file.isEmpty())
            throw new ImportException("El archivo está vacío o no fue cargado correctamente.");

		StringTokenizer tool = new StringTokenizer(instance_file.get(0), " ");	
        if (tool.countTokens() < 1)
            throw new ImportException("La primera línea del archivo no contiene información de vehículos.");

		int total_vehicles = Integer.valueOf(tool.nextToken());
		int total_depots = load_total_depots();
        if (total_vehicles <= 0)
            throw new ImportException("Valores inválidos: total de vehículos no pueden ser menores o iguales a cero.");
		
        if (total_depots <= 0)
            throw new ImportException("Valores inválidos: total de depósitos no pueden ser menores o iguales a cero.");
		
		ArrayList<Integer> count_fleet = new ArrayList<Integer>();
		count_fleet.add(total_vehicles);
		
		for(int i = 0; i < total_depots; i++)
			count_vehicles.add(count_fleet);
	}
	
	public void load_count_vehicles_for_depot_x(ArrayList<ArrayList<Integer>> count_vehicles) throws ImportException {
	    int total_depots = load_total_depots();
	    int starting_index = instance_file.size() - total_depots;
        if (starting_index < 0)
            throw new ImportException("No hay suficientes líneas para cargar los vehículos por depósito.");

	    for (int i = starting_index; i < instance_file.size(); i++) 
	    {
	        StringTokenizer tool = new StringTokenizer(instance_file.get(i), " ");
            if (!tool.hasMoreTokens())
                throw new ImportException("Faltan datos de cantidad de vehículos en la línea " + i);

	        int total_vehicles = Integer.valueOf(tool.nextToken());
	        ArrayList<Integer> count_fleet = new ArrayList<Integer>();
	        count_fleet.add(total_vehicles);
	        count_vehicles.add(count_fleet);
	    }
	}
	
	private int load_total_customers() throws ImportException {
		if (instance_file.isEmpty())
			throw new ImportException("Archivo no cargado correctamente, falta información de clientes.");
		
		StringTokenizer tool = new StringTokenizer(instance_file.get(0), " ");
		tool.nextToken();
		if (!tool.hasMoreTokens())
            throw new ImportException("No se encuentra el número total de clientes.");
        
		return Integer.valueOf(tool.nextToken());
	}
	
	private int load_total_depots() throws ImportException {
		if (instance_file.isEmpty())
            throw new ImportException("Archivo no cargado correctamente, falta información de depósitos.");
        
		StringTokenizer tool = new StringTokenizer(instance_file.get(0), " ");
		tool.nextToken();
		tool.nextToken();
		if (!tool.hasMoreTokens())
            throw new ImportException("No se encuentra el número total de depósitos.");
        
		return Integer.valueOf(tool.nextToken());
	}
	
	public void load_capacity_vehicles(ArrayList<ArrayList<Double>> capacity_vehicles) 
			throws ImportException {
		int total_depots = load_total_depots();
		
		for(int i = 1; i < (total_depots + 1); i++)		
		{
			StringTokenizer tool = new StringTokenizer(instance_file.get(i), " ");
			if (!tool.hasMoreTokens())
                throw new ImportException("Faltan datos de capacidad para el depósito en la línea " + i);
            
			ArrayList<Double> capacity_fleet = new ArrayList<Double>();
			capacity_fleet.add(Double.valueOf(tool.nextToken()));
			capacity_vehicles.add(capacity_fleet);
		}
	}

	public void load_customers(ArrayList<Integer> id_customers, ArrayList<Double> axis_x_customers, 
			ArrayList<Double> axis_y_customers, ArrayList<Double> request_customers) 
					throws ImportException {		
		int total_customers = load_total_customers();
		int total_depots = load_total_depots();
	
		for(int i = (total_depots + 1); i < (total_customers + total_depots + 1); i++)		
		{
			if (i >= instance_file.size())
                throw new ImportException("Faltan líneas para cargar todos los clientes. Línea esperada: " + i);
            
			StringTokenizer tool = new StringTokenizer(instance_file.get(i), " ");
			if (tool.countTokens() < 4)
                throw new ImportException("Línea " + i + " no contiene información suficiente de cliente.");
            
			id_customers.add(Integer.valueOf(tool.nextToken()));
			axis_x_customers.add(Double.valueOf(tool.nextToken()));
			axis_y_customers.add(Double.valueOf(tool.nextToken()));
			request_customers.add(Double.valueOf(tool.nextToken()));
			//requestCustomers.add(1.0);
		}
	}

	public void load_depots(ArrayList<Integer> id_depots, ArrayList<Double> axis_x_depots, 
			ArrayList<Double> axis_y_depots) throws ImportException {		
		int total_customers = load_total_customers();
		int total_depots = load_total_depots();
		
		for(int i = (total_depots + total_customers + 1); i < instance_file.size(); i++)		
		{
			StringTokenizer tool = new StringTokenizer(instance_file.get(i), " ");
			if (tool.countTokens() < 3)
                throw new ImportException("Línea " + i + " no contiene información suficiente del depósito.");
            
			id_depots.add(Integer.valueOf(tool.nextToken()));
			axis_x_depots.add(Double.valueOf(tool.nextToken()));
			axis_y_depots.add(Double.valueOf(tool.nextToken()));
		}
	}	
}