package eu.fusepool.rdfizer.pubmed;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.fusepool.datalifecycle.Rdfizer;

public class PubmedRdfizerTest {
    
    Rdfizer rdfizer;

    @Before
    public void setUp() throws Exception {
        PubmedRdfizer pubmedRdfizer = new PubmedRdfizer();
        pubmedRdfizer.parser = Parser.getInstance();
        rdfizer = pubmedRdfizer; 
    }

    @Test
    public void testTransform() {
        InputStream in = this.getClass().getResourceAsStream("Biomarkers_20110225.nmxl");        
        MGraph mGraph = rdfizer.transform(in);
        Assert.assertTrue(mGraph.size() > 0);
    }

}
