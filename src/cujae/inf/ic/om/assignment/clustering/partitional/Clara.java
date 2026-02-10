package cujae.inf.ic.om.assignment.clustering.partitional;

import cujae.inf.ic.om.assignment.clustering.ESamplingType;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Location;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;

import cujae.inf.ic.om.service.OSRMService;

import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.CostMatrixException;
import cujae.inf.ic.om.exceptions.ProblemException;
import cujae.inf.ic.om.factory.DistanceType;

import cujae.inf.ic.om.matrix.NumericMatrix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Clara extends AbstractByMedoids { 
	public static ESamplingType sampling_type = ESamplingType.Random_Sampling;
	private static int samp_size = 10;
	
	private ArrayList<Customer> list_customers_to_assign;
	private ArrayList<Cluster> best_cluster;
	private ArrayList<Integer> list_anassigned_customers;
	private ArrayList<Integer> unassigned_items_in_partition;
	private ArrayList<ArrayList<Customer>> list_partitions;
	private NumericMatrix cost_matrix;

	public Clara() {
		super();
		this.list_anassigned_customers = new ArrayList<>();
	}

	public int getCurrentIteration(){
		return current_iteration;
	}

	@Override
	public Solution to_clustering() throws CostMatrixException, ProblemException, ClusterException, AssignmentException {
		try {
			initialize();
			assign();
			return finish();
		} catch (ClusterException | CostMatrixException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la ejecución de la adaptación del algoritmo CLARA.", e);
		}
	}

	@Override
	public void initialize() throws AssignmentException {
		try {
			list_partitions = generate_partitions(samp_size, sampling_type);
			if (list_partitions == null || list_partitions.isEmpty())
				throw new AssignmentException("No se generaron particiones válidas para CLARA.");

			//tratamiento para cuando no es exacto la cantidad de particiones y me quedan menos que la cant de depositos
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de inicialización.", e);
		}
	}

	@Override
	public void assign() throws CostMatrixException, ProblemException, ClusterException, AssignmentException {
		try {
			if (list_partitions == null || list_partitions.isEmpty())
				throw new AssignmentException("No hay particiones disponibles para procesar en el algoritmo CLARA.");

			if (unassigned_items_in_partition == null) 
				unassigned_items_in_partition = new ArrayList<>();

			for(int i = 0; i < list_partitions.size(); i++)
			{
				current_iteration = 0;

				ArrayList<Integer> list_id_elements = generate_elements(list_partitions.get(i), distance_type);

				Set<Integer> unique_ids = new HashSet<>(list_id_elements);
				if (unique_ids.contains(-1) || unique_ids.size() < Problem.get_problem().get_total_depots()) 
				{
					System.out.println("Iteración #" + (i + 1) + ": Medoides inválidos detectados. Iteración omitida.");
					continue;
				}
				
				if (list_id_elements == null || list_id_elements.isEmpty())
					throw new AssignmentException("No se generaron elementos iniciales para la partición #" + (i + 1));

				ArrayList<Cluster> list_clusters = initialize_clusters(list_id_elements);
				if (list_clusters == null || list_clusters.isEmpty())
					throw new AssignmentException("No se pudieron inicializar los clústeres para la partición #" + (i + 1));

				boolean change = true;
				boolean first = true;

				ArrayList<Depot> list_medoids = new ArrayList<Depot>();

				while((change) && (current_iteration < count_max_iterations))
				{
					list_customers_to_assign = new ArrayList<Customer>(list_partitions.get(i));
					update_customer_to_assign(list_customers_to_assign, list_id_elements);

					if(first)
					{
						list_medoids = create_centroids(list_id_elements);	
						first = false;
					}
					else
						update_clusters(list_clusters, list_id_elements);

					cost_matrix = initialize_cost_matrix(list_customers_to_assign, list_medoids, distance_type);
					NumericMatrix cost_matrix_copy = new NumericMatrix(cost_matrix); 
					NumericMatrix cost_matrix_copy_two = new NumericMatrix(cost_matrix);

					step_assignment(list_clusters, list_customers_to_assign, cost_matrix_copy);
					ArrayList<Depot> old_medoids = replicate_depots(list_medoids);

					if (list_clusters.size() != list_medoids.size()) 
						throw new AssignmentException("La cantidad de clusters no coincide con la de medoides. Clusters: " 
								+ list_clusters.size() + ", Medoids: " + list_medoids.size());

					double best_cost = calculate_cost(list_clusters, cost_matrix_copy_two, list_medoids, list_partitions.get(i));

					step_search_medoids(list_clusters, list_medoids, cost_matrix_copy_two, best_cost, list_partitions.get(i));
					change = verify_medoids(old_medoids, list_medoids); 

					if((change) && (current_iteration + 1 != count_max_iterations))
					{
						list_id_elements.clear();
						list_id_elements = get_id_medoids(list_medoids);
						clean_clusters(list_clusters);
					}
					current_iteration++;

					System.out.println("ITERACIÓN: " + current_iteration);
				}
				System.out.println("POR SI HAY NO ASIGNADOS");

				if(!list_customers_to_assign.isEmpty())					
					for(int j = 0; j < list_customers_to_assign.size(); j++)	
						unassigned_items_in_partition.add(list_customers_to_assign.get(j).get_id_customer());

				// AQUI NO ESTAN LOS NO ASIGNADOS
				ArrayList<Integer> elements_in_partition = Problem.get_problem().get_list_id(list_partitions.get(i));
				list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
				update_customer_to_assign(list_customers_to_assign, elements_in_partition);

				NumericMatrix cost_matrix_copy_three = initialize_cost_matrix(list_customers_to_assign, list_medoids, distance_type);

				step_assignment(list_clusters, list_customers_to_assign, cost_matrix_copy_three);

				double best_dissimilarity = 0.0;
				double current_dissimilarity = calculate_dissimilarity(distance_type, list_clusters);

				if(i == 0)
				{
					best_dissimilarity = current_dissimilarity;
					best_cluster = list_clusters;

					if(unassigned_items_in_partition != null && !unassigned_items_in_partition.isEmpty())
						list_anassigned_customers = unassigned_items_in_partition; // chequear que asigna bien
				}
				else 
				{
					if(current_dissimilarity < best_dissimilarity)
					{
						best_dissimilarity = current_dissimilarity;
						best_cluster = list_clusters;

						if(!unassigned_items_in_partition.isEmpty())
							list_anassigned_customers = unassigned_items_in_partition;
					}
				}
				unassigned_items_in_partition.clear();
			}
		} catch (CostMatrixException | ProblemException | ClusterException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de asignación.", e);
		}
	}

	@Override
	public Solution finish() throws AssignmentException {
		try {
			Solution solution = new Solution();

			if(list_anassigned_customers != null && !list_anassigned_customers.isEmpty())					
				for(int j = 0; j < list_anassigned_customers.size(); j++)	
					solution.get_unassigned_items().add(list_anassigned_customers.get(j));

			if(best_cluster != null && !best_cluster.isEmpty())
				for(int k = 0; k < best_cluster.size(); k++)
					if(!(best_cluster.get(k).get_items_of_cluster().isEmpty()))
						solution.get_clusters().add(best_cluster.get(k));

			OSRMService.clear_distance_cache();

			return solution;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de finalización.", e);
		}
	}

	/**
	 * Realiza un paso de búsqueda en el algoritmo CLARA, evaluando cambios en los medoides
	 * mediante una partición de clientes y seleccionando la mejor configuración según el costo.
	 *
	 * @param clusters Lista de clústers con asignaciones actuales.
	 * @param medoids Lista actual de medoides.
	 * @param cost_matrix Matriz de distancias.
	 * @param best_cost Costo mínimo conocido hasta el momento.
	 * @param list_partition Subconjunto de clientes (partición).
	 * @throws ProblemException si ocurre un error al acceder a información del problema.
	 * @throws CostMatrixException si ocurre un error en la matriz de distancias.
	 * @throws AssignmentException si ocurre un error durante la búsqueda de medoides.
	 */
	private void step_search_medoids(ArrayList<Cluster> clusters, ArrayList<Depot> medoids, NumericMatrix cost_matrix, 
			double best_cost, ArrayList<Customer> list_partition) 
					throws ProblemException, CostMatrixException, AssignmentException {
		try {
			if (clusters == null || medoids == null || cost_matrix == null || list_partition == null)
				throw new AssignmentException("Parámetros nulos en el paso de búsqueda de medoides.");

			double current_cost = 0.0;

			ArrayList<Depot> old_medoids = replicate_depots(medoids);

			System.out.println("--------------------------------------------------------------------");
			System.out.println("PROCESO DE BÚSQUEDA");

			for(int i = 0; i < clusters.size(); i++) 
			{	
				Location best_located_medoid = new Location(medoids.get(i).get_location_depot().get_axis_x(), medoids.get(i).get_location_depot().get_axis_y());

				System.out.println("--------------------------------------------------");
				System.out.println("MEJOR MEDOIDE ID: " + medoids.get(i).get_id_depot());
				System.out.println("MEJOR MEDOIDE LOCATION X: " + best_located_medoid.get_axis_x());
				System.out.println("MEJOR MEDOIDE LOCATION Y: " + best_located_medoid.get_axis_y());
				System.out.println("--------------------------------------------------");

				for(int j = 1; j < clusters.get(i).get_items_of_cluster().size(); j++) 
				{
					int new_id_medoid = clusters.get(i).get_items_of_cluster().get(j);
					Customer new_medoid = new Customer();

					new_medoid.set_id_customer(Problem.get_problem().get_customer_by_id_customer(new_id_medoid).get_id_customer());
					new_medoid.set_request_customer(Problem.get_problem().get_customer_by_id_customer(new_id_medoid).get_request_customer());	

					Location location = new Location();
					location.set_axis_x(Problem.get_problem().get_customer_by_id_customer(new_id_medoid).get_location_customer().get_axis_x());
					location.set_axis_y(Problem.get_problem().get_customer_by_id_customer(new_id_medoid).get_location_customer().get_axis_y());
					new_medoid.set_location_customer(location);

					medoids.get(i).set_id_depot(new_id_medoid);
					medoids.get(i).set_location_depot(new_medoid.get_location_customer());

					System.out.println("ID DEL NUEVO MEDOIDE: " + new_id_medoid);
					System.out.println("X DEL NUEVO MEDOIDE: " + new_medoid.get_location_customer().get_axis_x());
					System.out.println("Y DEL NUEVO MEDOIDE: " + new_medoid.get_location_customer().get_axis_y());

					System.out.println("--------------------------------------------------");
					System.out.println("LISTA DE MEDOIDES");
					System.out.println("ID: " + medoids.get(i).get_id_depot());
					System.out.println("X: " + medoids.get(i).get_location_depot().get_axis_x());
					System.out.println("Y: " + medoids.get(i).get_location_depot().get_axis_y());					

					System.out.println("LISTA DE ANTERIORES MEDOIDES");
					System.out.println("ID: " + old_medoids.get(i).get_id_depot());
					System.out.println("X: " + old_medoids.get(i).get_location_depot().get_axis_x());
					System.out.println("Y: " + old_medoids.get(i).get_location_depot().get_axis_y());
					System.out.println("--------------------------------------------------");

					current_cost = calculate_cost(clusters, cost_matrix, medoids, list_partition);

					System.out.println("---------------------------------------------");
					System.out.println("ACTUAL COSTO TOTAL: " + current_cost);
					System.out.println("---------------------------------------------");

					if(current_cost < best_cost) 
					{
						best_cost = current_cost;	
						best_located_medoid = medoids.get(i).get_location_depot();

						System.out.println("NUEVO MEJOR COSTO TOTAL: " + best_cost);
						System.out.println("NUEVO MEDOIDE ID: " + medoids.get(i).get_id_depot());
						System.out.println("NUEVO MEDOIDE LOCATION X: " + best_located_medoid.get_axis_x());
						System.out.println("NUEVO MEDOIDE LOCATION Y: " + best_located_medoid.get_axis_y());
						System.out.println("---------------------------------------------");

						old_medoids.get(i).set_id_depot(medoids.get(i).get_id_depot());
						old_medoids.get(i).get_location_depot().set_axis_x(medoids.get(i).get_location_depot().get_axis_x());
						old_medoids.get(i).get_location_depot().set_axis_y(medoids.get(i).get_location_depot().get_axis_y());
					}
					else
					{
						medoids.get(i).set_id_depot(old_medoids.get(i).get_id_depot());
						medoids.get(i).get_location_depot().set_axis_x(old_medoids.get(i).get_location_depot().get_axis_x());					
						medoids.get(i).get_location_depot().set_axis_y(old_medoids.get(i).get_location_depot().get_axis_y());	
					}
					System.out.println("ID MEDOIDE: " + medoids.get(i).get_id_depot());
					System.out.println("LISTA DE MEDOIDES X: " + medoids.get(i).get_location_depot().get_axis_x());
					System.out.println("LISTA DE MEDOIDES Y: " + medoids.get(i).get_location_depot().get_axis_y());
					System.out.println("---------------------------------------------");
				}
				medoids.get(i).set_location_depot(best_located_medoid);
			}
		} catch (ProblemException | CostMatrixException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado en el paso de búsqueda de medoides.", e);
		}
	}

	/**
	 * Calcula el costo total de una configuración de medoides sobre una partición dada,
	 * sumando las distancias de los clientes al medoide correspondiente en su clúster.
	 *
	 * @param clusters Lista de clústers actuales.
	 * @param cost_matrix Matriz de distancias.
	 * @param medoids Lista de medoides asociados a los clústers.
	 * @param list_partition Partición de clientes utilizada.
	 * @return Valor del costo total.
	 * @throws CostMatrixException si ocurre un error accediendo a la matriz de distancias.
	 */
	private double calculate_cost(ArrayList<Cluster> clusters, NumericMatrix cost_matrix, ArrayList<Depot> medoids, 
			ArrayList<Customer> list_partition) 
					throws CostMatrixException {
		try {
			if (clusters == null || medoids == null || cost_matrix == null || list_partition == null)
				throw new CostMatrixException("Parámetros nulos al calcular el costo total en CLARA.");

			double cost = 0.0;

			int id_customer =  -1;
			int pos_customer = -1;
			int pos_depot = -1;
			ArrayList<Integer> list_id_customers = new ArrayList<Integer>();

			System.out.println("-------------------------------------------------------------------------------");
			System.out.println("CALCULO DEL MEJOR COSTO");

			if (clusters.size() != medoids.size()) 
			    throw new CostMatrixException("El número de clústers y medoides no coincide. Clusters: " 
			        + clusters.size() + ", Medoids: " + medoids.size());
			
			for(int i = 0; i < clusters.size(); i++) 
			{			
				pos_depot = Problem.get_problem().get_pos_element(medoids.get(i).get_id_depot(), list_partition);
				list_id_customers = clusters.get(i).get_items_of_cluster();

				System.out.println("-------------------------------------------------------------------------------");		
				System.out.println("ID MEDOIDE: " + medoids.get(i).get_id_depot());
				System.out.println("POSICIÓN DEL MEDOIDE: " + pos_depot);
				System.out.println("CLIENTES ASIGNADOS AL MEDOIDE: " + list_id_customers);
				System.out.println("-------------------------------------------------------------------------------");

				for(int j = 0; j < list_id_customers.size(); j++) 
				{	
					id_customer =  list_id_customers.get(j);				
					pos_customer = Problem.get_problem().get_pos_element(id_customer, list_partition);

					if(pos_depot == pos_customer)
						cost += 0.0;
					else
						cost += cost_matrix.getItem(pos_depot, pos_customer);	

					System.out.println("ID CLIENTE: " + id_customer);
					System.out.println("POSICIÓN DEL CLIENTE: " + pos_customer);
					System.out.println("COSTO: " + cost_matrix.getItem(pos_depot, pos_customer));
					System.out.println("-------------------------------------------------------------------------------");
					System.out.println("COSTO ACUMULADO: " + cost);	
					System.out.println("-------------------------------------------------------------------------------");
				}
			}
			System.out.println("MEJOR COSTO TOTAL: " + cost);	
			System.out.println("-------------------------------------------------------------------------------");

			return cost;
		} catch (CostMatrixException e) {
			throw e;
		} catch (Exception e) {
			throw new CostMatrixException("Error inesperado al calcular el costo total en CLARA.", e);
		}
	}
	
	/**
	 * Calcula el coeficiente de disimilitud actual de los clústers como la suma
	 * de las distancias entre pares de elementos en cada clúster.
	 *
	 * @param distance_type Tipo de distancia a utilizar.
	 * @param clusters Lista de clústers con los clientes agrupados.
	 * @return Valor promedio del coeficiente de disimilitud.
	 * @throws CostMatrixException si ocurre un error en la matriz de distancias.
	 */
	protected double calculate_dissimilarity(DistanceType distance_type, ArrayList<Cluster> clusters) 
			throws CostMatrixException {
		try {
			if (clusters == null || clusters.isEmpty())
				throw new CostMatrixException("La lista de clústers no puede estar vacía para calcular la disimilitud.");

			double current_dissimilarity = 0.0;
			NumericMatrix dissimilarity_matrix = initialize_cost_matrix(Problem.get_problem().get_customers(), Problem.get_problem().get_depots(), distance_type);

			int pos_first_item = -1;
			int pos_second_item = -1;
			int total_clusters = clusters.size(); 
			int total_items = 0; 

			for(int i = 0; i < total_clusters; i++)
			{
				total_items = clusters.get(i).get_items_of_cluster().size(); 

				for(int j = 0; j < total_items; j++)
				{
					pos_first_item = Problem.get_problem().get_pos_element(clusters.get(i).get_items_of_cluster().get(j));

					for(int k = (j + 1); k < total_items; k++)
					{
						pos_second_item = Problem.get_problem().get_pos_element(clusters.get(i).get_items_of_cluster().get(k));
						current_dissimilarity += dissimilarity_matrix.getItem(pos_first_item, pos_second_item);
					}
				}
			}
			current_dissimilarity /= total_clusters;

			System.out.println("COEFICIENTE DE DISIMILITUD ACTUAL: " + current_dissimilarity);	

			return current_dissimilarity;
		} catch (Exception e) {
			throw new CostMatrixException("Error al calcular el coeficiente de disimilitud.", e);
		}
	}
}