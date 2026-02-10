package cujae.inf.ic.om.factory.interfaces;

public enum EFileFormatExporter {
    CSV("csv"),
    JSON("json"),
    XML("xml"),
    TXT("txt"),
    XLS("xls");
    
    private final String extension;

    EFileFormatExporter(String extension) {
        this.extension = extension;
    }
    
    public String get_extension() {
        return extension;
    }
    
    /**
     * Obtiene el formato de exportación a partir de una extensión de archivo.
     * 
     * @param filename Nombre del archivo o extensión.
     * @return El tipo de formato correspondiente.
     * @throws IllegalArgumentException si no coincide con ningún formato soportado.
     */
    public static EFileFormatExporter from_extension(String filename) {
        if (filename == null || filename.trim().isEmpty())
            throw new IllegalArgumentException("El nombre del archivo es nulo o vacío.");

        String ext = filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase()
                : filename.toLowerCase();

        for (EFileFormatExporter format : EFileFormatExporter.values()) {
            if (format.get_extension().equals(ext)) {
                return format;
            }
        }

        throw new IllegalArgumentException("Formato de exportación no soportado: ." + ext);
    }
}
