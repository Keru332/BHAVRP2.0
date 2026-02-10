package cujae.inf.ic.om.assignment.clustering.partitional;

import cujae.inf.ic.om.assignment.clustering.ESamplingType;

import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.CostMatrixException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Fleet;
import cujae.inf.ic.om.problem.input.Location;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;

import cujae.inf.ic.om.factory.DistanceType;

import cujae.inf.ic.om.matrix.NumericMatrix;
import cujae.inf.ic.om.matrix.RowCol;

import java.util.ArrayList;
import java.util.Random;

public abstract class AbstractByMedoids extends AbstractPartitional {
	
	/**
	 * Genera particiones de clientes según el tamaño de muestra y el tipo de muestreo.
	 *
	 * @param samp_size Tamaño de cada partición.
	 * @param sampling_type Tipo de muestreo (aleatorio o secuencial).
	 * @return Lista de particiones, cada una representada como una lista de clientes.
	 * @throws AssignmentException Si ocurre un error durante la generación de particiones.
	 */
	protected ArrayList<ArrayList<Customer>> generate_partitions(int samp_size, ESamplingType sampling_type) 
			throws AssignmentException {
		try {
			ArrayList<ArrayList<Customer>> partitions = new ArrayList<ArrayList<Customer>>();
			ArrayList<Customer> partition = new ArrayList<Customer>();

			int total_customers = Problem.get_problem().get_total_customers();
			int total_partitions = total_customers/samp_size;

			if(total_customers % samp_size != 0)
				total_partitions += 1;

			System.out.println("TOTAL DE PARTICIONES: " + total_partitions);
			System.out.println("---------------------------------------------------------------");


			switch(sampling_type.ordinal())
			{
			case 0:
			{
				int j = 0;
				int pos_element = -1;
				Random rdm = new Random();

				ArrayList<Customer> customers = new ArrayList<Customer>(Problem.get_problem().get_customers());

				for(int i = 0; i < total_partitions; i++)
				{
					while((j < (samp_size * (i + 1))) && (j < total_customers))
					{
						pos_element = rdm.nextInt(customers.size());
						partition.add(customers.remove(pos_element));

						j++;
					}
					System.out.println("PARTICIÓN " + (i + 1) + ": ");
					System.out.println("TOTAL DE ELEMENTOS DE LA PARTICIÓN " + (i + 1) + ": " + partition.size());
					for(int k = 0; k < partition.size(); k++)
						System.out.println("ELEMENTOS DE LA PARTICIÓN " + (i + 1) + ": " +  partition.get(k).get_id_customer());
					System.out.println("---------------------------------------------------------------");

					partitions.add(partition);
					partition = new ArrayList<Customer>();
				}
				break;
			}
			case 1:
			{
				int j = 0;

				for(int i = 0; i < total_partitions; i++)
				{
					while((j < (samp_size * (i + 1))) && (j < total_customers))
					{
						partition.add(Problem.get_problem().get_customers().get(j));
						j++;
					}
					System.out.println("PARTICIÓN " + (i + 1) + ": ");
					System.out.println("TOTAL DE ELEMENTOS DE LA PARTICIÓN " + (i + 1) + ": " + partition.size());

					for(int k = 0; k < partition.size(); k++)
						System.out.println("ELEMENTOS DE LA PARTICIÓN " + (i + 1) + ": " +  partition.get(k).get_id_customer());

					System.out.println("---------------------------------------------------------------");

					partitions.add(partition);
					partition = new ArrayList<Customer>();
				}
				break;
			}
			default:
				throw new AssignmentException("Tipo de muestreo no reconocido: " + sampling_type);
			}
			return partitions;
		} catch (Exception e) {
			throw new AssignmentException("Error al generar las particiones de clientes.", e);
		}
	}
	
