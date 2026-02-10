package cujae.inf.ic.om.assignment.classical.urgency;

import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.CostMatrixException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;

import cujae.inf.ic.om.service.OSRMService;

import cujae.inf.ic.om.matrix.NumericMatrix;

import java.util.ArrayList;
import java.util.Collections;

public class Sweep extends AbstractByUrgency implements IUrgencyWithMU {
	private Solution solution = new Solution();
	
	private ArrayList<Cluster> list_clusters;	
	private ArrayList<Customer> list_customers_to_assign;
	private ArrayList<Integer> list_id_depots;
	
	private NumericMatrix urgency_matrix;
	private NumericMatrix closest_matrix;
	
	private ArrayList<ArrayList<Integer>> list_depots_ordered;
	private int mu_id_depot;
	private ArrayList<Double> list_urgencies;

	public Sweep() {
		super();
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
			throw new AssignmentException("Error inesperado durante la ejecución de la heurística Sweep.", e);
		}
	}
	
	@Override
	public void initialize() throws CostMatrixException, ProblemException, ClusterException, AssignmentException {
		try {
			list_clusters = initialize_clusters();	
			if (list_clusters == null || list_clusters.isEmpty())
				throw new ClusterException("No se pudieron inicializar los clústeres.");

			list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
			if (list_customers_to_assign.isEmpty())
				throw new AssignmentException("No hay clientes disponibles para asignar.");

			list_id_depots = new ArrayList<Integer>(Problem.get_problem().get_list_id_depots());
			if (list_id_depots.isEmpty())
				throw new AssignmentException("No hay depósitos definidos para el problema.");

			urgency_matrix = initialize_cost_matrix(list_customers_to_assign, Problem.get_problem().get_depots(), distance_type);
			closest_matrix = new NumericMatrix(urgency_matrix);

			list_depots_ordered = get_depots_ordered(list_customers_to_assign, list_id_depots, closest_matrix);
			mu_id_depot = find_cluster_with_mu(list_clusters);
			list_urgencies = get_list_urgencies(list_customers_to_assign, list_depots_ordered, urgency_matrix, mu_id_depot);	
			if (list_depots_ordered.size() != list_customers_to_assign.size() || list_urgencies.size() != list_customers_to_assign.size())
				throw new AssignmentException("Tamaño inconsistente entre las estructuras internas de urgencias y depósitos ordenados.");
		
		} catch (ClusterException | CostMatrixException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de inicialización.", e);
		}
	}
	
	@Override
	public void assign() throws CostMatrixException, ProblemException, ClusterException, AssignmentException {
		try {
			int pos_customer = -1;
			int id_customer = -1;
			double request_customer = 0.0;

			int id_closest_depot = -1;
			double capacity_depot = 0.0;

			int pos_cluster = -1;
			double request_cluster = 0.0;

			while((!list_customers_to_assign.isEmpty()) && (!list_clusters.isEmpty())) 
			{
				if (list_urgencies.isEmpty() || list_depots_ordered.isEmpty())
					throw new AssignmentException("Estructuras internas vacías: urgencias o depósitos ordenados.");

				pos_customer = list_urgencies.indexOf(Collections.max(list_urgencies));
				if (pos_customer < 0 || pos_customer >= list_customers_to_assign.size())
					throw new AssignmentException("Índice inválido al seleccionar el cliente con mayor urgencia.");

				id_customer = list_customers_to_assign.get(pos_customer).get_id_customer();
				request_customer = list_customers_to_assign.get(pos_customer).get_request_customer();

				if (list_depots_ordered.get(pos_customer).isEmpty())
					throw new AssignmentException("El cliente con ID " + id_customer + " no tiene depósitos disponibles.");

				id_closest_depot = list_depots_ordered.get(pos_customer).get(0).intValue(); 
				capacity_depot = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(id_closest_depot));

				pos_cluster = find_cluster(id_closest_depot, list_clusters);
				if (pos_cluster == -1) {
					// Este depósito ya no tiene clúster, pero aún está listado para este cliente
					// Lo eliminamos de la lista de depósitos disponibles del cliente
					list_depots_ordered.get(pos_customer).remove(0);

					if (list_depots_ordered.get(pos_customer).isEmpty()) {
						solution.get_unassigned_items().add(id_customer);
						list_customers_to_assign.remove(pos_customer);
						list_urgencies.remove(pos_customer);
						list_depots_ordered.remove(pos_customer);
					} else {
						list_urgencies.set(pos_customer, get_urgency(
							id_customer,
							list_depots_ordered.get(pos_customer),
							urgency_matrix,
							mu_id_depot
						));
					}
					continue; // Saltar esta iteración y seguir con el siguiente cliente
				}
				if(pos_cluster != -1)
				{
					request_cluster = list_clusters.get(pos_cluster).get_request_cluster();

					if(capacity_depot >= (request_cluster + request_customer))
					{
						request_cluster += request_customer;

						list_clusters.get(pos_cluster).set_request_cluster(request_cluster);
						list_clusters.get(pos_cluster).get_items_of_cluster().add(id_customer);

						list_customers_to_assign.remove(pos_customer);
						list_urgencies.remove(pos_customer);
						list_depots_ordered.remove(pos_customer);

						if(id_closest_depot == mu_id_depot)
						{
							mu_id_depot = find_cluster_with_mu(list_clusters);
							if(id_closest_depot != mu_id_depot)
							{
								urgency_matrix = initialize_cost_matrix(Problem.get_problem().get_customers(), Problem.get_problem().get_depots(), distance_type);
								list_urgencies = get_list_urgencies(list_customers_to_assign, list_depots_ordered, urgency_matrix, mu_id_depot);
							}
						}
					}
					else
					{
						if(capacity_depot > request_cluster)
						{
							list_depots_ordered.get(pos_customer).remove(0);

							if(list_depots_ordered.get(pos_customer).isEmpty())
							{
								solution.get_unassigned_items().add(id_customer);
								list_customers_to_assign.remove(pos_customer);
								list_urgencies.remove(pos_customer);
								list_depots_ordered.remove(pos_customer);							
							}
							else
								list_urgencies.set(pos_customer, get_urgency(id_customer, list_depots_ordered.get(pos_customer), 
										urgency_matrix, mu_id_depot));
						}						
						else
						{					
							int pos_depot = Problem.get_problem().find_pos_element(list_id_depots, id_closest_depot);
							int current_pos_depot = -1;
							int total_available_depots = 0;

							for (int i = list_depots_ordered.size() - 1; i >= 0; i--) 
							{
								if (!list_depots_ordered.get(i).contains(id_closest_depot))
									continue;

								current_pos_depot = Problem.get_problem().find_pos_element(list_depots_ordered.get(i), id_closest_depot);

								if (current_pos_depot != -1) 
								{
									total_available_depots = list_depots_ordered.get(i).size();

									if (total_available_depots == 1) 
									{
										solution.get_unassigned_items().add(list_customers_to_assign.get(i).get_id_customer());

										list_customers_to_assign.remove(i);
										list_urgencies.remove(i);
										list_depots_ordered.remove(i);
									} else {
										if (current_pos_depot == 0)
											list_urgencies.set(i, get_urgency(
												list_customers_to_assign.get(i).get_id_customer(),
												list_depots_ordered.get(i),
												urgency_matrix,
												mu_id_depot
											));
										list_depots_ordered.get(i).remove(current_pos_depot);
									}
								}
							}		
							list_id_depots.remove(pos_depot);

							if(!(list_clusters.get(pos_cluster).get_items_of_cluster().isEmpty()))
								solution.get_clusters().add(list_clusters.remove(pos_cluster));
							else
								list_clusters.remove(pos_cluster);
						}
					}
				}
			}
		} catch (ProblemException | CostMatrixException | ClusterException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de asignación.", e);
		}
	}
	
	@Override
	public Solution finish() throws AssignmentException {
		try {
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
	 * Retorna el identificador del depósito cuyo clúster tiene mayor demanda insatisfecha.
	 *
	 * @param clusters Lista de clústeres generados.
	 * @return ID del depósito con mayor capacidad restante.
	 * @throws ProblemException si ocurre un error al acceder a datos del problema o la lista está vacía.
	 */
	private int find_cluster_with_mu(ArrayList<Cluster> clusters) throws ProblemException{
		if (clusters == null || clusters.isEmpty()) 
			throw new ProblemException("La lista de clústeres está vacía o es nula en find_cluster_with_mu.");
		
		int id_depot = clusters.get(0).get_id_cluster();
		double mu_request = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(id_depot)) - 
				clusters.get(0).get_request_cluster();
		
		double cu_request = 0.0;
		
		for(int i = 1; i < clusters.size(); i++)
		{
			cu_request = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(clusters.get(i).get_id_cluster())) - 
					clusters.get(i).get_request_cluster();
			
			if(cu_request > mu_request)
			{
				mu_request = cu_request;
				id_depot = clusters.get(i).get_id_cluster();
			}						
		}
		return id_depot;
	}
	
	/**
	 * Calcula la urgencia de cada cliente respecto al depósito con mayor demanda insatisfecha.
	 *
	 * @param list_customers_to_assign Lista de clientes a asignar.
	 * @param list_id_depots Lista de listas de depósitos por cliente.
	 * @param urgency_matrix Matriz de distancias.
	 * @param mu_id_depot Identificador del depósito con mayor demanda insatisfecha.
	 * @return Lista de urgencias para cada cliente.
	 * @throws CostMatrixException si ocurre un error en la matriz o las listas de entrada son inconsistentes.
	 */
	public ArrayList<Double> get_list_urgencies(ArrayList<Customer> list_customers_to_assign, ArrayList<ArrayList<Integer>> list_id_depots, 
			NumericMatrix urgency_matrix, int mu_id_depot) throws CostMatrixException {
		if (list_customers_to_assign == null || list_id_depots == null || urgency_matrix == null) 
			throw new CostMatrixException("Uno o más parámetros son nulos en get_list_urgencies.");
		
		if ((list_customers_to_assign.isEmpty() || list_id_depots.isEmpty()) &&
			    !(list_customers_to_assign.isEmpty() && list_id_depots.isEmpty()))
			    throw new CostMatrixException("Las listas de clientes o depósitos están vacías de forma inconsistente.");

		if (list_customers_to_assign.size() != list_id_depots.size() && list_id_depots.size() != 1) 
			throw new CostMatrixException("Tamaño inconsistente entre clientes y depósitos.");
		
		ArrayList<Double> urgencies = new ArrayList<Double>();
		
		for (int i = 0; i < list_customers_to_assign.size(); i++) 
		{
			int id_customer = list_customers_to_assign.get(i).get_id_customer();
			ArrayList<Integer> current_depots = (list_id_depots.size() == 1) ? list_id_depots.get(0) : list_id_depots.get(i);

			if (current_depots == null || current_depots.isEmpty()) 
				throw new CostMatrixException("La lista de depósitos para el cliente " + id_customer + " está vacía o nula.");
			
			urgencies.add(get_urgency(id_customer, current_depots, urgency_matrix, mu_id_depot));
		}
		return urgencies;
	}

	/**
	 * Calcula la urgencia de un cliente respecto a su depósito más cercano y al de mayor demanda insatisfecha.
	 *
	 * @param id_customer ID del cliente.
	 * @param list_id_depots Lista de ID de depósitos.
	 * @param urgency_matrix Matriz de distancias.
	 * @param mu_id_depot ID del depósito con mayor demanda insatisfecha.
	 * @return Valor de urgencia.
	 * @throws CostMatrixException si ocurre un error al acceder a posiciones inválidas en la matriz.
	 */
	@Override
	public double get_urgency(int id_customer, ArrayList<Integer> list_id_depots, NumericMatrix urgency_matrix, int mu_id_depot) throws CostMatrixException {
		if (list_id_depots == null || list_id_depots.isEmpty()) 
			throw new CostMatrixException("La lista de depósitos está vacía o es nula para el cliente " + id_customer);
		
		if (urgency_matrix == null) 
			throw new CostMatrixException("La matriz de distancias es nula.");
		
		double urgency = 0.0;
		int pos_customer_matrix = -1;
		double mu_dist = 0.0;
		double closest_dist = 0.0;
		int pos_mu_depot_matrix = -1;
		int pos_depot_matrix_closest = -1;
		
		pos_customer_matrix = Problem.get_problem().get_pos_element(id_customer);
		pos_depot_matrix_closest = Problem.get_problem().get_pos_element(list_id_depots.get(0));
		
		if (pos_customer_matrix < 0) 
			throw new CostMatrixException("Índice de cliente inválido: " + pos_customer_matrix);
		
		pos_mu_depot_matrix = Problem.get_problem().get_pos_element(mu_id_depot);

		if (pos_mu_depot_matrix < 0 || pos_depot_matrix_closest < 0)
			throw new CostMatrixException("Índices inválidos de depósitos para cliente " + id_customer);
		
		mu_dist = urgency_matrix.getItem(pos_mu_depot_matrix, pos_customer_matrix);
		closest_dist = urgency_matrix.getItem(pos_depot_matrix_closest, pos_customer_matrix);
		
		if (closest_dist == Double.POSITIVE_INFINITY && mu_dist == Double.POSITIVE_INFINITY) 
			throw new CostMatrixException("No se puede calcular la urgencia: ambas distancias son infinitas para el cliente " + id_customer);
		
		if(mu_dist == Double.POSITIVE_INFINITY) // y la otra distancia no puede ser posiinf
			urgency = closest_dist;
		else
			urgency = calculate_urgency(closest_dist, mu_dist);
		
		return urgency;
	}
}