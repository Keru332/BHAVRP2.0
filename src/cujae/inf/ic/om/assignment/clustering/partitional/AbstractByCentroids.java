package cujae.inf.ic.om.assignment.clustering.partitional;

import cujae.inf.ic.om.exceptions.AssignmentException;
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

public abstract class AbstractByCentroids extends AbstractPartitional {
	
	/**
	 * Genera una lista de elementos representativos (clientes) seleccionados como centroides iniciales.
	 *
	 * @param distance_type Tipo de distancia a utilizar.
	 * @return Lista de identificadores de clientes seleccionados como centroides.
	 * @throws CostMatrixException Si ocurre un error al calcular la matriz de costos.
	 * @throws ProblemException Si ocurre un error al acceder a los datos del problema.
	 * @throws AssignmentException Si ocurre un error inesperado durante la selección de elementos.
	 */
	protected ArrayList<Integer> generate_elements(DistanceType distance_type) throws CostMatrixException, ProblemException, AssignmentException {
		try {
			ArrayList<Integer> id_elements = new ArrayList<Integer>();
			int total_customers = Problem.get_problem().get_total_customers();
			int total_depots = Problem.get_problem().get_total_depots();
			int counter = total_depots;

			RowCol rc_best_all = new RowCol();
			int id_element = -1 ;

			Depot depot = new Depot();
			depot.set_id_depot(-1);
			depot.set_location_depot(calculate_mean_coordinate());

			ArrayList<Depot> list_depot = new ArrayList<Depot>();
			list_depot.add(depot);

			NumericMatrix cost_matrix = initialize_cost_matrix(Problem.get_problem().get_customers(), list_depot, distance_type);;

			System.out.println("LISTADO DE ELEMENTOS SELECCIONADOS:" + id_elements);

			while(counter > 0)
			{
				rc_best_all = cost_matrix.indexBiggerValue(total_customers, 0, total_customers, (total_customers - 1));

				if (rc_best_all.getRow() < 0 || rc_best_all.getCol() < 0)
					throw new AssignmentException("No se pudo seleccionar un cliente representativo válido.");

				System.out.println("ROW SELECCIONADA: " + rc_best_all.getRow());
				System.out.println("COL SELECCIONADA: " + rc_best_all.getCol());
				System.out.println("VALOR SELECCIONADO: " + cost_matrix.getItem(rc_best_all.getRow(), rc_best_all.getCol()));

				id_element = Problem.get_problem().get_customers().get(rc_best_all.getCol()).get_id_customer();	
				id_elements.add(id_element);	

				System.out.println("ELEMENTO: " + id_element); 
				System.out.println("LISTADO DE ELEMENTOS ACTUALIZADOS:" + id_elements);

				cost_matrix.setItem(rc_best_all.getRow(), rc_best_all.getCol(), Double.NEGATIVE_INFINITY);
				counter--;
			}
			System.out.println("LISTADO DE ELEMENTOS SELECCIONADOS:" + id_elements);

			return sorted_elements(id_elements, distance_type);
		} catch (Exception e) {
			throw new AssignmentException("Error al generar elementos representativos para los centroides.", e);
		}
	}
	
