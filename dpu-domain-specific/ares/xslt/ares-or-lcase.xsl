<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:are="http://wwwinfo.mfcr.cz/ares/xml_doc/schemas/ares/ares_answer_or/v_1.0.3"
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
    
	<xsl:function name="f:getClenJmenoURIpart" as="xs:string">
		<xsl:param name="context" as="node()"/>
		<xsl:if test="$context/d:fo">
			<xsl:value-of select="encode-for-uri(
						lower-case(
							concat(
								if ($context/d:fo/d:tp) then 
									concat(normalize-space($context/d:fo/d:tp/text()), '-') 
								else '', 
								if ($context/d:fo/d:ot) then 
									normalize-space($context/d:fo/d:ot) 
								else 
									concat(
										normalize-space($context/d:fo/d:p/text()),
										'-', 
										normalize-space($context/d:fo/d:j/text())
									),
								if ($context/d:fo/d:tz) then 
									concat('-', normalize-space($context/d:fo/d:tz/text())) 
								else ''
								)
							)
						)
			"/>
		</xsl:if>
		<xsl:if test="$context/d:po/d:ico">
			<xsl:value-of select="normalize-space($context/d:po/d:ico/text())"/>
		</xsl:if>
		<xsl:if test="$context/d:po and not ($context/d:po/d:ico)">
			<xsl:value-of select="encode-for-uri(lower-case(normalize-space($context/d:po/d:of/text())))"/>
		</xsl:if>
	</xsl:function>

	<xsl:function name="f:getClenURI" as="xs:anyURI">
		<xsl:param name="context" as="node()"/>
		<xsl:if test="$context/d:fo">
			<xsl:value-of select="f:pathIdURI(
					concat('person',
					if ($context/d:fo/d:dn) then concat('/',normalize-space($context/d:fo/d:dn/text())) else ''), 
					f:getClenJmenoURIpart($context))"/>
		</xsl:if>
		<xsl:if test="$context/d:po">
			<xsl:value-of select="f:pathIdURI(
					'business-entity',
					f:getClenJmenoURIpart($context))"/>
		</xsl:if>
	</xsl:function>
    
	<xsl:function name="f:fixPrice" as="xs:string">
		<xsl:param name="price" as="xs:string"/>
		<xsl:value-of select="replace(normalize-space(replace(replace(replace($price,';00',''),'Kč',''),'.-','')),',','.')"/>
	</xsl:function>

	<xsl:function name="f:getClenTitle" as="xs:string">
		<xsl:param name="context" as="node()"/>
		<xsl:if test="$context/d:fo">
			<xsl:value-of select="
							concat(
								if ($context/d:fo/d:tp) then 
									concat(normalize-space($context/d:fo/d:tp/text()),' ') 
								else '', 
								if ($context/d:fo/d:ot) then 
									normalize-space($context/d:fo/d:ot) 
								else 
									concat(
										normalize-space($context/d:fo/d:p/text()),
										' ', 
										normalize-space($context/d:fo/d:j/text())
									),
								if ($context/d:fo/d:tz) then 
									concat(normalize-space($context/d:fo/d:tz/text()),' ') 
								else ''
								)
			"/>
		</xsl:if>
		<xsl:if test="$context/d:po/d:ico">
			<xsl:value-of select="concat(normalize-space($context/d:po/d:ico/text()), ' - ', normalize-space($context/d:po/d:of/text()))"/>
		</xsl:if>
		<xsl:if test="$context/d:po and not ($context/d:po/d:ico)">
			<xsl:value-of select="normalize-space($context/d:po/d:of/text())"/>
		</xsl:if>
	</xsl:function>

	<xsl:function name="f:getClenstviVPredstavenstvuURI" as="xs:anyURI">
		<xsl:param name="ico" as="xs:string"/>
		<xsl:param name="context" as="node()"/>
		<xsl:value-of select="f:getClenstviURI($ico, 'clenstvi-v-predstavenstvu', $context)"/>
	</xsl:function>

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

	<xsl:function name="f:getProkuraURI" as="xs:anyURI">
		<xsl:param name="ico" as="xs:string"/>
		<xsl:param name="context" as="node()"/>
		<xsl:value-of select="f:icoBasedDomainURI($ico,'prokura')"/>
	</xsl:function>

	<xsl:function name="f:getClenstviURI" as="xs:anyURI">
		<xsl:param name="ico" as="xs:string"/>
		<xsl:param name="clenstvi" as="xs:string"/>
		<xsl:param name="context" as="node()"/>
		<xsl:value-of select="f:icoBasedDomainURI(
				concat($ico,
							'/',$clenstvi,
							if ($context/d:fo) then concat('/',normalize-space($context/d:fo/d:dn/text())) else ''),  
							f:getClenJmenoURIpart($context)
						)"/>
	</xsl:function>

	<xsl:function name="f:getClenstviVDozorciRadeURI" as="xs:anyURI">
		<xsl:param name="ico" as="xs:string"/>
		<xsl:param name="context" as="node()"/>
		<xsl:value-of select="f:getClenstviURI($ico, 'clenstvi-v-dozorci-rade', $context)"/>
	</xsl:function>

	<xsl:function name="f:getClenstviVStatutarnimOrganuURI" as="xs:anyURI">
		<xsl:param name="ico" as="xs:string"/>
		<xsl:param name="context" as="node()"/>
		<xsl:value-of select="f:getClenstviURI($ico, 'clenstvi-v-statutarnim-organu', $context)"/>
	</xsl:function>

	<xsl:function name="f:getAkcionarstviURI" as="xs:anyURI">
		<xsl:param name="ico" as="xs:string"/>
		<xsl:param name="context" as="node()"/>
		<xsl:value-of select="f:getClenstviURI($ico, 'akcionarstvi', $context)"/>
	</xsl:function>

	<xsl:function name="f:getSpolecnictviSVklademURI" as="xs:anyURI">
		<xsl:param name="ico" as="xs:string"/>
		<xsl:param name="context" as="node()"/>
		<xsl:value-of select="f:getClenstviURI($ico, 'spolecnictvi-s-vkladem', $context)"/>
	</xsl:function>

	<xsl:function name="f:getZastavaniFunkceVPredstavenstvuURI" as="xs:anyURI">
		<xsl:param name="ico" as="xs:string"/>
		<xsl:param name="context" as="node()"/>
		<xsl:value-of select="f:getZastavaniFunkceURI($ico, 'zastavani-funkce-v-predstavenstvu', $context)"/>
	</xsl:function>

	<xsl:function name="f:getZastavaniFunkceVDozorciRadeURI" as="xs:anyURI">
		<xsl:param name="ico" as="xs:string"/>
		<xsl:param name="context" as="node()"/>
		<xsl:value-of select="f:getZastavaniFunkceURI($ico, 'zastavani-funkce-v-dozorci-rade', $context)"/>
	</xsl:function>

	<xsl:function name="f:getVkladSpolecnikaURI" as="xs:anyURI">
		<xsl:param name="ico" as="xs:string"/>
		<xsl:param name="context" as="node()"/>
		<xsl:value-of select="f:getZastavaniFunkceURI($ico, 'vklad-spolecnika', $context)"/>
	</xsl:function>

	<xsl:function name="f:getZastavaniFunkceVStatutarnimOrganuURI" as="xs:anyURI">
		<xsl:param name="ico" as="xs:string"/>
		<xsl:param name="context" as="node()"/>
		<xsl:value-of select="f:getZastavaniFunkceURI($ico, 'zastavani-funkce-v-statutarnim-organu', $context)"/>
	</xsl:function>

	<xsl:function name="f:getZastavaniFunkceURI" as="xs:anyURI">
		<xsl:param name="ico" as="xs:string"/>
		<xsl:param name="funkce" as="xs:string"/>
		<xsl:param name="context" as="node()"/>
		<xsl:value-of select="f:icoBasedDomainURI(
				concat($ico,
							'/',$funkce,'/', 
							if ($context/d:fo) then normalize-space($context/d:fo/d:dn/text()) else ''),  
							concat(
								f:getClenJmenoURIpart($context), 
								if ($context/d:vf/d:dza) then 
									concat('/', normalize-space($context/d:vf/d:dza/text())) 
								else ''
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
    
	<xsl:template match="d:vypis_or">
		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="normalize-space(d:zau/d:ico/text())"/>		
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:zau" mode="linked">
		<!-- Základní údaje -->
        <xsl:param name="ico"/>
        <gr:BusinessEntity rdf:about="{concat($beURIprefix, 'CZ', normalize-space($ico))}">
            <dcterms:valid rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="normalize-space(d:pod)"/></dcterms:valid>
            <rdf:type rdf:resource="http://www.w3.org/ns/regorg#RegisteredOrganization"/>
            <adms:identifier rdf:resource="{f:icoBasedURI($ico,concat('identifier/',$ico))}"/>
            <gr:legalName><xsl:value-of select="normalize-space(d:of)"/></gr:legalName>
			<xsl:if test="d:dzor">
				<dcterms:issued rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="normalize-space(d:dzor)"/></dcterms:issued>
			</xsl:if>
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

	<xsl:template match="d:reg">
		<!-- Registrace -->
		<xsl:param name="ico"/>
		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:cin" mode="linked">
		<!-- Činnosti -->
		<xsl:param name="ico"/>
		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:cin">
		<!-- Činnosti -->
		<xsl:param name="ico"/>
		<xsl:apply-templates>
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:osk">
		<!-- Činnosti -->
		<xsl:param name="ico"/>
		<xsl:apply-templates>
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:kap">
		<!-- Kapitál -->
		<xsl:param name="ico"/>
        <lodares:zakladni-kapital rdf:resource="{f:icoBasedDomainURI($ico, 'zakladni-kapital')}"/>
        <xsl:if test="d:za/d:spl/d:kc and not(contains(d:za/d:spl/d:kc/text(), '%'))">
			<lodares:zakladni-kapital-splaceno rdf:resource="{f:icoBasedDomainURI($ico, 'zakladni-kapital-splaceno')}"/>
        </xsl:if>
        <xsl:if test="d:za/d:spl/d:kc and contains(d:za/d:spl/d:kc/text(), '%')">
			<lodares:zakladni-kapital-splaceno-procent rdf:datatype="http://www.w3.org/2001/XMLSchema#nonNegativeInteger"><xsl:value-of select="normalize-space(d:za/d:spl/d:kc//text())"/></lodares:zakladni-kapital-splaceno-procent>
        </xsl:if>
        <xsl:if test="d:za/d:spl/d:prc">
			<lodares:zakladni-kapital-splaceno-procent rdf:datatype="http://www.w3.org/2001/XMLSchema#nonNegativeInteger"><xsl:value-of select="normalize-space(d:za/d:spl/d:prc/text())"/></lodares:zakladni-kapital-splaceno-procent>
        </xsl:if>
        <xsl:if test="d:akcie">
			<xsl:for-each select="d:akcie/d:em">
				<lodares:emise rdf:resource="{f:icoBasedDomainURI($ico, concat('emise/',count(./preceding-sibling::*)+1))}"/>
			</xsl:for-each>
        </xsl:if>
	</xsl:template>

	<xsl:template match="d:kap" mode="linked">
		<!-- Kapitál -->
		<xsl:param name="ico"/>
		<xsl:if test="d:za/d:vk/d:kc/text()">
			<gr:PriceSpecification rdf:about="{f:icoBasedDomainURI($ico, 'zakladni-kapital')}">
				<gr:hasCurrency>CZK</gr:hasCurrency>
				<gr:hasCurrencyValue rdf:datatype="http://www.w3.org/2001/XMLSchema#decimal"><xsl:value-of select="f:fixPrice(d:za/d:vk/d:kc/text())"/></gr:hasCurrencyValue>
			</gr:PriceSpecification>
		</xsl:if>
		<xsl:if test="d:kj/d:vk/d:kc/text()">
			<gr:PriceSpecification rdf:about="{f:icoBasedDomainURI($ico, 'zakladni-kapital')}">
				<gr:hasCurrency>CZK</gr:hasCurrency>
				<gr:hasCurrencyValue rdf:datatype="http://www.w3.org/2001/XMLSchema#decimal"><xsl:value-of select="f:fixPrice(d:kj/d:vk/d:kc/text())"/></gr:hasCurrencyValue>
			</gr:PriceSpecification>
		</xsl:if>
        <xsl:if test="d:za/d:spl/d:kc and not(contains(d:za/d:spl/d:kc/text, '%'))">
			<gr:PriceSpecification rdf:about="{f:icoBasedDomainURI($ico, 'zakladni-kapital-splaceno')}">
				<gr:hasCurrency>CZK</gr:hasCurrency>
				<gr:hasCurrencyValue rdf:datatype="http://www.w3.org/2001/XMLSchema#decimal"><xsl:value-of select="f:fixPrice(d:za/d:spl/d:kc/text())"/></gr:hasCurrencyValue>
			</gr:PriceSpecification>
        </xsl:if>
        <xsl:if test="d:akcie">
			<xsl:for-each select="d:akcie/d:em">
				<lodares:Emise rdf:about="{f:icoBasedDomainURI($ico, concat('emise/',count(./preceding-sibling::*)+1))}">
					<lodares:druh-akcie rdf:resource="{f:pathIdURI($druhAkcieScheme, normalize-space(d:da/text()))}"/>
					<xsl:if test="d:pd"><lodares:podoba-akcie rdf:resource="{f:pathIdURI($podobaAkcieScheme, normalize-space(d:pd/text()))}"/></xsl:if>
					<lodares:hodnota-akcie rdf:resource="{f:icoBasedDomainURI($ico, concat('emise/',count(./preceding-sibling::*)+1,'/hodnota'))}"/>
					<lodares:pocet-akcii rdf:datatype="http://www.w3.org/2001/XMLSchema#integer"><xsl:value-of select="normalize-space(d:pocet/text())"/></lodares:pocet-akcii>
				</lodares:Emise>
			</xsl:for-each>
        </xsl:if>
        <xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
        </xsl:apply-templates>
	</xsl:template>
	
    <xsl:template mode="linked" match="d:akcie">
		<xsl:param name="ico"/>
		<xsl:for-each select="d:em">
			<gr:PriceSpecification rdf:about="{f:icoBasedDomainURI($ico, concat('emise/',count(./preceding-sibling::*)+1,'/hodnota'))}">
				<gr:hasCurrency>CZK</gr:hasCurrency>
				<gr:hasCurrencyValue rdf:datatype="http://www.w3.org/2001/XMLSchema#decimal"><xsl:value-of select="f:fixPrice(d:h/text())"/></gr:hasCurrencyValue>
			</gr:PriceSpecification>
		</xsl:for-each>
		<xsl:apply-templates select="d:em/*" mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="linked" match="d:pd">
        <!-- Právní forma -->
        <skos:Concept rdf:about="{f:pathIdURI($podobaAkcieScheme, normalize-space(./text()))}">
            <skos:inScheme rdf:resource="{f:pathURI($podobaAkcieScheme)}"/>
            <skos:prefLabel><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel>
        </skos:Concept>    
    </xsl:template>

    <xsl:template mode="linked" match="d:da">
        <!-- Právní forma -->
        <skos:Concept rdf:about="{f:pathIdURI($druhAkcieScheme, normalize-space(./text()))}">
            <skos:inScheme rdf:resource="{f:pathURI($druhAkcieScheme)}"/>
            <skos:prefLabel><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel>
        </skos:Concept>    
    </xsl:template>

	<xsl:template match="d:sop">
		<xsl:param name="ico"/>
		<!-- Statutární orgán - představenstvo -->
		<lodares:predstavenstvo rdf:resource="{f:icoBasedDomainURI($ico, 'predstavenstvo')}"/>
	</xsl:template>

	<xsl:template match="d:sop" mode="linked">
		<xsl:param name="ico"/>
		<!-- Statutární orgán - představenstvo -->
		<lodares:Predstavenstvo rdf:about="{f:icoBasedDomainURI($ico, 'predstavenstvo')}">
			<dcterms:title><xsl:value-of select="concat(normalize-space(../d:zau/d:of/text()),' - Představenstvo')"/></dcterms:title>
			<xsl:for-each select="d:csp">
				<lodares:clen-predstavenstva rdf:resource="{f:getClenstviVPredstavenstvuURI($ico,d:c)}"/>
			</xsl:for-each>
			<xsl:for-each select="d:t">
				<dcterms:description xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></dcterms:description>
			</xsl:for-each>
		</lodares:Predstavenstvo>

		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:so" mode="linked">
		<xsl:param name="ico"/>
		<!-- Statutární orgán -->
		<lodares:StatutarniOrgan rdf:about="{f:icoBasedDomainURI($ico, 'statutarni-organ')}">
			<dcterms:title><xsl:value-of select="concat(normalize-space(../d:zau/d:of/text()),' - Statutární orgán')"/></dcterms:title>
			<xsl:for-each select="d:cso">
				<lodares:clen-statutarniho-organu rdf:resource="{f:getClenstviVStatutarnimOrganuURI($ico,d:c)}"/>
			</xsl:for-each>
			<xsl:for-each select="d:t">
				<dcterms:description xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></dcterms:description>
			</xsl:for-each>
		</lodares:StatutarniOrgan>

		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:aki" mode="linked">
		<xsl:param name="ico"/>
		<!-- Akcionáři -->
		<lodares:Akcionari rdf:about="{f:icoBasedDomainURI($ico, 'akcionari')}">
			<dcterms:title><xsl:value-of select="concat(normalize-space(../d:zau/d:of/text()),' - Akcionáři')"/></dcterms:title>
			<xsl:for-each select="d:akr">
				<lodares:akcionarstvi rdf:resource="{f:getAkcionarstviURI($ico,.)}"/>
			</xsl:for-each>
		</lodares:Akcionari>

		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:dr">
		<xsl:param name="ico"/>
		<!-- Dozorčí rada -->
		<lodares:dozorci-rada rdf:resource="{f:icoBasedDomainURI($ico, 'dozorci-rada')}"/>
	</xsl:template>

	<xsl:template match="d:so">
		<xsl:param name="ico"/>
		<!-- Statutární orgán -->
		<lodares:statutarni-organ rdf:resource="{f:icoBasedDomainURI($ico, 'statutarni-organ')}"/>
	</xsl:template>

	<xsl:template match="d:aki">
		<xsl:param name="ico"/>
		<!-- Akcionáři -->
		<lodares:akcionari rdf:resource="{f:icoBasedDomainURI($ico, 'akcionari')}"/>
	</xsl:template>

	<xsl:template match="d:akr" mode="linked">
		<xsl:param name="ico"/>
		<!-- Akcionář -->
		<xsl:variable name="currentURI" select="f:getAkcionarstviURI($ico,.)"/>
		<lodares:Akcionarstvi rdf:about="{$currentURI}">
			<dcterms:title><xsl:value-of select="f:getClenTitle(.)"/></dcterms:title>
			<xsl:if test="d:po">
				<lodares:akcionar rdf:resource="{f:getPOURI(d:po)}"/>
			</xsl:if>
			<xsl:if test="d:fo">
				<lodares:akcionar rdf:resource="{f:getClenURI(.)}"/>
			</xsl:if>
			<xsl:for-each select="d:t">
				<dcterms:description xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></dcterms:description>
			</xsl:for-each>
		</lodares:Akcionarstvi>

		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:ssv">
		<xsl:param name="ico"/>
		<!-- Statutární orgán -->
		<lodares:spolecnici-s-vkladem rdf:resource="{f:icoBasedDomainURI($ico, 'spolecnici-s-vkladem')}"/>
	</xsl:template>

	<xsl:template match="d:dr" mode="linked">
		<xsl:param name="ico"/>
		<!-- Dozorčí rada -->
		<lodares:DozorciRada rdf:about="{f:icoBasedDomainURI($ico, 'dozorci-rada')}">
		<dcterms:title><xsl:value-of select="concat(normalize-space(../d:zau/d:of/text()),' - Dozorčí rada')"/></dcterms:title>
			<xsl:for-each select="d:cdr">
				<lodares:clen-dozorci-rady rdf:resource="{f:getClenstviVDozorciRadeURI($ico,d:c)}"/>
			</xsl:for-each>
			<xsl:for-each select="d:t">
				<dcterms:description xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></dcterms:description>
			</xsl:for-each>
		</lodares:DozorciRada>

		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:ssv" mode="linked">
		<xsl:param name="ico"/>
		<!-- Společníci s vkladem -->
		<lodares:SpolecniciSVkladem rdf:about="{f:icoBasedDomainURI($ico, 'spolecnici-s-vkladem')}">
		<dcterms:title><xsl:value-of select="concat(normalize-space(../d:zau/d:of/text()),' - Společníci s vkladem')"/></dcterms:title>
			<xsl:for-each select="d:ss">
				<lodares:spolecnictvi-s-vkladem rdf:resource="{f:getSpolecnictviSVklademURI($ico,.)}"/>
			</xsl:for-each>
		</lodares:SpolecniciSVkladem>

		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:ss" mode="linked">
		<xsl:param name="ico"/>
		<!-- Společnictví s vkladem -->
		<xsl:variable name="currentURI" select="f:getSpolecnictviSVklademURI($ico,.)"/>
		<lodares:SpolecnictviSVkladem rdf:about="{$currentURI}">
			<dcterms:title><xsl:value-of select="f:getClenTitle(.)"/></dcterms:title>
			<xsl:if test="d:fo">
				<lodares:spolecnik-s-vkladem rdf:resource="{f:getClenURI(.)}"/>
			</xsl:if>
			<xsl:if test="d:po">
				<lodares:spolecnik-s-vkladem rdf:resource="{f:getPOURI(d:po)}"/>
			</xsl:if>
			<lodares:vklad rdf:resource="{$currentURI}/vklad"/>

			<xsl:if test="d:vks/d:spl/d:kc and not(contains(d:vks/d:spl/d:kc/text(), '%'))">
				<lodares:splaceno rdf:resource="{$currentURI}/splaceno"/>
			</xsl:if>
			<xsl:if test="d:vks/d:spl/d:kc and contains(d:vks/d:spl/d:kc/text(), '%')">
				<lodares:splaceno-procent rdf:datatype="http://www.w3.org/2001/XMLSchema#nonNegativeInteger"><xsl:value-of select="replace(replace(normalize-space(d:vks/d:spl/d:kc/text()),'%',''),',','.')"/></lodares:splaceno-procent>
			</xsl:if>
			<xsl:if test="d:vks/d:spl/d:prc">
				<lodares:splaceno-procent rdf:datatype="http://www.w3.org/2001/XMLSchema#decimal"><xsl:value-of select="replace(replace(normalize-space(d:vks/d:spl/d:prc/text()),'%',''),',','.')"/></lodares:splaceno-procent>
			</xsl:if>

			<lodares:obchodni-podil-spolecnika><xsl:value-of select="normalize-space(d:vks/d:op/d:t/text())"/></lodares:obchodni-podil-spolecnika>
		</lodares:SpolecnictviSVkladem>

		<xsl:if test="d:vks/d:vk/d:kc/text()">
			<gr:PriceSpecification rdf:about="{$currentURI}/vklad">
				<gr:hasCurrency>CZK</gr:hasCurrency>
				<gr:hasCurrencyValue rdf:datatype="http://www.w3.org/2001/XMLSchema#decimal"><xsl:value-of select="f:fixPrice(d:vks/d:vk/d:kc/text())"/></gr:hasCurrencyValue>
			</gr:PriceSpecification>
		</xsl:if>
		<xsl:if test="d:vks/d:spl/d:kc and not(contains(d:vks/d:spl/d:kc/text(), '%'))">
			<gr:PriceSpecification rdf:about="{$currentURI}/splaceno">
				<gr:hasCurrency>CZK</gr:hasCurrency>
				<gr:hasCurrencyValue rdf:datatype="http://www.w3.org/2001/XMLSchema#decimal"><xsl:value-of select="replace(normalize-space(d:vks/d:spl/d:kc/text()),',','.')"/></gr:hasCurrencyValue>
			</gr:PriceSpecification>
		</xsl:if>

		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:cso" mode="linked">
		<xsl:param name="ico"/>
		<!-- Statutární orgán -->
		<lodares:ClenstviVStatutarnimOrganu rdf:about="{f:getClenstviVStatutarnimOrganuURI($ico, d:c)}">
			<dcterms:title><xsl:value-of select="f:getClenTitle(d:c)"/></dcterms:title>
			<lodares:clen-predstavenstva rdf:resource="{f:getClenURI(d:c)}"/>
			<xsl:if test="d:c/d:cle/d:dza/text()">
				<dcterms:issued rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="normalize-space(d:c/d:cle/d:dza/text())"/></dcterms:issued>
			</xsl:if>
			<lodares:zastavani-funkce-v-statutarnim-organu rdf:resource="{f:getZastavaniFunkceVStatutarnimOrganuURI($ico, d:c)}"/>
			<lodares:kod-angm rdf:resource="{f:pathIdURI($kodAngmScheme, normalize-space(d:c/d:kan/text()))}"/>
		</lodares:ClenstviVStatutarnimOrganu>

		<lodares:ZastavaniFunkceVStatutarnimOrganu rdf:about="{f:getZastavaniFunkceVStatutarnimOrganuURI($ico, d:c)}">
			<dcterms:title><xsl:value-of select="normalize-space(d:c/d:f/text())"/></dcterms:title>
			<xsl:if test="d:c/d:vf/d:dza/text()">
				<dcterms:issued rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="normalize-space(d:c/d:vf/d:dza/text())"/></dcterms:issued>
			</xsl:if>
			<lodares:funkce-v-statutarnim-organu rdf:resource="{f:pathIdURI($funkceVStatutarnimOrganuScheme, normalize-space(d:c/d:f/text()))}"/>
		</lodares:ZastavaniFunkceVStatutarnimOrganu>

		<xsl:apply-templates mode="linked" select="d:c/d:fo">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:csp" mode="linked">
		<xsl:param name="ico"/>
		<!-- Statutární orgán - představenstvo -->
		<lodares:ClenstviVPredstavenstvu rdf:about="{f:getClenstviVPredstavenstvuURI($ico, d:c)}">
			<dcterms:title><xsl:value-of select="f:getClenTitle(d:c)"/></dcterms:title>
			<lodares:clen-predstavenstva rdf:resource="{f:getClenURI(d:c)}"/>
			<xsl:if test="d:c/d:cle/d:dza/text()">
				<dcterms:issued rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="normalize-space(d:c/d:cle/d:dza/text())"/></dcterms:issued>
			</xsl:if>
			<lodares:zastavani-funkce-v-predstavenstvu rdf:resource="{f:getZastavaniFunkceVPredstavenstvuURI($ico, d:c)}"/>
			<lodares:kod-angm rdf:resource="{f:pathIdURI($kodAngmScheme, normalize-space(d:c/d:kan/text()))}"/>
		</lodares:ClenstviVPredstavenstvu>

		<lodares:ZastavaniFunkceVPredstavenstvu rdf:about="{f:getZastavaniFunkceVPredstavenstvuURI($ico, d:c)}">
			<dcterms:title><xsl:value-of select="normalize-space(d:c/d:f/text())"/></dcterms:title>
			<xsl:if test="d:c/d:vf/d:dza/text()">
				<dcterms:issued rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="normalize-space(d:c/d:vf/d:dza/text())"/></dcterms:issued>
			</xsl:if>
			<lodares:funkce-v-predstavenstvu rdf:resource="{f:pathIdURI($funkceVPredstavenstvuScheme, normalize-space(d:c/d:f/text()))}"/>
		</lodares:ZastavaniFunkceVPredstavenstvu>

		<xsl:apply-templates mode="linked" select="d:c/d:fo">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:cdr" mode="linked">
		<xsl:param name="ico"/>
		<!-- Dozorčí rada -->
		<lodares:ClenstviVDozorciRade rdf:about="{f:getClenstviVDozorciRadeURI($ico, d:c)}">
			<dcterms:title><xsl:value-of select="f:getClenTitle(d:c)"/></dcterms:title>
			<lodares:clen-v-dozorci-rade rdf:resource="{f:getClenURI(d:c)}"/>
			<xsl:if test="d:c/d:cle/d:dza/text()">
				<dcterms:issued rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="normalize-space(d:c/d:cle/d:dza/text())"/></dcterms:issued>
			</xsl:if>
			<lodares:zastavani-funkce-v-dozorci-rade rdf:resource="{f:getZastavaniFunkceVDozorciRadeURI($ico, d:c)}"/>
			<lodares:kod-angm rdf:resource="{f:pathIdURI($kodAngmScheme, normalize-space(d:c/d:kan/text()))}"/>
		</lodares:ClenstviVDozorciRade>

		<lodares:ZastavaniFunkceVDozorciRade rdf:about="{f:getZastavaniFunkceVDozorciRadeURI($ico, d:c)}">
			<dcterms:title><xsl:value-of select="normalize-space(d:c/d:f/text())"/></dcterms:title>
			<xsl:if test="d:c/d:vf/d:dza/text()">
				<dcterms:issued rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="normalize-space(d:c/d:vf/d:dza/text())"/></dcterms:issued>
			</xsl:if>
			<lodares:funkce-v-predstavenstvu rdf:resource="{f:pathIdURI($funkceVDozorciRadeScheme, normalize-space(d:c/d:f/text()))}"/>
		</lodares:ZastavaniFunkceVDozorciRade>

		<xsl:apply-templates mode="linked" select="d:c/d:fo">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

    <xsl:template mode="linked" match="d:po">
        <!-- právnická osoba - např. akcionář -->
		<xsl:param name="ico"/>
        <gr:BusinessEntity rdf:about="{f:getPOURI(.)}">
			<gr:legalName><xsl:value-of select="normalize-space(d:of/text())"/></gr:legalName>
			<xsl:if test="d:ico">
				<adms:identifier rdf:resource="{concat(f:getPOURI(.),'/identifier/', normalize-space(d:ico/text()))}"/>
			</xsl:if>
			<schema:address rdf:resource="{concat(f:getPOURI(.),'/hq-address')}"/>
        </gr:BusinessEntity>    

		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="linked" match="d:fo">
        <!-- Člen představenstva nebo dozorčí rady nebo prokurista nebo společník s vkladem-->
		<xsl:param name="ico"/>
        <foaf:Person rdf:about="{f:getClenURI(..)}">
			<xsl:if test="d:ot"><foaf:name><xsl:value-of select="normalize-space(d:ot/text())"/></foaf:name></xsl:if>
			<xsl:if test="d:p"><foaf:familyName><xsl:value-of select="normalize-space(d:p/text())"/></foaf:familyName></xsl:if>
			<xsl:if test="d:j"><foaf:givenName><xsl:value-of select="normalize-space(d:j/text())"/></foaf:givenName></xsl:if>
			<xsl:if test="d:dn"><foaf:dateOfBirth rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="normalize-space(d:dn/text())"/></foaf:dateOfBirth></xsl:if>
			<xsl:if test="d:tp">
				<foaf:title><xsl:value-of select="normalize-space(d:tp/text())"/></foaf:title>
			</xsl:if>
			<xsl:if test="d:tz">
				<foaf:title><xsl:value-of select="normalize-space(d:tz/text())"/></foaf:title>
			</xsl:if>
			<schema:address rdf:resource="{concat(f:getClenURI(..),'/address')}"/>
        </foaf:Person>    
		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="linked" match="d:b[not(parent::d:zau)]">
		<xsl:param name="ico"/>
		<schema:PostalAddress rdf:about="{concat(f:getClenURI(../..),'/address')}">
			<schema:streetAddress><xsl:value-of select="
			concat(
				normalize-space(d:nu/text()), 
				if (d:ca) then 
					concat(' ', 
							normalize-space(d:ca/text())
							) 
				else '',
				if (d:cd) then 
						concat(' ', 
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
			<schema:addressLocality><xsl:value-of select="normalize-space(d:nok/text())"/></schema:addressLocality>
			<schema:addressRegion><xsl:value-of select="normalize-space(d:n/text())"/></schema:addressRegion>
			<xsl:if test="d:zahr_psc">
				<schema:postalCode><xsl:value-of select="normalize-space(d:zahr_psc/text())"/></schema:postalCode>
			</xsl:if>
			<xsl:if test="d:psc">
				<schema:postalCode><xsl:value-of select="normalize-space(d:psc/text())"/></schema:postalCode>
			</xsl:if>
			<xsl:if test="d:ns">
				<schema:addressCountry><xsl:value-of select="normalize-space(d:ns/text())"/></schema:addressCountry>
			</xsl:if>
			<xsl:apply-templates mode="linked"/>
		</schema:PostalAddress>
    </xsl:template>


    <xsl:template mode="linked" match="d:f[ancestor::d:csp]">
        <!-- Funkce v představenstvu -->
        <skos:Concept rdf:about="{f:pathIdURI($funkceVPredstavenstvuScheme, normalize-space(./text()))}">
            <skos:inScheme rdf:resource="{f:pathURI($funkceVPredstavenstvuScheme)}"/>
            <skos:prefLabel><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel>
        </skos:Concept>    
    </xsl:template>

    <xsl:template mode="linked" match="d:kan[ancestor::d:csp]">
        <!-- Kod angm ??? v představenstvu -->
        <skos:Concept rdf:about="{f:pathIdURI($kodAngmScheme, normalize-space(./text()))}">
            <skos:inScheme rdf:resource="{f:pathURI($kodAngmScheme)}"/>
            <skos:prefLabel><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel>
        </skos:Concept>    
    </xsl:template>

    <xsl:template mode="linked" match="d:f[ancestor::d:cdr]">
        <!-- Funkce v dozorčí radě -->
        <skos:Concept rdf:about="{f:pathIdURI($funkceVDozorciRadeScheme, normalize-space(./text()))}">
            <skos:inScheme rdf:resource="{f:pathURI($funkceVDozorciRadeScheme)}"/>
            <skos:prefLabel><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel>
        </skos:Concept>    
    </xsl:template>

    <xsl:template mode="linked" match="d:kan[ancestor::d:cdr]">
        <!-- Kod angm ??? v dozorčí radě -->
        <skos:Concept rdf:about="{f:pathIdURI($kodAngmScheme, normalize-space(./text()))}">
            <skos:inScheme rdf:resource="{f:pathURI($kodAngmScheme)}"/>
            <skos:prefLabel><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel>
        </skos:Concept>    
    </xsl:template>

	<xsl:template match="d:pro" mode="linked">
		<!-- Prokura -->
		<xsl:param name="ico"/>
		<lodares:Prokura rdf:about="{f:getProkuraURI($ico, .)}">
			<xsl:apply-templates>
				<xsl:with-param name="ico" select="$ico"/>
			</xsl:apply-templates>
		</lodares:Prokura>
		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="d:pra">
		<!-- Prokurista -->
		<xsl:param name="ico"/>
		<lodares:prokurista rdf:resource="{f:getClenURI(.)}"/>
	</xsl:template>

	<xsl:template match="d:pro">
		<!-- Prokura -->
		<xsl:param name="ico"/>
		<xsl:if test="d:t">
			<lodares:prokura-text xml:lang="cs"><xsl:value-of select="normalize-space(d:t/text())"/></lodares:prokura-text>
		</xsl:if>
		<xsl:if test="not(d:t)">
			<xsl:for-each select="d:pra">
				<lodares:prokura rdf:resource="{f:getProkuraURI($ico, .)}"/>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>

	<xsl:template match="d:pra" mode="linked">
		<!-- Prokurista -->
		<xsl:param name="ico"/>
		<xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" select="$ico"/>
		</xsl:apply-templates>
	</xsl:template>

    <xsl:template match="d:pp">
        <!-- Obor činnosti -->
        <xsl:param name="ico" tunnel="yes"/>
        <xsl:apply-templates>
			<xsl:with-param name="ico" tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="d:pp" mode="linked">
        <!-- Obor činnosti -->
        <xsl:param name="ico" tunnel="yes"/>
        <xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="d:t[parent::d:pp]">
        <!-- Obor činnosti -->
        <xsl:param name="ico" tunnel="yes"/>
        <xsl:variable name="schemePath">concept-scheme/organization-activities</xsl:variable>
        <rov:orgActivity rdf:resource="{f:pathIdURI($schemePath, normalize-space(./text()))}"/>
    </xsl:template>
    
    <xsl:template match="d:t[parent::d:osk]">
        <!-- Obor činnosti -->
		<lodares:ostatni xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></lodares:ostatni>
    </xsl:template>

    <xsl:template match="d:pfo[d:pfo]">
        <!-- Právní forma -->
        <rov:orgType rdf:resource="{f:pathIdURI('concept-scheme/organization-types', normalize-space(d:kpf/text()))}"/>    
    </xsl:template>

    <xsl:template mode="linked" match="d:pfo[d:pfo]">
        <!-- Právní forma -->
        <xsl:variable name="schemePath">concept-scheme/organization-types</xsl:variable>
        <skos:Concept rdf:about="{f:pathIdURI($schemePath, d:kpf/text())}">
            <skos:inScheme rdf:resource="{f:pathURI($schemePath)}"/>
            <skos:prefLabel xml:lang="cs"><xsl:value-of select="normalize-space(d:npf/text())"/></skos:prefLabel>
			<skos:notation><xsl:value-of select="normalize-space(d:kpf/text())"/></skos:notation>
        </skos:Concept>    
    </xsl:template>

    <xsl:template match="d:psu">
        <!-- Interní příznaky subjektu -->
        <!-- Není blíže určeno ve XML schématu, hodnoty jako "NAAANNNNNNNNNNNNNNNNNNNNANNNNN" -->
    </xsl:template>
    
    <xsl:template match="d:s" mode="linked">
        <xsl:param name="ico" tunnel="yes"/>
        <xsl:apply-templates mode="linked">
			<xsl:with-param name="ico" tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="d:s">
        <xsl:param name="ico" tunnel="yes"/>
        <xsl:apply-templates>
			<xsl:with-param name="ico" tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="d:ssu">
        <!-- Stav subjektu -->
        <rov:orgStatus rdf:resource="{f:pathIdURI('concept-scheme/organization-statuses', normalize-space(./text()))}"/>
    </xsl:template>
    
    <xsl:template match="d:ssu" mode="linked">
        <!-- Stav subjektu -->
		<skos:Concept rdf:about="{f:pathIdURI('concept-scheme/organization-statuses', normalize-space(./text()))}">
			<skos:inScheme rdf:resource="{f:pathURI('concept-scheme/organization-statuses')}"/>
			<skos:prefLabel xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel>
		</skos:Concept>
    </xsl:template>

    <!-- Templates for linked resources -->
    
    <xsl:template mode="linked" match="d:t[parent::d:pp]">
        <!-- Obor činnosti -->
        <xsl:param name="ico" tunnel="yes"/>
        <xsl:variable name="schemePath">concept-scheme/organization-activities</xsl:variable>
        <skos:Concept rdf:about="{f:pathIdURI($schemePath, normalize-space(./text()))}">
            <skos:inScheme rdf:resource="{f:pathURI($schemePath)}"/>
            <skos:prefLabel><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel>
        </skos:Concept>
    </xsl:template>

    <xsl:template match="d:si">
        <xsl:param name="ico" tunnel="yes"/>
        <schema:address rdf:resource="{concat(f:getPOURI(..),'/hq-address')}"/>
    </xsl:template>

    <xsl:template mode="linked" match="d:si">
		<xsl:param name="ico"/>
		<schema:PostalAddress rdf:about="{concat(f:getPOURI(..),'/hq-address')}">
			<xsl:if test="d:nu">
				<schema:streetAddress><xsl:value-of select="
				concat(
					normalize-space(d:nu/text()), 
					if (d:ca) then 
						concat(' ', 
								normalize-space(d:ca/text())
								) 
					else '',
					if (d:cd) then 
							concat(' ', 
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
			</xsl:if>
			<xsl:if test="d:nok">
				<schema:addressLocality><xsl:value-of select="normalize-space(d:nok/text())"/></schema:addressLocality>
			</xsl:if>
			<xsl:if test="d:n">
				<schema:addressRegion><xsl:value-of select="normalize-space(d:n/text())"/></schema:addressRegion>
			</xsl:if>
			<xsl:if test="d:zahr_psc">
				<schema:postalCode><xsl:value-of select="normalize-space(d:zahr_psc/text())"/></schema:postalCode>
			</xsl:if>
			<xsl:if test="d:psc">
				<schema:postalCode><xsl:value-of select="normalize-space(d:psc/text())"/></schema:postalCode>
			</xsl:if>
			<xsl:if test="d:ns">
				<schema:addressCountry><xsl:value-of select="normalize-space(d:ns/text())"/></schema:addressCountry>
			</xsl:if>
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


    <xsl:template mode="linked" match="d:pfo[not(d:pfo)]">
        <!-- Právní forma -->
        <xsl:variable name="schemePath">concept-scheme/organization-types</xsl:variable>
        <skos:Concept rdf:about="{f:pathIdURI($schemePath, normalize-space(d:kpf/text()))}">
            <skos:inScheme rdf:resource="{f:pathURI($schemePath)}"/>
            <xsl:apply-templates mode="linked"/>
        </skos:Concept>    
    </xsl:template>
    
    <xsl:template mode="linked" match="d:kpf[parent::d:pfo]">
        <!-- Kód právní formy -->
        <skos:notation><xsl:value-of select="normalize-space(./text())"/></skos:notation>
    </xsl:template>
    
    <xsl:template mode="linked" match="d:npf[parent::d:pfo]">
        <!-- Název právní formy -->
        <skos:prefLabel xml:lang="cs"><xsl:value-of select="normalize-space(./text())"/></skos:prefLabel> 
    </xsl:template>
    
    <!-- Catch-all empty template -->
    <xsl:template mode="#all" match="*|text()|@*"/>
    
</xsl:stylesheet>