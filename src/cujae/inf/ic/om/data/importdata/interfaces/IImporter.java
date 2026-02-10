package cujae.inf.ic.om.data.importdata.interfaces;

import cujae.inf.ic.om.data.importdata.utils.ProblemRawData;

import cujae.inf.ic.om.exceptions.ImportException;

/**
 * Interfaz general para cargar los datos del problema MDVRP desde distintos formatos.
 */
public interface IImporter {
	
	/**
	 * Carga todos los datos del problema desde un archivo en la ruta indicada.
	 *
	 * @param path Ruta del archivo a cargar.
	 * @return Objeto contenedor con todos los datos del problema.
	 * @throws ImportException Si ocurre un error durante la importación.
	 */
	ProblemRawData import_data(String path) throws ImportException;
}