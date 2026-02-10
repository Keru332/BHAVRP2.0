package cujae.inf.ic.om.assignment.classical.cluster;

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

public class CoefficientPropagation extends AbstractByNotUrgency {

	public static double degradation_coefficient = 0.5;

	private Solution solution = new Solution();	
	
	private ArrayList<Cluster> list_clusters;
	private ArrayList<Customer> list_customers_to_assign;
	private NumericMatrix cost_matrix;
	private ArrayList<Double> list_coefficients;
	private ArrayList<ArrayList<Double>> list_scaled_distances;
	private NumericMatrix scaled_matrix; 

	public CoefficientPropagation() {
		super();
	}

	@Override
	public Solution to_clustering() throws ClusterException, CostMatrixException, ProblemException, AssignmentException {
		try {
			if(degradation_coefficient > 1 || degradation_coefficient < 0)
				degradation_coefficient = 0.5;

			initialize();
			assign();
			return finish();
		} catch (ClusterException | CostMatrixException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la ejecución de la heurística Coefficient Propagation.", e);
		}
	}	
		
	@Override
	public void initialize() throws ClusterException, CostMatrixException, AssignmentException {
		try {
			list_clusters = initialize_clusters();
			if (list_clusters == null || list_clusters.isEmpty())
				throw new ClusterException("Error al inicializar los clústeres: lista vacía o nula.");

			list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
			if (list_customers_to_assign.isEmpty())
				throw new AssignmentException("No hay clientes disponibles para asignar.");

			cost_matrix = initialize_cost_matrix(Problem.get_problem().get_customers(), Problem.get_problem().get_depots(), distance_type);
			list_coefficients = initialize_coefficients();
			if (list_coefficients == null || list_coefficients.isEmpty())
				throw new AssignmentException("No se pudieron inicializar los coeficientes de atracción.");

			list_scaled_distances = new ArrayList<ArrayList<Double>>(fill_list_scaled_distances());
			if (list_scaled_distances.isEmpty())
				throw new AssignmentException("No se pudieron calcular las distancias escaladas.");

			scaled_matrix = initialize_scaled_matrix(list_scaled_distances); 
		} catch (ClusterException | CostMatrixException e) {
			throw e;
		} catch (Exception e) {
	        throw new AssignmentException("Error inesperado durante la fase de inicialización: " + e.getMessage(), e);
	    }
	}

