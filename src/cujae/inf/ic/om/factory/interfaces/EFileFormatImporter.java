package cujae.inf.ic.om.factory.interfaces;

import cujae.inf.ic.om.exceptions.ImportException;

public enum EFileFormatImporter {
    DAT, TXT, CSV, JSON, XML;

    /**
     * Determina el formato de archivo a partir de su extensión.
     *
     * @param filename Nombre del archivo (con extensión).
     * @return El formato correspondiente como enum.
     * @throws ImportException si el archivo no tiene una extensión válida o no está soportada.
     */
    public static EFileFormatImporter from_extension(String filename) throws ImportException {
        if (filename == null || filename.trim().isEmpty())
            throw new ImportException("El nombre del archivo es nulo o vacío.");

        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        switch (extension) {
            case "dat":
                return DAT;
            case "txt":
                return TXT;
            case "csv":
                return CSV;
            case "json":
                return JSON;
            case "xml":
                return XML;
            default:
                throw new ImportException("Formato de archivo no soportado: ." + extension);
        }
    }
}

