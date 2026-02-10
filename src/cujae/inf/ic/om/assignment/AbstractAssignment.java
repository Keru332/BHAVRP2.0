package cujae.inf.ic.om.assignment;

import cujae.inf.ic.om.exceptions.CostMatrixException;

import cujae.inf.ic.om.factory.DistanceType;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;

import cujae.inf.ic.om.matrix.NumericMatrix;

import java.util.ArrayList;

public abstract class AbstractAssignment extends AbstractAssignmentTemplate {
	
	public static DistanceType distance_type = DistanceType.Euclidean;
	
    /**
     * Busca la posición de un clúster dentro de una lista de clústeres.
     *
     * @param id_cluster ID del clúster.
     * @param clusters Lista de clústeres.
     * @return Índice del clúster en la lista, o -1 si no se encuentra.
     */
	protected int find_cluster(int id_cluster, ArrayList<Cluster> clusters){
		int pos_cluster = -1;

		int i = 0;
		boolean found = false;

		while((i < clusters.size()) && (!found))
		{
			if(clusters.get(i).get_id_cluster() == id_cluster)
			{
				found = true;
				pos_cluster = i;
			}
			else 
				i++;
		}
		return pos_cluster;
	}
	
    /**
     * Inicializa la matriz de costos según el tipo de distancia.
     *
     * @param list_customers Lista de clientes.
     * @param list_depots Lista de depósitos.
     * @param distance_type Tipo de distancia a utilizar.
     * @return Matriz de costos.
     * @throws CostMatrixException si ocurre un error al calcular las distancias.
     */
	public static NumericMatrix initialize_cost_matrix(ArrayList<Customer> list_customers, 
			ArrayList<Depot> list_depots, DistanceType distance_type) 
					throws CostMatrixException {
	    try {
	        if (distance_type == DistanceType.Real) 
	        {
	            return Problem.get_problem().fill_cost_matrix_real(list_customers, list_depots);
	        } else {
	            return Problem.get_problem().fill_cost_matrix(list_customers, list_depots, distance_type);
	        }
	    } catch (Exception e) {
	        throw new CostMatrixException("Error al inicializar la matriz de costos con tipo de distancia: " + distance_type, e);
	    }
	}	
}