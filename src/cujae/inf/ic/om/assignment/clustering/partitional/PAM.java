package cujae.inf.ic.om.assignment.clustering.partitional;

import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.CostMatrixException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Location;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;

import cujae.inf.ic.om.service.OSRMService;

import cujae.inf.ic.om.matrix.NumericMatrix;

import java.util.ArrayList;

public class PAM extends AbstractByMedoids {
	private ArrayList<Integer> list_id_elements;
	private ArrayList<Cluster> list_clusters;
	private ArrayList<Customer> list_customers_to_assign;
	
	public PAM() {
		super();
	}

	@Override
	public Solution to_clustering() throws AssignmentException, ClusterException, ProblemException, CostMatrixException {
		try {
			list_id_elements = null;
			list_clusters = null;
			list_customers_to_assign = null;
			current_iteration = 0;
			
			initialize();
			assign();
			return finish();
		} catch (ClusterException | CostMatrixException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la ejecución de la adaptación del algoritmo PAM.", e);
		}
	}
	
	@Override	
	public void initialize() throws ClusterException, ProblemException, CostMatrixException, AssignmentException {	
		try {
			list_id_elements = generate_elements(seed_type, distance_type);
			if (list_id_elements == null || list_id_elements.isEmpty())
				throw new AssignmentException("No se pudieron generar elementos iniciales (medoides).");

			list_clusters = initialize_clusters(list_id_elements);		
			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("No se pudieron inicializar los clústeres.");

			list_customers_to_assign = new ArrayList<Customer>();
		} catch (ClusterException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de inicialización.", e);
		}
	}	
		
	@Override	
	public void assign() throws AssignmentException, ClusterException, ProblemException, CostMatrixException {	
		try {
			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("Los clústeres no están inicializados.");

			if (list_id_elements == null || list_id_elements.isEmpty())
				throw new AssignmentException("La lista de identificadores de elementos está vacía.");

			ArrayList<Depot> list_medoids = new ArrayList<Depot>();

			boolean change = true;
			boolean first = true;

			while((change) && (current_iteration < count_max_iterations))
			{
				list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
				if (list_customers_to_assign.isEmpty())
					throw new AssignmentException("La lista de clientes está vacía durante la iteración " + current_iteration);

				update_customer_to_assign(list_customers_to_assign, list_id_elements);

				if(first)
				{
					list_medoids = create_centroids(list_id_elements);	
					if (list_medoids == null || list_medoids.isEmpty())
						throw new AssignmentException("No se pudieron generar los centroides iniciales (medoides).");

					first = false;
				}
				else
					update_clusters(list_clusters, list_id_elements);

				NumericMatrix cost_matrix = initialize_cost_matrix(list_customers_to_assign, list_medoids, distance_type);
				NumericMatrix cost_matrix_copy = new NumericMatrix(cost_matrix); 

				step_assignment(list_clusters, list_customers_to_assign, cost_matrix);
				ArrayList<Depot> old_medoids = replicate_depots(list_medoids);

				double best_cost = calculate_cost(list_clusters, cost_matrix_copy, list_medoids);

				step_search_medoids(list_clusters, list_medoids, cost_matrix_copy, best_cost);
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
		} catch (ClusterException | ProblemException | CostMatrixException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de asignación.", e);
		}
	}	

	@Override
	public Solution finish() throws AssignmentException {
		try {
		Solution solution = new Solution();
		
		if(!list_customers_to_assign.isEmpty())					
			for(int j = 0; j < list_customers_to_assign.size(); j++)	
				solution.get_unassigned_items().add(list_customers_to_assign.get(j).get_id_customer());

		if(!list_clusters.isEmpty())
			for(int k = 0; k < list_clusters.size(); k++)
				if(!(list_clusters.get(k).get_items_of_cluster().isEmpty()))
					solution.get_clusters().add(list_clusters.get(k));
		
		OSRMService.clear_distance_cache();
		
		return solution;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de finalización.", e);
		}
	}
	
	/**
	 * Realiza un paso de búsqueda de nuevos medoides en el algoritmo PAM, evaluando posibles mejoras en el costo total.
	 *
	 * @param clusters Lista de clústers actuales.
	 * @param medoids Lista actual de medoides.
	 * @param cost_matrix Matriz de distancias entre clientes y medoides.
	 * @param best_cost Mejor costo conocido hasta el momento.
	 * @throws ProblemException si ocurre un error accediendo a los datos del problema.
	 * @throws CostMatrixException si ocurre un error accediendo a la matriz de distancias.
	 * @throws AssignmentException si ocurre un error inesperado durante la búsqueda.
	 */
	private void step_search_medoids(ArrayList<Cluster> clusters, ArrayList<Depot> medoids, NumericMatrix cost_matrix, double best_cost) 
			throws ProblemException, CostMatrixException, AssignmentException {
		try {
			if (clusters == null || medoids == null || cost_matrix == null)
				throw new AssignmentException("Parámetros nulos detectados al realizar la búsqueda de medoides.");

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

					current_cost = calculate_cost(clusters, cost_matrix, medoids);

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
			throw new AssignmentException("Error inesperado durante la búsqueda de nuevos medoides.", e);
		}
	}

	/**
	 * Calcula el costo total de una configuración de medoides, es decir, la suma de las distancias entre cada cliente y su medoide asignado.
	 *
	 * @param clusters Lista de clústers con los clientes asignados.
	 * @param cost_matrix Matriz de distancias entre todos los elementos.
	 * @param medoids Lista actual de medoides.
	 * @return Costo total asociado a la configuración de medoides.
	 * @throws CostMatrixException si ocurre un error accediendo a la matriz de distancias.
	 */
	private double calculate_cost(ArrayList<Cluster> clusters, NumericMatrix cost_matrix, ArrayList<Depot> medoids) 
			throws CostMatrixException {
		try {
			if (clusters == null || medoids == null || cost_matrix == null)
				throw new CostMatrixException("Parámetros nulos detectados al calcular el costo de los medoides.");

			double cost = 0.0;

			int id_customer =  -1;
			int pos_customer = -1;
			int pos_depot = -1;
			ArrayList<Integer> list_id_customers = new ArrayList<Integer>();

			System.out.println("-------------------------------------------------------------------------------");
			System.out.println("CALCULO DEL MEJOR COSTO");

			for(int i = 0; i < clusters.size(); i++) 
			{			
				pos_depot = Problem.get_problem().get_pos_element(medoids.get(i).get_id_depot());
				list_id_customers = clusters.get(i).get_items_of_cluster();

				System.out.println("-------------------------------------------------------------------------------");		
				System.out.println("ID MEDOIDE: " + medoids.get(i).get_id_depot());
				System.out.println("POSICIÓN DEL MEDOIDE: " + pos_depot);
				System.out.println("CLIENTES ASIGNADOS AL MEDOIDE: " + list_id_customers);
				System.out.println("-------------------------------------------------------------------------------");

				for(int j = 0; j < list_id_customers.size(); j++) 
				{	
					id_customer =  list_id_customers.get(j);				
					pos_customer = Problem.get_problem().get_pos_element(id_customer);

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
			throw new CostMatrixException("Error al calcular el costo total de los medoides.", e);
		}
	}
}