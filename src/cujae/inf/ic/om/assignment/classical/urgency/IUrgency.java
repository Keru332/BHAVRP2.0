package cujae.inf.ic.om.assignment.classical.urgency;

import cujae.inf.ic.om.problem.input.Customer;

import cujae.inf.ic.om.exceptions.CostMatrixException;
import cujae.inf.ic.om.matrix.NumericMatrix;

import java.util.ArrayList;

public interface IUrgency {

	/*Retorna un listado con las urgencias de los clientes del listado entrado por parámetro*/
	ArrayList<Double> get_list_urgencies(ArrayList<Customer> list_customers_to_assign, ArrayList<ArrayList<Integer>> list_id_depots, NumericMatrix urgency_matrix) throws CostMatrixException;
	
	/*Método encargado de obtener la urgencia*/
	double get_urgency(int id_customer, ArrayList<Integer> list_id_depots, NumericMatrix urgency_matrix) throws CostMatrixException;
}