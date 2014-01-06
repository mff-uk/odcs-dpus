<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://xrg.cz/link/sourceOfLaw/1" xpath-default-namespace="http://xrg.cz/link/sourceOfLaw/1">
	<xsl:output method="text"  encoding="UTF-8" indent="yes"/>

    <!--  soud prvniho stupne, prvni institution, which contains "okresni" or "obvodni" in URI -->
	<xsl:variable name="soudPrvniStupen">&lt;<xsl:value-of select="//paragraph[institution[ contains(@rdf:about,'obvodni') or contains(@rdf:about,'okresni')  ]][1]/institution[contains(@rdf:about,'obvodni') or contains(@rdf:about,'okresni') ][1]/@rdf:about"></xsl:value-of>&gt;</xsl:variable>

	<!--  soud druheho stupne, prvni institution, which contains "vrchni" or "krajsky" or "mestsky" in URI -->
	<xsl:variable name="soudDruhyStupen">&lt;<xsl:value-of select="//paragraph[institution[ contains(@rdf:about,'vrchni') or contains(@rdf:about,'krajsky') or contains(@rdf:about,'mestsky') ]][1]/institution[ contains(@rdf:about,'vrchni') or contains(@rdf:about,'krajsky') or contains(@rdf:about,'mestsky') ][1]/@rdf:about"></xsl:value-of>&gt;</xsl:variable>
	

<xsl:template match="/">

@prefix rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;.
@prefix owl2xml: &lt;http://www.w3.org/2006/12/owl2-xml#&gt;.
@prefix xsd: &lt;http://www.w3.org/2001/XMLSchema#&gt;.
@prefix schema: &lt;http://prov4j.org/w3p/schema#&gt;.
@prefix owl: &lt;http://www.w3.org/2002/07/owl#&gt;.
@prefix rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt;.
@prefix vann:     &lt;http://purl.org/vocab/vann/&gt; .

@prefix foaf:     &lt;http://xmlns.com/foaf/0.1/&gt; .
@prefix dcterms:  &lt;http://purl.org/dc/terms/&gt; .
@prefix opmo:   &lt;http://openprovenance.org/model/opmo#&gt;.
@prefix cc: &lt;http://creativecommons.org/ns#&gt;.
@prefix void: &lt;http://rdfs.org/ns/void#&gt; .
@prefix frbr: &lt;http://purl.org/vocab/frbr/core#&gt;.
@prefix skos:     &lt;http://www.w3.org/2004/02/skos/core#&gt; .

