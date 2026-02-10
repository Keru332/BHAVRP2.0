package cujae.inf.ic.om.assignment.classical;

import cujae.inf.ic.om.assignment.AbstractAssignment;
import cujae.inf.ic.om.exceptions.ClusterException;

import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;

import java.util.ArrayList;

public abstract class AbstractHeuristic extends AbstractAssignment {

	/**
	 * Inicializa los clusters de la solución con los identificadores de los depósitos.
	 *
	 * @return Lista de clústeres inicializados.
	 * @throws ClusterException si los depósitos no están cargados correctamente.
	 */
	protected ArrayList<Cluster> initialize_clusters() throws ClusterException {
		ArrayList<Cluster> list_clusters = new ArrayList<Cluster>();
		ArrayList<Depot> depots = Problem.get_problem().get_depots();
		ArrayList<Integer> list_id_depots = Problem.get_problem().get_list_id_depots();
		
		if (depots == null || depots.isEmpty()) 
			throw new ClusterException("No hay depósitos definidos en el problema.");
		
		if (list_id_depots == null || list_id_depots.size() != depots.size()) 
			throw new ClusterException("La lista de IDs de depósitos es inválida o no coincide con la cantidad de depósitos.");

		for(int i = 0; i < depots.size(); i++)
		{
			ArrayList<Integer> list_id_items = new ArrayList<Integer>();
			Cluster cluster = new Cluster(Problem.get_problem().get_list_id_depots().get(i).intValue(), 0.0, list_id_items); 
			list_clusters.add(cluster);
		}
		System.out.println("--------------------------------------------------");
		System.out.println("LISTA DE CLUSTERS");

		for(int i = 0; i < list_clusters.size(); i++)
		{
			System.out.println("ID CLUSTER: " + list_clusters.get(i).get_id_cluster());
			System.out.println("DEMANDA DEL CLUSTER: " + list_clusters.get(i).get_request_cluster());
			System.out.println("ELEMENTOS DEL CLUSTER: " + list_clusters.get(i).get_items_of_cluster());
		}
		System.out.println("--------------------------------------------------");

		return list_clusters;
	}
}