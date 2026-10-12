package cujae.inf.ic.om.assignment.clustering.density;



import cujae.inf.ic.om.assignment.clustering.density.utils.FibHeap;
import cujae.inf.ic.om.assignment.clustering.density.utils.Pair;
import cujae.inf.ic.om.assignment.clustering.density.utils.Tuple3;
import cujae.inf.ic.om.assignment.clustering.density.utils.UnionFind;
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

import java.util.*;

import static java.lang.Math.max;

/**
 * Adaptacion a MDVRP del algoritmo HDBSCAN de la libreria publica JSAT
 */

public class HDBSCAN extends AbstractDensity
{
    /**
     * minimium number of points
     */
    private int m_pts;
    private int m_clSize;
    private Solution solution = new Solution();
    private ArrayList<Cluster> list_clusters;
    private ArrayList<Depot> list_depots;
    private ArrayList<Customer> list_customers;

    private Set<Customer> assignedCustomers;

    /**
     * Creates a new HDBSCAN object using a threshold of 15 points to form a
     * cluster.
     */
    public HDBSCAN()
    {
        super();
    }



    public void setMinClusterSize(int m_clSize)
    {
        this.m_clSize = m_clSize;
    }

    /**
     *
     * @return the minimum number of data points needed to form a
     * cluster
     */
    public int getMinClusterSize()
    {
        return m_clSize;
    }
    /**
     *
     * @param m_pts the number of neighbors to consider, acts as a smoothing
     * over the density estimate
     */
    public void setMinPoints(int m_pts)
    {
        this.m_pts = m_pts;
    }

    /**
     *
     * @return the number of neighbors to consider, acts as a smoothing
     * over the density estimate
     */
    public int getMinPoints()
    {
        return m_pts;
    }


    public void assign() throws AssignmentException, ClusterException {
        @SuppressWarnings("unchecked")
        final ArrayList<Customer> X = list_customers;
        final int N = X.size();

        //Calcular distancia de núcleo
        double[] core = new double[N];
        for (int i = 0; i < N; i++) {
            List<Double> dists = new ArrayList<>(N - 1);
            for (int j = 0; j < N; j++) {
                if (i == j) continue;
                dists.add(distance(X.get(i).get_location_customer(), X.get(j).get_location_customer()));
            }
            Collections.sort(dists);
            core[i] = dists.get(m_pts - 1);  // distancia al (m_pts-1)-ésimo vecino
        }

        //prims algorithm
        double[] C = new double[N];
        Arrays.fill(C, Double.MAX_VALUE);
        int[] E = new int[N];
        Arrays.fill(E, -1);//-1 "a special flag value indicating that there is no edge connecting v to earlier vertices"

        FibHeap<Integer> Q = new FibHeap<>();
        List<FibHeap.FibNode<Integer>> q_nodes = new ArrayList<>(N);
        for(int i = 0; i < N; i++)
            q_nodes.add(Q.insert(i, C[i]));
        Set<Integer> F = new HashSet<>();

        /**
         * First 2 indicate the edges, 3d value is the weight
         */
        List<Tuple3<Integer, Integer, Double>> mst_edges = new ArrayList<>(N*2);

        while(Q.size() > 0)
        {
            //a. Find and remove a vertex v from Q having the minimum possible value of C[v]
            FibHeap.FibNode<Integer> node = Q.removeMin();
            int v = node.getValue();
            q_nodes.set(v, null);
            //b. Add v to F and, if E[v] is not the special flag value, also add E[v] to F
            F.add(v);

            if(E[v] >= 0)
                mst_edges.add(new Tuple3<>(v, E[v], C[v]));

            /*
             * c. Loop over the edges vw connecting v to other vertices w. For
             * each such edge, if w still belongs to Q and vw has smaller weight
             * than C[w]:
             *    Set C[w] to the cost of edge vw
             *    Set E[w] to point to edge vw.
             */

            for(int w = 0; w < N; w++)
            {
                FibHeap.FibNode<Integer> w_node = q_nodes.get(w);
                if (w_node == null)//this node is already in F
                    continue;

                double mutual_reach_dist_vw = max(core[v], max(core[w], distance(X.get(v).get_location_customer(), X.get(w).get_location_customer())));
                if (mutual_reach_dist_vw < C[w])
                {
                    Q.decreaseKey(w_node, mutual_reach_dist_vw);
                    C[w] = mutual_reach_dist_vw;
                    E[w] = v;
                }

            }

        }

        //prim is done, we have the MST!

        /*
         * 3. Extend the MST to obtain MSText, by adding for each vertex a “self
         * edge” with the core distance of the corresponding object as weight
         */

        for(int i = 0; i < N; i++)
            mst_edges.add(new Tuple3<>(i, i, core[i]));

        //4. Extract the HDBSCAN hierarchy as a dendrogram from MSText:

        List<UnionFind<Integer>> ufs = new ArrayList<>(N);
        for(int i = 0; i < N; i++)
            ufs.add(new UnionFind<>(i));
        //sort edges from smallest weight to largest
        PriorityQueue<Tuple3<Integer, Integer, Double>> edgeQ = new PriorityQueue<>(2*N,
                (o1, o2) -> o1.getZ().compareTo(o2.getZ())
        );
        edgeQ.addAll(mst_edges);

        //everyone starts in their own cluster!
        List<List<Integer>> currentGroups = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            List<Integer> list = new ArrayList<>();
            list.add(i);
            currentGroups.add(list);
        }

