package cujae.inf.ic.om.assignment;

import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.CostMatrixException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.output.Solution;

public interface IAssignment {
	
	/**
	 * Ejecuta el algoritmo de asignación o agrupamiento de clientes a depósitos.
	 *
	 * Este método orquesta el proceso completo: inicialización, asignación y finalización,
	 * devolviendo una instancia de {@link Solution} con los clústeres generados y los elementos no asignados.
	 *
	 * @return Una solución que representa el resultado de la asignación de clientes a depósitos.
	 * @throws ClusterException si ocurre un error al crear o manipular clústeres.
	 * @throws CostMatrixException si ocurre un error al construir o utilizar la matriz de costos.
	 * @throws ProblemException si se encuentran inconsistencias en los datos del problema.
	 * @throws AssignmentException si ocurre un error general durante el proceso de asignación.
	 */
	Solution to_clustering() 
			throws ClusterException, CostMatrixException, ProblemException, AssignmentException;
}