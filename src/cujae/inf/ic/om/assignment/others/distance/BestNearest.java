package cujae.inf.ic.om.assignment.others.distance;

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

/*Clase que modela como asignar los clientes a los depósitos partiendo del criterio de ser los mejores candidatos*/
public class BestNearest extends AbstractByNotUrgency {

	private Solution solution = new Solution();
	
	private ArrayList<Cluster> list_clusters;
	private ArrayList<Customer> list_customers_to_assign;
	private ArrayList<Integer> list_id_depots;
	private NumericMatrix cost_matrix;
	
    public BestNearest() {
        super();
    }
    
    @Override
    public Solution to_clustering() throws AssignmentException, ProblemException, ClusterException, CostMatrixException {
		try {
			initialize();
			assign();
			return finish();
		} catch (ClusterException | CostMatrixException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la ejecución de la heurística Best Nearest.", e);
		}
    }
	
    @Override
    public void initialize() throws CostMatrixException, ClusterException, AssignmentException {
    	try {
    		list_clusters = initialize_clusters();	
    		if (list_clusters == null || list_clusters.isEmpty())
    			throw new AssignmentException("No se pudieron inicializar los clústeres.");

    		list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
    		if (list_customers_to_assign == null || list_customers_to_assign.isEmpty())
    			throw new AssignmentException("La lista de clientes está vacía.");

    		list_id_depots = new ArrayList<Integer>(Problem.get_problem().get_list_id_depots());  
    		if (list_id_depots == null || list_id_depots.isEmpty())
    			throw new AssignmentException("La lista de depósitos está vacía.");

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
	public void assign() throws AssignmentException, ProblemException, ClusterException {
		try {
			if (list_customers_to_assign == null || list_customers_to_assign.isEmpty())
				throw new AssignmentException("No hay clientes disponibles para asignar.");

			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("No hay clústeres disponibles para realizar la asignación.");

			if (cost_matrix == null)
				throw new CostMatrixException("La matriz de costos no fue inicializada correctamente.");

			int id_customer = -1;
			int pos_customer = -1;
			double request_customer = 0.0;

			int id_depot = -1;
			int pos_depot = -1;
			double capacity_depot = 0.0;

			int pos_cluster = -1; 
			double request_cluster = 0.0;

			RowCol rc_best_all = null;

			int total_customers = list_customers_to_assign.size();
			int total_depots = list_id_depots.size(); 

			while((!list_customers_to_assign.isEmpty()) && (!list_clusters.isEmpty()) && (!cost_matrix.fullMatrix(total_customers, 0, (total_customers + total_depots - 1), (total_customers - 1), Double.POSITIVE_INFINITY))) 
			{					
				rc_best_all = cost_matrix.indexLowerValue(total_customers, 0, (total_customers + total_depots - 1), (total_customers - 1));

				pos_depot = rc_best_all.getRow() - total_customers;
				id_depot = list_id_depots.get(pos_depot);      	
				capacity_depot = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(id_depot));

				pos_customer = rc_best_all.getCol();
				id_customer = Problem.get_problem().get_customers().get(pos_customer).get_id_customer();
				request_customer = Problem.get_problem().get_customers().get(pos_customer).get_request_customer();

				pos_cluster = find_cluster(id_depot, list_clusters);

				if(pos_cluster != -1)
				{
					request_cluster = list_clusters.get(pos_cluster).get_request_cluster();

					if(capacity_depot >= (request_cluster + request_customer)) 
					{
						request_cluster += request_customer;

						list_clusters.get(pos_cluster).set_request_cluster(request_cluster);
						list_clusters.get(pos_cluster).get_items_of_cluster().add(id_customer);

						cost_matrix.fillValue(total_customers, pos_customer, (total_customers + total_depots - 1), pos_customer, Double.POSITIVE_INFINITY);
						list_customers_to_assign.remove(Problem.get_problem().find_pos_customer(list_customers_to_assign, id_customer));
					} 
					else
						cost_matrix.setItem(rc_best_all.getRow(), pos_customer, Double.POSITIVE_INFINITY);

					if(is_full_depot(list_customers_to_assign, request_cluster, capacity_depot))
					{
						if(!(list_clusters.get(pos_cluster).get_items_of_cluster().isEmpty()))
							solution.get_clusters().add(list_clusters.remove(pos_cluster));
						else
							list_clusters.remove(pos_cluster);

						cost_matrix.fillValue(rc_best_all.getRow(), 0, rc_best_all.getRow(), (total_customers + total_depots - 1), Double.POSITIVE_INFINITY);
					}
				}
			}
		} catch (ClusterException | ProblemException e) {
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