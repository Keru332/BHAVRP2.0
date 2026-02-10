package cujae.inf.ic.om.assignment.classical;

import cujae.inf.ic.om.exceptions.AssignmentException;

import cujae.inf.ic.om.problem.input.Customer;

import java.util.ArrayList;

public abstract class AbstractByNotUrgency extends AbstractHeuristic {
	
	/**
	 * Determina si el depósito ya no puede recibir más clientes, evaluando si existe
	 * algún cliente cuya demanda sea menor o igual a la capacidad restante del clúster.
	 *
	 * @param customers Lista de clientes pendientes por asignar.
	 * @param request_cluster Demanda cubierta actualmente por el clúster.
	 * @param capacity_depot Capacidad total del depósito correspondiente.
	 * @return true si el depósito está lleno, false en caso contrario.
	 * @throws AssignmentException si los parámetros de entrada son inválidos.
	 */
	protected boolean is_full_depot(ArrayList<Customer> customers, double request_cluster, 
			double capacity_depot) throws AssignmentException {
		if (customers == null) 
			throw new AssignmentException("La lista de clientes no puede ser nula.");
		
		if (capacity_depot < 0 || request_cluster < 0) 
			throw new AssignmentException("Los valores de capacidad del depósito o demanda cubierta no pueden ser negativos.");
		
		if (request_cluster > capacity_depot) 
			throw new AssignmentException("La demanda cubierta no puede superar la capacidad del depósito.");
		
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
}