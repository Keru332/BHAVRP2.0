package cujae.inf.ic.om.evaluation.internalvalidation.metrics.combined;

import cujae.inf.ic.om.evaluation.internalvalidation.interfaces.ICombined;

import cujae.inf.ic.om.evaluation.internalvalidation.metrics.AbstractMetric;

import cujae.inf.ic.om.exceptions.MetricException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Location;

import cujae.inf.ic.om.problem.output.Cluster;

import java.util.ArrayList;

/**
 * Implementa el cálculo del Índice de Ray-Turi, una métrica de validación interna que evalúa la calidad de un
 * agrupamiento mediante la relación entre la dispersión dentro de los clústeres y la mínima distancia
 * al cuadrado entre los centroides de los clústeres. Este índice busca favorecer agrupamientos donde
 * los clústeres son compactos y están bien separados entre sí.
 * 
 * Valores bajos del índice indican un mejor agrupamiento, ya que reflejan alta separación entre clústeres y baja
 * dispersión interna dentro de los clústeres.
 */
public class RayTuriIndex extends AbstractMetric implements ICombined {

	/*Constructor de la clase RayTuriIndex.*/
	public RayTuriIndex() {
		super();
	}
	
    /**
     * Calcula el Índice de Ray-Turi global para un conjunto de clústeres.
     * 
     * @param clusters Lista de clústeres generados por un algoritmo de agrupamiento.
     * @return El valor del Índice de Ray-Turi calculado.
     * @throws MetricException Si los datos son inválidos o se produce un error durante el cálculo.
     * @throws ProblemException 
     */
	@Override
	public double evaluate_global(ArrayList<Cluster> clusters) throws MetricException, ProblemException {
	    if (clusters == null || clusters.isEmpty()) 
	        throw new MetricException("La lista de clústeres no puede ser nula ni vacía.");
	    
		if (clusters.size() < 2) 
			throw new MetricException("Se requieren al menos dos clústeres para calcular el Índice de Ray - Turi.");
	    
		int k = clusters.size();
		double total_within_cluster_distance = 0.0;
		double min_centroid_distance_squared = Double.MAX_VALUE;
		
		for (Cluster cluster: clusters) 
		{
			ArrayList<Customer> customers = get_customer_of_cluster(cluster);
			
			if (customers == null || customers.isEmpty()) 
				 throw new MetricException("Cada clúster debe tener al menos un cliente asignado.");
			
			Location centroid = calculate_centroid(customers);
			total_within_cluster_distance += calculate_sum_of_squared_distances(customers, centroid);
		}
		for (int i = 0; i < k; i++) 
		{
			Cluster cluster_one = clusters.get(i);
            ArrayList<Customer> list_of_customers_one = get_customer_of_cluster(cluster_one);
            Location centroid1 = calculate_centroid(list_of_customers_one);

            for (int j = i + 1; j < k; j++) 
            {
                Cluster cluster_two = clusters.get(j);
                ArrayList<Customer> list_of_customers_two = get_customer_of_cluster(cluster_two);
                Location centroid2 = calculate_centroid(list_of_customers_two);

                double distanceSquared = calculate_distance_squared(centroid1, centroid2);
                min_centroid_distance_squared = Math.min(min_centroid_distance_squared, distanceSquared);
            }
		}
		if (min_centroid_distance_squared == 0) 
			throw new MetricException("La distancia mínima entre centroides no puede ser cero.");
        
		return total_within_cluster_distance / min_centroid_distance_squared;
	}
	
    /* Calcula la suma de las distancias al cuadrado entre los puntos y su centroide.*/
    private double calculate_sum_of_squared_distances(ArrayList<Customer> customers, Location centroid) throws MetricException {
        if (customers == null || customers.isEmpty()) 
        	throw new MetricException("La lista de clientes no puede ser nula ni vacía.");

        double sum_of_squared_distances = 0.0;

        for (Customer customer : customers) 
        {
            double distance_squared = calculate_distance_squared(customer.get_location_customer(), centroid);

            if (distance_squared < 0) 
            	throw new MetricException("Se detectó una distancia al cuadrado negativa: " + distance_squared);
            
            sum_of_squared_distances += distance_squared;
        }
        return sum_of_squared_distances;
    }
}