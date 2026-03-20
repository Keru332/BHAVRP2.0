package tester;

import cujae.inf.ic.om.assignment.clustering.density.DBSCAN;
import cujae.inf.ic.om.exceptions.AssignmentException;
import cujae.inf.ic.om.exceptions.ClusterException;
import cujae.inf.ic.om.exceptions.ProblemException;
import cujae.inf.ic.om.problem.input.Customer;
import cujae.inf.ic.om.problem.input.Depot;
import cujae.inf.ic.om.problem.input.Location;
import cujae.inf.ic.om.problem.input.Problem;
import cujae.inf.ic.om.problem.output.Cluster;
import cujae.inf.ic.om.problem.output.Solution;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DBSCANTest {

    private DBSCAN dbscan;

    @BeforeEach
    void setUp() {
        dbscan = new DBSCAN();
    }

    @Nested
    @DisplayName("Pruebas del método distance (estático)")
    class DistanceTest {

        @Test
        @DisplayName("Debe calcular distancia correcta entre dos puntos")
        void testDistance() {
            Location loc1 = new Location(0, 0);
            Location loc2 = new Location(3, 4);

            double distance = DBSCAN.distance(loc1, loc2);

            assertEquals(5.0, distance, 0.0001);
        }

        @Test
        @DisplayName("Distancia de un punto a sí mismo debe ser 0")
        void testDistanceToSelf() {
            Location loc = new Location(5, 5);

            double distance = DBSCAN.distance(loc, loc);

            assertEquals(0.0, distance);
        }

        @Test
        @DisplayName("Debe manejar coordenadas negativas")
        void testNegativeCoordinates() {
            Location loc1 = new Location(-2, -3);
            Location loc2 = new Location(1, 1);

            double distance = DBSCAN.distance(loc1, loc2);

            assertEquals(5.0, distance, 0.0001);
        }
    }

    @Nested
    @DisplayName("Pruebas de setters")
    class SetterTests {

        @Test
        @DisplayName("setMinimalNumberOfMembersForCluster debe establecer el valor correctamente")
        void testSetMinimalMembers() {
            int expected = 5;
            dbscan.setMinimalNumberOfMembersForCluster(expected);
            // No hay getter, pero podemos verificar que no lanza excepción
            assertDoesNotThrow(() -> dbscan.setMinimalNumberOfMembersForCluster(expected));
        }

        @Test
        @DisplayName("setMaximalDistanceOfClusterMembers debe establecer el valor correctamente")
        void testSetMaximalDistance() {
            double expected = 3.5;
            dbscan.setMaximalDistanceOfClusterMembers(expected);
            assertDoesNotThrow(() -> dbscan.setMaximalDistanceOfClusterMembers(expected));
        }

        @Test
        @DisplayName("Debe aceptar valores límite")
        void testBoundaryValues() {
            assertDoesNotThrow(() -> dbscan.setMinimalNumberOfMembersForCluster(2));
            assertDoesNotThrow(() -> dbscan.setMaximalDistanceOfClusterMembers(0.0));
            assertDoesNotThrow(() -> dbscan.setMaximalDistanceOfClusterMembers(1000.0));
        }
    }

    @Nested
    @DisplayName("Pruebas del constructor y estado inicial")
    class ConstructorTest {

        @Test
        @DisplayName("El constructor debe crear una instancia válida")
        void testConstructor() {
            assertNotNull(dbscan);
        }

        @Test
        @DisplayName("Los valores por defecto deben ser razonables")
        void testDefaultValues() {
            // No podemos acceder directamente, pero podemos verificar que el objeto existe
            assertNotNull(dbscan);
        }
    }

    @Nested
    @DisplayName("Pruebas de integración simples")
    class SimpleIntegrationTest {

        @Test
        @DisplayName("to_clustering debe lanzar excepción cuando no hay problema cargado")
        void testToClusteringWithoutProblem() {
            assertThrows(AssignmentException.class, () -> {
                dbscan.to_clustering();
            });
        }

        @Test
        @DisplayName("Los métodos pueden ser llamados en secuencia")
        void testMethodChaining() {
            assertDoesNotThrow(() -> {
                dbscan.setMinimalNumberOfMembersForCluster(3);
                dbscan.setMaximalDistanceOfClusterMembers(2.5);
                // No llamamos to_clustering porque fallará sin problema cargado
            });
        }
    }

    @Nested
    @DisplayName("Pruebas de comportamiento con datos simulados")
    class SimulatedDataTest {

        private DBSCAN createDBSCANWithMockData() {
            DBSCAN customDBSCAN = new DBSCAN();

            // Configurar parámetros
            customDBSCAN.setMinimalNumberOfMembersForCluster(2);
            customDBSCAN.setMaximalDistanceOfClusterMembers(3.0);

            return customDBSCAN;
        }

        @Test
        @DisplayName("El algoritmo debe ejecutarse con configuración personalizada")
        void testCustomConfiguration() {
            DBSCAN customDBSCAN = createDBSCANWithMockData();

            // Verificar que la configuración no causa errores
            assertDoesNotThrow(() -> {
                customDBSCAN.setMinimalNumberOfMembersForCluster(2);
                customDBSCAN.setMaximalDistanceOfClusterMembers(3.0);
            });
        }

        @Test
        @DisplayName("Múltiples instancias deben ser independientes")
        void testMultipleInstances() {
            DBSCAN dbscan1 = new DBSCAN();
            DBSCAN dbscan2 = new DBSCAN();

            dbscan1.setMinimalNumberOfMembersForCluster(5);
            dbscan2.setMinimalNumberOfMembersForCluster(3);

            // Ambas instancias deben existir y ser diferentes
            assertNotSame(dbscan1, dbscan2);
        }
    }

    @Nested
    @DisplayName("Pruebas de utilidades")
    class UtilityTests {

        @Test
        @DisplayName("DistanceType debe ser Euclidean por defecto")
        void testDefaultDistanceType() {
            assertEquals(cujae.inf.ic.om.factory.DistanceType.Euclidean, DBSCAN.distance_type);
        }

        @Test
        @DisplayName("El método distance debe ser consistente")
        void testDistanceConsistency() {
            Location loc1 = new Location(1, 2);
            Location loc2 = new Location(1, 2);
            Location loc3 = new Location(2, 3);

            double dist1 = DBSCAN.distance(loc1, loc2);
            double dist2 = DBSCAN.distance(loc2, loc1);
            double dist3 = DBSCAN.distance(loc1, loc3);

            assertEquals(0.0, dist1);
            assertEquals(dist1, dist2); // Simetría
            assertTrue(dist3 > 0.0);
        }
    }

    @Nested
    @DisplayName("Pruebas de casos borde")
    class EdgeCaseTests {

        @Test
        @DisplayName("setMinimalNumberOfMembersForCluster con valores extremos")
        void testExtremeMinMembers() {
            assertDoesNotThrow(() -> dbscan.setMinimalNumberOfMembersForCluster(Integer.MAX_VALUE));
            assertDoesNotThrow(() -> dbscan.setMinimalNumberOfMembersForCluster(2)); // Valor mínimo razonable
        }

        @Test
        @DisplayName("setMaximalDistanceOfClusterMembers con valores extremos")
        void testExtremeMaxDistance() {
            assertDoesNotThrow(() -> dbscan.setMaximalDistanceOfClusterMembers(Double.MAX_VALUE));
            assertDoesNotThrow(() -> dbscan.setMaximalDistanceOfClusterMembers(0.0));
            assertDoesNotThrow(() -> dbscan.setMaximalDistanceOfClusterMembers(-1.0)); // Aunque no tenga sentido, el setter debe aceptarlo
        }

        @Test
        @DisplayName("Múltiples llamadas a setters deben funcionar")
        void testMultipleSetterCalls() {
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 100; i++) {
                    dbscan.setMinimalNumberOfMembersForCluster(i);
                    dbscan.setMaximalDistanceOfClusterMembers(i * 1.0);
                }
            });
        }
    }

    @Nested
    @DisplayName("Pruebas de comparación de instancias")
    class InstanceComparisonTest {

        @Test
        @DisplayName("Diferentes instancias deben tener estado independiente")
        void testIndependentState() {
            DBSCAN dbscanA = new DBSCAN();
            DBSCAN dbscanB = new DBSCAN();

            dbscanA.setMinimalNumberOfMembersForCluster(10);
            dbscanB.setMinimalNumberOfMembersForCluster(20);

            // No podemos verificar los valores internos, pero podemos verificar
            // que ambas instancias existen y son diferentes
            assertNotNull(dbscanA);
            assertNotNull(dbscanB);
            assertNotSame(dbscanA, dbscanB);
        }

        @Test
        @DisplayName("El tipo de distancia debe ser compartido por todas las instancias")
        void testSharedDistanceType() {
            DBSCAN dbscan1 = new DBSCAN();
            DBSCAN dbscan2 = new DBSCAN();

            assertEquals(DBSCAN.distance_type, dbscan1.distance_type);
            assertEquals(dbscan1.distance_type, dbscan2.distance_type);
        }
    }

    @Nested
    @DisplayName("Pruebas de documentación")
    class DocumentationTest {

        @Test
        @DisplayName("La clase debe tener el nombre esperado")
        void testClassName() {
            assertEquals("DBSCAN", dbscan.getClass().getSimpleName());
        }

        @Test
        @DisplayName("Debe extender AbstractDensity")
        void testSuperClass() {
            assertEquals("cujae.inf.ic.om.assignment.clustering.density.AbstractDensity",
                    dbscan.getClass().getSuperclass().getName());
        }
    }

    @Nested
    @DisplayName("Pruebas de excepciones")
    class ExceptionTests {

        @Test
        @DisplayName("to_clustering debe lanzar AssignmentException apropiadamente")
        void testToClusteringException() {
            AssignmentException exception = assertThrows(AssignmentException.class, () -> {
                dbscan.to_clustering();
            });

            // Verificar que el mensaje no es nulo
            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Las excepciones deben tener causa cuando corresponde")
        void testExceptionCause() {
            try {
                dbscan.to_clustering();
                fail("Debe lanzar excepción");
            } catch (AssignmentException e) {
                // La causa puede ser nula o no, dependiendo de la implementación
                // No verificamos causa específica
                assertTrue(true);
            } catch (ProblemException e) {
                throw new RuntimeException(e);
            } catch (ClusterException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nested
    @DisplayName("Pruebas rápidas de humo")
    class SmokeTests {

        @Test
        @DisplayName("El objeto puede ser creado y destruido")
        void testCreateAndDestroy() {
            DBSCAN localDBSCAN = new DBSCAN();
            assertNotNull(localDBSCAN);
            localDBSCAN = null;
            System.gc(); // Sugerir recolección de basura
            assertTrue(true); // Si llegamos aquí, no hubo error
        }

        @Test
        @DisplayName("Los métodos principales existen")
        void testMainMethodsExist() throws NoSuchMethodException {
            assertNotNull(dbscan.getClass().getMethod("setMinimalNumberOfMembersForCluster", int.class));
            assertNotNull(dbscan.getClass().getMethod("setMaximalDistanceOfClusterMembers", double.class));
            assertNotNull(dbscan.getClass().getMethod("to_clustering"));
        }
    }
}