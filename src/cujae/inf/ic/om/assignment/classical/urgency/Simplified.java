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
import cujae.inf.ic.om.matrix.RowCol;

import java.util.ArrayList;
import java.util.Collections;

public class Simplified extends AbstractByUrgency implements IUrgency {
	private Solution solution = new Solution();	
	
	private ArrayList<Cluster> list_clusters;
	private ArrayList<Customer> list_customers_to_assign;

	private NumericMatrix urgency_matrix;
	private NumericMatrix closest_matrix;
	
	private ArrayList<ArrayList<Integer>> list_id_depots;
	private ArrayList<ArrayList<Integer>> list_depots_ordered;
	private ArrayList<Double> list_urgencies;
	
	public Simplified() {
		super();
	}
	
	@Override
	public Solution to_clustering() throws CostMatrixException, ClusterException, ProblemException, AssignmentException {
		try {
			initialize();
			assign();
			return finish();
		} catch (ClusterException | CostMatrixException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la ejecución de la heurística Simplified.", e);
		}
	}	
	
	@Override
	public void initialize() throws CostMatrixException, ClusterException, AssignmentException {
		try {
			list_clusters = initialize_clusters();
			if (list_clusters == null || list_clusters.isEmpty())
				throw new ClusterException("No se pudieron inicializar los clústeres correctamente.");

			list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
			if (list_customers_to_assign.isEmpty())
				throw new AssignmentException("La lista de clientes a asignar está vacía.");

			urgency_matrix = initialize_cost_matrix(list_customers_to_assign, Problem.get_problem().get_depots(), distance_type);
			closest_matrix = new NumericMatrix(urgency_matrix);

			list_id_depots = new ArrayList<ArrayList<Integer>>();
			list_id_depots.add(Problem.get_problem().get_list_id_depots());

			list_depots_ordered = get_depots_ordered(list_customers_to_assign, list_id_depots.get(0), closest_matrix);
			list_urgencies = get_list_urgencies(list_customers_to_assign, list_id_depots, urgency_matrix);
			if (list_depots_ordered.size() != list_customers_to_assign.size() || list_urgencies.size() != list_customers_to_assign.size())
				throw new AssignmentException("Las estructuras de urgencia o depósitos ordenados no coinciden con la cantidad de clientes.");
		
		} catch (ClusterException | CostMatrixException e) {
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

			int total_items = list_customers_to_assign.size();

			while((!list_customers_to_assign.isEmpty()) && (!list_clusters.isEmpty())) 
			{
				if (list_urgencies.isEmpty() || list_depots_ordered.isEmpty())
					throw new AssignmentException("Las estructuras internas de urgencia o depósitos están vacías o inconsistentes.");

				pos_customer = list_urgencies.indexOf(Collections.max(list_urgencies));
				if (pos_customer < 0 || pos_customer >= list_customers_to_assign.size())
					throw new AssignmentException("No se pudo determinar el cliente con mayor urgencia.");

				id_customer = list_customers_to_assign.get(pos_customer).get_id_customer();
				request_customer = list_customers_to_assign.get(pos_customer).get_request_customer();

				if (list_depots_ordered.get(pos_customer).isEmpty())
					throw new AssignmentException("No quedan depósitos disponibles para el cliente con ID: " + id_customer);
				
				id_closest_depot = list_depots_ordered.get(pos_customer).get(0).intValue();
				capacity_depot = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(id_closest_depot));

				pos_cluster = find_cluster(id_closest_depot, list_clusters);
				if (pos_cluster == -1)
					throw new ClusterException("No se encontró el clúster asociado al depósito más cercano.");
				
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
								list_urgencies.set(pos_customer, get_urgency(id_customer, list_id_depots.get(0), urgency_matrix));
						}
						else
						{							
							int pos_depot = Problem.get_problem().find_pos_element(list_id_depots.get(0), id_closest_depot);
							int current_pos_depot = -1;
							int total_available_depots = 0;

							for(int i = 0; i < list_depots_ordered.size(); i++)
							{ 
								if (!list_depots_ordered.get(i).contains(id_closest_depot)) 
									continue;
								
								current_pos_depot = Problem.get_problem().find_pos_element(list_depots_ordered.get(i), id_closest_depot);

								if(current_pos_depot != -1)
								{
									total_available_depots = list_depots_ordered.get(i).size();

									if(total_available_depots == 1)
									{
										solution.get_unassigned_items().add(list_customers_to_assign.get(i).get_id_customer());

										list_customers_to_assign.remove(i);
										list_urgencies.remove(i);
										list_depots_ordered.remove(i);
									}
									else
									{
										if(current_pos_depot == 0)
											list_urgencies.set(i, get_urgency(list_customers_to_assign.get(i).get_id_customer(), list_id_depots.get(0), urgency_matrix));
										else 
										{
											if(current_pos_depot == 1)
											{
												double current_urgency = list_urgencies.get(i); 
												int pos_element = Problem.get_problem().get_pos_element(list_customers_to_assign.get(i).get_id_customer());

												RowCol rc_closest_depot = urgency_matrix.indexLowerValue(total_items, pos_element, (total_items + list_id_depots.size() - 1), pos_element);
												double second_dist = urgency_matrix.getItem(rc_closest_depot.getRow(), rc_closest_depot.getCol());	
												urgency_matrix.setItem(rc_closest_depot.getRow(), rc_closest_depot.getCol(), Double.POSITIVE_INFINITY);

												rc_closest_depot = urgency_matrix.indexLowerValue(total_items, pos_element, (total_items + list_id_depots.size() - 1),  pos_element);
												double third_dist = urgency_matrix.getItem(rc_closest_depot.getRow(), rc_closest_depot.getCol());

												double first_dist = current_urgency - second_dist;
												current_urgency = calculate_urgency(first_dist, third_dist);

												list_urgencies.set(i, current_urgency);
											}
										}
										list_depots_ordered.get(i).remove(current_pos_depot);
									}									
								}
							}
							list_id_depots.get(0).remove(pos_depot);	

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
	 * @param list_customers_to_assign Lista de clientes a asignar.
	 * @param list_id_depots Lista de listas de depósitos por cliente.
	 * @param urgency_matrix Matriz de distancias.
	 * @return Lista de urgencias por cliente.
	 * @throws CostMatrixException si ocurre un error al consultar la matriz de costos.
	 */
	@Override
	public ArrayList<Double> get_list_urgencies(ArrayList<Customer> list_customers_to_assign, ArrayList<ArrayList<Integer>> list_id_depots, 
			NumericMatrix urgency_matrix) throws CostMatrixException {
		if (list_customers_to_assign == null || list_id_depots == null || urgency_matrix == null)
			throw new CostMatrixException("Alguno de los parámetros de entrada es nulo.");

		if (list_customers_to_assign.isEmpty() || list_id_depots.isEmpty())
			throw new CostMatrixException("Las listas de clientes o depósitos están vacías.");

		if (list_customers_to_assign.size() != list_id_depots.size() && list_id_depots.size() != 1)
			throw new CostMatrixException("La cantidad de clientes no coincide con la cantidad de listas de depósitos.");

		ArrayList<Double> urgencies = new ArrayList<Double>();

		for (int i = 0; i < list_customers_to_assign.size(); i++) 
		{
			int id_customer = list_customers_to_assign.get(i).get_id_customer();
			ArrayList<Integer> current_depots = (list_id_depots.size() == 1) ? list_id_depots.get(0) : list_id_depots.get(i);

			if (current_depots == null || current_depots.isEmpty())
				throw new CostMatrixException("La lista de depósitos para el cliente " + id_customer + " está vacía o es nula.");

			urgencies.add(get_urgency(id_customer, current_depots, urgency_matrix));
		}
		return urgencies;
	}

	/**
	 * Calcula la urgencia de un cliente con respecto a una lista de depósitos.
	 *
	 * @param id_customer Identificador del cliente.
	 * @param list_id_depots Lista de identificadores de depósitos.
	 * @param urgency_matrix Matriz de distancias.
	 * @return Valor de urgencia calculado.
	 * @throws CostMatrixException si ocurre un error al acceder a la matriz de costos o índices inválidos.
	 */
	@Override
	public double get_urgency(int id_customer, ArrayList<Integer> list_id_depots, NumericMatrix urgency_matrix) throws CostMatrixException {
		if (list_id_depots == null || list_id_depots.isEmpty())
			throw new CostMatrixException("La lista de depósitos está vacía o es nula para el cliente " + id_customer);

		if (urgency_matrix == null)
			throw new CostMatrixException("La matriz de urgencia es nula.");
		
		double urgency = 0.0;
		double closest_dist = 0.0;
		double other_dist = 0.0;
		RowCol rc_closet_depot = new RowCol();
		int pos_matrix_customer = -1;

		int total_customers = Problem.get_problem().get_customers().size();
		pos_matrix_customer = Problem.get_problem().get_pos_element(id_customer);
		
		if (pos_matrix_customer < 0)
			throw new CostMatrixException("Posición de cliente inválida en la matriz.");
			
		try {
			rc_closet_depot = urgency_matrix.indexLowerValue(total_customers, pos_matrix_customer, (total_customers + list_id_depots.size() - 1), pos_matrix_customer);
			closest_dist = urgency_matrix.getItem(rc_closet_depot.getRow(), rc_closet_depot.getCol());	
			urgency_matrix.setItem(rc_closet_depot.getRow(), rc_closet_depot.getCol(), Double.POSITIVE_INFINITY);

			rc_closet_depot = urgency_matrix.indexLowerValue(total_customers, pos_matrix_customer, (total_customers + list_id_depots.size() - 1),  pos_matrix_customer);
			other_dist = urgency_matrix.getItem(rc_closet_depot.getRow(), rc_closet_depot.getCol());

			if(other_dist == Double.POSITIVE_INFINITY)
				urgency = closest_dist;
			else
				urgency = calculate_urgency(closest_dist, other_dist);

			return urgency;
		} catch (Exception e) {
			throw new CostMatrixException("Error al calcular la urgencia para el cliente " + id_customer, e);
		}
	}
}