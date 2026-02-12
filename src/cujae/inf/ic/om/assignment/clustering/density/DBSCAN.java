package cujae.inf.ic.om.assignment.clustering.density;

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


public class DBSCAN extends AbstractDensity {
    public static DistanceType distance_type = DistanceType.Euclidean;
    private static Solution solution = new Solution();
    private ArrayList<Integer> list_id_elements;
    private ArrayList<Cluster> list_clusters;
    private ArrayList<Depot> list_depots;

    private double epsilon = 1f;
    private int minimumNumberOfClusterMembers = 2;
    private ArrayList<Customer> list_customers;

    // Estado del algoritmo
    private Set<Customer> visitedCustomers;
    private Set<Customer> assignedCustomers;
    private Queue<Customer> expansionQueue;


    public DBSCAN(int minNumElements, double maxDistance) {
        super();
        setMinimalNumberOfMembersForCluster(minNumElements);
        setMaximalDistanceOfClusterMembers(maxDistance);
    }
    public void setMinimalNumberOfMembersForCluster(final int minimalNumberOfMembers) {
        this.minimumNumberOfClusterMembers = minimalNumberOfMembers;
    }
    public void setMaximalDistanceOfClusterMembers(final double maximalDistance) {
        this.epsilon = maximalDistance;
    }

    private ArrayList<Customer> getNeighbours(final Customer inputValue) throws ClusterException {
        ArrayList<Customer> neighbours = new ArrayList<Customer>();
        for (Customer candidate : list_customers) {
            if (distance(inputValue.get_location_customer(), candidate.get_location_customer()) <= epsilon) {
                neighbours.add(candidate);
            }
        }
        return neighbours;
    }
    private ArrayList<Customer> getNeighboursDepots(final Depot inputValue) throws ClusterException {
        ArrayList<Customer> neighbours = new ArrayList<Customer>();
        for (Customer candidate : list_customers) {
            if (distance(inputValue.get_location_depot(), candidate.get_location_customer()) <= epsilon) {
                neighbours.add(candidate);
            }
        }
        return neighbours;
    }

    @Override
    public Solution to_clustering() throws ClusterException, ProblemException, AssignmentException {
        try {
            initialize();
            assign();
            return finish();
        } catch (ClusterException | ProblemException e) {
            throw e;
        } catch (Exception e) {
            throw new AssignmentException("Error inesperado en DBSCAN.");
        }
    }

    public void initialize() throws AssignmentException, ProblemException, ClusterException {
        try {
            list_id_elements = Problem.get_problem().get_list_id_depots();
            if (list_id_elements == null || list_id_elements.isEmpty())
                throw new AssignmentException("No se encontraron elementos iniciales para los cl�steres.");

            list_clusters = initialize_clusters(list_id_elements);
            if (list_clusters == null || list_clusters.isEmpty())
                throw new ClusterException("No se pudieron inicializar los cl�steres a partir de los elementos proporcionados.");

            list_customers = new ArrayList<Customer>(Problem.get_problem().get_customers());
            if (list_customers.isEmpty())
                throw new AssignmentException("La lista de clientes est� vac�a.");

            list_depots = new ArrayList<Depot>(Problem.get_problem().get_depots());
            if (list_depots.isEmpty())
                throw new AssignmentException("La lista de dep�sitos est� vac�a.");

            if (list_customers == null) {
                throw new ProblemException("DBSCAN: List of input values is null.");
            }

            if (list_customers.isEmpty()) {
                throw new ProblemException("DBSCAN: List of input values is empty.");
            }

            if (list_customers.size() < 2) {
                throw new ProblemException("DBSCAN: Less than two input values cannot be clustered. Number of input values: " + list_customers.size());
            }

            if (epsilon < 0) {
                throw new ProblemException("DBSCAN: Maximum distance of input values cannot be negative. Current value: " + epsilon);
            }

            if (minimumNumberOfClusterMembers < 2) {
                throw new ProblemException("DBSCAN: Clusters with less than 2 members don't make sense. Current value: " + minimumNumberOfClusterMembers);
            }

        } catch (ClusterException | ProblemException e) {
            throw e;
        } catch (Exception e) {
            throw new AssignmentException("Error inesperado durante la fase de inicializaci�n.", e);
        }
    }


    public void assign() throws ProblemException, ClusterException {
        visitedCustomers = new HashSet<>();
        assignedCustomers = new HashSet<>();
        expansionQueue = new LinkedList<>();

        for (int i = 0; i < list_depots.size(); i++) {
            Depot depot = list_depots.get(i);
            Cluster cluster = list_clusters.get(i);
            ArrayList<Customer> directNeighbors = getNeighboursDepots(depot);

            if (directNeighbors.size() >= minimumNumberOfClusterMembers) {
                expandCluster(depot, directNeighbors, cluster);
            }else{
                throw new ClusterException("No se pudieron asignar clientes al deposito, considere aumentar radio o reducir minimo de puntos.");
            }
        }
        assignUnassignedCustomers();
    }

    private void expandCluster(Depot depot, ArrayList<Customer> seeds, Cluster cluster)
            throws ClusterException {

        expansionQueue.clear();
        expansionQueue.addAll(seeds);

        while (!expansionQueue.isEmpty()) {
            Customer current = expansionQueue.poll();

            // Si ya fue visitado, saltar
            if (visitedCustomers.contains(current)) {
                continue;
            }
            visitedCustomers.add(current);

            // Asignar cliente al cluster
            assignCustomerToCluster(current, cluster);

            // Obtener vecinos del cliente actual
            ArrayList<Customer> neighbors = getNeighbours(current);

            // Si es punto núcleo, añadir vecinos a la cola de expansión
            if (neighbors.size() >= minimumNumberOfClusterMembers) {
                for (Customer neighbor : neighbors) {
                    if (!visitedCustomers.contains(neighbor) &&
                            !expansionQueue.contains(neighbor)) {
                        expansionQueue.add(neighbor);
                    }
                }
            }
        }
    }

    private void assignCustomerToCluster(Customer customer, Cluster cluster) throws ClusterException {
        if (!assignedCustomers.contains(customer)) {
            cluster.get_items_of_cluster().add(customer.get_id_customer());
            double totalRequest = cluster.get_request_cluster() + customer.get_request_customer();
            cluster.set_request_cluster(totalRequest);

            assignedCustomers.add(customer);
        }
    }

    private void assignUnassignedCustomers() throws ClusterException {
        for (Customer customer : list_customers) {
            if (!assignedCustomers.contains(customer)) {
                Depot nearestDepot = findNearestDepot(customer);

                for (Cluster cluster : list_clusters) {
                    if (cluster.get_id_cluster() == nearestDepot.get_id_depot()) {
                        assignCustomerToCluster(customer, cluster);
                        break;
                    }
                }
            }
        }
    }

    private Depot findNearestDepot(Customer customer) {
        Depot nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Depot depot : list_depots) {
            double dist = distance(depot.get_location_depot(), customer.get_location_customer());
            if (dist < minDistance) {
                minDistance = dist;
                nearest = depot;
            }
        }
        return nearest;
    }



    public Solution finish(){
        solution.get_clusters().clear();
        solution.get_clusters().addAll(list_clusters);
        return solution;
    }

    public static double distance(Location loc1, Location loc2) {
        double dx = loc1.get_axis_x() - loc2.get_axis_x();
        double dy = loc1.get_axis_y() - loc2.get_axis_y();
        return Math.sqrt(dx * dx + dy * dy);
    }
}