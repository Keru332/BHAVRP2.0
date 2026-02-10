package cujae.inf.ic.om.assignment.clustering.partitional;

import cujae.inf.ic.om.assignment.clustering.AbstractClustering;
import cujae.inf.ic.om.assignment.clustering.ESeedType;

import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.CostMatrixException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Location;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;

import cujae.inf.ic.om.matrix.NumericMatrix;
import cujae.inf.ic.om.matrix.RowCol;

import cujae.inf.ic.om.factory.DistanceType;

import java.util.ArrayList;
import java.util.Random;

public abstract class AbstractPartitional extends AbstractClustering {
	
	public static ESeedType seed_type = ESeedType.Nearest_Depot;
	public static int count_max_iterations = 100; // UN VALOR APROPIADO Y CONFIGURABLE?
	public static int current_iteration = 0;
	
	/**
	 * Crea los objetos Depot que funcionarán como centroides iniciales a partir de los identificadores de clientes.
	 *
	 * @param id_elements Lista de identificadores de clientes que serán usados como centroides.
	 * @return Lista de objetos Depot inicializados como centroides.
	 * @throws ProblemException si ocurre un error al acceder a los datos de los clientes.
	 */
	protected ArrayList<Depot> create_centroids(ArrayList<Integer> id_elements) throws ProblemException {
		try {
			ArrayList<Depot> centroids = new ArrayList<Depot>();

			for (Integer id : id_elements) 
			{
				if (id == null || id == -1) 
					continue;
				
				Customer customer = Problem.get_problem().get_customer_by_id_customer(id);
				if (customer == null)
					throw new ProblemException("Cliente con ID " + id + " no encontrado en el problema.");

				Depot centroid = new Depot();
				centroid.set_id_depot(id);

				Location location = new Location();
				location.set_axis_x(customer.get_location_customer().get_axis_x());
				location.set_axis_y(customer.get_location_customer().get_axis_y());
				centroid.set_location_depot(location);

				centroids.add(centroid);
			}
			if (centroids.isEmpty()) 
				throw new ProblemException("No se pudieron crear centroides válidos a partir de los identificadores.");
			
			return centroids;
		} catch (Exception e) {
			throw new ProblemException("Error al crear los centroides a partir de los identificadores.", e);
		}
	}
	
