package cujae.inf.ic.om.controller.tools;

import cujae.inf.ic.om.exceptions.CostMatrixException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.matrix.NumericMatrix;

import java.util.ArrayList;
import java.util.Random;

import java.math.BigDecimal;

public abstract class AbstractTools {
	
	/**
	 * Ordena aleatoriamente los depósitos del problema y actualiza la matriz de costos
	 * para reflejar el nuevo orden.
	 *
	 * @throws ProblemException Si ocurre un error relacionado con los datos del problema.
	 * @throws CostMatrixException Si ocurre un error al manipular la matriz de costos.
	 */
	public static void random_ordenate() 
			throws ProblemException, CostMatrixException {
		Random random = new Random();
		int pos_random = -1;
		
		ArrayList<Depot> ordered_depots = new ArrayList<Depot>();
		ArrayList<Depot> copy_depots = new ArrayList<Depot>(Problem.get_problem().get_depots());
		
		while(!Problem.get_problem().get_depots().isEmpty())
		{
			pos_random = random.nextInt(Problem.get_problem().get_depots().size());
			ordered_depots.add(Problem.get_problem().get_depots().remove(pos_random));
		}
		for(int i = 0; i < ordered_depots.size(); i++)
			Problem.get_problem().get_depots().add(ordered_depots.get(i));
		
		NumericMatrix copy_cost_matrix = new NumericMatrix(Problem.get_problem().get_cost_matrix());
		int current_depots = 0;
		int total_depots = Problem.get_problem().get_total_depots();
		int total_customers = Problem.get_problem().get_total_customers();
		
		while(current_depots < total_depots)
		{
			copy_cost_matrix.deleteCol(total_customers);			
			current_depots++;
		}
		int pos_depot;
		
		for(int j = 0; j < copy_depots.size(); j++)
		{
			pos_depot = Problem.get_problem().find_pos_depot(copy_depots, Problem.get_problem().get_depots().get(j).get_id_depot());
			copy_cost_matrix.addCol((total_customers + j), Problem.get_problem().get_cost_matrix().getCol(total_customers + pos_depot));
		}	
		current_depots = 0;
		
		while(current_depots < total_depots)
		{
			copy_cost_matrix.deleteRow(total_customers);
			current_depots++;
		}
		
		for(int k = 0; k < copy_depots.size(); k++)
		{
			pos_depot = Problem.get_problem().find_pos_depot(copy_depots, Problem.get_problem().get_depots().get(k).get_id_depot());
			copy_cost_matrix.addRow((total_customers + k), Problem.get_problem().get_cost_matrix().getRow(total_customers + pos_depot));
		}	
		Problem.get_problem().set_cost_matrix(copy_cost_matrix);
	}
	
