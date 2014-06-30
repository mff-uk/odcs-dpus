<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:are="http://wwwinfo.mfcr.cz/ares/xml_doc/schemas/ares/ares_answer_rzp/v_1.0.5"
    xmlns:d="http://wwwinfo.mfcr.cz/ares/xml_doc/schemas/ares/ares_datatypes/v_1.0.5"
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
    xmlns:lodares="http://linked.opendata.cz/ontology/ares#"
    xmlns:ruianlink="http://ruian.linked.opendata.cz/ontology/links/"
    xmlns:foaf="http://xmlns.com/foaf/0.1/"
    version="2.0">
    
    <!--
        Seznam zkratek: http://wwwinfo.mfcr.cz/ares/xml_doc/schemas/documentation/zkr_103.txt
        XML schéma: http://wwwinfo.mfcr.cz/ares/xml_doc/schemas/ares/ares_datatypes/v_1.0.3/ares_datatypes_v_1.0.3.xsd
    -->
    
    <xsl:param name="namespace">http://linked.opendata.cz/resource/</xsl:param>
	<xsl:variable name="beURIprefix">http://linked.opendata.cz/resource/business-entity/</xsl:variable>
    <xsl:variable name="baseURI" select="concat($namespace, 'domain/ares/')"/>
    <xsl:variable name="icoScheme" select="concat($namespace, 'concept-scheme/CZ-ICO')"/>
    <xsl:variable name="druhAkcieScheme">concept-scheme/druh-akcie</xsl:variable>
    <xsl:variable name="podobaAkcieScheme">concept-scheme/podoba-akcie</xsl:variable>
    <xsl:variable name="funkceVPredstavenstvuScheme">concept-scheme/funkce-v-predstavenstvu</xsl:variable>
    <xsl:variable name="funkceVDozorciRadeScheme">concept-scheme/funkce-v-dozorci-rade</xsl:variable>
    <xsl:variable name="funkceVStatutarnimOrganuScheme">concept-scheme/funkce-v-statutarnim-organu</xsl:variable>
    <xsl:variable name="kodAngmScheme">concept-scheme/kod-angm</xsl:variable>
	<xsl:variable name="ruianPrefix">http://ruian.linked.opendata.cz/resource/</xsl:variable>
    <xsl:strip-space elements="*"/>
    
    <xsl:output encoding="UTF-8" indent="yes" method="xml" normalization-form="NFC"/>
    
	<xsl:function name="f:getPOURI" as="xs:anyURI">
		<xsl:param name="context" as="node()"/>
		<xsl:value-of select="if ($context/d:ico) then f:icoBasedURI(normalize-space($context/d:ico/text()),'') else f:pathURI(
				concat('business-entity',
							'/',
							encode-for-uri(
								lower-case(
									normalize-space($context/d:of/text())
								)
							)
						)
				)"/>
	</xsl:function>

    <xsl:function name="f:classURI" as="xs:anyURI">
        <xsl:param name="classLabel" as="xs:string"/>
        <xsl:param name="id" as="xs:string"/>
        <xsl:value-of select="f:pathIdURI(encode-for-uri(replace(lower-case(normalize-space($classLabel)), '\s', '-')), normalize-space($id))"/>
    </xsl:function>
    
    <xsl:function name="f:icoBasedURI" as="xs:anyURI">
        <xsl:param name="ico" as="xs:string"/>
        <xsl:param name="fragment" as="xs:string"/>
        <xsl:value-of select="concat($beURIprefix, 'CZ', normalize-space($ico), if (string-length($fragment) > 0) then concat('/', normalize-space($fragment)) else '')"/>
    </xsl:function>
    
    <xsl:function name="f:icoBasedDomainURI" as="xs:anyURI">
        <xsl:param name="ico" as="xs:string"/>
        <xsl:param name="fragment" as="xs:string"/>
        <xsl:value-of select="concat($baseURI,'business-entity/CZ', normalize-space($ico), '/', normalize-space($fragment))"/>
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
    
    <xsl:template match="are:ares_odpovedi">
        <rdf:RDF>
            <xsl:apply-templates/>
        </rdf:RDF>
    </xsl:template>
    
    <xsl:template match="are:odpoved">
        <xsl:apply-templates/>
    </xsl:template>
    
	<xsl:template match="d:vypis_rzp">
		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="normalize-space(d:zau/d:ico/text())"/>		
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:zau" mode="linked">
		<!-- Základní údaje -->
        <xsl:param name="ico"/>
        <gr:BusinessEntity rdf:about="{concat($beURIprefix, 'CZ', normalize-space($ico))}">
            <dcterms:modified rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="normalize-space(d:datum_zmeny)"/></dcterms:modified>
            <rdf:type rdf:resource="http://www.w3.org/ns/regorg#RegisteredOrganization"/>
            <adms:identifier rdf:resource="{f:icoBasedURI($ico,concat('identifier/',$ico))}"/>
            <gr:legalName><xsl:value-of select="normalize-space(d:of)"/></gr:legalName>
            <dcterms:issued rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="normalize-space(d:zi1)"/></dcterms:issued>
			<xsl:apply-templates>
				<xsl:with-param name="ico" select="$ico"/>
			</xsl:apply-templates>
			<xsl:apply-templates select="following-sibling::*">
				<xsl:with-param name="ico" select="$ico"/>
			</xsl:apply-templates>
        </gr:BusinessEntity>
		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

    <xsl:template mode="linked" match="d:ico">
		<xsl:param name="ico"/>
        <adms:Identifier rdf:about="{f:icoBasedURI(.,concat('identifier/',.))}">
            <skos:notation><xsl:value-of select="normalize-space(text())"/></skos:notation>
            <skos:prefLabel><xsl:value-of select="normalize-space(text())"/></skos:prefLabel>
            <skos:inScheme rdf:resource="{$icoScheme}"/>
            <adms:schemeAgency>Český statistický úřad</adms:schemeAgency>
            <xsl:apply-templates mode="identifier" select="../d:dv|../d:ror/d:sz/d:sd"/>
        </adms:Identifier>
    </xsl:template>

	<xsl:template match="d:zi">
		<!-- Živnosti -->
		<xsl:param name="ico"/>
		<xsl:apply-templates>
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:zi" mode="linked">
		<!-- Živnosti -->
		<xsl:param name="ico"/>
		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:z" mode="linked">
		<!-- Živnosti -->
		<xsl:param name="ico"/>

		<lodares:Zivnost rdf:about="{f:icoBasedURI($ico,concat('zivnost/',d:vznik, '/', d:s, '/', d:druh, '/', encode-for-uri(normalize-space(d:pp))))}">
			<xsl:apply-templates>
				<xsl:with-param name="ico" select="$ico"/>
			</xsl:apply-templates>
		</lodares:Zivnost>

		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:z">
		<!-- Živnosti -->
		<xsl:param name="ico"/>
		<lodares:zivnost rdf:resource="{f:icoBasedURI($ico,concat('zivnost/',d:vznik, '/', d:s, '/', d:druh, '/', encode-for-uri(normalize-space(d:pp))))}"/>
	</xsl:template>

	<xsl:template match="d:vznik">
		<!-- Živnosti: vznik-->
		<xsl:param name="ico"/>
		<dcterms:created rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="./text()"/></dcterms:created>
	</xsl:template>

	<xsl:template match="d:zanik">
		<!-- Živnosti: vznik-->
		<xsl:param name="ico"/>
		<lodares:zanik rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="./text()"/></lodares:zanik>
	</xsl:template>

	<xsl:template match="d:s">
		<!-- Živnosti: vznik-->
		<xsl:param name="ico"/>
        <xsl:variable name="schemePath">concept-scheme/stavy-zivnosti</xsl:variable>
		<lodares:stav-zivnosti rdf:resource="{f:pathIdURI($schemePath, ./text())}"/>
	</xsl:template>
	
	<xsl:template match="d:pry">
		<!-- Živnosti: provozovny-->
		<xsl:param name="ico"/>
		<xsl:apply-templates>
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:pry" mode="linked">
		<!-- Živnosti: provozovny-->
		<xsl:param name="ico"/>
		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:pr">
		<!-- Živnosti: provozovna-->
		<xsl:param name="ico"/>
		<schema:hasPOS rdf:resource="{f:icoBasedURI($ico,concat('provozovna/',d:icp))}"/>
	</xsl:template>

	<xsl:template match="d:pr" mode="linked">
		<!-- Živnosti: provozovna-->
		<xsl:param name="ico"/>
		<schema:Place rdf:about="{f:icoBasedURI($ico,concat('provozovna/',d:icp))}">
			<xsl:apply-templates>
				<xsl:with-param name="ico" select="$ico"/>
				<xsl:with-param name="icp" select="d:icp" tunnel="yes"/>
			</xsl:apply-templates>
		</schema:Place>
		
		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
			<xsl:with-param name="icp" select="d:icp" tunnel="yes"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:zahajeni">
		<!-- Živnosti: provozovna - zahajeni cinnosti-->
		<xsl:param name="ico"/>
		<dcterms:created rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="./text()"/></dcterms:created>
	</xsl:template>

	<xsl:template match="d:npr">
		<!-- Živnosti: provozovna - název-->
		<xsl:param name="ico"/>
		<dcterms:title><xsl:value-of select="./text()"/></dcterms:title>
	</xsl:template>

	<xsl:template match="d:typ_provozovny">
		<!-- Živnosti: provozovna - typ-->
		<xsl:param name="ico"/>
        <xsl:variable name="schemePath">concept-scheme/typy-provozoven</xsl:variable>
		<lodares:typ-provozovny rdf:resource="{f:pathIdURI($schemePath, ./text())}"/>
	</xsl:template>

	<xsl:template match="d:icp">
		<!-- Živnosti: provozovna - icp-->
		<xsl:param name="ico"/>
		<skos:notation><xsl:value-of select="./text()"/></skos:notation>
	</xsl:template>

	<xsl:template match="d:ap">
		<!-- Živnosti: provozovna - adresa-->
		<xsl:param name="ico"/>
		<xsl:param name="icp" tunnel="yes"/>
		<schema:address rdf:resource="{f:icoBasedURI($ico,concat('provozovna/',$icp, '/adresa'))}"/>
	</xsl:template>

	<xsl:template match="d:obory_cinnosti">
		<!-- Živnosti: provozovna - obory činnosti-->
		<xsl:param name="ico"/>
			<xsl:apply-templates>
				<xsl:with-param name="ico" select="$ico"/>
			</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:obor_cinnosti">
		<!-- Živnosti: provozovna - obory činnosti-->
		<xsl:param name="ico"/>
        <xsl:variable name="schemePath">concept-scheme/organization-activities</xsl:variable>
        <rov:orgActivity rdf:resource="{f:pathIdURI($schemePath, normalize-space(d:t/text()))}"/>
	</xsl:template>

    <xsl:template mode="linked" match="d:obor_cinnosti">
        <!-- Obor činnosti -->
        <xsl:param name="ico" tunnel="yes"/>
        <xsl:variable name="schemePath">concept-scheme/organization-activities</xsl:variable>
        <skos:Concept rdf:about="{f:pathIdURI($schemePath, normalize-space(d:t/text()))}">
            <skos:inScheme rdf:resource="{f:pathURI($schemePath)}"/>
            <skos:prefLabel><xsl:value-of select="normalize-space(d:t/text())"/></skos:prefLabel>
        </skos:Concept>
    </xsl:template>

	<xsl:template match="d:obory_cinnosti" mode="linked">
		<!-- Živnosti: provozovna - obory činnosti-->
		<xsl:param name="ico"/>
			<xsl:apply-templates mode="linked">
				<xsl:with-param name="ico" select="$ico"/>
			</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:ap" mode="linked">
		<!-- Živnosti: provozovna - adresa-->
		<xsl:param name="ico"/>
		<xsl:param name="icp" tunnel="yes"/>
		<schema:PostalAddress rdf:about="{f:icoBasedURI($ico,concat('provozovna/',$icp, '/adresa'))}">
			<schema:streetAddress><xsl:value-of select="
			concat(
				normalize-space(d:nu/text()), 
				if (d:ca) then 
					concat(if (d:nu) then ' ' else '', 
							normalize-space(d:ca/text())
							) 
				else '',
				if (d:cd) then 
						concat(if (d:nu) then ' ' else '', 
								normalize-space(d:cd/text()),
								if (d:co) then 
									concat('/',
												normalize-space(d:co/text())
											) 
								else ''
								)
				else ''
			)
			"/></schema:streetAddress>
			<xsl:if test="d:nok"><schema:addressRegion><xsl:value-of select="normalize-space(d:nok/text())"/></schema:addressRegion></xsl:if>
			<schema:addressLocality><xsl:value-of select="
				concat( normalize-space(d:n/text()), if (d:nco and d:nco != d:n) then concat(' - ', d:nco) else '')
				"/></schema:addressLocality>
			<xsl:if test="d:zahr_psc">
				<schema:postalCode><xsl:value-of select="normalize-space(d:zahr_psc/text())"/></schema:postalCode>
			</xsl:if>
			<xsl:if test="d:psc">
				<schema:postalCode><xsl:value-of select="normalize-space(d:psc/text())"/></schema:postalCode>
			</xsl:if>
			<xsl:if test="d:ns">
				<schema:addressCountry><xsl:value-of select="normalize-space(d:ns/text())"/></schema:addressCountry>
			</xsl:if>		
			<lodares:id_adresy><xsl:value-of select="d:ida"/></lodares:id_adresy>
		</schema:PostalAddress>
	</xsl:template>

	<xsl:template match="d:typ_provozovny" mode="linked">
		<!-- Živnosti: provozovna - typ-->
		<xsl:param name="ico"/>
        <xsl:variable name="schemePath">concept-scheme/typy-provozoven</xsl:variable>

        <skos:Concept rdf:about="{f:pathIdURI($schemePath, ./text())}">
            <skos:inScheme rdf:resource="{f:pathURI($schemePath)}"/>
            <skos:prefLabel xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel>
        </skos:Concept>    

	</xsl:template>

	<xsl:template match="d:s" mode="linked">
		<!-- Předmět podnikání -->
		<xsl:param name="ico"/>
        <xsl:variable name="schemePath">concept-scheme/stavy-zivnosti</xsl:variable>

        <skos:Concept rdf:about="{f:pathIdURI($schemePath, ./text())}">
            <skos:inScheme rdf:resource="{f:pathURI($schemePath)}"/>
            <skos:prefLabel xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel>
        </skos:Concept>    

	</xsl:template>

	<xsl:template match="d:druh" mode="linked">
		<!-- Předmět podnikání -->
		<xsl:param name="ico"/>
        <xsl:variable name="schemePath">concept-scheme/druhy-zivnosti</xsl:variable>

        <skos:Concept rdf:about="{f:pathIdURI($schemePath, ./text())}">
            <skos:inScheme rdf:resource="{f:pathURI($schemePath)}"/>
            <skos:prefLabel xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel>
        </skos:Concept>    

	</xsl:template>

	<xsl:template match="d:druh">
		<!-- Živnosti: vznik-->
		<xsl:param name="ico"/>
        <xsl:variable name="schemePath">concept-scheme/druhy-zivnosti</xsl:variable>
		<lodares:druh-zivnosti rdf:resource="{f:pathIdURI($schemePath, ./text())}"/>
	</xsl:template>
	
	<xsl:template match="d:pp">
		<!-- Živnosti -->
		<xsl:param name="ico"/>
        <xsl:variable name="schemePath">concept-scheme/predmety-podnikani</xsl:variable>
		<lodares:predmet-podnikani rdf:resource="{f:pathIdURI($schemePath, ./text())}"/>
	</xsl:template>

	<xsl:template match="d:pp" mode="linked">
		<!-- Předmět podnikání -->
		<xsl:param name="ico"/>
        <xsl:variable name="schemePath">concept-scheme/predmety-podnikani</xsl:variable>

        <skos:Concept rdf:about="{f:pathIdURI($schemePath, ./text())}">
            <skos:inScheme rdf:resource="{f:pathURI($schemePath)}"/>
            <skos:prefLabel xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel>
        </skos:Concept>    

	</xsl:template>

    <xsl:template match="d:t[parent::d:pp]">
        <!-- Obor činnosti -->
        <xsl:param name="ico" tunnel="yes"/>
        <xsl:variable name="schemePath">concept-scheme/organization-activities</xsl:variable>
        <rov:orgActivity rdf:resource="{f:pathIdURI($schemePath, normalize-space(./text()))}"/>
    </xsl:template>
    
    <xsl:template match="d:pf">
        <!-- Právní forma -->
        <rov:orgType rdf:resource="{f:pathIdURI('concept-scheme/organization-types', normalize-space(d:kpf/text()))}"/>    
    </xsl:template>

    <xsl:template mode="linked" match="d:pf">
        <!-- Právní forma -->
        <xsl:variable name="schemePath">concept-scheme/organization-types</xsl:variable>
        <skos:Concept rdf:about="{f:pathIdURI($schemePath, d:kpf/text())}">
            <skos:inScheme rdf:resource="{f:pathURI($schemePath)}"/>
            <skos:prefLabel xml:lang="cs"><xsl:value-of select="normalize-space(d:npf/text())"/></skos:prefLabel>
			<skos:notation><xsl:value-of select="normalize-space(d:kpf/text())"/></skos:notation>
        </skos:Concept>    
    </xsl:template>

    <!-- Templates for linked resources -->
    
    <xsl:template mode="linked" match="d:pfo">
        <!-- Právní forma -->
        <xsl:variable name="schemePath">concept-scheme/organization-types</xsl:variable>
        <skos:Concept rdf:about="{f:pathIdURI($schemePath, normalize-space(d:kpf/text()))}">
            <skos:inScheme rdf:resource="{f:pathURI($schemePath)}"/>
            <xsl:apply-templates mode="linked"/>
        </skos:Concept>    
    </xsl:template>
    
    <xsl:template mode="linked" match="d:kpf">
        <!-- Kód právní formy -->
        <skos:notation><xsl:value-of select="normalize-space(./text())"/></skos:notation>
    </xsl:template>
    
    <xsl:template mode="linked" match="d:npf">
        <!-- Název právní formy -->
        <skos:prefLabel xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel> 
    </xsl:template>
    
    <!-- Catch-all empty template -->
    <xsl:template mode="#all" match="*|text()|@*"/>
    
</xsl:stylesheet>