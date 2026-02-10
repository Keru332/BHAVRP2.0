package cujae.inf.ic.om.assignment.classical.cyclic;

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

/**
 * Implementa la heurística CyclicAssignment, donde cada depósito asigna de forma cíclica
 * el cliente más cercano al último cliente que se le asignó, manteniendo el equilibrio 
 * entre los depósitos.
 */
public class CyclicAssignment extends AbstractByNotUrgency {
	private Solution solution = new Solution();
	
	private ArrayList<Cluster> list_clusters;
	private ArrayList<Customer> list_customers_to_assign;
	private NumericMatrix cost_matrix;
	
	public CyclicAssignment() {
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
			throw new AssignmentException("Error inesperado durante la ejecución de la heurística Cyclic Assignment.", e);
		}
	}
				
	@Override
	public void initialize() throws ClusterException, CostMatrixException, AssignmentException {
		try {
			list_clusters = initialize_clusters();
			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("No se pudieron inicializar los clústeres.");

			list_customers_to_assign = new ArrayList<>(Problem.get_problem().get_customers());
			if (list_customers_to_assign.isEmpty())
				throw new AssignmentException("La lista de clientes a asignar está vacía.");

			cost_matrix = initialize_cost_matrix(list_customers_to_assign, Problem.get_problem().get_depots(), distance_type);
			if (cost_matrix == null)
				throw new CostMatrixException("No se pudo inicializar la matriz de costos.");
		
		} catch (ClusterException | CostMatrixException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de inicialización.", e);
		}
	}
	
	@Override
	public void assign() throws ProblemException, ClusterException, AssignmentException, CostMatrixException {
		try {
			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("No hay clústeres disponibles para asignar.");

			if (list_customers_to_assign == null || list_customers_to_assign.isEmpty())
				throw new AssignmentException("No hay clientes disponibles para asignar.");

			int pos_element_matrix = -1;
			double capacity_depot = 0.0;	

			int id_customer = -1;
			double request_customer = 0.0;

			int pos_cluster = -1;
			double request_cluster = 0.0; 

			RowCol rc_best_element = null;
			int j = 0;
			boolean is_next = true;

			int total_clusters = Problem.get_problem().get_depots().size();
			int total_items = list_customers_to_assign.size();

			ArrayList<Integer> items_selected = new ArrayList<Integer>();

			for (int i = 0; i < total_clusters; i++) 
				items_selected.add((total_items + i));

			while((!list_customers_to_assign.isEmpty()) && (!list_clusters.isEmpty()))
			{
				if(is_next)
				{
					if (j >= list_clusters.size())
						throw new AssignmentException("Índice de clúster fuera de rango.");

					capacity_depot = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(list_clusters.get(j).get_id_cluster()));
					pos_element_matrix = items_selected.get(j);
					pos_cluster = j;
				}

				rc_best_element = cost_matrix.indexLowerValue(pos_element_matrix, 0, pos_element_matrix, (total_items - 1));

				id_customer = Problem.get_problem().get_customers().get(rc_best_element.getCol()).get_id_customer(); 
				request_customer = Problem.get_problem().get_customers().get(rc_best_element.getCol()).get_request_customer();

				if(pos_cluster != -1)
				{
					request_cluster = list_clusters.get(pos_cluster).get_request_cluster();

					if(capacity_depot >= request_cluster + request_customer) 
					{	
						request_cluster += request_customer;

						list_clusters.get(pos_cluster).set_request_cluster(request_cluster);
						list_clusters.get(pos_cluster).get_items_of_cluster().add(id_customer);

						list_customers_to_assign.remove(Problem.get_problem().find_pos_customer(list_customers_to_assign, id_customer));

						cost_matrix.fillValue(0, rc_best_element.getCol(), (total_items  + total_clusters - 1), rc_best_element.getCol(), Double.POSITIVE_INFINITY);
						cost_matrix.fillValue(pos_element_matrix, 0, pos_element_matrix, (total_items - 1), Double.POSITIVE_INFINITY);

						if(is_full_depot(list_customers_to_assign, request_cluster, capacity_depot))
						{
							if(!(list_clusters.get(pos_cluster).get_items_of_cluster().isEmpty()))
								solution.get_clusters().add(list_clusters.remove(pos_cluster));
							else
								list_clusters.remove(pos_cluster);

							items_selected.remove(j);	
						}
						else 
							j++;

						is_next = true;
					} 
					else 
					{
						is_next = false;
						cost_matrix.setItem(pos_element_matrix, rc_best_element.getCol(), Double.POSITIVE_INFINITY);

						if(is_full_depot(list_customers_to_assign, request_cluster, capacity_depot))
						{
							if(!(list_clusters.get(pos_cluster).get_items_of_cluster().isEmpty()))
								solution.get_clusters().add(list_clusters.remove(pos_cluster));
							else
								list_clusters.remove(pos_cluster);

							items_selected.remove(j);	
							is_next = true;
						}
						else
						{  // codigo repetido puede optimizarse revisar
							if(cost_matrix.fullMatrix(pos_element_matrix, 0, pos_element_matrix, (total_items - 1), Double.POSITIVE_INFINITY))
							{
								if(!(list_clusters.get(pos_cluster).get_items_of_cluster().isEmpty()))
									solution.get_clusters().add(list_clusters.remove(pos_cluster));
								else
									list_clusters.remove(pos_cluster);

								list_clusters.remove(pos_cluster);
								items_selected.remove(j);
								is_next = true;
							}
						}	
					}
					if (j == list_clusters.size())
					{
						if((!list_customers_to_assign.isEmpty()) && (!list_clusters.isEmpty()))
						{
							j = 0;
							items_selected.clear();

							for(int k = 0; k < list_clusters.size(); k++) 
							{
								if(!list_clusters.get(k).get_items_of_cluster().isEmpty())
									pos_element_matrix = Problem.get_problem().get_pos_element(list_clusters.get(k).get_items_of_cluster().get(list_clusters.get(k).get_items_of_cluster().size() - 1));	

								items_selected.add(pos_element_matrix);
							}
							is_next = true;
						}	
					}
				}
			}
		} catch (ProblemException | ClusterException | CostMatrixException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de asignación.", e);
		}
	}
	
	@Override
	public Solution finish() throws AssignmentException {	
		try {
			if(!list_customers_to_assign.isEmpty())					
				for(int i = 0; i < list_customers_to_assign.size(); i++)	
					solution.get_unassigned_items().add(list_customers_to_assign.get(i).get_id_customer());

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