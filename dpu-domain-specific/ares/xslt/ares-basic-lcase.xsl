<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:are="http://wwwinfo.mfcr.cz/ares/xml_doc/schemas/ares/ares_answer_basic/v_1.0.3"
    xmlns:d="http://wwwinfo.mfcr.cz/ares/xml_doc/schemas/ares/ares_datatypes/v_1.0.3"
    xmlns:u="http://wwwinfo.mfcr.cz/ares/xml_doc/schemas/uvis_datatypes/v_1.0.3"
    xmlns:f="http://opendata.cz/xslt/functions#"
    exclude-result-prefixes="xs are d u f"
    
    xmlns:adms="http://www.w3.org/ns/adms#"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:gr="http://purl.org/goodrelations/v1#"
    xmlns:lex="http://purl.org/lex#"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rov="http://www.w3.org/ns/regorg#"
    xmlns:schema="http://schema.org/"
    xmlns:skos="http://www.w3.org/2004/02/skos/core#"
    xmlns:void="http://rdfs.org/ns/void#"
    xmlns:owl="http://www.w3.org/2002/07/owl#"
    xmlns:ruianlink="http://ruian.linked.opendata.cz/ontology/links/"
    version="2.0">
    
    <!--
        Seznam zkratek: http://wwwinfo.mfcr.cz/ares/xml_doc/schemas/documentation/zkr_103.txt
        XML schéma: http://wwwinfo.mfcr.cz/ares/xml_doc/schemas/ares/ares_datatypes/v_1.0.3/ares_datatypes_v_1.0.3.xsd
    -->
    
    <xsl:param name="namespace">http://linked.opendata.cz/resource/</xsl:param>
	<xsl:variable name="beURIprefix">http://linked.opendata.cz/resource/business-entity/</xsl:variable>
	<xsl:variable name="nacePrefix">http://ec.europa.eu/eurostat/ramon/rdfdata/nace_r2/</xsl:variable>
	<xsl:variable name="ruianPrefix">http://ruian.linked.opendata.cz/resource/</xsl:variable>
    <xsl:variable name="baseURI" select="concat($namespace, 'domain/ares/')"/>
    <xsl:variable name="icoScheme" select="concat($namespace, 'concept-scheme/CZ-ICO')"/>
    <xsl:strip-space elements="*"/>
    
    <xsl:output encoding="UTF-8" indent="yes" method="xml" normalization-form="NFC"/>
    
    <xsl:function name="f:classURI" as="xs:anyURI">
        <xsl:param name="classLabel" as="xs:string"/>
        <xsl:param name="id" as="xs:string"/>
        <xsl:value-of select="f:pathIdURI(encode-for-uri(replace(lower-case(normalize-space($classLabel)), '\s', '-')), normalize-space($id))"/>
    </xsl:function>
    
    <xsl:function name="f:icoBasedURI" as="xs:anyURI">
        <xsl:param name="ico" as="xs:string"/>
        <xsl:param name="fragment" as="xs:string"/>
        <xsl:value-of select="concat($beURIprefix, normalize-space($ico), if (string-length($fragment) > 0) then concat('/', normalize-space($fragment)) else '')"/>
    </xsl:function>
    
    <xsl:function name="f:icoBasedAddressURI" as="xs:anyURI">
        <xsl:param name="ico" as="xs:string"/>
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="f:icoBasedURI(normalize-space($ico), concat('postal-address/', $context/local-name()))"/>
    </xsl:function>
    
    <xsl:function name="f:pathIdURIWithICOFallback" as="xs:anyURI">
        <xsl:param name="ico" as="xs:string"/>
        <xsl:param name="path" as="xs:string"/>
        <xsl:param name="id" as="node()*"/>
        <xsl:param name="context" as="node()"/>
        <xsl:choose>
            <xsl:when test="$id">
                <xsl:value-of select="f:pathIdURI($path, normalize-space($id))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="f:icoBasedURI($ico, concat($path, '/', $context/local-name()))"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    
    <xsl:function name="f:pathURI" as="xs:anyURI">
        <xsl:param name="path" as="xs:string"/>
        <xsl:value-of select="concat($baseURI, $path)"/>
    </xsl:function>
    
    <xsl:function name="f:pathIdURI" as="xs:anyURI">
        <xsl:param name="path" as="xs:string"/>
        <xsl:param name="id" as="xs:string"/>
        <xsl:value-of select="concat(f:pathURI($path), '/', encode-for-uri(normalize-space($id)))"/>
    </xsl:function>
    
    <xsl:function name="f:prefixICO" as="xs:string">
        <xsl:param name="ico" as="xs:string"/>
        <xsl:value-of select="concat('CZ', $ico)"/>
    </xsl:function>

    
    <xsl:template match="are:ares_odpovedi">
        <rdf:RDF>
            <xsl:apply-templates/>
        </rdf:RDF>
    </xsl:template>
    
    <xsl:template match="are:odpoved">
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="d:vbas">
        <xsl:variable name="ico" select="f:prefixICO(normalize-space(d:ico/text()))"/>
        <gr:BusinessEntity rdf:about="{f:icoBasedURI($ico,'')}">
            <rdf:type rdf:resource="http://www.w3.org/ns/regorg#RegisteredOrganization"/>
            <rov:registration rdf:resource="{f:classURI('Identifier', $ico)}"/>
            <gr:legalName><xsl:value-of select="normalize-space(d:of)"/></gr:legalName>
            <xsl:apply-templates>
                <xsl:with-param name="ico" tunnel="yes" select="$ico"/>
            </xsl:apply-templates>
        </gr:BusinessEntity>    
        <xsl:apply-templates mode="linked">
            <xsl:with-param name="ico" tunnel="yes" select="$ico"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- gr:BusinessEntity's properties -->
    
    <xsl:template match="d:aa">
        <!-- Adresa ARES -->
        <xsl:param name="ico" tunnel="yes"/>
        <schema:address rdf:resource="{f:pathIdURIWithICOFallback($ico, 'postal-address', d:ida, .)}"/>
    </xsl:template>
    
    <xsl:template match="d:ad">
        <!-- Adresa doručovací -->
        <xsl:param name="ico" tunnel="yes"/>
        <schema:address rdf:resource="{f:icoBasedAddressURI($ico, .)}"/>
    </xsl:template>
    
    <xsl:template match="d:nace[not(d:nace)]">
        <xsl:param name="ico" tunnel="yes"/>
        <rov:orgActivity rdf:resource="{f:pathIdURIWithICOFallback($ico, 'concept-scheme/nace', text(), .)}"/>
    </xsl:template>
    
    <xsl:template match="d:nace[d:nace]">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="d:obory_cinnosti">
        <!-- Obory činnosti -->
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="d:obor_cinnosti">
        <!-- Obor činnosti -->
        <xsl:param name="ico" tunnel="yes"/>
        <rov:orgActivity rdf:resource="{f:pathIdURIWithICOFallback($ico, 'concept-scheme/organization-activities', d:k, .)}"/>
    </xsl:template>
    
    <xsl:template match="d:pf">
        <!-- Právní forma -->
        <rov:orgType rdf:resource="{f:pathIdURI('concept-scheme/organization-types', d:kpf/text())}"/>    
    </xsl:template>
    
    <xsl:template match="d:ppi/d:pp/d:t">
        <!-- Předměty podnikání -->
        <rov:orgActivity>
            <skos:Concept>
                <skos:prefLabel xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel>
            </skos:Concept>
        </rov:orgActivity>
    </xsl:template>
    
    <xsl:template match="d:psu">
        <!-- Interní příznaky subjektu -->
        <!-- Není blíže určeno ve XML schématu, hodnoty jako "NAAANNNNNNNNNNNNNNNNNNNNANNNNN" -->
    </xsl:template>
    
    <xsl:template match="d:ror/d:sor/d:ssu">
        <!-- Stav subjektu -->
        <rov:orgStatus>
            <skos:Concept>
                <skos:inScheme rdf:resource="{f:pathURI('concept-scheme/organization-statuses')}"/>
                <skos:prefLabel xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel>
            </skos:Concept>
        </rov:orgStatus>
    </xsl:template>
    
    <!-- lex:Court's properties -->
    
    <xsl:template mode="court" match="d:k">
        <adms:identifier>
            <adms:Identifier>
                <skos:notation><xsl:value-of select="normalize-space(./text())"/></skos:notation>
            </adms:Identifier>
        </adms:identifier>
    </xsl:template>
    
    <xsl:template mode="court" match="d:t">
        <dcterms:title xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></dcterms:title>
    </xsl:template>
    
    <!-- Templates for linked resources -->
    
    <xsl:template mode="linked" match="d:ico">
        <adms:Identifier rdf:about="{f:classURI('Identifier', f:prefixICO(normalize-space(text())))}">
            <skos:notation><xsl:value-of select="normalize-space(text())"/></skos:notation>
            <skos:inScheme rdf:resource="{$icoScheme}"/>
            <adms:schemeAgency xml:lang="cs">Český statistický úřad</adms:schemeAgency>
            <xsl:apply-templates mode="identifier" select="../d:dv|../d:ror/d:sz/d:sd"/>
        </adms:Identifier>
    </xsl:template>
    
    <xsl:template mode="identifier" match="d:dv">
        <!-- Datum vydání -->
        <dcterms:issued rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="normalize-space(./text())"/></dcterms:issued>
    </xsl:template>
    
    <xsl:template mode="identifier" match="d:ror/d:sz/d:sd">
        <!-- Registrace subjektu u soudu -->
        <!-- TODO: Nalinkovat na http://linked.opendata.cz/resource/dataset/court/cz? -->
        <dcterms:creator>
            <lex:Court>
                <xsl:apply-templates mode="court"/>
            </lex:Court>
        </dcterms:creator>
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template mode="linked" match="d:aa">
        <!-- Adresa ARES -->
        <xsl:param name="ico" tunnel="yes"/>
        <schema:PostalAddress rdf:about="{f:pathIdURIWithICOFallback($ico, 'postal-address', d:ida, .)}">
            <xsl:apply-templates mode="linked"/>
        </schema:PostalAddress>    
    </xsl:template>
    
    <xsl:template mode="linked" match="d:ad">
        <!-- Adresa doručovací -->
        <xsl:param name="ico" tunnel="yes"/>
        <schema:PostalAddress rdf:about="{f:icoBasedAddressURI($ico, .)}">
            <xsl:apply-templates mode="linked"/>
        </schema:PostalAddress>
    </xsl:template>
    
