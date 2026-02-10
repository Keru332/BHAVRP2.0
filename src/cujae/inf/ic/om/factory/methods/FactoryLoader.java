package cujae.inf.ic.om.factory.methods;

import cujae.inf.ic.om.exceptions.FactoryCreationException;

/* Clase que construye una instancia de un objeto.*/
public class FactoryLoader {

	public static Object getInstance(String class_name) throws FactoryCreationException {
		@SuppressWarnings("rawtypes")
		Class c = null;
		
		try {
			c = Class.forName(class_name); 
		} catch (ClassNotFoundException e) {
			throw new FactoryCreationException("Clase no encontrada: " + class_name, e);
		}
		Object instance = null;
		
		try {
			instance = c.newInstance();
		} catch (InstantiationException e) {
			throw new FactoryCreationException("Error al instanciar la clase: " + class_name, e);
		} catch (IllegalAccessException e) {
			throw new FactoryCreationException("Constructor no accesible en la clase: " + class_name, e);
		}
		return instance;
	}
}