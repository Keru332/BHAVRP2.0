package cujae.inf.ic.om.factory.interfaces;

/* Enumerado que indica los tipos de métricas.*/
public enum EMetricType {
	
	SSE 
	{
        @Override
        public String toString() {
            return cujae.inf.ic.om.evaluation.internalvalidation.metrics.cohesion.SumOfSquaredErrors.class.getName();
        }
    },

    SSB 
    {
        @Override
        public String toString() {
            return cujae.inf.ic.om.evaluation.internalvalidation.metrics.separation.SumOfSquaresBetween.class.getName();
        }
    },

    DunnIndex 
    {
        @Override
        public String toString() {
            return cujae.inf.ic.om.evaluation.internalvalidation.metrics.combined.DunnIndex.class.getName();
        }
    },
    
    SilhouetteCoefficient 
    {
        @Override
        public String toString() {
            return cujae.inf.ic.om.evaluation.internalvalidation.metrics.combined.SilhouetteCoefficient.class.getName();
        }
    },

    CalinskiHarabaszIndex 
    {
        @Override
        public String toString() {
            return cujae.inf.ic.om.evaluation.internalvalidation.metrics.combined.CalinskiHarabaszIndex.class.getName();
        }
    },

    XieBeniIndex 
    {
        @Override
        public String toString() {
            return cujae.inf.ic.om.evaluation.internalvalidation.metrics.combined.XieBeniIndex.class.getName();
        }
    },

    BallHallIndex 
    {
        @Override
        public String toString() {
            return cujae.inf.ic.om.evaluation.internalvalidation.metrics.cohesion.BallHallIndex.class.getName();
        }
    },
    
    DaviesBouldinIndex 
    {
        @Override
        public String toString() {
            return cujae.inf.ic.om.evaluation.internalvalidation.metrics.combined.DaviesBouldinIndex.class.getName();
        }
    },

    RayTuriIndex 
    {
        @Override
        public String toString() {
            return cujae.inf.ic.om.evaluation.internalvalidation.metrics.combined.RayTuriIndex.class.getName();
        }
    }
}