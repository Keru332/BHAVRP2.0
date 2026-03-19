package cujae.inf.ic.om.assignment.clustering.density;

import cujae.inf.ic.om.assignment.clustering.AbstractClustering;
import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.ProblemException;
import cujae.inf.ic.om.factory.DistanceType;
import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Location;
import cujae.inf.ic.om.problem.input.Problem;
import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;

import java.util.*;

public abstract class AbstractDensity extends AbstractClustering {

    public static DistanceType distance_type = DistanceType.Euclidean;

    public AbstractDensity() {
    }

    public static double distance(Location loc1, Location loc2) {
        double dx = loc1.get_axis_x() - loc2.get_axis_x();
        double dy = loc1.get_axis_y() - loc2.get_axis_y();
        return Math.sqrt(dx * dx + dy * dy);
    }

    protected ArrayList<Customer> getNeighbours(final Customer inputValue, ArrayList<Customer> list_customers, double epsilon) throws ClusterException {
        ArrayList<Customer> neighbours = new ArrayList<Customer>();
        for (Customer candidate : list_customers) {
            if (distance(inputValue.get_location_customer(), candidate.get_location_customer()) <= epsilon) {
                neighbours.add(candidate);
            }
        }
        return neighbours;
    }


    public void assignCustomerToCluster(Customer customer, Cluster cluster, Set<Customer> assignedCustomers) throws ClusterException {
        if (!assignedCustomers.contains(customer)) {
            cluster.get_items_of_cluster().add(customer.get_id_customer());
            double totalRequest = cluster.get_request_cluster() + customer.get_request_customer();
            cluster.set_request_cluster(totalRequest);

            assignedCustomers.add(customer);
        }
    }

    public int calculateMaxClientsPerDepot(int totalCustomers, int totalDepots) {
        return (int) Math.ceil((double) totalCustomers / totalDepots);
    }
}