@prefix lex:        &lt;http://purl.org/lex#&gt; .
@prefix lexm:        &lt;http://purl.org/lex/meta#&gt; .
@prefix lexdt:        &lt;http://purl.org/lex/datatypes#&gt; .
@prefix lexissuertypes: &lt;http://purl.org/lex/lex-issuer-types#&gt; .
		@prefix lexsoltypes: &lt;http://purl.org/lex/lex-sol-types#&gt; .  
		@prefix sdo: &lt;http://salt.semanticauthoring.org/ontologies/sdo#&gt; .
		@prefix sao: &lt;http://salt.semanticauthoring.org/ontologies/sao#&gt; .
            
       
	<!-- Decision metadata (taken from meta elem section) -->
	
	
	<!-- Spisova znacka (with spaces and '/'-->
	<xsl:variable name="metadataText"><xsl:value-of select='/document/body/meta/text()'/></xsl:variable>
	<xsl:variable name="spisovaZnackaTemp"><xsl:value-of select='substring-after($metadataText,"Spisová značka :")'/></xsl:variable>
	<xsl:variable name="spisovaZnacka"><xsl:value-of select='normalize-space(substring-before($spisovaZnackaTemp,"Datum"))'/></xsl:variable> <!-- Contains spisovou znacku as in the meta section, normalized spaces-->
	
	<!-- Spisova znacka for use in URI -->
	<xsl:variable name="spisovaZnackaBaseWithDashTemp"><xsl:value-of select='replace($spisovaZnacka," ","-")'/></xsl:variable>
	<xsl:variable name="spisovaZnackaBaseWithDash"><xsl:value-of select='replace($spisovaZnackaBaseWithDashTemp,"/","-")'/></xsl:variable>	
	<xsl:variable name="spisovaZnackaInURI"><xsl:value-of select='$spisovaZnackaBaseWithDash'/></xsl:variable>
	
	<!-- Cislo jednaci neni k dispozici -->
	
	<!-- Year for use in URI -->
	<xsl:variable name="yearSpisZnacka"><xsl:value-of select='substring($spisovaZnackaInURI,string-length($spisovaZnackaInURI)-3)'/></xsl:variable>
	
	<!-- Date when issued -->
	<xsl:variable name="decDate"><xsl:value-of select='substring(normalize-space(substring-after($metadataText,"Datum rozhodnutí :")),1,10)'/></xsl:variable>
	
	<xsl:variable name="monthTemp"><xsl:value-of select='substring-before(substring-after($decDate,"."),".")'/></xsl:variable>
	<xsl:variable name="month">
		<xsl:choose>
			
			<xsl:when test="string-length($monthTemp) = 1"> <!-- test that it is a single digit-->
				<xsl:value-of select='concat("0",$monthTemp)'/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select='$monthTemp'/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	
	<xsl:variable name="dayTemp"><xsl:value-of select='substring-before($decDate,".")'/></xsl:variable>
	<xsl:variable name="day">
		<xsl:choose>
			
			<xsl:when test="string-length($dayTemp) = 1"> <!-- test that it is a single digit-->
				<xsl:value-of select='concat("0",$dayTemp)'/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select='$dayTemp'/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	
	<xsl:variable name="yearIssued"><xsl:value-of select='normalize-space(substring-after(substring-after($decDate,"."),"."))'/></xsl:variable>
	<xsl:variable name="normDecDate"><xsl:value-of select='$yearIssued'/>-<xsl:value-of select='$month'/>-<xsl:value-of select='$day'/></xsl:variable>

	
	<!-- lex:decisionKind = USNESENI-->
	<xsl:variable name="usneseniTemp"><xsl:value-of select='normalize-space(substring-after($metadataText,"Typ rozhodnutí :"))'/></xsl:variable>
	<xsl:variable name="usneseniTemp2"><xsl:value-of select='substring-before($usneseniTemp," ")'/></xsl:variable>
	<xsl:variable name="usneseni"><xsl:value-of select='$usneseniTemp2'/></xsl:variable>
	
	<!-- lex:decisionCategory = D-->
	
	<xsl:variable name="categoryTemp"><xsl:value-of select='normalize-space(substring-after(substring-after($metadataText,"Kategorie rozhodnutí"), ":"))'/></xsl:variable>
	<xsl:variable name="categoryTemp2"><xsl:value-of select='substring($categoryTemp,1,1)'/></xsl:variable> <!-- We suppose that category of decisions is a single letter -->
	<xsl:variable name="category"><xsl:value-of select='$categoryTemp2'/></xsl:variable>
	
	
	<!-- Variables Decision (Work, Expre, Mani) & File-->
	
	<!--<xsl:variable name="decCore"><xsl:value-of select="lower-case($spisovaZnacka)"/></xsl:variable>-->
	<xsl:variable name="decPrefix">http://linked.opendata.cz/resource/legislation/cz/decision/<xsl:value-of select="$yearSpisZnacka"/>/<xsl:value-of select="$spisovaZnackaInURI"/></xsl:variable>
	
	<xsl:variable name="decision">&lt;<xsl:value-of select="$decPrefix"/>&gt;</xsl:variable>
	<xsl:variable name="decExprCore"><xsl:value-of select="$decPrefix"/>/expression/<xsl:value-of select='substring-after($decPrefix,"http://linked.opendata.cz/resource/legislation/")'></xsl:value-of>/cs</xsl:variable>
	<xsl:variable name="decExpr">&lt;<xsl:value-of select="$decExprCore"></xsl:value-of>&gt;</xsl:variable>
	<!--<xsl:variable name="decExprParaCore">&lt;<xsl:value-of select="$decExprCore"/>/section/para/</xsl:variable>--> <!-- para number should be add later-->
	
	<!--<xsl:variable name="filename"><xsl:value-of select='substring($path,string-length(substring-before($path, "rozhodnuti"))+1)'/></xsl:variable>-->
	<xsl:variable name="decMani">&lt;<xsl:value-of select="$decExprCore"/>/manifestation&gt;</xsl:variable>

	<!-- create file for the processed decision -->
	<!-- in case there is also decision number, e.g. 56-Co-97-2011-29, remove the last number (-29)-->
	<xsl:variable name="file">&lt;http://linked.opendata.cz/resource/legislation/cz/file/<xsl:value-of select="$yearSpisZnacka"/>/<xsl:value-of select="$spisovaZnackaInURI"/>&gt;</xsl:variable>
	

	<!-- CREATING TRIPLES FOR THE PROCESSED DECISION -->

	<xsl:if test="string-length($metadataText) > 0">  <!-- metadata should be available -->

		<!-- Check that spisova znacka has reasonable format. If not, skip the generation of metadata-->
		<xsl:if test="matches($yearSpisZnacka,'^[0-9]{4}$') and matches($spisovaZnackaInURI,'^[0-9]+-[a-zA-Z]+-[0-9]+-[0-9]{4}(-[0-9]+){0,1}$')"> <!-- test that spisova znacka has reasonable format-->
			
			<!-- Create basic decision and file record -->	
			<xsl:call-template name="createDecisionAndFileBasicRecord">
				<xsl:with-param name="decision"><xsl:value-of select="$decision"/></xsl:with-param>
				<xsl:with-param name="decisionIdentifierInURI"><xsl:value-of select="$spisovaZnackaInURI"/></xsl:with-param> <!-- there is only spisova znacka, neni cislo jednaci -->
				<xsl:with-param name="file"><xsl:value-of select="$file"/></xsl:with-param>
				<xsl:with-param name="fileIdentifierInURI"><xsl:value-of select="$spisovaZnackaInURI"/></xsl:with-param>  <!-- there is only spisova znacka, neni cislo jednaci -->
				<xsl:with-param name="yearInURI"><xsl:value-of select="$yearSpisZnacka"/></xsl:with-param>
				<xsl:with-param name="yearDecisionIssued"><xsl:value-of select="$yearIssued"/></xsl:with-param>
			</xsl:call-template>
			
			
			
			
			<!-- more info about decision -->
			<xsl:if test="matches($yearIssued,'^[0-9]{4}$') and matches($month,'[0-1][0-9]') and matches($day,'[0-3][0-9]')"> <!-- test that the date was extracted -->
				<xsl:value-of select="$decision"/> dcterms:issued "<xsl:value-of select="$normDecDate"/>"^^xsd:date .
			</xsl:if>
			
			<xsl:if test="string-length($usneseni) > 0"> <!-- If something was parsed -->
				<xsl:value-of select="$decision"/> lex:decisionKind &lt;http://linked.opendata.cz/resource/legislation/cz/decision-kind/supreme-court/<xsl:value-of select="replace(lower-case($usneseni),' ','-')"/>&gt; .
				&lt;http://linked.opendata.cz/resource/legislation/cz/decision-kind/supreme-court/<xsl:value-of select="replace(lower-case($usneseni),' ','-')"/>&gt; skos:prefLabel "<xsl:value-of select="$usneseni"/>" . 
			</xsl:if>
			
			<xsl:if test="string-length($category) = 1"> <!-- It must be one single letter -->
				<xsl:value-of select="$decision"/> lex:decisionCategory &lt;http://linked.opendata.cz/resource/legislation/cz/decision-category/supreme-court/<xsl:value-of select="lower-case($category)"/>&gt; .  
				&lt;http://linked.opendata.cz/resource/legislation/cz/decision-category/supreme-court/<xsl:value-of select="lower-case($category)"/>&gt; skos:prefLabel "<xsl:value-of select="$category"/>" . 
		    </xsl:if>
			
			
			<xsl:variable name="subjectTemp"><xsl:value-of select='substring-after($metadataText,"Heslo :")'/></xsl:variable>
			
			
			<xsl:choose>
				<xsl:when test="string-length(substring-before($subjectTemp,'Dotčené předpisy :')) > 0">
					<xsl:call-template name="parseSubjects">
						<xsl:with-param name="pText"><xsl:value-of select='substring-before($subjectTemp,"Dotčené předpisy :")'/></xsl:with-param>
						<xsl:with-param name="decision"><xsl:value-of select="$decision"/></xsl:with-param>
					</xsl:call-template>	
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="parseSubjects">
						<xsl:with-param name="pText"><xsl:value-of select='substring-before($subjectTemp,"Kategorie rozhodnutí :")'/></xsl:with-param>
						<xsl:with-param name="decision"><xsl:value-of select="$decision"/></xsl:with-param>
					</xsl:call-template>
					
				</xsl:otherwise>
			</xsl:choose>
		
			
			<!-- duvod dovolani lex reasonsToAppeal -->
			
			
			<xsl:for-each select="document/body/meta/reasons/*">
				
				<!-- every individual act may contain two or more URIs in @rdf:about -->
				<xsl:for-each select="tokenize(@rdf:about, ' ')">
				
					<xsl:variable name="reason"><xsl:value-of select='.'/></xsl:variable>
					<xsl:if test="contains($reason, 'http://')">
					
					<xsl:value-of select="$decision"/> lex:reasonToAppeal &lt;<xsl:value-of select='$reason'/>&gt;  .
						
					</xsl:if>
				
				</xsl:for-each>
				
			</xsl:for-each>
			
			
			<!-- dotcene predpisy lex concernedSourcesOfLaw -->
			
			<xsl:for-each select="document/body/meta/concernedSources/*">
				
				<!-- every individual act may contain two or more URIs in @rdf:about -->
				<xsl:for-each select="tokenize(@rdf:about, ' ')">
				
					<xsl:variable name="source"><xsl:value-of select='.'/></xsl:variable>
					
					<xsl:if test="contains($source, 'http://')">
					<xsl:value-of select="$decision"/> lex:concernedSourceOfLaw &lt;<xsl:value-of select='$source'/>&gt;  .
					</xsl:if>
				
				</xsl:for-each>
				
			</xsl:for-each>
			
			
			
			<!-- Create new decision expression and manifestation -->
			<xsl:value-of select="$decExpr"/> a  frbr:Expression .
			<xsl:value-of select="$decExpr"/> frbr:realizationOf <xsl:value-of select="$decision"/> .
			
			<xsl:value-of select="$decMani"/> a  frbr:Manifestation .
			<xsl:value-of select="$decMani"/> frbr:embodimentOf <xsl:value-of select="$decExpr"/> .
		
		    <!--<xsl:value-of select="$decMani"/> dcterms:source """<xsl:value-of select="$filename"/>""" .-->
		
		    <!-- Create new court  -->
		
			<!-- Court -selects the first institution, which is typically the institution responsible for the processed decision. NO, because there could be e.g. "pravni veta" before "soud"-->
			<!-- It is set to be http://linked.opendata.cz/resource/court/cz/nejvyssi-soud by default. Which still does not solve the strange decision below: 
				PROBLEM: 4 Cmo 386/2012
				
				<body><meta>
    Právní věta : Proti usnesení, jímž <institution id="1" label="B1" name="soud prvního stupně" rdf:about="http://linked.opendata.cz/resource/court/cz/soud-prvni-stupne" refers_to="0">soud prvního stupně</institution> podle ustanovení § 76h <act id="2" label="A1" name="" rdf:about="http://linked.opendata.cz/resource/legislation/cz/act/1963/99-1963" refers_to="0">o. s. ř.</act> vyzve navrhovatele ke složení doplatku jistoty ( <act id="3" label="A2" name="" rdf:about="http://linked.opendata.cz/resource/legislation/cz/act/1963/99-1963/section/75/1" refers_to="0">§ 75 odst. 1, věta druhá, o. s. ř.</act> ), je odvolání přípustné.
    
    Soud : <institution id="4" label="B2" name="Vrchní soud v Olomouci" rdf:about="http://linked.opendata.cz/resource/court/cz/vrchni-soud-v-olomouci" refers_to="0">Vrchní soud v Olomouci</institution>
    
				-->
			
			<!--<xsl:variable name="court">&lt;<xsl:value-of select="/document/body/meta/institution[position()=1]/@rdf:about"/>&gt;</xsl:variable>-->
			<xsl:variable name="court">&lt;http://linked.opendata.cz/resource/court/cz/nejvyssi-soud&gt;</xsl:variable>
			
			<xsl:call-template name="createCourt">
				<xsl:with-param name="court"><xsl:value-of select="$court"/></xsl:with-param>
				<xsl:with-param name="file"><xsl:value-of select="$file"/></xsl:with-param>
				<xsl:with-param name="decision"><xsl:value-of select="$decision"/></xsl:with-param>
				
			</xsl:call-template>
			
	
		</xsl:if>
	</xsl:if>	
		
	    <!-- Following should be called for individual paragraphs -->
       <xsl:for-each select="//paragraph">		
	    	
					<!--<xsl:variable name="decExprPara"><xsl:value-of select="$decExprParaCore"/><xsl:number></xsl:number>&gt;</xsl:variable>-->
		   
       	
					<xsl:call-template name="extractActsCitations">
						<!--<xsl:with-param name="paragraphURI"><xsl:value-of select="$decExprPara"/></xsl:with-param>-->
						<xsl:with-param name="paragraph"><xsl:copy-of select="."/></xsl:with-param>
					</xsl:call-template>
					
					<xsl:call-template name="extractJudgmentsCitations">
						<!-- <xsl:with-param name="paragraphURI"><xsl:value-of select="$decExprPara"/></xsl:with-param>-->
						<xsl:with-param name="paragraph"><xsl:copy-of select="."/></xsl:with-param>
					</xsl:call-template>
			
	    	
	   </xsl:for-each>
	
				

	
	</xsl:template>
	  
	 
	<!-- subject are divided by space -->
	<xsl:template name="parseSubjects">
		<xsl:param name="pText"/>
		<xsl:param name="decision"/>
		<xsl:if test="string-length(normalize-space($pText)) > 0">
		<xsl:choose>
			<xsl:when test="not(contains($pText, ','))">
				<!-- if there is no comma than just copy the text  -->
				<!--<xsl:value-of select="$decision"/> dcterms:subject "<xsl:value-of select="normalize-space($pText)"/>" .-->
				
				
				<xsl:variable name="subjectPrepared"><xsl:value-of select="normalize-space($pText)"/></xsl:variable>
				<xsl:variable name="subjectPrepared2">http://linked.opendata.cz/resource/legislation/cz/decision-subject/<xsl:value-of select="replace(lower-case($subjectPrepared), ' ', '-')"/></xsl:variable>
				<xsl:value-of select="$decision"/> dcterms:subject &lt;<xsl:value-of select="$subjectPrepared2"/>&gt; .
				&lt;<xsl:value-of select="$subjectPrepared2"/>&gt;  a skos:Concept .
				&lt;<xsl:value-of select="$subjectPrepared2"/>&gt; skos:inScheme &lt;http://linked.opendata.cz/ontology/legislation/cz/DecisionSubjectsScheme&gt; .
				
				
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="subjectPrepared"><xsl:value-of select="normalize-space(substring-before($pText, ','))"/></xsl:variable>
				<xsl:variable name="subjectPrepared2">http://linked.opendata.cz/resource/legislation/cz/decision-subject/<xsl:value-of select="replace(lower-case($subjectPrepared), ' ', '-')"/></xsl:variable>
				<xsl:value-of select="$decision"/> dcterms:subject &lt;<xsl:value-of select="$subjectPrepared2"/>&gt; .
				&lt;<xsl:value-of select="$subjectPrepared2"/>&gt;  a skos:Concept .
				&lt;<xsl:value-of select="$subjectPrepared2"/>&gt; skos:inScheme &lt;http://linked.opendata.cz/ontology/legislation/cz/DecisionSubjectsScheme&gt; .
				
				
				
				<!-- Process the rest -->
				<xsl:call-template name="parseSubjects">
					<xsl:with-param name="pText" select=
						"normalize-space(substring-after($pText, ','))"/>
					<xsl:with-param name="decision"><xsl:value-of select="$decision"/></xsl:with-param>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
		</xsl:if>
	</xsl:template>
	
	<!-- subject are divided by space -->
	<xsl:template name="parseReasons">
		<xsl:param name="pText"/>
		<xsl:param name="decision"/>
		<xsl:if test="string-length(normalize-space($pText)) > 0">
			<xsl:choose>
				<xsl:when test="not(contains($pText, 'rdf:about'))">
					<!-- if there is no rdf:about than do nothing  -->
					<!--<xsl:value-of select="$decision"/> dcterms:subject "<xsl:value-of select="normalize-space($pText)"/>" .-->
					<!--<xsl:copy-of select="normalize-space($pText)"/>-->
					
					
					
				</xsl:when>
				<xsl:otherwise>
					<!--<xsl:if test="string-length(normalize-space(substring-before($pText, ','))) > 0">
					 <!-\- Take everything before the comma -\->
				     <xsl:value-of select="$decision"/> dcterms:subject "<xsl:value-of select="normalize-space(substring-before($pText, ','))"/>" .
				</xsl:if>
				-->
					<xsl:variable name="referenceToSourceOfLaw"><xsl:value-of select="substring-after($pText, 'rdf:about=')"/></xsl:variable>
					
					
					<xsl:value-of select="$decision"/> lex:reasonsToAppeal &lt;<xsl:value-of select="normalize-space($referenceToSourceOfLaw)"/>&gt;  .
					
					
					<!-- Process the rest -->
					<xsl:call-template name="parseSubjects">
						<xsl:with-param name="pText" select=
							"normalize-space(substring-after($pText, 'rdf:about='))"/>
						<xsl:with-param name="decision"><xsl:value-of select="$decision"/></xsl:with-param>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	
	  
	  
	<xsl:template name="createDecisionAndFileBasicRecord">
		<xsl:param name="decision"> </xsl:param>
		<xsl:param name="decisionIdentifierInURI"/> <!-- Typically it is spisova znacka (with dashes), but it could be also cislo jednaci -->
		<xsl:param name="file"> </xsl:param> 
		<xsl:param name="fileIdentifierInURI"/> <!-- Spisova znacka (with dashes) -->
		<xsl:param name="yearInURI"/> 
		<xsl:param name="yearDecisionIssued"></xsl:param>
		
		
		<!-- Metadata for decision/file-->
		<xsl:variable name="senat"><xsl:value-of select='substring-before($decisionIdentifierInURI,"-")'/></xsl:variable>
		<xsl:variable name="fileKind"><xsl:value-of select='substring-before(substring-after($decisionIdentifierInURI,"-"),"-")'/></xsl:variable>
		<xsl:variable name="fileNumber"><xsl:value-of select='substring-before(substring-after(substring-after($decisionIdentifierInURI,"-"),"-"),"-")'/></xsl:variable>
		<!-- year is already available -->
		
		<!-- Create new decision object -->
		<xsl:value-of select="$decision"/> a lex:Decision .
		
		<!-- is the decision identifier cislo jednaci? if yes, take the last number (decisionNumber) in: 56-Co-97-2011-29 -->
		<xsl:variable name="decisionNumber"><xsl:value-of select='substring-after(substring-after(substring-after(substring-after($decisionIdentifierInURI,"-"),"-"),"-"),"-")'/></xsl:variable>
		
		<xsl:choose>
			<xsl:when test="string-length($decisionNumber) > 0"> <!-- cislo jednaci is available -->
				<xsl:variable name="decisionIdentifierText"><xsl:value-of select='concat($senat," ",$fileKind," ",$fileNumber,"/",$yearInURI, "-", $decisionNumber)'/></xsl:variable>
				<xsl:value-of select="$decision"/> dcterms:identifier "<xsl:value-of select="$decisionIdentifierText"/>" . 
				<xsl:value-of select="$decision"/> dcterms:title "<xsl:value-of select="$decisionIdentifierText"/>" .
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="decisionIdentifierText"><xsl:value-of select='concat($senat," ",$fileKind," ",$fileNumber,"/",$yearInURI)'/></xsl:variable>
				<xsl:value-of select="$decision"/> dcterms:identifier "<xsl:value-of select="$decisionIdentifierText"/>" . 
				<xsl:value-of select="$decision"/> dcterms:title "<xsl:value-of select="$decisionIdentifierText"/>" . 
			</xsl:otherwise>
		</xsl:choose>
		
		<!-- ECLI -->
		<xsl:if test="string-length($yearDecisionIssued) > 0"> <!-- we have the year when it was issued -->
			<xsl:if test="matches($yearDecisionIssued,'^[0-9]{4}$')"> <!-- year is in the right format -->
				<xsl:variable name="spisovaZnackaECLITemp"><xsl:value-of select='upper-case(replace($decisionIdentifierInURI,"-","."))'/></xsl:variable>
				<xsl:variable name="spisovaZnackaECLI">ECLI:CZ:NS:<xsl:value-of select="$yearDecisionIssued"/>:<xsl:value-of select="$spisovaZnackaECLITemp"/>.1</xsl:variable>
				<xsl:value-of select="$decision"/> dcterms:identifier "<xsl:value-of select="$spisovaZnackaECLI"/>" . 
			</xsl:if>
		</xsl:if>
		
		
		<xsl:variable name="fileIdentifierText"><xsl:value-of select='concat($senat," ",$fileKind," ",$fileNumber,"/",$yearInURI)'/></xsl:variable>

		<!-- Create new file object -->
		<xsl:value-of select="$file"/> a lex:File .
		<xsl:value-of select="$file"/> dcterms:identifier "<xsl:value-of select="$fileIdentifierText"/>" . 
		<xsl:value-of select="$file"/> dcterms:title "<xsl:value-of select="$fileIdentifierText"/>" . 
		<xsl:value-of select="$file"/> lex:senateNumber "<xsl:value-of select="$senat"/>" .
		<xsl:value-of select="$file"/> lex:fileKind "<xsl:value-of select="$fileKind"/>" . 
		<xsl:value-of select="$file"/> lex:fileNumber "<xsl:value-of select="$fileNumber"/>"^^xsd:positiveInteger .
		<xsl:value-of select="$file"/> lex:fileYear "<xsl:value-of select="$yearInURI"/>" . <!-- ^^xsd:gYear . -->
		
		
		<!-- ustavni soud! <judgment id="60" label="C38" name="" rdf:about="http://linked.opendata.cz/resource/legislation/cz/judgment/2003/ii-us-279-03" refers_to="33">sp. zn. II. ÚS 279/03</judgment>
		-->
		
		<xsl:value-of select="$decision"/> lex:belongsToFile <xsl:value-of select="$file"/> .
		
	</xsl:template>
	  

	  
	<xsl:template name="extractActsCitations">
		<!--<xsl:param name="paragraphURI"></xsl:param>-->
		<xsl:param name="paragraph"></xsl:param>
		
		<xsl:for-each select="act">	
			
			<xsl:variable name="actId"><xsl:value-of select='@id'/></xsl:variable>
			<xsl:variable name="generatedID"><xsl:value-of select="generate-id()"/></xsl:variable>
		
			<!-- @rdf:about may contain list of URIs-->
			<xsl:for-each select="tokenize(@rdf:about, ' ')">
				
				
				<!-- DATA not created, only metadata 
				<xsl:call-template name="textChunkCreation">
					<xsl:with-param name="uri">&lt;<xsl:value-of select="."/>&gt;</xsl:with-param>
					<xsl:with-param name="id"><xsl:value-of select="$actId"/></xsl:with-param>
					<xsl:with-param name="generatedID"><xsl:value-of select="$generatedID"/></xsl:with-param>
					<xsl:with-param name="paragraphURI"><xsl:value-of select="$paragraphURI"/></xsl:with-param>
				</xsl:call-template>
				-->
				
			
				
			</xsl:for-each>
		
			
			
		</xsl:for-each>
		
	</xsl:template>
	
	
	
	<xsl:template name="extractJudgmentsCitations">
		<!--<xsl:param name="paragraphURI"></xsl:param>-->
		<xsl:param name="paragraph"></xsl:param>
		
		<xsl:for-each select="judgment">
			
			<xsl:variable name="judgmentId"><xsl:value-of select='@id'/></xsl:variable>
			<xsl:variable name="generatedID"><xsl:value-of select="generate-id()"/></xsl:variable>

			<xsl:variable name="refersTo"><xsl:value-of select='@refers_to'/></xsl:variable>
			<xsl:variable name="court">&lt;<xsl:value-of select="//institution[@id=$refersTo]/@rdf:about"/>&gt;</xsl:variable>
			
			<!-- @rdf:about may contain list of URIs-->
			<xsl:for-each select="tokenize(@rdf:about, ' ')">
				
			    
				<xsl:call-template name="createObjectsForJudgment">
					<xsl:with-param name="decisionURI"><xsl:value-of select="."/></xsl:with-param>
					<xsl:with-param name="refersTo"><xsl:value-of select="$refersTo"/></xsl:with-param>
					<xsl:with-param name="court"><xsl:value-of select="$court"/></xsl:with-param>
					
				</xsl:call-template>
				
			</xsl:for-each>
			
			
		</xsl:for-each>
		
	</xsl:template>
	
	<xsl:template name="createObjectsForJudgment">
		<xsl:param name="decisionURI"></xsl:param>
		<xsl:param name="refersTo"></xsl:param>

		<xsl:param name="court"></xsl:param>
	
		<!-- prepare for creation of new objects for decision, expr, man, file -->
		<xsl:variable name="decision">&lt;<xsl:value-of select="$decisionURI"/>&gt;</xsl:variable>
		<!-- <xsl:variable name="decExpr">&lt;<xsl:value-of select="$decisionURI"/>/expression&gt;</xsl:variable>-->
		
		<xsl:variable name="decExprCore"><xsl:value-of select="$decisionURI"/>/expression/<xsl:value-of select='substring-after($decisionURI,"http://linked.opendata.cz/resource/legislation/")'></xsl:value-of>/cs</xsl:variable>
		<xsl:variable name="decExpr">&lt;<xsl:value-of select="$decExprCore"></xsl:value-of>&gt;</xsl:variable>
		
		<xsl:variable name="decMani">&lt;<xsl:value-of select="$decExprCore"/>/manifestation&gt;</xsl:variable>
	
		
		
		
		
	
		
		<!--parse URI to get identifier for decision and parse identifier for file -->
		
		<xsl:variable name="year"><xsl:value-of select="substring(substring-after($decisionURI,'decision/'),1,4)"/></xsl:variable> 
		<xsl:variable name="IdentifierInURIForDecision"><xsl:value-of select="substring-after(substring-after($decisionURI,'decision/'), '/')" /></xsl:variable>
		<!-- in case there is also decision number, e.g. 56-Co-97-2011-29, remove the last number (-29)-->
		<xsl:variable name="IdentifierInURIForFile"><xsl:value-of select="replace($IdentifierInURIForDecision,'(^[0-9]+-[a-zA-Z]+-[0-9]+-[0-9]{4})(-[0-9]+){0,1}$', '$1')" /></xsl:variable>
		
		
		<!-- Create new file-->
		<!-- if available, the last number in: 56-Co-97-2011-29 -->
		<xsl:variable name="file">&lt;http://linked.opendata.cz/resource/legislation/cz/file/<xsl:value-of select="$year"/>/<xsl:value-of select="$IdentifierInURIForFile"/>&gt;</xsl:variable> 
		
		<!-- Check that spisova znacka has reasonable format. If not, skip the generation of metadata-->
		<xsl:if test="matches($year,'^[0-9]{4}$') and matches($IdentifierInURIForDecision,'^[0-9]+-[a-zA-Z]+-[0-9]+-[0-9]{4}(-[0-9]+){0,1}$')"> <!-- test that the date was extracted -->

			
			<xsl:call-template name="createDecisionAndFileBasicRecord">
				<xsl:with-param name="decision"><xsl:value-of select="$decision"/></xsl:with-param>
				<xsl:with-param name="decisionIdentifierInURI"><xsl:value-of select="$IdentifierInURIForDecision"/></xsl:with-param>
				<xsl:with-param name="file"><xsl:value-of select="$file"/></xsl:with-param>
				<xsl:with-param name="fileIdentifierInURI"><xsl:value-of select="$IdentifierInURIForDecision"/></xsl:with-param>
				<xsl:with-param name="yearInURI"><xsl:value-of select="$year"/></xsl:with-param>
				<xsl:with-param name="yearDecisionIssued"></xsl:with-param> <!-- year issued is not known -->
			</xsl:call-template>
			
		
			<!-- Create new decision expression and manifestation -->
			<xsl:value-of select="$decExpr"/> a  frbr:Expression .
			<!-- Publication and sections/paragraphs created only for the main processed decision <xsl:value-of select="$decExpr"/> a  sdo:Publication .-->
			<xsl:value-of select="$decExpr"/> frbr:realizationOf <xsl:value-of select="$decision"/> .
			
			<xsl:value-of select="$decMani"/> a  frbr:Manifestation .
			<xsl:value-of select="$decMani"/> frbr:embodimentOf <xsl:value-of select="$decExpr"/> .
			
			<!--<xsl:value-of select="$decision"/> lex:belongsToFile <xsl:value-of select="$file"/> .-->
			
			
			<!-- court -->
			
	
				<xsl:call-template name="createCourt">
					<xsl:with-param name="court"><xsl:value-of select="$court"/></xsl:with-param>
					<xsl:with-param name="file"><xsl:value-of select="$file"/></xsl:with-param>
					<xsl:with-param name="decision"><xsl:value-of select="$decision"/></xsl:with-param>
					
				</xsl:call-template>
				
			 
			
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="createCourt">
		<xsl:param name="court"></xsl:param>
		<xsl:param name="file"></xsl:param>
		<xsl:param name="decision"></xsl:param>
		
		<!-- Check that there is certain court -->
		<xsl:if test="string-length($court) > 2"> <!-- not just <>-->
		
		<xsl:choose>
			<xsl:when test="contains($court,'soud-dovolaci') or contains($court,'dovolaci-soud')">
				<xsl:variable name="courtFinal">&lt;http://linked.opendata.cz/resource/court/cz/nejvyssi-soud&gt;</xsl:variable>
				
				<xsl:if test="string-length($courtFinal) > 2">
					<xsl:value-of select="$courtFinal"/> a lex:Court .
					
					<xsl:value-of select="$file"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .		
					<xsl:value-of select="$decision"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .
				</xsl:if>
			
			</xsl:when>
			<xsl:when test="contains($court,'soud-prvni-stupne-pro') or contains($court,'soud-prvni-stupne')">
				<xsl:variable name="courtFinal"><xsl:value-of select="$soudPrvniStupen"></xsl:value-of></xsl:variable>
				
				<xsl:if test="string-length($courtFinal) > 2">
					<xsl:value-of select="$courtFinal"/> a lex:Court .
				
					<xsl:value-of select="$file"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .		
					<xsl:value-of select="$decision"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .
				</xsl:if>
				
			</xsl:when>
			<xsl:when test="contains($court,'odvolaci-soud') or contains($court,'soud-druhy-stupne') or contains($court,'soud-druhy-stupne-pro')">
				<xsl:variable name="courtFinal"><xsl:value-of select="$soudDruhyStupen"></xsl:value-of></xsl:variable>
				
				<xsl:if test="string-length($courtFinal) > 2">
					<xsl:value-of select="$courtFinal"/> a lex:Court .
					
					<xsl:value-of select="$file"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .		
					<xsl:value-of select="$decision"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .
				</xsl:if>
				
				
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="courtFinal"><xsl:value-of select="$court"/></xsl:variable>
				
				<xsl:if test="string-length($courtFinal) > 2">
					<xsl:value-of select="$courtFinal"/> a lex:Court .
					
					<xsl:value-of select="$file"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .		
					<xsl:value-of select="$decision"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	
		
		
		
		
		
		<!-- Check that the court name is not blacklisted -->
		<!--<xsl:if test="not(contains($court,'odvolaci-soud') or contains($court,'soud-prvni-stupne') )">-->
			
			
			
			
		
		</xsl:if>
			
		<!-- </xsl:if> -->
		
		
    </xsl:template>
	
	
	
	
	

	
	

    
	<xsl:template match="text()" />    
    
	<!-- <xsl:template match="/document/body/*">	
       <xsl:value-of select="$expression" /> lex:refersTo  <xsl:value-of select="@rdf:about" />
    </xsl:template>-->
</xsl:stylesheet>
