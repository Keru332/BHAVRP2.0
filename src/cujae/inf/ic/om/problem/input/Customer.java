package cujae.inf.ic.om.problem.input;

import cujae.inf.ic.om.exceptions.ProblemException;

public class Customer {
	private int id_customer;
	private double request_customer;
	private Location location_customer;
	
	public Customer() {
		super();
	}
	
	public Customer(int id_customer, double request_customer,
			Location location_customer) throws ProblemException {
		super();
		if (request_customer < 0)
			throw new ProblemException("La demanda del cliente no puede ser negativa.");
		if (location_customer == null)
			throw new ProblemException("La ubicación del cliente no puede ser nula.");

		this.id_customer = id_customer;
		this.request_customer = request_customer;
		this.location_customer = location_customer;
	}

	public int get_id_customer() {
		return id_customer;
	}

	public void set_id_customer(int id_customer) {
		this.id_customer = id_customer;
	}

	public double get_request_customer() {
		return request_customer;
	}

	public void set_request_customer(double request_customer) throws ProblemException {
		if (request_customer < 0)
			throw new ProblemException("La demanda del cliente no puede ser negativa.");
		
		this.request_customer = request_customer;
	}

	public Location get_location_customer() {
		return location_customer;
	}

	public void set_location_customer(Location location_customer) throws ProblemException {
		if (location_customer == null)
			throw new ProblemException("La ubicación del cliente no puede ser nula.");
		
		this.location_customer = location_customer;
	}
}