	/**
	 * Ordena los identificadores de los elementos seleccionados como centroides,
	 * emparejando clientes con depósitos según la menor distancia.
	 *
	 * @param id_elements Lista de identificadores de clientes seleccionados.
	 * @param distance_type Tipo de distancia a utilizar.
	 * @return Lista ordenada de identificadores de clientes como centroides.
	 * @throws CostMatrixException Si ocurre un error en el cálculo de distancias.
	 * @throws ProblemException Si ocurre un error al acceder a los datos del problema.
	 * @throws AssignmentException Si ocurre un error inesperado durante la ordenación.
	 */
	private ArrayList<Integer> sorted_elements (ArrayList<Integer> id_elements, DistanceType distance_type) throws CostMatrixException, ProblemException, AssignmentException {
		try {
			int total_depots = Problem.get_problem().get_total_depots();
			int j = 0;

			ArrayList<Integer> sorted_elements = new ArrayList<Integer>();
			ArrayList<Customer> customers = new ArrayList<Customer>();

			for(int i = 0; i < id_elements.size(); i++)
			{
				sorted_elements.add(-1);
				customers.add(Problem.get_problem().get_customer_by_id_customer(id_elements.get(i)));
			}

			NumericMatrix cost_matrix = null;
			RowCol rc_best_all = new RowCol();

			if (distance_type == DistanceType.Real) 
				cost_matrix = Problem.get_problem().fill_cost_matrix_real(customers, Problem.get_problem().get_depots());
			else 
				cost_matrix = Problem.get_problem().fill_cost_matrix(customers, Problem.get_problem().get_depots(), distance_type);

			//System.out.println("----ORGANIZAR ELEMENTOS SELECCIONADOS-----------------------------------------------------");

			while(j < id_elements.size())
			{
				rc_best_all = cost_matrix.indexLowerValue(0, id_elements.size(), (id_elements.size() - 1), (id_elements.size() + total_depots - 1));

				if (rc_best_all == null) 
					throw new AssignmentException("No se pudo encontrar un valor válido en la matriz de costos al ordenar elementos.");

				System.out.println("ROW SELECCIONADA: " + rc_best_all.getRow());
				System.out.println("COL SELECCIONADA: " + rc_best_all.getCol());
				System.out.println("VALOR SELECCIONADO: " + cost_matrix.getItem(rc_best_all.getRow(), rc_best_all.getCol()));

				cost_matrix.fillValue(0, rc_best_all.getCol(), (id_elements.size() - 1), rc_best_all.getCol(), Double.POSITIVE_INFINITY);
				cost_matrix.fillValue(rc_best_all.getRow(), (id_elements.size() - 1), rc_best_all.getRow(), (id_elements.size() + total_depots - 1), Double.POSITIVE_INFINITY);

				sorted_elements.set((rc_best_all.getCol() - id_elements.size()), id_elements.get(rc_best_all.getRow()));

				System.out.println("LISTADO DE ELEMENTOS SELECCIONADOS ORDENADOS ACTUALIZADA:" + sorted_elements);

				j++;
			}
			System.out.println("LISTADO DE ELEMENTOS SELECCIONADOS ORDENADOS:" + sorted_elements);

			return sorted_elements;
		} catch (CostMatrixException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado al ordenar los elementos seleccionados.", e);
		}
	}
	
	/**
	 * Calcula la coordenada promedio (centroide) de todos los clientes del problema.
	 *
	 * @return Objeto Location que representa el centroide calculado.
	 * @throws AssignmentException Si ocurre un error durante el cálculo o si no existen clientes.
	 */
	private Location calculate_mean_coordinate() throws AssignmentException {
		try {
			double axis_x = 0.0;
			double axis_y = 0.0;

			ArrayList<Location> list_coordinates_customers = Problem.get_problem().get_list_coordinates_customers();

			if (list_coordinates_customers == null || list_coordinates_customers.isEmpty()) 
				throw new AssignmentException("La lista de coordenadas de clientes está vacía o no disponible.");
			
			for(int i = 0; i < list_coordinates_customers.size(); i++) 
			{
				axis_x += list_coordinates_customers.get(i).get_axis_x();
				axis_y += list_coordinates_customers.get(i).get_axis_y();
			}

			axis_x /= list_coordinates_customers.size();
			axis_y /= list_coordinates_customers.size();

			Location mean_location = new Location(axis_x, axis_y);

			return mean_location;
		} catch (Exception e) {
			throw new AssignmentException("Error al calcular la coordenada promedio de los clientes.", e);
		}
	}
	
