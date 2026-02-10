package cujae.inf.ic.om.assignment.classical.cluster;

import cujae.inf.ic.om.assignment.classical.AbstractByNotUrgency;

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

public class ThreeCriteriaClustering extends AbstractByNotUrgency {
	private Solution solution = new Solution();
	
	private ArrayList<Cluster> list_clusters;
	private ArrayList<Customer> list_customers_to_assign;
	private NumericMatrix cost_matrix;

	public ThreeCriteriaClustering() {
		super();
	}
	
	@Override
	public Solution to_clustering() throws ClusterException, CostMatrixException, ProblemException, AssignmentException {
		try {
			initialize();
			assign();
			return finish();
		} catch (ClusterException | CostMatrixException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la ejecución de la heurística Three Criteria Clustering.", e);
		}
	}
	
	@Override
	public void initialize() throws ClusterException, CostMatrixException, AssignmentException {
		try {
			list_clusters = initialize_clusters();
			if (list_clusters == null || list_clusters.isEmpty())
				throw new ClusterException("No se pudieron inicializar los clústeres.");

			list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
			if (list_customers_to_assign.isEmpty())
				throw new AssignmentException("No hay clientes disponibles para asignar.");

			cost_matrix = initialize_cost_matrix(Problem.get_problem().get_customers(), Problem.get_problem().get_depots(), distance_type);
		} catch (ClusterException | CostMatrixException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de inicialización.", e);
		}
	}
	
