package cujae.inf.ic.om.controller;

import cujae.inf.ic.om.assignment.AbstractAssignment;

import cujae.inf.ic.om.controller.tools.EOrderType;
import cujae.inf.ic.om.controller.tools.AbstractTools;
import cujae.inf.ic.om.controller.tools.ExecutionConfig;
import cujae.inf.ic.om.data.exportdata.formats.CSVExporter;
import cujae.inf.ic.om.data.exportdata.formats.JSONExporter;
import cujae.inf.ic.om.data.exportdata.interfaces.IExporter;
import cujae.inf.ic.om.data.exportdata.utils.MetricRecord;
import cujae.inf.ic.om.data.importdata.interfaces.IImporter;
import cujae.inf.ic.om.data.importdata.utils.ProblemRawData;

import cujae.inf.ic.om.evaluation.internalvalidation.interfaces.ICohesion;
import cujae.inf.ic.om.evaluation.internalvalidation.interfaces.ICombined;
import cujae.inf.ic.om.evaluation.internalvalidation.interfaces.ISeparation;

import cujae.inf.ic.om.evaluation.internalvalidation.metrics.AbstractMetric;

import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.CostMatrixException;
import cujae.inf.ic.om.exceptions.ExportException;
import cujae.inf.ic.om.exceptions.FactoryCreationException;
import cujae.inf.ic.om.exceptions.ImportException;
import cujae.inf.ic.om.exceptions.MetricException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.factory.interfaces.EAssignmentType;
import cujae.inf.ic.om.factory.interfaces.EFileFormatExporter;
import cujae.inf.ic.om.factory.interfaces.IFactoryAssignment;
import cujae.inf.ic.om.factory.interfaces.EMetricType;
import cujae.inf.ic.om.factory.interfaces.IFactoryExporter;
import cujae.inf.ic.om.factory.interfaces.IFactoryImporter;

import cujae.inf.ic.om.factory.methods.FactoryAssignment;
import cujae.inf.ic.om.factory.methods.FactoryExporter;
import cujae.inf.ic.om.factory.methods.FactoryImporter;
import cujae.inf.ic.om.factory.methods.FactoryMetric;

