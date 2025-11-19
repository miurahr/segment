package net.loomchild.segment.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;

import org.xml.sax.InputSource;

/**
 * Helper class for JAXB binding.
 * Responsible for marshalling and unmarshalling using given schema and context.
 * @author loomchild
 */
public class Bind {

	private final Marshaller marshaller;

	private final Unmarshaller unmarshaller;

	/**
	 * Creates Bind.
	 * @param context JAXB context
	 * @param schema XML schema
	 */
	public Bind(JAXBContext context, Schema schema) {
		try {
			unmarshaller = context.createUnmarshaller();
			unmarshaller.setEventHandler(new LoggingValidationEventHandler());
			unmarshaller.setSchema(schema);
			marshaller = context.createMarshaller();
			marshaller.setSchema(schema);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		} catch (JAXBException e) {
			throw new XmlException("JAXB error", e);
		}
	}

	/**
	 * Writes given object to given writer validating it.
	 * @param writer
	 * @param object
	 */
	public void marshal(Writer writer, Object object) {
		try {
			marshaller.marshal(object, writer);
		} catch (JAXBException e) {
			throw new XmlException("JAXB marshalling error", e);
		}
	}

	/**
	 * Writes given object to a file with given name validating it.
	 * @param fileName
	 * @param object
	 */
	public void marshal(String fileName, Object object) {
		try {
			Writer writer = Util.getWriter(Util.getFileOutputStream(fileName));
			marshal(writer, object);
			writer.close();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * Retrieves object from given reader validation the input.
	 * @param reader
	 * @return object
	 */
	public Object unmarshal(Reader reader) {
		try {
			Source source = new SAXSource(Util.getXmlReader(), new InputSource(
					reader));
			return unmarshaller.unmarshal(source);
		} catch (JAXBException e) {
			throw new XmlException("JAXB unmarshalling error", e);
		}
	}

	/**
	 * Retrieves object from a file with given name validating the input.
	 * @param fileName
	 * @return object
	 */
	public Object unmarshal(String fileName) {
		try {
			Reader reader = Util.getReader(Util.getFileInputStream(fileName));
			Object object = unmarshal(reader);
			reader.close();
			return object;
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

}
