package cujae.inf.ic.om.assignment.others.distance;

import cujae.inf.ic.om.assignment.classical.AbstractByNotUrgency;

import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;

import cujae.inf.ic.om.service.OSRMService;

import java.util.ArrayList;
import java.util.Random;

/*Clase que modela como asignar los clientes a los depósitos de forma aleatoria la selección de ambos*/
public class RandomByElement extends AbstractByNotUrgency {
	private Random random = new Random();
	private Solution solution = new Solution();
	
	private ArrayList<Cluster> list_clusters;
	private ArrayList<Integer> list_id_depots;
	private ArrayList<Customer> list_customers_to_assign;

	public RandomByElement() {
		super();
	}

	@Override
	public Solution to_clustering() throws AssignmentException {	
		try {
			initialize();
			assign();
			return finish();
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la ejecución de la heurística Random by Element.", e);
		}
	}

	@Override
	public void initialize() throws ClusterException, AssignmentException {
		try {
			list_clusters = initialize_clusters();
			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("Los clústeres no pudieron ser inicializados correctamente.");

			list_id_depots = Problem.get_problem().get_list_id_depots();
			if (list_id_depots == null || list_id_depots.isEmpty())
				throw new AssignmentException("La lista de depósitos no se pudo cargar correctamente.");

			list_customers_to_assign = new ArrayList<>(Problem.get_problem().get_customers());

			if (list_customers_to_assign == null || list_customers_to_assign.isEmpty())
				throw new AssignmentException("La lista de clientes a asignar no está disponible.");

		} catch (ClusterException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de inicialización.", e);
		}
	}
	
	@Override
	public void assign() throws AssignmentException, ClusterException, ProblemException {
		try {
			if (list_customers_to_assign == null || list_customers_to_assign.isEmpty())
				throw new AssignmentException("No hay clientes disponibles para asignar.");

			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("No hay clústeres disponibles.");

			if (list_id_depots == null || list_id_depots.isEmpty())
				throw new AssignmentException("No hay depósitos disponibles para la asignación.");

			while((!list_customers_to_assign.isEmpty()) && (!list_clusters.isEmpty()))
			{		
				if (list_id_depots.isEmpty())
					throw new AssignmentException("No quedan depósitos disponibles para la asignación.");

				int pos_rdm_depot = random.nextInt(list_id_depots.size());
				int id_depot = list_id_depots.get(pos_rdm_depot);
				double capacity_depot = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(id_depot));

				if (capacity_depot <= 0)
					throw new AssignmentException("El depósito seleccionado no tiene capacidad disponible.");
				
				int pos_rdm_customer = random.nextInt(list_customers_to_assign.size());
				int id_customer = list_customers_to_assign.get(pos_rdm_customer).get_id_customer();
				double request_customer = list_customers_to_assign.get(pos_rdm_customer).get_request_customer();

				int pos_cluster = find_cluster(id_depot, list_clusters);

				if(pos_cluster != -1)
				{
					double requestCluster = list_clusters.get(pos_cluster).get_request_cluster();

					if(capacity_depot >= (requestCluster + request_customer)) 
					{
						requestCluster += request_customer;

						list_clusters.get(pos_cluster).set_request_cluster(requestCluster);
						list_clusters.get(pos_cluster).get_items_of_cluster().add(id_customer);

						list_customers_to_assign.remove(pos_rdm_customer);
					}
					if(is_full_depot(list_customers_to_assign, requestCluster, capacity_depot))
					{
						list_id_depots.remove(pos_rdm_depot);

						if(!(list_clusters.get(pos_cluster).get_items_of_cluster().isEmpty()))
							solution.get_clusters().add(list_clusters.remove(pos_cluster));
						else
							list_clusters.remove(pos_cluster);
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