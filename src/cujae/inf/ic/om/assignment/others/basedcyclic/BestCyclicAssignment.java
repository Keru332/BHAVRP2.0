package cujae.inf.ic.om.assignment.others.basedcyclic;

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

/*Clase que modela como asignar el mejor cliente al último cliente - depósito asignando en forma paralela */
public class BestCyclicAssignment extends AbstractByNotUrgency {
	private Solution solution = new Solution();
	
	private ArrayList<Cluster> clusters;
	private ArrayList<Customer> customers_to_assign;
	private NumericMatrix cost_matrix;
    	
	public BestCyclicAssignment() {
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
			throw new AssignmentException("Error inesperado durante la ejecución de la heurística Best Cyclic Assignment.", e);
		}
	}
	
	@Override
	public void initialize() throws CostMatrixException, ClusterException, AssignmentException {
		try {
			clusters = initialize_clusters();
			if (clusters == null || clusters.isEmpty())
				throw new ClusterException("No fue posible inicializar los clústeres.");

			customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
			if (customers_to_assign.isEmpty())
				throw new AssignmentException("La lista de clientes a asignar está vacía.");

			cost_matrix = initialize_cost_matrix(customers_to_assign, Problem.get_problem().get_depots(), distance_type);
		} catch (ClusterException | CostMatrixException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado durante la fase de inicialización.", e);
		}
	}
	
	@Override
	public void assign() throws AssignmentException, ProblemException, ClusterException {
		try {
			if (customers_to_assign == null || clusters == null || cost_matrix == null)
				throw new AssignmentException("Las estructuras necesarias para la asignación no han sido correctamente inicializadas.");

			int pos_element_matrix = -1;

			double capacity_depot = 0.0;   

			double request_customer = 0.0;    

			double request_cluster = 0.0; 
			int pos_cluster = -1;

			RowCol rc_best_all_selected = null;

			int total_items = customers_to_assign.size();
			int total_clusters = Problem.get_problem().get_depots().size();

			ArrayList<Integer> items_selected = new ArrayList<>();

			for(int i = 0; i < total_clusters; i++) 
				items_selected.add((total_items + i));

			while((!customers_to_assign.isEmpty()) && (!clusters.isEmpty()))
			{
				rc_best_all_selected = get_best_value_of_selected(items_selected, cost_matrix, total_items);
				if (rc_best_all_selected == null)
					throw new AssignmentException("No se encontró un valor válido en la matriz de costos para continuar la asignación.");

				pos_element_matrix = rc_best_all_selected.getRow();

				if(pos_element_matrix >= total_items) 
					pos_cluster = pos_element_matrix - total_items;
				else
					pos_cluster = find_element_in_selected(items_selected, pos_element_matrix);

				if (pos_cluster < 0 || pos_cluster >= clusters.size())
					throw new AssignmentException("La posición del clúster calculada es inválida.");

				capacity_depot = Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depot_by_id_depot(clusters.get(pos_cluster).get_id_cluster()));

				request_customer = Problem.get_problem().get_customers().get(rc_best_all_selected.getCol()).get_request_customer();

				if(pos_cluster != -1)
				{
					request_cluster = clusters.get(pos_cluster).get_request_cluster();

					if (capacity_depot >= request_cluster + request_customer) 
					{	
						request_cluster += request_customer;

						clusters.get(pos_cluster).set_request_cluster(request_cluster);
						clusters.get(pos_cluster).get_items_of_cluster().add(Problem.get_problem().get_customers().get(rc_best_all_selected.getCol()).get_id_customer());

						customers_to_assign.remove(Problem.get_problem().find_pos_customer(customers_to_assign, Problem.get_problem().get_customers().get(rc_best_all_selected.getCol()).get_id_customer()));

						cost_matrix.fillValue(0, rc_best_all_selected.getCol(), (total_items  + total_clusters - 1), rc_best_all_selected.getCol(), Double.POSITIVE_INFINITY);
						cost_matrix.fillValue(pos_element_matrix, 0, pos_element_matrix, (total_items - 1), Double.POSITIVE_INFINITY);

						items_selected.remove(pos_cluster);
						items_selected.add(pos_cluster, rc_best_all_selected.getCol());
					} 
					else 
						cost_matrix.setItem(pos_element_matrix, rc_best_all_selected.getCol(), Double.POSITIVE_INFINITY);

					if(is_full_depot(customers_to_assign, request_cluster, capacity_depot))
					{
						if(!(clusters.get(pos_cluster).get_items_of_cluster().isEmpty()))
							solution.get_clusters().add(clusters.get(pos_cluster));

						clusters.remove(pos_cluster);
						clusters.add(pos_cluster, null);

						items_selected.remove(pos_cluster);	
						items_selected.add(pos_cluster, -1);
						cost_matrix.fillValue(pos_element_matrix, 0, rc_best_all_selected.getRow(), (total_items - 1), Double.POSITIVE_INFINITY);
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
    		if(!customers_to_assign.isEmpty())					
    			for(int i = 0; i < customers_to_assign.size(); i++)	
    				solution.get_unassigned_items().add(customers_to_assign.get(i).get_id_customer());

    		if(!clusters.isEmpty()) //no va a pasar
    			for(int k = 0; k < clusters.size(); k++)
    				if((clusters.get(k) != null) && (!clusters.get(k).get_items_of_cluster().isEmpty()))
    					solution.get_clusters().add(clusters.get(k));

    		OSRMService.clear_distance_cache();

    		return solution;
    	} catch (Exception e) {
    		throw new AssignmentException("Error inesperado durante la fase de finalización.", e);
    	}
    }    
	
    /**
     * Busca la mejor posición (menor valor) en la matriz de costos entre las filas correspondientes a los elementos seleccionados.
     * Omite los valores -1 de la lista y compara únicamente valores fuera de la diagonal principal.
     *
     * @param items_selected Lista de identificadores de elementos seleccionados.
     * @param cost_matrix Matriz de costos donde se realiza la búsqueda.
     * @param total_items Número total de elementos del problema (dimensión relevante de la matriz).
     * @return Objeto RowCol con la posición (fila y columna) del mejor valor encontrado.
     * @throws AssignmentException si la lista de elementos es nula o vacía, o si ocurre un error durante la búsqueda.
     */
    private RowCol get_best_value_of_selected(ArrayList<Integer> items_selected, NumericMatrix cost_matrix, 
    		int total_items) 
    				throws AssignmentException {
    	try {
    		if (items_selected == null || items_selected.isEmpty())
    			throw new AssignmentException("La lista de elementos seleccionados está vacía o es nula.");

    		if (cost_matrix == null)
    			throw new AssignmentException("La matriz de costos es nula.");

    		RowCol rc_best_all = null;

    		if((items_selected != null) && (!items_selected.isEmpty())) //esto no pasa
    		{	// verificar que no tome valores de la diagonal principal
    			RowCol rc_current = null;
    			boolean is_first = true;

    			//rcBestAll = costMatrix.indexLowerValue(itemsSelected.get(0), 0, itemsSelected.get(0), (totalItems - 1)); 
    			//función que busque la primera posicion que no es -1 en itemsSelected
    			for(int i = 0; i < items_selected.size(); i++)
    			{
    				if(items_selected.get(i) != -1)
    				{
    					if(is_first)
    					{
    						rc_best_all = cost_matrix.indexLowerValue(items_selected.get(i), 0, items_selected.get(i), (total_items - 1));
    						rc_current = rc_best_all;

    						is_first = false;
    					}		
    					else
    						rc_current = cost_matrix.indexLowerValue(items_selected.get(i), 0, items_selected.get(i), (total_items - 1));

    					if(cost_matrix.getItem(rc_current.getRow(), rc_current.getCol()) < cost_matrix.getItem(rc_best_all.getRow(), rc_best_all.getCol()))
    						rc_best_all = rc_current;
    				}			
    			}
    		}
    		if (rc_best_all == null)
    			throw new AssignmentException("No se encontró ningún valor válido en la matriz para los elementos seleccionados.");

    		return rc_best_all;
    	} catch (Exception e) {
    		throw new AssignmentException("Error al obtener el mejor valor entre los elementos seleccionados.", e);
    	}
    }
	
    /**
     * Busca la posición de un elemento dado dentro de la lista de elementos seleccionados.
     *
     * @param items_selected Lista de elementos seleccionados.
     * @param id_item Identificador del elemento a buscar.
     * @return Posición del elemento en la lista, o -1 si no se encuentra.
     * @throws AssignmentException si la lista es nula o si el identificador es nulo.
     */
    private int find_element_in_selected(ArrayList<Integer> items_selected, Integer id_item) 
    		throws AssignmentException {
    	try {
    		if (items_selected == null)
    			throw new AssignmentException("La lista de elementos seleccionados es nula.");

    		if (id_item == null)
    			throw new AssignmentException("El identificador del elemento es nulo.");

    		int pos_cluster = -1;
    		int i = 0;
    		boolean found = false;

    		while((i < items_selected.size()) && (!found))
    		{
    			if(items_selected.get(i).intValue() == id_item.intValue())
    			{
    				pos_cluster = i;
    				found = true;
    			}
    			else
    				i++;
    		}
    		return pos_cluster;
    	} catch (Exception e) {
    		throw new AssignmentException("Error al buscar el elemento dentro de los seleccionados.", e);
    	}
    }
}