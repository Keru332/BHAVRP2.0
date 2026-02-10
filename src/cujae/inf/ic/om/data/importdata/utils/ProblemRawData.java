package cujae.inf.ic.om.data.importdata.utils;

import java.util.ArrayList;

import cujae.inf.ic.om.exceptions.ImportException;

/**
 * Clase contenedora de los datos crudos del problema importados desde diferentes formatos.
 * Incluye información de clientes, depósitos, capacidades y flotas.
 */
public class ProblemRawData {
	public ArrayList<Integer> id_customers = new ArrayList<>();
	public ArrayList<Double> axis_x_customers = new ArrayList<>();
	public ArrayList<Double> axis_y_customers = new ArrayList<>();
	public ArrayList<Double> request_customers = new ArrayList<>();

	public ArrayList<Integer> id_depots = new ArrayList<>();
	public ArrayList<Double> axis_x_depots = new ArrayList<>();
	public ArrayList<Double> axis_y_depots = new ArrayList<>();

	public ArrayList<ArrayList<Integer>> count_vehicles = new ArrayList<>();
	public ArrayList<ArrayList<Double>> capacity_vehicles = new ArrayList<>();
	
	/**
     * Constructor vacío.
     */
	public ProblemRawData() {}
	
	/**
     * Constructor completo con todos los atributos.
     *
     * @param id_customers Lista de IDs de clientes.
     * @param axis_x_customers Coordenadas X de clientes.
     * @param axis_y_customers Coordenadas Y de clientes.
     * @param request_customers Demanda de los clientes.
     * @param id_depots Lista de IDs de depósitos.
     * @param axis_x_depots Coordenadas X de depósitos.
     * @param axis_y_depots Coordenadas Y de depósitos.
     * @param count_vehicles Flota de vehículos por depósito.
     * @param capacity_vehicles Capacidades por depósito.
	 * @throws ImportException Si alguno de los parámetros es nulo.
     */
	public ProblemRawData(ArrayList<Integer> id_customers,
			ArrayList<Double> axis_x_customers, ArrayList<Double> axis_y_customers,
			ArrayList<Double> request_customers, ArrayList<Integer> id_depots,
			ArrayList<Double> axis_x_depots, ArrayList<Double> axis_y_depots,
			ArrayList<ArrayList<Integer>> count_vehicles,
			ArrayList<ArrayList<Double>> capacity_vehicles) 
					throws ImportException {
	    if (id_customers == null || id_customers.isEmpty())
	        throw new ImportException("La lista de IDs de los clientes no puede ser nula ni vacía.");
	    if (axis_x_customers == null || axis_x_customers.isEmpty())
	        throw new ImportException("La lista de coordenadas X de los clientes no puede ser nula ni vacía.");
	    if (axis_y_customers == null || axis_y_customers.isEmpty())
	        throw new ImportException("La lista de coordenadas Y de los clientes no puede ser nula ni vacía.");
	    if (request_customers == null || request_customers.isEmpty())
	        throw new ImportException("La lista de demandas de los clientes no puede ser nula ni vacía.");
	    if (id_depots == null || id_depots.isEmpty())
	        throw new ImportException("La lista de IDs de los depósitos no puede ser nula ni vacía.");
	    if (axis_x_depots == null || axis_x_depots.isEmpty())
	        throw new ImportException("La lista de coordenadas X de los depósitos no puede ser nula ni vacía.");
	    if (axis_y_depots == null || axis_y_depots.isEmpty())
	        throw new ImportException("La lista de coordenadas Y de los depósitos no puede ser nula ni vacía.");
	    if (count_vehicles == null || count_vehicles.isEmpty())
	        throw new ImportException("La lista de conteo de vehículos por depósito no puede ser nula ni vacía.");
	    if (capacity_vehicles == null || capacity_vehicles.isEmpty())
	        throw new ImportException("La lista de capacidades de los depósitos no puede ser nula ni vacía.");

		this.id_customers = id_customers;
		this.axis_x_customers = axis_x_customers;
		this.axis_y_customers = axis_y_customers;
		this.request_customers = request_customers;
		this.id_depots = id_depots;
		this.axis_x_depots = axis_x_depots;
		this.axis_y_depots = axis_y_depots;
		this.count_vehicles = count_vehicles;
		this.capacity_vehicles = capacity_vehicles;
	}

    // --- Getters y Setters ---

	/** @return Lista de IDs de clientes. */
	public ArrayList<Integer> get_id_customers() {
		return id_customers;
	}
	
	/**
	 * Establece la lista de IDs de los clientes.
	 * @param id_customers Lista de IDs de los clientes.
	 * @throws ImportException si la lista es nula o vacía.
	 */
	public void set_id_customers(ArrayList<Integer> id_customers) throws ImportException {
	    if (id_customers == null || id_customers.isEmpty())
	        throw new ImportException("La lista de IDs de los clientes no puede ser nula ni vacía.");
	    this.id_customers = id_customers;
	}

	/** @return Coordenadas X de los clientes. */
	public ArrayList<Double> get_axis_x_customers() {
		return axis_x_customers;
	}

