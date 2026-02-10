package cujae.inf.ic.om.assignment.clustering.partitional;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;

import cujae.inf.ic.om.service.OSRMService;

import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.CostMatrixException;
import cujae.inf.ic.om.exceptions.ProblemException;
import cujae.inf.ic.om.factory.DistanceType;

import cujae.inf.ic.om.matrix.NumericMatrix;

import java.util.ArrayList;

public class Farthest_First extends AbstractByCentroids {
	
	private ArrayList<Integer> list_id_elements;
	//private ArrayList<Integer> listIDElements1;
	//private ArrayList<Integer> listIDElements2;
	
	private ArrayList<Cluster> list_clusters;
	private ArrayList<Customer> list_customers_to_assign;
	private ArrayList<Depot> list_centroids;
	
	public Farthest_First() {
		super();
	}
	
	public int getCurrentIteration(){
		return current_iteration;
	}
	
	@Override
	public Solution to_clustering() throws ProblemException, CostMatrixException, AssignmentException, ClusterException {
		try {
			initialize();
			assign();
			return finish();
		} catch (ClusterException | CostMatrixException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la ejecución de la adaptación del algoritmo Farthest-First.", e);
		}
	}
	
	@Override
	public void initialize() throws ClusterException, ProblemException, CostMatrixException, AssignmentException {
		try {
			if (distance_type == null)
				throw new AssignmentException("El tipo de distancia no ha sido especificado.");
			
			list_id_elements = generate_elements(distance_type);
			//listIDElements1 = generateElementsXXX(distanceType);
			//listIDElements2 = generateElementsXX(distanceType);
			if (list_id_elements == null || list_id_elements.isEmpty())
				throw new AssignmentException("La lista de elementos generados está vacía.");
			
			list_clusters = initialize_clusters(list_id_elements);
			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("No se pudieron inicializar los clústeres con los elementos proporcionados.");
			
			list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
			list_centroids = new ArrayList<Depot>();

			current_iteration = 0;
		} catch (ClusterException | ProblemException | CostMatrixException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de inicialización.", e);
		}
	}

	@Override
	public void assign() throws ProblemException, CostMatrixException, AssignmentException {
		try {
			if (list_id_elements == null || list_id_elements.isEmpty())
				throw new AssignmentException("La lista de elementos iniciales está vacía. Asegúrese de llamar a initialize() antes.");

			if (list_clusters == null || list_clusters.isEmpty())
				throw new AssignmentException("Los clústeres no han sido inicializados.");

			boolean first = true;
			boolean change = false;

			do 
			{
				if(first)
				{
					update_customer_to_assign(list_customers_to_assign, list_id_elements);
					list_centroids = create_centroids(list_id_elements);
					
					if (list_centroids == null || list_centroids.isEmpty())
						throw new AssignmentException("Error al crear los centroides a partir de los elementos.");

					first = false;
				}
				else
				{
					clean_clusters(list_clusters);
					list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
				}
				if (list_customers_to_assign.isEmpty())
					throw new AssignmentException("No hay clientes para asignar.");
				
				NumericMatrix cost_matrix = initialize_cost_matrix(list_customers_to_assign, list_centroids, distance_type);
				if (cost_matrix == null)
					throw new AssignmentException("No se pudo generar la matriz de costos para la asignación.");
				
				step_assignment(list_clusters, list_customers_to_assign, cost_matrix);

				if (distance_type == DistanceType.Real)
					change = verify_centroids(list_clusters, list_centroids);
				else 
					change = verify_centroids(list_clusters, list_centroids, distance_type);

				current_iteration ++;

				System.out.println("ITERACIÓN ACTUAL: " + current_iteration);

			} while((change) && (current_iteration < count_max_iterations));
		} catch (CostMatrixException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de asignación.", e);
		}
	}
	
	@Override
	public Solution finish() throws AssignmentException {
		try {
			Solution solution = new Solution();
			
			if (list_customers_to_assign == null)
				throw new AssignmentException("La lista de clientes no ha sido inicializada.");

			if(!list_customers_to_assign.isEmpty())					
				for(int j = 0; j < list_customers_to_assign.size(); j++)	
					solution.get_unassigned_items().add(list_customers_to_assign.get(j).get_id_customer());

			if (list_clusters == null)
				throw new AssignmentException("La lista de clústeres no ha sido inicializada.");
			
			if(!list_clusters.isEmpty())
				for(int k = 0; k < list_clusters.size(); k++)
					if(!list_clusters.get(k).get_items_of_cluster().isEmpty())
						solution.get_clusters().add(list_clusters.get(k));

			OSRMService.clear_distance_cache();

			return solution;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la finalización.", e);
		}
	}
}