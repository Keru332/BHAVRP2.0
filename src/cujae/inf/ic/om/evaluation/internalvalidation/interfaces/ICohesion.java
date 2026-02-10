package cujae.inf.ic.om.evaluation.internalvalidation.interfaces;

import cujae.inf.ic.om.exceptions.MetricException;
import cujae.inf.ic.om.exceptions.ProblemException;
import cujae.inf.ic.om.problem.output.Cluster;

/**
 * Interfaz que define los métodos básicos para evaluar métricas que miden 
 * cohesión en el agrupamiento.
 */
public interface ICohesion extends IMetric {
	
	/**
     * Evalúa una métrica para un único clúster.
     *
     * @param cluster El clúster a evaluar.
     * @return El valor de la métrica para el clúster dado.
	 * @throws MetricException 
	 * @throws ProblemException 
     */
	double evaluate_cohesion(Cluster cluster) throws MetricException, ProblemException;
}