<!-- RUIAN START -->
    <xsl:template mode="linked" match="d:au">
        <!-- Adresa RUIAN -->
        <xsl:apply-templates mode="linked"/>
    </xsl:template>

    <xsl:template mode="linked" match="u:ka">
        <!-- Adresni misto RUIAN -->
        <ruianlink:adresni-misto>
			<xsl:attribute name="rdf:resource" select="concat($ruianPrefix, 'adresni-mista/', text())"/>
        </ruianlink:adresni-misto>
    </xsl:template>

    <xsl:template mode="linked" match="u:kob">
        <!-- Objekt RUIAN -->
        <ruianlink:stavebni-objekt>
			<xsl:attribute name="rdf:resource" select="concat($ruianPrefix, 'stavebni-objekty/', text())"/>
        </ruianlink:stavebni-objekt>
    </xsl:template>

    <xsl:template mode="linked" match="u:kul">
        <!-- Ulice RUIAN -->
        <ruianlink:ulice>
			<xsl:attribute name="rdf:resource" select="concat($ruianPrefix, 'ulice/', text())"/>
        </ruianlink:ulice>
    </xsl:template>

    <xsl:template mode="linked" match="u:ko">
        <!-- Obec RUIAN -->
        <ruianlink:obec>
			<xsl:attribute name="rdf:resource" select="concat($ruianPrefix, 'obce/', text())"/>
        </ruianlink:obec>
    </xsl:template>

    <xsl:template mode="linked" match="u:kco">
        <!-- Část obce RUIAN -->
        <ruianlink:cast-obce>
			<xsl:attribute name="rdf:resource" select="concat($ruianPrefix, 'casti-obci/', text())"/>
        </ruianlink:cast-obce>
    </xsl:template>

    <xsl:template mode="linked" match="u:kok">
        <!-- Okres RUIAN -->
        <ruianlink:okres>
			<xsl:attribute name="rdf:resource" select="concat($ruianPrefix, 'okresy/', text())"/>
        </ruianlink:okres>
    </xsl:template>

    <xsl:template mode="linked" match="u:kmc">
        <!-- MOMC RUIAN -->
        <ruianlink:momc>
			<xsl:attribute name="rdf:resource" select="concat($ruianPrefix, 'momc/', text())"/>
        </ruianlink:momc>
    </xsl:template>

    <xsl:template mode="linked" match="u:kk">
        <!-- VUSC RUIAN -->
        <ruianlink:vusc>
			<xsl:attribute name="rdf:resource" select="concat($ruianPrefix, 'vusc/', text())"/>
        </ruianlink:vusc>
    </xsl:template>

    <xsl:template mode="linked" match="u:kol">
        <!-- RS RUIAN -->
        <ruianlink:region-soudrznosti>
			<xsl:attribute name="rdf:resource" select="concat($ruianPrefix, 'regiony-soudrznosti/', text())"/>
        </ruianlink:region-soudrznosti>
    </xsl:template>

    <xsl:template mode="linked" match="u:kso">
        <!-- Správní obvod RUIAN -->
        <ruianlink:spravni-obvod>
			<xsl:attribute name="rdf:resource" select="concat($ruianPrefix, 'spravni-obvody/', text())"/>
        </ruianlink:spravni-obvod>
    </xsl:template>

 <!-- RUIAN END -->

    <xsl:template mode="linked" match="d:at[parent::d:aa]">
        <!-- Adresa textem -->
        <schema:description><xsl:value-of select="normalize-space(./text())"/></schema:description>    
    </xsl:template>
    
    <xsl:template mode="linked" match="d:cd[parent::d:aa]|d:ca[parent::d:aa]">
        <!-- Číslo domu -->
        <xsl:variable name="cislo_orientacni" select="../d:co"/>
        <xsl:variable name="nazev_obce" select="../d:n"/>
        <xsl:variable name="ulice" select="../d:nu"/>
        <xsl:variable name="cislo">
            <xsl:choose>
                <xsl:when test="$cislo_orientacni"><xsl:value-of select="concat(., '/', $cislo_orientacni)"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="normalize-space(./text())"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <schema:streetAddress>
            <xsl:choose>
                <xsl:when test="not($ulice) and $nazev_obce"><xsl:value-of select="concat(normalize-space($nazev_obce/text()), ' ', normalize-space($cislo/text()))"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="concat(normalize-space($ulice/text()), ' ', normalize-space($cislo/text()))"/></xsl:otherwise>
            </xsl:choose>
        </schema:streetAddress>
    </xsl:template>
    
    <xsl:template mode="linked" match="d:n[parent::d:aa]">
        <!-- Název obce -->
        <xsl:variable name="nazev_casti_obce" select="../d:nco"/>
        <xsl:variable name="nazev_mestske_casti" select="../d:nmc"/>
        <xsl:variable name="nazev">
            <xsl:choose>
                <xsl:when test="$nazev_casti_obce and $nazev_mestske_casti">
                    <xsl:value-of select="concat(normalize-space(./text()), ', ', normalize-space($nazev_mestske_casti/text()), ' - ', normalize-space($nazev_casti_obce/text()))"/>
                </xsl:when>
                <xsl:when test="$nazev_casti_obce and not($nazev_mestske_casti)">
                    <xsl:value-of select="concat(normalize-space(./text()), ', ', normalize-space($nazev_casti_obce/text()))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space(./text())"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <schema:addressLocality><xsl:value-of select="$nazev"/></schema:addressLocality>
    </xsl:template>
    
    <xsl:template mode="linked" match="d:nok[parent::d:aa]">
        <!-- Název okresu -->
        <schema:addressRegion><xsl:value-of select="normalize-space(./text())"/></schema:addressRegion>    
    </xsl:template>
    
    <xsl:template mode="linked" match="d:ns[parent::d:aa]">
        <!-- Název státu -->
        <schema:addressCountry><xsl:value-of select="normalize-space(./text())"/></schema:addressCountry>
    </xsl:template>
    
    <xsl:template mode="linked" match="d:nace[d:nace]">
		<xsl:apply-templates mode="linked"/>
    </xsl:template>

    <xsl:template mode="linked" match="d:nace[not(d:nace)]">
        <!-- NACE kód -->
        <xsl:param name="ico" tunnel="yes"/>
        <xsl:variable name="NACEURI">
			<xsl:if test="string-length(text()) &lt; 3">
				<xsl:value-of select="concat($nacePrefix, text())"/>
			</xsl:if>
			<xsl:if test="string-length(text()) &gt; 2">
				<xsl:value-of select="concat($nacePrefix, replace(text(), '([0-9][0-9])([0-9][0-9]?).*', '$1.$2'))"/>
			</xsl:if>
        </xsl:variable>
        <skos:Concept rdf:about="{f:pathIdURIWithICOFallback($ico, 'concept-scheme/nace', text(), .)}">
            <skos:inScheme rdf:resource="http://ec.europa.eu/eurostat/ramon/rdfdata/nace_r2"/>
            <owl:sameAs>
				<xsl:attribute name="rdf:resource" select="$NACEURI"/>
            </owl:sameAs>
            <xsl:apply-templates mode="linked"/>
        </skos:Concept>  
    </xsl:template>
    
    <xsl:template mode="linked" match="text()[parent::d:nace]">
        <skos:notation><xsl:value-of select="normalize-space(.)"/></skos:notation>    
    </xsl:template>
    
    <xsl:template mode="linked" match="d:obory_cinnosti">
        <!-- Obory činnosti -->
        <xsl:apply-templates mode="linked"/>
    </xsl:template>
    
    <xsl:template mode="linked" match="d:obor_cinnosti">
        <!-- Obor činnosti -->
        <xsl:param name="ico" tunnel="yes"/>
        <xsl:variable name="schemePath">concept-scheme/organization-activities</xsl:variable>
        <skos:Concept rdf:about="{f:pathIdURIWithICOFallback($ico, $schemePath, d:k, .)}">
            <skos:inScheme rdf:resource="{f:pathURI($schemePath)}"/>
            <xsl:apply-templates mode="linked"/>
        </skos:Concept>
    </xsl:template>
    
    <xsl:template mode="linked" match="d:k[parent::d:obor_cinnosti]">
        <!-- Kód oboru činnosti -->
        <skos:notation><xsl:value-of select="normalize-space(./text())"/></skos:notation>
    </xsl:template>
    
    <xsl:template mode="linked" match="d:t[parent::d:obor_cinnosti]">
        <!-- Název oboru činnosti -->
        <skos:prefLabel xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel>
    </xsl:template>
    
    <xsl:template mode="linked" match="d:psc[parent::d:aa]|d:zahr_psc[parent::d:aa]">
        <!-- Poštovní směrovací číslo, zahraniční PSČ -->
        <schema:postalCode><xsl:value-of select="normalize-space(./text())"/></schema:postalCode>
    </xsl:template>
    
    <xsl:template mode="linked" match="d:pb[parent::d:ad]">
        <!-- PSČ a obec -->
        <xsl:analyze-string select="normalize-space(./text())" regex="(\d{{5}})\s+(\w{{1,54}})">
            <xsl:matching-substring>
                <schema:postalCode><xsl:value-of select="regex-group(1)"/></schema:postalCode>
                <schema:addressLocality><xsl:value-of select="regex-group(2)"/></schema:addressLocality>
            </xsl:matching-substring>
            <xsl:non-matching-substring>
                <schema:description><xsl:value-of select="normalize-space(.)"/></schema:description>
            </xsl:non-matching-substring>
        </xsl:analyze-string>
    </xsl:template>
    
    <xsl:template mode="linked" match="d:uc[parent::d:ad]">
        <!-- Ulice a číslo -->
        <schema:streetAddress><xsl:value-of select="normalize-space(./text())"/></schema:streetAddress>
    </xsl:template>
    
    <xsl:template mode="linked" match="d:pf">
        <!-- Právní forma -->
        <xsl:variable name="schemePath">concept-scheme/organization-types</xsl:variable>
        <skos:Concept rdf:about="{f:pathIdURI($schemePath, d:kpf/text())}">
            <skos:inScheme rdf:resource="{f:pathURI($schemePath)}"/>
            <xsl:apply-templates mode="linked"/>
        </skos:Concept>    
    </xsl:template>
    
    <xsl:template mode="linked" match="d:kpf[parent::d:pf]">
        <!-- Kód právní formy -->
        <skos:notation><xsl:value-of select="normalize-space(./text())"/></skos:notation>
    </xsl:template>
    
    <xsl:template mode="linked" match="d:npf[parent::d:pf]">
        <!-- Název právní formy -->
        <skos:prefLabel xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel> 
    </xsl:template>
    
    <!-- Catch-all empty template -->
    <xsl:template mode="#all" match="*|text()|@*"/>
    
</xsl:stylesheet>