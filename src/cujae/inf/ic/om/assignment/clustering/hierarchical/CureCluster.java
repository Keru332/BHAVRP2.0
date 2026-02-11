package cujae.inf.ic.om.assignment.clustering.hierarchical;

import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Location;
import cujae.inf.ic.om.problem.output.Cluster;

import java.util.ArrayList;

import static cujae.inf.ic.om.assignment.clustering.hierarchical.CURE.*;

public class CureCluster {
    private  int numRepresentativePoints = 5; // Número de puntos representativos
    private  double shrinkFactor = 0.2; // Factor de contracción (0-1)

    private Cluster cluster;
    private ArrayList<Location> representativePoints;
    private Location centroid;
    private ArrayList<Customer> customers;
    private double request;
    private Depot depot;

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    public CureCluster(Cluster cluster, ArrayList<Customer> customers) {
        this.cluster = cluster;
        this.customers = new ArrayList<>(customers);
        this.request = calculateTotalRequest(customers);
        this.centroid = calculateCentroid(customers);
        this.representativePoints = selectRepresentativePoints(customers);
        shrinkRepresentativePoints();
    }

    private double calculateTotalRequest(ArrayList<Customer> customers) {
        double total = 0.0;
        for (Customer c : customers) {
            total += c.get_request_customer();
        }
        return total;
    }

    private Location calculateCentroid(ArrayList<Customer> customers) {
        double sumX = 0.0;
        double sumY = 0.0;
        for (Customer c : customers) {
            sumX += c.get_location_customer().get_axis_x();
            sumY += c.get_location_customer().get_axis_y();
        }
        return new Location(sumX / customers.size(), sumY / customers.size());
    }

    private ArrayList<Location> selectRepresentativePoints(ArrayList<Customer> customers) {
        ArrayList<Location> points = new ArrayList<>();
        if (customers.isEmpty()) return points;

        // Convertir customers a locations
        ArrayList<Location> locations = new ArrayList<>();
        for (Customer c : customers) {
            locations.add(c.get_location_customer());
        }

        // 1. Primer punto: más lejano del centroide
        Location farthest = null;
        double maxDist = -1.0;
        for (Location loc : locations) {
            double dist = distance(loc, centroid);
            if (dist > maxDist) {
                maxDist = dist;
                farthest = loc;
            }
        }
        points.add(farthest);

        // 2. Seleccionar puntos restantes: más lejanos de los ya seleccionados
        while (points.size() < Math.min(numRepresentativePoints, locations.size())) {
            Location nextPoint = null;
            double maxMinDist = -1.0;

            for (Location loc : locations) {
                double minDist = Double.MAX_VALUE;
                for (Location selected : points) {
                    double dist = distance(loc, selected);
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }
                if (minDist > maxMinDist) {
                    maxMinDist = minDist;
                    nextPoint = loc;
                }
            }
            if (nextPoint != null) {
                points.add(nextPoint);
            }
        }

        return points;
    }

    private void shrinkRepresentativePoints() {
        ArrayList<Location> shrunkPoints = new ArrayList<>();
        for (Location point : representativePoints) {
            double newX = centroid.get_axis_x() + shrinkFactor *
                    (point.get_axis_x() - centroid.get_axis_x());
            double newY = centroid.get_axis_y() + shrinkFactor *
                    (point.get_axis_y() - centroid.get_axis_y());
            shrunkPoints.add(new Location(newX, newY));
        }
        representativePoints = shrunkPoints;
    }

    public void merge(CureCluster other) throws ClusterException {
        // Combinar clientes
        this.customers.addAll(other.customers);
        this.request += other.request;

        // Recalcular centroide y puntos representativos
        this.centroid = calculateCentroid(this.customers);
        this.representativePoints = selectRepresentativePoints(this.customers);
        shrinkRepresentativePoints();

        // Actualizar el cluster base
        this.cluster.set_request_cluster(this.request);
        for (Integer item : other.cluster.get_items_of_cluster()) {
            this.cluster.get_items_of_cluster().add(item);
        }
    }

    public Cluster getCluster() { return cluster; }
    public ArrayList<Location> getRepresentativePoints() { return representativePoints; }
    public Location getCentroid() { return centroid; }
    public double getRequest() { return request; }
    public ArrayList<Customer> getCustomers() { return customers; }


}

