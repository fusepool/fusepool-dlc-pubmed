package eu.fusepool.rdfizer.pubmed.xslt;

import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/**
 * @author giorgio
 *
 */
public class ResourceURIResolver implements URIResolver {

    URIResolver defaultResolver = null;
    
    public ResourceURIResolver(URIResolver resolver) {
        defaultResolver = resolver ;
    }
    
    public Source resolve(String href, String base)
            throws TransformerException {

        if(href==null||"".equals(href))
            return null;
        
        StreamSource sSource = null ;
        InputStream xslIs = this.getClass().getResourceAsStream("/xsl/" + href) ;
        if(xslIs!=null) {
            sSource = new StreamSource(xslIs) ;
            return  sSource ;
        } else {
            xslIs = this.getClass().getResourceAsStream(href) ; 
            if(xslIs!=null) {
                sSource = new StreamSource(xslIs) ;
                return  sSource ;
            } else {
                return defaultResolver.resolve(href, base) ;
            }
        }
    }

}
