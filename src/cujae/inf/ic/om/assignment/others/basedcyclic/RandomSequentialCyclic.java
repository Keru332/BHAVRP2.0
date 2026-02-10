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
import java.util.Random;

/*Clase que modela como asignar clientes a los depósitos dn forma secuencial por depósitos escogiendo el depósito al azar*/
public class RandomSequentialCyclic extends AbstractByNotUrgency {
	Random random = new Random();

	private Solution solution = new Solution();	
	
	private ArrayList<Cluster> list_clusters;
	private ArrayList<Customer> list_customers_to_assign;
	private ArrayList<Integer> list_id_depots;
	private NumericMatrix cost_matrix;
		
	public RandomSequentialCyclic() {
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
			throw new AssignmentException("Error inesperado durante la ejecución de la heurística Random Sequential Cyclic.", e);
		}
	}
	
	@Override
	public void initialize() throws CostMatrixException, ClusterException, AssignmentException {
		try {
			list_clusters = initialize_clusters();
			if (list_clusters == null || list_clusters.isEmpty()) 
				throw new AssignmentException("No se pudo inicializar la lista de clústeres.");
			
			list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
			if (list_customers_to_assign.isEmpty()) 
				throw new AssignmentException("No hay clientes disponibles para asignar.");
			
			list_id_depots = new ArrayList<Integer>(Problem.get_problem().get_list_id_depots());
			if (list_id_depots.isEmpty()) 
				throw new AssignmentException("No se encontraron depósitos disponibles.");
			
			cost_matrix = initialize_cost_matrix(list_customers_to_assign, Problem.get_problem().get_depots(), distance_type);
			if (cost_matrix == null) 
				throw new AssignmentException("La matriz de costos no se inicializó correctamente.");
			
		} catch (ClusterException | CostMatrixException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de inicialización.", e);
		}
	}	
	
	@Override
	public void assign() throws AssignmentException, CostMatrixException, ProblemException, ClusterException {
		try {
			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("La lista de clústeres está vacía.");
			
			if (list_customers_to_assign == null || list_customers_to_assign.isEmpty())
				throw new AssignmentException("No hay clientes disponibles para asignar.");
			
			if (list_id_depots == null || list_id_depots.isEmpty())
				throw new AssignmentException("No hay depósitos disponibles.");
			
			if (cost_matrix == null)
				throw new AssignmentException("La matriz de costos no fue inicializada.");

			int pos_element_matrix = -1;

			int id_customer = -1;
			double request_customer = 0.0;

			int pos_rdm_depot = -1;
			double capacity_depot = 0.0;

			int pos_cluster = -1;
			double request_cluster = 0.0;

			RowCol rc_best_element = null;

			int count_try = 0;

			boolean is_full = false;

			int total_items = list_customers_to_assign.size();
			int total_clusters = list_id_depots.size();

			while((!list_customers_to_assign.isEmpty()) && (!list_clusters.isEmpty()))
			{
				pos_rdm_depot = random.nextInt(list_id_depots.size());
				pos_element_matrix = Problem.get_problem().get_pos_element(list_id_depots.get(pos_rdm_depot));
				capacity_depot = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(list_id_depots.get(pos_rdm_depot)));

				pos_cluster = pos_rdm_depot;

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

							list_customers_to_assign.remove(Problem.get_problem().find_pos_customer(list_customers_to_assign, id_customer));

							cost_matrix.fillValue(rc_best_element.getRow(), 0, rc_best_element.getRow(), (total_items - 1), Double.POSITIVE_INFINITY);
							cost_matrix.fillValue(0, rc_best_element.getCol(), (total_items + total_clusters - 1), rc_best_element.getCol(), Double.POSITIVE_INFINITY);

							pos_element_matrix = Problem.get_problem().get_pos_element(list_clusters.get(pos_cluster).get_items_of_cluster().get(list_clusters.get(pos_cluster).get_items_of_cluster().size() - 1));

							if(is_full_depot(list_customers_to_assign, request_cluster, capacity_depot))
							{
								if(!(list_clusters.get(pos_cluster).get_items_of_cluster().isEmpty()))
									solution.get_clusters().add(list_clusters.remove(pos_cluster));
								else
									list_clusters.remove(pos_cluster);

								list_id_depots.remove(pos_cluster);
								is_full = true;
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

								list_id_depots.remove(pos_cluster);
								is_full = true;
							}
						}
					}
					is_full = false;
					count_try = 0;				
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