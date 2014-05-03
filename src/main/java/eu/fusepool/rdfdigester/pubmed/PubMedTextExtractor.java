package eu.fusepool.rdfdigester.pubmed;

import java.io.IOException;
import java.security.AccessController;
import java.security.AllPermission;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.ws.rs.Path;

import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.DC;
import org.apache.clerezza.rdf.ontologies.DCTERMS;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.ontologies.SIOC;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;
import org.apache.stanbol.enhancer.servicesapi.ContentSource;
import org.apache.stanbol.enhancer.servicesapi.EnhancementException;
import org.apache.stanbol.enhancer.servicesapi.EnhancementJobManager;
import org.apache.stanbol.enhancer.servicesapi.impl.ByteArraySource;
import org.apache.stanbol.enhancer.servicesapi.rdf.TechnicalClasses;
import org.apache.stanbol.entityhub.model.clerezza.RdfValueFactory;
import org.apache.stanbol.entityhub.servicesapi.model.Entity;
import org.apache.stanbol.entityhub.servicesapi.model.Representation;
import org.apache.stanbol.entityhub.servicesapi.site.SiteManager;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fusepool.datalifecycle.RdfDigester;
import eu.fusepool.ecs.ontologies.ECS;



/**
 * Extracts text from properties dcterms:title and dcterms:abstract of individuals of type bibo:Document. 
 * The extracted text is added as a value of the sioc.content property to the same individuals. 
 * The text can be used for indexing purposes.
 */

@Component(immediate = false, metatype = true, 
configurationFactory = true, policy = ConfigurationPolicy.OPTIONAL)
@Service(RdfDigester.class)
@Properties(value = {
	    @Property(name = Constants.SERVICE_RANKING, 
	    		intValue = PubMedTextExtractor.DEFAULT_SERVICE_RANKING)
})

public class PubMedTextExtractor implements RdfDigester {
	
	public static final int DEFAULT_SERVICE_RANKING = 101;
	
    public final String DIGESTER_TYPE_LABEL = "digesterImpl";
	
	public final String DIGESTER_TYPE_VALUE = "pubmed";
	
	private static Logger log = LoggerFactory.getLogger(PubMedTextExtractor.class);
	
	@Reference
    private ContentItemFactory contentItemFactory;
	
	@Reference
    private EnhancementJobManager enhancementJobManager;
	
	/**
     * This service allows to get entities from configures sites
     */
    @Reference
    private SiteManager siteManager;
	
	/**
	 * Looks for sioc:content property in the input document graph in individual of type bibo:Document. If there's no such property
	 * it adds it. The value of the property is taken from the following properties:
	 * dcterms:title, dcterms:abstract 
	 */
	public void extractText(MGraph graph) {
		// select all the resources that are bibo:Document and do not have a sioc:content property 
		Set<UriRef> articleRefs = getArticles( graph);
		for (UriRef articleRef : articleRefs) {                        
            
            log.info("Adding sioc:content property to article: " + articleRef.getUnicodeString());
			// extract text from properties and add it to the patent with a sioc:content property
            addSiocContentToArticle(graph, articleRef);            
            // add subjects from contributors and publishers (to be implemented)
        }
		
	}
	
	/**
     * Select all resources of type bibo:Document that do not have a sioc:content property and have at least a
     * dcterms:title property.
     */
    private Set<UriRef> getArticles(MGraph graph) {
    	Set<UriRef> result = new HashSet<UriRef>();
    	
    	Lock lock = null;
        if (graph instanceof LockableMGraph) {
            lock = ((LockableMGraph)graph).getLock().readLock();
            lock.lock();
        }
    	try {
	        Iterator<Triple> idocument = graph.filter(null, RDF.type, Ontology.Document);
	        while (idocument.hasNext()) {
	            Triple triple = idocument.next();
	            UriRef articleRef = (UriRef) triple.getSubject();
	            GraphNode node = new GraphNode(articleRef, graph);
	            if (!node.getObjects(SIOC.content).hasNext() && node.getObjects(DCTERMS.title).hasNext() ) {
	                result.add(articleRef);
	            }
	        }
    	}
    	finally {
            if (lock != null) {
            lock.unlock();
            }
        }
        
        log.info(result.size() + " Document nodes found.");
        
        return result;
    }
    
    /**
     * Add a sioc:content property to a resource. 
     * The value is taken from dcterm:title and dcterms:abstract properties 
     */

    private void addSiocContentToArticle(MGraph graph, UriRef articleRef) {
    
    	AccessController.checkPermission(new AllPermission());
    	
    	String textContent = "";
    	
    	GraphNode node = new GraphNode(articleRef, graph);
    	
    	Lock lock = null;
        if (graph instanceof LockableMGraph) {
            lock = ((LockableMGraph)graph).getLock().readLock();
            lock.lock();
        }
    	try {    	
	        Iterator<Literal> titles = node.getLiterals(DCTERMS.title);
	        while (titles.hasNext()) {
	        	String title = titles.next().getLexicalForm() + "\n";
	            textContent += title;
	        }
	        
	        Iterator<Literal> abstracts = node.getLiterals(DCTERMS.abstract_);
	        while (abstracts.hasNext()) {
	        	String _abstract = abstracts.next().getLexicalForm() + "\n";
	            textContent += _abstract;
	        }
    	}
    	finally {
            if (lock != null) {
            lock.unlock();
            }
        }
        
        if(!"".equals(textContent)) {
        	
        	graph.add(new TripleImpl(articleRef, SIOC.content, new PlainLiteralImpl(textContent)));
        	        	
        	// Resources with this type have sioc:content and rdfs:label indexed by the ECS 
        	// when added to the content graph
        	graph.add(new TripleImpl(articleRef, RDF.type, ECS.ContentItem));
        	
        	log.info("Added sioc:content property to patent " + articleRef.getUnicodeString());
        }
        else {
        	log.info("No text found in dcterms:title or dcterms:abstract to add to sioc:content");
        }

    }
	
	@Activate
    protected void activate(ComponentContext context) {
        log.info("The PubMedTextExtractor service is being activated");

    }
	
	public String getName() {
		return this.DIGESTER_TYPE_VALUE;
	}

    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.info("The PubMedTextExtractor service is being deactivated");
    }

}
