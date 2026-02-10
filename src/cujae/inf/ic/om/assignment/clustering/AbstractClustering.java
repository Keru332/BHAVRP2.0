package cujae.inf.ic.om.assignment.clustering;

import cujae.inf.ic.om.assignment.AbstractAssignment;

import cujae.inf.ic.om.controller.tools.AbstractTools;

import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Location;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractClustering extends AbstractAssignment {
	
	/**
	 * Inicializa los clústeres de la solución con los identificadores de clientes y depósitos proporcionados.
	 *
	 * @param list_id_elements Lista de identificadores (clientes o depósitos).
	 * @return Lista de clústeres inicializados.
	 * @throws ClusterException si la lista de elementos es nula o vacía.
	 * @throws ProblemException si ocurre un error al obtener datos del problema.
	 */
	protected ArrayList<Cluster> initialize_clusters(ArrayList<Integer> list_id_elements) 
			throws ClusterException, ProblemException {
		if (list_id_elements == null || list_id_elements.isEmpty()) 
	        throw new ClusterException("La lista de identificadores para inicializar los clústeres está vacía o es nula.");
		
		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		int total_elements = list_id_elements.size();
		ArrayList<Integer> list_id_customers;
		Cluster cluster;

		if(total_elements == Problem.get_problem().get_depots().size())
		{
			for(int i = 0; i < total_elements; i++)
			{
				list_id_customers = new ArrayList<Integer>();
				int id = list_id_elements.get(i).intValue();
	            list_id_customers.add(id);
				
	            double request = 0.0;
	            if (id != -1) 
	                request = Problem.get_problem().get_request_by_id_customer(id);
	            
				cluster = new Cluster(
						Problem.get_problem().get_depots().get(i).get_id_depot(), 
						request,
						list_id_customers
				);
				//cluster = new Cluster(listIDElements.get(i).intValue(), InfoProblem.getProblem().getRequestByIDCustomer(listIDElements.get(i).intValue()), listIDCustomers);
				clusters.add(cluster);
			}
		}
		else // UPGMC
		{
			List<Integer> ids_depots = new ArrayList<>();
			for (Depot depot : Problem.get_problem().get_depots()) 
			    ids_depots.add(depot.get_id_depot());
			
			List<Integer> ids_customers = new ArrayList<>();
			for (Customer customer : Problem.get_problem().get_customers()) 
			    ids_customers.add(customer.get_id_customer());
			
			for(int i = 0; i < total_elements; i++)
			{
				list_id_customers = new ArrayList<Integer>();
				Integer id = list_id_elements.get(i);

				if(ids_depots.contains(id))
					cluster = new Cluster(id, 0.0, list_id_customers);
				else
				{
					list_id_customers.add(id);
					double request = Problem.get_problem().get_request_by_id_customer(id);
					cluster = new Cluster(id, request, list_id_customers);
				}
				clusters.add(cluster);	
			}
		}
		System.out.println("--------------------------------------------------");
		System.out.println("LISTA DE CLUSTERS");
		
		for(int i = 0; i < clusters.size(); i++)
		{
			System.out.println("ID CLUSTER: " + clusters.get(i).get_id_cluster());
			System.out.println("DEMANDA DEL CLUSTER: " + clusters.get(i).get_request_cluster());
			System.out.println("ELEMENTOS DEL CLUSTER: " + clusters.get(i).get_items_of_cluster());
		}
		System.out.println("--------------------------------------------------");

		return clusters;
	}
	
	/**
	 * Determina si un depósito ha alcanzado su capacidad o no tiene clientes asignables.
	 *
	 * @param customers Lista de clientes a evaluar.
	 * @param request_cluster Demanda acumulada en el clúster asociado al depósito.
	 * @param capacity_depot Capacidad total del depósito.
	 * @return true si el depósito está lleno o no tiene clientes asignables; false en otro caso.
	 */
	protected boolean is_full_depot(ArrayList<Customer> customers, double request_cluster, double capacity_depot) {
		if (customers == null || customers.isEmpty()) 
			return true;
		
		boolean is_full = true;
		double current_request = capacity_depot - request_cluster;

		if(current_request > 0)
		{
			int i = 0;
			while((i < customers.size()) && (is_full))
			{
				if(customers.get(i).get_request_customer() <= current_request)
					is_full = false;
				else
					i++;
			}
		}	
		return is_full;
		// cuando no quedan cliente lo saca como lleno ver si comviene en todos los casos
	}

	/**
	 * Recalcula el centroide geométrico (promedio de coordenadas) de los clientes en un clúster.
	 *
	 * @param cluster Clúster del cual se desea recalcular el centroide.
	 * @return Ubicación del nuevo centroide como objeto Location.
	 * @throws ProblemException si ocurre un error accediendo a las ubicaciones de los clientes.
	 */
	protected Location recalculate_centroid(Cluster cluster) throws ProblemException {
		if (cluster == null || cluster.get_items_of_cluster().isEmpty()) 
	        throw new ProblemException("No se puede calcular el centroide: el clúster está vacío o es nulo.");
	   
		double ave_axis_x = 0.0;
		double ave_axis_y = 0.0;
		int count_customers = cluster.get_items_of_cluster().size();

		for(int i = 0; i < count_customers; i++) 
		{	
			Location location = new Location();
			location = Problem.get_problem().get_location_by_id_customer(cluster.get_items_of_cluster().get(i)); 

			ave_axis_x += location.get_axis_x();
			ave_axis_y += location.get_axis_y();
		}

		ave_axis_x = (ave_axis_x / count_customers);
		ave_axis_y = (ave_axis_y / count_customers);

		Location location_centroid = new Location();
		location_centroid.set_axis_x(AbstractTools.truncate_double(ave_axis_x, 6));
		location_centroid.set_axis_y(AbstractTools.truncate_double(ave_axis_y, 6));

		return location_centroid;
	}
}