	/**
	 * Verifica si los centroides han cambiado con respecto a los clústeres y los actualiza si es necesario.
	 *
	 * @param clusters Lista de clústeres actuales.
	 * @param centroids Lista de centroides a verificar.
	 * @param distance_type Tipo de distancia a utilizar para actualizar los centroides.
	 * @return true si se detectaron cambios en los centroides, false en caso contrario.
	 * @throws ProblemException Si ocurre un error al acceder al problema.
	 * @throws CostMatrixException Si ocurre un error con la matriz de costos.
	 * @throws AssignmentException Si ocurre un error inesperado durante la verificación.
	 */
	protected boolean verify_centroids(ArrayList<Cluster> clusters, ArrayList<Depot> centroids, DistanceType distance_type) 
			throws ProblemException, CostMatrixException, AssignmentException {
		try {
			if (clusters == null || centroids == null || clusters.size() != centroids.size()) 
				throw new AssignmentException("La cantidad de clústeres y centroides no coincide o es inválida.");

			boolean change = false;
			Location dummy_depot;

			System.out.println("change: " + change);

			for(int i = 0; i < clusters.size(); i++) 
			{
				if(!clusters.get(i).get_items_of_cluster().isEmpty())
					dummy_depot = recalculate_centroid(clusters.get(i));
				else
					dummy_depot = centroids.get(i).get_location_depot(); 

				System.out.println("------------------------------------------------------------------");
				System.out.println("DUMMY_DEPOT" + i + " X: " + dummy_depot.get_axis_x());
				System.out.println("DUMMY_DEPOT" + i + " Y: " + dummy_depot.get_axis_y());

				System.out.println("CENTROIDE" + i + " X: " + centroids.get(i).get_location_depot().get_axis_x());
				System.out.println("CENTROIDE" + i + " Y: " + centroids.get(i).get_location_depot().get_axis_y());

				if((centroids.get(i).get_location_depot().get_axis_x() != dummy_depot.get_axis_x()) || (centroids.get(i).get_location_depot().get_axis_y() != dummy_depot.get_axis_y())) 
				{
					change = true;

					centroids.get(i).set_id_depot(-1);

					Location location = new Location();
					location.set_axis_x(dummy_depot.get_axis_x());
					location.set_axis_y(dummy_depot.get_axis_y());
					centroids.get(i).set_location_depot(location);	

					System.out.println("change: " + change);
					System.out.println("CENTROIDE" + i + " X: " + centroids.get(i).get_location_depot().get_axis_x());
					System.out.println("CENTROIDE" + i + " Y: " + centroids.get(i).get_location_depot().get_axis_y());
				}
				else
				{
					System.out.println("CENTROIDE" + i + " X: " + centroids.get(i).get_location_depot().get_axis_x());
					System.out.println("CENTROIDE" + i + " Y: " + centroids.get(i).get_location_depot().get_axis_y());
				}
			}	
			if(change)
				update_centroids(clusters, centroids, distance_type);

			System.out.println("CAMBIO LOS CENTROIDES: " + change);

			return change;
		} catch (ProblemException | CostMatrixException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error durante la verificación y actualización de los centroides.", e);
		}
	}
	
	/**
	 * Verifica si los centroides han cambiado con respecto a los clústeres y los actualiza si es necesario.
	 * Esta versión utiliza distancias reales para la actualización.
	 *
	 * @param clusters Lista de clústeres actuales.
	 * @param centroids Lista de centroides a verificar.
	 * @return true si se detectaron cambios en los centroides, false en caso contrario.
	 * @throws ProblemException Si ocurre un error al acceder al problema.
	 * @throws CostMatrixException Si ocurre un error con la matriz de costos.
	 * @throws InstantiationException Si ocurre un error durante la instancia de centroides.
	 * @throws AssignmentException Si ocurre un error inesperado durante la verificación.
	 */
	protected boolean verify_centroids(ArrayList<Cluster> clusters, ArrayList<Depot> centroids) 
			throws ProblemException, CostMatrixException, InstantiationException, AssignmentException {
		try {
			if (clusters == null || centroids == null || clusters.size() != centroids.size()) 
				throw new AssignmentException("La cantidad de clústeres y centroides no coincide o es inválida.");

			boolean change = false;
			Location dummy_depot;

			System.out.println("change: " + change);

			for(int i = 0; i < clusters.size(); i++) 
			{
				if(!clusters.get(i).get_items_of_cluster().isEmpty())
					dummy_depot = recalculate_centroid(clusters.get(i));
				else
					dummy_depot = centroids.get(i).get_location_depot(); 

				System.out.println("------------------------------------------------------------------");
				System.out.println("DUMMY_DEPOT" + i + " X: " + dummy_depot.get_axis_x());
				System.out.println("DUMMY_DEPOT" + i + " Y: " + dummy_depot.get_axis_y());

				System.out.println("CENTROIDE" + i + " X: " + centroids.get(i).get_location_depot().get_axis_x());
				System.out.println("CENTROIDE" + i + " Y: " + centroids.get(i).get_location_depot().get_axis_y());

				if((centroids.get(i).get_location_depot().get_axis_x() != dummy_depot.get_axis_x()) || (centroids.get(i).get_location_depot().get_axis_y() != dummy_depot.get_axis_y())) 
				{
					change = true;

					centroids.get(i).set_id_depot(-1);

					Location location = new Location();
					location.set_axis_x(dummy_depot.get_axis_x());
					location.set_axis_y(dummy_depot.get_axis_y());
					centroids.get(i).set_location_depot(location);	

					System.out.println("change: " + change);
					System.out.println("CENTROIDE" + i + " X: " + centroids.get(i).get_location_depot().get_axis_x());
					System.out.println("CENTROIDE" + i + " Y: " + centroids.get(i).get_location_depot().get_axis_y());
				}
				else
				{
					System.out.println("CENTROIDE" + i + " X: " + centroids.get(i).get_location_depot().get_axis_x());
					System.out.println("CENTROIDE" + i + " Y: " + centroids.get(i).get_location_depot().get_axis_y());
				}
			}	
			if(change)
				update_centroids(clusters, centroids);

			System.out.println("CAMBIO LOS CENTROIDES: " + change);

			return change;
		} catch (ProblemException | CostMatrixException | InstantiationException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error durante la verificación y actualización de los centroides (distancias reales).", e);
		}
	}
	
