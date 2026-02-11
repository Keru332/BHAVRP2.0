package cujae.inf.ic.om.assignment.clustering.hierarchical;

import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.CostMatrixException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Location;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;

import cujae.inf.ic.om.service.OSRMService;

import cujae.inf.ic.om.factory.DistanceType;

import cujae.inf.ic.om.matrix.NumericMatrix;
import cujae.inf.ic.om.matrix.RowCol;

import java.util.ArrayList;

public class UPGMC extends AbstractHierarchical {
	public static DistanceType distance_type = DistanceType.Euclidean;
	private static Solution solution = new Solution();
	
	private ArrayList<Integer> list_id_elements;
	private ArrayList<Cluster> list_clusters;
	private ArrayList<Customer> list_customers_to_assign;
	private ArrayList<Depot> list_depots;
	private NumericMatrix cost_matrix;
	
	public UPGMC() {
		super();
	}

	@Override
	public Solution to_clustering() throws ProblemException, ClusterException, CostMatrixException, AssignmentException {
		try {
			initialize();
			assign();
			return finish();
		} catch (ClusterException | CostMatrixException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la ejecuci�n de la adaptaci�n del algoritmo UPGMC.", e);
		}
	}
			
	@Override
	public void initialize() throws ClusterException, ProblemException, AssignmentException {
		try {
			list_id_elements = Problem.get_problem().get_list_id_elements();
			if (list_id_elements == null || list_id_elements.isEmpty())
				throw new AssignmentException("No se encontraron elementos iniciales para los cl�steres.");

			list_clusters = initialize_clusters(list_id_elements);
			if (list_clusters == null || list_clusters.isEmpty())
				throw new ClusterException("No se pudieron inicializar los cl�steres a partir de los elementos proporcionados.");

			list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
			if (list_customers_to_assign.isEmpty())
				throw new AssignmentException("La lista de clientes est� vac�a.");

			list_depots = new ArrayList<Depot>(Problem.get_problem().get_depots());
			if (list_depots.isEmpty())
				throw new AssignmentException("La lista de dep�sitos est� vac�a.");
			
		} catch (ClusterException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de inicializaci�n.", e);
		}
	}
	