	/**
	 * Ordena los depósitos del problema en orden descendente según su capacidad total
	 * y actualiza la matriz de costos.
	 *
	 * @throws ProblemException Si ocurre un error relacionado con los datos del problema.
	 * @throws CostMatrixException Si ocurre un error al manipular la matriz de costos.
	 */
	public static void descendent_ordenate() 
			throws ProblemException, CostMatrixException {
		Depot depot = new Depot();
		ArrayList<Depot> copy_depots = new ArrayList<Depot>(Problem.get_problem().get_depots());
		int total_depots = Problem.get_problem().get_total_depots();
		
		for(int i = 0; i < (total_depots - 1); i++)
		{
			for(int j = 0; j < (total_depots - i - 1); j++)
			{
				if(Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depots().get(j + 1)) > Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depots().get(j)))
				{
					depot = Problem.get_problem().get_depots().get(j + 1);
					Problem.get_problem().get_depots().set((j + 1), Problem.get_problem().get_depots().get(j));
					Problem.get_problem().get_depots().set(j, depot);
				}
			}
		}
		NumericMatrix copy_cost_matrix = new NumericMatrix(Problem.get_problem().get_cost_matrix());
		int current_depots = 0;
		int total_customers = Problem.get_problem().get_total_customers();
		
		while(current_depots < total_depots)
		{
			copy_cost_matrix.deleteCol(total_customers);
			current_depots++;
		}
		int pos_depot;
		
		for(int j = 0; j < copy_depots.size(); j++)
		{
			pos_depot = Problem.get_problem().find_pos_depot(copy_depots, Problem.get_problem().get_depots().get(j).get_id_depot());
			copy_cost_matrix.addCol((total_customers + j), Problem.get_problem().get_cost_matrix().getCol(total_customers + pos_depot));
		}	
		current_depots = 0;
		
		while(current_depots < total_depots)
		{
			copy_cost_matrix.deleteRow(total_customers);
			current_depots++;
		}
		for(int k = 0; k < copy_depots.size(); k++)
		{
			pos_depot = Problem.get_problem().find_pos_depot(copy_depots, Problem.get_problem().get_depots().get(k).get_id_depot());
			copy_cost_matrix.addRow((total_customers + k), Problem.get_problem().get_cost_matrix().getRow(total_customers + pos_depot));
		}	
		Problem.get_problem().set_cost_matrix(copy_cost_matrix);
	}

	/**
	 * Ordena los depósitos del problema en orden ascendente según su capacidad total
	 * y actualiza la matriz de costos.
	 *
	 * @throws ProblemException Si ocurre un error relacionado con los datos del problema.
	 * @throws CostMatrixException Si ocurre un error al manipular la matriz de costos.
	 */
	public static void ascendent_ordenate() 
			throws ProblemException, CostMatrixException {
		Depot depot = new Depot();
		ArrayList<Depot> copy_depots = new ArrayList<Depot>(Problem.get_problem().get_depots());
		int total_depots = Problem.get_problem().get_total_depots();
		
		for(int i = 0; i < (total_depots - 1); i++)
		{
			for(int j = 0; j < (total_depots - i - 1); j++)
			{
				if(Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depots().get(j + 1)) < Problem.get_problem().get_total_capacity_by_depot(Problem.get_problem().get_depots().get(j)))
				{
					depot = Problem.get_problem().get_depots().get(j + 1);
					Problem.get_problem().get_depots().set((j + 1), Problem.get_problem().get_depots().get(j));
					Problem.get_problem().get_depots().set(j, depot);
				}
			}
		}
		NumericMatrix copy_cost_matrix = new NumericMatrix(Problem.get_problem().get_cost_matrix());
		int current_depots = 0;
		int total_customers = Problem.get_problem().get_total_customers();
		
		while(current_depots < total_depots)
		{
			copy_cost_matrix.deleteCol(total_customers);
			current_depots++;
		}
		int pos_depot;
		
		for(int j = 0; j < copy_depots.size(); j++)
		{
			pos_depot = Problem.get_problem().find_pos_depot(copy_depots, Problem.get_problem().get_depots().get(j).get_id_depot());
			copy_cost_matrix.addCol((total_customers + j), Problem.get_problem().get_cost_matrix().getCol(total_customers + pos_depot));
		}	
		current_depots = 0;
		
		while(current_depots < total_depots)
		{
			copy_cost_matrix.deleteRow(total_customers);
			current_depots++;
		}
		for(int k = 0; k < copy_depots.size(); k++)
		{
			pos_depot = Problem.get_problem().find_pos_depot(copy_depots, Problem.get_problem().get_depots().get(k).get_id_depot());
			copy_cost_matrix.addRow((total_customers + k), Problem.get_problem().get_cost_matrix().getRow(total_customers + pos_depot));
		}	
		Problem.get_problem().set_cost_matrix(copy_cost_matrix);
	}

	/**
	 * Ordena aleatoriamente una lista de clientes sin modificar la matriz de costos.
	 *
	 * @param customers Lista de clientes a ordenar aleatoriamente.
	 */
	public static void random_ordenate(ArrayList<Customer> customers) {
		Random random = new Random();
		int pos_random = -1;
		ArrayList<Customer> ordered_customers = new ArrayList<Customer>();
		
		while(!customers.isEmpty())
		{
			pos_random = random.nextInt(customers.size());
			ordered_customers.add(customers.remove(pos_random));
		}
		for(int i = 0; i < ordered_customers.size(); i++)
			customers.add(ordered_customers.get(i));
		
		/* no necesito actualizar la matriz*/
	}
	
	/**
	 * Ordena una lista de clientes en orden descendente según la demanda.
	 *
	 * @param customers Lista de clientes a ordenar.
	 * @throws ProblemException Si ocurre un error al acceder a la información del problema.
	 */
	public static void descendent_ordenate(ArrayList<Customer> customers) 
			throws ProblemException {
		Customer customer = new Customer();
		int total_customers = customers.size();
		
		for(int i = 0; i < (total_customers - 1); i++)
		{
			for(int j = 0; j < (total_customers - i - 1); j++)
			{
				if(Problem.get_problem().get_request_by_id_customer(customers.get(j + 1).get_id_customer()) > Problem.get_problem().get_request_by_id_customer(customers.get(j).get_id_customer()))
				{
					customer = customers.get(j + 1);
					customers.set((j + 1), customers.get(j));
					customers.set(j, customer);
					
					/* no actualizo problem*/
				}
			}
		}
	}

	/**
	 * Ordena una lista de clientes en orden ascendente según la demanda.
	 *
	 * @param customers Lista de clientes a ordenar.
	 * @throws ProblemException Si ocurre un error al acceder a la información del problema.
	 */
	public static void ascendent_ordenate(ArrayList<Customer> customers) 
			throws ProblemException {
		Customer customer = new Customer();
		int total_customers = customers.size();
		
		for(int i = 0; i < (total_customers - 1); i++)
		{
			for(int j = 0; j < (total_customers - i - 1); j++)
			{
				if(Problem.get_problem().get_request_by_id_customer(customers.get(j + 1).get_id_customer()) < Problem.get_problem().get_request_by_id_customer(customers.get(j).get_id_customer()))
				{
					customer = customers.get(j + 1);
					customers.set(j+1, customers.get(j));
					customers.set(j, customer);
				}
			}
		}
	}
	
	/**
	 * Trunca un número double al número especificado de cifras decimales.
	 *
	 * @param number Número a truncar.
	 * @param decimalPlace Cantidad de cifras decimales deseadas.
	 * @return Número truncado.
	 */
	public static double truncate_double(double number, int decimalPlace) {
        double number_round;
        BigDecimal big_dec = new BigDecimal(number);
        big_dec = big_dec.setScale(decimalPlace, BigDecimal.ROUND_DOWN);
        number_round = big_dec.doubleValue();
        
        return number_round;
    }
}