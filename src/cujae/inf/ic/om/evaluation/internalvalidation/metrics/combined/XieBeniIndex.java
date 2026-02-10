package cujae.inf.ic.om.evaluation.internalvalidation.metrics.combined;

import cujae.inf.ic.om.evaluation.internalvalidation.interfaces.ICombined;

import cujae.inf.ic.om.evaluation.internalvalidation.metrics.AbstractMetric;
import cujae.inf.ic.om.evaluation.internalvalidation.metrics.cohesion.SumOfSquaredErrors;

import cujae.inf.ic.om.exceptions.MetricException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Location;

import cujae.inf.ic.om.problem.output.Cluster;

import java.util.ArrayList;

/**
 * Implementa el cálculo del Índice de Xie-Beni, una métrica de validación interna que evalúa la calidad 
 * de un agrupamiento mediante la relación entre la dispersión dentro de los clústeres (SSE) y 
 * la mínima distancia al cuadrado entre los centroides de los clústeres. Este índice busca capturar 
 * tanto la compactación de los clústeres como su separación, penalizando agrupamientos donde los 
 * centroides están demasiado cercanos entre sí o donde los puntos están lejos de sus respectivos centroides.
 * 
 * Valores bajos del índice indican un mejor agrupamiento, ya que reflejan una alta separación entre clústeres y una baja
 * dispersión interna dentro de los clústeres.
 */
public class XieBeniIndex extends AbstractMetric implements ICombined {

	/*Constructor de la clase XieBeniIndex.*/
	public XieBeniIndex() {
		super();
	}
	
	/**
     * Calcula el Índice de Xie-Beni global para un conjunto de clústeres.
     * 
     * @param clusters Lista de clústeres generados por un algoritmo de agrupamiento.
     * @return El valor del Índice de Xie-Beni calculado.
	 * @throws MetricException Si los datos son inválidos o se produce un error durante el cálculo.
	 * @throws ProblemException 
     */
	@Override
	public double evaluate_global(ArrayList<Cluster> clusters) throws MetricException, ProblemException {
		if (clusters == null || clusters.isEmpty()) 
        	throw new MetricException("La lista de clústeres no puede ser nula ni vacía.");

		if (clusters.size() < 2) 
			throw new MetricException("Se requieren al menos dos clústeres para calcular el Índice de Xie - Beni.");
		
		SumOfSquaredErrors sse = new SumOfSquaredErrors();
		
		double SSE_Global = 0.0;
		
		for (Cluster cluster : clusters) 
			SSE_Global += sse.evaluate_cohesion(cluster);
		
		double min_distance_squared = calculate_min_centroid_distance_squared(clusters);
		
        if (min_distance_squared == 0) 
        	throw new MetricException("El índice de Xie-Beni no está definido cuando la distancia mínima entre centroides es cero.");
		
		return (SSE_Global / clusters.size()) / min_distance_squared;
	}
	
	/* Calcula la mínima distancia al cuadrado entre los centroides de los clústeres.*/
	private double calculate_min_centroid_distance_squared(ArrayList<Cluster> clusters) throws MetricException, ProblemException {
		double min_distance_squared = Double.MAX_VALUE;
		
		for (int i = 0; i < clusters.size(); i++) 
		{
            Cluster cluster_one = clusters.get(i);
            ArrayList<Customer> list_of_customers_one = get_customer_of_cluster(cluster_one);

            if (list_of_customers_one == null || list_of_customers_one.isEmpty()) 
            	throw new MetricException("El clúster " + i + " debe contener al menos un cliente.");
            
            Location centroid_one = calculate_centroid(list_of_customers_one);
            
            for (int j = i + 1; j < clusters.size(); j++) 
            {
                Cluster cluster_two = clusters.get(j);
                ArrayList<Customer> list_of_customers_two = get_customer_of_cluster(cluster_two);
                
                if (list_of_customers_two == null || list_of_customers_two.isEmpty()) 
                	throw new MetricException("El clúster " + j + " debe contener al menos un cliente.");

                Location centroid_two = calculate_centroid(list_of_customers_two);
                double distance_squared = calculate_distance_squared(centroid_one, centroid_two);
                min_distance_squared = Math.min(min_distance_squared, distance_squared);
            }
        }
		return min_distance_squared;
	}
}