package cujae.inf.ic.om.assignment.clustering.hierarchical;

import cujae.inf.ic.om.assignment.clustering.AbstractClustering;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;

import java.util.ArrayList;

public abstract class AbstractHierarchical extends AbstractClustering {

	/**
     * Verifica si al menos uno de los clústeres está asociado a un depósito registrado.
     *
     * @param clusters Lista de clústeres a evaluar.
     * @return true si existe al menos un clúster con identificador de depósito.
     * @throws ProblemException si la lista de clústeres es nula o vacía, o si los datos de los depósitos no están cargados.
     */
	protected boolean find_depot_of_cluster(ArrayList<Cluster> clusters) throws ProblemException {
		if (clusters == null || clusters.isEmpty()) 
			throw new ProblemException("La lista de clústeres está vacía o no ha sido inicializada.");

		int i = 0;
		boolean found = false;

		ArrayList<Depot> depots = Problem.get_problem().get_depots();
		if (depots == null || depots.isEmpty()) 
			throw new ProblemException("No hay depósitos cargados en el problema.");

		while((i < clusters.size()) && (!found))
		{
			int j = 0; 
			while((j < Problem.get_problem().get_depots().size()) && (!found))
			{
				if(clusters.get(i).get_id_cluster() == depots.get(j).get_id_depot())
					found = true;
				else 
					j++;
			}
			i++;
		}
		return found;
	}
	
    /**
     * Determina si el depósito se encuentra lleno respecto a la demanda restante
     * y la posición del cliente actual en evaluación jerárquica.
     *
     * @param clusters Lista ordenada de clústeres.
     * @param request_cluster Demanda cubierta en el clúster actual.
     * @param capacity_depot Capacidad total del depósito.
     * @param current_customer Índice del cliente actual.
     * @return true si el depósito está lleno; false si aún se puede asignar algún cliente.
     * @throws ProblemException si los parámetros son inconsistentes o la lista de clústeres no es válida.
     */
	protected boolean is_full_depot(ArrayList<Cluster> clusters, double request_cluster, double capacity_depot, int current_customer) throws ProblemException {
		if (clusters == null || clusters.isEmpty()) 
            throw new ProblemException("La lista de clústeres es nula o está vacía.");

        if (current_customer < 0 || current_customer >= clusters.size()) 
            throw new ProblemException("Índice de cliente actual fuera de rango: " + current_customer);
		
		boolean is_full = true;
		double current_request = capacity_depot - request_cluster;

		if(current_request > 0)
		{
			int i = 0;
			while(((i < clusters.size()) && (i < current_customer)) && (is_full))
			{
				if(clusters.get(i).get_request_cluster() <= current_request)
					is_full = false;
				else
					i++;
			}
		}	
		return is_full;
	}
}