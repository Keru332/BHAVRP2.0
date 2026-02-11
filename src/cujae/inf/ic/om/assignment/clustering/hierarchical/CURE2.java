package cujae.inf.ic.om.assignment.clustering.hierarchical;

import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.CostMatrixException;
import cujae.inf.ic.om.exceptions.ProblemException;
import cujae.inf.ic.om.factory.DistanceType;
import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Location;
import cujae.inf.ic.om.problem.input.Problem;
import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;
import cujae.inf.ic.om.service.OSRMService;

import java.util.ArrayList;

public class CURE2 extends AbstractHierarchical {
	public static DistanceType distance_type = DistanceType.Euclidean;
	private static Solution solution = new Solution();

    private ArrayList<CureCluster> list_clusters;
	private ArrayList<CureCluster> list_clustersDepot;

    @Override
	public Solution to_clustering() throws ProblemException, ClusterException,
			CostMatrixException, AssignmentException {
		try {
			initialize();
			assign();
			return finish();
		} catch (ClusterException | CostMatrixException | ProblemException e) {
			throw e;
		} catch (Exception e) {
			throw new AssignmentException("Error inesperado en CURE.");
		}
	}

	@Override
	public void initialize() throws ClusterException, ProblemException, AssignmentException {
		try {
            ArrayList<Integer> list_id_elements = Problem.get_problem().get_list_id_elements();
			if (list_id_elements == null || list_id_elements.isEmpty())
				throw new AssignmentException("No se encontraron elementos iniciales para los clusteres.");


            ArrayList<Customer> list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
			if (list_customers_to_assign.isEmpty())
				throw new AssignmentException("La lista de clientes est� vac�a.");

			list_clusters = new ArrayList<>();
			for (Customer customer : Problem.get_problem().get_customers()) {
				Cluster base = new Cluster();
				base.set_id_cluster(customer.get_id_customer());
				base.set_request_cluster(customer.get_request_customer());
				base.get_items_of_cluster().add(customer.get_id_customer());

				ArrayList<Customer> list = new ArrayList<>();
				list.add(customer);
				list_clusters.add(new CureCluster(base, list));
			}
			if (list_clusters == null || list_clusters.isEmpty())
				throw new ClusterException("No se pudieron inicializar los cl�steres a partir de los elementos proporcionados.");


            ArrayList<Depot> list_depots = new ArrayList<Depot>(Problem.get_problem().get_depots());
			if (list_depots.isEmpty())
				throw new AssignmentException("La lista de dep�sitos est� vac�a.");

			list_clustersDepot = new ArrayList<>();
			for(Depot depot : list_depots){
				Cluster base = new Cluster();
				base.set_id_cluster(depot.get_id_depot());
				base.set_request_cluster(0.0);
				base.get_items_of_cluster().add(depot.get_id_depot());
				ArrayList<Customer> list = new ArrayList<>();
				list.add(new Customer(depot.get_id_depot(), 0.0, depot.get_location_depot()));
				CureCluster c = new CureCluster(base, list);
				c.setDepot(depot);
				list_clustersDepot.add(c);
			}
		} catch (Exception e) {
			throw new AssignmentException("Error en inicialización CURE.");
		}
	}

	@Override
	public void assign() throws CostMatrixException, AssignmentException {
		try {
			boolean change = false;
			while (!list_clusters.isEmpty() && !change) {

				double minDistance = Double.MAX_VALUE;
				int cluster1Idx = -1;
				int cluster2Idx = -1;

				for (int i = 0; i < list_clustersDepot.size(); i++) {
					for (int j = 0; j < list_clusters.size(); j++) {
						double dist = calculateClusterDistance(
								list_clustersDepot.get(i),
								list_clusters.get(j)
						);

						double tempFusedRequest = list_clustersDepot.get(i).getRequest() +
								list_clusters.get(j).getRequest();

						if(tempFusedRequest <= Problem.get_problem().get_total_capacity_by_depot(list_clustersDepot.get(i).getDepot())){
							if (dist < minDistance) {
								minDistance = dist;
								cluster1Idx = i;
								cluster2Idx = j;
							}
						}
					}
				}

				if (cluster1Idx == -1 || cluster2Idx == -1) {
					System.out.println("No se puede crear mas clusteres");
					change = true;
				}

				if(!change){
					CureCluster cluster1 = list_clustersDepot.get(cluster1Idx);
					CureCluster cluster2 = list_clusters.get(cluster2Idx);

					cluster1.merge(cluster2);
					list_clusters.remove(cluster2Idx);
				}
			}

		} catch (Exception e) {
			throw new AssignmentException("Error en asignación de CURE.");
		}
	}

	private double calculateClusterDistance(CureCluster c1, CureCluster c2) {
		double minDistance = Double.MAX_VALUE;

		for (Location p1 : c1.getRepresentativePoints()) {
			for (Location p2 : c2.getRepresentativePoints()) {
				double dist = distance(p1, p2);
				if (dist < minDistance) {
					minDistance = dist;
				}
			}
		}

		return minDistance;
	}
	private double calculateClusterDepotDistance(CureCluster cluster, Depot depot) {
		double minDistance = Double.MAX_VALUE;
		Location depotLocation = depot.get_location_depot();

		for (Location point : cluster.getRepresentativePoints()) {
			double dist = distance(point, depotLocation);
			if (dist < minDistance) {
				minDistance = dist;
			}
		}

		return minDistance;
	}

	@Override
	public Solution finish() throws AssignmentException {
		try {
			for (CureCluster cureCluster : list_clustersDepot) {
				Cluster cluster = cureCluster.getCluster();
				if (!cluster.get_items_of_cluster().isEmpty()) {
					solution.get_clusters().add(cluster);
				}
			}
			for (CureCluster cluster : list_clusters) {
				for(Integer customerID : cluster.getCluster().get_items_of_cluster())
					solution.get_unassigned_items().add(customerID);
			}

			OSRMService.clear_distance_cache();
			return solution;

		} catch (Exception e) {
			throw new AssignmentException("Error en finalización.", e);
		}
	}

	public static double distance(Location loc1, Location loc2) {
		double dx = loc1.get_axis_x() - loc2.get_axis_x();
		double dy = loc1.get_axis_y() - loc2.get_axis_y();
		return Math.sqrt(dx * dx + dy * dy);
	}


}