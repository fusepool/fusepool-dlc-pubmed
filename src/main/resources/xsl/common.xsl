<!--
    Author: Sarven Capadisli <info@csarven.ca>
    Author URI: http://csarven.ca/#i
-->
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:owl="http://www.w3.org/2002/07/owl#"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:skos="http://www.w3.org/2004/02/skos/core#"
    xmlns:prov="http://www.w3.org/ns/prov#"
    xmlns:fn="http://270a.info/xpath-function/"

    exclude-result-prefixes="xsl fn"
    >

    <xsl:output encoding="utf-8" indent="yes" method="xml" omit-xml-declaration="no"/>
<!-- CONFIG START -->
    <xsl:variable name="xmlDocumentBaseURI" select="'http://fusepool.info/data/'"/>
    <xsl:variable name="baseURI" select="'http://fusepool.info/'"/>
    <xsl:variable name="creator" select="'http://csarven.ca/#i'"/>
    <xsl:variable name="lang" select="'en'"/>
    <xsl:variable name="uriThingSeparator" select="'/'"/>

    <xsl:variable name="provenance" select="concat($baseURI, 'prov', $uriThingSeparator)"/>
    <xsl:variable name="concept" select="concat($baseURI, 'concept/')"/>
    <xsl:variable name="code" select="concat($baseURI, 'code/')"/>
    <xsl:variable name="class" select="concat($baseURI, 'class', $uriThingSeparator)"/>
    <xsl:variable name="property" select="concat($baseURI, 'property', $uriThingSeparator)"/>
    <xsl:variable name="doc" select="concat($baseURI, 'doc/')"/>
    <xsl:variable name="pubmed" select="concat($doc, 'pubmed/')"/>
    <xsl:variable name="pmc" select="concat($doc, 'pmc/')"/>
    <xsl:variable name="journal" select="concat($doc, 'journal/')"/>
    <xsl:variable name="ncbipmc" select="'http://www.ncbi.nlm.nih.gov/pmc/articles/'"/>
    <xsl:variable name="doi" select="'http://dx.doi.org/'"/>

    <xsl:variable name="conceptSchemePubMed" select="concat($concept, 'pubmed')"/>
    <xsl:variable name="entityID" select="concat($baseURI, 'id', $uriThingSeparator)"/>
    <xsl:variable name="provDocument" select="document($pathToProvDocument)/rdf:RDF"/>
<!-- CONFIG END -->

    <xsl:variable name="now" select="fn:now()"/>

    <xsl:variable name="rdf" select="'http://www.w3.org/1999/02/22-rdf-syntax-ns#'"/>
    <xsl:variable name="rdfs" select="'http://www.w3.org/2000/01/rdf-schema#'"/>
    <xsl:variable name="owl" select="'http://www.w3.org/2002/07/owl#'"/>
    <xsl:variable name="xsd" select="'http://www.w3.org/2001/XMLSchema#'"/>
    <xsl:variable name="skos" select="'http://www.w3.org/2004/02/skos/core#'"/>
    <xsl:variable name="xkos" select="'http://purl.org/linked-data/xkos#'"/>
    <xsl:variable name="foaf" select="'http://xmlns.com/foaf/0.1/'"/>
    <xsl:variable name="schema" select="'http://schema.org/'"/>
    <xsl:variable name="prov" select="'http://www.w3.org/ns/prov#'"/>
    <xsl:variable name="bibo" select="'http://purl.org/ontology/bibo/'"/>

    <!--Adapted from https://github.com/csarven/linked-sdmx/blob/master/scripts/common.xsl -->
    <xsl:template name="langTextNode">
        <xsl:if test="@xml:lang">
            <xsl:copy-of select="@*[name() = 'xml:lang']"/>
        </xsl:if>

        <xsl:choose>
            <xsl:when test="@lang">
                <xsl:attribute name="xml:lang" select="lower-case(@lang)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="$lang">
                    <xsl:attribute name="xml:lang" select="$lang"/>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:value-of select="normalize-space(.)"/>
    </xsl:template>

    <!-- Copied from http://www.xsltfunctions.com/xsl/functx_substring-before-if-contains.html -->
    <xsl:function name="fn:substring-before-if-contains" as="xs:string?">
        <xsl:param name="arg" as="xs:string?"/>
        <xsl:param name="delim" as="xs:string"/>
        <xsl:sequence select="if (contains($arg,$delim)) then substring-before($arg,$delim) else $arg"/>
    </xsl:function>


    <!--Copied from https://github.com/csarven/linked-research/blob/master/scripts/common.xsl -->
    <xsl:template name="rdfDatatypeXSD">
        <xsl:param name="type"/>

        <xsl:attribute name="rdf:datatype"><xsl:text>http://www.w3.org/2001/XMLSchema#</xsl:text><xsl:value-of select="$type"/></xsl:attribute>
    </xsl:template>


    <!--Copied from https://github.com/csarven/linked-research/blob/master/scripts/common.xsl -->
    <xsl:template name="provActivity">
        <xsl:param name="provUsedA"/>
        <xsl:param name="provUsedB"/>
        <xsl:param name="provGenerated"/>

        <xsl:variable name="now" select="format-dateTime(current-dateTime(), '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]Z')"/>
        <xsl:variable name="provActivity" select="concat($provenance, 'activity', $uriThingSeparator, replace($now, '\D', ''))"/>

        <rdf:Description rdf:about="{$provActivity}">
            <rdf:type rdf:resource="{$prov}Activity"/>
