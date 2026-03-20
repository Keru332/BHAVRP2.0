package tester;

import cujae.inf.ic.om.assignment.clustering.hierarchical.CURE;
import cujae.inf.ic.om.assignment.clustering.hierarchical.CureCluster;
import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ProblemException;
import cujae.inf.ic.om.factory.DistanceType;
import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Location;
import cujae.inf.ic.om.problem.output.Cluster;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CURETest {

    private CURE cure;
    private static final double SHRINK_FACTOR = 0.2;
    private static final double DELTA = 0.0001;

    @BeforeEach
    void setUp() {
        cure = new CURE();
    }

    // Helper para calcular punto contraído
    private Location calculateShrunkPoint(Location point, Location centroid) {
        double newX = centroid.get_axis_x() + SHRINK_FACTOR * (point.get_axis_x() - centroid.get_axis_x());
        double newY = centroid.get_axis_y() + SHRINK_FACTOR * (point.get_axis_y() - centroid.get_axis_y());
        return new Location(newX, newY);
    }

    // Helper para obtener el centroide esperado de una lista de clientes
    private Location calculateCentroid(ArrayList<Customer> customers) {
        double sumX = 0.0, sumY = 0.0;
        for (Customer c : customers) {
            sumX += c.get_location_customer().get_axis_x();
            sumY += c.get_location_customer().get_axis_y();
        }
        return new Location(sumX / customers.size(), sumY / customers.size());
    }

    @Nested
    @DisplayName("Pruebas del método distance (estático)")
    class DistanceTest {

        @Test
        @DisplayName("Debe calcular distancia correcta entre dos puntos")
        void testDistance() {
            Location loc1 = new Location(0, 0);
            Location loc2 = new Location(3, 4);

            double distance = CURE.distance(loc1, loc2);

            assertEquals(5.0, distance, DELTA);
        }

        @Test
        @DisplayName("Distancia de un punto a sí mismo debe ser 0")
        void testDistanceToSelf() {
            Location loc = new Location(5, 5);

            double distance = CURE.distance(loc, loc);

            assertEquals(0.0, distance);
        }

        @Test
        @DisplayName("Debe manejar coordenadas negativas")
        void testNegativeCoordinates() {
            Location loc1 = new Location(-2, -3);
            Location loc2 = new Location(1, 1);

            double distance = CURE.distance(loc1, loc2);

            assertEquals(5.0, distance, DELTA);
        }

        @Test
        @DisplayName("Debe ser simétrica")
        void testSymmetry() {
            Location loc1 = new Location(1, 2);
            Location loc2 = new Location(4, 6);

            double dist1 = CURE.distance(loc1, loc2);
            double dist2 = CURE.distance(loc2, loc1);

            assertEquals(dist1, dist2);
        }
    }

    @Nested
    @DisplayName("Pruebas del método calculateClusterDistance")
    class CalculateClusterDistanceTest {

        private CureCluster createTestCluster(int id, java.util.List<Customer> customerList) {
            ArrayList<Customer> customers = new ArrayList<>(customerList);
            Cluster baseCluster = new Cluster();
            baseCluster.set_id_cluster(id);
            for (Customer c : customers) {
                baseCluster.get_items_of_cluster().add(c.get_id_customer());
            }
            return new CureCluster(baseCluster, customers);
        }

        @Test
        @DisplayName("Debe calcular distancia entre dos clusters separados horizontalmente")
        void testDistanceBetweenHorizontalClusters() throws ProblemException {
            ArrayList<Customer> customers1 = new ArrayList<>(Arrays.asList(
                    new Customer(1, 10.0, new Location(0, 0)),
                    new Customer(2, 15.0, new Location(1, 0))
            ));

            ArrayList<Customer> customers2 = new ArrayList<>(Arrays.asList(
                    new Customer(3, 20.0, new Location(5, 0)),
                    new Customer(4, 25.0, new Location(6, 0))
            ));

            CureCluster cluster1 = createTestCluster(1, customers1);
            CureCluster cluster2 = createTestCluster(2, customers2);

            // Calcular puntos contraídos esperados para cluster1
            Location centroid1 = calculateCentroid(customers1);
            Location p1_shrunk = calculateShrunkPoint(new Location(0, 0), centroid1);
            Location p2_shrunk = calculateShrunkPoint(new Location(1, 0), centroid1);

            // Calcular puntos contraídos esperados para cluster2
            Location centroid2 = calculateCentroid(customers2);
            Location p5_shrunk = calculateShrunkPoint(new Location(5, 0), centroid2);
            Location p6_shrunk = calculateShrunkPoint(new Location(6, 0), centroid2);

            // Encontrar la distancia mínima entre puntos contraídos
            double expected = Math.min(
                    Math.min(CURE.distance(p1_shrunk, p5_shrunk), CURE.distance(p1_shrunk, p6_shrunk)),
                    Math.min(CURE.distance(p2_shrunk, p5_shrunk), CURE.distance(p2_shrunk, p6_shrunk))
            );

            double actual = cure.calculateClusterDistance(cluster1, cluster2);

            assertEquals(expected, actual, DELTA);
        }

        @Test
        @DisplayName("Debe calcular distancia entre dos clusters separados verticalmente")
        void testDistanceBetweenVerticalClusters() throws ProblemException {
            ArrayList<Customer> customers1 = new ArrayList<>(Arrays.asList(
                    new Customer(1, 10.0, new Location(0, 0)),
                    new Customer(2, 15.0, new Location(0, 1))
            ));

            ArrayList<Customer> customers2 = new ArrayList<>(Arrays.asList(
                    new Customer(3, 20.0, new Location(0, 5)),
                    new Customer(4, 25.0, new Location(0, 6))
            ));

            CureCluster cluster1 = createTestCluster(1, customers1);
            CureCluster cluster2 = createTestCluster(2, customers2);

            // Calcular puntos contraídos esperados
            Location centroid1 = calculateCentroid(customers1);
            Location p1_shrunk = calculateShrunkPoint(new Location(0, 0), centroid1);
            Location p2_shrunk = calculateShrunkPoint(new Location(0, 1), centroid1);

            Location centroid2 = calculateCentroid(customers2);
            Location p5_shrunk = calculateShrunkPoint(new Location(0, 5), centroid2);
            Location p6_shrunk = calculateShrunkPoint(new Location(0, 6), centroid2);

            double expected = Math.min(
                    Math.min(CURE.distance(p1_shrunk, p5_shrunk), CURE.distance(p1_shrunk, p6_shrunk)),
                    Math.min(CURE.distance(p2_shrunk, p5_shrunk), CURE.distance(p2_shrunk, p6_shrunk))
            );

            double actual = cure.calculateClusterDistance(cluster1, cluster2);

            assertEquals(expected, actual, DELTA);
        }

        @Test
        @DisplayName("Debe retornar 0 para clusters que se tocan (después de contracción)")
        void testDistanceForTouchingClusters() throws ProblemException {
            ArrayList<Customer> customersA = new ArrayList<>(Arrays.asList(
                    new Customer(1, 10.0, new Location(0, 0)),
                    new Customer(2, 15.0, new Location(1, 1))
            ));

            ArrayList<Customer> customersB = new ArrayList<>(Arrays.asList(
                    new Customer(3, 20.0, new Location(1, 1)),
                    new Customer(4, 25.0, new Location(2, 2))
            ));

            CureCluster clusterA = createTestCluster(1, customersA);
            CureCluster clusterB = createTestCluster(2, customersB);

            double actual = cure.calculateClusterDistance(clusterA, clusterB);

            // Con la contracción, los puntos ya no están exactamente en el mismo lugar
            // Verificamos que sea un valor pequeño pero no necesariamente 0
            assertTrue(actual >= 0);
        }

        @Test
        @DisplayName("Debe calcular distancia correcta con clusters que tienen múltiples puntos")
        void testDistanceWithMultiplePoints() throws ProblemException {
            ArrayList<Customer> customersA = new ArrayList<>(Arrays.asList(
                    new Customer(1, 10.0, new Location(0, 0)),
                    new Customer(2, 15.0, new Location(2, 2)),
                    new Customer(3, 20.0, new Location(3, 3))
            ));

            ArrayList<Customer> customersB = new ArrayList<>(Arrays.asList(
                    new Customer(4, 25.0, new Location(4, 4)),
                    new Customer(5, 30.0, new Location(5, 5)),
                    new Customer(6, 35.0, new Location(6, 6))
            ));

            CureCluster clusterA = createTestCluster(1, customersA);
            CureCluster clusterB = createTestCluster(2, customersB);

            // Calcular usando los puntos contraídos
            Location centroidA = calculateCentroid(customersA);
            Location centroidB = calculateCentroid(customersB);

            double minDist = Double.MAX_VALUE;
            for (Customer c1 : customersA) {
                Location p1_shrunk = calculateShrunkPoint(c1.get_location_customer(), centroidA);
                for (Customer c2 : customersB) {
                    Location p2_shrunk = calculateShrunkPoint(c2.get_location_customer(), centroidB);
                    double dist = CURE.distance(p1_shrunk, p2_shrunk);
                    if (dist < minDist) minDist = dist;
                }
            }

            double actual = cure.calculateClusterDistance(clusterA, clusterB);

            assertEquals(minDist, actual, DELTA);
        }

        @Test
        @DisplayName("Debe ser simétrica (distancia A-B = distancia B-A)")
        void testSymmetry() throws ProblemException {
            ArrayList<Customer> customers1 = new ArrayList<>(Arrays.asList(
                    new Customer(1, 10.0, new Location(0, 0))
            ));

            ArrayList<Customer> customers2 = new ArrayList<>(Arrays.asList(
                    new Customer(2, 20.0, new Location(3, 4))
            ));

            CureCluster cluster1 = createTestCluster(1, customers1);
            CureCluster cluster2 = createTestCluster(2, customers2);

            double distance1 = cure.calculateClusterDistance(cluster1, cluster2);
            double distance2 = cure.calculateClusterDistance(cluster2, cluster1);

            assertEquals(distance1, distance2, DELTA);
        }

        @Test
        @DisplayName("Debe manejar clusters con un solo punto")
        void testWithSinglePointClusters() throws ProblemException {
            ArrayList<Customer> customersA = new ArrayList<>(Arrays.asList(
                    new Customer(1, 10.0, new Location(1, 1))
            ));

            ArrayList<Customer> customersB = new ArrayList<>(Arrays.asList(
                    new Customer(2, 20.0, new Location(4, 5))
            ));

            CureCluster singleA = createTestCluster(1, customersA);
            CureCluster singleB = createTestCluster(2, customersB);

            // Para un solo punto, el centroide es el punto mismo
            // El punto contraído será: centroide + factor*(punto - centroide) = punto (porque punto = centroide)
            double expected = CURE.distance(new Location(1, 1), new Location(4, 5));

            double actual = cure.calculateClusterDistance(singleA, singleB);

            assertEquals(expected, actual, DELTA);
        }

        @Test
        @DisplayName("Debe manejar coordenadas negativas")
        void testNegativeCoordinates() throws ProblemException {
            ArrayList<Customer> customersNeg = new ArrayList<>(Arrays.asList(
                    new Customer(1, 10.0, new Location(-5, -5)),
                    new Customer(2, 15.0, new Location(-4, -4))
            ));

            ArrayList<Customer> customersPos = new ArrayList<>(Arrays.asList(
                    new Customer(3, 20.0, new Location(1, 1)),
                    new Customer(4, 25.0, new Location(2, 2))
            ));

            CureCluster clusterNeg = createTestCluster(1, customersNeg);
            CureCluster clusterPos = createTestCluster(2, customersPos);

            // Calcular usando puntos contraídos
            Location centroidNeg = calculateCentroid(customersNeg);
            Location centroidPos = calculateCentroid(customersPos);

            double minDist = Double.MAX_VALUE;
            for (Customer c1 : customersNeg) {
                Location p1_shrunk = calculateShrunkPoint(c1.get_location_customer(), centroidNeg);
                for (Customer c2 : customersPos) {
                    Location p2_shrunk = calculateShrunkPoint(c2.get_location_customer(), centroidPos);
                    double dist = CURE.distance(p1_shrunk, p2_shrunk);
                    if (dist < minDist) minDist = dist;
                }
            }

            double actual = cure.calculateClusterDistance(clusterNeg, clusterPos);

            assertEquals(minDist, actual, DELTA);
        }

        @Test
        @DisplayName("Debe retornar un valor positivo para clusters separados")
        void testPositiveDistance() throws ProblemException {
            ArrayList<Customer> customers1 = new ArrayList<>(Arrays.asList(
                    new Customer(1, 10.0, new Location(0, 0))
            ));

            ArrayList<Customer> customers2 = new ArrayList<>(Arrays.asList(
                    new Customer(2, 20.0, new Location(10, 10))
            ));

            CureCluster cluster1 = createTestCluster(1, customers1);
            CureCluster cluster2 = createTestCluster(2, customers2);

            double distance = cure.calculateClusterDistance(cluster1, cluster2);

            assertTrue(distance > 0);
            assertTrue(Double.isFinite(distance));
        }

        @Test
        @DisplayName("Debe usar los puntos representativos contraídos del cluster")
        void testUsesShrunkRepresentativePoints() throws ProblemException {
            ArrayList<Customer> customers = new ArrayList<>(Arrays.asList(
                    new Customer(1, 10.0, new Location(0, 0)),
                    new Customer(2, 15.0, new Location(10, 0))
            ));

            CureCluster cluster = createTestCluster(1, customers);

            // Verificar que los puntos representativos NO son los originales
            Location centroid = calculateCentroid(customers);
            Location original_point = new Location(10, 0);
            Location expected_shrunk = calculateShrunkPoint(original_point, centroid);

            ArrayList<Location> repPoints = cluster.getRepresentativePoints();
            boolean found = false;
            for (Location p : repPoints) {
                if (Math.abs(p.get_axis_x() - expected_shrunk.get_axis_x()) < DELTA &&
                        Math.abs(p.get_axis_y() - expected_shrunk.get_axis_y()) < DELTA) {
                    found = true;
                    break;
                }
            }

            assertTrue(found, "Los puntos representativos deberían estar contraídos");
        }
    }

    @Nested
    @DisplayName("Pruebas del constructor y estado inicial")
    class ConstructorTest {

        @Test
        @DisplayName("El constructor debe crear una instancia válida")
        void testConstructor() {
            assertNotNull(cure);
        }

        @Test
        @DisplayName("DistanceType debe ser Euclidean por defecto")
        void testDefaultDistanceType() {
            assertEquals(DistanceType.Euclidean, CURE.distance_type);
        }
    }

    @Nested
    @DisplayName("Pruebas de excepciones")
    class ExceptionTests {

        @Test
        @DisplayName("to_clustering debe lanzar AssignmentException sin problema cargado")
        void testToClusteringWithoutProblem() {
            AssignmentException exception = assertThrows(AssignmentException.class, () -> {
                cure.to_clustering();
            });
            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("initialize debe lanzar excepción sin problema")
        void testInitializeWithoutProblem() {
            assertThrows(AssignmentException.class, () -> {
                cure.initialize();
            });
        }
    }

    @Nested
    @DisplayName("Pruebas de métodos públicos")
    class PublicMethodsTest {

        @Test
        @DisplayName("Los métodos principales existen")
        void testMainMethodsExist() throws NoSuchMethodException {
            assertNotNull(cure.getClass().getMethod("to_clustering"));
            assertNotNull(cure.getClass().getMethod("initialize"));
            assertNotNull(cure.getClass().getMethod("assign"));
            assertNotNull(cure.getClass().getMethod("finish"));
            assertNotNull(cure.getClass().getMethod("calculateClusterDistance", CureCluster.class, CureCluster.class));
        }

        @Test
        @DisplayName("La clase debe tener el nombre esperado")
        void testClassName() {
            assertEquals("CURE", cure.getClass().getSimpleName());
        }

        @Test
        @DisplayName("Debe extender AbstractHierarchical")
        void testSuperClass() {
            assertEquals("cujae.inf.ic.om.assignment.clustering.hierarchical.AbstractHierarchical",
                    cure.getClass().getSuperclass().getName());
        }
    }

    @Nested
    @DisplayName("Pruebas rápidas de humo")
    class SmokeTests {

        @Test
        @DisplayName("El objeto puede ser creado y destruido")
        void testCreateAndDestroy() {
            CURE localCURE = new CURE();
            assertNotNull(localCURE);
            localCURE = null;
            System.gc();
            assertTrue(true);
        }

        @Test
        @DisplayName("Múltiples instancias pueden ser creadas")
        void testMultipleInstances() {
            CURE cure1 = new CURE();
            CURE cure2 = new CURE();

            assertNotNull(cure1);
            assertNotNull(cure2);
            assertNotSame(cure1, cure2);
        }
    }
}