import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {
	private static Controller controller = null;
	public static EOrderType order_type = EOrderType.Input;
	private Solution solution;
	private ExecutionConfig executionConfig;

	private Controller() {
		super();
	}
	
	/* Método que implementa el Patrón Singleton.*/
	public static Controller get_controller() {
		if (controller == null)
			controller = new Controller();
		
		return controller;
	}

	public Solution get_solution() {
		return solution;
	}

	public void set_solution(Solution solution) {
		this.solution = solution;
	}
	
	/**
	 * Elimina la solución actual almacenada en el controlador.
	 */
	public void clear_solution() {
		this.solution = null;
	}
	
	public void configure_export_options(String output_format, boolean export_solution, 
			boolean export_metrics_summary, boolean export_visualization, String instance_path)
			throws ImportException, ProblemException, CostMatrixException {
	    if (output_format == null || output_format.isEmpty())
	        throw new ProblemException("Debe especificarse un formato de salida para la exportación.");

		ExecutionConfig config = new ExecutionConfig();
		
	    config.output_format = EFileFormatExporter.valueOf(output_format.toUpperCase());
	    config.export_solution = export_solution;
	    config.export_metrics_summary = export_metrics_summary;
	    config.export_visualization = export_visualization;
	    
	    String file_name = new File(instance_path).getName();
	    Matcher matcher = Pattern.compile("\\d+").matcher(file_name);
	    if (matcher.find()) 
	        config.instance_number = matcher.group();
	    else {
	        System.err.println("Advertencia: No se pudo extraer un número de la instancia, se usará 'unknown'.");
	        config.instance_number = "unknown";
	    }
	    config.instance_path = instance_path;

	    this.executionConfig = config;
	}
	
	/**
	 * Carga los datos del problema, incluyendo clientes, depósitos y flotas, usando coordenadas.
	 *
	 * @param instance_path Ruta del archivo con la instancia.
	 * @return true si la carga fue exitosa.
	 * @throws ImportException Si falla la importación de datos.
	 * @throws ProblemException Si ocurre un error durante la carga de datos.
	 */
	public boolean load_problem(String instance_path) 
					throws ImportException, ProblemException {
	    IFactoryImporter factory = new FactoryImporter();
	    IImporter importer = factory.create_importer(instance_path);

	    ProblemRawData data = importer.import_data(instance_path);
		
		boolean loaded = false;

		System.out.println("ENTRADA A LA CARGA DE DATOS");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("CANTIDAD DE CLIENTES: " + data.id_customers.size());
		System.out.println("-------------------------------------------------------------------------------");

		for(int i = 0; i < data.id_customers.size(); i++)
		{
			System.out.println("ID CLIENTE: " + data.id_customers.get(i));
			System.out.println("DEMANDA : " + data.request_customers.get(i));
			System.out.println("X : " + data.axis_x_customers.get(i));
			System.out.println("Y : " + data.axis_y_customers.get(i));
		}
		System.out.println("CANTIDAD DE DEPÓSITOS: " + data.id_depots.size());
		System.out.println("-------------------------------------------------------------------------------");

		int total_vehicles = 0;
		double capacity_vehicle = 0.0;

		for(int i = 0; i < data.id_depots.size(); i++)
		{
			System.out.println("ID DEPÓSITO: " + data.id_depots.get(i));
			System.out.println("X : " + data.axis_x_depots.get(i));
			System.out.println("Y : " + data.axis_y_depots.get(i));

			System.out.println("CANTIDAD DE FLOTAS DEL DEPÓSITO: " + data.count_vehicles.get(i).size());

			for(int j = 0; j < data.count_vehicles.get(i).size(); j++)
			{
				total_vehicles = data.count_vehicles.get(i).get(j);
				capacity_vehicle = data.capacity_vehicles.get(i).get(j);

				System.out.println("CANTIDAD DE VEHÍCULOS: " + data.count_vehicles.get(i).get(j));
				System.out.println("CAPACIDAD DE LOS VEHÍCULOS: " + data.capacity_vehicles.get(i).get(j));
			}
			//System.out.println("CAPACIDAD TOTAL DEL DEPÓSITO: " + Problem.getProblem().getTotalCapacityByDepot(idDepots.get(i)));
			System.out.println("CAPACIDAD TOTAL DEL DEPÓSITO: " + (total_vehicles * capacity_vehicle));

			System.out.println("-------------------------------------------------------------------------------");
		}		
		if((data.id_customers != null && !data.id_customers.isEmpty()) && (data.request_customers != null && !data.request_customers.isEmpty()) && 
				(data.axis_x_customers != null && !data.axis_x_customers.isEmpty()) && (data.axis_y_customers != null && !data.axis_y_customers.isEmpty()) && 
				(data.id_depots != null && !data.id_depots.isEmpty()) && (data.axis_x_depots != null && !data.axis_x_depots.isEmpty()) && (data.axis_y_depots!= null && 
				!data.axis_y_depots.isEmpty()) && (data.count_vehicles != null && !data.count_vehicles.isEmpty()) && (data.capacity_vehicles != null && 
				!data.capacity_vehicles.isEmpty()))
		{
			Problem.get_problem().load_customer(data.id_customers, data.request_customers, data.axis_x_customers, data.axis_y_customers);
			Problem.get_problem().load_depot(data.id_depots, data.axis_x_depots, data.axis_y_depots, data.count_vehicles, data.capacity_vehicles);
			loaded = true;
		}

		System.out.println("RESUMEN DE LA CARGA DE DATOS:");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("CANTIDAD DE CLIENTES: " + data.id_customers.size());
		System.out.println("DEMANDA TOTAL DE LOS CLIENTES: " + Problem.get_problem().get_total_request());
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("CANTIDAD DE DEPÓSITOS: " + data.id_depots.size());
		System.out.println("CAPACIDAD TOTAL DE LOS DEPÓSITOS: " + Problem.get_problem().get_total_capacity());
		System.out.println("-------------------------------------------------------------------------------");
		
		for(int i = 0; i < data.id_depots.size(); i++)
		{
			System.out.println("ID DEPÓSITO: " + data.id_depots.get(i));			
			System.out.println("CANTIDAD DE FLOTAS DEL DEPÓSITO: " + data.count_vehicles.get(i).size());
			for(int j = 0; j < data.count_vehicles.get(i).size(); j++)
			{
				total_vehicles = data.count_vehicles.get(i).get(j);
				capacity_vehicle = data.capacity_vehicles.get(i).get(j);

				System.out.println("CANTIDAD DE VEHÍCULOS: " + data.count_vehicles.get(i).get(j));
				System.out.println("CAPACIDAD DE LOS VEHÍCULOS: " + data.capacity_vehicles.get(i).get(j));
			}
			System.out.println("CAPACIDAD TOTAL DEL DEPÓSITO: " + (total_vehicles * capacity_vehicle));
			System.out.println("-------------------------------------------------------------------------------");
		}
		System.out.println("CARGA EXITOSA: " + loaded);
		System.out.println("FIN DE LA CARGA DE DATOS");
		System.out.println("-------------------------------------------------------------------------------");

		return loaded;
	}
	
	/**
	 * Ejecuta la heurística de asignación especificada y genera una solución.
	 * 
	 * @param assignment_type Tipo de heurística de asignación a ejecutar.
	 * @return Una instancia de {@link Solution} que contiene los clústeres formados y los elementos no asignados.
	 * @throws ProblemException Si ocurre un error al acceder a los datos del problema (clientes o depósitos).
	 * @throws CostMatrixException Si ocurre un error al calcular o manipular la matriz de costos.
	 * @throws FactoryCreationException Si ocurre un error al instanciar la heurística seleccionada.
	 * @throws ClusterException Si ocurre un error al crear, manipular o acceder a los clústeres.
	 * @throws AssignmentException Si ocurre un error durante alguna etapa del proceso de asignación.
	 */
	public Solution execute_assignment(EAssignmentType assignment_type) 
			throws ProblemException, CostMatrixException, FactoryCreationException, ClusterException, AssignmentException {
		AbstractAssignment assignment = new_assignment(assignment_type);
		
		System.out.println("EJECUCIÓN DE LA HEURÍSTICA");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("HEURÍSTICA: " + assignment_type.name());
		System.out.println("-------------------------------------------------------------------------------");
			
		if(assignment_type.equals(EAssignmentType.NearestByCustomer) || assignment_type.equals(EAssignmentType.SequentialCyclic) || assignment_type.equals(EAssignmentType.CyclicAssignment) || assignment_type.equals(EAssignmentType.KMEANS) ||
		   assignment_type.equals(EAssignmentType.CoefficientPropagation) || assignment_type.equals(EAssignmentType.NearestByDepot))
		{ 
			switch(order_type.ordinal())
			{
				case 0:
				{
					AbstractTools.ascendent_ordenate();
					break;
				}
				case 1:
				{
					AbstractTools.descendent_ordenate();
					break;
				}
				case 3:
				{
					AbstractTools.random_ordenate();
					break;
				}
			}
		}
		if (Problem.get_problem() == null)
			throw new AssignmentException("La instancia del problema no está inicializada.");

		if (Problem.get_problem().get_customers() == null || Problem.get_problem().get_customers().isEmpty())
			throw new AssignmentException("La lista de clientes está vacía o no ha sido inicializada.");

		if (Problem.get_problem().get_depots() == null || Problem.get_problem().get_depots().isEmpty())
			throw new AssignmentException("La lista de depósitos está vacía o no ha sido inicializada.");

		solution = assignment.to_clustering();
		
		int[] cluster_items;
		
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("VERSION: ACTUALIZADA");
		System.out.println("HEURÍSTICA: " + assignment_type.name());
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("SOLUTION: ");
		System.out.println("CANTIDAD DE CLUSTERS: " + solution.get_clusters().size());
		System.out.println("-------------------------------------------------------------------------------");
		
		for(int i = 0; i < solution.get_clusters().size(); i++)
		{
			System.out.println("ID CLUSTER: " + solution.get_clusters().get(i).get_id_cluster());
			System.out.println("DEMANDA DEL CLUSTER: " + solution.get_clusters().get(i).get_request_cluster());
			System.out.println("TOTAL DE ELEMENTOS DEL CLUSTER : " + solution.get_clusters().get(i).get_items_of_cluster().size());
			System.out.println("-------------------------------------------------------------------------------");
			
			cluster_items = new int[solution.get_clusters().get(i).get_items_of_cluster().size()];
			
			for(int j = 0; j < solution.get_clusters().get(i).get_items_of_cluster().size(); j++)
			{
				//System.out.println("ID DEL ELEMENTO: " + solution.getClusters().get(i).getItemsOfCluster().get(j).intValue());
				cluster_items[j] =  solution.get_clusters().get(i).get_items_of_cluster().get(j).intValue();
			}	
			System.out.println("ID DE LOS ELEMENTOS: " + Arrays.toString(cluster_items));
			System.out.println("-------------------------------------------------------------------------------");
		}
		System.out.println("TOTAL DE CLIENTES NO ASIGNADOS: " + solution.get_total_unassigned_items());
		
		if(solution.get_total_unassigned_items() > 0)
		{
			System.out.println("CLIENTES NO ASIGNADOS: ");
			System.out.println("-------------------------------------------------------------------------------");
			for(int i = 0; i < solution.get_unassigned_items().size(); i++)
				System.out.println("ID DEL ELEMENTO NO ASIGNADO: " + solution.get_unassigned_items().get(i).intValue());
			System.out.println("-------------------------------------------------------------------------------");
		}
		ArrayList<Integer> id_depot_without_customers = new ArrayList<Integer>();
		id_depot_without_customers = get_depots_without_customers(); // here
		
		System.out.println("TOTAL DE DEPÓSITOS SIN ASIGNACIÓN DE CLIENTES: " + id_depot_without_customers.size());
		
		if(!id_depot_without_customers.isEmpty())
		{
			System.out.println("DEPÓSITOS SIN ASIGNACIÓN DE CLIENTES: ");
			System.out.println("-------------------------------------------------------------------------------");
			for(int i = 0; i < id_depot_without_customers.size(); i++)
				System.out.println("ID DEL DEPÓSITO: " + id_depot_without_customers.get(i).intValue());
		}
		//clean_controller();
		return solution;
	}
	
	/**
	 * Crea una instancia de la heurística de asignación especificada.
	 *
	 * @param type_assignment Tipo de heurística.
	 * @return Instancia de la heurística creada.
	 * @throws FactoryCreationException Si no se puede crear la instancia.
	 */
	private AbstractAssignment new_assignment(EAssignmentType type_assignment) throws FactoryCreationException {
		IFactoryAssignment i_factory_assignment = new FactoryAssignment();
		AbstractAssignment assignment = i_factory_assignment.create_assignment(type_assignment);
		return assignment;
	}
	
	/**
	 * Evalúa la solución actual utilizando las métricas especificadas y devuelve los resultados.
	 *
	 * @param metric_types Lista de tipos de métricas a evaluar.
	 * @return Lista de resultados de métricas evaluadas.
	 * @throws MetricException Si ocurre un error al calcular alguna métrica.
	 * @throws ProblemException Si la solución o sus clústeres están vacíos.
	 * @throws FactoryCreationException Si no se puede crear la métrica.
	 * @throws ExportException Si ocurre un error relacionado con la exportación durante la evaluación.
	 */
	public List<MetricRecord> evaluate_solution(List<EMetricType> metric_types) 
			throws MetricException, ProblemException, FactoryCreationException, ExportException {
		if (solution == null || solution.get_clusters().isEmpty()) 
			throw new ProblemException("No hay solución cargada para evaluar.");

		List<MetricRecord> results = new ArrayList<>();
		ArrayList<Cluster> clusters = solution.get_clusters();
		FactoryMetric factory_metric = new FactoryMetric();

		for (EMetricType metric_type : metric_types) 
		{
			AbstractMetric metric;

			try {
				metric = factory_metric.create_metric(metric_type);
			} catch (FactoryCreationException e) {
				System.err.println("Error creando la métrica " + metric_type + ": " + e.getMessage());
				continue;
			}

			try {
				if (metric instanceof ICohesion) 
				{
					ICohesion cohesion_metric = (ICohesion) metric;

					System.out.println("-------------------------------------------------------------------------------");
					System.out.println("MÉTRICA DE COHESIÓN: " + metric_type.name());

					double total = 0.0;
					for (int i = 0; i < clusters.size(); i++) 
					{
						double result = cohesion_metric.evaluate_cohesion(clusters.get(i));
						System.out.printf("Cluster %d: %.4f\n", i + 1, result);
						total += result;
					}
					double average = total / clusters.size();
					System.out.printf("Promedio Global: %.4f\n", average);
					results.add(new MetricRecord(metric_type.name(), average));
				}
				else if (metric instanceof ISeparation) 
				{
					ISeparation separation_metric = (ISeparation) metric;

					System.out.println("-------------------------------------------------------------------------------");
					System.out.println("MÉTRICA DE SEPARACIÓN: " + metric_type.name());

					double total = 0.0;
					int count = 0;

					for (int i = 0; i < clusters.size(); i++) 
					{
						for (int j = i + 1; j < clusters.size(); j++) {
							double result = separation_metric.evaluate_separation(clusters.get(i), clusters.get(j));
							System.out.printf("Separación entre Clúster %d y %d: %.4f\n", i + 1, j + 1, result);
							total += result;
							count++;
						}
					}
					if (count > 0) 
					{
						double promedio = total / count;
						System.out.printf("Promedio Global de Separación: %.4f\n", promedio);
						results.add(new MetricRecord(metric_type.name(), promedio));
					} else {
						System.out.println("No fue posible calcular la separación global (no hay suficientes pares).");
					}
				}
				else if (metric instanceof ICombined) 
				{
					ICombined separation_metric = (ICombined) metric;

					System.out.println("-------------------------------------------------------------------------------");
					System.out.println("MÉTRICA DE SEPARACIÓN/COMBINADA: " + metric_type.name());

					double result = separation_metric.evaluate_global(clusters);
					System.out.printf("Resultado Global: %.4f\n", result);
					results.add(new MetricRecord(metric_type.name(), result));
				}
			}catch (MetricException | ExportException e) {
				System.err.println("Error evaluando la métrica " + metric_type + ": " + e.getMessage());
			} catch (Exception e) {
				System.err.println("Error inesperado evaluando la métrica " + metric_type + ": " + e.getMessage());
			}
		}
		return results;
	}
	
	public void export_results(Solution solution, List<List<MetricRecord>> executions, 
		    double[] execution_times, String heuristic_name) 
		    throws ExportException, ProblemException {
		if (executionConfig == null)
	        throw new ExportException("La configuración de ejecución no ha sido inicializada.");

	    String folder = "results/" + executionConfig.output_format.name().toLowerCase() + "/";
	    String base_name = heuristic_name.toLowerCase();
	    String suffix = executionConfig.export_solution ? "_solution." : "_metrics.";
	    String export_path = folder + base_name + suffix + executionConfig.output_format.name().toLowerCase();

	    IFactoryExporter factory = new FactoryExporter();
	    IExporter exporter = factory.create_exporter(executionConfig.output_format);

	    if (executionConfig.export_solution) 
	        exporter.export_solution(heuristic_name, solution, export_path);
	    else 
	        exporter.export_metrics(executionConfig.instance_number, executions, execution_times, heuristic_name, export_path);
	    
	    System.out.println("-------------------------------------------------------------------------------");
	    System.out.println("Resultado exportado a: " + export_path);
	    
	    if (executionConfig.export_metrics_summary && executions.size() == 1) 
	    {
	        CSVExporter exportercsv = new CSVExporter();

	        Map<EMetricType, Map<String, Map<String, Double>>> results = new LinkedHashMap<>();
	        Map<String, Double> metric_values = new LinkedHashMap<>();

	        for (MetricRecord r : executions.get(0)) 
	        {
	            EMetricType metric_type = null;
	            try {
	                metric_type = EMetricType.valueOf(r.get_name_enum().name());
	            } catch (Exception e) {
	                continue; // métrica desconocida
	            }
	            results.putIfAbsent(metric_type, new LinkedHashMap<String, Map<String, Double>>());
	            results.get(metric_type).putIfAbsent(executionConfig.instance_number, new LinkedHashMap<String, Double>());
	            results.get(metric_type).get(executionConfig.instance_number).put(r.get_name(), r.get_value());
	        }
	        for (Map.Entry<EMetricType, Map<String, Map<String, Double>>> entry : results.entrySet()) 
	        {
	            String path = "results/csv/keel/" + entry.getKey().name().toLowerCase() + ".csv";
	            exportercsv.export_metrics_by_metric(entry.getKey(), entry.getValue(), path);
	        }
	        Map<String, Map<String, Double>> time_results = new LinkedHashMap<>();
	        Map<String, Double> time_map = new LinkedHashMap<>();
	        double total_time = 0;
	        for (int i = 0; i < execution_times.length; i++) 
	            if (execution_times[i] >= 0) total_time += execution_times[i];
	        
	        time_map.put("Time (ms)", total_time);
	        time_results.put(executionConfig.instance_number, time_map);

	        exportercsv.export_metrics_by_metric(null, time_results, "results/csv/keel/time.csv");

	        System.out.println("-------------------------------------------------------------------------------");
	        System.out.println("Resumen KEEL exportado a: results/csv/keel/");
	    }
	    
	    if (executionConfig.export_visualization && solution != null) 
	    {
	        JSONExporter jsonExporter = new JSONExporter();
	        String vis_path = "results/json/visualization_" + base_name + ".json";
	        jsonExporter.export_visualization_format(solution, heuristic_name, vis_path);
	        
		    System.out.println("-------------------------------------------------------------------------------");
	        System.out.println("Visualización exportada a: " + vis_path);
	    }
	}
	
	/*Método encargado de devolver los depósitos a los que no se les asigno ningún cliente en la solución.*/
	public ArrayList<Integer> get_depots_without_customers() throws ProblemException{
		ArrayList<Integer> id_depots = new ArrayList<Integer>();
		
		int total_depots = Problem.get_problem().get_depots().size();
		int total_clusters = solution.get_clusters().size();
		
		if (total_depots > 0 && total_clusters == 0) 
		    throw new ProblemException("No se han generado clústeres para los depósitos cargados.");
		
		if(total_clusters < total_depots)
		{
			for(int i = 0; i < total_depots; i++)
			{
				int j = 0;
				boolean found = false;
				
				int idDepot = Problem.get_problem().get_depots().get(i).get_id_depot();
				
				while((j < total_clusters) && (!found))
				{
					if(solution.get_clusters().get(j).get_id_cluster() == idDepot)
						found = true;
					else
						j++;
				}
				if(!found)
					id_depots.add(idDepot);
			}
		}	
		return id_depots;
	}
	
	/*Método encargado de restaurar los parámetros globales de la clase Controller.*/
	public void clean_controller() {
		solution.get_clusters().clear();
		solution.get_unassigned_items().clear();
		solution = null;
	}
	
	/* Método encargado de destruir la instancia de la controladora.*/
	public static void destroy_controller() {
		controller = null;
	}
}

