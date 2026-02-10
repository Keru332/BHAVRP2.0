package cujae.inf.ic.om.evaluation.internalvalidation.metrics.separation;

import cujae.inf.ic.om.evaluation.internalvalidation.interfaces.ISeparation;

import cujae.inf.ic.om.evaluation.internalvalidation.metrics.AbstractMetric;

import cujae.inf.ic.om.exceptions.MetricException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Location;

import cujae.inf.ic.om.problem.output.Cluster;

import java.util.ArrayList;

/**
 * Implementación de la métrica Sum of Squares Between (SSB).
 * 
 * Esta métrica evalúa la separación entre los clústeres midiendo la distancia entre sus centroides
 * y el centroide global del conjunto de datos.
 */
public class SumOfSquaresBetween extends AbstractMetric implements ISeparation {
	
	/*Constructor de la clase SumOfSquaresBetween.*/
	public SumOfSquaresBetween() {
		super();
	}

    /**
     * Evalúa la métrica SSB entre dos clústeres.
     *
     * @param first_cluster Primer clúster.
     * @param second_cluster Segundo clúster.
     * @return Valor de SSB entre ambos clústeres.
     * @throws MetricException Si alguno de los clústeres es nulo o está vacío.
     * @throws ProblemException 
     */
	@Override
	public double evaluate_separation(Cluster first_cluster, Cluster second_cluster) throws MetricException, ProblemException {
		if (first_cluster == null || second_cluster == null) 
			throw new MetricException("Ambos clústeres deben ser diferentes de null.");

		ArrayList<Customer> customers_1 = get_customer_of_cluster(first_cluster);
        ArrayList<Customer> customers_2 = get_customer_of_cluster(second_cluster);
		
        if (customers_1 == null || customers_2 == null ||
                customers_1.isEmpty() || customers_2.isEmpty()) 
        	throw new MetricException("Ambos clústeres deben tener al menos un cliente asignado.");
            
        Location centroid_1 = calculate_centroid(customers_1);
        Location centroid_2 = calculate_centroid(customers_2);

		return calculate_distance_squared(centroid_1, centroid_2);
	}
}