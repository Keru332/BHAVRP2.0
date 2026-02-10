package cujae.inf.ic.om.data.exportdata.formats;

import cujae.inf.ic.om.controller.Controller;

import cujae.inf.ic.om.controller.tools.AbstractTools;

import cujae.inf.ic.om.data.exportdata.interfaces.IExporter;

import cujae.inf.ic.om.data.exportdata.utils.MetricRecord;

import cujae.inf.ic.om.exceptions.ExportException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Fleet;
import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;

import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Clase encargada de exportar soluciones y métricas en formato JSON.
 * Implementa la interfaz IExporter.
 */
public class JSONExporter implements IExporter {

    /**
     * Exporta una solución generada por una heurística al formato JSON.
     *
     * @param heuristic_name Nombre de la heurística utilizada.
     * @param solution Objeto Solution con la asignación cliente-depósito.
     * @param path Ruta del archivo JSON de salida.
     * @throws ExportException Si ocurre un error durante la escritura del archivo.
     * @throws ProblemException Si ocurre un error al recuperar datos del problema.
     */
	@Override
	public void export_solution(String heuristic_name, Solution solution,
			String path) throws ExportException, ProblemException {
        if (heuristic_name == null || heuristic_name.trim().isEmpty())
            throw new ExportException("El nombre de la heurística no puede ser nulo o vacío.");
        if (solution == null)
            throw new ExportException("La solución a exportar no puede ser nula.");
        if (path == null || path.trim().isEmpty())
            throw new ExportException("La ruta del archivo de salida no puede ser nula o vacía.");

	    try (FileWriter writer = new FileWriter(path)) {

	        ArrayList<Integer> depots_without_customers = Controller.get_controller().get_depots_without_customers();

	        Map<String, Object> export_json = new LinkedHashMap<>();
	        export_json.put("heuristic", heuristic_name);
	        export_json.put("solution", solution);
	        export_json.put("unassigned_depots", depots_without_customers);

	        Gson gson = new GsonBuilder().setPrettyPrinting().create();
	        gson.toJson(export_json, writer);

        } catch (IOException e) {
            throw new ExportException("Error exportando solución a JSON: " + path, e);
        }
	}

    /**
     * Exporta las métricas de evaluación a un archivo JSON, incluyendo resultados por ejecución y estadísticas agregadas.
     *
     * @param instance_number Identificador de la instancia evaluada.
     * @param executions Lista de ejecuciones con sus métricas individuales.
     * @param execution_times Arreglo con los tiempos de ejecución correspondientes a cada corrida.
     * @param heuristic_name Nombre de la heurística utilizada.
     * @param path Ruta del archivo JSON de salida.
     * @throws ExportException Si ocurre un error durante la exportación o validación de los datos.
     */
	@Override
	public void export_metrics(String instance_number,
			List<List<MetricRecord>> executions, double[] execution_times,
			String heuristic_name, String path) throws ExportException {
        if (instance_number == null || instance_number.trim().isEmpty())
            throw new ExportException("El identificador de la instancia no puede ser nulo o vacío.");
        if (executions == null || executions.isEmpty())
            throw new ExportException("La lista de ejecuciones no puede ser nula ni vacía.");
        if (execution_times == null || execution_times.length != executions.size())
            throw new ExportException("El arreglo de tiempos de ejecución es nulo o no coincide con el número de ejecuciones.");
        if (heuristic_name == null || heuristic_name.trim().isEmpty())
            throw new ExportException("El nombre de la heurística no puede ser nulo o vacío.");
        if (path == null || path.trim().isEmpty())
            throw new ExportException("La ruta del archivo de salida no puede ser nula o vacía.");

		try (FileWriter writer = new FileWriter(path)) {
			int num_executions = executions.size();
			int num_metrics = executions.get(0).size();

			int num_customers = Problem.get_problem().get_total_customers();
			int num_depots = Problem.get_problem().get_total_depots();
			double total_demand = Problem.get_problem().get_total_request();

			Map<String, Object> jsonRoot = new LinkedHashMap<>();
			jsonRoot.put("heuristic", heuristic_name);
			jsonRoot.put("instance", instance_number);
			jsonRoot.put("customers", num_customers);
			jsonRoot.put("depots", num_depots);
			jsonRoot.put("total_demand", total_demand);
			jsonRoot.put("runs", num_executions);

			List<Map<String, Object>> jsonExecutions = new ArrayList<>();

			double[] min = new double[num_metrics + 1];
			double[] max = new double[num_metrics + 1];
			double[] sum = new double[num_metrics + 1];
			Arrays.fill(min, Double.MAX_VALUE);
			Arrays.fill(max, Double.MIN_VALUE);

			for (int i = 0; i < num_executions; i++) 
			{
				Map<String, Object> runData = new LinkedHashMap<>();
				runData.put("run", i + 1);

				List<MetricRecord> metricList = executions.get(i);
				for (int j = 0; j < num_metrics; j++) 
				{
					double value = AbstractTools.truncate_double(metricList.get(j).get_value(), 2);
					String name = metricList.get(j).get_name();
					runData.put(name, value);
					sum[j] += value;
					min[j] = Math.min(min[j], value);
					max[j] = Math.max(max[j], value);
				}

				double time = AbstractTools.truncate_double(execution_times[i], 2);
				runData.put("Time", time);
				sum[num_metrics] += time;
				min[num_metrics] = Math.min(min[num_metrics], time);
				max[num_metrics] = Math.max(max[num_metrics], time);

				jsonExecutions.add(runData);
			}
			jsonRoot.put("results", jsonExecutions);

			Map<String, Object> statsMin = new LinkedHashMap<>();
			Map<String, Object> statsMax = new LinkedHashMap<>();
			Map<String, Object> statsAvg = new LinkedHashMap<>();

			List<MetricRecord> refMetrics = executions.get(0);
			for (int i = 0; i < num_metrics; i++) 
			{
				String name = refMetrics.get(i).get_name();
				statsMin.put(name, AbstractTools.truncate_double(min[i], 2));
				statsMax.put(name, AbstractTools.truncate_double(max[i], 2));
				statsAvg.put(name, AbstractTools.truncate_double(sum[i] / num_executions, 2));
			}
			statsMin.put("Time", AbstractTools.truncate_double(min[num_metrics], 2));
			statsMax.put("Time", AbstractTools.truncate_double(max[num_metrics], 2));
			statsAvg.put("Time", AbstractTools.truncate_double(sum[num_metrics] / num_executions, 2));

			Map<String, Object> statistics = new LinkedHashMap<>();
			statistics.put("Min", statsMin);
			statistics.put("Max", statsMax);
			statistics.put("Avg", statsAvg);

			jsonRoot.put("statistics", statistics);

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(jsonRoot, writer);

		} catch (IOException e) {
			throw new ExportException("Error exportando métricas a JSON: " + path, e);
		}
	}
	