<!--dcterms:title-->
            <xsl:variable name="informedBy" select="$provDocument/rdf:Description[prov:generated/@rdf:resource = $provUsedA]/@rdf:about"/>
            <xsl:if test="$informedBy">
                <prov:wasInformedBy rdf:resource="{$informedBy}"/>
            </xsl:if>
            <prov:startedAtTime rdf:datatype="{$xsd}dateTime"><xsl:value-of select="$now"/></prov:startedAtTime>
            <prov:wasAssociatedWith rdf:resource="{$creator}"/>
            <prov:used rdf:resource="{$provUsedA}"/>
            <xsl:if test="$provUsedB">
                <prov:used rdf:resource="{$provUsedB}"/>
            </xsl:if>
            <prov:used rdf:resource="{$xslDocument}"/>
            <prov:generated>
                <rdf:Description rdf:about="{$provGenerated}">
                    <prov:wasDerivedFrom rdf:resource="{$provUsedA}"/>
                    <prov:wasGeneratedBy rdf:resource="{$provActivity}"/>
                </rdf:Description>
            </prov:generated>
        </rdf:Description>
    </xsl:template>

    <!--Copied from https://github.com/csarven/linked-research/blob/master/scripts/common.xsl -->
    <xsl:function name="fn:now">
        <xsl:value-of select="format-dateTime(current-dateTime(), '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]Z')"/>
    </xsl:function>


    <xsl:template name="dateNormalized">
        <xsl:param name="date"/>

        <xsl:analyze-string select="$date" regex="([0-9]{{4}})([0-9]{{2}})([0-9]{{2}})">
            <xsl:matching-substring>
                <xsl:call-template name="rdfDatatypeXSD">
                    <xsl:with-param name="type" select="'date'"/>
                </xsl:call-template>

                <xsl:value-of select="regex-group(1)"/><xsl:text>-</xsl:text><xsl:value-of select="regex-group(2)"/><xsl:text>-</xsl:text><xsl:value-of select="regex-group(3)"/>
            </xsl:matching-substring>

            <xsl:non-matching-substring>
                <xsl:value-of select="$date"/>
            </xsl:non-matching-substring>
        </xsl:analyze-string>
    </xsl:template>


    <xsl:function name="fn:getPubURIBase">
        <xsl:param name="pub-id-type"/>

        <xsl:choose>
            <xsl:when test="$pub-id-type = 'pmc'">
                <xsl:value-of select="$pmc"/>
            </xsl:when>
            <xsl:when test="$pub-id-type = 'pmid'">
                <xsl:value-of select="$pubmed"/>
            </xsl:when>
            <xsl:when test="$pub-id-type = 'doi'">
                <xsl:value-of select="$doi"/>
            </xsl:when>
            <xsl:otherwise>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
</xsl:stylesheet>
