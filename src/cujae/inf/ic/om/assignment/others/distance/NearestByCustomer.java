package cujae.inf.ic.om.assignment.others.distance;

import cujae.inf.ic.om.assignment.classical.AbstractByNotUrgency;

import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.CostMatrixException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;

import cujae.inf.ic.om.service.OSRMService;

import cujae.inf.ic.om.matrix.NumericMatrix;
import cujae.inf.ic.om.matrix.RowCol;

import java.util.ArrayList;

/*Clase que modela como asignar los clientes a los depósitos considerando el orden de la lista de clientes y se parte del criterio de cercanía a los depósitos*/
public class NearestByCustomer extends AbstractByNotUrgency {
	private Solution solution = new Solution();	
	
	private ArrayList<Cluster> list_clusters; 
	private ArrayList<Customer> list_customers_to_assign;
	private NumericMatrix cost_matrix;
	
	public NearestByCustomer() {
		super();
	}

	@Override
	public Solution to_clustering() throws AssignmentException {
		try {
			initialize();
			assign();
			return finish();
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la ejecución de la heurística Nearest by Customer.", e);
		}
	}
	
	@Override
	public void initialize() throws ClusterException, CostMatrixException, AssignmentException {
		try {
			list_clusters = initialize_clusters(); 
			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("La lista de clústeres no pudo ser inicializada o está vacía.");

			list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
			if (list_customers_to_assign == null || list_customers_to_assign.isEmpty())
				throw new AssignmentException("La lista de clientes no se pudo inicializar correctamente.");

			cost_matrix = initialize_cost_matrix(list_customers_to_assign, Problem.get_problem().get_depots(), distance_type);
			if (cost_matrix == null)
				throw new CostMatrixException("La matriz de costos no se generó correctamente.");

		} catch (ClusterException | CostMatrixException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de inicialización.", e);
		}
	}

	@Override
	public void assign() throws AssignmentException {
		try {
			if (list_customers_to_assign == null || list_customers_to_assign.isEmpty())
				throw new AssignmentException("La lista de clientes está vacía. Asegúrese de llamar a initialize().");

			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("No se han inicializado clústeres.");

			if (cost_matrix == null)
				throw new CostMatrixException("La matriz de costos no está disponible.");

			int id_depot = -1;
			int pos_depot = -1;
			double capacity_depot = 0.0;

			int pos_customer = 0;
			double request_customer = 0.0;

			int pos_cluster = -1;
			double request_cluster = 0.0;

			RowCol rc_best_depot = null;
			int count_try = 0;  

			int total_customers = list_customers_to_assign.size();
			int total_depots = Problem.get_problem().get_depots().size();

			while((!list_customers_to_assign.isEmpty()) && (!list_clusters.isEmpty()) && (!cost_matrix.fullMatrix(total_customers, 0, (total_customers + total_depots - 1), (total_customers - 1), Double.POSITIVE_INFINITY)))
			{	
				rc_best_depot = cost_matrix.indexLowerValue(total_customers, pos_customer, (total_customers + total_depots - 1), pos_customer);
				if (rc_best_depot == null)
					throw new AssignmentException("No se encontró un depósito válido para el cliente en la posición " + pos_customer);
				
				pos_depot = (rc_best_depot.getRow() - total_customers);
				if (pos_depot < 0 || pos_depot >= total_depots)
					throw new AssignmentException("Posición del depósito fuera de rango: " + pos_depot);

				id_depot = Problem.get_problem().get_list_id_depots().get(pos_depot);
				capacity_depot = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(id_depot));

				request_customer = list_customers_to_assign.get(0).get_request_customer();

				cost_matrix.setItem(rc_best_depot.getRow(), rc_best_depot.getCol(), Double.POSITIVE_INFINITY);

				pos_cluster = find_cluster(id_depot, list_clusters);

				if(pos_cluster != -1)
				{
					request_cluster = list_clusters.get(pos_cluster).get_request_cluster();

					if(capacity_depot >= (request_cluster + request_customer)) 
					{
						request_cluster += request_customer;

						list_clusters.get(pos_cluster).set_request_cluster(request_cluster);
						list_clusters.get(pos_cluster).get_items_of_cluster().add(list_customers_to_assign.get(0).get_id_customer());

						list_customers_to_assign.remove(0);
						pos_customer++;

						if(count_try != 0)
							count_try = 0;
					} 
					else 
					{
						count_try++;

						if(count_try >= list_clusters.size())
						{
							count_try = 0;

							solution.get_unassigned_items().add(list_customers_to_assign.get(0).get_id_customer());
							list_customers_to_assign.remove(0);
							pos_customer++;
						}
					}

					if(is_full_depot(list_customers_to_assign, request_cluster, capacity_depot))
					{
						if(!(list_clusters.get(pos_cluster).get_items_of_cluster().isEmpty()))
							solution.get_clusters().add(list_clusters.remove(pos_cluster));
						else
							list_clusters.remove(pos_cluster);

						cost_matrix.fillValue(rc_best_depot.getRow(), 0, rc_best_depot.getRow(), (total_customers - 1), Double.POSITIVE_INFINITY);
					}
				}
			}
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