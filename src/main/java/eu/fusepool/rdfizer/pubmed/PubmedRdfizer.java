/*.
 * Copyright 2013 Fusepool Project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.fusepool.rdfizer.pubmed;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.TransformerException;

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXNotRecognizedException;

import eu.fusepool.datalifecycle.Rdfizer;
import eu.fusepool.rdfizer.pubmed.xslt.XMLProcessor;
import eu.fusepool.rdfizer.pubmed.xslt.impl.PubmedXSLTProcessor;

@Component(immediate = true, metatype = true, policy = ConfigurationPolicy.OPTIONAL)
@Service(Rdfizer.class)
@Properties(value = {
    @Property(name = Constants.SERVICE_RANKING,
            intValue = PubmedRdfizer.DEFAULT_SERVICE_RANKING)
})
public class PubmedRdfizer implements Rdfizer {
    
    /**
     * Default value for the {@link Constants#SERVICE_RANKING} used by this
     * engine. This is a negative value to allow easy replacement by this engine
     * depending to a remote service with one that does not have this
     * requirement
     */
    public static final int DEFAULT_SERVICE_RANKING = 101;
    public final String RDFIZER_TYPE_LABEL = "rdfizer";
    public final String RDFIZER_TYPE_VALUE = "pubmed";
    final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Reference
    Parser parser;
    
    public MGraph transform(InputStream stream) {
        InputStream rdfIs = null;
        try {
            MGraph xml2rdf = new SimpleMGraph();
            XMLProcessor processor = new PubmedXSLTProcessor();
            log.debug("Starting transformation from XML to RDF");
            rdfIs = processor.processXML(stream);
            parser.parse(xml2rdf, rdfIs, SupportedFormat.RDF_XML);
            rdfIs.close();
            log.info("Completed transformation from XML to RDF");
            return xml2rdf;
        } catch (TransformerException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (SAXNotRecognizedException ex) {
            throw new RuntimeException(ex);
        }
        
    }
    
    public String getName() {
        return this.RDFIZER_TYPE_VALUE;
    }

    @Activate
    protected void activate(ComponentContext context) {
        log.info("PubMed XML Rdfize service is being activated");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.info("PubMed XML Rdfize service is being deactivated");
    }

}
