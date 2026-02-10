package cujae.inf.ic.om.factory.methods;

import cujae.inf.ic.om.factory.interfaces.IFactoryMetric;
import cujae.inf.ic.om.factory.interfaces.EMetricType;

import cujae.inf.ic.om.evaluation.internalvalidation.metrics.AbstractMetric;

import cujae.inf.ic.om.exceptions.FactoryCreationException;

/* Clase que implementa el Patrón Factory para la carga dinámica de un determinado método de métrica.*/
public class FactoryMetric implements IFactoryMetric {

	@Override
	public AbstractMetric create_metric(EMetricType metric_type) throws FactoryCreationException {
		try {
			return (AbstractMetric) Class.forName(metric_type.toString()).newInstance();
            
        } catch (ClassNotFoundException e) {
        	throw new FactoryCreationException("Error: La clase de la métrica no existe en el classpath.", e);
        
        } catch (InstantiationException | IllegalAccessException e) {
        	throw new FactoryCreationException("Error: No se pudo instanciar la métrica: " + metric_type.name(), e);
        }		
	}
}