	/**
	 * Actualiza los centroides usando una matriz de distancias entre centroides y depósitos basada en el tipo de distancia indicado.
	 *
	 * @param clusters Lista de clústeres actuales.
	 * @param centroids Lista de centroides a actualizar.
	 * @param distance_type Tipo de distancia a usar para construir la matriz de costos.
	 * @throws ProblemException Si ocurre un error al acceder al problema.
	 * @throws CostMatrixException Si ocurre un error con la matriz de costos.
	 * @throws AssignmentException Si ocurre un error inesperado durante la actualización.
	 */
	private void update_centroids(ArrayList<Cluster> clusters, ArrayList<Depot> centroids, DistanceType distance_type) 
			throws ProblemException, CostMatrixException, AssignmentException {		
		try {
			NumericMatrix cost_matrix = Problem.get_problem().calculate_cost_matrix(centroids, Problem.get_problem().get_depots(), distance_type);
			ArrayList<Depot> temp_centroids = new ArrayList<Depot>(centroids);

			int total_centroids = centroids.size();
			RowCol rc_best_all = new RowCol();
			int pos_centroid = -1;
			int pos_depot = -1;

			System.out.println("-------------------------------------" );
			for(int i = 0; i < centroids.size(); i++)
			{
				System.out.println("CENTROIDE ID: " + centroids.get(i).get_id_depot());
				System.out.println("CENTROIDE X: " + centroids.get(i).get_location_depot().get_axis_x());
				System.out.println("CENTROIDE Y: " + centroids.get(i).get_location_depot().get_axis_y());		
			}
			for(int i = 0; i < cost_matrix.getRowCount(); i++)
			{
				for(int j = 0; j < cost_matrix.getColCount(); j++){
					System.out.println("Row: " + i + " Col: " + j + " VALUE: " + cost_matrix.getItem(i, j));
				}
				System.out.println("---------------------------------------------");
			}		
			while(!cost_matrix.fullMatrix(0, 0, (total_centroids  - 1), (total_centroids - 1), Double.POSITIVE_INFINITY))
			{
				rc_best_all = cost_matrix.indexLowerValue();

				System.out.println("BestAllRow: " + rc_best_all.getRow());
				System.out.println("BestAllCol: " + rc_best_all.getCol());
				System.out.println("COSTO: " + cost_matrix.getItem(rc_best_all.getRow(), rc_best_all.getCol()));

				pos_centroid = rc_best_all.getRow();
				pos_depot = rc_best_all.getCol();

				System.out.println("POSICIÓN DEL CENTROIDE: " + pos_centroid);
				System.out.println("POSICIÓN DEL DEPOSITO: " + pos_depot);

				if(pos_centroid != pos_depot)
				{
					Depot depot = new Depot();

					depot.set_id_depot(temp_centroids.get(pos_centroid).get_id_depot());
					System.out.println("ID CENTROIDE: " + temp_centroids.get(pos_centroid).get_id_depot());

					double axisX = 0.0; 
					double axisY = 0.0; 
					axisX = temp_centroids.get(pos_centroid).get_location_depot().get_axis_x();
					axisY = temp_centroids.get(pos_centroid).get_location_depot().get_axis_y();

					Location location = new Location();
					location.set_axis_x(axisX);
					location.set_axis_y(axisY);
					depot.set_location_depot(location);

					ArrayList<Fleet> fleet = new ArrayList<Fleet>();
					fleet.addAll(temp_centroids.get(pos_centroid).get_fleet_depot());
					depot.set_fleet_depot(fleet);

					centroids.set(pos_depot, depot);
				}
				cost_matrix.fillValue(0, pos_depot, (total_centroids - 1), pos_depot, Double.POSITIVE_INFINITY);
				cost_matrix.fillValue(pos_centroid, 0, pos_centroid, (total_centroids - 1), Double.POSITIVE_INFINITY);

				for(int i = 0; i < cost_matrix.getRowCount(); i++)
				{
					for(int j = 0; j < cost_matrix.getColCount(); j++){
						System.out.println("Row: " + i + " Col: " + j + " VALUE: " + cost_matrix.getItem(i, j));
					}
					System.out.println("---------------------------------------------");
				}		
			}
		} catch (IllegalArgumentException | SecurityException e) {
			throw new AssignmentException("Error al construir la matriz de costos para centroides.", e);
		}
	}
	
