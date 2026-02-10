package cujae.inf.ic.om.data.exportdata.utils;

import cujae.inf.ic.om.exceptions.ExportException;
import cujae.inf.ic.om.factory.interfaces.EMetricType;

/**
 * Clase que representa una métrica individual con su nombre abreviado y valor numérico.
 * Es utilizada durante la exportación de resultados para encapsular cada métrica calculada.
 */
public class MetricRecord {
	private String name;
	private double value;

	/**
	 * Constructor principal de MetricRecord.
	 *
	 * @param name  Nombre de la métrica.
	 * @param value Valor numérico asociado a la métrica.
	 * @throws ExportException Si el nombre es nulo o vacío, o si el valor es negativo.
	 */
	public MetricRecord(String name, double value) 
			throws ExportException {
		if (name == null || name.trim().isEmpty()) 
			throw new ExportException("El nombre de la métrica no puede ser nulo ni vacío.");
		
		this.name = name;
		this.value = value;
	}

	/**
	 * Retorna el nombre de la métrica.
	 *
	 * @return Nombre de la métrica.
	 */
	public String get_name() {
		return name;
	}


	/**
	 * Retorna el valor numérico de la métrica.
	 *
	 * @return Valor de la métrica.
	 */
	public double get_value() {
		return value;
	}
	
	public void set_name(String name) {
		this.name = name;
	}

	public EMetricType get_name_enum() {
	    try {
	        return EMetricType.valueOf(this.name);
	    } catch (IllegalArgumentException | NullPointerException e) {
	        return null;
	    }
	}
}