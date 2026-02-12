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
import java.util.ArrayList;
import java.util.Iterator;


public class DBSCAN extends AbstractDensity {
    public static DistanceType distance_type = DistanceType.Euclidean;
    private static Solution solution = new Solution();
    private ArrayList<Integer> list_id_elements;
    private ArrayList<Cluster> list_clusters;
    private ArrayList<Depot> list_depots;

    private double epsilon = 1f;
    private int minimumNumberOfClusterMembers = 2;
    private ArrayList<Customer> inputValues = null;
    private ArrayList<Customer> NoVisitedPoints = new ArrayList<Customer>();

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
        for (Customer candidate : inputValues) {
            if (distance(inputValue.get_location_customer(), candidate.get_location_customer()) <= epsilon) {
                neighbours.add(candidate);
            }
        }
        return neighbours;
    }
    private ArrayList<Customer> getNeighboursDepots(final Depot inputValue) throws ClusterException {
        ArrayList<Customer> neighbours = new ArrayList<Customer>();
        for (Customer candidate : inputValues) {
            if (distance(inputValue.get_location_depot(), candidate.get_location_customer()) <= epsilon) {
                neighbours.add(candidate);
            }
        }
        return neighbours;
    }



    /**
     * Merges the elements of the right collection to the left one and returns
     * the combination.
     *
     * @param neighbours1 left collection
     * @param neighbours2 right collection
     * @return Modified left collection
     */
    private ArrayList<Customer> mergeRightToLeftCollection(final ArrayList<Customer> neighbours1,
                                                    final ArrayList<Customer> neighbours2) {
        for (Customer tempPt : neighbours2) {
            if (!neighbours1.contains(tempPt)) {
                neighbours1.add(tempPt);
            }
        }
        return neighbours1;
    }

    /**
     * Applies the clustering and returns a collection of clusters (i.e., a list
     * of lists of the respective cluster members).
     *
     * @return Collection of clusters identified as part of the clustering process
     */

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

            inputValues = new ArrayList<Customer>(Problem.get_problem().get_customers());
            if (inputValues.isEmpty())
                throw new AssignmentException("La lista de clientes est� vac�a.");

            list_depots = new ArrayList<Depot>(Problem.get_problem().get_depots());
            if (list_depots.isEmpty())
                throw new AssignmentException("La lista de dep�sitos est� vac�a.");

        } catch (ClusterException | ProblemException e) {
            throw e;
        } catch (Exception e) {
            throw new AssignmentException("Error inesperado durante la fase de inicializaci�n.", e);
        }
    }


    public void assign() throws ProblemException, ClusterException {
        if (inputValues == null) {
            throw new ProblemException("DBSCAN: List of input values is null.");
        }

        if (inputValues.isEmpty()) {
            throw new ProblemException("DBSCAN: List of input values is empty.");
        }

        if (inputValues.size() < 2) {
            throw new ProblemException("DBSCAN: Less than two input values cannot be clustered. Number of input values: " + inputValues.size());
        }

        if (epsilon < 0) {
            throw new ProblemException("DBSCAN: Maximum distance of input values cannot be negative. Current value: " + epsilon);
        }

        if (minimumNumberOfClusterMembers < 2) {
            throw new ProblemException("DBSCAN: Clusters with less than 2 members don't make sense. Current value: " + minimumNumberOfClusterMembers);
        }
        ArrayList<Customer> NoisePoint = new ArrayList<>();
        ArrayList<Depot> DepotsList = new ArrayList<>(list_depots);

        ArrayList<Cluster> resultList = new ArrayList<>();
        NoVisitedPoints = new ArrayList<>(inputValues);


        while(!NoVisitedPoints.isEmpty()){
            for(Depot depots : DepotsList){
                ArrayList<Customer> vecino = getNeighboursDepots(depots);
                if(vecino.size() < minimumNumberOfClusterMembers){
                    throw new ProblemException("El deposito no tiene clientes cercanos, considere aumentar el valor del radio o disminuir el numero de puntos");
                }else{
                    Cluster base = new Cluster(depots.get_id_depot(),0.0, new ArrayList<>());
                    base.get_items_of_cluster().add(depots.get_id_depot());

                    for (Customer customer : vecino) {
                        if (NoVisitedPoints.contains(customer)) {
                            NoVisitedPoints.remove(customer);

                            ArrayList<Customer> vecinoCustomer = getNeighbours(customer);

                            if (vecinoCustomer.size() >= minimumNumberOfClusterMembers) {
                                vecino.addAll(vecinoCustomer);
                            }
                            double totalRequest = base.get_request_cluster() + customer.get_request_customer();
                            base.get_items_of_cluster().add(customer.get_id_customer());
                            base.set_request_cluster(totalRequest);
                        }
                    }
                }

            }
        }

        ArrayList<Customer> neighbours;

    }
    public Solution finish(){
        return solution;
    }

    public static double distance(Location loc1, Location loc2) {
        double dx = loc1.get_axis_x() - loc2.get_axis_x();
        double dy = loc1.get_axis_y() - loc2.get_axis_y();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
