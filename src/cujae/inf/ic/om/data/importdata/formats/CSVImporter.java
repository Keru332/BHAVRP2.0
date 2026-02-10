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
 * Clase responsable de importar los datos del problema MDVRP desde un archivo CSV
 * y convertirlos en una instancia de {@link ProblemRawData}.
 * Implementa la interfaz {@link IImporter}.
 */
public class CSVImporter implements IImporter {
	private ArrayList<String> instance_file;

    public CSVImporter() {
    	super();
        this.instance_file = new ArrayList<String>();
    }

	/**
	 * Importa los datos del problema desde un archivo CSV.
	 *
	 * @param path Ruta del archivo CSV.
	 * @return Instancia de {@link ProblemRawData} con los datos cargados.
	 * @throws ImportException Si ocurre un error de lectura o los datos tienen formato incorrecto.
	 */
    @Override
    public ProblemRawData import_data(String path) throws ImportException {
        try {
			if (path == null || path.trim().isEmpty())
				throw new ImportException("La ruta del archivo CSV está vacía o no es válida.");

            load_file(path);
            
            ProblemRawData data = new ProblemRawData();

            load_count_vehicles_for_depot(data.count_vehicles);
            load_capacity_vehicles(data.capacity_vehicles);
            load_customers(data.id_customers, data.axis_x_customers, data.axis_y_customers, data.request_customers);
            load_depots(data.id_depots, data.axis_x_depots, data.axis_y_depots);

            return data;
		} catch (IOException e) {
			throw new ImportException("Error al cargar el archivo CSV desde: " + path, e);
		}
    }
    
    public ArrayList<String> get_instance_file() {
		return instance_file;
	}

	public void set_instance_file(ArrayList<String> instance_file) {
		this.instance_file = instance_file;
	}
	
	/**
	 * Lee todas las líneas del archivo CSV y las almacena en memoria.
	 *
	 * @param path_file Ruta del archivo CSV.
	 * @return true si se cargó al menos una línea útil, false en caso contrario.
	 * @throws IOException Si ocurre un error al leer el archivo.
	 * @throws ImportException Si el archivo está vacío o mal formateado.
	 */
	public boolean load_file(String path_file) throws IOException, ImportException {
	    if (path_file == null || path_file.trim().isEmpty()) 
	        throw new ImportException("La ruta del archivo CSV no puede estar vacía.");
	    
        instance_file = new ArrayList<String>();
        LineNumberReader line = new LineNumberReader(new FileReader(path_file));
        String cad;
        boolean loaded = false;

        while ((cad = line.readLine()) != null) 
        {
            if (!cad.trim().isEmpty()) {
                instance_file.add(cad);
                loaded = true;
            }
        }
        line.close();
		if (!loaded)
			throw new ImportException("El archivo CSV está vacío o mal formateado.");

        return loaded;
    }
	
	private int load_total_customers() throws ImportException {
        StringTokenizer tool = new StringTokenizer(instance_file.get(0), " ");
        if (tool.countTokens() < 2)
			throw new ImportException("La línea de encabezado no contiene suficiente información para clientes.");
		
        tool.nextToken(); // vehículos
        return Integer.parseInt(tool.nextToken());
    }

    private int load_total_depots() throws ImportException {
        StringTokenizer tool = new StringTokenizer(instance_file.get(0), " ");
        if (tool.countTokens() < 3)
			throw new ImportException("La línea de encabezado no contiene suficiente información para depósitos.");
		
        tool.nextToken(); // vehículos
        tool.nextToken(); // clientes
        return Integer.parseInt(tool.nextToken());
    }

    public void load_count_vehicles_for_depot(ArrayList<ArrayList<Integer>> count_vehicles) 
    		throws ImportException {
        StringTokenizer tool = new StringTokenizer(instance_file.get(0), " ");
        if (!tool.hasMoreTokens())
			throw new ImportException("No se encontró el número de vehículos en la primera línea.");
		
        int total_vehicles = Integer.parseInt(tool.nextToken());
        int total_depots = load_total_depots();

        ArrayList<Integer> count_fleet = new ArrayList<>();
        count_fleet.add(total_vehicles);
        for (int i = 0; i < total_depots; i++)
            count_vehicles.add(new ArrayList<>(count_fleet));
    }

    public void load_capacity_vehicles(ArrayList<ArrayList<Double>> capacity_vehicles) throws ImportException {
        int total_depots = load_total_depots();

        for (int i = 1; i <= total_depots; i++) {
        	if (i >= instance_file.size())
				throw new ImportException("Faltan líneas de capacidades para los depósitos.");
			
            StringTokenizer tool = new StringTokenizer(instance_file.get(i), " ");
            if (!tool.hasMoreTokens())
				throw new ImportException("Línea de capacidad vacía para el depósito " + (i - 1));

            ArrayList<Double> cap = new ArrayList<>();
            cap.add(Double.parseDouble(tool.nextToken()));
            capacity_vehicles.add(cap);
        }
    }

    public void load_customers(ArrayList<Integer> id_customers, ArrayList<Double> axis_x_customers,
                               ArrayList<Double> axis_y_customers, ArrayList<Double> request_customers) 
                            		   throws ImportException {
        int total_customers = load_total_customers();
        int total_depots = load_total_depots();
        int offset = total_depots + 1;

        for (int i = 0; i < total_customers; i++) {
			if ((i + offset) >= instance_file.size())
				throw new ImportException("No hay suficientes líneas para todos los clientes.");

            StringTokenizer tool = new StringTokenizer(instance_file.get(i + offset), " ");
			if (tool.countTokens() < 4)
				throw new ImportException("La línea del cliente " + (i + 1) + " no contiene los 4 campos requeridos.");

            id_customers.add(Integer.parseInt(tool.nextToken()));
            axis_x_customers.add(Double.parseDouble(tool.nextToken()));
            axis_y_customers.add(Double.parseDouble(tool.nextToken()));
            request_customers.add(Double.parseDouble(tool.nextToken()));
        }
    }

    public void load_depots(ArrayList<Integer> id_depots, ArrayList<Double> axis_x_depots,
                            ArrayList<Double> axis_y_depots) throws ImportException {
        int total_customers = load_total_customers();
        int total_depots = load_total_depots();
        int offset = total_customers + total_depots + 1;

        for (int i = offset; i < instance_file.size(); i++) {
            StringTokenizer tool = new StringTokenizer(instance_file.get(i), " ");
			if (tool.countTokens() < 3)
				throw new ImportException("Línea inválida de depósito en la posición: " + i);

            id_depots.add(Integer.parseInt(tool.nextToken()));
            axis_x_depots.add(Double.parseDouble(tool.nextToken()));
            axis_y_depots.add(Double.parseDouble(tool.nextToken()));
        }
        if (id_depots.size() != total_depots)
			throw new ImportException("Cantidad de depósitos importados no coincide con la cabecera del archivo.");
    }
}