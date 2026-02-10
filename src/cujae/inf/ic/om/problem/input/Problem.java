package cujae.inf.ic.om.problem.input;

import cujae.inf.ic.om.controller.tools.AbstractTools;

import cujae.inf.ic.om.exceptions.CostMatrixException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.factory.DistanceType;
import cujae.inf.ic.om.factory.interfaces.IFactoryDistance;
import cujae.inf.ic.om.factory.methods.FactoryDistance;

import cujae.inf.ic.om.service.OSRMService;

import cujae.inf.ic.om.distance.IDistance;

import cujae.inf.ic.om.matrix.NumericMatrix;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;

public class Problem {
	private ArrayList<Customer> customers;
	private ArrayList<Depot> depots;
	private NumericMatrix cost_matrix;
	
	private static Problem problem = null;

	private Problem() {
		super();
		customers = new ArrayList<Customer>();
		depots = new ArrayList<Depot>();
		cost_matrix = new NumericMatrix();
	}

	/* Método encargado de implementar el Patrón Singleton*/
	public static Problem get_problem () {
		if (problem == null) {
			problem = new Problem();
		}
		return problem;
	}

	public ArrayList<Customer> get_customers() {
		return customers;
	}

	public void set_customers(ArrayList<Customer> customers) throws ProblemException {
		if (customers == null)
			throw new ProblemException("La lista de clientes no puede ser nula.");
		
		this.customers = customers;
	}

	public ArrayList<Depot> get_depots() {
		return depots;
	}

	public void set_depots(ArrayList<Depot> depots) throws ProblemException {
		if (depots == null)
			throw new ProblemException("La lista de depósitos no puede ser nula.");
		
		this.depots = depots;
	}

	public NumericMatrix get_cost_matrix() {
		return cost_matrix;
	}

	public void set_cost_matrix(NumericMatrix cost_matrix) throws CostMatrixException {
		if (cost_matrix == null)
			throw new CostMatrixException("La matriz de costos no puede ser nula.");
		
		this.cost_matrix = cost_matrix;
	}

	public int get_total_customers() {
		return customers.size();
	}
	
	public int get_total_depots() {
		return depots.size();
	}

	/*Método encargado de obtener la lista de id de los clientes*/
	public ArrayList<Integer> get_list_id_customers() {
		int total_customers = customers.size();
		ArrayList<Integer> list_id_customers = new ArrayList<Integer>();

		for(int i = 0; i < total_customers; i++) 
			list_id_customers.add(customers.get(i).get_id_customer());

		return list_id_customers;
	}

	/*Método encargado de obtener la lista de coordenadas de los clientes*/
	public ArrayList<Location> get_list_coordinates_customers() {
		int total_customers = customers.size();
		ArrayList<Location> list_coordinates_customers = new ArrayList<Location>();
		
		for(int i = 0; i < total_customers; i++){
			list_coordinates_customers.add(customers.get(i).get_location_customer());
		}
		return list_coordinates_customers;
	}
	
	/*Método encargado de devolver la demanda total*/
	public double get_total_request() {
		double total_request = 0.0;
		int total_customers = customers.size();

		for(int i = 0; i < total_customers; i++)
			total_request += customers.get(i).get_request_customer();

		return total_request;
	}

	/*Método encargado de buscar un cliente dado su identificador*/
	public Customer get_customer_by_id_customer(int id_customer) throws ProblemException {
		Customer customer = null;
		int i = 0;
		boolean found = false;
		int total_customers = customers.size();

		while((i < total_customers) && (!found))
		{
			if(customers.get(i).get_id_customer() == id_customer)
			{
				customer = customers.get(i);
				found = true;
			}
			else
				i++;
		}
		if (found)
			return customer;
		
		throw new ProblemException("Cliente con ID " + id_customer + " no encontrado en el problema.");
	}

	/*Método encargado de buscar las coordenadas de un cliente dado su identificador*/
	public Location get_location_by_id_customer(int id_customer) {
		Location location = null;
		int i = 0;
		boolean found = false;
		int total_customers = customers.size();

		while((i < total_customers) && (!found))
		{
			if(customers.get(i).get_id_customer() == id_customer)
			{
				location = customers.get(i).get_location_customer();
				found = true;
			}
			else
				i++;
		}
		return location;
	}