	@Override
	public void assign() throws CostMatrixException, AssignmentException {
		try {
			int totalDepots = list_depots.size();
			
			int currentDepots =  -1;
			int currentCustomers = -1;

			int posCol = -1;
			int posRow = -1;

			int idCustomerOne = -1;
			int posCustomerOne = -1;
			int idCustomerTwo = -1;
			int posCustomerTwo = -1;

			int posClusterOne = -1;
			double requestClusterOne = 0.0;
			int posClusterTwo = -1;
			double requestClusterTwo = 0.0;

			int idDepot = -1;
			int pos_depot = -1;		
			int posDepotMatrix = -1;
			double capacityDepot = 0.0;

			int pos_cluster = -1;
			double request_cluster = 0.0;
			double totalRequest = 0.0;

			int id_depot_with_mu = -1;
			double capacity_depot_with_mu = 0.0;
			RowCol rcBestAll = new RowCol();
			Location newLocation = new Location();
			
			boolean change = true;
			
			while(!(list_customers_to_assign.isEmpty()) && (totalDepots > 0))
			{
				currentDepots = list_depots.size();
				currentCustomers = list_customers_to_assign.size();

				System.out.println("Current Depots: " + currentDepots);
				System.out.println("Current Customers: " + currentCustomers);
				
				if(change)
				{
					try {
						cost_matrix = initialize_cost_matrix(list_customers_to_assign, list_depots, distance_type);
					} catch (IllegalArgumentException | SecurityException e) {
						e.printStackTrace();
					}
				}
				
				rcBestAll = cost_matrix.indexLowerValue(0, 0, (currentCustomers + currentDepots - 1), (currentCustomers - 1));
				
				System.out.println("rcBestAll " + cost_matrix.getItem(rcBestAll.getRow(), rcBestAll.getCol()));
				System.out.println("bestAllRow" + rcBestAll.getRow());
				System.out.println("bestAllCol" + rcBestAll.getCol());
				
				posCol = rcBestAll.getCol();
				posRow = rcBestAll.getRow();

				if((posCol < currentCustomers) && (posRow < currentCustomers)) 
				{ 
					posCustomerOne = posCol;
					idCustomerOne = list_customers_to_assign.get(posCustomerOne).get_id_customer();

					System.out.println("--------------------------------------");
					System.out.println("ID Customer One: " + idCustomerOne);
					
					posCustomerTwo = posRow;
					idCustomerTwo = list_customers_to_assign.get(posCustomerTwo).get_id_customer();				

					System.out.println("idCustomerTwo" + idCustomerTwo);
					
					posClusterOne = find_cluster(idCustomerOne, list_clusters);	
					posClusterTwo = find_cluster(idCustomerTwo, list_clusters);
					
					System.out.println("--------------------------------------");
					System.out.println("Position Cluster One: " + posClusterOne);
					System.out.println("Position Cluster Two: " + posClusterTwo);
					
					if((posClusterOne != -1) && (posClusterTwo != -1)) 
					{
						requestClusterOne = list_clusters.get(posClusterOne).get_request_cluster();
						requestClusterTwo = list_clusters.get(posClusterTwo).get_request_cluster();
						totalRequest = requestClusterOne + requestClusterTwo;
						
						System.out.println("--------------------------------------");
						System.out.println("Request Cluster One: " + requestClusterOne);
						System.out.println("Request Cluster Two: " + requestClusterTwo);
						System.out.println("Total Request: " + totalRequest);
						
						id_depot_with_mu = get_id_cluster_with_mu(list_depots, list_clusters);
						pos_depot = Problem.get_problem().find_pos_depot(list_depots, id_depot_with_mu);
						capacity_depot_with_mu = Problem.get_problem().get_total_capacity_by_depot(list_depots.get(pos_depot));
						pos_cluster = find_cluster(id_depot_with_mu, list_clusters);
						request_cluster = list_clusters.get(pos_cluster).get_request_cluster();

						System.out.println("--------------------------------------");
						System.out.println("ID Depot With MU: " + id_depot_with_mu);
						System.out.println("Position Depot: " + pos_depot);
						System.out.println("Capacity Depot With MU: " + capacity_depot_with_mu);
						System.out.println("Position Cluster: " + pos_cluster);
						System.out.println("Request Cluster: " + request_cluster);
						
						if(capacity_depot_with_mu >= (request_cluster + totalRequest)) 
						{
							list_clusters.get(posClusterOne).set_request_cluster(totalRequest);

							for (int i = 0; i < list_clusters.get(posClusterTwo).get_items_of_cluster().size(); i++)
								list_clusters.get(posClusterOne).get_items_of_cluster().add(list_clusters.get(posClusterTwo).get_items_of_cluster().get(i));			

							newLocation = recalculate_centroid(list_clusters.get(posClusterOne));
							
							System.out.println("--------------------------------------");
							System.out.println("New Location:");
							System.out.println("Axis X: " + newLocation.get_axis_x());
							System.out.println("Axis Y: " + newLocation.get_axis_y());
							
							posCustomerOne = Problem.get_problem().find_pos_customer(list_customers_to_assign, idCustomerOne);
							list_customers_to_assign.get(posCustomerOne).set_location_customer(newLocation);
							
							System.out.println("--------------------------------------");
							System.out.println("New Location:");
							System.out.println("Axis X: " + newLocation.get_axis_x());
							System.out.println("Axis Y:" + newLocation.get_axis_y());
							System.out.println("Position Customer One: " + posCustomerOne);
							
							list_clusters.remove(posClusterTwo);
							posCustomerTwo = Problem.get_problem().find_pos_customer(list_customers_to_assign, idCustomerTwo);
							list_customers_to_assign.remove(posCustomerTwo);
							
							System.out.println("--------------------------------------");
							System.out.println("List Clusters: " + list_clusters.size());
							System.out.println("Position Customer Two: " + posCustomerTwo);
							System.out.println("List Customers To Assign: " + list_customers_to_assign.size());
							
							change = true;
						}
						else
						{
							cost_matrix.setItem(rcBestAll.getRow(), rcBestAll.getCol(), Double.POSITIVE_INFINITY);
							change = false;
						}
					}	
				}
				else
				{				
					if(((posCol < currentCustomers) && (posRow >= currentCustomers)) || ((posRow < currentCustomers) && (posCol >= currentCustomers)))
					{
						if(posCol < currentCustomers)
						{
							posCustomerOne = rcBestAll.getCol();
							posDepotMatrix = rcBestAll.getRow();
						}
						else
						{
							posCustomerOne = rcBestAll.getRow();
							posDepotMatrix = rcBestAll.getCol();
						}
						
						System.out.println("--------------------------------------");
						System.out.println("Position Customer One: " + posCustomerOne);
						System.out.println("Position Depot Matrix: " + posDepotMatrix);

						idCustomerOne = list_customers_to_assign.get(posCustomerOne).get_id_customer();			
						posClusterOne = find_cluster(idCustomerOne, list_clusters);
						
						System.out.println("--------------------------------------");
						System.out.println("ID Customer One: " + idCustomerOne);
						System.out.println("Position Cluster One: " + posClusterOne);

						pos_depot = (posDepotMatrix - currentCustomers); 
						idDepot = list_depots.get(pos_depot).get_id_depot();
						capacityDepot = Problem.get_problem().get_total_capacity_by_depot(list_depots.get(pos_depot));
						pos_cluster = find_cluster(idDepot, list_clusters);

						System.out.println("--------------------------------------");
						System.out.println("Position Depot: " + pos_depot);
						System.out.println("ID Depot: " + idDepot);
						System.out.println("Capacity Depot: " + capacityDepot);
						System.out.println("Position Cluster: " + pos_cluster);
						
						if((posClusterOne != -1) && (pos_cluster != -1))
						{
							requestClusterOne = list_clusters.get(posClusterOne).get_request_cluster();
							request_cluster = list_clusters.get(pos_cluster).get_request_cluster();

							System.out.println("--------------------------------------");
							System.out.println("Request Cluster One: " + requestClusterOne);
							System.out.println("Request Cluster: " + request_cluster);
							
							if(capacityDepot >= (request_cluster + requestClusterOne)) 
							{
								request_cluster += requestClusterOne;
								list_clusters.get(pos_cluster).set_request_cluster(request_cluster);

								for (int i = 0; i < list_clusters.get(posClusterOne).get_items_of_cluster().size(); i++)
									list_clusters.get(pos_cluster).get_items_of_cluster().add(list_clusters.get(posClusterOne).get_items_of_cluster().get(i));
						
								list_customers_to_assign.remove(Problem.get_problem().find_pos_customer(list_customers_to_assign, idCustomerOne));
								
								list_clusters.remove(posClusterOne);
								change = true;
							}
							else
							{
								cost_matrix.setItem(rcBestAll.getRow(), rcBestAll.getCol(), Double.POSITIVE_INFINITY);
								change = false;		
							}
							
							//PENDIENTE ENTRADA CON LISTCUSTOMER VACIA EN EL ISFULLDEPOT TRADICIONAL
							if(is_full_depot(list_clusters, request_cluster, capacityDepot, list_customers_to_assign.size()))
							{
								pos_cluster = find_cluster(idDepot, list_clusters);
								
								System.out.println("--------------------------------------");
								System.out.println("Position Cluster: " + pos_cluster);
								
								if(!(list_clusters.get(pos_cluster).get_items_of_cluster().isEmpty()))
									solution.get_clusters().add(list_clusters.remove(pos_cluster));
								else
									list_clusters.remove(pos_cluster);

								list_depots.remove(pos_depot);
								totalDepots--;
								change = true;
							}
						}
					}
				}
			}
		} catch (CostMatrixException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de asignaci�n.", e);
		}
	}
	