	/**
	 * Exporta una solución en formato compatible con herramientas de visualización.
	 *
	 * @param solution Objeto Solution con la asignación cliente-depósito.
	 * @param heuristic_name Nombre de la heurística utilizada.
	 * @param path Ruta del archivo JSON de salida.
	 * @throws ExportException Si ocurre un error durante la exportación.
	 */
	public void export_visualization_format(Solution solution, String heuristic_name, String path) throws ExportException {
	    if (solution == null)
	        throw new ExportException("La solución a exportar no puede ser nula.");
	    if (heuristic_name == null || heuristic_name.trim().isEmpty())
	        throw new ExportException("El nombre de la heurística no puede ser nulo o vacío.");
	    if (path == null || path.trim().isEmpty())
	        throw new ExportException("La ruta del archivo de salida no puede ser nula o vacía.");

	    try (FileWriter writer = new FileWriter(path)) {
	        Map<String, Object> root = new LinkedHashMap<>();
	        List<Depot> all_depots = Problem.get_problem().get_depots();
	        List<Customer> all_customers = Problem.get_problem().get_customers();
	        
	        double max_capacity = 0;
	        int total_vehicles = 0;
	        for (Depot depot : all_depots) {
	            for (Fleet fleet : depot.get_fleet_depot()) {
	                max_capacity = Math.max(max_capacity, fleet.get_capacity_vehicle());
	                total_vehicles += fleet.get_count_vehicles();
	            }
	        }
	        int vehicles_per_depot = all_depots.isEmpty() ? 0 : total_vehicles / all_depots.size();

	        root.put("heuristic", heuristic_name);
	        root.put("max_vehicles_capacity", max_capacity);
	        root.put("vehicles_per_depot", vehicles_per_depot);
	        root.put("total_depots", all_depots.size());
	        root.put("total_customers", all_customers.size());
	        root.put("assigned_depots", solution.get_clusters().size());

	        List<Map<String, Object>> depotsJson = new ArrayList<>();
	        for (Depot depot : all_depots) {
	            Map<String, Object> depotMap = new LinkedHashMap<>();
	            depotMap.put("depot_id", String.valueOf(depot.get_id_depot()));
	            depotMap.put("coordinate_x", depot.get_location_depot().get_axis_x());
	            depotMap.put("coordinate_y", depot.get_location_depot().get_axis_y());
	            depotsJson.add(depotMap);
	        }

	        List<Map<String, Object>> stopsJson = new ArrayList<>();
	        for (Cluster cluster : solution.get_clusters()) {
	            Map<String, Object> stop = new LinkedHashMap<>();
	            int clusterDepotId = cluster.get_id_cluster();

	            Depot matchingDepot = null;
	            for (Depot depot : all_depots) {
	                if (depot.get_id_depot() == clusterDepotId) {
	                    matchingDepot = depot;
	                    break;
	                }
	            }

	            if (matchingDepot == null) {
	                throw new ExportException("No se encontró el depósito con ID: " + clusterDepotId + " para el clúster.");
	            }

	            stop.put("depot_id", String.valueOf(clusterDepotId));
	            stop.put("coordinate_x", matchingDepot.get_location_depot().get_axis_x());
	            stop.put("coordinate_y", matchingDepot.get_location_depot().get_axis_y());
	            stop.put("total_customers_assigned", cluster.get_items_of_cluster().size());

	            List<Map<String, Object>> passengerList = new ArrayList<>();
	            for (int custId : cluster.get_items_of_cluster()) {
	                Map<String, Object> p = new LinkedHashMap<>();
	                p.put("customer_id", "customer_id_" + custId);
	                passengerList.add(p);
	            }
	            stop.put("customers_list", passengerList);
	            stopsJson.add(stop);
	        }
	        root.put("depots_assigned", stopsJson);

	        List<Map<String, Object>> passengersJson = new ArrayList<>();
	        for (Customer customer : all_customers) {
	            Map<String, Object> cust = new LinkedHashMap<>();
	            cust.put("id", "customer_id_" + customer.get_id_customer());
	            cust.put("coordinate_x", customer.get_location_customer().get_axis_x());
	            cust.put("coordinate_y", customer.get_location_customer().get_axis_y());
	            passengersJson.add(cust);
	        }
	        root.put("customers_list", passengersJson);

	        Gson gson = new GsonBuilder().setPrettyPrinting().create();
	        gson.toJson(root, writer);
	    } catch (IOException e) {
	        throw new ExportException("Error al exportar solución para visualización en formato JSON.", e);
	    }
	}
}