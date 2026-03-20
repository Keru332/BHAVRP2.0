package tester;

import cujae.inf.ic.om.controller.Controller;
import cujae.inf.ic.om.data.exportdata.utils.MetricRecord;
import cujae.inf.ic.om.factory.interfaces.EAssignmentType;
import cujae.inf.ic.om.factory.interfaces.EMetricType;
import cujae.inf.ic.om.problem.output.Solution;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class CUREUnitTest {

    public static void main(String[] args) {

        String basePath = "instances/test/";
        String[] testInstances = {
                //"cure_case1.txt",
                //"cure_case2.txt",
                //"cure_case3.txt",
                //"cure_case4.txt"
        };

        List<EMetricType> metrics = Arrays.asList(
                EMetricType.SSE,
                EMetricType.DaviesBouldinIndex,
                EMetricType.SilhouetteCoefficient
        );

        Controller controller = Controller.get_controller();

        for (String instance : testInstances) {

            try {

                String path = basePath + instance;

                System.out.println("--------------------------------------------------");
                System.out.println("EJECUTANDO PRUEBA CURE");
                System.out.println("Instancia: " + new File(path).getName());

                controller.load_problem(path);

                long start = System.currentTimeMillis();

                Solution solution = controller.execute_assignment(EAssignmentType.CURE);

                long end = System.currentTimeMillis();

                double time = (end - start) / 1000.0;

                List<MetricRecord> results = controller.evaluate_solution(metrics);

                System.out.println("Tiempo ejecución: " + time + " s");

                System.out.println("Métricas:");

                for (MetricRecord m : results) {
                    System.out.println(m.get_name_enum() + " = " + m.get_value());
                }

                System.out.println("Resultado: ejecución correcta");

                controller.clean_controller();

            } catch (Exception e) {

                System.out.println("Error en prueba: " + instance);
                System.out.println(e.getMessage());

            }
        }

        System.out.println("--------------------------------------------------");
        System.out.println("FINALIZADAS PRUEBAS CURE");

    }
}
