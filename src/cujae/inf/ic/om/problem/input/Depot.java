package cujae.inf.ic.om.problem.input;

import cujae.inf.ic.om.exceptions.ProblemException;

import java.util.ArrayList;

public class Depot {
	private int id_depot;
	private Location location_depot;
	private ArrayList<Fleet> fleet_depot;

	public Depot() {
		super();
		fleet_depot = new ArrayList<Fleet>();
	}
	 
	public Depot(int id_depot) {
		super();
		this.id_depot = id_depot;
		this.location_depot = new Location(0.0, 0.0);
		fleet_depot = new ArrayList<Fleet>();
	}
	
	public Depot(int id_depot, Location location_depot,
			ArrayList<Fleet> fleet_depot) throws ProblemException {
		super();
		if (location_depot == null)
			throw new ProblemException("La ubicación del depósito no puede ser nula.");
		if (fleet_depot == null)
			throw new ProblemException("La lista de flotas no puede ser nula.");

		this.id_depot = id_depot;
		this.location_depot = location_depot;
		this.fleet_depot = fleet_depot;
	}

	public int get_id_depot() {
		return id_depot;
	}

	public void set_id_depot(int id_depot) {
		this.id_depot = id_depot;
	}

	public Location get_location_depot() {
		return location_depot;
	}

	public void set_location_depot(Location location_depot) throws ProblemException {
		if (location_depot == null)
			throw new ProblemException("La ubicación del depósito no puede ser nula.");
		
		this.location_depot = location_depot;
	}

	public ArrayList<Fleet> get_fleet_depot() {
		return fleet_depot;
	}

	public void set_fleet_depot(ArrayList<Fleet> fleet_depot) throws ProblemException {
		if (fleet_depot == null)
			throw new ProblemException("La lista de flotas del depósito no puede ser nula.");
		
		this.fleet_depot = fleet_depot;
	}
}