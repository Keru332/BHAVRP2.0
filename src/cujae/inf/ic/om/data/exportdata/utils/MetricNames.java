package cujae.inf.ic.om.data.exportdata.utils;

import cujae.inf.ic.om.factory.interfaces.EMetricType;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase utilitaria que asocia cada tipo de métrica con su abreviatura estándar.
 * Se utiliza durante el proceso de exportación para mostrar los nombres de métricas
 * de forma homogénea y legible en los archivos de salida.
 */
public class MetricNames {
	
    /**
     * Mapa que relaciona cada tipo de métrica (EMetricType) con su abreviatura textual.
     * Las claves corresponden a los tipos definidos en el enumerado EMetricType,
     * y los valores a sus representaciones abreviadas como cadenas de texto.
     */
    public static final Map<EMetricType, String> ABBREVIATIONS = new HashMap<>();

    // Inicialización estática del mapa de abreviaturas
    static {
        ABBREVIATIONS.put(EMetricType.SSE, "SSE");
        ABBREVIATIONS.put(EMetricType.SSB, "SSB");
        ABBREVIATIONS.put(EMetricType.DunnIndex, "D");
        ABBREVIATIONS.put(EMetricType.SilhouetteCoefficient, "CS");
        ABBREVIATIONS.put(EMetricType.CalinskiHarabaszIndex, "CH");
        ABBREVIATIONS.put(EMetricType.XieBeniIndex, "XB");
        ABBREVIATIONS.put(EMetricType.DaviesBouldinIndex, "DB");
        ABBREVIATIONS.put(EMetricType.RayTuriIndex, "RT");
        ABBREVIATIONS.put(EMetricType.BallHallIndex, "BH");
    }
    
    // Constructor privado para evitar instanciación
    private MetricNames() {
        throw new UnsupportedOperationException("Esta clase utilitaria no debe ser instanciada.");
    }
}