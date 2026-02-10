package cujae.inf.ic.om.evaluation.internalvalidation.metrics.combined;

import cujae.inf.ic.om.evaluation.internalvalidation.interfaces.ICombined;

import cujae.inf.ic.om.evaluation.internalvalidation.metrics.AbstractMetric;
import cujae.inf.ic.om.evaluation.internalvalidation.metrics.cohesion.SumOfSquaredErrors;

import cujae.inf.ic.om.exceptions.MetricException;
import cujae.inf.ic.om.exceptions.ProblemException;

//import cujae.inf.ic.om.evaluation.metrics.separation.SumOfSquaresBetween;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Location;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;

import java.util.ArrayList;

/**
 * Implementa el cálculo del Índice de Calinski-Harabasz, una métrica de validación interna que evalúa 
 * la calidad de un agrupamiento mediante la relación entre la dispersión entre clústeres y la dispersión 
 * dentro de los clústeres. Este índice combina las métricas SSE (Sum of Squared Errors) y 
 * SSB (Sum of Squares Between) para proporcionar una medida unificada de la calidad del agrupamiento.
 * 
 * Valores altos del índice indican un mejor agrupamiento, ya que reflejan una alta separación entre clústeres y una
 * baja dispersión interna dentro de los clústeres.
 */
public class CalinskiHarabaszIndex extends AbstractMetric implements ICombined {

	/*Constructor de la clase CalinskiHarabaszIndex.*/
	public CalinskiHarabaszIndex() {
		super();
	}
	
	/**
     * Calcula el Índice de Calinski-Harabasz global para un conjunto de clústeres.
     * 
     * @param clusters Lista de clústeres generados por un algoritmo de agrupamiento.
     * @return El valor del Índice de Calinski-Harabasz calculado.
     * @throws MetricException Si los parámetros son inválidos o si ocurre un error durante el cálculo.
	 * @throws ProblemException 
     */
	@Override
	public double evaluate_global(ArrayList<Cluster> clusters) throws MetricException, ProblemException {        
	    if (clusters == null || clusters.isEmpty()) 
	        throw new MetricException("La lista de clústeres no puede ser nula ni vacía.");
		
	    if (clusters.size() < 2) 
			throw new MetricException("Se requieren al menos dos clústeres para calcular el Índice de Calinski - Harabasz.");

		int k = clusters.size();
		int N = Problem.get_problem().get_total_customers();

        if (k <= 1) 
        	 throw new MetricException("El índice de Calinski-Harabasz no está definido para k <= 1.");
        
        if (N <= k) 
        	throw new MetricException("El número de clientes debe ser mayor que el número de clústeres.");

        ArrayList<Customer> all_customers = Problem.get_problem().get_customers();
        Location global_centroid = calculate_centroid(new ArrayList<>(all_customers));
        
		//SumOfSquaresBetween ssb = new SumOfSquaresBetween();
		double SSB_Global = 0.0;
				
		for (Cluster cluster : clusters) 
		{
		    ArrayList<Customer> customers = get_customer_of_cluster(cluster);
		    Location cluster_centroid = calculate_centroid(customers);
		    double distance_squared = calculate_distance_squared(cluster_centroid, global_centroid);
		    SSB_Global += customers.size() * distance_squared;
		}
		    
		SumOfSquaredErrors sse = new SumOfSquaredErrors();
		double SSE_Global = 0.0;
		
		for (Cluster cluster : clusters) 
			SSE_Global += sse.evaluate_cohesion(cluster);
		
        if (SSE_Global == 0) 
        	throw new MetricException("El índice de Calinski-Harabasz no está definido cuando SSE es igual a cero.");
		
		return (SSB_Global / (SSE_Global / clusters.size())) * ((double) (N - k) / (k - 1));
	}
}