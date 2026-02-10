package cujae.inf.ic.om.controller.tools;

import java.util.List;

import cujae.inf.ic.om.factory.DistanceType;
import cujae.inf.ic.om.factory.interfaces.EFileFormatExporter;
import cujae.inf.ic.om.factory.interfaces.EFileFormatImporter;
import cujae.inf.ic.om.factory.interfaces.EMetricType;

public class ExecutionConfig {
	public String instance_path;
	public String instance_number;
    public EFileFormatImporter input_format;
    public DistanceType distance_type;
    public EFileFormatExporter output_format;
    public boolean export_solution;
    public boolean export_metrics_summary;
    public boolean export_visualization;
    public int number_of_executions;
    public List<EMetricType> selected_metrics;
	
    public ExecutionConfig() {
		super();
	}
    
    public ExecutionConfig(String instance_path, String instance_number,
			EFileFormatImporter input_format, DistanceType distance_type,
			EFileFormatExporter output_format, boolean export_solution,
			boolean export_metrics_summary, boolean export_visualization,
			int number_of_executions, List<EMetricType> selected_metrics) {
		super();
		this.instance_path = instance_path;
		this.instance_number = instance_number;
		this.input_format = input_format;
		this.distance_type = distance_type;
		this.output_format = output_format;
		this.export_solution = export_solution;
		this.export_metrics_summary = export_metrics_summary;
		this.export_visualization = export_visualization;
		this.number_of_executions = number_of_executions;
		this.selected_metrics = selected_metrics;
	}

	public String get_instance_path() {
		return instance_path;
	}

	public void set_instance_path(String instance_path) {
		this.instance_path = instance_path;
	}
	
	public String get_instance_number() {
	    return instance_number;
	}

	public void set_instance_number(String instance_number) {
	    this.instance_number = instance_number;
	}

	public EFileFormatImporter get_input_format() {
		return input_format;
	}

	public void set_input_format(EFileFormatImporter input_format) {
		this.input_format = input_format;
	}

	public DistanceType get_distance_type() {
		return distance_type;
	}

	public void set_distance_type(DistanceType distance_type) {
		this.distance_type = distance_type;
	}

	public EFileFormatExporter get_output_format() {
		return output_format;
	}

	public void set_output_format(EFileFormatExporter output_format) {
		this.output_format = output_format;
	}

	public boolean is_export_solution() {
		return export_solution;
	}

	public void set_export_solution(boolean export_solution) {
		this.export_solution = export_solution;
	}

	public boolean is_export_metrics_summary() {
		return export_metrics_summary;
	}

	public void set_export_metrics_summary(boolean export_metrics_summary) {
		this.export_metrics_summary = export_metrics_summary;
	}

	public boolean is_export_visualization() {
		return export_visualization;
	}

	public void set_export_visualization(boolean export_visualization) {
		this.export_visualization = export_visualization;
	}

	public int get_number_of_executions() {
		return number_of_executions;
	}

	public void set_number_of_executions(int number_of_executions) {
		this.number_of_executions = number_of_executions;
	}

	public List<EMetricType> get_selected_metrics() {
		return selected_metrics;
	}

	public void set_selected_metrics(List<EMetricType> selected_metrics) {
		this.selected_metrics = selected_metrics;
	}
}