        int next_cluster_label = 0;
        /**
         * List of all the cluster options we have found
         */
        List<List<Integer>> cluster_options = new ArrayList<>();
        /**
         * Stores a list for each cluster. Each value in the sub list is the
         * weight at which that data point was added to the cluster
         */
        List<List<Double>> entry_size = new ArrayList<>();
        List<Double> birthSize = new ArrayList<>();
        List<Double> deathSize = new ArrayList<>();
        List<Pair<Integer, Integer>> children = new ArrayList<>();
        int[] map_to_cluster_label = new int[N];
        Arrays.fill(map_to_cluster_label, -1);

        while(!edgeQ.isEmpty())
        {
            Tuple3<Integer, Integer, Double> edge = edgeQ.poll();
            double weight = edge.getZ();
            int from = edge.getX();
            int to = edge.getY();

            if(to == from)
                continue;

            UnionFind<Integer> union_from  = ufs.get(from);
            UnionFind<Integer> union_to  = ufs.get(to);

            int clust_A = union_from.find().getItem();
            int clust_B = union_to.find().getItem();

            UnionFind<Integer> clust_A_tmp = union_from.find();
            UnionFind<Integer> clust_B_tmp = union_to.find();

            union_from.union(union_to);

            int a_size = currentGroups.get(clust_A).size();
            int b_size = currentGroups.get(clust_B).size();
            int new_size = a_size+b_size;

            int mergedClust;
            int otherClust;
            if(union_from.find().getItem() == clust_A)
            {
                mergedClust = clust_A;
                otherClust = clust_B;
            }
            else//other way around
            {
                mergedClust = clust_B;
                otherClust = clust_A;
            }


            if(new_size >= m_clSize && a_size < m_clSize && b_size < m_clSize)
            {//birth of a new cluster!
                cluster_options.add(currentGroups.get(mergedClust));

                List<Double> dl = new ArrayList<>(new_size);
                for(int i = 0; i < new_size; i++)
                    dl.add(weight);
                entry_size.add(dl);

                children.add(null);//we have not children!
                birthSize.add(weight);
                deathSize.add(Double.MAX_VALUE);//we don't know yet
                map_to_cluster_label[mergedClust] = next_cluster_label;
                next_cluster_label++;
            }
            else if(new_size >= m_clSize && a_size >= m_clSize && b_size >= m_clSize)
            {//birth of a new cluster from the death of two others!
                //record the weight that the other two died at
                deathSize.set(map_to_cluster_label[mergedClust], weight);
                deathSize.set(map_to_cluster_label[otherClust], weight);

                //replace with new object so that old references in cluster_options are not altered further
                currentGroups.set(mergedClust, new ArrayList<>(currentGroups.get(mergedClust)));

                cluster_options.add(currentGroups.get(mergedClust));
                List<Double> dl = new ArrayList<>(new_size);
                for(int i = 0; i < new_size; i++)
                    dl.add(weight);
                entry_size.add(dl);

                children.add(new Pair<>(map_to_cluster_label[mergedClust], map_to_cluster_label[otherClust]));
                birthSize.add(weight);
                deathSize.add(Double.MAX_VALUE);//we don't know yet
                map_to_cluster_label[mergedClust] = next_cluster_label;
                next_cluster_label++;
            }
            else if(new_size >= m_clSize)
            {//existing cluster has grown in size, so add the points and record their weight for use later
                //index may change, so book keeping update
                if(map_to_cluster_label[mergedClust] == -1)//the people being added are the new owners
                {
                    //set to avoid index out of boudns bellow
                    int c = map_to_cluster_label[mergedClust] = map_to_cluster_label[otherClust];
                    //make sure we keep track of the correct list
                    cluster_options.set(c, currentGroups.get(mergedClust));
                    map_to_cluster_label[otherClust] = -1;
                }


                for(int indx : currentGroups.get(otherClust))
                    try
                    {
                        entry_size.get(map_to_cluster_label[mergedClust]).add(weight);
                    }
                    catch(IndexOutOfBoundsException ex)
                    {
                        ex.printStackTrace();
                        throw new AssignmentException(ex.getMessage());
                    }

            }

            currentGroups.get(mergedClust).addAll(currentGroups.get(otherClust));
            currentGroups.set(otherClust, null);

        }

