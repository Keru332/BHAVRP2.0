package cujae.inf.ic.om.factory.methods;

import cujae.inf.ic.om.assignment.AbstractAssignment;

import cujae.inf.ic.om.exceptions.FactoryCreationException;

import cujae.inf.ic.om.factory.interfaces.EAssignmentType;
import cujae.inf.ic.om.factory.interfaces.IFactoryAssignment;

/* Clase que implementa el Patrón Factory para la carga dinámica de un determinado método de asignación.*/
public class FactoryAssignment implements IFactoryAssignment {

	@Override
	public AbstractAssignment create_assignment(EAssignmentType assignment_type) throws FactoryCreationException {
		try {
			return (AbstractAssignment) AbstractAssignment.class.getClassLoader().loadClass(assignment_type.toString()).newInstance();

		} catch (ClassNotFoundException e) {
			throw new FactoryCreationException("No se encontró la clase para el tipo de asignación: " + assignment_type, e);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new FactoryCreationException("Error al instanciar la heurística de asignación: " + assignment_type, e);
		}
	}
}