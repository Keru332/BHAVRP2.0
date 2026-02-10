package cujae.inf.ic.om.evaluation.internalvalidation.interfaces;

import cujae.inf.ic.om.exceptions.MetricException;
import cujae.inf.ic.om.exceptions.ProblemException;
import cujae.inf.ic.om.problem.output.Cluster;

import java.util.ArrayList;

/**
 * Interfaz que define los métodos básicos para evaluar métricas que miden 
 * cohesión y separación combinadas en el agrupamiento.
 */
public interface ICombined extends IMetric {
	
	/**
     * Evalúa una métrica globalmente para una lista de clústeres.
     *
     * @param clusters La lista de clústeres a evaluar.
     * @return El valor global de la métrica para todos los clústeres.
	 * @throws MetricException 
	 * @throws ProblemException 
     */
	double evaluate_global(ArrayList<Cluster> clusters) throws MetricException, ProblemException;
}