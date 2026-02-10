package cujae.inf.ic.om.assignment.others.basedcyclic;

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
import cujae.inf.ic.om.matrix.RowCol;

import java.util.ArrayList;

/*Clase que modela como asignar elementos en forma secuencial por depósitos*/
public class SequentialCyclic extends AbstractByNotUrgency {
	private Solution solution = new Solution();
		
	private ArrayList<Cluster> list_clusters;
	private ArrayList<Customer> list_customers_to_assign;
	private ArrayList<Integer> list_id_depots;
	private NumericMatrix cost_matrix;
	
	public SequentialCyclic() {
		super();
	}
	
	@Override
	public Solution to_clustering() throws AssignmentException, CostMatrixException, ProblemException, ClusterException {
		try {
			initialize();
			assign();
			return finish();
		} catch (ClusterException | CostMatrixException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la ejecución de la heurística Sequential Cyclic.", e);
		}
	}
	
	@Override
	public void initialize() throws CostMatrixException, ClusterException, AssignmentException {
		try {
			list_clusters = initialize_clusters();
			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("Los clústeres no fueron inicializados correctamente.");

			list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
			if (list_customers_to_assign == null || list_customers_to_assign.isEmpty())
				throw new AssignmentException("La lista de clientes está vacía.");

			list_id_depots = new ArrayList<Integer>(Problem.get_problem().get_list_id_depots());
			if (list_id_depots == null || list_id_depots.isEmpty())
				throw new AssignmentException("La lista de depósitos está vacía.");

			cost_matrix = initialize_cost_matrix(list_customers_to_assign, Problem.get_problem().get_depots(), distance_type);
			if (cost_matrix == null)
				throw new CostMatrixException("La matriz de costos no se pudo inicializar.");

		} catch (ClusterException | CostMatrixException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de inicialización.", e);
		}
	}
		
	@Override
	public void assign() throws AssignmentException, CostMatrixException, ProblemException, ClusterException {
		try {
			if (list_customers_to_assign == null || list_customers_to_assign.isEmpty())
				throw new AssignmentException("No hay clientes para asignar.");

			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("No hay clústeres disponibles para realizar la asignación.");

			if (list_id_depots == null || list_id_depots.isEmpty())
				throw new AssignmentException("No hay depósitos disponibles.");

			if (cost_matrix == null)
				throw new CostMatrixException("Matriz de costos no inicializada.");

			int pos_element_matrix = -1;

			double capacity_depot = 0.0;

			int id_customer = -1;
			double request_customer = 0.0;

			int pos_cluster = -1;
			double request_cluster = 0.0;

			RowCol rc_best_element = null;

			int i = 0;
			int count_try = 0;

			boolean is_full = false;

			int total_items = list_customers_to_assign.size();
			int total_clusters = list_id_depots.size();

			while((!list_customers_to_assign.isEmpty()) && (!list_clusters.isEmpty()))
			{
				pos_element_matrix = total_items + i;
				capacity_depot = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(list_id_depots.get(0)));	
				pos_cluster = 0;

				if(pos_cluster != -1)
				{
					request_cluster = list_clusters.get(pos_cluster).get_request_cluster();

					while(!is_full) 
					{
						rc_best_element = cost_matrix.indexLowerValue(pos_element_matrix, 0, pos_element_matrix, (total_items - 1));

						id_customer = Problem.get_problem().get_customers().get(rc_best_element.getCol()).get_id_customer();
						request_customer = Problem.get_problem().get_customers().get(rc_best_element.getCol()).get_request_customer();

						if(capacity_depot >= (request_cluster + request_customer))
						{	
							request_cluster += request_customer;

							list_clusters.get(pos_cluster).set_request_cluster(request_cluster);
							list_clusters.get(pos_cluster).get_items_of_cluster().add(id_customer);

							try {
							    int pos_to_remove = Problem.get_problem().find_pos_customer(list_customers_to_assign, id_customer);
							    list_customers_to_assign.remove(pos_to_remove);

							    cost_matrix.fillValue(rc_best_element.getRow(), 0, rc_best_element.getRow(), (total_items - 1), Double.POSITIVE_INFINITY);
							    cost_matrix.fillValue(0, rc_best_element.getCol(), (total_items + total_clusters - 1), rc_best_element.getCol(), Double.POSITIVE_INFINITY);
							} catch (ProblemException e) {
							    // Cliente ya no está en la lista, solo invalida esa posición y continúa
							    cost_matrix.setItem(rc_best_element.getRow(), rc_best_element.getCol(), Double.POSITIVE_INFINITY);
							    continue;
							}
							
							cost_matrix.fillValue(rc_best_element.getRow(), 0, rc_best_element.getRow(), (total_items - 1), Double.POSITIVE_INFINITY);
							cost_matrix.fillValue(0, rc_best_element.getCol(), (total_items + total_clusters - 1), rc_best_element.getCol(), Double.POSITIVE_INFINITY);

							pos_element_matrix = Problem.get_problem().get_pos_element(list_clusters.get(pos_cluster).get_items_of_cluster().get(list_clusters.get(pos_cluster).get_items_of_cluster().size() - 1));

							if(is_full_depot(list_customers_to_assign, request_cluster, capacity_depot))
							{
								if(!(list_clusters.get(pos_cluster).get_items_of_cluster().isEmpty()))
									solution.get_clusters().add(list_clusters.remove(pos_cluster));
								else
									list_clusters.remove(pos_cluster);

								is_full = true;
								list_id_depots.remove(0);
							}
						} 
						else 
						{				
							cost_matrix.setItem(rc_best_element.getRow(), rc_best_element.getCol(), Double.POSITIVE_INFINITY);
							count_try++;

							if((count_try >= list_customers_to_assign.size()))
							{
								if(!list_clusters.get(pos_cluster).get_items_of_cluster().isEmpty())
									solution.get_clusters().add(list_clusters.get(pos_cluster));

								list_clusters.remove(pos_cluster);

								list_id_depots.remove(0);
								is_full = true;
							}
						}
					}
					is_full = false;
					count_try = 0;	
					i++;
				}		
			}
		} catch (ClusterException | CostMatrixException | ProblemException e) {
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
}