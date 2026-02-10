package cujae.inf.ic.om.factory.interfaces;

/* Enumerado que indica los tipos de métodos de asignación*/
public enum EAssignmentType {

	BestCyclicAssignment	
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.others.basedcyclic.BestCyclicAssignment.class.getName(); 
		}
	}, 

	BestNearest
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.others.distance.BestNearest.class.getName(); 
		}
	}, 

	CLARA
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.clustering.partitional.Clara.class.getName(); 
		}
	}, 
	
	CoefficientPropagation
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.classical.cluster.CoefficientPropagation.class.getName(); 
		}
	}, 

	CyclicAssignment
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.classical.cyclic.CyclicAssignment.class.getName(); 
		}
	}, 

	Farthest_First
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.clustering.partitional.Farthest_First.class.getName(); 
		}
	},
	
	KMEANS
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.clustering.partitional.Kmeans.class.getName(); 
		}
	}, 
	
	NearestByCustomer
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.others.distance.NearestByCustomer.class.getName(); 
		}
	}, 
	
	NearestByDepot
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.others.distance.NearestByDepot.class.getName(); 
		}
	}, 
	
	PAM
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.clustering.partitional.PAM.class.getName(); 
		}
	}, 
	
	Parallel
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.classical.urgency.Parallel.class.getName(); 
		}
	}, 
	
	RandomByElement
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.others.distance.RandomByElement.class.getName(); 
		}
	}, 

	RandomSequentialCyclic
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.others.basedcyclic.RandomSequentialCyclic.class.getName(); 
		}
	}, 
	
	SequentialCyclic
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.others.basedcyclic.SequentialCyclic.class.getName(); 
		}
	}, 

	Simplified
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.classical.urgency.Simplified.class.getName(); 
		}
	}, 
	
	Sweep
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.classical.urgency.Sweep.class.getName(); 
		}
	}, 
	
	ThreeCriteriaClustering
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.classical.cluster.ThreeCriteriaClustering.class.getName(); 
		}
	}, 

	UPGMC
	{
		@Override
		public String toString()
		{
			return cujae.inf.ic.om.assignment.clustering.hierarchical.UPGMC.class.getName(); 
		}
	};
}