	@Override
	public void assign() throws ProblemException, CostMatrixException, 
		ClusterException, AssignmentException {
		try {
			if (list_customers_to_assign == null || list_customers_to_assign.isEmpty())
				throw new AssignmentException("No hay clientes por asignar.");

			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("No se han definido clústeres para realizar la asignación.");

			int id_customer = -1;
			double request_customer = 0.0;

			double capacity_depot = 0;

			int pos_cluster = -1;
			double request_cluster = 0.0;

			// METODO DETERMINAR CANDIDATOS Y OTRO METODO ASIGNAR CANDIDATOS
			double difference = -1.0; 
			double percent = -1.0;
			int pos_min_value = -1;
			int pos_customer_ref = -1;

			ArrayList<Double> list_averages = null;
			ArrayList<Double> list_variances = null;

			ArrayList<Integer> list_id_candidates = new ArrayList<Integer>();
			ArrayList<ArrayList<Double>> list_values_candidates = new ArrayList<ArrayList<Double>>(); 
			ArrayList<Double> list_differences = new ArrayList<Double>();

			bucle_init:
				while((!list_customers_to_assign.isEmpty()) && (!list_clusters.isEmpty()))
				{
					list_id_candidates.clear();
					list_values_candidates.clear();
					list_differences.clear();

					for(int i = 0; i < list_customers_to_assign.size(); i++) 
					{
						id_customer = list_customers_to_assign.get(i).get_id_customer();
						list_averages = get_list_criterias_by_clusters(id_customer, list_clusters, 1);

						pos_min_value = list_averages.indexOf(Collections.min(list_averages));
						difference = get_difference(list_averages, pos_min_value);
						percent = 0.1 * list_averages.get(pos_min_value);

						if(difference >= percent)
						{
							list_id_candidates.add(id_customer);
							list_values_candidates.add(list_averages);
							list_differences.add(difference);				
						}
					}

					bucle_phase_I:	
						while((!list_id_candidates.isEmpty()))
						{	
							pos_customer_ref = list_differences.indexOf(Collections.max(list_differences));
							id_customer = list_id_candidates.get(pos_customer_ref);
							request_customer = Problem.get_problem().get_request_by_id_customer(id_customer);

							pos_cluster = list_values_candidates.get(pos_customer_ref).indexOf(Collections.min(list_values_candidates.get(pos_customer_ref)));
							request_cluster = list_clusters.get(pos_cluster).get_request_cluster();
							capacity_depot = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(list_clusters.get(pos_cluster).get_id_cluster()));

							if(capacity_depot >= (request_cluster + request_customer))
							{
								request_cluster += request_customer;
								list_clusters.get(pos_cluster).set_request_cluster(request_cluster);
								list_clusters.get(pos_cluster).get_items_of_cluster().add(id_customer);

								list_customers_to_assign.remove(Problem.get_problem().find_pos_customer(list_customers_to_assign, id_customer));
								list_id_candidates.clear();
								list_differences.clear();
								list_values_candidates.clear();

								if(is_full_depot(list_customers_to_assign, request_cluster, capacity_depot))
								{
									if(!(list_clusters.get(pos_cluster).get_items_of_cluster().isEmpty()))
										solution.get_clusters().add(list_clusters.remove(pos_cluster));
									else
										list_clusters.remove(pos_cluster);
								}
								continue bucle_init;
							}
							else
							{
								list_id_candidates.remove(pos_customer_ref);
								list_differences.remove(pos_customer_ref);
								list_values_candidates.remove(pos_customer_ref);

								continue bucle_phase_I;
							}
						}	

					if(!list_customers_to_assign.isEmpty())
					{
						for(int j = 0; j < list_customers_to_assign.size(); j++) 
						{
							id_customer = list_customers_to_assign.get(j).get_id_customer();
							list_variances = get_list_criterias_by_clusters(id_customer, list_clusters, 2);

							pos_min_value = list_variances.indexOf(Collections.min(list_variances));
							difference = get_difference(list_variances, pos_min_value);
							percent = 0.4 * list_variances.get(pos_min_value);

							if(difference >= percent)
							{
								list_id_candidates.add(id_customer);
								list_values_candidates.add(list_variances);
								list_differences.add(difference);
							}
						}

						bucle_phase_II:
							while((!list_id_candidates.isEmpty()))
							{
								pos_customer_ref = list_differences.indexOf(Collections.max(list_differences));
								id_customer = list_id_candidates.get(pos_customer_ref);
								request_customer = Problem.get_problem().get_request_by_id_customer(id_customer);

								pos_cluster = list_values_candidates.get(pos_customer_ref).indexOf(Collections.min(list_values_candidates.get(pos_customer_ref)));
								request_cluster = list_clusters.get(pos_cluster).get_request_cluster();
								capacity_depot = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(list_clusters.get(pos_cluster).get_id_cluster()));

								if(capacity_depot >= (request_cluster + request_customer))
								{
									request_cluster += request_customer;
									list_clusters.get(pos_cluster).set_request_cluster(request_cluster);
									list_clusters.get(pos_cluster).get_items_of_cluster().add(id_customer);

									list_customers_to_assign.remove(Problem.get_problem().find_pos_customer(list_customers_to_assign, id_customer));
									list_id_candidates.clear();
									list_differences.clear();
									list_values_candidates.clear();

									if(is_full_depot(list_customers_to_assign, request_cluster, capacity_depot))
									{
										if(!(list_clusters.get(pos_cluster).get_items_of_cluster().isEmpty()))
											solution.get_clusters().add(list_clusters.remove(pos_cluster));
										else
											list_clusters.remove(pos_cluster);
									}

									continue bucle_init;
								}
								else
								{
									list_id_candidates.remove(pos_customer_ref);
									list_differences.remove(pos_customer_ref);
									list_values_candidates.remove(pos_customer_ref);

									continue bucle_phase_II;
								}
							}	

						ArrayList<Double> list_nearest_dist = new ArrayList<Double>();

						for(int k = 0; k < list_customers_to_assign.size(); k++)
						{
							id_customer = list_customers_to_assign.get(k).get_id_customer();
							list_averages = get_list_criterias_by_clusters(id_customer, list_clusters, 1); //pq 1

							list_id_candidates.add(id_customer);
							list_values_candidates.add(list_averages);

							pos_cluster = list_averages.indexOf(Collections.min(list_averages));
							ArrayList<Double> listDistCC = get_distances_in_cluster(id_customer, list_clusters.get(pos_cluster));
							list_nearest_dist.add(listDistCC.get(listDistCC.indexOf(Collections.min(listDistCC))));
						}

						bucle_phase_III:
							while(!list_id_candidates.isEmpty())
							{	
								pos_customer_ref = list_nearest_dist.indexOf(Collections.min(list_nearest_dist));
								id_customer = list_customers_to_assign.get(pos_customer_ref).get_id_customer();
								request_customer = Problem.get_problem().get_request_by_id_customer(id_customer);

								pos_cluster = list_values_candidates.get(pos_customer_ref).indexOf(Collections.min(list_values_candidates.get(pos_customer_ref)));
								request_cluster = list_clusters.get(pos_cluster).get_request_cluster();
								capacity_depot = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(list_clusters.get(pos_cluster).get_id_cluster()));

								if(capacity_depot >= (request_cluster + request_customer))
								{		
									request_cluster += request_customer;
									list_clusters.get(pos_cluster).set_request_cluster(request_cluster);
									list_clusters.get(pos_cluster).get_items_of_cluster().add(id_customer);

									list_customers_to_assign.remove(Problem.get_problem().find_pos_customer(list_customers_to_assign, id_customer));
									list_values_candidates.clear();
									list_id_candidates.remove(pos_customer_ref);

									if(is_full_depot(list_customers_to_assign, request_cluster, capacity_depot))
									{
										if(!(list_clusters.get(pos_cluster).get_items_of_cluster().isEmpty()))
											solution.get_clusters().add(list_clusters.remove(pos_cluster));
										else
											list_clusters.remove(pos_cluster);
									}
									continue bucle_init;
								}
								else
								{
									list_values_candidates.remove(pos_customer_ref);
									list_nearest_dist.remove(pos_customer_ref);
									list_id_candidates.remove(pos_customer_ref);

									if(list_id_candidates.isEmpty())
										for(int j = 0; j < list_customers_to_assign.size(); j++)
											solution.get_unassigned_items().add(list_customers_to_assign.remove(j).get_id_customer());

									continue bucle_phase_III;
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
					if(!list_clusters.get(k).get_items_of_cluster().isEmpty())
						solution.get_clusters().add(list_clusters.get(k));

			OSRMService.clear_distance_cache();

			return solution;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de finalización.", e);
		}
	}
	
	/**
	 * Calcula la diferencia entre el menor valor y el segundo menor valor de una lista.
	 *
	 * @param list_values Lista de valores.
	 * @param pos_first_min Posición del menor valor.
	 * @return Diferencia entre el segundo menor y el menor valor.
	 * @throws AssignmentException si la lista tiene menos de dos elementos o se produce un error.
	 */
	private double get_difference(ArrayList<Double> list_values, int pos_first_min) throws AssignmentException {
		try {
			
			if (list_values == null || list_values.isEmpty()) 
				throw new AssignmentException("La lista es nula o vacía.");
	        
	        if (pos_first_min < 0 || pos_first_min >= list_values.size()) 
	            throw new AssignmentException("La posición del mínimo valor es inválida.");
	        
			double difference = 0.0;
			double second_min = list_values.get(0);
			int i = 1;

			if((pos_first_min == 0) && (list_values.size() > 1))
			{
				second_min = list_values.get(1);
				i = 2; 
			}
			for (; i < list_values.size(); i++)
			{
				if((list_values.get(i) < second_min) && (i != pos_first_min))
					second_min = list_values.get(i);			
			}
			difference = second_min - list_values.get(pos_first_min);

			return difference;		
		} catch (Exception e) {
			throw new AssignmentException("Error al calcular la diferencia entre criterios de clúster.", e);
		}
	}
	
	/**
	 * Devuelve una lista con los valores de uno de los criterios para un cliente en todos los clústeres.
	 *
	 * @param id_customer ID del cliente.
	 * @param clusters Lista de clústeres.
	 * @param criteria Criterio (1 = promedio, 2 = varianza).
	 * @return Lista de valores del criterio.
	 * @throws CostMatrixException si ocurre un error accediendo a la matriz de costos.
	 * @throws AssignmentException si el criterio es inválido o se produce otro error.
	 */	
	private ArrayList<Double> get_list_criterias_by_clusters(int id_customer, ArrayList<Cluster> clusters, 
			int criteria) throws CostMatrixException, AssignmentException {
		try {
			if (clusters == null || clusters.isEmpty()) 
	            throw new AssignmentException("La lista de clústeres está vacía o es nula.");
	        
			ArrayList<Double> list_values = new ArrayList<Double>();
			int pos_customer = Problem.get_problem().get_pos_element(id_customer);

			switch(criteria)
			{
			case 1: 
			{
				for(int i = 0; i < clusters.size(); i++)
					list_values.add(get_avg_by_cluster(pos_customer, clusters.get(i), cost_matrix));
				break;
			}
			case 2: 
			{
				for(int i = 0; i < clusters.size(); i++)
					list_values.add(get_var_by_cluster(pos_customer, clusters.get(i), cost_matrix));
				break;
			}
			}
			return list_values;		
		} catch (CostMatrixException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error al calcular los criterios del cliente " + id_customer, e);
		}
	}
	
	/**
	 * Calcula la distancia promedio de un cliente hacia todos los elementos de un clúster.
	 *
	 * @param pos_customer Posición del cliente.
	 * @param cluster Clúster destino.
	 * @param cost_matrix Matriz de costos.
	 * @return Promedio de distancias.
	 * @throws CostMatrixException si ocurre un error accediendo a la matriz de costos.
	 */
	private double get_avg_by_cluster(int pos_customer, Cluster cluster, NumericMatrix cost_matrix) 
			throws CostMatrixException {
		try {
			 if (cluster == null || cost_matrix == null) 
		            throw new CostMatrixException("El clúster o la matriz de costos es nula.");
		    
			double distances = 0.0;

			for(int i = 0; i < cluster.get_items_of_cluster().size(); i++)
				distances += cost_matrix.getItem(pos_customer, Problem.get_problem().get_pos_element(cluster.get_items_of_cluster().get(i)));

			distances += cost_matrix.getItem(pos_customer, Problem.get_problem().get_pos_element(cluster.get_id_cluster()));

			return distances/(cluster.get_items_of_cluster().size() + 1);
		} catch (Exception e) {
			throw new CostMatrixException("Error al calcular distancia promedio al clúster.", e);
		}
	}
	
	/**
	 * Calcula la varianza de las distancias de un cliente hacia todos los elementos del clúster.
	 *
	 * @param pos_customer Posición del cliente.
	 * @param cluster Clúster destino.
	 * @param cost_matrix Matriz de costos.
	 * @return Varianza de distancias.
	 * @throws CostMatrixException si ocurre un error accediendo a la matriz de costos.
	 */
	private double get_var_by_cluster(int pos_customer, Cluster cluster, NumericMatrix cost_matrix) throws CostMatrixException {
		try {
			if (cluster == null || cost_matrix == null) 
	            throw new CostMatrixException("El clúster o la matriz de costos es nula.");
	        
			int pos_element = -1;	
			double distance = 0.0;
			double difference = 0.0;
			double avg_dist = get_avg_by_cluster(pos_customer, cluster, cost_matrix);

			for(int i = 0; i < cluster.get_items_of_cluster().size(); i++)
			{
				pos_element = Problem.get_problem().get_pos_element(cluster.get_items_of_cluster().get(i));
				distance = cost_matrix.getItem(pos_customer, pos_element);
				difference += Math.pow(distance - avg_dist, 2);
			}
			pos_element = Problem.get_problem().get_pos_element(cluster.get_id_cluster());
			distance = cost_matrix.getItem(pos_customer, pos_element);
			difference += Math.pow(distance - avg_dist, 2);

			return difference/(cluster.get_items_of_cluster().size() + 1);
		} catch (Exception e) {
			throw new CostMatrixException("Error al calcular varianza de distancias al clúster.", e);
		}
	}
	
	/**
	 * Devuelve una lista con las distancias desde un cliente a todos los elementos de un clúster.
	 *
	 * @param id_customer_ref ID del cliente de referencia.
	 * @param cluster Clúster destino.
	 * @return Lista de distancias.
	 * @throws CostMatrixException si ocurre un error accediendo a la matriz de costos.
	 */
	private ArrayList<Double> get_distances_in_cluster(int id_customer_ref, Cluster cluster) throws CostMatrixException {
		try {
			if (cluster == null || cluster.get_items_of_cluster() == null) 
	            throw new CostMatrixException("El clúster es nulo o no contiene elementos.");
	        
			ArrayList<Double> list_dist_cluster = new ArrayList<Double>();
			int pos_customer_ref = Problem.get_problem().get_pos_element(id_customer_ref);
			int pos_cc = -1;

			for(int i = 0; i < cluster.get_items_of_cluster().size(); i++)
			{
				pos_cc = Problem.get_problem().get_pos_element(cluster.get_items_of_cluster().get(i));
				list_dist_cluster.add(cost_matrix.getItem(pos_customer_ref, pos_cc));
			}
			if(list_dist_cluster.isEmpty())
			{
				pos_cc = Problem.get_problem().get_pos_element(cluster.get_id_cluster());
				list_dist_cluster.add(cost_matrix.getItem(pos_customer_ref, pos_cc));
			}
			return list_dist_cluster;
		} catch (Exception e) {
			throw new CostMatrixException("Error al calcular distancias internas del clúster.", e);
		}
	}		
}