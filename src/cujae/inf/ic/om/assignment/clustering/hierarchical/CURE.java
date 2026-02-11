package cujae.inf.ic.om.assignment.clustering.hierarchical;

import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.CostMatrixException;
import cujae.inf.ic.om.exceptions.ProblemException;
import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Location;
import cujae.inf.ic.om.problem.input.Problem;
import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;
import cujae.inf.ic.om.factory.DistanceType;
import cujae.inf.ic.om.service.OSRMService;

import java.util.*;

public class CURE extends AbstractHierarchical {
	public static DistanceType distance_type = DistanceType.Euclidean;
	private static Solution solution = new Solution();

	private ArrayList<Integer> list_id_elements;
	private ArrayList<CureCluster> list_clusters;
	private ArrayList<Customer> list_customers_to_assign;
	private ArrayList<Depot> list_depots;
	private ArrayList<Customer> unnasigned_list_cure_clusters;

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
			unnasigned_list_cure_clusters = new ArrayList<>();
			list_id_elements = Problem.get_problem().get_list_id_elements();
			if (list_id_elements == null || list_id_elements.isEmpty())
				throw new AssignmentException("No se encontraron elementos iniciales para los clusteres.");


			list_customers_to_assign = new ArrayList<Customer>(Problem.get_problem().get_customers());
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


			list_depots = new ArrayList<Depot>(Problem.get_problem().get_depots());
			if (list_depots.isEmpty())
				throw new AssignmentException("La lista de dep�sitos est� vac�a.");
		} catch (Exception e) {
			throw new AssignmentException("Error en inicialización CURE.");
		}
	}

	@Override
	public void assign() throws CostMatrixException, AssignmentException {
		try {
			int totalDepot = list_depots.size();

			//CALCULAR capacidad máxima de cualquier depósito
			double maxDepotCapacity = 0;
			for (Depot depot : list_depots) {
				try {
					double capacity = Problem.get_problem().get_total_capacity_by_depot(depot);
					if (capacity > maxDepotCapacity) {
						maxDepotCapacity = capacity;
					}
				} catch (Exception e) {
					throw new AssignmentException("Error al obtener deposito");
				}
			}

			while (list_clusters.size() > totalDepot) {
				// Encontrar el par de clusters más cercano
				double minDistance = Double.MAX_VALUE;
				int cluster1Idx = -1;
				int cluster2Idx = -1;

				for (int i = 0; i < list_clusters.size(); i++) {
					for (int j = i + 1; j < list_clusters.size(); j++) {
						double dist = calculateClusterDistance(
								list_clusters.get(i),
								list_clusters.get(j)
						);

						double tempFusedRequest = list_clusters.get(i).getRequest() +
								list_clusters.get(j).getRequest();

						if (dist < minDistance && tempFusedRequest <= maxDepotCapacity) {
							minDistance = dist;
							cluster1Idx = i;
							cluster2Idx = j;
						}
					}
				}

				if (cluster1Idx == -1 || cluster2Idx == -1) {
					System.out.println("No se puede crear mas clusteres");
					break;
				}

				// Fusionar los clusters
				CureCluster cluster1 = list_clusters.get(cluster1Idx);
				CureCluster cluster2 = list_clusters.get(cluster2Idx);

				cluster1.merge(cluster2);
				list_clusters.remove(cluster2Idx);
			}

		} catch (Exception e) {
			throw new AssignmentException("Error en asignación de CURE.");
		}
	}

	private double calculateClusterDistance(CureCluster c1, CureCluster c2) {
		// Distancia mínima entre puntos representativos
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
		// Distancia mínima entre puntos representativos y depósito
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

	public void assignClustersToDepots() throws AssignmentException {
		try {
			ArrayList<Depot> availableDepots = new ArrayList<>(list_depots);
			list_clusters.sort((c1, c2) -> Double.compare(c2.getRequest(), c1.getRequest()));

			// Buscar mejor deposito
			for (CureCluster cluster : list_clusters) {
				Depot bestDepot = null;
				double bestScore = Double.MAX_VALUE;

				// Evaluar  deposito
				for (Depot depot : availableDepots) {
					try {
						double depotCapacity = Problem.get_problem().get_total_capacity_by_depot(depot);
						double clusterRequest = cluster.getRequest();

						if (clusterRequest <= depotCapacity) {
							double distance = calculateClusterDepotDistance(cluster, depot);
							// Score simple: distancia dividida por capacidad sobrante
							// (Prefiere depósitos cercanos y con más espacio)
							double remainingCapacity = depotCapacity - clusterRequest;
							double score = distance / (remainingCapacity + 1); // +1 para evitar división por 0

							if (score < bestScore) {
								bestScore = score;
								bestDepot = depot;
							}
						}
					} catch (Exception e) {
						throw new AssignmentException("Error al asignar deposito");
					}
				}

				// Asignar o marcar como no asignado
				if (bestDepot != null) {
					cluster.setDepot(bestDepot);
					System.out.println("✓ Cluster (demanda: " + cluster.getRequest() +
							") asignado a depósito " + bestDepot.get_id_depot());

					// ELIMINAR el depósito asignado de disponibles
					// availableDepots.remove(bestDepot);

				} else {
					// Si no encontró depósito con capacidad, marcar clientes como no asignados
					System.out.println("✗ Cluster (demanda: " + cluster.getRequest() +
							") NO asignado - demanda excede capacidad de cualquier depósito");
					moveClusterCustomersToUnassigned(cluster);
				}
			}

		} catch (Exception e) {
			throw new AssignmentException("Error en asignación a depósitos.", e);
		}
	}

	private void moveClusterCustomersToUnassigned(CureCluster cluster) {
		// Mover todos los clientes del cluster a la lista de no asignados
        unnasigned_list_cure_clusters.addAll(cluster.getCustomers());

		// Eliminar el cluster de la lista principal
		list_clusters.remove(cluster);
	}

	@Override
	public Solution finish() throws AssignmentException {
		try {
			assignClustersToDepots();

			for (CureCluster cureCluster : list_clusters) {
				Cluster cluster = cureCluster.getCluster();
				if (!cluster.get_items_of_cluster().isEmpty()) {
					Depot depot = cureCluster.getDepot();
					if (depot != null) {
						cluster.get_items_of_cluster().add(depot.get_id_depot());
					}
					solution.get_clusters().add(cluster);
				}
			}
			for (Customer customer : unnasigned_list_cure_clusters) {
				solution.get_unassigned_items().add(customer.get_id_customer());
			}

			OSRMService.clear_distance_cache();
			return solution;

		} catch (Exception e) {
			throw new AssignmentException("Error en finalización.", e);
		}
	}

	// Método para calcular distancia
	public static double distance(Location loc1, Location loc2) {
		double dx = loc1.get_axis_x() - loc2.get_axis_x();
		double dy = loc1.get_axis_y() - loc2.get_axis_y();
		return Math.sqrt(dx * dx + dy * dy);
	}


}