package cujae.inf.ic.om.evaluation.internalvalidation.metrics.combined;

import cujae.inf.ic.om.evaluation.internalvalidation.interfaces.ICombined;

import cujae.inf.ic.om.evaluation.internalvalidation.metrics.AbstractMetric;

import cujae.inf.ic.om.exceptions.MetricException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;

import cujae.inf.ic.om.problem.output.Cluster;

import java.util.ArrayList;

/**
 * Implementación del Índice de Dunn para evaluar la calidad de los agrupamientos.
 * 
 * El Índice de Dunn mide la relación entre la mínima distancia inter-clúster 
 * y el diámetro máximo intra-clúster. Valores más altos indican mejores agrupamientos, 
 * donde los clústeres son compactos y bien separados.
 */
public class DunnIndex extends AbstractMetric implements ICombined {

	/*Constructor de la clase DunnIndex.*/
	public DunnIndex() {
		super();
	}

	/**
     * Evalúa el Índice de Dunn globalmente para una lista de clústeres.
     *
     * @param clusters La lista de clústeres a evaluar.
     * @return El valor del Índice de Dunn global.
	 * @throws MetricException Si la lista es inválida, contiene clústeres vacíos o si el diámetro máximo es cero.
	 * @throws ProblemException 
     */
	@Override
	public double evaluate_global(ArrayList<Cluster> clusters) throws MetricException, ProblemException {
		if (clusters == null || clusters.isEmpty()) 
			throw new MetricException("La lista de clústeres no puede ser nula ni vacía.");
        
		if (clusters.size() < 2) 
			throw new MetricException("Se requieren al menos dos clústeres para calcular el Índice de Dunn.");
	    
		for (Cluster cluster : clusters) 
	        if (cluster.get_items_of_cluster().size() < 2) 
	        	 throw new MetricException("Cada clúster debe contener al menos dos clientes para calcular el Índice de Dunn.");
		
		double min_inter_cluster_distance = Double.MAX_VALUE;
        double max_intra_cluster_diameter = Double.MIN_VALUE;
		
        for (int i = 0; i < clusters.size(); i++) 
        {
            Cluster cluster_one = clusters.get(i);
            for (int j = i + 1; j < clusters.size(); j++) 
            {
                Cluster cluster_two = clusters.get(j);
                double inter_cluster_distance = calculate_min_inter_cluster_distance(cluster_one, cluster_two);
                min_inter_cluster_distance = Math.min(min_inter_cluster_distance, inter_cluster_distance);
            }
        }
        for (Cluster cluster : clusters) {
            double intra_cluster_diameter = calculate_max_intra_cluster_diameter(cluster);
            max_intra_cluster_diameter = Math.max(max_intra_cluster_diameter, intra_cluster_diameter);
        }
        if (max_intra_cluster_diameter == 0) 
        	throw new MetricException("El diámetro máximo intra-clúster es cero. No se puede calcular el Índice de Dunn.");
        
		return min_inter_cluster_distance / max_intra_cluster_diameter;
	}
	
	/* Calcula la distancia mínima entre dos clústeres.*/
	private double calculate_min_inter_cluster_distance(Cluster cluster1, Cluster cluster2) throws MetricException, ProblemException {
        ArrayList<Customer> list_of_customers_one = get_customer_of_cluster(cluster1);
        ArrayList<Customer> list_of_customers_two = get_customer_of_cluster(cluster2);

        double min_distance = Double.MAX_VALUE;
        for (Customer customer_one : list_of_customers_one) 
        {
            for (Customer customer_two : list_of_customers_two) 
            {
                double distance = calculate_distance_squared(customer_one.get_location_customer(), customer_two.get_location_customer());
                min_distance = Math.min(min_distance, distance);
            }
        }
        return Math.sqrt(min_distance);
    }
	
	/* Calcula el diámetro máximo dentro de un clúster.*/
	private double calculate_max_intra_cluster_diameter(Cluster cluster) throws MetricException, ProblemException {
        ArrayList<Customer> customers = get_customer_of_cluster(cluster);
        double max_diameter = 0.0;

        for (int i = 0; i < customers.size(); i++) 
        {
            for (int j = i + 1; j < customers.size(); j++) 
            {
                Customer customer_one = customers.get(i);
                Customer customer_two = customers.get(j);
                double distance = calculate_distance_squared(customer_one.get_location_customer(), customer_two.get_location_customer());
                max_diameter = Math.max(max_diameter, distance);
            }
        }
        return Math.sqrt(max_diameter);
    }
}