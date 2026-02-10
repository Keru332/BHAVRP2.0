package cujae.inf.ic.om.factory.interfaces;

import cujae.inf.ic.om.data.importdata.interfaces.IImporter;

import cujae.inf.ic.om.exceptions.ImportException;

/**
 * Interfaz que define el contrato para las fábricas de importadores de datos.
 * Permite obtener una instancia de {@link IImporter} a partir del formato del archivo.
 */
public interface IFactoryImporter {
	
    /**
     * Crea una instancia de {@link IImporter} en función del tipo de archivo proporcionado.
     *
     * @param path Ruta al archivo a importar.
     * @return Instancia de {@link IImporter} correspondiente al tipo de archivo.
     * @throws ImportException si el tipo de archivo no es soportado o hay un error de inicialización.
     */
    IImporter create_importer(String path) throws ImportException;
}