	/**
	 * Actualiza los centroides usando distancias reales entre centroides y depósitos.
	 *
	 * @param clusters Lista de clústeres actuales.
	 * @param centroids Lista de centroides a actualizar.
	 * @throws ProblemException Si ocurre un error al acceder al problema.
	 * @throws CostMatrixException Si ocurre un error con la matriz de costos.
	 * @throws InstantiationException Si ocurre un error durante la instancia de centroides.
	 * @throws AssignmentException Si ocurre un error inesperado durante la actualización.
	 */
	private void update_centroids(ArrayList<Cluster> clusters, ArrayList<Depot> centroids) 
			throws ProblemException, CostMatrixException, InstantiationException, AssignmentException {		
		try {
			NumericMatrix cost_matrix = Problem.get_problem().calculate_cost_matrix_real(centroids, Problem.get_problem().get_depots());;
			ArrayList<Depot> temp_centroids = new ArrayList<Depot>(centroids);

			int total_centroids = centroids.size();
			RowCol rc_best_all = new RowCol();
			int pos_centroid = -1;
			int pos_depot = -1;

			System.out.println("-------------------------------------" );
			for(int i = 0; i < centroids.size(); i++)
			{
				System.out.println("CENTROIDE ID: " + centroids.get(i).get_id_depot());
				System.out.println("CENTROIDE X: " + centroids.get(i).get_location_depot().get_axis_x());
				System.out.println("CENTROIDE Y: " + centroids.get(i).get_location_depot().get_axis_y());		
			}
			for(int i = 0; i < cost_matrix.getRowCount(); i++)
			{
				for(int j = 0; j < cost_matrix.getColCount(); j++){
					System.out.println("Row: " + i + " Col: " + j + " VALUE: " + cost_matrix.getItem(i, j));
				}
				System.out.println("---------------------------------------------");
			}		
			while(!cost_matrix.fullMatrix(0, 0, (total_centroids  - 1), (total_centroids - 1), Double.POSITIVE_INFINITY))
			{
				rc_best_all = cost_matrix.indexLowerValue();

				System.out.println("BestAllRow: " + rc_best_all.getRow());
				System.out.println("BestAllCol: " + rc_best_all.getCol());
				System.out.println("COSTO: " + cost_matrix.getItem(rc_best_all.getRow(), rc_best_all.getCol()));

				pos_centroid = rc_best_all.getRow();
				pos_depot = rc_best_all.getCol();

				System.out.println("POSICIÓN DEL CENTROIDE: " + pos_centroid);
				System.out.println("POSICIÓN DEL DEPOSITO: " + pos_depot);

				if(pos_centroid != pos_depot)
				{
					Depot depot = new Depot();

					depot.set_id_depot(temp_centroids.get(pos_centroid).get_id_depot());
					System.out.println("ID CENTROIDE: " + temp_centroids.get(pos_centroid).get_id_depot());

					double axisX = 0.0; 
					double axisY = 0.0; 
					axisX = temp_centroids.get(pos_centroid).get_location_depot().get_axis_x();
					axisY = temp_centroids.get(pos_centroid).get_location_depot().get_axis_y();

					Location location = new Location();
					location.set_axis_x(axisX);
					location.set_axis_y(axisY);
					depot.set_location_depot(location);

					ArrayList<Fleet> fleet = new ArrayList<Fleet>();
					fleet.addAll(temp_centroids.get(pos_centroid).get_fleet_depot());
					depot.set_fleet_depot(fleet);

					centroids.set(pos_depot, depot);
				}
				cost_matrix.fillValue(0, pos_depot, (total_centroids - 1), pos_depot, Double.POSITIVE_INFINITY);
				cost_matrix.fillValue(pos_centroid, 0, pos_centroid, (total_centroids - 1), Double.POSITIVE_INFINITY);

				for(int i = 0; i < cost_matrix.getRowCount(); i++)
				{
					for(int j = 0; j < cost_matrix.getColCount(); j++){
						System.out.println("Row: " + i + " Col: " + j + " VALUE: " + cost_matrix.getItem(i, j));
					}
					System.out.println("---------------------------------------------");
				}		
			}
		} catch (IllegalArgumentException | SecurityException e) {
			throw new AssignmentException("Error al construir la matriz de costos real para centroides.", e);
		}
	}
}