	/**
	 * Genera una lista de identificadores de clientes que actuarán como medoides iniciales,
	 * asignando uno a cada depósito según las distancias mínimas.
	 *
	 * @param customers Lista de clientes entre los cuales se seleccionarán los medoides.
	 * @param distance_type Tipo de distancia a utilizar para calcular la matriz de costos.
	 * @return Lista de identificadores de clientes seleccionados como medoides.
	 * @throws CostMatrixException Si ocurre un error durante el cálculo de la matriz de costos.
	 * @throws AssignmentException Si ocurre un error inesperado al seleccionar los elementos.
	 */
	protected ArrayList<Integer> generate_elements(ArrayList<Customer> customers, DistanceType distance_type) 
			throws CostMatrixException, AssignmentException {
		try {
			ArrayList<Integer> id_elements = new ArrayList<Integer>();

			int total_customers = customers.size();
			int total_depots = Problem.get_problem().get_total_depots();
			int counter = total_depots;

			NumericMatrix cost_matrix = initialize_cost_matrix(customers, Problem.get_problem().get_depots(), distance_type);
			RowCol rc_best_all = new RowCol();
			int id_element = -1 ;

			for(int i = 0; i < counter; i++)
				id_elements.add(-1);

			System.out.println("LISTADO DE ELEMENTOS SELECCIONADOS:" + id_elements);

			while(counter > 0)
			{
				rc_best_all = cost_matrix.indexLowerValue(total_customers, 0, (total_customers + total_depots - 1), (total_customers - 1));

				System.out.println("ROW SELECCIONADA: " + rc_best_all.getRow());
				System.out.println("COL SELECCIONADA: " + rc_best_all.getCol());
				System.out.println("VALOR SELECCIONADO: " + cost_matrix.getItem(rc_best_all.getRow(), rc_best_all.getCol()));

				id_element = customers.get(rc_best_all.getCol()).get_id_customer();	
				id_elements.set((rc_best_all.getRow() - total_customers), id_element);	

				System.out.println("ELEMENTO: " + id_element); 
				System.out.println("LISTADO DE ELEMENTOS ACTUALIZADOS:" + id_elements);

				cost_matrix.fillValue(total_customers, rc_best_all.getCol(), (total_customers + total_depots - 1), rc_best_all.getCol(), Double.POSITIVE_INFINITY);
				cost_matrix.fillValue(rc_best_all.getRow(), 0, rc_best_all.getRow(), (total_customers + total_depots - 1), Double.POSITIVE_INFINITY);
				counter--;
			}
			System.out.println("--------------------------------------------------");
			System.out.println("CENTROIDES/MEDOIDES INICIALES");
			System.out.println(id_elements);
			System.out.println("--------------------------------------------------");

			return id_elements;
		} catch (Exception e) {
			throw new AssignmentException("Error al generar los elementos iniciales para los medoides.", e);
		}
	}

	/**
	 * Extrae los identificadores de los depósitos (medoides) actuales.
	 *
	 * @param medoids Lista de depósitos considerados como medoides.
	 * @return Lista de identificadores de los medoides.
	 * @throws AssignmentException Si la lista de medoides es nula o contiene valores inválidos.
	 */
	protected ArrayList<Integer> get_id_medoids(ArrayList<Depot> medoids) 
			throws AssignmentException {
		try {
			if (medoids == null)
				throw new AssignmentException("La lista de medoides es nula.");

			ArrayList<Integer> id_medoids = new ArrayList<Integer>();

			for (Depot depot : medoids) {
	            if (depot == null)
	                throw new AssignmentException("Se encontró un medoide nulo en la lista.");
	            id_medoids.add(depot.get_id_depot());
	        }

			System.out.println("--------------------------------------------------");
			System.out.println("ID MEDOIDES ACTUALES");
			System.out.println("--------------------------------------------------");
			System.out.println(id_medoids);

			return id_medoids;		
		} catch (Exception e) {
			throw new AssignmentException("Error al obtener los identificadores de los medoides.", e);
		}
	}
	
