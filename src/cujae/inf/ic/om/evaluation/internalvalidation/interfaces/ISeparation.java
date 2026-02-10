package cujae.inf.ic.om.evaluation.internalvalidation.interfaces;

import cujae.inf.ic.om.exceptions.MetricException;
import cujae.inf.ic.om.exceptions.ProblemException;
import cujae.inf.ic.om.problem.output.Cluster;

/**
 * Interfaz que define los métodos básicos para evaluar métricas que miden 
 * separación en el agrupamiento.
 */
public interface ISeparation extends IMetric {
	
	/**
     * Evalúa una métrica globalmente para dos clústeres.
     *
     * @param first_cluster Primer clúster a evaluar.
     * @param second_cluster Segundo clúster a evaluar.
     * @return El valor global de la métrica para todos los clústeres.
	 * @throws MetricException 
	 * @throws ProblemException 
     */
	double evaluate_separation(Cluster first_cluster, Cluster second_cluster) throws MetricException, ProblemException;
}