package cujae.inf.ic.om.problem.input;

import cujae.inf.ic.om.exceptions.ProblemException;

public class Location {
	private double axis_x;
	private double axis_y;
	
	public Location() {
		super();
	}

	public Location(double axis_x, double axis_y) {
		super();
		this.axis_x = axis_x;
		this.axis_y = axis_y;
	}

	public double get_axis_x() {
		return axis_x;
	}

	public void set_axis_x(double axis_x) throws ProblemException {
		if (Double.isNaN(axis_x) || Double.isInfinite(axis_x))
			throw new ProblemException("La coordenada X debe ser un valor numérico válido.");
		
		this.axis_x = axis_x;
	}

	public double get_axis_y() {
		return axis_y;
	}

	public void set_axis_y(double axis_y) throws ProblemException {
		if (Double.isNaN(axis_y) || Double.isInfinite(axis_y))
			throw new ProblemException("La coordenada Y debe ser un valor numérico válido.");
		
		this.axis_y = axis_y;
	}
}