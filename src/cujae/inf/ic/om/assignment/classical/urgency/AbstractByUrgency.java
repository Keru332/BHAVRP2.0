package cujae.inf.ic.om.assignment.classical.urgency;

import cujae.inf.ic.om.assignment.classical.AbstractHeuristic;

import cujae.inf.ic.om.exceptions.CostMatrixException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.matrix.NumericMatrix;
import cujae.inf.ic.om.matrix.RowCol;

import java.util.ArrayList;

public abstract class AbstractByUrgency extends AbstractHeuristic {
	
	/**
     * Calcula la urgencia como la diferencia entre una distancia de referencia y la más cercana.
     *
     * @param first_distance  Distancia más cercana.
     * @param other_distance  Otra distancia de referencia.
     * @return Urgencia calculada.
     */
	protected double calculate_urgency(double first_distance, double other_distance) {		
		return other_distance - first_distance;	
	}
	
	/**
     * Para cada cliente, genera una lista con los identificadores de los depósitos ordenados por cercanía.
     *
     * @param list_customers_to_assign Lista de clientes a asignar.
     * @param list_id_depots Lista de identificadores de depósitos.
     * @param cost_matrix Matriz de costos.
     * @return Lista de listas con los depósitos ordenados por cercanía para cada cliente.
     * @throws CostMatrixException Si ocurre un error al acceder a la matriz de costos.
     */
	protected ArrayList<ArrayList<Integer>> get_depots_ordered(ArrayList<Customer> list_customers_to_assign, 
			ArrayList<Integer> list_id_depots, NumericMatrix cost_matrix) throws CostMatrixException {
		ArrayList<ArrayList<Integer>> list_nearest_depots_by_customer = new ArrayList<ArrayList<Integer>>();

		for(int i = 0; i < list_customers_to_assign.size(); i++)
			list_nearest_depots_by_customer.add(get_depots_ordered_by_customer(list_customers_to_assign.get(i).get_id_customer(), list_id_depots, cost_matrix, list_id_depots.size()));
		
		return list_nearest_depots_by_customer;
	}
		
	/**
     * Dado un cliente, genera una lista de depósitos ordenados por cercanía.
     *
     * @param id_customer ID del cliente.
     * @param list_id_depots Lista de identificadores de depósitos.
     * @param cost_matrix Matriz de costos.
     * @param current_depots Cantidad de depósitos disponibles actualmente.
     * @return Lista ordenada de identificadores de depósitos más cercanos.
     * @throws CostMatrixException Si ocurre un error al consultar la matriz de costos.
     */
	private ArrayList<Integer> get_depots_ordered_by_customer(int id_customer, ArrayList<Integer> list_id_depots, 
			NumericMatrix cost_matrix, int current_depots) throws CostMatrixException {
		ArrayList<Integer> list_closest_depots_by_customer = new ArrayList<Integer>();

		try {
			int pos_customer = Problem.get_problem().get_pos_element(id_customer);		
			int total_customers = Problem.get_problem().get_customers().size();
			int total_depots = list_id_depots.size();

			RowCol rc_current_best_depot;
			int counter = 0;

			int id_closest_depot = -1;
			int pos_closest_depot = -1;

			while(counter < current_depots)
			{
				rc_current_best_depot = cost_matrix.indexLowerValue(total_customers, pos_customer, (total_customers + total_depots - 1),  pos_customer);	

				pos_closest_depot = rc_current_best_depot.getRow() - total_customers;
				id_closest_depot = Problem.get_problem().get_list_id_depots().get(pos_closest_depot);
				list_closest_depots_by_customer.add(id_closest_depot);

				cost_matrix.setItem(rc_current_best_depot.getRow(), pos_customer, Double.POSITIVE_INFINITY);

				counter++;
			}
		} catch (Exception e) {
			throw new CostMatrixException("Error al obtener los depósitos ordenados para el cliente con ID " + id_customer + ".", e);
		}
		return list_closest_depots_by_customer;
	}
}