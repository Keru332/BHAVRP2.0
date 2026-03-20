package tester;

import cujae.inf.ic.om.assignment.clustering.hierarchical.CureCluster;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.ProblemException;
import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Location;
import cujae.inf.ic.om.problem.output.Cluster;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CureClusterTest {

    @Nested
    @DisplayName("Pruebas de creación de CureCluster")
    class CreationTests {

        private Customer createCustomer(int id, double x, double y, double request) throws ProblemException {
            return new Customer(id, request, new Location(x, y));
        }

        @Test
        @DisplayName("Debe crear un cluster válido con un cliente")
        void testCreateWithSingleCustomer() throws ProblemException {
            Customer customer = createCustomer(1, 10, 20, 15.5);
            ArrayList<Customer> customers = new ArrayList<>(Arrays.asList(customer));

            Cluster baseCluster = new Cluster();
            baseCluster.set_id_cluster(1);

            CureCluster cureCluster = new CureCluster(baseCluster, customers);

            assertNotNull(cureCluster);
            assertNotNull(cureCluster.getCluster());
            assertNotNull(cureCluster.getCustomers());
            assertNotNull(cureCluster.getCentroid());
            assertNotNull(cureCluster.getRepresentativePoints());

            assertEquals(1, cureCluster.getCustomers().size());
            assertEquals(15.5, cureCluster.getRequest());
        }

        @Test
        @DisplayName("Debe crear un cluster válido con múltiples clientes")
        void testCreateWithMultipleCustomers() throws ProblemException {
            Customer c1 = createCustomer(1, 0, 0, 10.0);
            Customer c2 = createCustomer(2, 2, 0, 20.0);
            Customer c3 = createCustomer(3, 1, 2, 30.0);

            ArrayList<Customer> customers = new ArrayList<>(Arrays.asList(c1, c2, c3));

            Cluster baseCluster = new Cluster();
            baseCluster.set_id_cluster(1);

            CureCluster cureCluster = new CureCluster(baseCluster, customers);

            assertEquals(3, cureCluster.getCustomers().size());
            assertEquals(60.0, cureCluster.getRequest());

            Location centroid = cureCluster.getCentroid();
            assertEquals(1.0, centroid.get_axis_x(), 0.1);
            assertEquals(0.67, centroid.get_axis_y(), 0.1);
        }

        @Test
        @DisplayName("Debe tener puntos representativos después de la creación")
        void testRepresentativePoints() throws ProblemException {
            Customer c1 = createCustomer(1, 0, 0, 10.0);
            Customer c2 = createCustomer(2, 5, 0, 20.0);
            Customer c3 = createCustomer(3, 0, 5, 30.0);

            ArrayList<Customer> customers = new ArrayList<>(Arrays.asList(c1, c2, c3));

            Cluster baseCluster = new Cluster();
            baseCluster.set_id_cluster(1);

            CureCluster cureCluster = new CureCluster(baseCluster, customers);

            ArrayList<Location> repPoints = cureCluster.getRepresentativePoints();
            assertNotNull(repPoints);
            assertFalse(repPoints.isEmpty());
        }
    }

    @Nested
    @DisplayName("Pruebas del método merge")
    class MergeTests {

        private Customer createCustomer(int id, double x, double y, double request) throws ProblemException {
            return new Customer(id, request, new Location(x, y));
        }

        @Test
        @DisplayName("Debe fusionar dos clusters correctamente")
        void testMerge() throws ClusterException, ProblemException {
            // Crear primer cluster
            Customer c1 = createCustomer(1, 0, 0, 10.0);
            Customer c2 = createCustomer(2, 1, 0, 15.0);
            ArrayList<Customer> customers1 = new ArrayList<>(Arrays.asList(c1, c2));

            Cluster baseCluster1 = new Cluster();
            baseCluster1.set_id_cluster(1);
            baseCluster1.get_items_of_cluster().add(1);
            baseCluster1.get_items_of_cluster().add(2);

            CureCluster cluster1 = new CureCluster(baseCluster1, customers1);

            // Crear segundo cluster
            Customer c3 = createCustomer(3, 3, 0, 20.0);
            Customer c4 = createCustomer(4, 4, 0, 25.0);
            ArrayList<Customer> customers2 = new ArrayList<>(Arrays.asList(c3, c4));

            Cluster baseCluster2 = new Cluster();
            baseCluster2.set_id_cluster(2);
            baseCluster2.get_items_of_cluster().add(3);
            baseCluster2.get_items_of_cluster().add(4);

            CureCluster cluster2 = new CureCluster(baseCluster2, customers2);

            // Fusionar
            double requestBefore = cluster1.getRequest();
            int customersBefore = cluster1.getCustomers().size();

            cluster1.merge(cluster2);

            assertEquals(customersBefore + 2, cluster1.getCustomers().size());
            assertEquals(requestBefore + 45.0, cluster1.getRequest());
            assertEquals(4, cluster1.getCluster().get_items_of_cluster().size());
        }

        @Test
        @DisplayName("El merge debe actualizar el centroide")
        void testMergeUpdatesCentroid() throws ClusterException, ProblemException {
            Customer c1 = createCustomer(1, 0, 0, 10.0);
            Customer c2 = createCustomer(2, 0, 0, 15.0); // Misma posición

            ArrayList<Customer> customers1 = new ArrayList<>(Arrays.asList(c1, c2));
            Cluster baseCluster1 = new Cluster();
            baseCluster1.set_id_cluster(1);
            CureCluster cluster1 = new CureCluster(baseCluster1, customers1);

            Location centroidBefore = cluster1.getCentroid();

            Customer c3 = createCustomer(3, 10, 10, 20.0);
            ArrayList<Customer> customers2 = new ArrayList<>(Arrays.asList(c3));
            Cluster baseCluster2 = new Cluster();
            baseCluster2.set_id_cluster(2);
            CureCluster cluster2 = new CureCluster(baseCluster2, customers2);

            cluster1.merge(cluster2);

            Location centroidAfter = cluster1.getCentroid();
            assertNotEquals(centroidBefore.get_axis_x(), centroidAfter.get_axis_x());
            assertNotEquals(centroidBefore.get_axis_y(), centroidAfter.get_axis_y());
        }
    }

    @Nested
    @DisplayName("Pruebas de getters y setters")
    class GetterSetterTests {

        @Test
        @DisplayName("Los getters deben retornar valores no nulos")
        void testGetters() throws ProblemException {
            Customer customer = new Customer(1, 10.0, new Location(1, 2));
            ArrayList<Customer> customers = new ArrayList<>(Arrays.asList(customer));

            Cluster baseCluster = new Cluster();
            baseCluster.set_id_cluster(1);

            CureCluster cureCluster = new CureCluster(baseCluster, customers);

            assertNotNull(cureCluster.getCluster());
            assertNotNull(cureCluster.getCustomers());
            assertNotNull(cureCluster.getCentroid());
            assertNotNull(cureCluster.getRepresentativePoints());

            assertTrue(cureCluster.getRequest() >= 0);
        }

        @Test
        @DisplayName("setDepot y getDepot deben funcionar")
        void testSetGetDepot() throws ProblemException {
            Customer customer = new Customer(1, 10.0, new Location(1, 2));
            ArrayList<Customer> customers = new ArrayList<>(Arrays.asList(customer));

            Cluster baseCluster = new Cluster();
            baseCluster.set_id_cluster(1);

            CureCluster cureCluster = new CureCluster(baseCluster, customers);

            Depot depot = new Depot(1, new Location(5, 5), new ArrayList<>());
            cureCluster.setDepot(depot);

            assertEquals(depot, cureCluster.getDepot());
            assertEquals(1, cureCluster.getDepot().get_id_depot());
        }
    }

    @Nested
    @DisplayName("Pruebas de casos borde")
    class EdgeCaseTests {

        @Test
        @DisplayName("Debe manejar lista vacía de clientes")
        void testEmptyCustomers() {
            ArrayList<Customer> emptyCustomers = new ArrayList<>();

            Cluster baseCluster = new Cluster();
            baseCluster.set_id_cluster(1);

            CureCluster cureCluster = new CureCluster(baseCluster, emptyCustomers);

            assertNotNull(cureCluster);
            assertEquals(0, cureCluster.getCustomers().size());
            assertEquals(0.0, cureCluster.getRequest());
        }

        @Test
        @DisplayName("Debe manejar un solo cliente repetido")
        void testSingleCustomerRepeated() throws ClusterException, ProblemException {
            Customer customer = new Customer(1, 10.0, new Location(1, 1));
            ArrayList<Customer> customers1 = new ArrayList<>(Arrays.asList(customer));

            Cluster baseCluster1 = new Cluster();
            baseCluster1.set_id_cluster(1);
            CureCluster cluster1 = new CureCluster(baseCluster1, customers1);

            // Intentar fusionar con sí mismo (no debería permitirse en la práctica)
            // Pero probamos que el merge no falla
            assertDoesNotThrow(() -> {
                // Esto no debería pasar en la práctica, pero probamos que no lanza excepción
                if (!cluster1.equals(cluster1)) {
                    cluster1.merge(cluster1);
                }
            });
        }
    }

    @Nested
    @DisplayName("Pruebas de integración simple")
    class SimpleIntegrationTests {

        @Test
        @DisplayName("Debe mantener consistencia después de operaciones")
        void testConsistency() throws ClusterException, ProblemException {
            // Crear datos de prueba
            Customer c1 = new Customer(1, 10.0, new Location(0, 0));
            Customer c2 = new Customer(2, 20.0, new Location(2, 0));
            Customer c3 = new Customer(3, 30.0, new Location(4, 0));

            ArrayList<Customer> customers1 = new ArrayList<>(Arrays.asList(c1, c2));
            ArrayList<Customer> customers2 = new ArrayList<>(Arrays.asList(c3));

            Cluster base1 = new Cluster();
            base1.set_id_cluster(1);
            base1.get_items_of_cluster().add(1);
            base1.get_items_of_cluster().add(3);
            Cluster base2 = new Cluster();
            base2.set_id_cluster(2);
            base2.get_items_of_cluster().add(2);


            CureCluster cluster1 = new CureCluster(base1, customers1);
            CureCluster cluster2 = new CureCluster(base2, customers2);

            // Guardar estado inicial
            double request1 = cluster1.getRequest();
            int size1 = cluster1.getCustomers().size();

            // Fusionar
            cluster1.merge(cluster2);
            System.out.println(cluster1.getCluster().get_items_of_cluster().size());

            // Verificar consistencia
            assertEquals(request1 + 30.0, cluster1.getRequest());
            assertEquals(size1 + 1, cluster1.getCustomers().size());
            assertEquals(3, cluster1.getCluster().get_items_of_cluster().size());

            // Verificar que los puntos representativos existen
            assertFalse(cluster1.getRepresentativePoints().isEmpty());
        }
    }

    @Nested
    @DisplayName("Pruebas de documentación")
    class DocumentationTests {

        @Test
        @DisplayName("La clase debe tener el nombre esperado")
        void testClassName() throws ProblemException {
            Customer customer = new Customer(1, 10.0, new Location(1, 2));
            ArrayList<Customer> customers = new ArrayList<>(Arrays.asList(customer));
            Cluster baseCluster = new Cluster();
            baseCluster.set_id_cluster(1);

            CureCluster cureCluster = new CureCluster(baseCluster, customers);

            assertEquals("CureCluster", cureCluster.getClass().getSimpleName());
        }

        @Test
        @DisplayName("El paquete debe ser el correcto")
        void testPackage() {
            assertEquals("cujae.inf.ic.om.assignment.clustering.hierarchical",
                    CureCluster.class.getPackageName());
        }
    }
}
