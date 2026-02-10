package cujae.inf.ic.om.data.importdata.formats;

import java.io.IOException;
import java.util.ArrayList;

import cujae.inf.ic.om.data.importdata.interfaces.IImporter;
import cujae.inf.ic.om.data.importdata.utils.ProblemRawData;
import cujae.inf.ic.om.exceptions.ImportException;

public abstract class AbstractLineImporter implements IImporter {
    protected ArrayList<String> instance_file;

    public ArrayList<String> get_instance_file() { return instance_file; }

    public void set_instance_file(ArrayList<String> instance_file) {
        this.instance_file = instance_file;
    }

    protected int load_total_customers() throws ImportException {
		return 0;}

    protected int load_total_depots() throws ImportException {
		return 0;}

    protected void load_count_vehicles_for_depot(ArrayList<ArrayList<Integer>> count_vehicles) throws ImportException {}

    protected void load_capacity_vehicles(ArrayList<ArrayList<Double>> capacity_vehicles) throws ImportException {}

    protected void load_customers() throws ImportException {}

    protected void load_depots() throws ImportException {}

    public abstract ProblemRawData import_data(String path) throws ImportException;

    protected abstract boolean load_file(String path_file) throws IOException, ImportException;
}