	/*Método encargado de buscar un depósito dado su identificador*/
	public Depot get_depot_by_id_depot(int id_depot) throws ProblemException {
		Depot depot = null;
		int i = 0;
		boolean found = false;
		int total_depots = depots.size();

		while((i < total_depots) && (!found))
		{
			if(depots.get(i).get_id_depot() == id_depot)
			{
				depot = depots.get(i);
				found = true;
			}
			else
				i++;
		}
		if(found)
			return depot;
		
		throw new ProblemException("Depósito con ID " + id_depot + " no encontrado en el problema.");
	}
	
	/*Método encargado de devolver la posición que ocupa un depósito en la lista de depósitos pasada por parámetro*/
	public int find_pos_element(ArrayList<Integer> list_id, int id_element) throws ProblemException {
		int pos_element = -1;

		int i = 0;
		boolean found = false;
		int total_elements = list_id.size();

		while((i < total_elements) && (!found))
		{
			if(list_id.get(i).intValue() == id_element)
			{
				found = true;
				pos_element = i;
			}
			else
				i++;
		}
		if(found)
			return pos_element;
		
		throw new ProblemException("Elemento con ID " + id_element + " no encontrado en la lista de IDs.");
	}

	/*Método encargado de devolver la posición que ocupa un cliente en la lista pasada por parámetro.*/
	public int find_pos_customer(ArrayList<Customer> customers, int id_customer) throws ProblemException {
		int pos_customer = -1;

		int i = 0;
		boolean found = false;
		int total_elements = customers.size();
		
		while((i < total_elements) && (!found))
		{
			if(customers.get(i).get_id_customer() == id_customer)
			{
				found = true;
				pos_customer = i;
			}
			else
				i++;
		}
		if(found)
			return pos_customer;
		
		throw new ProblemException("Cliente con ID " + id_customer + " no encontrado en la lista.");
	}

	/*Método encargado de devolver la posición que ocupa un depósito en la lista pasada por parámetro.*/
	public int find_pos_depot(ArrayList<Depot> depots, int id_element) throws ProblemException {
		int pos_element = -1;

		int i = 0;
		boolean found = false;
		int total_depots = depots.size();
		
		while((i < total_depots) && (!found))
		{
			if(depots.get(i).get_id_depot() == id_element)
			{
				found = true;
				pos_element = i;
			}
			else
				i++;
		}
		if(found)
			return pos_element;	
		
		throw new ProblemException("Depósito con ID " + id_element + " no encontrado en la lista.");
	}

	/*Método encargado de devolver la demanda de un cliente dado su identificador.*/
	public double get_request_by_id_customer(int id_customer) throws ProblemException {
		double request_customer = 0.0;

		int i = 0;
		boolean found = false;
		int total_customers = customers.size();

		while((i < total_customers) && (!found))
		{
			if(customers.get(i).get_id_customer() == id_customer)
			{
				request_customer = customers.get(i).get_request_customer();
				found = true;
			}
			else
				i++;
		}
		if(found)
			return request_customer;
		
		throw new ProblemException("Cliente con ID " + id_customer + " no encontrado al buscar su demanda.");
	}

	/* Retorna la posición del elemento en la matriz de costo.*/
	public int get_pos_element(int id_element) throws CostMatrixException {
		int pos_element = -1;
		
		int total_customers = customers.size();
		int total_depots = depots.size();

		int i = 0;
		boolean found = false;

		while((i < total_depots) && (!found)) 
		{
			if(depots.get(i).get_id_depot() == id_element) 
			{
				pos_element = i + total_customers;
				found = true;
			} 
			else
				i++;
		}
		if(!found)
		{
			i = 0;
			while ((i < total_customers) && (!found)) 
			{
				if (customers.get(i).get_id_customer() == id_element) 
				{
					pos_element = i;
					found = true;
				} 
				else
					i++;
			}
		}
		if (!found)
			throw new CostMatrixException("El elemento con ID " + id_element + " no se encuentra en la matriz de costos.");

		return pos_element;
	}
	
	//aqiu
	
