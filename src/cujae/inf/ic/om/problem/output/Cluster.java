package cujae.inf.ic.om.problem.output;

import cujae.inf.ic.om.exceptions.ClusterException;

import java.util.ArrayList;

public class Cluster {

	private int id_cluster;
	private double request_cluster;
	private ArrayList<Integer> items_of_cluster;
	
	public Cluster() {
		super();
		id_cluster = -1;
		this.request_cluster = 0.0;
		this.items_of_cluster = new ArrayList<Integer>();	
	}

	public Cluster(int id_cluster, double request_cluster,
			ArrayList<Integer> items_of_cluster) throws ClusterException {
		super();
		if (request_cluster < 0)
			throw new ClusterException("La demanda del clúster no puede ser negativa.");
		if (items_of_cluster == null)
			throw new ClusterException("La lista de elementos del clúster no puede ser nula.");
		
		this.id_cluster = id_cluster;
		this.request_cluster = request_cluster;
		this.items_of_cluster = items_of_cluster;
	}

	public int get_id_cluster() {
		return id_cluster;
	}

	public void set_id_cluster(int id_cluster) {
		this.id_cluster = id_cluster;
	}

	public ArrayList<Integer> get_items_of_cluster() {
		return items_of_cluster;
	}

	public void set_items_of_cluster(ArrayList<Integer> items_of_cluster) throws ClusterException {
		if (items_of_cluster == null)
			throw new ClusterException("La lista de elementos del clúster no puede ser nula.");
		
		this.items_of_cluster = items_of_cluster;
	}

	public double get_request_cluster() {
		return request_cluster;
	}

	public void set_request_cluster(double request_cluster) throws ClusterException {
		if (request_cluster < 0)
			throw new ClusterException("La demanda del clúster no puede ser negativa.");
		
		this.request_cluster = request_cluster;
	}

	 public void clean_cluster() {
		 request_cluster = 0.0;	
		 items_of_cluster.clear();	 
    }
}