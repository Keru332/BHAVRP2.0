package cujae.inf.ic.om.evaluation.internalvalidation.metrics.combined;

import cujae.inf.ic.om.evaluation.internalvalidation.interfaces.ICombined;

import cujae.inf.ic.om.evaluation.internalvalidation.metrics.AbstractMetric;

import cujae.inf.ic.om.exceptions.MetricException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;

import cujae.inf.ic.om.problem.output.Cluster;

import java.util.ArrayList;

/**
 * Implementa el cálculo del Coeficiente de Silueta, una métrica de validación interna
 * que evalúa simultáneamente la cohesión intra-clúster y la separación inter-clúster.
 * 
 * El Coeficiente de Silueta proporciona un valor entre -1 y 1 para cada punto:
 * - Valores cercanos a 1 indican que el punto está bien asignado y está claramente dentro de su clúster.
 * - Valores cercanos a 0 indican que el punto está en la frontera entre dos clústeres.
 * - Valores negativos indican que el punto podría estar mal asignado, ya que está más cerca de otro clúster.
 */
public class SilhouetteCoefficient extends AbstractMetric implements ICombined {

	/*Constructor de la clase SilhouetteCoefficient.*/
	public SilhouetteCoefficient() {
		super();
	}
	
    /**
     * Calcula el Coeficiente de Silueta global para un conjunto de clústeres.
     * 
     * @param clusters Lista de clústeres generados por un algoritmo de agrupamiento.
     * @return El Coeficiente de Silueta global, que es el promedio de los coeficientes
     *         de silueta de todos los puntos en todos los clústeres.
     * @throws MetricException Si los datos son inválidos o se produce un error durante el cálculo.
     * @throws ProblemException 
     */
	@Override
	public double evaluate_global(ArrayList<Cluster> clusters) throws MetricException, ProblemException {
		if (clusters == null || clusters.isEmpty()) 
	        throw new MetricException("La lista de clústeres no puede ser nula ni vacía.");
	    
		if (clusters.size() < 2) 
			throw new MetricException("Se requieren al menos dos clústeres para calcular el coeficiente de Silueta.");
		
		double global_silhouette_sum = 0.0;
        int total_customers = 0;

        for (Cluster cluster : clusters) 
        {
            ArrayList<Customer> customers = get_customer_of_cluster(cluster);
            total_customers += customers.size();

            for (Customer customer : customers) 
            {
                double a = calculate_a(customer, cluster);
                double b = calculate_b(customer, cluster, clusters);
                
                if (Double.isNaN(a) || Double.isNaN(b) || a < 0 || b < 0) 
                	throw new MetricException("Error al calcular 'a' o 'b': se obtuvieron valores inválidos.");
                
                double silhouette = (b - a) / Math.max(a, b);
                if (Double.isNaN(silhouette) || Double.isInfinite(silhouette)) 
                	throw new MetricException("El coeficiente de Silueta calculado no es válido: " + silhouette);
                    
                global_silhouette_sum += silhouette;
            }
        }
        if (total_customers == 0) 
        	throw new MetricException("No hay clientes en los clústeres para calcular el coeficiente de Silueta.");
        
        return global_silhouette_sum / total_customers;
	}
	
	/* Calcula la cohesión intra-clúster (valor 'a') para un cliente dado.*/
	private double calculate_a(Customer customer, Cluster cluster) throws MetricException, ProblemException {
        ArrayList<Customer> customers = get_customer_of_cluster(cluster);
        
        if (customers == null || customers.size() <= 1) 
        	throw new MetricException("El clúster debe contener al menos dos clientes para calcular la cohesión interna.");
        
        double distance_sum = 0.0;

        for (Customer other_customer : customers) 
        {
            if (!customer.equals(other_customer)) 
            {
            	double distance_squared = calculate_distance_squared(customer.get_location_customer(), 
            			other_customer.get_location_customer());
                
                if (distance_squared < 0) 
                	throw new MetricException("Se detectó una distancia negativa al calcular la cohesión interna.");
            	
            	distance_sum += distance_squared;
            }
        }
        return Math.sqrt(distance_sum / (customers.size() - 1));
    }
	
	/* Calcula la separación inter-clúster (valor 'b') para un cliente dado.*/
	private double calculate_b(Customer customer, Cluster cluster, ArrayList<Cluster> clusters) throws MetricException, ProblemException {
        if (clusters == null || clusters.size() <= 1) 
        	throw new MetricException("Debe haber al menos dos clústeres para calcular la separación entre clústeres.");
		
        double min_average_distance = Double.MAX_VALUE;

        for (Cluster other_cluster : clusters) 
        {
            if (!other_cluster.equals(cluster)) 
            {
                ArrayList<Customer> other_customers = get_customer_of_cluster(other_cluster);
                
                if (other_customers == null || other_customers.isEmpty()) 
                    continue;
                
                double distance_sum = 0.0;

                for (Customer other_customer : other_customers) 
                {
                	double distance_squared = calculate_distance_squared(customer.get_location_customer(), 
                			other_customer.get_location_customer());
                	
                    if (distance_squared < 0) 
                    	throw new MetricException("Se detectó una distancia negativa al calcular la separación inter-clúster.");
                	
                    distance_sum += distance_squared;
                }
                double average_distance = Math.sqrt(distance_sum / other_customers.size());
                min_average_distance = Math.min(min_average_distance, average_distance);
            }
        }
        if (min_average_distance == Double.MAX_VALUE) 
        	throw new MetricException("No fue posible calcular la separación inter-clúster.");
        
        return min_average_distance;
    }
}