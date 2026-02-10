package cujae.inf.ic.om.evaluation.internalvalidation.metrics;

import cujae.inf.ic.om.distance.IDistance;

import cujae.inf.ic.om.exceptions.MetricException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.factory.DistanceType;
import cujae.inf.ic.om.factory.interfaces.IFactoryDistance;
import cujae.inf.ic.om.factory.methods.FactoryDistance;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Location;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;

import java.util.ArrayList;

/**
 * Clase abstracta base que implementa funcionalidades comunes para todas las métricas 
 * de evaluación.
 */
public abstract class AbstractMetric {
	
	/*Tipo de distancia utilizado para calcular métricas.*/
	public static DistanceType distance_type = DistanceType.Euclidean;
	
	/*Constructor de la clase abstracta Metric.*/
	public AbstractMetric() {
		super();
	}

	/**
     * Obtiene una lista de clientes asociados a un clúster.
     *
     * Este método recibe un clúster como entrada y devuelve una lista de objetos Customer correspondientes
     * a los identificadores de los clientes almacenados en el clúster. Utiliza el método getCustomerByIDCustomer
     * del problema para recuperar los detalles completos de cada cliente.
     *
     * @param cluster El clúster del cual se desea obtener los clientes.
     * @return Una lista de objetos Customer asociados al clúster.
	 * @throws ProblemException 
     *//*
	protected ArrayList<Customer> get_customer_of_cluster(Cluster cluster) {
		ArrayList<Customer> customer_of_cluster = new ArrayList<Customer>();
		
		for(int i = 0; i < cluster.get_items_of_cluster().size(); i++)
			customer_of_cluster.add(Problem.getProblem().get_customer_by_id_customer(cluster.get_items_of_cluster().get(i).intValue()));
	
		return customer_of_cluster;
	}
	*/
	protected ArrayList<Customer> get_customer_of_cluster(Cluster cluster) throws ProblemException {
	    ArrayList<Customer> customer_of_cluster = new ArrayList<>();

	    for (Integer id : cluster.get_items_of_cluster()) {
	        Customer customer = Problem.get_problem().get_customer_by_id_customer(id);
	        if (customer == null) {
	            System.err.println("ERROR: Cliente con ID " + id + " no encontrado en el problema.");
	        }
	        customer_of_cluster.add(customer);
	    }

	    return customer_of_cluster;
	}
	
	/**
     * Calcula el centroide de un conjunto de clientes.
     *
     * Este método calcula el centroide promedio de un grupo de clientes, representado como un objeto Location.
     * El centroide se calcula como el promedio de las coordenadas X e Y de todos los clientes en la lista.
     *
     * @param customers La lista de clientes para los cuales se desea calcular el centroide.
     * @return Un objeto Location que representa el centroide del grupo de clientes.
	 * @throws ProblemException 
     */
	protected Location calculate_centroid(ArrayList<Customer> customers) throws ProblemException { 
		double ave_axis_x = 0.0;
		double ave_axis_y = 0.0;
		Location location_centroid = new Location();
		
		int count_customers = customers.size(); 
		
		for(int i = 0; i < count_customers; i++) 
		{
			ave_axis_x += customers.get(i).get_location_customer().get_axis_x();
			ave_axis_y += customers.get(i).get_location_customer().get_axis_y();			
		}
		
		ave_axis_x = (ave_axis_x / count_customers);
		ave_axis_y = (ave_axis_y / count_customers);

		location_centroid.set_axis_x(ave_axis_x);
		location_centroid.set_axis_y(ave_axis_y);
		
		return location_centroid;
	}
	
	/**
     * Calcula la distancia al cuadrado entre dos ubicaciones utilizando el tipo de distancia especificado.
     *
     * @param location_one La primera ubicación.
     * @param location_two La segunda ubicación.
     * @return La distancia al cuadrado entre las dos ubicaciones.
	 * @throws MetricException Si hay error de cálculo o datos inválidos.
     */
	protected double calculate_distance_squared(Location location_one, Location location_two) throws MetricException {
        try {
            IDistance distance = new_distance(distance_type);
            double distance_value = distance.calculateDistance(
                location_one.get_axis_x(), location_one.get_axis_y(),
                location_two.get_axis_x(), location_two.get_axis_y()
            );
            return Math.pow(distance_value, 2);
        } catch (Exception e) {
            throw new MetricException("Error al calcular la distancia entre dos ubicaciones.", e);
        }
    }
    
	/* Crea una instancia de distancia según el tipo especificado.*/
	protected IDistance new_distance(DistanceType distance_type) throws MetricException {
	    try {
	        IFactoryDistance factoryDistance = new FactoryDistance();
	        return factoryDistance.createDistance(distance_type);
	    } catch (Exception e) {
	        throw new MetricException("Error al crear instancia de distancia para la métrica.", e);
	    }
	}
}