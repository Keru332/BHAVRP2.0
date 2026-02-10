package cujae.inf.ic.om.evaluation.internalvalidation.metrics.cohesion;

import cujae.inf.ic.om.evaluation.internalvalidation.interfaces.ICohesion;

import cujae.inf.ic.om.evaluation.internalvalidation.metrics.AbstractAverageDispersion;

import cujae.inf.ic.om.exceptions.MetricException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Location;

import cujae.inf.ic.om.problem.output.Cluster;

import java.util.ArrayList;

/**
 * Implementa el cálculo del Índice de Ball-Hall, una métrica de validación interna que evalúa la calidad de un
 * agrupamiento considerando exclusivamente la dispersión intra-clúster. Este índice mide la dispersión
 * dentro de cada clúster y debe minimizarse, ya que valores más bajos indican clústeres más compactos.
 */
public class BallHallIndex extends AbstractAverageDispersion implements ICohesion {

	/*Constructor de la clase BallHallIndex.*/
	public BallHallIndex() {
		super();
	}
	
    /**
     * Calcula el Índice de Ball-Hall para un de clúster.
     * 
     * @param cluster El clúster a evaluar.
     * @return El valor de dispersión promedio del clúster.
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
        return calculate_average_distance(customers, centroid);
	}	
}