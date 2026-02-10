package cujae.inf.ic.om.data.exportdata.formats;

import cujae.inf.ic.om.controller.Controller;

import cujae.inf.ic.om.controller.tools.AbstractTools;

import cujae.inf.ic.om.data.exportdata.interfaces.IExporter;

import cujae.inf.ic.om.data.exportdata.utils.MetricRecord;

import cujae.inf.ic.om.exceptions.ExportException;
import cujae.inf.ic.om.exceptions.ProblemException;
import cujae.inf.ic.om.factory.interfaces.EMetricType;

import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;

import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Clase encargada de exportar soluciones y métricas en formato CSV.
 * Implementa la interfaz IExporter.
 */
public class CSVExporter implements IExporter {

    /**
     * Exporta una solución al formato CSV, escribiendo la asignación de clientes a depósitos.
     *
     * @param heuristic_name Nombre de la heurística utilizada.
     * @param solution Objeto Solution que contiene los clústeres generados.
     * @param path Ruta del archivo de salida.
     * @throws ExportException Si ocurre un error durante la escritura del archivo.
     * @throws ProblemException Si ocurre un error al recuperar información del problema.
     */
	@Override
	public void export_solution(String heuristic_name, Solution solution, String path)
			throws ExportException, ProblemException {
        if (heuristic_name == null || heuristic_name.trim().isEmpty())
            throw new ExportException("El nombre de la heurística no puede ser nulo o vacío.");
        if (solution == null)
            throw new ExportException("La solución a exportar no puede ser nula.");
        if (path == null || path.trim().isEmpty())
            throw new ExportException("La ruta del archivo de salida no puede ser nula o vacía.");
		
		FileWriter writer = null;

		try {
			writer = new FileWriter(path);

			writer.write("Heuristic: " + heuristic_name + "\n");
			writer.write("Solution:\n");
			writer.write("Number of clusters: " + solution.get_clusters().size() + "\n");
			writer.write("\n");
			
			for (Cluster cluster : solution.get_clusters()) 
			{
				writer.write("Cluster ID: " + cluster.get_id_cluster() + "\n");
				writer.write("Cluster demand: " + cluster.get_request_cluster() + "\n");
				writer.write("Number of elements: " + cluster.get_items_of_cluster().size() + "\n");

				writer.write("Element IDs:\n");
				List<Integer> items = cluster.get_items_of_cluster();
				StringBuilder item_line = new StringBuilder();
				item_line.append("[");
				for (int i = 0; i < items.size(); i++) 
				{
					item_line.append(items.get(i));
					if (i < items.size() - 1) 
						item_line.append(", ");
				}
				item_line.append("]");
				writer.write(item_line.toString() + "\n");
				writer.write("\n");
			}
			writer.write("Unassigned customers: " + solution.get_total_unassigned_items() + "\n");
			if (solution.get_total_unassigned_items() > 0) 
			{
				writer.write("Unassigned element IDs:\n");
				for (Integer id : solution.get_unassigned_items()) 
					writer.write(id + "\n");
			}
			ArrayList<Integer> depots_without_customers = Controller.get_controller().get_depots_without_customers();
			writer.write("Depots without assigned customers: " + depots_without_customers.size() + "\n");

			if (!depots_without_customers.isEmpty()) 
			{
				writer.write("Unassigned depot IDs:\n");
				for (Integer id : depots_without_customers) 
					writer.write(id + "\n");
			}
		} catch (IOException e) {
			throw new ExportException("Error al exportar la solución: " + path, e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					throw new ExportException("Error al cerrar el archivo de solución: " + path, e);
				}
			}
		}
	}

    /**
     * Exporta los valores de las métricas de evaluación en múltiples ejecuciones a un archivo CSV.
     *
     * @param instanceNumber Identificador de la instancia evaluada.
     * @param executions Lista de ejecuciones, cada una con sus métricas.
     * @param execution_times Tiempos de ejecución asociados a cada corrida.
     * @param heuristicName Nombre de la heurística utilizada.
     * @param path Ruta del archivo CSV de salida.
     * @throws ExportException Si ocurre un error durante la exportación o validación de los datos.
     */
	@Override
	public void export_metrics(String instance_number, List<List<MetricRecord>> executions, 
			double[] execution_times, String heuristic_name, String path) 
					throws ExportException {
        if (instance_number == null || instance_number.trim().isEmpty())
            throw new ExportException("El número de instancia no puede ser nulo o vacío.");
        if (executions == null || executions.isEmpty())
            throw new ExportException("La lista de ejecuciones no puede ser nula o vacía.");
        if (execution_times == null || execution_times.length != executions.size())
            throw new ExportException("Los tiempos de ejecución no coinciden con las ejecuciones.");
        if (heuristic_name == null || instance_number.trim().isEmpty())
            throw new ExportException("El nombre de la heurística no puede ser nulo o vacío.");
        if (path == null || path.trim().isEmpty())
            throw new ExportException("La ruta del archivo no puede ser nula o vacía.");

	    FileWriter writer = null;

	    try {
	        writer = new FileWriter(path);

	        int num_executions = executions.size();
	        int num_metrics = executions.get(0).size();

	        int num_customers = Problem.get_problem().get_total_customers();
	        int num_depots = Problem.get_problem().get_total_depots();
	        double total_demand = Problem.get_problem().get_total_request();

	        // Encabezado de metadatos.
	        writer.write("Instance:," + instance_number + ",Customers:," + num_customers + ",Depots:," + num_depots + "\n");
	        writer.write("Executions:," + num_executions + ",Total Demand:," + total_demand + ",,,\n");
	        writer.write("\n\n");

	        // Encabezado de métricas dinámico.
	        writer.write("Run");
	        for (MetricRecord metric : executions.get(0)) 
	            writer.write("," + metric.get_name());
	        
	        writer.write(",Time\n");

	        // Inicializar acumuladores.
	        double[] min = new double[num_metrics + 1];
	        double[] max = new double[num_metrics + 1];
	        double[] sum = new double[num_metrics + 1];
	        Arrays.fill(min, Double.MAX_VALUE);
	        Arrays.fill(max, Double.MIN_VALUE);

	        // Filas de ejecuciones.
	        for (int i = 0; i < num_executions; i++) 
	        {
	            writer.write((i + 1) + ""); // RUN
	            List<MetricRecord> metrics = executions.get(i);

	            for (int j = 0; j < num_metrics; j++) 
	            {
	                double value = metrics.get(j).get_value();
	                writer.write("," + AbstractTools.truncate_double(value, 2));
	                sum[j] += AbstractTools.truncate_double(value, 2);
	                min[j] = Math.min(min[j], AbstractTools.truncate_double(value, 2));
	                max[j] = Math.max(max[j], AbstractTools.truncate_double(value, 2));
	            }
	            double time = execution_times[i];
	            writer.write("," + time + "\n");

	            sum[num_metrics] += time;
	            min[num_metrics] = Math.min(min[num_metrics], time);
	            max[num_metrics] = Math.max(max[num_metrics], time);
	        }
	        writer.write("\n");

	        // Estadísticas finales.
	        writer.write("Min");
	        for (int i = 0; i < num_metrics; i++) writer.write("," + min[i]);
	        writer.write("," + min[num_metrics] + "\n");

	        writer.write("Max");
	        for (int i = 0; i < num_metrics; i++) writer.write("," + max[i]);
	        writer.write("," + max[num_metrics] + "\n");

	        writer.write("Avg");
	        for (int i = 0; i < num_metrics; i++) writer.write("," + (sum[i] / num_executions));
	        writer.write("," + (sum[num_metrics] / num_executions) + "\n");

	    } catch (IOException e) {
	        throw new ExportException("Error al exportar métricas a CSV: " + path, e);
	    } finally {
	        if (writer != null) {
	            try {
	                writer.close();
	            } catch (IOException e) {
	                throw new ExportException("Error al cerrar el archivo CSV: " + path, e);
	            }
	        }
	    }
	}
	
    /**
     * Exporta un resumen de métricas por heurística en formato CSV.
     *
     * @param instanceNumber Identificador de la instancia.
     * @param summary Mapa que asocia nombres de heurísticas con sus métricas promedio.
     * @param path Ruta del archivo CSV de salida.
     * @throws ExportException Si ocurre un error durante el proceso de exportación.
     */
	public void export_metrics_summary(String instance_number, Map<String, List<MetricRecord>> summary, 
			String path) 
					throws ExportException {
        if (summary == null || summary.isEmpty())
            throw new ExportException("No se puede exportar un resumen vacío.");
        if (path == null || path.trim().isEmpty())
            throw new ExportException("La ruta del archivo no puede ser nula o vacía.");

	    FileWriter writer = null;
	    
	    try {
	        writer = new FileWriter(path);

	        Set<String> heuristics = summary.keySet();
	        Iterator<String> iterator = heuristics.iterator();
	        if (!iterator.hasNext()) 
	            throw new ExportException("No hay datos para exportar.");
	        
	        List<String> heuristic_names = new ArrayList<>(summary.keySet());
	        List<MetricRecord> first_metrics = summary.get(heuristic_names.get(0));

	        writer.write("Metric");
	        for (String heuristic : heuristic_names) 
	            writer.write("," + heuristic);
	        writer.write("\n");

	        int num_metrics = first_metrics.size();
	        for (int i = 0; i < num_metrics; i++) 
	        {
	            String metric_name = first_metrics.get(i).get_name();
	            writer.write(metric_name);

	            for (String heuristic : heuristic_names) 
	            {
	                List<MetricRecord> metrics = summary.get(heuristic);
	                double value = metrics.get(i).get_value();
	                writer.write("," + AbstractTools.truncate_double(value, 2));
	            }
	            writer.write("\n");
	        }
	    } catch (IOException e) {
	        throw new ExportException("Error al exportar resumen de métricas: " + path, e);
	    } finally {
	        if (writer != null) {
	            try {
	                writer.close();
	            } catch (IOException e) {
	                throw new ExportException("Error al cerrar el archivo CSV: " + path, e);
	            }
	        }
	    }
	}
	
	/**
	 * Exporta una tabla CSV con una métrica fija, mostrando los promedios para cada heurística e instancia.
	 *
	 * @param metric Tipo de métrica evaluada (puede ser null si se exportan tiempos).
	 * @param data Mapa de instancia -> heurística -> valor promedio.
	 * @param path Ruta del archivo de salida.
	 * @throws ExportException Si ocurre un error de escritura.
	 */
	public void export_metrics_by_metric(EMetricType metric, Map<String, Map<String, Double>> data, String path) 
			throws ExportException {
		if (data == null || data.isEmpty())
			throw new ExportException("Los datos a exportar no pueden ser nulos o vacíos.");
		if (path == null || path.trim().isEmpty())
			throw new ExportException("La ruta del archivo no puede ser nula o vacía.");
		
		FileWriter writer = null;

		try {
			writer = new FileWriter(path);

			Set<String> heuristics = new TreeSet<>();
			for (Map<String, Double> row : data.values()) 
			{
				heuristics.addAll(row.keySet());
				break;
			}
			writer.write("Instance");
			for (String heuristic : heuristics)
				writer.write("," + heuristic);
			
			writer.write("\n");

			for (Map.Entry<String, Map<String, Double>> entry : data.entrySet()) 
			{
				String instance = entry.getKey();
				Map<String, Double> heuristic_values = entry.getValue();

				writer.write(instance);
				for (String heuristic : heuristics) {
					Double value = heuristic_values.getOrDefault(heuristic, Double.NaN);
					writer.write("," + AbstractTools.truncate_double(value, 4));
				}
				writer.write("\n");
			}
		} catch (IOException e) {
			throw new ExportException("Error al exportar la métrica: " + path, e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					throw new ExportException("Error al cerrar el archivo: " + path, e);
				}
			}
		}
	}
}