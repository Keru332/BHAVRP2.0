package cujae.inf.ic.om.evaluation.internalvalidation.metrics.cohesion;

import cujae.inf.ic.om.evaluation.internalvalidation.interfaces.ICohesion;

import cujae.inf.ic.om.evaluation.internalvalidation.metrics.AbstractMetric;

import cujae.inf.ic.om.exceptions.MetricException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Location;

import cujae.inf.ic.om.problem.output.Cluster;

import java.util.ArrayList;

/**
 * Implementación de la métrica Sum of Squared Errors (SSE).
 * 
 * Esta métrica evalúa la cohesión dentro de los clústeres midiendo la suma de las distancias cuadráticas
 * entre cada punto y el centroide de su clúster.
 */
public class SumOfSquaredErrors extends AbstractMetric implements ICohesion {

	/*Constructor de la clase SumOfSquaredErrors.*/
	public SumOfSquaredErrors() {
		super();
	}
	
	/**
	 * Evalúa la métrica SSE para un único clúster.
	 *
	 * @param cluster El clúster a evaluar.
	 * @return El valor de SSE para el clúster dado.
	 * @throws MetricException Si el clúster es nulo o no contiene clientes.
	 * @throws ProblemException 
	 */
	@Override
	public double evaluate_cohesion(Cluster cluster) throws MetricException, ProblemException {
		if (cluster == null) 
			throw new MetricException("El clúster no puede ser null.");
		
		ArrayList<Customer> customers = get_customer_of_cluster(cluster);
		if (customers == null || customers.isEmpty()) 
			throw new MetricException("El clúster debe contener al menos un cliente.");

		Location centroid = calculate_centroid(customers);
		double sse = 0.0;

		for (Customer customer : customers) 
		{
			Location customer_location = customer.get_location_customer();
			double distance_squared = calculate_distance_squared(customer_location, centroid);
            sse += distance_squared;
		}
		return sse;
	}
}