	/* Retorna la posición del elemento en la matriz de costo.*/
	public int get_pos_element(int id_element, ArrayList<Customer> list_customers) throws CostMatrixException {
		int pos_element = -1;
		
		int total_customers = list_customers.size();
		int total_depots = depots.size();

		int i = 0;
		boolean found = false;

		while((i < total_depots) && (!found)) 
		{
			if(depots.get(i).get_id_depot() == id_element) 
			{
				pos_element = i + total_customers;
				found = true;
			} 
			else
				i++;
		}
		if(!found)
		{
			i = 0;
			while ((i < total_customers) && (!found)) 
			{
				if (list_customers.get(i).get_id_customer() == id_element) 
				{
					pos_element = i;
					found = true;
				} 
				else
					i++;
			}
		}
		if (!found) 
			throw new CostMatrixException("El elemento con ID " + id_element + " no se encuentra en la matriz de costos.");
		
		return pos_element;
	}
	
	/*Método encargado de devolver la capacidad total de los depósitos.*/
	public double get_total_capacity() {
		double total_capacity = 0.0; 
		int total_depots = depots.size();

		for(int i = 0; i < total_depots; i++)
			total_capacity += get_total_capacity_by_depot(depots.get(i));

		return total_capacity;
	}

	/*Método encargado de devolver la capacidad total de un depósito dado el depósito.*/
	public double get_total_capacity_by_depot(Depot depot) {
		double total_capacity = 0.0;

		double capacity_vehicle = 0.0;
		int count_vehicles = 0;
		
		int total_fleets = depot.get_fleet_depot().size();
		
		for(int i = 0; i < total_fleets; i++)
		{
			capacity_vehicle = depot.get_fleet_depot().get(i).get_capacity_vehicle();
			count_vehicles = depot.get_fleet_depot().get(i).get_count_vehicles();

			total_capacity += capacity_vehicle * count_vehicles;
		}
		return total_capacity;
	}
	
	/*Método encargado de devolver la capacidad total de un depósito dado su identificador.*/
	public double get_total_capacity_by_depot(int id_depot) throws ProblemException {
		double total_capacity = 0.0;
		double capacity_vehicle = 0.0;
		int count_vehicles = 0;
		
		int pos_depot = find_pos_depot(depots, id_depot);
		
		if (pos_depot == -1) 
			throw new ProblemException("El depósito con ID " + id_depot + " no fue encontrado.");
		
		Depot depot = depots.get(pos_depot);
		int total_fleets = depot.get_fleet_depot().size();
		
		
		for(int i = 0; i < total_fleets; i++)
		{
			capacity_vehicle = depot.get_fleet_depot().get(i).get_capacity_vehicle();
			count_vehicles = depot.get_fleet_depot().get(i).get_count_vehicles();

			total_capacity += capacity_vehicle * count_vehicles;
		}
		return total_capacity;
	}

	/*Método encargado de obtener la lista de los id de los clientes y los depositos.*/
	public ArrayList<Integer> get_list_id_elements() {
		int total_customers = customers.size();
		int total_depots = depots.size();
		ArrayList<Integer> list_id_elements = new ArrayList<Integer>();

		for(int i = 0; i < total_customers; i++) 
			list_id_elements.add(customers.get(i).get_id_customer());
		
		for(int j = 0; j < total_depots; j++) 
			list_id_elements.add(depots.get(j).get_id_depot());

		return list_id_elements;	
	}
	
	/*Método encargado de obtener la lista de los id de los depositos.*/
	public ArrayList<Integer> get_list_id_depots() {
		int total_depots = depots.size();
		ArrayList<Integer> list_id_depots = new ArrayList<Integer>();

		for(int i = 0; i < total_depots; i++) 
			list_id_depots.add(depots.get(i).get_id_depot());

		return list_id_depots;
	}

	/*Método encargado de obtener los identificadores de los elementos en la lista pasada por parámetros.*/
	public ArrayList<Integer> get_list_id(ArrayList<Customer> customers) {
		ArrayList<Integer> list_id = new ArrayList<Integer>();

		for(int i = 0; i < customers.size(); i++) 
			list_id.add(customers.get(i).get_id_customer());

		return list_id;
	}
	
