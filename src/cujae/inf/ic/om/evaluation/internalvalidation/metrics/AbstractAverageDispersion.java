package cujae.inf.ic.om.evaluation.internalvalidation.metrics;

import cujae.inf.ic.om.exceptions.MetricException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Location;

import java.util.ArrayList;

/**
 * Clase abstracta base para métricas que calculan la dispersión promedio intra-clúster.
 * 
 * Métricas que heredan de esta clase:
 * - Índice de Ball-Hall (evalúa la dispersión promedio intra-clúster).
 * - Índice de Davies-Bouldin (combina cohesión y separación).
 */
public abstract class AbstractAverageDispersion extends AbstractMetric {
	
	/**
	 * Calcula la dispersión promedio de los puntos en un clúster respecto a su centroide.
	 * 
	 * @param customers Lista de clientes asignados al clúster.
	 * @param centroid Centroide del clúster.
	 * @return La dispersión promedio de los puntos en el clúster respecto al centroide.
	 * @throws MetricException Si hay error de cálculo o datos inválidos.
	 */
    protected double calculate_average_distance(ArrayList<Customer> customers, Location centroid) throws MetricException {
    	if (customers == null || customers.isEmpty()) 
            throw new MetricException("The list of customers cannot be null or empty.");
        
    	double total_distance = 0.0;

        for (Customer customer : customers) {
            double distance = Math.sqrt(calculate_distance_squared(customer.get_location_customer(), centroid));

	        if (distance < 0) 
	            throw new MetricException("Negative distance detected between customer and centroid: " + distance);
	        
	        total_distance += distance;
        }
        return total_distance / customers.size();
    }
}