	@Override
	public Solution finish() throws AssignmentException {	
		try {
			finish(list_clusters, solution);

			if(!list_clusters.isEmpty())
				for(int k = 0; k < list_clusters.size(); k++)
					if(!(list_clusters.get(k).get_items_of_cluster().isEmpty()))
						solution.get_clusters().add(list_clusters.get(k));

			OSRMService.clear_distance_cache();

			return solution;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de finalizaci�n.", e);
		}
	}
	
	/**
	 * Obtiene el identificador del dep�sito con la mayor capacidad restante disponible,
	 * considerando la demanda ya cubierta en su cl�ster correspondiente.
	 *
	 * @param depots Lista de dep�sitos disponibles.
	 * @param clusters Lista de cl�steres con demanda acumulada.
	 * @return ID del dep�sito con mayor capacidad restante.
	 * @throws AssignmentException si las listas est�n vac�as, nulas o inconsistentes.
	 */
	private int get_id_cluster_with_mu(ArrayList<Depot> depots, ArrayList<Cluster> clusters) 
			throws AssignmentException {
		if (depots == null || depots.isEmpty()) 
			throw new AssignmentException("La lista de dep�sitos est� vac�a o no ha sido inicializada.");

		if (clusters == null || clusters.isEmpty()) 
			throw new AssignmentException("La lista de cl�steres est� vac�a o no ha sido inicializada.");

		try {
			int id_depot_mu = depots.get(0).get_id_depot();
			int pos_cluster = find_cluster(id_depot_mu, clusters);

			if (pos_cluster == -1)
				throw new AssignmentException("No se encontr� el cl�ster correspondiente al dep�sito " + id_depot_mu);

			double request_cluster = clusters.get(pos_cluster).get_request_cluster();
			double max_capacity_depot = Problem.get_problem().get_total_capacity_by_depot(depots.get(0)); 
			max_capacity_depot -= request_cluster;
			int total_depots = depots.size();

			double current_capacity_depot; 

			for(int i = 1; i < total_depots; i++)
			{
				pos_cluster = find_cluster(depots.get(i).get_id_depot(), clusters);
				request_cluster = clusters.get(pos_cluster).get_request_cluster();
				current_capacity_depot = Problem.get_problem().get_total_capacity_by_depot(depots.get(i));
				current_capacity_depot -= request_cluster;

				if(max_capacity_depot < current_capacity_depot)
				{
					max_capacity_depot = current_capacity_depot;
					id_depot_mu = depots.get(i).get_id_depot(); 
				}
			}
			return id_depot_mu;
		} catch (Exception e) {
			throw new AssignmentException("Error al determinar el dep�sito con mayor capacidad restante.", e);
		}
	}
	