/*
	// Método encargado de cargar los datos del problema usando listas de distancias y el tipo de distancia
	public boolean loadProblem(ArrayList<Integer> idCustomers, ArrayList<Double> requestCustomers, ArrayList<Integer> idDepots, 
			ArrayList<ArrayList<Integer>> countVehicles, ArrayList<ArrayList<Double>> capacityVehicles,  
			ArrayList<ArrayList<Double>> listDistances)throws IllegalArgumentException, SecurityException, 
			ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		boolean loaded = false;

		System.out.println("ENTRADA A LA CARGA DE DATOS");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("CANTIDAD DE CLIENTES: " + idCustomers.size());
		System.out.println("-------------------------------------------------------------------------------");
		for(int i = 0; i < idCustomers.size(); i++)
		{
			System.out.println("ID CLIENTE: " + idCustomers.get(i));
			System.out.println("DEMANDA : " + requestCustomers.get(i));
		}
		System.out.println("CANTIDAD DE DEPÓSITOS: " + idDepots.size());
		System.out.println("-------------------------------------------------------------------------------");
		for(int i = 0; i < idDepots.size(); i++)
		{
			System.out.println("ID DEPÓSITO: " + idDepots.get(i));
			//System.out.println("CAPACIDAD DEL DEPÓSITO: " + Problem.getProblem().getTotalCapacityByDepot(depot));
			System.out.println("CANTIDAD DE FLOTAS DEL DEPÓSITO: " + countVehicles.get(i).size());
			for(int j = 0; j < countVehicles.get(i).size(); j++)
			{
				System.out.println("CANTIDAD DE VEHÍCULOS: " + countVehicles.get(i).get(j));
				System.out.println("CAPACIDAD DE LOS VEHÍCULOS: " + capacityVehicles.get(i).get(j));
			}
			System.out.println("-------------------------------------------------------------------------------");
		}
		
		if((idCustomers != null && !idCustomers.isEmpty()) && (requestCustomers != null && !requestCustomers.isEmpty()) && 
				(idCustomers.size() == requestCustomers.size()) && (idDepots != null && !idDepots.isEmpty()) && 
				(countVehicles != null && !countVehicles.isEmpty()) && (capacityVehicles != null && !capacityVehicles.isEmpty()) && 
				(idDepots.size() == countVehicles.size()) && (idDepots.size() == capacityVehicles.size()) && (countVehicles.size() == capacityVehicles.size()) && 
				(listDistances != null && !listDistances.isEmpty()) && (listDistances.size() == (idCustomers.size() + idDepots.size())))
		
		{
			Problem.getProblem().loadCustomer(idCustomers, requestCustomers);
			Problem.getProblem().loadDepot(idDepots, countVehicles, capacityVehicles);

			if((Problem.getProblem().getTotalCapacity() >= Problem.getProblem().getTotalRequest()))
			{
				loaded = true;
				Problem.getProblem().fillCostMatrix(listDistances);
			}
		}
		
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("DEMANDA TOTAL DE LOS CLIENTES: " + Problem.getProblem().getTotalRequest());
		System.out.println("CAPACIDAD TOTAL DE LOS DEPÓSITOS: " + Problem.getProblem().getTotalCapacity());
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("CARGA EXITOSA: " + loaded);
		System.out.println("FIN DE LA CARGA DE DATOS");
		System.out.println("-------------------------------------------------------------------------------");
		
		return loaded;
	}

	// Método encargado de cargar los datos del problema usando matriz de costo.
	public boolean loadProblem(ArrayList<Integer> idCustomers, ArrayList<Double> requestCustomers, ArrayList<Integer> idDepots, 
			ArrayList<ArrayList<Integer>> countVehicles, ArrayList<ArrayList<Double>> capacityVehicles, 
			NumericMatrix costMatrix)throws IllegalArgumentException, SecurityException, ClassNotFoundException, 
			InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		boolean loaded = false;

		System.out.println("ENTRADA A LA CARGA DE DATOS");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("CANTIDAD DE CLIENTES: " + idCustomers.size());
		System.out.println("-------------------------------------------------------------------------------");
		for(int i = 0; i < idCustomers.size(); i++)
		{
			System.out.println("ID CLIENTE: " + idCustomers.get(i));
			System.out.println("DEMANDA : " + requestCustomers.get(i));
		}
		System.out.println("CANTIDAD DE DEPÓSITOS: " + idDepots.size());
		System.out.println("-------------------------------------------------------------------------------");
		for(int i = 0; i < idDepots.size(); i++)
		{
			System.out.println("ID DEPÓSITO: " + idDepots.get(i));
			System.out.println("CANTIDAD DE FLOTAS DEL DEPÓSITO: " + countVehicles.get(i).size());
			System.out.println("-------------------------------------------------------------------------------");
			for(int j = 0; j < countVehicles.get(i).size(); i++)
			{
				System.out.println("CANTIDAD DE VEHÍCULOS: " + countVehicles.get(i).get(j));
				System.out.println("CAPACIDAD DE LOS VEHÍCULOS: " + capacityVehicles.get(i).get(j));
			}
			System.out.println("-------------------------------------------------------------------------------");
		}
		
		if((idCustomers != null && !idCustomers.isEmpty()) && (requestCustomers != null && !requestCustomers.isEmpty()) && 
				(idCustomers.size() == requestCustomers.size()) && (idDepots != null && !idDepots.isEmpty()) && 
				(countVehicles != null && !countVehicles.isEmpty()) && (capacityVehicles != null && !capacityVehicles.isEmpty()) && 
				(idDepots.size() == countVehicles.size()) && (idDepots.size() == capacityVehicles.size()) && (countVehicles.size() == capacityVehicles.size()) && 
				(costMatrix.getColCount() == (idCustomers.size() + idDepots.size())) && (costMatrix.getRowCount() == (idCustomers.size() + idDepots.size())))
		{
			Problem.getProblem().loadCustomer(idCustomers, requestCustomers);
			Problem.getProblem().loadDepot(idDepots, countVehicles, capacityVehicles);

			if((Problem.getProblem().getTotalCapacity() >= Problem.getProblem().getTotalRequest()))
			{
				loaded = true;
				Problem.getProblem().setCostMatrix(costMatrix);
			}
		}

		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("DEMANDA TOTAL DE LOS CLIENTES: " + Problem.getProblem().getTotalRequest());
		System.out.println("CAPACIDAD TOTAL DE LOS DEPÓSITOS: " + Problem.getProblem().getTotalCapacity());
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("CARGA EXITOSA: " + loaded);
		System.out.println("FIN DE LA CARGA DE DATOS");
		System.out.println("-------------------------------------------------------------------------------");
		
		return loaded;
	}
	
	// Método encargado de cargar los datos del problema (incluido las coordenadas) usando listas de distancias
	public boolean loadProblem(ArrayList<Integer> idCustomers, ArrayList<Double> requestCustomers, ArrayList<Double> axisXCustomers, 
			ArrayList<Double> axisYCustomers, ArrayList<Integer> idDepots, ArrayList<Double> axisXDepots, ArrayList<Double> axisYDepots, 
			ArrayList<ArrayList<Integer>> countVehicles, ArrayList<ArrayList<Double>> capacityVehicles, 
			ArrayList<ArrayList<Double>> listDistances)throws IllegalArgumentException, SecurityException, 
			ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
	boolean loaded = false;

	System.out.println("ENTRADA A LA CARGA DE DATOS");
	System.out.println("-------------------------------------------------------------------------------");
	System.out.println("CANTIDAD DE CLIENTES: " + idCustomers.size());
	System.out.println("-------------------------------------------------------------------------------");
	for(int i = 0; i < idCustomers.size(); i++)
	{
		System.out.println("ID CLIENTE: " + idCustomers.get(i));
		System.out.println("DEMANDA : " + requestCustomers.get(i));
		System.out.println("X : " + axisXCustomers.get(i));
		System.out.println("Y : " + axisYCustomers.get(i));
	}
	System.out.println("CANTIDAD DE DEPÓSITOS: " + idDepots.size());
	System.out.println("-------------------------------------------------------------------------------");
	
	int totalVehicles = 0;
	double capacityVehicle = 0.0;
	
	for(int i = 0; i < idDepots.size(); i++)
	{
		System.out.println("ID DEPÓSITO: " + idDepots.get(i));
		System.out.println("X : " + axisXDepots.get(i));
		System.out.println("Y : " + axisYDepots.get(i));
		
		System.out.println("CANTIDAD DE FLOTAS DEL DEPÓSITO: " + countVehicles.get(i).size());
		for(int j = 0; j < countVehicles.get(i).size(); j++)
		{
			totalVehicles = countVehicles.get(i).get(j);
			capacityVehicle = capacityVehicles.get(i).get(j);
			
			System.out.println("CANTIDAD DE VEHÍCULOS: " + countVehicles.get(i).get(j));
			System.out.println("CAPACIDAD DE LOS VEHÍCULOS: " + capacityVehicles.get(i).get(j));
		}
		
	//	System.out.println("CAPACIDAD TOTAL DEL DEPÓSITO: " + Problem.getProblem().getTotalCapacityByDepot(idDepots.get(i)));
		System.out.println("CAPACIDAD TOTAL DEL DEPÓSITO: " + (totalVehicles * capacityVehicle));
		
		System.out.println("-------------------------------------------------------------------------------");
	}		
		
		if((idCustomers != null && !idCustomers.isEmpty()) && (requestCustomers != null && !requestCustomers.isEmpty()) && (axisXCustomers != null && !axisXCustomers.isEmpty()) && (axisYCustomers != null && !axisYCustomers.isEmpty()) && 
				(idDepots != null && !idDepots.isEmpty()) && (axisXDepots != null && !axisXDepots.isEmpty()) && (axisYDepots!= null && !axisYDepots.isEmpty()) && (countVehicles != null && !countVehicles.isEmpty()) && 
				(capacityVehicles != null && !capacityVehicles.isEmpty()) && (listDistances != null && !listDistances.isEmpty()))
		
		{
			Problem.getProblem().loadCustomer(idCustomers, requestCustomers, axisXCustomers, axisYCustomers);
			Problem.getProblem().loadDepot(idDepots, axisXDepots, axisYDepots, countVehicles, capacityVehicles);
								
			if((Problem.getProblem().getTotalCapacity() >= Problem.getProblem().getTotalRequest()))
			{
				loaded = true;
				Problem.getProblem().fillCostMatrix(listDistances);
			}
		}

		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("DEMANDA TOTAL DE LOS CLIENTES: " + Problem.getProblem().getTotalRequest());
		System.out.println("CAPACIDAD TOTAL DE LOS DEPÓSITOS: " + Problem.getProblem().getTotalCapacity());
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("CARGA EXITOSA: " + loaded);
		System.out.println("FIN DE LA CARGA DE DATOS");
		System.out.println("-------------------------------------------------------------------------------");
		
		return loaded;
	}
		
	// Método encargado de cargar los datos del problema (incluido las coordenadas) usando listas de distancias
	public boolean loadProblem(ArrayList<Integer> idCustomers, ArrayList<Double> requestCustomers, ArrayList<Double> axisXCustomers, ArrayList<Double> axisYCustomers, ArrayList<Integer> idDepots, ArrayList<Double> axisXDepots, ArrayList<Double> axisYDepots, ArrayList<ArrayList<Integer>> countVehicles, ArrayList<ArrayList<Double>> capacityVehicles,
			NumericMatrix costMatrix)throws IllegalArgumentException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
	boolean loaded = false;

	System.out.println("ENTRADA A LA CARGA DE DATOS");
	System.out.println("-------------------------------------------------------------------------------");
	System.out.println("CANTIDAD DE CLIENTES: " + idCustomers.size());
	System.out.println("-------------------------------------------------------------------------------");
	for(int i = 0; i < idCustomers.size(); i++)
	{
		System.out.println("ID CLIENTE: " + idCustomers.get(i));
		System.out.println("DEMANDA : " + requestCustomers.get(i));
		System.out.println("X : " + axisXCustomers.get(i));
		System.out.println("Y : " + axisYCustomers.get(i));
	}
	System.out.println("CANTIDAD DE DEPÓSITOS: " + idDepots.size());
	System.out.println("-------------------------------------------------------------------------------");
	for(int i = 0; i < idDepots.size(); i++)
	{
		System.out.println("ID DEPÓSITO: " + idDepots.get(i));
		System.out.println("X : " + axisXDepots.get(i));
		System.out.println("Y : " + axisYDepots.get(i));
		//System.out.println("CAPACIDAD DEL DEPÓSITO: " + Problem.getProblem().getTotalCapacityByDepot(depot));
		System.out.println("CANTIDAD DE FLOTAS DEL DEPÓSITO: " + countVehicles.get(i).size());
		for(int j = 0; j < countVehicles.get(i).size(); j++)
		{
			System.out.println("CANTIDAD DE VEHÍCULOS: " + countVehicles.get(i).get(j));
			System.out.println("CAPACIDAD DE LOS VEHÍCULOS: " + capacityVehicles.get(i).get(j));
		}
		System.out.println("-------------------------------------------------------------------------------");
	}		
		
		if((idCustomers != null && !idCustomers.isEmpty()) && (requestCustomers != null && !requestCustomers.isEmpty()) && (axisXCustomers != null && !axisXCustomers.isEmpty()) && (axisYCustomers != null && !axisYCustomers.isEmpty()) && 
				(idDepots != null && !idDepots.isEmpty()) && (axisXDepots != null && !axisXDepots.isEmpty()) && (axisYDepots!= null && !axisYDepots.isEmpty()) && (countVehicles != null && !countVehicles.isEmpty()) && 
				(capacityVehicles != null && !capacityVehicles.isEmpty()) && (costMatrix.getColCount() == (idCustomers.size() + idDepots.size())) && (costMatrix.getRowCount() == (idCustomers.size() + idDepots.size())))
		
		{
			Problem.getProblem().loadCustomer(idCustomers, requestCustomers, axisXCustomers, axisYCustomers);
			Problem.getProblem().loadDepot(idDepots, axisXDepots, axisYDepots, countVehicles, capacityVehicles);
								
			if((Problem.getProblem().getTotalCapacity() >= Problem.getProblem().getTotalRequest()))
			{
				loaded = true;
				Problem.getProblem().setCostMatrix(costMatrix);
			}
		}

		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("DEMANDA TOTAL DE LOS CLIENTES: " + Problem.getProblem().getTotalRequest());
		System.out.println("CAPACIDAD TOTAL DE LOS DEPÓSITOS: " + Problem.getProblem().getTotalCapacity());
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("CARGA EXITOSA: " + loaded);
		System.out.println("FIN DE LA CARGA DE DATOS");
		System.out.println("-------------------------------------------------------------------------------");
		
		return loaded;
	}
}
*/