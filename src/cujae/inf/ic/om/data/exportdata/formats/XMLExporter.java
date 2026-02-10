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

import java.util.ArrayList;
import java.util.List;

/**
 * Clase encargada de exportar soluciones y métricas en formato XML.
 * Implementa la interfaz IExporter.
 */
public class XMLExporter implements IExporter {

    /**
     * Exporta una solución en formato XML, incluyendo información de los clústeres,
     * clientes no asignados y depósitos sin clientes.
     *
     * @param heuristic_name Nombre de la heurística utilizada.
     * @param solution Objeto Solution con los datos a exportar.
     * @param path Ruta del archivo de salida.
     * @throws ExportException Si ocurre un error durante la exportación.
     * @throws ProblemException Si ocurre un error al obtener datos del controlador.
     */
    @Override
    public void export_solution(String heuristic_name, Solution solution, String path)
            throws ExportException, ProblemException {
        if (heuristic_name == null || heuristic_name.trim().isEmpty())
            throw new ExportException("El nombre de la heurística no puede ser nulo o vacío.");
        if (solution == null)
            throw new ExportException("La solución a exportar no puede ser nula.");
        if (path == null || path.trim().isEmpty())
            throw new ExportException("La ruta del archivo XML no puede ser nula o vacía.");

        FileWriter writer = null;

        try {
            writer = new FileWriter(path);

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<solution>\n");

            writer.write("\t<heuristic>" + heuristic_name + "</heuristic>\n");
            writer.write("\t<cluster_count>" + solution.get_clusters().size() + "</cluster_count>\n");

            for (Cluster cluster : solution.get_clusters()) {
                writer.write("\t<cluster>\n");
                writer.write("\t\t<id>" + cluster.get_id_cluster() + "</id>\n");
                writer.write("\t\t<demand>" + cluster.get_request_cluster() + "</demand>\n");
                writer.write("\t\t<element_count>" + cluster.get_items_of_cluster().size() + "</element_count>\n");
                writer.write("\t\t<elements>\n");
                for (Integer id : cluster.get_items_of_cluster()) {
                    writer.write("\t\t\t<element>" + id + "</element>\n");
                }
                writer.write("\t\t</elements>\n");
                writer.write("\t</cluster>\n");
            }

            writer.write("\t<unassigned_customers count=\"" + solution.get_total_unassigned_items() + "\">\n");
            for (Integer id : solution.get_unassigned_items()) {
                writer.write("\t\t<customer>" + id + "</customer>\n");
            }
            writer.write("\t</unassigned_customers>\n");

            ArrayList<Integer> depotsWithout = Controller.get_controller().get_depots_without_customers();
            writer.write("\t<unassigned_depots count=\"" + depotsWithout.size() + "\">\n");
            for (Integer id : depotsWithout) {
                writer.write("\t\t<depot>" + id + "</depot>\n");
            }
            writer.write("\t</unassigned_depots>\n");

            writer.write("</solution>");

        } catch (IOException e) {
            throw new ExportException("Error writing XML solution file: " + path, e);
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                throw new ExportException("Error closing XML file: " + path, e);
            }
        }
    }

    /**
     * Exporta las métricas de evaluación en formato XML, incluyendo todas las ejecuciones
     * y los tiempos asociados.
     *
     * @param instance_number Número o identificador de la instancia.
     * @param executions Lista de listas de métricas por ejecución.
     * @param execution_times Arreglo de tiempos de ejecución por corrida.
     * @param heuristic_name Nombre de la heurística utilizada.
     * @param path Ruta del archivo de salida.
     * @throws ExportException Si ocurre un error durante la escritura del archivo XML.
     */
    @Override
    public void export_metrics(String instance_number, List<List<MetricRecord>> executions, double[] execution_times, 
    		String heuristic_name, String path) throws ExportException {
        if (instance_number == null || instance_number.trim().isEmpty())
            throw new ExportException("El número de instancia no puede ser nulo o vacío.");
        if (executions == null || executions.isEmpty())
            throw new ExportException("La lista de ejecuciones no puede ser nula ni vacía.");
        if (execution_times == null || execution_times.length != executions.size())
            throw new ExportException("El arreglo de tiempos no coincide con la cantidad de ejecuciones.");
        if (heuristic_name == null || heuristic_name.trim().isEmpty())
            throw new ExportException("El nombre de la heurística no puede ser nulo o vacío.");
        if (path == null || path.trim().isEmpty())
            throw new ExportException("La ruta del archivo XML no puede ser nula o vacía.");

        FileWriter writer = null;

        try {
            writer = new FileWriter(path);

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<metrics>\n");

            writer.write("\t<heuristic>" + heuristic_name + "</heuristic>\n");
            writer.write("\t<instance>" + instance_number + "</instance>\n");
            writer.write("\t<executions count=\"" + executions.size() + "\">\n");

            for (int i = 0; i < executions.size(); i++) {
                writer.write("\t\t<execution run=\"" + (i + 1) + "\">\n");
                for (MetricRecord record : executions.get(i)) {
                    writer.write("\t\t\t<metric name=\"" + record.get_name() + "\">" +
                            AbstractTools.truncate_double(record.get_value(), 2) +
                            "</metric>\n");
                }
                writer.write("\t\t\t<time>" + AbstractTools.truncate_double(execution_times[i], 2) + "</time>\n");
                writer.write("\t\t</execution>\n");
            }

            writer.write("\t</executions>\n");
            writer.write("</metrics>");

        } catch (IOException e) {
            throw new ExportException("Error al escribir el archivo XML de métricas: " + path, e);
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                throw new ExportException("Error al cerrar el archivo XML: " + path, e);
            }
        }
    }
}