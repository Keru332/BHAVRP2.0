package cujae.inf.ic.om.factory.interfaces;

import cujae.inf.ic.om.evaluation.internalvalidation.metrics.AbstractMetric;

import cujae.inf.ic.om.exceptions.FactoryCreationException;

/* Interfaz que define cómo crear un objeto Metric.*/
public interface IFactoryMetric {
	AbstractMetric create_metric(EMetricType metric_type) throws FactoryCreationException;
}