	/**
	 * Actualiza cada clúster de la lista con un nuevo cliente (medoide) identificado por su ID.
	 * Se añade el cliente al clúster correspondiente y se actualiza la demanda total del clúster.
	 *
	 * @param clusters Lista de clústeres a actualizar.
	 * @param id_elements Identificadores de clientes que se asignarán como medoides.
	 * @throws ClusterException Si ocurre un error al acceder o modificar un clúster.
	 * @throws ProblemException Si ocurre un error al obtener información del problema.
	 * @throws AssignmentException Si las listas están vacías, nulas o de tamaños incompatibles.
	 */
	protected void update_clusters(ArrayList<Cluster> clusters, ArrayList<Integer> id_elements) 
			throws ClusterException, ProblemException, AssignmentException {
		if (clusters == null || id_elements == null)
	        throw new AssignmentException("Las listas de clústeres o de elementos son nulas.");

	    if (clusters.size() != id_elements.size())
	        throw new AssignmentException("La cantidad de clústeres no coincide con la cantidad de elementos.");
		
	    try {
	    	for(int i = 0; i < clusters.size(); i++)
	    	{
	    		clusters.get(i).get_items_of_cluster().add(id_elements.get(i));
	    		clusters.get(i).set_request_cluster(Problem.get_problem().get_request_by_id_customer(id_elements.get(i)));
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
	    } catch (Exception e) {
	    	throw new AssignmentException("Error al actualizar los clústeres con los elementos asignados.", e);
	    }
	}
	
	/**
	 * Verifica si ha habido algún cambio en las posiciones de los medoides con respecto a la iteración anterior.
	 *
	 * @param old_medoids Lista de medoides anteriores.
	 * @param current_medoids Lista de medoides actuales.
	 * @return {@code true} si al menos un medoide cambió de posición; {@code false} en caso contrario.
	 * @throws AssignmentException Si alguna lista es nula o tienen tamaños distintos.
	 */
	protected boolean verify_medoids(ArrayList<Depot> old_medoids, ArrayList<Depot> current_medoids) 
			throws AssignmentException {
		if (old_medoids == null || current_medoids == null)
			throw new AssignmentException("Una o ambas listas de medoides son nulas.");

		if (old_medoids.size() != current_medoids.size())
			throw new AssignmentException("Las listas de medoides no tienen el mismo tamaño.");

		try {
			boolean change = false;
			int i = 0;

			while((!change) && (i < current_medoids.size()))
			{
				if((old_medoids.get(i).get_location_depot().get_axis_x() != current_medoids.get(i).get_location_depot().get_axis_x()) || (old_medoids.get(i).get_location_depot().get_axis_y() != current_medoids.get(i).get_location_depot().get_axis_y()))
					change = true;
				else
					i++;
			}
			System.out.println("change:  " + change);

			return change;
		} catch (Exception e) {
			throw new AssignmentException("Error al verificar cambios en los medoides.", e);
		}
	}
	
	/**
	 * Genera una copia profunda de los depósitos especificados, replicando su posición y flota.
	 *
	 * @param depots Lista de depósitos a replicar.
	 * @return Lista de nuevas instancias de depósitos equivalentes a los originales.
	 * @throws ProblemException Si ocurre un error al acceder a la información de la flota.
	 * @throws AssignmentException Si la lista de depósitos es nula o inválida.
	 */
	protected ArrayList<Depot> replicate_depots(ArrayList<Depot> depots) 
			throws ProblemException, AssignmentException {
		try {
			if (depots == null || depots.isEmpty())
				throw new AssignmentException("La lista de depósitos está vacía o es nula.");

			ArrayList<Depot> new_depots = new ArrayList<Depot>();

			System.out.println("--------------------------------------------------");
			System.out.println("MEDOIDES/CENTROIDES ACTUALES");

			for(int i = 0; i < depots.size(); i++)
			{
				Depot depot = new Depot();
				depot.set_id_depot(depots.get(i).get_id_depot());

				double axis_x = 0.0; 
				double axis_y = 0.0; 
				axis_x = depots.get(i).get_location_depot().get_axis_x();
				axis_y = depots.get(i).get_location_depot().get_axis_y();

				Location location = new Location();
				location.set_axis_x(axis_x);
				location.set_axis_y(axis_y);
				depot.set_location_depot(location);

				ArrayList<Fleet> fleet = new ArrayList<Fleet>();
				fleet.addAll(Problem.get_problem().get_depots().get(i).get_fleet_depot());
				depot.set_fleet_depot(fleet);

				new_depots.add(depot);

				System.out.println("--------------------------------------------------");
				System.out.println("ID MEDOIDE/CENTROIDE: " + new_depots.get(i).get_id_depot());
				System.out.println("X: " + new_depots.get(i).get_location_depot().get_axis_x());
				System.out.println("Y: " + new_depots.get(i).get_location_depot().get_axis_y());
				System.out.println("CAPACIDAD DE VEHICULO: " + new_depots.get(i).get_fleet_depot().get(0).get_capacity_vehicle());
				System.out.println("CANTIDAD DE VEHICULOS: " + new_depots.get(i).get_fleet_depot().get(0).get_count_vehicles());
			}
			return new_depots;
		} catch (ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error al replicar los depósitos.", e);
		}
	}
}