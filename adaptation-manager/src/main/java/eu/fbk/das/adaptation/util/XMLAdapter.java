package eu.fbk.das.adaptation.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.FactoryConfigurationError;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Marshal from filesystem a file and map to an object
 * 
 * @author admin
 *
 */
public class XMLAdapter {

    private static final Logger logger = LogManager.getLogger(XMLAdapter.class);

    public static String marshal(Object obj) {
	JAXBContext jc;
	String xml = "";

	try {
	    jc = JAXBContext.newInstance(obj.getClass());
	    Marshaller m = jc.createMarshaller();
	    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
	    OutputStream output = new ByteArrayOutputStream();
	    m.marshal(obj, output);
	    xml = output.toString();
	} catch (JAXBException e) {
	    logger.error(e.getMessage(), e);
	} catch (FactoryConfigurationError e) {
	    logger.error(e.getMessage(), e);
	}
	return xml;
    }
}
