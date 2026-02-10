package cujae.inf.ic.om.data.importdata.utils;

import cujae.inf.ic.om.controller.tools.AbstractTools;

import cujae.inf.ic.om.exceptions.ImportException;

import java.util.ArrayList;

public class DistanceUtils {
	
    /**
     * Calcula la distancia euclidiana entre dos puntos dados por sus coordenadas.
     *
     * @param axis_x_start Coordenada X del punto de inicio.
     * @param axis_y_start Coordenada Y del punto de inicio.
     * @param axis_x_end Coordenada X del punto de destino.
     * @param axis_y_end Coordenada Y del punto de destino.
     * @return Distancia euclidiana entre los dos puntos.
     */
    public Double calculate_distance(double axis_x_start, double axis_y_start, double axis_x_end, 
    		double axis_y_end) {
        double axis_x = Math.pow((axis_x_start - axis_x_end), 2);
        double axis_y = Math.pow((axis_y_start - axis_y_end), 2);
        return Math.sqrt(axis_x + axis_y);
    }
    
    /**
     * Llena la lista de distancias entre todos los clientes y depósitos, incluyendo distancias
     * cliente-cliente, cliente-depósito, depósito-cliente y depósito-depósito.
     *
     * @param id_customers IDs de los clientes.
     * @param axis_x_customers Coordenadas X de los clientes.
     * @param axis_y_customers Coordenadas Y de los clientes.
     * @param id_depots IDs de los depósitos.
     * @param axis_x_depots Coordenadas X de los depósitos.
     * @param axis_y_depots Coordenadas Y de los depósitos.
     * @param list_distances Lista destino donde se guardarán las distancias calculadas.
     * @throws ImportException si alguno de los parámetros es nulo o las listas tienen tamaños inconsistentes.
     */
    public void fill_list_distances(ArrayList<Integer> id_customers, ArrayList<Double> axis_x_customers, 
    		ArrayList<Double> axis_y_customers, ArrayList<Integer> id_depots, ArrayList<Double> axis_x_depots, 
    		ArrayList<Double> axis_y_depots, ArrayList<ArrayList<Double>> list_distances) 
    				throws ImportException {
    	if (id_customers == null)
    	    throw new ImportException("La lista de IDs de clientes es nula.");
    	if (axis_x_customers == null)
    	    throw new ImportException("La lista de coordenadas X de clientes es nula.");
    	if (axis_y_customers == null)
    	    throw new ImportException("La lista de coordenadas Y de clientes es nula.");
    	if (id_depots == null)
    	    throw new ImportException("La lista de IDs de depósitos es nula.");
    	if (axis_x_depots == null)
    	    throw new ImportException("La lista de coordenadas X de depósitos es nula.");
    	if (axis_y_depots == null)
    	    throw new ImportException("La lista de coordenadas Y de depósitos es nula.");    	
    	if (list_distances == null)
    	    throw new ImportException("La lista de distancias es nula.");
    	
    	int total_customers = id_customers.size();
    	int total_depots = id_depots.size();
    	
        if (axis_x_customers.size() != total_customers || axis_y_customers.size() != total_customers)
            throw new ImportException("Las dimensiones de los datos de clientes son inconsistentes.");
        if (axis_x_depots.size() != total_depots || axis_y_depots.size() != total_depots)
            throw new ImportException("Las dimensiones de los datos de depósitos son inconsistentes.");
    	
        try {
        	for(int i = 0; i < total_customers; i++)
        	{	    	
        		ArrayList<Double> distances_from_customers = new ArrayList<Double>();

        		for(int j = 0; j < total_customers; j++)
        			distances_from_customers.add(calculate_distance(AbstractTools.truncate_double(axis_x_customers.get(j), 6), AbstractTools.truncate_double(axis_y_customers.get(j), 6), AbstractTools.truncate_double(axis_x_customers.get(i), 6), AbstractTools.truncate_double(axis_y_customers.get(i), 6)));
        		//distancesFromCustomers.add(calculateDistance(axisXCustomers.get(j), axisYCustomers.get(j), axisXCustomers.get(i), axisYCustomers.get(i)));

        		for(int k = 0; k < total_depots; k++)
        			distances_from_customers.add(calculate_distance(AbstractTools.truncate_double(axis_x_depots.get(k), 6), AbstractTools.truncate_double(axis_y_depots.get(k), 6), AbstractTools.truncate_double(axis_x_customers.get(i), 6), AbstractTools.truncate_double(axis_y_customers.get(i), 6)));
        		//distancesFromCustomers.add(calculateDistance(axisXDepots.get(k), axisYDepots.get(k), axisXCustomers.get(i), axisYCustomers.get(i)));

        		list_distances.add(distances_from_customers);//hasta aqui voy a tener la lista de distancias llena de cada cliente y deposito a los clientes
        	}
        	for(int i = 0; i < total_depots; i++)
        	{
        		ArrayList<Double> distances_from_customers = new ArrayList<Double>();

        		for(int j = 0; j < total_customers; j++)
        			distances_from_customers.add(calculate_distance(axis_x_customers.get(j), axis_y_customers.get(j), axis_x_depots.get(i), axis_y_depots.get(i)));

        		for(int k = 0; k < total_depots; k++)
        			distances_from_customers.add(calculate_distance(axis_x_depots.get(k), axis_y_depots.get(k), axis_x_depots.get(i), axis_y_depots.get(i)));

        		list_distances.add(distances_from_customers);//ya aqui la voy a tener llena completa
        	}
        } catch (Exception e) {
        	throw new ImportException("Error al calcular o llenar la matriz de distancias.", e);
        }
    }
}