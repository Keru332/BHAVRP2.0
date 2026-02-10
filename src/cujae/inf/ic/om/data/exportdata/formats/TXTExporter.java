package cujae.inf.ic.om.data.exportdata.formats;

import cujae.inf.ic.om.controller.Controller;

import cujae.inf.ic.om.controller.tools.AbstractTools;

import cujae.inf.ic.om.data.exportdata.interfaces.IExporter;

import cujae.inf.ic.om.data.exportdata.utils.MetricRecord;

import cujae.inf.ic.om.exceptions.ExportException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;

import java.io.FileWriter;
import java.io.IOException;

import java.util.List;

/**
 * Clase responsable de exportar soluciones y métricas en formato TXT.
 * Implementa la interfaz {@code IExporter} para soportar la escritura de archivos
 * de salida legibles directamente por humanos, útil para depuración o visualización simple.
 */
public class TXTExporter implements IExporter {
	
    /**
     * Exporta la solución generada por una heurística al formato TXT.
     *
     * @param heuristic_name Nombre de la heurística utilizada.
     * @param solution Objeto {@code Solution} que contiene la asignación cliente-depósito.
     * @param path Ruta del archivo de salida.
     * @throws ExportException Si ocurre un error al escribir o cerrar el archivo.
     * @throws ProblemException Si hay errores internos en la obtención de datos del problema.
     */
    @Override
    public void export_solution(String heuristic_name, Solution solution, String path)
            throws ExportException, ProblemException {

        if (heuristic_name == null || heuristic_name.isEmpty())
            throw new ExportException("El nombre de la heurística no puede ser nulo ni vacío.");
        if (solution == null)
            throw new ExportException("La solución proporcionada es nula.");
        if (path == null || path.isEmpty())
            throw new ExportException("La ruta de exportación no puede ser nula ni vacía.");

        FileWriter writer = null;

        try {
            writer = new FileWriter(path);

            writer.write("Heuristic: " + heuristic_name + "\n\n");
            writer.write("Solution:\n");
            writer.write("Number of clusters: " + solution.get_clusters().size() + "\n\n");

            for (Cluster cluster : solution.get_clusters()) 
            {
                writer.write("Cluster ID: " + cluster.get_id_cluster() + "\n");
                writer.write("Cluster demand: " + cluster.get_request_cluster() + "\n");
                writer.write("Number of elements: " + cluster.get_items_of_cluster().size() + "\n");
                writer.write("Element IDs:\n");

                List<Integer> items = cluster.get_items_of_cluster();
                StringBuilder ids = new StringBuilder("[");
                for (int i = 0; i < items.size(); i++) 
                {
                    ids.append(items.get(i));
                    if (i < items.size() - 1) ids.append(", ");
                }
                ids.append("]");
                writer.write(ids.toString() + "\n\n");
            }
            writer.write("Unassigned customers: " + solution.get_total_unassigned_items() + "\n");
            if (solution.get_total_unassigned_items() > 0) 
            {
                writer.write("Unassigned element IDs:\n");
                for (Integer id : solution.get_unassigned_items()) {
                    writer.write(id + "\n");
                }
            }
            List<Integer> depotsWithout = Controller.get_controller().get_depots_without_customers();
            writer.write("Depots without assigned customers: " + depotsWithout.size() + "\n");
            if (!depotsWithout.isEmpty()) 
            {
                writer.write("Unassigned depot IDs:\n");
                for (Integer id : depotsWithout) {
                    writer.write(id + "\n");
                }
            }
        } catch (IOException e) {
            throw new ExportException("Error al escribir el archivo TXT de solución: " + path, e);
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                throw new ExportException("Error al cerrar el archivo TXT de solución: " + path, e);
            }
        }
    }
	
    /**
     * Exporta las métricas de validación interna generadas por una heurística al formato TXT.
     *
     * @param instance_number Número o identificador de la instancia.
     * @param executions Lista de listas de métricas por ejecución.
     * @param execution_times Tiempos de ejecución correspondientes a cada ejecución.
     * @param heuristic_name Nombre de la heurística evaluada.
     * @param path Ruta del archivo de salida.
     * @throws ExportException Si ocurre un error al escribir o cerrar el archivo.
     */
    @Override
    public void export_metrics(String instance_number, List<List<MetricRecord>> executions, double[] execution_times, 
    		String heuristic_name, String path) throws ExportException {
        if (instance_number == null || instance_number.isEmpty())
            throw new ExportException("El número de instancia no puede ser nulo ni vacío.");
        if (executions == null || executions.isEmpty())
            throw new ExportException("La lista de ejecuciones no puede ser nula ni vacía.");
        if (execution_times == null || execution_times.length != executions.size())
            throw new ExportException("El arreglo de tiempos de ejecución es nulo o no corresponde al número de ejecuciones.");
        if (heuristic_name == null || heuristic_name.isEmpty())
            throw new ExportException("El nombre de la heurística no puede ser nulo ni vacío.");
        if (path == null || path.isEmpty())
            throw new ExportException("La ruta de exportación no puede ser nula ni vacía.");

        FileWriter writer = null;

        try {
            writer = new FileWriter(path);

            writer.write("Heuristic: " + heuristic_name + "\n");
            writer.write("Instance: " + instance_number + "\n");
            writer.write("Executions: " + executions.size() + "\n\n");

            List<MetricRecord> headers = executions.get(0);
            writer.write("Run");
            for (MetricRecord m : headers) 
                writer.write(" | " + m.get_name());
            
            writer.write(" | Time (ms)\n");

            for (int i = 0; i < executions.size(); i++) 
            {
                writer.write((i + 1) + "");
                for (MetricRecord m : executions.get(i)) 
                    writer.write(" | " + AbstractTools.truncate_double(m.get_value(), 2));
                
                writer.write(" | " + AbstractTools.truncate_double(execution_times[i], 2) + "\n");
            }
        } catch (IOException e) {
            throw new ExportException("Error al escribir el archivo TXT de métricas: " + path, e);
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                throw new ExportException("Error al cerrar el archivo TXT de métricas: " + path, e);
            }
        }
    }
}