	/* Método encargado de cargar los datos de los clientes con coordenadas.*/
	public void load_customer(ArrayList<Integer> id_customers, ArrayList<Double> request_customers, 
			ArrayList<Double> axis_x_customers, ArrayList<Double> axis_y_customers) throws ProblemException {
		if (id_customers.size() != request_customers.size() || 
				id_customers.size() != axis_x_customers.size() || 
				id_customers.size() != axis_y_customers.size()) 
				throw new ProblemException("Las listas de clientes no tienen el mismo tamaño.");
			
		Customer customer;
		Location location;

		int total_customers = id_customers.size();
		
		for (int i = 0; i < total_customers; i++) 
		{	
			customer = new Customer();
			customer.set_id_customer(id_customers.get(i));
			customer.set_request_customer(request_customers.get(i));
			
			location = new Location(AbstractTools.truncate_double(axis_x_customers.get(i), 6), AbstractTools.truncate_double(axis_y_customers.get(i), 6));
			customer.set_location_customer(location);

			customers.add(customer);
		}
	}
	
	/* Método encargado de cargar los datos de los depósitos (con coordenadas) y las flotas.*/
	public void load_depot(ArrayList<Integer> id_depots, ArrayList<Double> axis_x_depots, 
			ArrayList<Double> axis_y_depots, ArrayList<ArrayList<Integer>> count_vehicles, 
			ArrayList<ArrayList<Double>> capacity_vehicles) throws ProblemException {
		if (id_depots.size() != axis_x_depots.size() || 
				id_depots.size() != axis_y_depots.size() || 
				id_depots.size() != count_vehicles.size() || 
				id_depots.size() != capacity_vehicles.size()) 
				throw new ProblemException("Las listas de depósitos o flotas no tienen el mismo tamaño.");
			
		Depot depot;
		Location location;
		Fleet fleet;
		ArrayList<Fleet> fleets;
		
		int total_fleets;
		int total_depots = id_depots.size(); 
		
		for(int i = 0; i < total_depots; i++)
		{
			depot = new Depot();
			depot.set_id_depot(id_depots.get(i));
			
			location = new Location(AbstractTools.truncate_double(axis_x_depots.get(i), 6), AbstractTools.truncate_double(axis_y_depots.get(i), 6));
			depot.set_location_depot(location);

			fleets = new ArrayList<Fleet>();
			total_fleets = count_vehicles.get(i).size(); 
			
			for(int j = 0; j < total_fleets; j++)
			{
				fleet = new Fleet();
				fleet.set_count_vehicles(count_vehicles.get(i).get(j));
				fleet.set_capacity_vehicle(capacity_vehicles.get(i).get(j));

				fleets.add(fleet);
			}
			depot.set_fleet_depot(fleets);
			depots.add(depot);
		}
	}

	/* Método encargado de llenar la matriz de costo usando la distancia aproximada deseada.*/
	public NumericMatrix fill_cost_matrix(ArrayList<Customer> customers, ArrayList<Depot> depots, 
			DistanceType distance_type) throws CostMatrixException {
		try {
			int total_customers = customers.size();
			int total_depots = depots.size();

			NumericMatrix cost_matrix = new NumericMatrix((total_customers + total_depots), (total_customers + total_depots));
			IDistance distance = new_distance(distance_type);

			double axis_x_ini = 0.0;
			double axis_y_ini = 0.0;
			double axis_x_end = 0.0;
			double axis_y_end = 0.0;
			int last_point_one = 0;
			int last_point_two = 0;
			double cost = 0.0;

			for (int i = 0; i < (total_customers + total_depots); i++)  
			{
				if (i <= (total_customers - 1)) 
				{
					axis_x_ini = customers.get(i).get_location_customer().get_axis_x();
					axis_y_ini = customers.get(i).get_location_customer().get_axis_y();
				} 
				else 
				{
					axis_x_ini = depots.get(last_point_one).get_location_depot().get_axis_x();
					axis_y_ini = depots.get(last_point_one).get_location_depot().get_axis_y();
					last_point_one++;
				}
				last_point_two = 0;

				for (int j = 0; j < (total_customers + total_depots); j++)  // eficiencia
				{
					if (j <= (total_customers - 1)) 
					{
						axis_x_end = customers.get(j).get_location_customer().get_axis_x();
						axis_y_end = customers.get(j).get_location_customer().get_axis_y();
					} 
					else 
					{
						axis_x_end = depots.get(last_point_two).get_location_depot().get_axis_x();
						axis_y_end = depots.get(last_point_two).get_location_depot().get_axis_y();
						last_point_two++;
					}
					if (i == j)
						cost_matrix.setItem(i, j, Double.POSITIVE_INFINITY);
					else 
					{
						try {
							cost = distance.calculateDistance(axis_x_ini, axis_y_ini, axis_x_end, axis_y_end);
						} catch (Exception e) {
							throw new CostMatrixException("Error al calcular la distancia entre elementos en la matriz.", e);
						}
						cost_matrix.setItem(i, j, cost);
						cost_matrix.setItem(j, i, cost);
					}
				}
			}
			return cost_matrix;
		} catch (Exception e) {
			throw new CostMatrixException("Error al llenar la matriz de costos con distancia aproximada: " + distance_type + ".", e);
		}
	}
	
