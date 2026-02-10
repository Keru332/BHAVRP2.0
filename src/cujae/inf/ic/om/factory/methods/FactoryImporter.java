package cujae.inf.ic.om.factory.methods;

import cujae.inf.ic.om.data.importdata.interfaces.IImporter;

import cujae.inf.ic.om.data.importdata.formats.CSVImporter;
import cujae.inf.ic.om.data.importdata.formats.DATImporter;
import cujae.inf.ic.om.data.importdata.formats.JSONImporter;
import cujae.inf.ic.om.data.importdata.formats.XMLImporter;

import cujae.inf.ic.om.exceptions.ImportException;

import cujae.inf.ic.om.factory.interfaces.EFileFormatImporter;
import cujae.inf.ic.om.factory.interfaces.IFactoryImporter;

/**
 * Fábrica concreta para generar importadores basados en la extensión del archivo.
 */
public class FactoryImporter implements IFactoryImporter {
	
	/**
     * Retorna una implementación de {@link IImporter} adecuada al formato de archivo dado.
     *
     * @param file_path Ruta del archivo a importar.
     * @return Instancia concreta de {@link IImporter}.
     * @throws ImportException Si el formato no es reconocido o el archivo es inválido.
     */
    @Override
    public IImporter create_importer(String path) throws ImportException {
        if (path == null || path.trim().isEmpty())
            throw new ImportException("La ruta del archivo no puede ser nula o vacía.");

        String extension = get_file_extension(path);

        try {
            EFileFormatImporter format = EFileFormatImporter.from_extension(extension);
            switch (format) {
                case DAT:
                	return new DATImporter();
                case TXT:
                    return new DATImporter();
                case CSV:
                    return new CSVImporter();
                case JSON:
                    return new JSONImporter();
                case XML:
                    return new XMLImporter();
                default:
                    throw new ImportException("Formato de archivo no soportado: " + extension);
            }
        } catch (IllegalArgumentException e) {
            throw new ImportException("Extensión de archivo no reconocida: " + extension, e);
        }
    }

    private String get_file_extension(String path) {
        int index = path.lastIndexOf('.');
        if (index == -1 || index == path.length() - 1)
            return "dat";  // Se asume DAT si no hay extensión
        return path.substring(index + 1).toLowerCase();
    }
}