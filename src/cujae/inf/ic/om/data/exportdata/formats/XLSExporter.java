package cujae.inf.ic.om.data.exportdata.formats;

import cujae.inf.ic.om.controller.Controller;

import cujae.inf.ic.om.controller.tools.AbstractTools;

import cujae.inf.ic.om.data.exportdata.interfaces.IExporter;

import cujae.inf.ic.om.data.exportdata.utils.MetricRecord;

import cujae.inf.ic.om.exceptions.ExportException;
import cujae.inf.ic.om.exceptions.ProblemException;

import cujae.inf.ic.om.problem.input.Problem;

import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;

import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Clase encargada de exportar soluciones y métricas en formato Excel (.xls).
 * Utiliza Apache POI para escribir hojas de cálculo legibles por el usuario.
 */
public class XLSExporter implements IExporter {

    /**
     * Exporta la solución de asignación cliente-depósito a un archivo Excel.
     * Cada clúster se escribe en bloques separados, junto con clientes no asignados y depósitos sin asignaciones.
     *
     * @param heuristic_name Nombre de la heurística aplicada.
     * @param solution Objeto Solution con la información de agrupamiento.
     * @param path Ruta del archivo .xls a generar.
     * @throws ExportException Si ocurre un error durante la escritura del archivo.
     * @throws ProblemException Si ocurre un error al obtener datos del controlador.
     */
	@Override
	public void export_solution(String heuristic_name, Solution solution,
			String path) throws ExportException, ProblemException {
        if (heuristic_name == null || heuristic_name.trim().isEmpty())
            throw new ExportException("El nombre de la heurística no puede ser nulo o vacío.");
        if (solution == null)
            throw new ExportException("La solución a exportar no puede ser nula.");
        if (path == null || path.trim().isEmpty())
            throw new ExportException("La ruta del archivo Excel no puede ser nula o vacía.");

		HSSFWorkbook workbook = null;
	    FileOutputStream out = null;
	    
	    try {
	    	workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet(heuristic_name);
            
            int rowNum = 0;

            HSSFRow header = sheet.createRow(rowNum++);
            header.createCell(0).setCellValue("Heuristic:");
            header.createCell(1).setCellValue(heuristic_name);

            HSSFRow clusterCount = sheet.createRow(rowNum++);
            clusterCount.createCell(0).setCellValue("Number of clusters:");
            clusterCount.createCell(1).setCellValue(solution.get_clusters().size());

            for (Cluster cluster : solution.get_clusters()) 
            {
                sheet.createRow(rowNum++);

                HSSFRow idRow = sheet.createRow(rowNum++);
                idRow.createCell(0).setCellValue("Cluster ID:");
                idRow.createCell(1).setCellValue(cluster.get_id_cluster());

                HSSFRow demandRow = sheet.createRow(rowNum++);
                demandRow.createCell(0).setCellValue("Cluster demand:");
                demandRow.createCell(1).setCellValue(cluster.get_request_cluster());

                HSSFRow elementsRow = sheet.createRow(rowNum++);
                elementsRow.createCell(0).setCellValue("Number of elements:");
                elementsRow.createCell(1).setCellValue(cluster.get_items_of_cluster().size());

                HSSFRow idsRow = sheet.createRow(rowNum++);
                idsRow.createCell(0).setCellValue("Element IDs:");
                StringBuilder ids = new StringBuilder();
                List<Integer> items = cluster.get_items_of_cluster();
                for (int i = 0; i < items.size(); i++) 
                {
                    ids.append(items.get(i));
                    if (i < items.size() - 1) ids.append(", ");
                }
                idsRow.createCell(1).setCellValue(ids.toString());
            }

            rowNum++;
            HSSFRow unassignedRow = sheet.createRow(rowNum++);
            unassignedRow.createCell(0).setCellValue("Unassigned customers:");
            unassignedRow.createCell(1).setCellValue(solution.get_total_unassigned_items());

            if (solution.get_total_unassigned_items() > 0) 
            {
                HSSFRow idsHeader = sheet.createRow(rowNum++);
                idsHeader.createCell(0).setCellValue("Unassigned element IDs:");

                for (Integer id : solution.get_unassigned_items()) 
                {
                    HSSFRow idRow = sheet.createRow(rowNum++);
                    idRow.createCell(0).setCellValue(id);
                }
            }

            ArrayList<Integer> depotsWithoutCustomers = Controller.get_controller().get_depots_without_customers();
            HSSFRow depotsRow = sheet.createRow(rowNum++);
            depotsRow.createCell(0).setCellValue("Depots without assigned customers:");
            depotsRow.createCell(1).setCellValue(depotsWithoutCustomers.size());

            if (!depotsWithoutCustomers.isEmpty()) {
                HSSFRow idsHeader = sheet.createRow(rowNum++);
                idsHeader.createCell(0).setCellValue("Unassigned depot IDs:");

                for (Integer id : depotsWithoutCustomers) {
                    HSSFRow idRow = sheet.createRow(rowNum++);
                    idRow.createCell(0).setCellValue(id);
                }
            }

            out = new FileOutputStream(path);
            workbook.write(out);
        } catch (IOException e) {
            throw new ExportException("Error al escribir el archivo Excel de solución: " + path, e);
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException e) {
                throw new ExportException("Error al cerrar el archivo Excel: " + path, e);
            }
        }
    }


    /**
     * Exporta las métricas de evaluación de una heurística en formato Excel.
     * Cada ejecución se representa como una fila, incluyendo estadísticas agregadas (mínimo, máximo y promedio).
     *
     * @param instance_number Identificador de la instancia evaluada.
     * @param executions Lista de ejecuciones, cada una con sus métricas.
     * @param execution_times Arreglo de tiempos de ejecución asociados.
     * @param heuristic_name Nombre de la heurística evaluada.
     * @param path Ruta del archivo .xls a generar.
     * @throws ExportException Si ocurre un error durante la exportación.
     */
	@Override
	public void export_metrics(String instance_number,
			List<List<MetricRecord>> executions, double[] execution_times,
			String heuristic_name, String path) throws ExportException {
        if (instance_number == null || instance_number.trim().isEmpty())
            throw new ExportException("El número de instancia no puede ser nulo o vacío.");
        if (executions == null || executions.isEmpty())
            throw new ExportException("La lista de ejecuciones no puede ser nula ni vacía.");
        if (execution_times == null || execution_times.length != executions.size())
            throw new ExportException("La cantidad de tiempos no coincide con la cantidad de ejecuciones.");
        if (heuristic_name == null || heuristic_name.trim().isEmpty())
            throw new ExportException("El nombre de la heurística no puede ser nulo o vacío.");
        if (path == null || path.trim().isEmpty())
            throw new ExportException("La ruta del archivo Excel no puede ser nula o vacía.");

	    HSSFWorkbook workbook = null;
	    FileOutputStream out = null;

		try {
			workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet(heuristic_name);
            
            int num_executions = executions.size();
            int num_metrics = executions.get(0).size();
            int num_customers = Problem.get_problem().get_total_customers();
            int num_depots = Problem.get_problem().get_total_depots();
            double total_demand = Problem.get_problem().get_total_request();
            
            HSSFRow meta1 = sheet.createRow(0);
            meta1.createCell(0).setCellValue("Instance:");
            meta1.createCell(1).setCellValue(instance_number);
            meta1.createCell(2).setCellValue("Customers:");
            meta1.createCell(3).setCellValue(num_customers);
            meta1.createCell(4).setCellValue("Depots:");
            meta1.createCell(5).setCellValue(num_depots);

            HSSFRow meta2 = sheet.createRow(1);
            meta2.createCell(0).setCellValue("Executions:");
            meta2.createCell(1).setCellValue(num_executions);
            meta2.createCell(2).setCellValue("Total Demand:");
            meta2.createCell(3).setCellValue(total_demand);

            // Encabezado
            HSSFRow header = sheet.createRow(0);
            header.createCell(0).setCellValue("Run");
            for (int i = 0; i < num_metrics; i++) 
                header.createCell(i + 1).setCellValue(executions.get(0).get(i).get_name());
            
            header.createCell(num_metrics + 1).setCellValue("Time");

            // Datos
            double[] min = new double[num_metrics + 1];
            double[] max = new double[num_metrics + 1];
            double[] sum = new double[num_metrics + 1];
            Arrays.fill(min, Double.MAX_VALUE);
            Arrays.fill(max, Double.MIN_VALUE);

            for (int i = 0; i < num_executions; i++) 
            {
                HSSFRow row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);

                List<MetricRecord> metricList = executions.get(i);
                for (int j = 0; j < num_metrics; j++) 
                {
                    double value = AbstractTools.truncate_double(metricList.get(j).get_value(), 2);
                    row.createCell(j + 1).setCellValue(value);
                    sum[j] += value;
                    min[j] = Math.min(min[j], value);
                    max[j] = Math.max(max[j], value);
                }

                double time = AbstractTools.truncate_double(execution_times[i], 2);
                row.createCell(num_metrics + 1).setCellValue(time);
                sum[num_metrics] += time;
                min[num_metrics] = Math.min(min[num_metrics], time);
                max[num_metrics] = Math.max(max[num_metrics], time);
            }

            int statsRow = num_executions + 2;

            HSSFRow minRow = sheet.createRow(statsRow);
            minRow.createCell(0).setCellValue("Min");
            for (int i = 0; i < min.length; i++) 
                minRow.createCell(i + 1).setCellValue(AbstractTools.truncate_double(min[i], 2));
            
            HSSFRow maxRow = sheet.createRow(statsRow + 1);
            maxRow.createCell(0).setCellValue("Max");
            for (int i = 0; i < max.length; i++) 
                maxRow.createCell(i + 1).setCellValue(AbstractTools.truncate_double(max[i], 2));
            
            HSSFRow avgRow = sheet.createRow(statsRow + 2);
            avgRow.createCell(0).setCellValue("Avg");
            for (int i = 0; i < sum.length; i++) 
                avgRow.createCell(i + 1).setCellValue(AbstractTools.truncate_double(sum[i] / num_executions, 2));
            
            out = new FileOutputStream(path);
            workbook.write(out);
        } catch (IOException e) {
            throw new ExportException("Error al escribir el archivo Excel de métricas: " + path, e);
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException e) {
                throw new ExportException("Error al cerrar el archivo Excel: " + path, e);
            }
        }
    }
}