        //Remove the last "cluster" because its the dumb one of everything
        cluster_options.remove(cluster_options.size()-1);
        entry_size.remove(entry_size.size()-1);
        birthSize.remove(birthSize.size()-1);
        deathSize.remove(deathSize.size()-1);
        children.remove(children.size()-1);

        /**
         * See equation (3) in paper
         */
        double[] S = new double[cluster_options.size()];
        for(int c = 0; c < S.length; c++)
        {
            double lambda_min = birthSize.get(c);
            double lambda_max = deathSize.get(c);
            double s = 0;
            for(double f_x : entry_size.get(c))
                s += Math.min(f_x, lambda_max) - lambda_min;
            S[c] = s;
        }

        boolean[] toKeep = new boolean[S.length];
        double[] S_hat = new double[cluster_options.size()];
        Arrays.fill(toKeep, true);
        Queue<Integer> notKeeping = new ArrayDeque<>();

        for(int i = 0; i < S.length; i++)
        {
            Pair<Integer, Integer> child = children.get(i);
            if(child == null)//I'm a leaf!
            {
                //for all leaf nodes, set ˆS(C_h)= S(C_h)
                S_hat[i] = S[i];
                continue;
            }
            int il = child.getFirstItem();
            int ir = child.getSecondItem();
            //If S(C_i) < ˆS(C_il)+ ˆ S(C_ir ), set ˆS(C_i)= ˆS(C_il)+ ˆS(C_ir )and set δi =0.
            if(S[i] < S_hat[il] + S_hat[ir])
            {
                S_hat[i] = S_hat[il] + S_hat[ir];
                toKeep[i] = false;
            }
            else//Else: set ˆS(C_i)= S(C_i)and set δ(·) = 0 for all clusters in C_i’s subtrees.
            {
                S_hat[i] = S[i];
                //place children in q to process and set all sub children as not keeping
                notKeeping.add(il);
                notKeeping.add(ir);
                while(!notKeeping.isEmpty())
                {
                    int c = notKeeping.poll();
                    toKeep[c] = false;
                    Pair<Integer, Integer> c_children = children.get(c);
                    if(c_children == null)
                        continue;
                    notKeeping.add(c_children.getFirstItem());
                    notKeeping.add(c_children.getSecondItem());
                }
            }
        }

        int[] labels = new int[N];
        Arrays.fill(labels, -1);  // por defecto ruido

        int clusterId = 0;
        for (int c = 0; c < toKeep.length; c++) {
            if (toKeep[c]) {
                for (int idx : cluster_options.get(c)) {
                    labels[idx] = clusterId;
                }
                clusterId++;
            }
        }

        assignedCustomers = new HashSet();
        ArrayList<Cluster> tempListCluster = new ArrayList<>();

        int numCluster = Arrays.stream(labels).max().getAsInt() + 1;
        while (tempListCluster.size() < numCluster){
            tempListCluster.add(new Cluster());
        }

        for (int c = 0; c < labels.length; c++){
            if(labels[c] != -1){
                tempListCluster.get(labels[c]).get_items_of_cluster().add(list_customers.get(c).get_id_customer());
                tempListCluster.get(labels[c]).set_request_cluster(list_customers.get(c).get_request_customer() + tempListCluster.get(labels[c]).get_request_cluster());
                assignedCustomers.add(list_customers.get(c));
            }

        }

