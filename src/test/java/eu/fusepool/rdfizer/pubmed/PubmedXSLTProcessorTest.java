package eu.fusepool.rdfizer.pubmed;

import java.io.InputStream;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.junit.Assert;
import org.junit.Test;
import eu.fusepool.rdfizer.pubmed.xslt.XMLProcessor;
import eu.fusepool.rdfizer.pubmed.xslt.impl.PubmedXSLTProcessor;

public class PubmedXSLTProcessorTest {


    @Test
    public void testProcessXml() throws Exception {
        
        // Start the transformer
        XMLProcessor processor = new PubmedXSLTProcessor();

        // Transform a XML file
        InputStream xmlIn = this.getClass().getResourceAsStream("Acta_Oncol20110616.nxml");

        InputStream rdfFromXmlIn = processor.processXML(xmlIn);

        Parser parser = Parser.getInstance();


        Graph graphFromXml = parser.parse(rdfFromXmlIn, SupportedFormat.RDF_XML);
        rdfFromXmlIn.close();
        
        Assert.assertTrue(graphFromXml.size() > 0);
        
        
    }

}