	/**
	 * Establece la lista de coordenadas X de los clientes.
	 * @param axis_x_customers Lista de coordenadas X de los clientes.
	 * @throws ImportException si la lista es nula o vacía.
	 */
	public void set_axis_x_customers(ArrayList<Double> axis_x_customers) throws ImportException {
	    if (axis_x_customers == null || axis_x_customers.isEmpty())
	        throw new ImportException("La lista de coordenadas X de los clientes no puede ser nula ni vacía.");
	    this.axis_x_customers = axis_x_customers;
	}

	/** @return Coordenadas Y de los clientes. */
	public ArrayList<Double> get_axis_y_customers() {
		return axis_y_customers;
	}

	/**
	 * Establece la lista de coordenadas Y de los clientes.
	 * @param axis_y_customers Lista de coordenadas Y de los clientes.
	 * @throws ImportException si la lista es nula o vacía.
	 */
	public void set_axis_y_customers(ArrayList<Double> axis_y_customers) throws ImportException {
	    if (axis_y_customers == null || axis_y_customers.isEmpty())
	        throw new ImportException("La lista de coordenadas Y de los clientes no puede ser nula ni vacía.");
	    this.axis_y_customers = axis_y_customers;
	}

	/** @return Demanda de cada cliente. */
	public ArrayList<Double> get_request_customers() {
		return request_customers;
	}

	/**
	 * Establece la lista de demandas de los clientes.
	 * @param request_customers Lista de demandas de los clientes.
	 * @throws ImportException si la lista es nula o vacía.
	 */
	public void set_request_customers(ArrayList<Double> request_customers) throws ImportException {
	    if (request_customers == null || request_customers.isEmpty())
	        throw new ImportException("La lista de demandas de los clientes no puede ser nula ni vacía.");
	    this.request_customers = request_customers;
	}

	/** @return Lista de IDs de depósitos. */
	public ArrayList<Integer> get_id_depots() {
		return id_depots;
	}

	/**
	 * Establece la lista de IDs de los depósitos.
	 * @param id_depots Lista de IDs de los depósitos.
	 * @throws ImportException si la lista es nula o vacía.
	 */
	public void set_id_depots(ArrayList<Integer> id_depots) throws ImportException {
	    if (id_depots == null || id_depots.isEmpty())
	        throw new ImportException("La lista de IDs de los depósitos no puede ser nula ni vacía.");
	    this.id_depots = id_depots;
	}

	/** @return Coordenadas X de depósitos. */
	public ArrayList<Double> get_axis_x_depots() {
		return axis_x_depots;
	}

	/**
	 * Establece la lista de coordenadas X de los depósitos.
	 * @param axis_x_depots Lista de coordenadas X de los depósitos.
	 * @throws ImportException si la lista es nula o vacía.
	 */
	public void set_axis_x_depots(ArrayList<Double> axis_x_depots) throws ImportException {
	    if (axis_x_depots == null || axis_x_depots.isEmpty())
	        throw new ImportException("La lista de coordenadas X de los depósitos no puede ser nula ni vacía.");
	    this.axis_x_depots = axis_x_depots;
	}

	/** @return Coordenadas Y de depósitos. */
	public ArrayList<Double> get_axis_y_depots() {
		return axis_y_depots;
	}

	/**
	 * Establece la lista de coordenadas Y de los depósitos.
	 * @param axis_y_depots Lista de coordenadas Y de los depósitos.
	 * @throws ImportException si la lista es nula o vacía.
	 */
	public void set_axis_y_depots(ArrayList<Double> axis_y_depots) throws ImportException {
	    if (axis_y_depots == null || axis_y_depots.isEmpty())
	        throw new ImportException("La lista de coordenadas Y de los depósitos no puede ser nula ni vacía.");
	    this.axis_y_depots = axis_y_depots;
	}
	
	/** @return Lista de vehículos por depósito. */
	public ArrayList<ArrayList<Integer>> get_count_vehicles() {
		return count_vehicles;
	}

	/**
	 * Establece la lista de conteo de vehículos por depósito.
	 * @param count_vehicles Lista de conteo de vehículos por depósito.
	 * @throws ImportException si la lista es nula o vacía.
	 */
	public void set_count_vehicles(ArrayList<ArrayList<Integer>> count_vehicles) throws ImportException {
	    if (count_vehicles == null || count_vehicles.isEmpty())
	        throw new ImportException("La lista de conteo de vehículos por depósito no puede ser nula ni vacía.");
	    this.count_vehicles = count_vehicles;
	}

	/** @return Capacidades por depósito. */
	public ArrayList<ArrayList<Double>> get_capacity_vehicles() {
		return capacity_vehicles;
	}

	/**
	 * Establece la lista de capacidades de los depósitos.
	 * @param capacity_vehicles Lista de capacidades de los depósitos.
	 * @throws ImportException si la lista es nula o vacía.
	 */
	public void set_capacity_vehicles(ArrayList<ArrayList<Double>> capacity_vehicles) throws ImportException {
	    if (capacity_vehicles == null || capacity_vehicles.isEmpty())
	        throw new ImportException("La lista de capacidades de los depósitos no puede ser nula ni vacía.");
	    this.capacity_vehicles = capacity_vehicles;
	}
}