	/**
	 * Completa la construcci�n de la soluci�n, identificando elementos que permanecen sin asignaci�n v�lida.
	 *
	 * @param clusters Lista de cl�steres generados durante la asignaci�n.
	 * @param solution Soluci�n parcial que se actualizar� con clientes no asignados.
	 * @throws ProblemException si ocurre un error al consultar la estructura del problema.
	 */
	private void finish(ArrayList<Cluster> clusters, Solution solution) throws AssignmentException {
		if (clusters == null || solution == null) 
			throw new AssignmentException("Las estructuras de cl�ster o soluci�n no est�n correctamente inicializadas.");
		
		try {
			ArrayList<Cluster> clusters_to_remove = new ArrayList<Cluster>();
			ArrayList<Customer> customers = Problem.get_problem().get_customers();

			for (int i = 0; i < clusters.size(); i++) 
			{
				Cluster cluster = clusters.get(i);
				int id_cluster = cluster.get_id_cluster();

				System.out.println("--------------------------------------");
				System.out.println("ID Cluster: " + id_cluster);
				System.out.println("Elementos del Cluster: " + cluster.get_items_of_cluster());
				System.out.println("Demanda del Cluster: " + cluster.get_request_cluster());

				boolean is_invalid = false;

				// B�squeda manual para evitar excepci�n si el cliente no est�
				for (Customer c : customers) 
				{
					if (c.get_id_customer() == id_cluster) 
					{
						is_invalid = true;
						break;
					}
				}
				if (is_invalid) 
				{
					for (Integer id : cluster.get_items_of_cluster())
						solution.get_unassigned_items().add(id);

					clusters_to_remove.add(cluster);
				}
			}
			clusters.removeAll(clusters_to_remove);
			
		} catch (Exception e) {
			throw new AssignmentException("Error durante el proceso de finalizaci�n de la soluci�n.", e);
		}
	}
}