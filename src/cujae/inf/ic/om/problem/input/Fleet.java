package cujae.inf.ic.om.problem.input;

import cujae.inf.ic.om.exceptions.ProblemException;

public class Fleet {
	private int count_vehicles;
	private double capacity_vehicle;

	public Fleet() {
		super();
	}

	public Fleet(int count_vehicles, double capacity_vehicle) throws ProblemException {
		super();
		if (count_vehicles < 1)
			throw new ProblemException("La cantidad de vehículos debe ser mayor o igual a 1.");
		if (capacity_vehicle <= 0)
			throw new ProblemException("La capacidad de los vehículos debe ser mayor que 0.");
		
		this.count_vehicles = count_vehicles;
		this.capacity_vehicle = capacity_vehicle;
	}

	public int get_count_vehicles() {
		return count_vehicles;
	}

	public void set_count_vehicles(int count_vehicles) throws ProblemException {
		if (count_vehicles < 1)
			throw new ProblemException("La cantidad de vehículos debe ser mayor o igual a 1.");
		
		this.count_vehicles = count_vehicles;
	}

	public double get_capacity_vehicle() {
		return capacity_vehicle;
	}

	public void set_capacity_vehicle(double capacity_vehicle) throws ProblemException {
		if (capacity_vehicle <= 0)
			throw new ProblemException("La capacidad de los vehículos debe ser mayor que 0.");
		
		this.capacity_vehicle = capacity_vehicle;
	}
}