        for (Cluster cl : tempListCluster){
            double cx = 0, cy = 0;
            ArrayList<Customer> tempCustomer = new ArrayList<>();

            for (int c = 0; c < cl.get_items_of_cluster().size(); c++) {
                Customer customer = list_customers.get(cl.get_items_of_cluster().get(c) - 1);
                tempCustomer.add(customer);
                cx += customer.get_location_customer().get_axis_x();
                cy += customer.get_location_customer().get_axis_y();
            }
            cx /= cl.get_items_of_cluster().size();
            cy /= cl.get_items_of_cluster().size();

            Depot nearestDepot = null;
            double minDist = Double.MAX_VALUE;
            for (Depot depot : list_depots) {
                double d = distance(
                        new Location(cx, cy),
                        depot.get_location_depot()
                );
                if (d < minDist) {
                    minDist = d;
                    nearestDepot = depot;
                }
            }
            int depotIndex = list_depots.indexOf(nearestDepot);
            Cluster cluster = list_clusters.get(depotIndex);

            if((cl.get_request_cluster() + cluster.get_request_cluster()) <= Problem.get_problem().get_total_capacity_by_depot(list_depots.get(depotIndex))){
                cluster.get_items_of_cluster().addAll(cl.get_items_of_cluster());
                cluster.set_request_cluster(cl.get_request_cluster() + cluster.get_request_cluster());

                System.out.println("este cluster tiene request :" + cluster.get_request_cluster());
            }
            else
                assignedCustomers.removeAll(tempCustomer);
        }

    }

    @Override
    public Solution to_clustering() throws ClusterException, CostMatrixException, ProblemException, AssignmentException {
        try {
            initialize();
            assign();
            return finish();
        } catch (ClusterException | ProblemException e) {
            throw e;
        } catch (Exception e) {
            throw new AssignmentException("Error inesperado en HDBSCAN.");
        }
    }

    public void initialize() throws AssignmentException, ProblemException, ClusterException {
        try {
            ArrayList<Integer> list_id_elements = Problem.get_problem().get_list_id_depots();
            if (list_id_elements == null || list_id_elements.isEmpty())
                throw new AssignmentException("No se encontraron elementos iniciales para los cl�steres.");

            list_clusters = new ArrayList<>();
            for(Integer id : list_id_elements){
                Cluster cluster = new Cluster(id, 0.0, new ArrayList<>());
                list_clusters.add(cluster);
            }
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

            setMinPoints((int) Math.max(3, Math.log(list_customers.size()*1.5)));
            setMinClusterSize(2);

        } catch (ClusterException | ProblemException e) {
            throw e;
        } catch (Exception e) {
            throw new AssignmentException("Error inesperado durante la fase de inicializaci�n.", e);
        }
    }


    public Solution finish() throws AssignmentException {
        ArrayList<Integer> unnasigned_items = new ArrayList<>();
        System.out.println("ETOOOOOOOOOOOOOOO");
        try {
            for (Customer customer : list_customers) {
                if (!assignedCustomers.contains(customer)) {
                    boolean asigned = false;
                    Queue<Integer> nearestDepot = findNearestDepotIndice(customer);
                    while (!nearestDepot.isEmpty() && !asigned) {
                        int indice = nearestDepot.poll();
                        Cluster cluster = list_clusters.get(indice);

                        double maxDepotCapacity = Problem.get_problem()
                                .get_total_capacity_by_depot(list_depots.get(indice));
                        double RequestNecesario = cluster.get_request_cluster() + customer.get_request_customer();

                        if (RequestNecesario > maxDepotCapacity) {
                            continue;
                        }
                        assignCustomerToCluster(customer, cluster, assignedCustomers);
                        asigned = true;
                    }
                    if (!asigned)
                        unnasigned_items.add(customer.get_id_customer());
                }
            }
        }catch (ClusterException e){
            throw new AssignmentException("Error al agregar en lista de no asignados.", e);
        }
        solution.get_clusters().clear();

        solution.get_clusters().addAll(list_clusters);

        for (Cluster c : list_clusters){
            if(c.get_items_of_cluster().isEmpty())
                solution.get_clusters().remove(c);
        }
        solution.get_unassigned_items().addAll(unnasigned_items);
        OSRMService.clear_distance_cache();
        return solution;
    }

    private Queue<Integer> findNearestDepotIndice(Customer customer) {
        // PriorityQueue que ordena por distancia
        Queue<Integer> indexDepotNearest = new PriorityQueue<>(
                (i1, i2) -> {
                    Depot d1 = list_depots.get(i1);
                    Depot d2 = list_depots.get(i2);
                    double dist1 = distance(d1.get_location_depot(), customer.get_location_customer());
                    double dist2 = distance(d2.get_location_depot(), customer.get_location_customer());
                    return Double.compare(dist1, dist2);
                }
        );
        for (int i = 0; i < list_depots.size(); i++) {
            indexDepotNearest.add(i);
        }

        return indexDepotNearest;
    }
}