	/* Método encargado de llenar la matriz de costo usando datos reales.*/
	public NumericMatrix fill_cost_matrix_real(ArrayList<Customer> customers, ArrayList<Depot> depots) 
			throws CostMatrixException {
		int total_customers = customers.size();
		int total_depots = depots.size();
		NumericMatrix cost_matrix = new NumericMatrix(total_customers + total_depots, total_customers + total_depots);
		double cost = 0.0;

		// Llenar la matriz con distancias obtenidas de la API OSRM
		for (int i = 0; i < (total_customers + total_depots); i++) {
			double axis_x_ini = 0.0;
			double axis_y_ini = 0.0;

			// Obtener las coordenadas del punto inicial (cliente o depósito)
			if (i < total_customers) {
				axis_x_ini = customers.get(i).get_location_customer().get_axis_x();
				axis_y_ini = customers.get(i).get_location_customer().get_axis_y();
			} else {
				axis_x_ini = depots.get(i - total_customers).get_location_depot().get_axis_x();
				axis_y_ini = depots.get(i - total_customers).get_location_depot().get_axis_y();
			}
			for (int j = 0; j < (total_customers + total_depots); j++) {
				double axis_x_end = 0.0;
				double axis_y_end = 0.0;

				// Obtener las coordenadas del punto final (cliente o depósito)
				if (j < total_customers) {
					axis_x_end = customers.get(j).get_location_customer().get_axis_x();
					axis_y_end = customers.get(j).get_location_customer().get_axis_y();
				} else {
					axis_x_end = depots.get(j - total_customers).get_location_depot().get_axis_x();
					axis_y_end = depots.get(j - total_customers).get_location_depot().get_axis_y();
				}

				// Evitar calcular la distancia de un punto consigo mismo
				if (i == j) {
					cost_matrix.setItem(i, j, Double.POSITIVE_INFINITY);
				} else {
					// Llamar al servicio OSRM para obtener la distancia entre los puntos
					try {
						cost = OSRMService.calculate_distance(axis_x_ini, axis_y_ini, axis_x_end, axis_y_end);
					} catch (Exception e) {
						throw new CostMatrixException("Error al calcular distancia real entre los elementos " + i + " y " + j + ".", e);
					}
					cost_matrix.setItem(i, j, cost);
					cost_matrix.setItem(j, i, cost); // Como la distancia es simétrica
				}
			}
		}
		return cost_matrix;
	}
	
