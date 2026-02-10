package cujae.inf.ic.om.evaluation.internalvalidation.metrics.combined;

import cujae.inf.ic.om.evaluation.internalvalidation.interfaces.ICombined;

import cujae.inf.ic.om.evaluation.internalvalidation.metrics.AbstractAverageDispersion;

import cujae.inf.ic.om.exceptions.MetricException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Location;

import cujae.inf.ic.om.problem.output.Cluster;

import java.util.ArrayList;

/**
 * Implementa el cálculo del Índice de Davies-Bouldin, una métrica de validación interna que evalúa 
 * la calidad de un agrupamiento mediante la relación entre la dispersión intra-clúster y la separación 
 * inter-clúster. Este índice busca favorecer agrupamientos donde los clústeres son compactos y 
 * están bien separados entre sí.
 * 
 * Valores bajos del índice indican un mejor agrupamiento, ya que reflejan una alta separación entre clústeres 
 * y una baja dispersión interna dentro de los clústeres.
 */
public class DaviesBouldinIndex extends AbstractAverageDispersion implements ICombined {

	/*Constructor de la clase DaviesBouldinIndex.*/
	public DaviesBouldinIndex() {
		super();
	}
	
    /**
     * Calcula el Índice de Davies-Bouldin global para un conjunto de clústeres.
     * 
     * @param clusters Lista de clústeres generados por un algoritmo de agrupamiento.
     * @return El valor del Índice de Davies-Bouldin calculado.
     * @throws MetricException Si la lista de clústeres es inválida, contiene clústeres vacíos o la distancia entre centroides es cero.
     * @throws ProblemException 
     */
	@Override
	public double evaluate_global(ArrayList<Cluster> clusters) throws MetricException, ProblemException {
	    if (clusters == null || clusters.isEmpty())
	        throw new MetricException("La lista de clústeres no puede ser nula ni vacía.");
	    
		if (clusters.size() < 2) 
			throw new MetricException("Se requieren al menos dos clústeres para calcular el Índice de Davies - Bouldin.");
	    
		int k = clusters.size();
        double db = 0.0;
        
        double[] sigma = new double[k];
        Location[] centroids = new Location[k];

        for (int i = 0; i < k; i++) 
        {
            Cluster cluster = clusters.get(i);
            ArrayList<Customer> customers = get_customer_of_cluster(cluster);
            
            if (customers == null || customers.isEmpty()) 
            	throw new MetricException("Cada clúster debe tener al menos un cliente asignado.");
            
            centroids[i] = calculate_centroid(customers);
            sigma[i] = calculate_average_distance(customers, centroids[i]);
        }
        for (int i = 0; i < k; i++) 
        {
            double max_ratio = 0.0;

            for (int j = 0; j < k; j++) 
            {
                if (i != j) {
                    double distance_between_centroids = Math.sqrt(calculate_distance_squared(centroids[i], centroids[j]));
                    
                    if (distance_between_centroids == 0) 
                    	throw new MetricException("La distancia entre centroides no puede ser cero.");
                    
                    double ratio = (sigma[i] + sigma[j]) / distance_between_centroids;
                    max_ratio = Math.max(max_ratio, ratio);
                }
            }
            db += max_ratio;
        }
		return db / k;
	}
}