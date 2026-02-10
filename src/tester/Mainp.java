package tester;

import cujae.inf.ic.om.assignment.AbstractAssignment;

import cujae.inf.ic.om.controller.Controller;

import cujae.inf.ic.om.controller.tools.AbstractTools;

import cujae.inf.ic.om.data.exportdata.formats.CSVExporter;

import cujae.inf.ic.om.data.exportdata.utils.MetricRecord;

import cujae.inf.ic.om.data.importdata.interfaces.IImporter;

import cujae.inf.ic.om.data.importdata.utils.ProblemRawData;

import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.CostMatrixException;
import cujae.inf.ic.om.exceptions.ExportException;
import cujae.inf.ic.om.exceptions.FactoryCreationException;
import cujae.inf.ic.om.exceptions.ImportException;
import cujae.inf.ic.om.exceptions.MetricException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.factory.DistanceType;

import cujae.inf.ic.om.factory.interfaces.EAssignmentType;
import cujae.inf.ic.om.factory.interfaces.EMetricType;
import cujae.inf.ic.om.factory.interfaces.IFactoryImporter;

import cujae.inf.ic.om.factory.methods.FactoryImporter;

import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Solution;

import cujae.inf.ic.om.matrix.NumericMatrix;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Mainp {
	
	public static void main(String[] args) 
			throws Exception {

		// --- Ruta general a la instancia ---
		// Acepta extensiones: .dat, .txt, .csv, .json, .xml
		// Si no se especifica extensión (ej: "p1"), se asume que es un archivo tipo .dat
		String instance_path = "instances//large_instance"; // error inducido
		//String instance_path = "instances//p1";
		
		// --- Número de ejecuciones para cada heurística ---
		int number_of_executions = 1;

		// --- Lista de métricas a utilizar en la evaluación ---
		List<EMetricType> selected_metrics = Arrays.asList(
				EMetricType.SSE,
				EMetricType.BallHallIndex,
				EMetricType.SSB,
				EMetricType.CalinskiHarabaszIndex,
				EMetricType.DunnIndex,
				EMetricType.SilhouetteCoefficient,
				EMetricType.DaviesBouldinIndex,
				EMetricType.XieBeniIndex,
				EMetricType.RayTuriIndex
				);

		// Configuración de la exportación
		String export_format = "txt"; // Opciones: csv, json, xls, txt, xml
		boolean export_solution = true; 		// true = exportar solución, false = exportar métricas
		boolean export_metrics_summary = false; // true = exportar resumen .csv para análisis en KEEL
		boolean export_visualization = false;   // true = exportar formato visualizable con VRPlotLIB
		
		boolean compare_large_vs_regular = false;
		if (compare_large_vs_regular) 
		{
		    run_large_vs_regular_comparison();
		    return;
		}
		
		Controller controller = Controller.get_controller();
		
		if (export_metrics_summary) 
			experiment_mode(selected_metrics);
		else {
			try {
				// --- Carga de la instancia del problema ---
				controller.load_problem(instance_path);
				System.out.println("CARGA EXITOSA DE LA INSTANCIA\n");

				// --- Configuración de exportación ---
				try {
					controller.configure_export_options(export_format, export_solution, export_metrics_summary, export_visualization, instance_path);
				} catch (Exception e) {
					System.err.println("Error al configurar exportación: " + e.getMessage());
					return;
				}

				// --- Selección individual de la heurística a ejecutar ---
				int j = 7;
				EAssignmentType heuristic;

				switch (j) {
				case 0: heuristic = EAssignmentType.ThreeCriteriaClustering; break;
				case 1: heuristic = EAssignmentType.BestCyclicAssignment; break;
				case 2: heuristic = EAssignmentType.Simplified; break;
				case 3: heuristic = EAssignmentType.NearestByCustomer; break;
				case 4: heuristic = EAssignmentType.Farthest_First; break;
				case 5: heuristic = EAssignmentType.UPGMC; break;
				case 6: heuristic = EAssignmentType.RandomByElement; break;
				case 7: heuristic = EAssignmentType.CLARA; break;
				default: throw new IllegalArgumentException("Índice de heurística no válido: " + j);
				}

				// --- Ejecución y evaluación ---
				List<List<MetricRecord>> executions = new ArrayList<>();
				double[] times = new double[number_of_executions];
				Solution last_solution = null;

				for (int i = 0; i < number_of_executions; i++) {
					try {
						long start = System.currentTimeMillis();
						//Problem.get_problem().get_customers().clear(); // error inducido
						last_solution = controller.execute_assignment(heuristic);
						long end = System.currentTimeMillis();
						times[i] = end - start;

						executions.add(controller.evaluate_solution(selected_metrics));
					} catch (Exception e) {
						System.err.println("Error en ejecución " + (i + 1) + ": " + e.getMessage());
						times[i] = -1;
					}
				}

				// --- Exportación de resultados ---
				try {
					controller.export_results(last_solution, executions, times, heuristic.name());
				} catch (Exception e) {
					System.err.println("Error al exportar resultados: " + e.getMessage());
				}
				
				// --- Visualización opcional de matriz de costos ---
				boolean show_cost_matrix = false;
				if (show_cost_matrix) 
				{
					try {
						NumericMatrix cost_matrix = AbstractAssignment.initialize_cost_matrix(
								Problem.get_problem().get_customers(),
								Problem.get_problem().get_depots(),
								DistanceType.Euclidean  // error inducido
						);
						System.out.println();
						System.out.println("-------------------------------------------------------------------------------");
						System.out.println("MATRIZ DE COSTOS CLIENTE-DEPÓSITO:");
						print_matrix(cost_matrix);
					} catch (Exception e) {
						System.err.println("Error al generar matriz de costos: " + e.getMessage());
					}
				}

				// --- Resumen final ---
				System.out.println("-------------------------------------------------------------------------------");
				System.out.println("RESUMEN DE LA EJECUCIÓN");
				System.out.println("-------------------------------------------------------------------------------");
				System.out.println("Heurística: " + heuristic.name());
				System.out.println("Instancia: " + new File(instance_path).getName());
				System.out.println("Ejecuciones: " + number_of_executions);

				double total = 0.0;
				for (double t : times) 
					if (t >= 0) 
						total += t;

				if (number_of_executions == 1) {
					System.out.printf("Tiempo: %.2f segundos%n", total / 1000.0);
				} else {
					System.out.printf("Tiempo total: %.2f ms%n", total);
					System.out.printf("Tiempo promedio: %.2f ms%n", total / number_of_executions);
				}

				System.out.println("Métricas evaluadas: " + selected_metrics.size());
				System.out.println("Finalización exitosa del flujo principal.");
				System.out.println("-------------------------------------------------------------------------------");

			} catch (Exception e) {
				System.err.println("Error crítico: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static void experiment_mode(List<EMetricType> metrics) 
			throws ImportException, ProblemException, AssignmentException, 
			MetricException, FactoryCreationException, ExportException, 
			CostMatrixException, ClusterException {

		String[] instance_ids = {"p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10"};
		String base_path = "instances//";

		EAssignmentType[] selected_heuristics = new EAssignmentType[] {
				EAssignmentType.BestCyclicAssignment,
				EAssignmentType.Farthest_First,
				EAssignmentType.NearestByCustomer,
				EAssignmentType.RandomByElement,
				EAssignmentType.Simplified,
				EAssignmentType.ThreeCriteriaClustering,
				EAssignmentType.UPGMC
		};

		Map<EMetricType, Map<String, Map<String, Double>>> results = new HashMap<>();
		Map<String, Map<String, Double>> time_results = new HashMap<>();
		List<String[]> failedHeuristics = new ArrayList<>();
		List<String> failedInstances = new ArrayList<>();

		for (EMetricType metric : metrics)
			results.put(metric, new LinkedHashMap<String, Map<String, Double>>());

		long start_execution_experiment = System.currentTimeMillis();

		for (String instance_id : instance_ids) 
		{
			String instance_path = base_path + instance_id;
			ProblemRawData data = null;
			try {
				IFactoryImporter importer_factory = new FactoryImporter();
				IImporter importer = importer_factory.create_importer(instance_path);
				data = importer.import_data(instance_path);
			} catch (Exception e) {
				System.err.println("Fallo al importar datos de la instancia " + instance_id + ": " + e.getMessage());
				failedInstances.add(instance_id); 

				for (EAssignmentType heuristic : selected_heuristics) 
				{
					failedHeuristics.add(new String[]{heuristic.name(), instance_id});
					for (EMetricType metric : metrics) 
					{
						if (!results.get(metric).containsKey(instance_id)) 
							results.get(metric).put(instance_id, new LinkedHashMap<String, Double>());
						results.get(metric).get(instance_id).put(heuristic.name(), -99.0);
					}
				}
				Map<String, Double> timeFallback = new LinkedHashMap<>();
				for (EAssignmentType h : selected_heuristics) {
					timeFallback.put(h.name(), -99.0);
				}
				time_results.put(instance_id, timeFallback);

				continue;
			}
			boolean loaded = false;
			try {
				loaded = Controller.get_controller().load_problem(instance_path);
			} catch (Exception e) {
				System.err.println("Error al cargar la instancia " + instance_id + ": " + e.getMessage());
			}
			if (!loaded) 
			{
				System.out.println("No se pudo cargar la instancia: " + instance_id);
				failedInstances.add(instance_id);

				for (EAssignmentType heuristic : selected_heuristics) 
				{
					failedHeuristics.add(new String[]{heuristic.name(), instance_id});
					for (EMetricType metric : metrics) 
					{
						if (!results.get(metric).containsKey(instance_id)) 
							results.get(metric).put(instance_id, new LinkedHashMap<String, Double>());
						results.get(metric).get(instance_id).put(heuristic.name(), -99.0);
					}
				}
				Map<String, Double> timeFallback = new LinkedHashMap<>();
				for (EAssignmentType h : selected_heuristics) 
					timeFallback.put(h.name(), -99.0);
				time_results.put(instance_id, timeFallback);
				continue;
			}
			Map<String, Double> instance_times = new LinkedHashMap<>();

			for (EAssignmentType heuristic : selected_heuristics) 
			{
				List<List<MetricRecord>> executions = new ArrayList<>();
				double[] execution_times = new double[20];
				boolean failed = false;

				for (int i = 0; i < 1; i++) 
				{
					try {
						long start = System.currentTimeMillis();
						Controller.get_controller().execute_assignment(heuristic);
						long end = System.currentTimeMillis();

						List<MetricRecord> run_metrics = Controller.get_controller().evaluate_solution(metrics);
						executions.add(run_metrics);
						execution_times[i] = end - start;

					} catch (Exception e) {
						System.err.println("Error al ejecutar " + heuristic.name() + " en " + instance_id + ": " + e.getMessage());
						failed = true;
						break;
					} finally {
						try {
							if (Problem.get_problem() != null && Problem.get_problem().get_customers() != null && !Problem.get_problem().get_customers().isEmpty()) 
								Controller.get_controller().clean_controller();
						} catch (Exception ex) {
							System.err.println("Error al limpiar el controlador: " + ex.getMessage());
						}
					}
				}
				if (failed) 
				{
					failedHeuristics.add(new String[]{heuristic.name(), instance_id});
					for (EMetricType metric : metrics) 
					{
						Map<String, Map<String, Double>> per_instance = results.get(metric);
						per_instance.putIfAbsent(instance_id, new LinkedHashMap<String, Double>());
						per_instance.get(instance_id).put(heuristic.name(), -99.0);
					}
					instance_times.put(heuristic.name(), -99.0);
					continue;
				}
				Map<EMetricType, Double> avg_metrics = new EnumMap<>(EMetricType.class);
				for (EMetricType metric : metrics)
					avg_metrics.put(metric, 0.0);

				for (List<MetricRecord> run : executions) 
				{
					for (MetricRecord mr : run) 
					{
						EMetricType type = mr.get_name_enum();
						if (type != null && avg_metrics.containsKey(type)) 
							avg_metrics.put(type, avg_metrics.get(type) + mr.get_value());
					}
				}
				for (EMetricType metric : metrics) 
				{
					Map<String, Map<String, Double>> per_instance = results.get(metric);
					per_instance.putIfAbsent(instance_id, new LinkedHashMap<String, Double>());
					per_instance.get(instance_id).put(heuristic.name(), avg_metrics.get(metric) / 20.0);
				}
				double avg_time = Arrays.stream(execution_times).sum() / 20.0;
				instance_times.put(heuristic.name(), avg_time);
			}
			time_results.put(instance_id, instance_times);
			Problem.get_problem().clean_info_problem();
		}
		CSVExporter exporter = new CSVExporter();

		for (Map.Entry<EMetricType, Map<String, Map<String, Double>>> entry : results.entrySet()) 
		{
			String path = "results/csv/keel/" + entry.getKey().name().toLowerCase() + ".csv";
			exporter.export_metrics_by_metric(entry.getKey(), entry.getValue(), path);
		}
		exporter.export_metrics_by_metric(null, time_results, "results/csv/keel/time.csv");

		long end_execution_experiment = System.currentTimeMillis();
		long total_ms = end_execution_experiment - start_execution_experiment;
		double total_minutes = total_ms / 60000.0;

		System.out.println();
		System.out.println("---------------------------------------------------------------------------------------------");
		System.out.println("Reporte de ejecución del experimento: ");
		System.out.println("Exportación finalizada: se generaron los archivos por métrica y tiempo.");
		System.out.println("Tiempo de ejecución:: " + AbstractTools.truncate_double(total_minutes, 2) + " minutos.");
		System.out.println();

		if (!failedInstances.isEmpty()) 
		{
			System.out.println("\nReporte de instancias que no se pudieron importar o cargar:");
			for (String id : failedInstances)
				System.out.println(" Instancia fallida: " + id);
		} else 
			System.out.println("Todas las instancias fueron cargadas correctamente.");

		if (!failedHeuristics.isEmpty()) 
		{
			System.out.println("\nReporte de heurísticas fallidas:");
			for (String[] pair : failedHeuristics)
				System.out.println(pair[0] + " falló en la instancia " + pair[1]);
		} else 
			System.out.println("No hubo heurísticas fallidas.");

		System.out.println("---------------------------------------------------------------------------------------------");
	}
	
	public static void run_large_vs_regular_comparison() throws Exception {
	    String[] instance_paths = {"instances//p1", "instances//large_instance"};
	    String[] instance_labels = {"Regular", "Grande"};

	    EAssignmentType[] all_heuristics = EAssignmentType.values();
	    Map<String, Map<String, Double>> time_results = new LinkedHashMap<>();

	    for (int i = 0; i < instance_paths.length; i++) {
	        String path = instance_paths[i];
	        String label = instance_labels[i];
	        Map<String, Double> current_results = new LinkedHashMap<>();

	        Controller controller = Controller.get_controller();
	        controller.load_problem(path);

	        for (EAssignmentType heuristic : all_heuristics) {
	            long start = System.currentTimeMillis();
	            try {
	                controller.execute_assignment(heuristic);
	            } catch (Exception e) {
	                System.err.println("\u274C Error con " + heuristic.name() + " en " + label + ": " + e.getMessage());
	                current_results.put(heuristic.name(), -1.0);
	                continue;
	            }
	            long end = System.currentTimeMillis();
	            double time = (end - start) / 1000.0;
	            current_results.put(heuristic.name(), time);
	            System.out.printf("\u2705 %s en %s: %.2f s\n", heuristic.name(), label, time);
	        }

	        time_results.put(label, current_results);
	        Problem.get_problem().clean_info_problem();
	    }

	    System.out.println("\n================ COMPARACI\u00d3N FINAL =================");
	    System.out.printf("%-25s %-10s %-10s\n", "HEUR\u00cdSTICA", "Regular", "Grande");
	    for (EAssignmentType heuristic : all_heuristics) {
	        double t1 = time_results.get("Regular").getOrDefault(heuristic.name(), -1.0);
	        double t2 = time_results.get("Grande").getOrDefault(heuristic.name(), -1.0);
	        System.out.printf("%-25s %-10.2f %-10.2f\n", heuristic.name(), t1, t2);
	    }

	    // Wilcoxon o análisis simple de incremento
	    System.out.println("\n================ RESUMEN ESTAD\u00cdSTICO =================");
	    int count = 0;
	    double sum_increase_pct = 0.0;

	    for (EAssignmentType heuristic : all_heuristics) {
	        double t1 = time_results.get("Regular").getOrDefault(heuristic.name(), -1.0);
	        double t2 = time_results.get("Grande").getOrDefault(heuristic.name(), -1.0);
	        if (t1 > 0 && t2 > 0) {
	            double pct = ((t2 - t1) / t1) * 100.0;
	            sum_increase_pct += pct;
	            count++;
	        }
	    }

	    double avg_increase = sum_increase_pct / count;
	    System.out.printf("\u2b06\ufe0f El tiempo promedio de ejecuci\u00f3n aument\u00f3 en un %.2f%% entre p1 y la instancia grande.\n", avg_increase);
	    System.out.println("==========================================================\n");
	}

	/**
	 * Imprime por consola una matriz numérica de distancias o costos.
	 * Las primeras filas son clientes (C1, C2, ...), las siguientes son depósitos (D1, D2, ...).
	 *
	 * @param matrix Matriz a imprimir.
	 */
	public static void print_matrix(NumericMatrix matrix) {
		for (int i = 0; i < matrix.getRowLength(); i++) {
			if (i < 10) {
				System.out.print("C" + (i + 1) + " ");
			} else {
				System.out.print("D" + (i - 9) + " ");
			}
			for (int j = 0; j < matrix.getColLength(); j++) {
				System.out.printf("%.2f ", matrix.getItem(i, j));
			}
			System.out.println();
		}
	}
}