	/**
	 * Limpia todos los clústeres de la lista, eliminando sus elementos y reiniciando su demanda acumulada.
	 *
	 * @param clusters Lista de clústeres a limpiar.
	 * @throws ClusterException si alguno de los clústeres es nulo o la lista es inválida.
	 */
	protected void clean_clusters(ArrayList<Cluster> clusters) 
			throws ClusterException {
		try {
			if (clusters == null || clusters.isEmpty()) 
				throw new ClusterException("La lista de clústeres está vacía o no fue inicializada.");

			for (Cluster c : clusters) 
			{
				if (c == null) 
					throw new ClusterException("Se encontró un clúster nulo al limpiar la lista.");
				c.clean_cluster(); 
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
		} catch (ClusterException e) {
			throw e;
		} catch (Exception e) {
			throw new ClusterException("Error inesperado al limpiar los clústeres.", e);
		}
	}
	
	/**
	 * Asigna clientes a clústeres según la matriz de costos, respetando las capacidades de los depósitos.
	 *
	 * @param clusters Lista de clústeres disponibles (uno por depósito).
	 * @param customer_to_assign Lista de clientes por asignar.
	 * @param cost_matrix Matriz de distancias entre clientes y depósitos.
	 * @return Lista de clústeres con clientes asignados.
	 * @throws ClusterException si ocurre un error al modificar un clúster.
	 * @throws ProblemException si hay un problema con la obtención de datos desde el problema.
	 */
	protected ArrayList<Cluster> step_assignment(ArrayList<Cluster> clusters, ArrayList<Customer> customer_to_assign, 
			NumericMatrix cost_matrix) 
					throws ProblemException, ClusterException {
		try {
			if (clusters == null || customer_to_assign == null || cost_matrix == null) 
				throw new ClusterException("Entrada nula en clusters, clientes por asignar o matriz de costos.");

			int id_depot = -1;
			int pos_depot = -1;				
			double capacity_depot = 0.0;			

			int id_customer = -1;
			int pos_customer = -1;
			double request_customer = 0.0;

			int pos_cluster = -1;
			double request_cluster = 0.0;

			RowCol rc_best_all = new RowCol();

			ArrayList<Customer> list_customers = new ArrayList<Customer>(customer_to_assign);
			int total_customers = customer_to_assign.size();
			int total_depots = clusters.size();

			System.out.println("--------------------------------------------------------------------");
			System.out.println("PROCESO DE ASIGNACIÓN");

			while((!customer_to_assign.isEmpty()) && (!cost_matrix.fullMatrix(total_customers, 0, (total_customers + total_depots - 1), (total_customers - 1), Double.POSITIVE_INFINITY))) 
			{
				rc_best_all = cost_matrix.indexLowerValue(total_customers, 0, (total_customers + total_depots - 1), (total_customers - 1));

				pos_customer = rc_best_all.getCol();
				id_customer = list_customers.get(pos_customer).get_id_customer();				
				request_customer = list_customers.get(pos_customer).get_request_customer();

				System.out.println("-----------------------------------------------------------");
				System.out.println("BestAllCol: " + rc_best_all.getCol());
				System.out.println("BestAllRow: " + rc_best_all.getRow());

				System.out.println("ID CLIENTE SELECCIONADO: " + id_customer);
				System.out.println("POSICIÓN DEL CLIENTE SELECCIONADO: " + pos_customer);
				System.out.println("DEMANDA DEL CLIENTE SELECCIONADO: " + request_customer);

				pos_depot = (rc_best_all.getRow() - total_customers); 
				id_depot = Problem.get_problem().get_depots().get(pos_depot).get_id_depot();
				capacity_depot = Problem.get_problem().get_total_capacity_by_depot(id_depot);

				System.out.println("ID DEPOSITO SELECCIONADO: " + id_depot);
				System.out.println("POSICIÓN DEL DEPOSITO SELECCIONADO: " + pos_depot);
				System.out.println("CAPACIDAD TOTAL DEL DEPOSITO SELECCIONADO: " + capacity_depot);				

				pos_cluster = find_cluster(id_depot, clusters);	

				System.out.println("POSICION DEL CLUSTER: " + pos_cluster);

				if(pos_cluster != -1)
				{
					request_cluster = clusters.get(pos_cluster).get_request_cluster();

					System.out.println("DEMANDA DEL CLUSTER: " + request_cluster);

					if(capacity_depot >= (request_cluster + request_customer)) 
					{
						request_cluster += request_customer;

						clusters.get(pos_cluster).set_request_cluster(request_cluster);
						clusters.get(pos_cluster).get_items_of_cluster().add(id_customer);		

						System.out.println("DEMANDA DEL CLUSTER ACTUALIZADA: " + request_cluster);
						System.out.println("ELEMENTOS DEL CLUSTER: " + clusters.get(pos_cluster).get_items_of_cluster());

						cost_matrix.fillValue(total_customers, pos_customer, (total_customers + total_depots - 1), pos_customer, Double.POSITIVE_INFINITY);
						customer_to_assign.remove(Problem.get_problem().find_pos_customer(customer_to_assign, id_customer));

						System.out.println("CANTIDAD DE CLIENTES SIN ASIGNAR: " + customer_to_assign.size());
					}
					else
						cost_matrix.setItem(rc_best_all.getRow(), pos_customer, Double.POSITIVE_INFINITY);

					if(is_full_depot(customer_to_assign, request_cluster, capacity_depot))
					{
						System.out.println("DEPOSITO LLENO");

						cost_matrix.fillValue(rc_best_all.getRow(), 0, rc_best_all.getRow(), (total_customers + total_depots - 1), Double.POSITIVE_INFINITY);
					}
				}
			}
			System.out.println("--------------------------------------------------");
			System.out.println("LISTA DE CLUSTERS");
			for(int i = 0; i < clusters.size(); i++)
			{
				System.out.println("ID CLUSTER: " + clusters.get(i).get_id_cluster());
				System.out.println("DEMANDA DEL CLUSTER: " + clusters.get(i).get_request_cluster());
				System.out.println("CANTIDAD DE ELEMENTOS EN EL CLUSTER: " + clusters.get(i).get_items_of_cluster().size());
				System.out.println("ELEMENTOS DEL CLUSTER: " + clusters.get(i).get_items_of_cluster());
			}
			System.out.println("--------------------------------------------------");

			return clusters;
		} catch (ClusterException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new ClusterException("Error inesperado durante la asignación paso a paso.", e);
		}
	}
	
	/**
	 * Elimina de la lista de clientes a asignar aquellos clientes que ya han sido seleccionados como centroides o medoides.
	 *
	 * @param customer_to_assign Lista de clientes aún por asignar.
	 * @param id_elements Identificadores de clientes seleccionados como centroides/medoides.
	 * @throws ProblemException si ocurre un error al buscar un cliente.
	 */
	protected void update_customer_to_assign(ArrayList<Customer> customer_to_assign, ArrayList<Integer> id_elements) 
			throws ProblemException {
		try {
			if (customer_to_assign == null || id_elements == null)
				throw new ProblemException("Las listas de clientes o identificadores no pueden ser nulas.");

			for(int i = 0; i < id_elements.size(); i++)
			{
				boolean found = false;
				int j = 0;

				while ((!found) && j < customer_to_assign.size())
				{
					if(customer_to_assign.get(j).get_id_customer() == id_elements.get(i).intValue())
					{
						found = true;
						customer_to_assign.remove(j);
					}
					else
						j++;
				}
			}
			System.out.println("CLIENTES A ASIGNAR");

			for(int i = 0; i < customer_to_assign.size(); i++)
			{
				System.out.println("--------------------------------------------------");
				System.out.println("ID CLIENTE: " + customer_to_assign.get(i).get_id_customer());
				System.out.println("X: " + customer_to_assign.get(i).get_location_customer().get_axis_x());
				System.out.println("Y: " + customer_to_assign.get(i).get_location_customer().get_axis_y());
				System.out.println("DEMANDA: " + customer_to_assign.get(i).get_request_customer());
			}
		} catch (Exception e) {
			throw new ProblemException("Error al actualizar la lista de clientes por asignar.", e);
		}
	}
	
	/**
	 * Genera una lista de identificadores de clientes que se utilizarán como centroides o medoides,
	 * de acuerdo con la estrategia de semilla especificada.
	 *
	 * @param seed_type Tipo de estrategia para seleccionar los elementos semilla.
	 * @param distance_type Tipo de métrica de distancia a utilizar.
	 * @return Lista de identificadores seleccionados como centroides/medoides.
	 * @throws CostMatrixException si ocurre un error al calcular la matriz de costos.
	 */
	protected ArrayList<Integer> generate_elements(ESeedType seed_type, DistanceType distance_type) 
			throws CostMatrixException {
		try {
			ArrayList<Integer> id_elements = new ArrayList<Integer>();

			int total_customers = Problem.get_problem().get_total_customers();
			int total_depots = Problem.get_problem().get_total_depots();
			int counter = total_depots;

			NumericMatrix cost_matrix = initialize_cost_matrix(Problem.get_problem().get_customers(), Problem.get_problem().get_depots(), distance_type);

			RowCol rc_best_all = new RowCol();
			int id_element = -1 ;

			switch(seed_type.ordinal()) 
			{
			case 0:
			{
				for(int i = 0; i < counter; i++)
					id_elements.add(-1);

				System.out.println("LISTADO DE ELEMENTOS SELECCIONADOS:" + id_elements);

				while(counter > 0)
				{
					rc_best_all = cost_matrix.indexBiggerValue(total_customers, 0, (total_customers + total_depots - 1), (total_customers - 1));

					System.out.println("ROW SELECCIONADA: " + rc_best_all.getRow());
					System.out.println("COL SELECCIONADA: " + rc_best_all.getCol());
					System.out.println("VALOR SELECCIONADO: " + cost_matrix.getItem(rc_best_all.getRow(), rc_best_all.getCol()));

					id_element = Problem.get_problem().get_customers().get(rc_best_all.getCol()).get_id_customer();	
					id_elements.set((rc_best_all.getRow() - total_customers), id_element);	

					System.out.println("ELEMENTO: " + id_element); 
					System.out.println("LISTADO DE ELEMENTOS ACTUALIZADOS:" + id_elements);

					cost_matrix.fillValue(total_customers, rc_best_all.getCol(), (total_customers + total_depots - 1), rc_best_all.getCol(), Double.NEGATIVE_INFINITY);
					cost_matrix.fillValue(rc_best_all.getRow(), 0, rc_best_all.getRow(), (total_customers + total_depots - 1), Double.NEGATIVE_INFINITY);
					counter--;
				}
				break;
			}
			case 1:
			{
				for(int i = 0; i < counter; i++)
					id_elements.add(-1);

				System.out.println("LISTADO DE ELEMENTOS SELECCIONADOS:" + id_elements);

				while(counter > 0)
				{
					rc_best_all = cost_matrix.indexLowerValue(total_customers, 0, (total_customers + total_depots - 1), (total_customers - 1));

					System.out.println("ROW SELECCIONADA: " + rc_best_all.getRow());
					System.out.println("COL SELECCIONADA: " + rc_best_all.getCol());
					System.out.println("VALOR SELECCIONADO: " + cost_matrix.getItem(rc_best_all.getRow(), rc_best_all.getCol()));


					id_element = Problem.get_problem().get_customers().get(rc_best_all.getCol()).get_id_customer();	
					id_elements.set((rc_best_all.getRow() - total_customers), id_element);	

					System.out.println("ELEMENTO: " + id_element); 
					System.out.println("LISTADO DE ELEMENTOS ACTUALIZADOS:" + id_elements);

					cost_matrix.fillValue(total_customers, rc_best_all.getCol(), (total_customers + total_depots - 1), rc_best_all.getCol(), Double.POSITIVE_INFINITY);
					cost_matrix.fillValue(rc_best_all.getRow(), 0, rc_best_all.getRow(), (total_customers + total_depots - 1), Double.POSITIVE_INFINITY);
					counter--;
				}
				break;
			}
			case 2:
			{
				Random rdm = new Random();

				while(counter > 0)
				{
					id_element = rdm.nextInt(total_customers); // DUDA + 1
					id_elements.add(Problem.get_problem().get_customers().get(id_element).get_id_customer());

					System.out.println("ELEMENTO: " + id_element); 
					System.out.println("LISTADO DE ELEMENTOS ACTUALIZADOS:" + id_elements);

					counter--;
				}
				break;
			}
			default:
				throw new CostMatrixException("Tipo de semilla no reconocido.");
			}
			System.out.println("--------------------------------------------------");
			System.out.println("CENTROIDES/MEDOIDES INICIALES");
			System.out.println(id_elements);
			System.out.println("--------------------------------------------------");

			return id_elements;
		} catch (Exception e) {
			throw new CostMatrixException("Error al generar elementos iniciales.", e);
		}
	}
}