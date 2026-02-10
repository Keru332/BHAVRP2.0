package cujae.inf.ic.om.factory.interfaces;

import cujae.inf.ic.om.data.exportdata.interfaces.IExporter;

import cujae.inf.ic.om.exceptions.ExportException;

/**
 * Interfaz que define el contrato para las fábricas de exportadores de datos.
 * Permite obtener una instancia de {@link IExporter} a partir del formato solicitado.
 */
public interface IFactoryExporter {

    /**
     * Crea una instancia concreta de {@link IExporter} en función del formato de exportación indicado.
     *
     * @param format Tipo de formato deseado (CSV, JSON, XML, TXT, EXCEL).
     * @return Instancia de {@link IExporter} adecuada para el formato.
     * @throws ExportException Si el formato no es soportado.
     */
    IExporter create_exporter(EFileFormatExporter format) throws ExportException;
}
