package cujae.inf.ic.om.factory.interfaces;

import cujae.inf.ic.om.assignment.AbstractAssignment;
import cujae.inf.ic.om.exceptions.FactoryCreationException;

/* Interfaz que define cómo crear un objeto Assignment.*/
public interface IFactoryAssignment {
	public AbstractAssignment create_assignment(EAssignmentType assignmentType) throws FactoryCreationException;
}
