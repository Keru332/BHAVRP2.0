package cujae.inf.ic.om.problem.output;

import cujae.inf.ic.om.exceptions.ClusterException;

import java.util.ArrayList;

public class Solution { 
	private ArrayList<Cluster> clusters;
	private ArrayList<Integer> unassigned_items;
	
	public Solution() {
		super();
		clusters = new ArrayList<Cluster>();
		unassigned_items = new ArrayList<Integer>();
	}

	public Solution(ArrayList<Cluster> list_clusters) throws ClusterException {
		super();
		if (list_clusters == null)
			throw new ClusterException("La lista de clústeres no puede ser nula.");
		
		this.clusters = list_clusters;
		unassigned_items = new ArrayList<Integer>();
	}

	public ArrayList<Cluster> get_clusters() {
		return clusters;
	}

	public void set_clusters(ArrayList<Cluster> clusters) throws ClusterException {
		if (clusters == null)
			throw new ClusterException("La lista de clústeres no puede ser nula.");
		
		this.clusters = clusters;
	}

	public ArrayList<Integer> get_unassigned_items() {
		return unassigned_items;
	}

	public void set_unassigned_items(ArrayList<Integer> unassigned_items) throws ClusterException {
		if (unassigned_items == null)
			throw new ClusterException("La lista de elementos no asignados no puede ser nula.");
		
		this.unassigned_items = unassigned_items;
	}

	/* Método que devuelve true o false en dependencia de si existen clientes que no fueron asignados.*/
	public boolean exist_unassigned_items() {
		return unassigned_items.isEmpty()? true : false;				
	}
	
	/* Método encarargado de devolver cuantos clientes no asignados hay.*/
	public int get_total_unassigned_items() {
		return unassigned_items.size(); 		
	}	
	
	public int elements_clustering() {
		int total_element = 0;
		
		for(int i = 0; i < clusters.size(); i++)
			total_element += clusters.get(i).get_items_of_cluster().size();
		
		return total_element;
	}
}