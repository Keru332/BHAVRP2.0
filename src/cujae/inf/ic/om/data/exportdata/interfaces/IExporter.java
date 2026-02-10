package cujae.inf.ic.om.data.exportdata.interfaces;

import cujae.inf.ic.om.data.exportdata.utils.MetricRecord;

import cujae.inf.ic.om.exceptions.ExportException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.output.Solution;

import java.util.List;

/**
 * Interfaz general para exportar resultados de asignación y métricas a distintos formatos.
 */
public interface IExporter {

    /**
     * Exporta la solución completa (clústeres, no asignados, etc.)
     * @throws ProblemException 
     */
    void export_solution(String heuristicName, Solution solution, String path) throws ExportException, ProblemException;

    /**
     * Exporta solo las métricas y tiempos para análisis.
     */
	void export_metrics(String instance_number, List<List<MetricRecord>> executions, double[] execution_times, String heuristicName, String path)
			throws ExportException;
}