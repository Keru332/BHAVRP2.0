package cujae.inf.ic.om.factory.methods;

import cujae.inf.ic.om.data.exportdata.formats.CSVExporter;
import cujae.inf.ic.om.data.exportdata.formats.XLSExporter;
import cujae.inf.ic.om.data.exportdata.formats.JSONExporter;
import cujae.inf.ic.om.data.exportdata.formats.TXTExporter;
import cujae.inf.ic.om.data.exportdata.formats.XMLExporter;

import cujae.inf.ic.om.data.exportdata.interfaces.IExporter;

import cujae.inf.ic.om.exceptions.ExportException;

import cujae.inf.ic.om.factory.interfaces.EFileFormatExporter;
import cujae.inf.ic.om.factory.interfaces.IFactoryExporter;

/**
 * Fábrica concreta para generar exportadores basados en el tipo de formato de salida.
 */
public class FactoryExporter implements IFactoryExporter {

    /**
     * Retorna una implementación de {@link IExporter} adecuada al formato de exportación dado.
     *
     * @param format Tipo de formato de exportación.
     * @return Instancia concreta de {@link IExporter}.
     * @throws ExportException Si el formato no es reconocido.
     */
    @Override
    public IExporter create_exporter(EFileFormatExporter format) throws ExportException {
        if (format == null)
            throw new ExportException("El formato de exportación no puede ser nulo.");

        switch (format) {
            case CSV:
                return new CSVExporter();
            case JSON:
                return new JSONExporter();
            case XML:
                return new XMLExporter();
            case TXT:
                return new TXTExporter();
            case XLS:
                return new XLSExporter();
            default:
                throw new ExportException("Formato de exportación no soportado: " + format.name());
        }
    }
}