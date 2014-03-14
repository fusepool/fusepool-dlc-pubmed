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
package eu.fusepool.rdfizer.pubmed.xslt;

import java.io.StringReader;


import org.apache.xml.resolver.tools.ResolvingXMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;


public class PubmedXMLReader extends ResolvingXMLReader {

    
    /**
     * @throws Exception 
     * @throws SAXNotRecognizedException 
     * 
     */
    public PubmedXMLReader() {
        super();        
    }

    /* (non-Javadoc)
     * @see org.apache.xml.resolver.tools.ResolvingXMLFilter#resolveEntity(java.lang.String, java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        return new InputSource(new StringReader(""));
    }
}