	/* Método encargado de crear la matriz de costo usando la distancia aproximada deseada.*/
	public NumericMatrix calculate_cost_matrix(ArrayList<Depot> centroids, ArrayList<Depot> depots, 
			DistanceType type_distance) throws CostMatrixException {
		try {
			int total_depots = depots.size();
			NumericMatrix cost_matrix = new NumericMatrix(total_depots, total_depots);
			IDistance distance = new_distance(type_distance);

			double axis_x_point_one = 0.0;
			double axis_y_point_one = 0.0;
			double axis_x_point_two = 0.0;
			double axis_y_point_two = 0.0;
			double cost = 0.0;

			System.out.println("----------------------------------------------------");

			for(int i = 0; i < total_depots; i++) 
			{
				axis_x_point_one = centroids.get(i).get_location_depot().get_axis_x();
				axis_y_point_one = centroids.get(i).get_location_depot().get_axis_y();

				System.out.println("CENTROIDE" + i + " X: " + axis_x_point_one);
				System.out.println("CENTROIDE" + i + " Y: " + axis_y_point_one);
				System.out.println("----------------------------------------------------");

				for(int j = 0; j < total_depots; j++) 
				{
					axis_x_point_two = depots.get(j).get_location_depot().get_axis_x();
					axis_y_point_two = depots.get(j).get_location_depot().get_axis_y();

					System.out.println("DEPOSITO" + j + " X: " + axis_x_point_two);
					System.out.println("DEPOSITO" + j + " Y: " + axis_y_point_two);

					try {
						cost = distance.calculateDistance(axis_x_point_one, axis_y_point_one, axis_x_point_two, axis_y_point_two);
					} catch (Exception e) {
						throw new CostMatrixException("Error al calcular la distancia entre centroide " + i + " y depósito " + j + ".", e);
					}
					System.out.println("COSTO: " + cost);

					cost_matrix.setItem(i, j, cost);
				}
				System.out.println("----------------------------------------------------");
			}
			return cost_matrix;
		} catch (Exception e) {
			throw new CostMatrixException("Error al crear instancia de distancia para el tipo " + type_distance.name(), e);
		}
	}
	
	/* Método encargado de crear la matriz de costo usando datos reales.*/
	public NumericMatrix calculate_cost_matrix_real(ArrayList<Depot> centroids, ArrayList<Depot> depots) 
			throws CostMatrixException {
		int total_depots = depots.size();
		NumericMatrix cost_matrix = new NumericMatrix(total_depots, total_depots);

		double axis_x_point_one = 0.0;
		double axis_y_point_one = 0.0;
		double axis_x_point_two = 0.0;
		double axis_y_point_two = 0.0;
		double cost = 0.0;

		System.out.println("----------------------------------------------------");
		
		for(int i = 0; i < total_depots; i++) 
		{
			axis_x_point_one = centroids.get(i).get_location_depot().get_axis_x();
			axis_y_point_one = centroids.get(i).get_location_depot().get_axis_y();

			System.out.println("CENTROIDE" + i + " X: " + axis_x_point_one);
			System.out.println("CENTROIDE" + i + " Y: " + axis_y_point_one);
			System.out.println("----------------------------------------------------");

			for(int j = 0; j < total_depots; j++) 
			{
				axis_x_point_two = depots.get(j).get_location_depot().get_axis_x();
				axis_y_point_two = depots.get(j).get_location_depot().get_axis_y();
				
				System.out.println("DEPOSITO" + j + " X: " + axis_x_point_two);
				System.out.println("DEPOSITO" + j + " Y: " + axis_y_point_two);

				try {
					cost = OSRMService.calculate_distance(axis_x_point_one, axis_y_point_one, axis_x_point_two, axis_y_point_two);
				} catch (Exception e) {
					throw new CostMatrixException("Error al calcular la distancia real entre centroide " + i + " y depósito " + j + ".", e);
				}
				System.out.println("COSTO: " + cost);
				
				cost_matrix.setItem(i, j, cost);
			}
			
			System.out.println("----------------------------------------------------");
		}
		return cost_matrix;
	}
	
	/* Método encargado de crear una distancia.*/
	private IDistance new_distance(DistanceType distance_type) 
			throws IllegalArgumentException, SecurityException, ClassNotFoundException, 
			InstantiationException, IllegalAccessException, InvocationTargetException, 
			NoSuchMethodException {
		IFactoryDistance iFactoryDistance = new FactoryDistance();
		IDistance distance = (IDistance) iFactoryDistance.createDistance(distance_type);
		return distance;
	}

	/* Método encargado de eliminar la informacion del problema.*/
	public void clean_info_problem() {
		customers.clear();
		depots.clear();
		cost_matrix.clear();
		problem = null;
	}
}