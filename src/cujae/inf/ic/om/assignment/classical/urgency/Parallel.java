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

public class Parallel extends AbstractByUrgency implements IUrgency {
	private Solution solution = new Solution();	
	
	private ArrayList<Cluster> list_clusters;	
	private ArrayList<Customer> list_customers_to_assign;
	private ArrayList<Integer> list_id_depots;
	
	private NumericMatrix urgency_matrix;
	private NumericMatrix closest_matrix;
	
	private ArrayList<ArrayList<Integer>> list_depots_ordered;
	private ArrayList<Double> list_urgencies;
	
	public Parallel() {
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
			throw new AssignmentException("Error inesperado durante la ejecución de la heurística Parallel.", e);
		}
	}		
		
	@Override
	public void initialize() throws ClusterException, CostMatrixException, AssignmentException {
		try {
			list_clusters = initialize_clusters();	
			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("No se pudo inicializar la lista de clústeres.");

			list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
			if (list_customers_to_assign.isEmpty())
				throw new AssignmentException("No hay clientes para asignar.");
			list_id_depots = new ArrayList<Integer>(Problem.get_problem().get_list_id_depots());
			if (list_id_depots.isEmpty())
				throw new AssignmentException("No hay depósitos disponibles para asignación.");

			urgency_matrix = initialize_cost_matrix(list_customers_to_assign, Problem.get_problem().get_depots(), distance_type);
			closest_matrix = urgency_matrix;
			
			if (urgency_matrix == null || urgency_matrix.getRowCount() == 0)
				throw new AssignmentException("La matriz de urgencias no pudo ser inicializada correctamente.");

			list_depots_ordered = get_depots_ordered(list_customers_to_assign, list_id_depots, closest_matrix);
			list_urgencies = get_list_urgencies(list_customers_to_assign, list_depots_ordered, urgency_matrix);
			
			if (list_depots_ordered == null || list_depots_ordered.isEmpty())
				throw new AssignmentException("La estructura de depósitos ordenados no fue generada correctamente.");

			if (list_urgencies == null || list_urgencies.isEmpty())
				throw new AssignmentException("La lista de urgencias no fue generada correctamente.");

		} catch (ClusterException | CostMatrixException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de inicialización: " + e.getMessage(), e);
		}
	}	
		
	@Override
	public void assign() throws ProblemException, ClusterException, CostMatrixException, AssignmentException {
		try {
			if (list_customers_to_assign == null || list_customers_to_assign.isEmpty())
				throw new AssignmentException("No hay clientes disponibles para asignar.");
			
			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("No hay clústeres disponibles para la asignación.");
			
			if (list_id_depots == null || list_id_depots.isEmpty())
				throw new AssignmentException("No hay depósitos disponibles.");
			
			if (list_urgencies == null || list_depots_ordered == null || urgency_matrix == null)
				throw new AssignmentException("Estructuras de datos no inicializadas correctamente.");

			int pos_customer = -1;
			int id_customer = -1;
			double request_customer = 0.0;	

			int id_closest_depot = -1;
			double capacity_depot = 0.0;

			int pos_cluster = -1;
			double request_cluster = 0.0;

			while((!list_customers_to_assign.isEmpty()) && (!list_clusters.isEmpty())) 	
			{
				pos_customer = list_urgencies.indexOf(Collections.max(list_urgencies));
				id_customer = list_customers_to_assign.get(pos_customer).get_id_customer();
				request_customer = list_customers_to_assign.get(pos_customer).get_request_customer();

				if (list_depots_ordered.get(pos_customer).isEmpty())
					throw new AssignmentException("No hay depósitos disponibles para el cliente " + id_customer);
				
				id_closest_depot = list_depots_ordered.get(pos_customer).get(0).intValue(); 
				capacity_depot = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(id_closest_depot));

				pos_cluster = find_cluster(id_closest_depot, list_clusters);
				if (pos_cluster == -1)
					throw new AssignmentException("No se encontró un clúster correspondiente al depósito " + id_closest_depot);

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
								list_urgencies.set(pos_customer, get_urgency(id_customer, list_depots_ordered.get(pos_customer), urgency_matrix));
						}
						else 
						{ 
							int current_pos_depot = -1;
							int pos_depot = Problem.get_problem().find_pos_element(list_id_depots, id_closest_depot);

							for(int i = 0; i < list_depots_ordered.size(); i++)
							{ 
								current_pos_depot = Problem.get_problem().find_pos_element(list_depots_ordered.get(i), id_closest_depot);

								if(current_pos_depot != -1)
								{
									list_depots_ordered.get(i).remove(current_pos_depot);

									if(list_depots_ordered.get(i).isEmpty())
									{
										solution.get_unassigned_items().add(list_customers_to_assign.get(i).get_id_customer());

										list_customers_to_assign.remove(i);
										list_urgencies.remove(i);
										list_depots_ordered.remove(i);
									}
									else
										list_urgencies.set(i, get_urgency(list_customers_to_assign.get(i).get_id_customer(), list_depots_ordered.get(i), 
												urgency_matrix));
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
	 * Calcula la urgencia de cada cliente con respecto a los depósitos más cercanos.
	 * 
	 * @param list_customers_to_assign Clientes a asignar.
	 * @param list_id_depots Lista de depósitos ordenados por cliente.
	 * @param urgency_matrix Matriz de distancias.
	 * @return Lista de urgencias.
	 * @throws CostMatrixException si ocurre un error al acceder a la matriz.
	 */
	@Override
	public ArrayList<Double> get_list_urgencies(ArrayList<Customer> list_customers_to_assign, ArrayList<ArrayList<Integer>> list_id_depots, 
			NumericMatrix urgency_matrix) throws CostMatrixException {
		if (list_customers_to_assign == null || list_id_depots == null || urgency_matrix == null)
			throw new CostMatrixException("Los parámetros no pueden ser nulos en get_list_urgencies().");

		if (list_customers_to_assign.isEmpty())
			throw new CostMatrixException("La lista de clientes está vacía en get_list_urgencies().");

		if (list_id_depots.isEmpty())
			throw new CostMatrixException("La lista de depósitos está vacía en get_list_urgencies().");

		if (list_id_depots.size() != list_customers_to_assign.size() && list_id_depots.size() > 1)
			throw new CostMatrixException("El número de listas de depósitos no coincide con la cantidad de clientes.");

		ArrayList<Double> urgencies = new ArrayList<Double>();
		
		if(list_id_depots.size() > 1)
			for(int i = 0; i < list_customers_to_assign.size(); i++)
				urgencies.add(get_urgency(list_customers_to_assign.get(i).get_id_customer(), list_id_depots.get(i), urgency_matrix));
		else
			for(int i = 0; i < list_customers_to_assign.size(); i++)
				urgencies.add(get_urgency(list_customers_to_assign.get(i).get_id_customer(), list_id_depots.get(0), urgency_matrix));

		return urgencies;
	}
	
	/**
	 * Calcula la urgencia de un cliente con respecto a una lista de depósitos.
	 * 
	 * @param id_customer Identificador del cliente.
	 * @param list_id_depots Lista de identificadores de depósitos.
	 * @param urgency_matrix Matriz de distancias.
	 * @return Valor de urgencia calculado.
	 * @throws CostMatrixException si ocurre un error al acceder a la matriz.
	 */
	@Override
	public double get_urgency(int id_customer, ArrayList<Integer> list_id_depots, NumericMatrix urgency_matrix) throws CostMatrixException {
		double urgency = 0.0;
		double closest_dist = 0.0;
		double other_dist = 0.0;
		int pos_matrix_customer = -1;
		int pos_matrix_depot = -1;
		
		if (list_id_depots == null || list_id_depots.isEmpty())
			throw new CostMatrixException("La lista de depósitos no puede ser nula ni vacía en get_urgency().");

		if (urgency_matrix == null)
			throw new CostMatrixException("La matriz de urgencias no puede ser nula en get_urgency().");
		
		pos_matrix_customer = Problem.get_problem().get_pos_element(id_customer);
		pos_matrix_depot = Problem.get_problem().get_pos_element(list_id_depots.get(0));
		
		if (pos_matrix_customer < 0 || pos_matrix_depot < 0)
			throw new CostMatrixException("Posición inválida en la matriz de costos en get_urgency().");
		
		closest_dist = urgency_matrix.getItem(pos_matrix_depot, pos_matrix_customer);
		
		if(list_id_depots.size() == 1)
			urgency = closest_dist;
		else
		{
			if(list_id_depots.size() > 1)
			{
				for(int i = 1; i < list_id_depots.size(); i++)
				{
					pos_matrix_depot = Problem.get_problem().get_pos_element(list_id_depots.get(i));	
					if (pos_matrix_depot < 0)
						throw new CostMatrixException("Depósito con ID inválido: " + list_id_depots.get(i));
					other_dist += urgency_matrix.getItem(pos_matrix_depot, pos_matrix_customer);
				}
				urgency = calculate_urgency(closest_dist, other_dist);
			}
		}
		return urgency;	
	}
}