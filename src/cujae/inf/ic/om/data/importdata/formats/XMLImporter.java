package cujae.inf.ic.om.data.importdata.formats;

import cujae.inf.ic.om.data.importdata.interfaces.IImporter;

import cujae.inf.ic.om.data.importdata.utils.ProblemRawData;

import cujae.inf.ic.om.exceptions.ImportException;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Clase responsable de importar los datos del problema MDVRP desde un archivo XML
 * y convertirlos en una instancia de {@link ProblemRawData}.
 * Implementa la interfaz {@link IImporter}.
 */
public class XMLImporter implements IImporter {
	
    public XMLImporter() {
    	super();
    }
    
    /**
     * Importa los datos desde un archivo XML y los encapsula en un objeto {@link ProblemRawData}.
     *
     * @param path Ruta del archivo XML a importar.
     * @return Objeto {@link ProblemRawData} con los datos cargados.
     * @throws ImportException si ocurre un error de lectura, formato o estructura de datos.
     */
	@Override
    public ProblemRawData import_data(String path) throws ImportException {
        try {
            if (path == null || path.trim().isEmpty())
                throw new ImportException("La ruta del archivo XML está vacía o no es válida.");

            File file = new File(path);
            if (!file.exists())
                throw new ImportException("El archivo especificado no existe: " + path);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            ProblemRawData data = new ProblemRawData();
            
            Element root = doc.getDocumentElement();
            if (root.getElementsByTagName("total_vehicles").getLength() == 0)
            	throw new ImportException("Faltan elementos obligatorios: Total de vehículos.");
            
            if (root.getElementsByTagName("total_depots").getLength() == 0) 
                throw new ImportException("Faltan elementos obligatorios: Total de depósitos.");

            int total_vehicles = Integer.parseInt(doc.getElementsByTagName("total_vehicles").item(0).getTextContent());
            int total_depots = Integer.parseInt(doc.getElementsByTagName("total_depots").item(0).getTextContent());
            data.count_vehicles.clear();

            ArrayList<Integer> fleet = new ArrayList<>();
            fleet.add(total_vehicles);
            for (int i = 0; i < total_depots; i++)
                data.count_vehicles.add(new ArrayList<>(fleet));
            
            if (total_vehicles <= 0)
                throw new ImportException("El valor de 'total_vehicles' no es válido (debe ser mayor que cero).");

            if (total_depots <= 0)
                throw new ImportException("El valor de 'total_depots' no es válido (debe ser mayor que cero).");

            NodeList capacity_list = doc.getElementsByTagName("capacity");
            if (capacity_list.getLength() != total_depots)
                throw new ImportException("El número de capacidades no coincide con el número de depósitos.");

            for (int i = 0; i < capacity_list.getLength(); i++) 
            {
                ArrayList<Double> cap = new ArrayList<>();
                cap.add(Double.parseDouble(capacity_list.item(i).getTextContent()));
                data.capacity_vehicles.add(cap);
            }
            if (capacity_list == null || capacity_list.getLength() == 0)
                throw new ImportException("Falta la sección <capacity> en el archivo XML.");

            NodeList customers = doc.getElementsByTagName("customer");
            if (customers.getLength() == 0)
                throw new ImportException("No se encontraron elementos de clientes en el archivo XML.");

            for (int i = 0; i < customers.getLength(); i++) 
            {
                Element c = (Element) customers.item(i);
                data.id_customers.add(Integer.parseInt(c.getElementsByTagName("id").item(0).getTextContent()));
                data.axis_x_customers.add(Double.parseDouble(c.getElementsByTagName("x").item(0).getTextContent()));
                data.axis_y_customers.add(Double.parseDouble(c.getElementsByTagName("y").item(0).getTextContent()));
                data.request_customers.add(Double.parseDouble(c.getElementsByTagName("request").item(0).getTextContent()));
            }
            if (customers == null || customers.getLength() == 0)
                throw new ImportException("Falta la sección <customer> en el archivo XML.");

            NodeList depots = doc.getElementsByTagName("depot");
            if (depots.getLength() != total_depots)
                throw new ImportException("La cantidad de depósitos no coincide con el número declarado.");

            for (int i = 0; i < depots.getLength(); i++) 
            {
                Element d = (Element) depots.item(i);
                data.id_depots.add(Integer.parseInt(d.getElementsByTagName("id").item(0).getTextContent()));
                data.axis_x_depots.add(Double.parseDouble(d.getElementsByTagName("x").item(0).getTextContent()));
                data.axis_y_depots.add(Double.parseDouble(d.getElementsByTagName("y").item(0).getTextContent()));
            }
            if (depots == null || depots.getLength() == 0)
                throw new ImportException("Falta la sección <depot> en el archivo XML.");
            
            return data;
        } catch (ImportException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportException("Error al importar archivo XML: " + path, e);
        }
    }
}