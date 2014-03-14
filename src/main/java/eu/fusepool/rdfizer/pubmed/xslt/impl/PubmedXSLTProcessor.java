package eu.fusepool.rdfizer.pubmed.xslt.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xml.resolver.tools.ResolvingXMLFilter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;

import eu.fusepool.rdfizer.pubmed.xslt.PubmedXMLReader;
import eu.fusepool.rdfizer.pubmed.xslt.ResourceURIResolver;
import eu.fusepool.rdfizer.pubmed.xslt.XMLProcessor;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

/**
 * @author giorgio
 * @author luigi
 *
 */
public class PubmedXSLTProcessor implements XMLProcessor {

    private TransformerFactory tFactory;

    public PubmedXSLTProcessor() {

        //just referencing the class to make sure it's in the OSGi import and available
        if (net.sf.saxon.TransformerFactoryImpl.class == null) {
            throw new RuntimeException("You'll never get this exception");
        }
        tFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", this.getClass().getClassLoader());

    }

    public InputStream processXML(InputStream is) throws SAXNotRecognizedException, TransformerException {
        URIResolver defResolver = tFactory.getURIResolver();
        ResourceURIResolver customResolver = new ResourceURIResolver(defResolver);
        tFactory.setURIResolver(customResolver);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream xslIs = this.getClass().getResourceAsStream("/xsl/pubmed.xsl");
        StreamSource xlsSS = new StreamSource(xslIs);
        Transformer transformer = tFactory.newTransformer(xlsSS);

        InputSource inputSource = new InputSource(is);

        ResolvingXMLFilter filter = new ResolvingXMLFilter(new PubmedXMLReader());

        Source saxSource = new SAXSource(filter, inputSource);

        StreamResult sRes = new StreamResult(outputStream);

        transformer.transform(saxSource, sRes);

        return new ByteArrayInputStream(outputStream.toByteArray());

    }

}