	@Override
	public void assign() throws ProblemException, CostMatrixException, 
		ClusterException, AssignmentException {
		try {
			if (list_customers_to_assign == null || list_customers_to_assign.isEmpty())
				throw new AssignmentException("No hay clientes por asignar.");

			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("No se han definido clústeres válidos.");

			if (scaled_matrix == null)
				throw new AssignmentException("La matriz escalada no fue inicializada correctamente.");

			int pos_customer = -1;
			int id_customer = -1;
			double request_customer = 0.0;

			int pos_depot = -1;
			int id_depot = -1;
			double capacity_depot = 0.0;

			int pos_cluster = -1;
			double request_cluster = 0.0;

			int pos_element = -1;
			RowCol rc_best_all = new RowCol();

			int total_items = list_customers_to_assign.size();
			int total_clusters = Problem.get_problem().get_depots().size();

			while((!list_customers_to_assign.isEmpty()) && (!list_clusters.isEmpty()) && (!scaled_matrix.fullMatrix(Double.POSITIVE_INFINITY)))
			{
				rc_best_all = scaled_matrix.indexLowerValue();
				if (rc_best_all == null)
					throw new AssignmentException("No se pudo encontrar un valor válido en la matriz escalada.");

				pos_customer = rc_best_all.getCol(); // puede devolver un deposito?
				if (pos_customer >= total_items)
					throw new AssignmentException("Se obtuvo una columna que no representa un cliente válido.");

				id_customer = Problem.get_problem().get_list_id_customers().get(pos_customer);
				request_customer = Problem.get_problem().get_request_by_id_customer(id_customer);

				pos_element = rc_best_all.getRow();

				if(pos_element >= total_items)	
				{
					id_depot =  Problem.get_problem().get_list_id_depots().get((pos_element - total_items)).intValue(); 

					pos_cluster = find_cluster(id_depot, list_clusters);
					pos_depot = pos_element;
				}
				else
				{
					pos_cluster = get_pos_cluster(pos_element, list_clusters);

					id_depot = list_clusters.get(pos_cluster).get_id_cluster();
					pos_depot = Problem.get_problem().get_pos_element(id_depot);
				}

				if(pos_cluster != -1)
				{
					request_cluster = list_clusters.get(pos_cluster).get_request_cluster();
					capacity_depot = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(id_depot));

					if(capacity_depot >= (request_cluster + request_customer))
					{
						request_cluster += request_customer;

						list_clusters.get(pos_cluster).set_request_cluster(request_cluster);
						list_clusters.get(pos_cluster).get_items_of_cluster().add(id_customer);

						calculate_attraction_coefficient(pos_customer, pos_element, list_coefficients);
						scaled_matrix.fillValue(0, pos_customer, (total_items + total_clusters - 1), pos_customer, Double.POSITIVE_INFINITY);

						list_customers_to_assign.remove(Problem.get_problem().find_pos_customer(list_customers_to_assign, id_customer));
						update_scaled_matrix(list_customers_to_assign, pos_customer, scaled_matrix, list_coefficients);

						if(is_full_depot(list_customers_to_assign, request_cluster, capacity_depot))
						{
							scaled_matrix.fillValue((pos_depot), 0, (pos_depot), (total_items + total_clusters - 1), Double.POSITIVE_INFINITY);
							for(int i = 0; i < list_clusters.get(pos_cluster).get_items_of_cluster().size(); i++)
							{
								int pos_element_one = Problem.get_problem().get_pos_element(list_clusters.get(pos_cluster).get_items_of_cluster().get(i));
								scaled_matrix.fillValue(pos_element_one, 0, pos_element_one, (total_items + total_clusters - 1), Double.POSITIVE_INFINITY);	
							}
							if(!(list_clusters.get(pos_cluster).get_items_of_cluster().isEmpty()))
								solution.get_clusters().add(list_clusters.remove(pos_cluster));
							else
								list_clusters.remove(pos_cluster);
						}
					}
					else
					{	if(scaled_matrix.fullMatrix(0, pos_customer, total_items + total_clusters - 1, pos_customer, Double.POSITIVE_INFINITY))
					{
						solution.get_unassigned_items().add(id_customer);
						list_customers_to_assign.remove(Problem.get_problem().find_pos_customer(list_customers_to_assign, id_customer));
					}
					scaled_matrix.setItem(pos_element, pos_customer, Double.POSITIVE_INFINITY);
					}
				}
			}
		} catch (ProblemException | CostMatrixException | ClusterException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de asignación: " + e.getMessage(), e);
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
			throw new AssignmentException("Error inesperado durante la fase de finalización: " + e.getMessage(), e);
		}
	}

	/**
	 * Calcula el nuevo coeficiente de atracción para un cliente específico.
	 *
	 * @param pos_customer Posición del cliente.
	 * @param pos_element Posición del cliente por el que se asignó.
	 * @param coefficients Lista de coeficientes.
	 * @throws AssignmentException si ocurre un error al actualizar el coeficiente.
	 */
	private void calculate_attraction_coefficient(int pos_customer, int pos_element, ArrayList<Double> coefficients) throws AssignmentException {
		try {
			if (coefficients == null || coefficients.isEmpty()) 
	            throw new AssignmentException("La lista de coeficientes está vacía o es nula.");
	        
	        if (pos_customer < 0 || pos_customer >= coefficients.size()) 
	            throw new AssignmentException("Índice de cliente fuera de rango en la lista de coeficientes.");
			
			double current_att_coeff = 1.0;
			double new_att_coeff = 1.0;

			if(pos_element < coefficients.size())
			{
				current_att_coeff = coefficients.get(pos_element);
				new_att_coeff = Math.min(1, (current_att_coeff + (current_att_coeff * degradation_coefficient)));
			}
			else
				new_att_coeff = 1;

			coefficients.set(pos_customer, new_att_coeff);
		} catch (Exception e) {
			throw new AssignmentException("Error al calcular el coeficiente de atracción.", e);
		}
	}
	
	/**
	 * Inicializa la lista de coeficientes de atracción con valores en cero.
	 *
	 * @return Lista de coeficientes.
	 * @throws AssignmentException si ocurre un error durante la inicialización.
	 */
	private ArrayList<Double> initialize_coefficients() throws AssignmentException {
	    try {
	        ArrayList<Double> coefficients = new ArrayList<>();
	        int total_items = Problem.get_problem().get_total_customers();

	        for (int i = 0; i < total_items; i++)
	            coefficients.add(0.0);

	        return coefficients;
	    } catch (Exception e) {
	        throw new AssignmentException("Error al inicializar la lista de coeficientes.", e);
	    }
	}
	
	/**
	 * Llena una lista de listas con las distancias escaladas.
	 *
	 * @return Lista de listas de distancias escaladas.
	 * @throws CostMatrixException si ocurre un error accediendo a la matriz de costos.
	 */
	private ArrayList<ArrayList<Double>> fill_list_scaled_distances() throws CostMatrixException {
		ArrayList<ArrayList<Double>> scaled_distances = new ArrayList<ArrayList<Double>>();

		ArrayList<Integer> list_id_customers = new ArrayList<Integer>(Problem.get_problem().get_list_id_customers());
		ArrayList<Integer> list_id_depots = new ArrayList<Integer>(Problem.get_problem().get_list_id_depots());

		 if (list_id_customers == null || list_id_depots == null) 
	            throw new CostMatrixException("Listas de clientes o depósitos nulas.");
		
		int total_items = list_id_customers.size();
		int total_clusters = list_id_depots.size();

		for(int i = 0; i < total_items; i++)
		{	  
			ArrayList<Double> list_distances = new ArrayList<Double>();

			for(int j = 0; j < (total_items + total_clusters); j++)
				list_distances.add(Double.POSITIVE_INFINITY);

			scaled_distances.add(list_distances);
		}
		int pos_customer_matrix;
		int pos_depot_matrix;

		for(int i = 0; i < total_clusters; i++)
		{
			ArrayList<Double> list_distances = new ArrayList<Double>();
			pos_depot_matrix = Problem.get_problem().get_pos_element(list_id_depots.get(i));

			for(int j = 0; j < total_items; j++)
			{
				pos_customer_matrix = Problem.get_problem().get_pos_element(list_id_customers.get(j));
				list_distances.add(cost_matrix.getItem(pos_customer_matrix, pos_depot_matrix)); //ahora viendolo de nuevo, no estoy segura si es asi o fila posDepotMatrix, y la columna la del cliente
			}
			for(int k = total_items; k < (total_items + total_clusters); k++)
				list_distances.add(Double.POSITIVE_INFINITY);

			scaled_distances.add(list_distances);
		}

		return scaled_distances;
	}
	
	/**
	 * Inicializa la matriz de distancias escaladas a partir de listas de distancias.
	 *
	 * @param scaled_distances Lista de listas con distancias.
	 * @return Matriz numérica escalada.
	 * @throws AssignmentException si ocurre un error al inicializar la matriz.
	 */
	private NumericMatrix initialize_scaled_matrix(ArrayList<ArrayList<Double>> scaled_distances) throws AssignmentException {
		try {
			if (scaled_distances == null || scaled_distances.isEmpty()) 
	            throw new AssignmentException("Las distancias escaladas están vacías o son nulas.");
	        
			int size = scaled_distances.size(); 
			NumericMatrix scaledMatrix = new NumericMatrix(size, size);

			int row = -1;
			int col = -1;
			double costInDistance = 0.0;

			for(int i = 0; i < scaled_distances.size(); i++)
			{
				row = i;

				for(int j = 0; j < scaled_distances.get(i).size(); j++)
				{
					col = j;	
					costInDistance = scaled_distances.get(i).get(j);
					scaledMatrix.setItem(row, col, costInDistance);	
				}
			}
			return scaledMatrix;
		} catch (Exception e) {
			throw new AssignmentException("Error al inicializar la matriz escalada.", e);
		}
	}
	
	/**
	 * Actualiza la matriz de distancias escaladas tras asignar un cliente.
	 *
	 * @param customers_to_assign Clientes restantes por asignar.
	 * @param pos_customer Índice del nuevo cliente asignado.
	 * @param scaled_matrix Matriz de distancias escaladas.
	 * @param coefficients Lista de coeficientes.
	 * @throws CostMatrixException si ocurre un error accediendo a la matriz.
	 */
	private void update_scaled_matrix(ArrayList<Customer> customers_to_assign, int pos_customer, 
			NumericMatrix scaled_matrix, ArrayList<Double> coefficients) throws CostMatrixException {
		if (customers_to_assign == null || coefficients == null || scaled_matrix == null) 
			throw new CostMatrixException("Parámetros nulos en update_scaled_matrix.");

		if (pos_customer < 0 || pos_customer >= coefficients.size()) 
			throw new CostMatrixException("Índice del cliente asignado fuera de rango.");

		int pos_new_customer = -1;
		double scaled_distance = 0.0;
		double distance = 0.0;	

		for(int i = 0; i < customers_to_assign.size(); i++)
		{
			pos_new_customer = Problem.get_problem().get_pos_element(customers_to_assign.get(i).get_id_customer());
			distance = cost_matrix.getItem(pos_new_customer, pos_customer);
			scaled_distance = distance * coefficients.get(pos_customer);

			scaled_matrix.setItem(pos_customer, pos_new_customer, scaled_distance);
		}
	}
	
	/**
	 * Devuelve la posición del clúster al que debe asignarse un cliente.
	 *
	 * @param pos_customer Índice del cliente.
	 * @param clusters Lista de clústeres.
	 * @return Índice del clúster correspondiente, o -1 si no se encuentra.
	 * @throws AssignmentException si ocurre un error accediendo a datos.
	 */
	private int get_pos_cluster(int pos_customer, ArrayList<Cluster> clusters) throws AssignmentException {
		try {
			if (clusters == null || clusters.isEmpty()) 
				throw new AssignmentException("La lista de clústeres está vacía o es nula.");

			int pos_cluster = -1;
			int id_customer = Problem.get_problem().get_list_id_customers().get(pos_customer);
			int i = 0;
			int j;
			boolean found = false;

			while((i < clusters.size()) && (!found))
			{
				j = 0;
				if(!clusters.get(i).get_items_of_cluster().isEmpty())
				{
					while((j < clusters.get(i).get_items_of_cluster().size()) && (!found))
					{
						if(clusters.get(i).get_items_of_cluster().get(j) == id_customer)
						{
							pos_cluster = i;
							found = true;
						}
						else
							j++;
					}
				}
				i++;
			}
			return pos_cluster;
		} catch (Exception e) {
			throw new AssignmentException("Error al determinar la posición del clúster para el cliente con posición " + pos_customer + ".", e);
		}
	}
}