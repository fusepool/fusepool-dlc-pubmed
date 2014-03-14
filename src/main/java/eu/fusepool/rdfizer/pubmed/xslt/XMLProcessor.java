package eu.fusepool.rdfizer.pubmed.xslt;

import java.io.InputStream;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXNotRecognizedException;

/**
 * Transforms an InputStream in a specific XML format to an RDF/XML Stream
 */
public interface XMLProcessor {

    public InputStream processXML(InputStream is) throws SAXNotRecognizedException, TransformerException;

}