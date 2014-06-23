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
	<xsl:variable name="spisovaZnackaText"><xsl:value-of select='document/metadata/table//td[preceding-sibling::node()[contains(text(),"Spisová značka")]]/normalize-space(string())'/></xsl:variable>
	
	<!-- Spisova znacka for use in URI -->
	<xsl:variable name="spisovaZnackaBaseWithDashTemp"><xsl:value-of select='replace(replace(replace($spisovaZnackaText,"\.","-")," ","-"),"Ú","U")'/></xsl:variable>
	<xsl:variable name="spisovaZnackaBaseWithDash"><xsl:value-of select='replace($spisovaZnackaBaseWithDashTemp,"/","-")'/></xsl:variable>	
	<xsl:variable name="spisovaZnackaInURI"><xsl:value-of select='$spisovaZnackaBaseWithDash'/></xsl:variable>
	
	<!-- Cislo jednaci neni k dispozici -->
	
	<!-- ECLI ECLI :CZ :US :2014 :1.US.672.14.1 -->
	<xsl:variable name="ecliTemp"><xsl:value-of select='document/metadata/table//td[preceding-sibling::node()[contains(text(),"Identifikátor evropské judikatury")]]/normalize-space(string())'/></xsl:variable>
	<xsl:variable name="ecli"><xsl:value-of select='replace($ecliTemp," ","")'/></xsl:variable>
	
	<!-- Year for use in URI -->
	<!-- year must be taken from datum podani  
	<xsl:variable name="yearSpisZnacka"><xsl:value-of select='substring($spisovaZnackaInURI,string-length($spisovaZnackaInURI)-3)'/></xsl:variable> -->
	<xsl:variable name="decDatePodani"><xsl:value-of select="/document/metadata/table//td[preceding-sibling::node()[contains(text(),'Datum podání')]]/normalize-space(string())"/></xsl:variable>
	<xsl:variable name="yearSpisZnacka"><xsl:value-of select='normalize-space(substring-after(substring-after($decDatePodani,"."),"."))'/></xsl:variable>
	
	
	<!-- Date when issued -->
	<xsl:variable name="decDate"><xsl:value-of select="/document/metadata/table//td[preceding-sibling::node()[contains(text(),'Datum rozhodnutí')]]/normalize-space(string())"/></xsl:variable>
	
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

	
	<!-- lex:decisionKind = USNESENI (for nsoud), for usoud, Forma rozhodnutí = Usneseni  -->
	<xsl:variable name="usneseni"><xsl:value-of select='/document/metadata/table//td[preceding-sibling::node()[contains(text(),"Forma rozhodnutí")]]/normalize-space(string())'/></xsl:variable> 
	
	<!-- lex:decisionCategory =  D (for nsoud), for usoud Typ řízení - O ústavních stížnostech -->
	
	<xsl:variable name="category"><xsl:value-of select='/document/metadata/table//td[preceding-sibling::node()[contains(text(),"Typ řízení")]]/normalize-space(string())'/></xsl:variable> 
	
	
	<!-- Manifestation link
	     http ://nalus.usoud.cz/Search/GetText.aspx?sz=1-672-14_1 -->
	<xsl:variable name="manifestationLinkTemp"><xsl:value-of select='document/metadata/table//td[preceding-sibling::node()[contains(text(),"URL adresa")]]/normalize-space(string())'/></xsl:variable>
	<xsl:variable name="manifestationLink"><xsl:value-of select='replace($manifestationLinkTemp," ","")'/></xsl:variable>
	
	
	<!-- Variables Decision (Work, Expre, Mani) & File-->
	
	<!--<xsl:variable name="decCore"><xsl:value-of select="lower-case($spisovaZnacka)"/></xsl:variable>-->
	<xsl:variable name="decPrefix">http://linked.opendata.cz/resource/legislation/cz/decision/<xsl:value-of select="$yearSpisZnacka"/>/<xsl:value-of select="$spisovaZnackaInURI"/></xsl:variable>
	<xsl:variable name="decision">&lt;<xsl:value-of select="$decPrefix"/>&gt;</xsl:variable>
	

	<!-- create file for the processed decision -->
	<!-- in case there is also decision number, e.g. 56-Co-97-2011-29, remove the last number (-29)-->
	<xsl:variable name="file">&lt;http://linked.opendata.cz/resource/legislation/cz/file/<xsl:value-of select="$yearSpisZnacka"/>/<xsl:value-of select="$spisovaZnackaInURI"/>&gt;</xsl:variable>
	
	<!--<xsl:variable name="court">&lt;<xsl:value-of select="/document/body/meta/institution[position()=1]/@rdf:about"/>&gt;</xsl:variable>-->
	<xsl:variable name="court">&lt;http://linked.opendata.cz/resource/court/cz/ustavni-soud&gt;</xsl:variable>
	

	<!-- CREATING TRIPLES FOR THE PROCESSED DECISION -->



		<!-- Check that spisova znacka has reasonable format. If not, skip the generation of metadata-->
		<xsl:if test="matches($yearSpisZnacka,'^[0-9]{4}$') and matches($spisovaZnackaInURI,'-US-[0-9]+-[0-9]+(-[0-9]+){0,1}$')"> <!-- test that spisova znacka has reasonable format-->
			
			<!-- Create basic decision and file record -->	
			<xsl:call-template name="createDecisionAndFileBasicRecordUS">
				
				<xsl:with-param name="decisionWithoutBrackets"><xsl:value-of select="$decPrefix"/></xsl:with-param>
				<xsl:with-param name="decisionIdentifierInURI"><xsl:value-of select="$spisovaZnackaInURI"/></xsl:with-param> <!-- there is only spisova znacka, neni cislo jednaci -->
				<xsl:with-param name="file"><xsl:value-of select="$file"/></xsl:with-param>
				<xsl:with-param name="fileIdentifierInURI"><xsl:value-of select="$spisovaZnackaInURI"/></xsl:with-param>  <!-- there is only spisova znacka, neni cislo jednaci -->
				<xsl:with-param name="yearInURIFull"><xsl:value-of select="$yearSpisZnacka"/></xsl:with-param>
				<xsl:with-param name="ecli"><xsl:value-of select="$ecli"/></xsl:with-param>
				<xsl:with-param name="manifestationLink"><xsl:value-of select="$manifestationLink"/></xsl:with-param>
				<xsl:with-param name="court"><xsl:value-of select="$court"></xsl:value-of></xsl:with-param>
			</xsl:call-template>
			

			<!-- more info about decision -->
			<xsl:if test="matches($yearIssued,'^[0-9]{4}$') and matches($month,'[0-1][0-9]') and matches($day,'[0-3][0-9]')"> <!-- test that the date was extracted -->
				<xsl:value-of select="$decision"/> dcterms:issued "<xsl:value-of select="$normDecDate"/>"^^xsd:date .
			</xsl:if>
			
			<xsl:if test="string-length($usneseni) > 0"> <!-- If something was parsed -->
				<xsl:value-of select="$decision"/> lex:decisionKind &lt;http://linked.opendata.cz/resource/legislation/cz/decision-kind/constitutional-court/<xsl:value-of select="replace(lower-case($usneseni),' ','-')"/>&gt; .
				&lt;http://linked.opendata.cz/resource/legislation/cz/decision-kind/constitutional-court/<xsl:value-of select="replace(lower-case($usneseni),' ','-')"/>&gt; skos:prefLabel "<xsl:value-of select="$usneseni"/>" . 
			</xsl:if>
			
			
			<xsl:if test="string-length($category) > 0">
				<xsl:value-of select="$decision"/> lex:decisionCategory &lt;http://linked.opendata.cz/resource/legislation/cz/decision-category/constitutional-court/<xsl:value-of select="replace(lower-case($category),' ','-')"/>&gt; .  
				&lt;http://linked.opendata.cz/resource/legislation/cz/decision-category/constitutional-court/<xsl:value-of select="replace(lower-case($category),' ','-')"/>&gt; skos:prefLabel "<xsl:value-of select="$category"/>" . 
		    </xsl:if>
			
			
			
			<xsl:variable name="subjectTemp"><xsl:value-of select='replace(/document/metadata/table//td[preceding-sibling::node()[contains(text(),"Předmět řízení")]]/string(),"(\n)+", "/")'/></xsl:variable>
			<xsl:call-template name="parseSubjects">
				<xsl:with-param name="pText"><xsl:value-of select='$subjectTemp'/></xsl:with-param>
				<xsl:with-param name="decision"><xsl:value-of select="$decision"/></xsl:with-param>
			</xsl:call-template>
		
			
			<!-- duvod dovolani lex reasonsToAppeal NOT used for USOUD -->
<!--			
			
			<xsl:for-each select="document/body/meta/reasons/*">
				
				<!-\- every individual act may contain two or more URIs in @rdf:about -\->
				<xsl:for-each select="tokenize(@rdf:about, ' ')">
				
					<xsl:variable name="reason"><xsl:value-of select='.'/></xsl:variable>
					<xsl:if test="contains($reason, 'http://')">
					
					<xsl:value-of select="$decision"/> lex:reasonToAppeal &lt;<xsl:value-of select='$reason'/>&gt;  .
						
					</xsl:if>
				
				</xsl:for-each>
				
			</xsl:for-each> -->
			
			
			<!-- Dotčené ústavní zákony a mezinárodní smlouvy lex concernedSourcesOfLaw  -->
			
			<xsl:for-each select="/document/metadata/table//td[preceding-sibling::node()[contains(text(),'Dotčené ústavní zákony a mezinárodní smlouvy')]]">
				
				<!-- every individual act may contain two or more URIs in @rdf:about -->
				<xsl:for-each select=".//@rdf:about">
					<xsl:for-each select="tokenize(., ' ')">
					
						<xsl:variable name="source"><xsl:value-of select='.'/></xsl:variable>
						
						<xsl:if test="contains($source, 'http://')">
						<xsl:value-of select="$decision"/> lex:concernedSourceOfLaw &lt;<xsl:value-of select='$source'/>&gt;  .
						</xsl:if>
					
					</xsl:for-each>
				</xsl:for-each>
				
			</xsl:for-each>
			
			<!-- Ostatní dotčené předpisy lex concernedSourcesOfLaw -->
			
			<xsl:for-each select="/document/metadata/table//td[preceding-sibling::node()[contains(text(),'Ostatní dotčené předpisy')]]">
				
				<!-- every individual act may contain two or more URIs in @rdf:about -->
				<xsl:for-each select=".//@rdf:about">
					<xsl:for-each select="tokenize(., ' ')">
						
						<xsl:variable name="source"><xsl:value-of select='.'/></xsl:variable>
						
						<xsl:if test="contains($source, 'http://')">
							<xsl:value-of select="$decision"/> lex:concernedSourceOfLaw &lt;<xsl:value-of select='$source'/>&gt;  .
						</xsl:if>
						
					</xsl:for-each>
				</xsl:for-each>
				
			</xsl:for-each>
			
		
	
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
			<xsl:when test="not(contains($pText, '/'))">
				<!-- if there is no slash -> than just copy the text  -->
				<!--<xsl:value-of select="$decision"/> dcterms:subject "<xsl:value-of select="normalize-space($pText)"/>" .-->
				
				
				<xsl:variable name="subjectPrepared"><xsl:value-of select="normalize-space($pText)"/></xsl:variable>
				<xsl:variable name="subjectPrepared2">http://linked.opendata.cz/resource/legislation/cz/decision-subject/<xsl:value-of select="replace(lower-case($subjectPrepared), ' ', '-')"/></xsl:variable>
				<xsl:value-of select="$decision"/> dcterms:subject &lt;<xsl:value-of select="$subjectPrepared2"/>&gt; .
				&lt;<xsl:value-of select="$subjectPrepared2"/>&gt;  a skos:Concept .
				&lt;<xsl:value-of select="$subjectPrepared2"/>&gt; skos:inScheme &lt;http://linked.opendata.cz/ontology/legislation/cz/DecisionSubjectsScheme&gt; .
				
				
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:variable name="subjectPrepared"><xsl:value-of select="normalize-space(substring-before($pText, '/'))"/></xsl:variable>
				<xsl:variable name="subjectPrepared2">http://linked.opendata.cz/resource/legislation/cz/decision-subject/<xsl:value-of select="replace(lower-case($subjectPrepared), ' ', '-')"/></xsl:variable>
				<xsl:value-of select="$decision"/> dcterms:subject &lt;<xsl:value-of select="$subjectPrepared2"/>&gt; .
				&lt;<xsl:value-of select="$subjectPrepared2"/>&gt;  a skos:Concept .
				&lt;<xsl:value-of select="$subjectPrepared2"/>&gt; skos:inScheme &lt;http://linked.opendata.cz/ontology/legislation/cz/DecisionSubjectsScheme&gt; .
				
				
				
				<!-- Process the rest -->
				<xsl:call-template name="parseSubjects">
					<xsl:with-param name="pText" select=
						"normalize-space(substring-after($pText, '/'))"/>
					<xsl:with-param name="decision"><xsl:value-of select="$decision"/></xsl:with-param>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
		</xsl:if>
	</xsl:template>
	
	
	<!-- 
		**************************
		BASIC METADATA FOR NSOUD 
		**************************
	-->  
	<xsl:template name="createDecisionAndFileBasicRecordNS">
		<xsl:param name="decisionWithoutBrackets"> </xsl:param>
		<xsl:param name="decisionIdentifierInURI"/> <!-- Typically it is spisova znacka (with dashes), but it could be also cislo jednaci -->
		<xsl:param name="file"> </xsl:param> 
		<xsl:param name="fileIdentifierInURI"/> <!-- Spisova znacka (with dashes) -->
		<xsl:param name="yearInURIFull"/> 
		<xsl:param name="court"></xsl:param>
		
		<xsl:variable name="decision">&lt;<xsl:value-of select="$decisionWithoutBrackets"/>&gt;</xsl:variable>
		
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
				<xsl:variable name="decisionIdentifierText"><xsl:value-of select='concat($senat," ",$fileKind," ",$fileNumber,"/",$yearInURIFull, "-", $decisionNumber)'/></xsl:variable>
				<xsl:value-of select="$decision"/> dcterms:identifier "<xsl:value-of select="$decisionIdentifierText"/>" . 
				<xsl:value-of select="$decision"/> dcterms:title "<xsl:value-of select="$decisionIdentifierText"/>" .
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="decisionIdentifierText"><xsl:value-of select='concat($senat," ",$fileKind," ",$fileNumber,"/",$yearInURIFull)'/></xsl:variable>
				<xsl:value-of select="$decision"/> dcterms:identifier "<xsl:value-of select="$decisionIdentifierText"/>" . 
				<xsl:value-of select="$decision"/> dcterms:title "<xsl:value-of select="$decisionIdentifierText"/>" . 
			</xsl:otherwise>
		</xsl:choose>
		
		<!-- ECLI -->
		<xsl:if test="string-length($yearInURIFull) > 0"> <!-- we have the year when it was issued -->
			<xsl:if test="matches($yearInURIFull,'^[0-9]{4}$')"> <!-- year is in the right format -->
				<xsl:variable name="spisovaZnackaECLITemp"><xsl:value-of select='upper-case(replace($decisionIdentifierInURI,"-","."))'/></xsl:variable>
				
				<xsl:choose>
					<xsl:when test="string-length($decisionNumber) > 0"> 
						<!-- cislo jednaci is available in spisova znacka URI -->
						<xsl:variable name="spisovaZnackaECLI">ECLI:CZ:NS:<xsl:value-of select="$yearInURIFull"/>:<xsl:value-of select="$spisovaZnackaECLITemp"/></xsl:variable>
						<xsl:value-of select="$decision"/> dcterms:identifier "<xsl:value-of select="$spisovaZnackaECLI"/>" . 
					</xsl:when>
					<xsl:otherwise>
						<!-- spisova znacka neobsahuje cislo jednaci - pridame .1 -->
						<xsl:variable name="spisovaZnackaECLI">ECLI:CZ:NS:<xsl:value-of select="$yearInURIFull"/>:<xsl:value-of select="$spisovaZnackaECLITemp"/>.1</xsl:variable>
						<xsl:value-of select="$decision"/> dcterms:identifier "<xsl:value-of select="$spisovaZnackaECLI"/>" . 
					</xsl:otherwise>
				</xsl:choose>
				
				
			</xsl:if>
		</xsl:if>
		
		
		<xsl:variable name="fileIdentifierText"><xsl:value-of select='concat($senat," ",$fileKind," ",$fileNumber,"/",$yearInURIFull)'/></xsl:variable>
		
		<!-- Create new file object -->
		<xsl:value-of select="$file"/> a lex:File .
		<xsl:value-of select="$file"/> dcterms:identifier "<xsl:value-of select="$fileIdentifierText"/>" . 
		<xsl:value-of select="$file"/> dcterms:title "<xsl:value-of select="$fileIdentifierText"/>" . 
		<xsl:value-of select="$file"/> lex:senateNumber "<xsl:value-of select="$senat"/>" .
		<xsl:value-of select="$file"/> lex:fileKind "<xsl:value-of select="$fileKind"/>" . 
		<xsl:value-of select="$file"/> lex:fileNumber "<xsl:value-of select="$fileNumber"/>"^^xsd:positiveInteger .
		<xsl:value-of select="$file"/> lex:fileYear "<xsl:value-of select="$yearInURIFull"/>" . <!-- ^^xsd:gYear . -->
		
		
		<xsl:value-of select="$decision"/> lex:belongsToFile <xsl:value-of select="$file"/> .
		
		
		
		<!-- Create new expression and manifestation -->	
		<xsl:variable name="decExprCore"><xsl:value-of select="$decisionWithoutBrackets"/>/expression/<xsl:value-of select='substring-after($decisionWithoutBrackets,"http://linked.opendata.cz/resource/legislation/")'></xsl:value-of>/cs</xsl:variable>
		<xsl:variable name="decExpr">&lt;<xsl:value-of select="$decExprCore"></xsl:value-of>&gt;</xsl:variable>
		
		<!-- Create new decision expression  -->
		<xsl:value-of select="$decExpr"/> a  frbr:Expression .
		<xsl:value-of select="$decExpr"/> frbr:realizationOf <xsl:value-of select="$decision"/> .
		
		<!-- Prepare new manifestation  -->
		<!-- sample manifestation: http://www.nsoud.cz/Judikatura/judikatura_ns.nsf/$$WebSearch1?SearchView&Query=%5Bspzn1%5D%20%3D%2021%20AND%20%5Bspzn2%5D%3DCdo%20AND%20%5Bspzn3%5D%3D54%20AND%20%5Bspzn4%5D%3D2013 -->
		<!-- Decoded URI: http://www.nsoud.cz/Judikatura/judikatura_ns.nsf/$$WebSearch1?SearchView&Query=[spzn1] = 21 AND [spzn2]=Cdo AND [spzn3]=54 AND [spzn4]=2013 -->
		
		<xsl:variable name="decMani">&lt;http://www.nsoud.cz/Judikatura/judikatura_ns.nsf/$$WebSearch1?SearchView&amp;Query=%5Bspzn1%5D%20%3D%20<xsl:value-of select='$senat'/>%20AND%20%5Bspzn2%5D%3D<xsl:value-of select='$fileKind'/>%20AND%20%5Bspzn3%5D%3D<xsl:value-of select='$fileNumber'/>%20AND%20%5Bspzn4%5D%3D<xsl:value-of select='$yearInURIFull'/>&gt;</xsl:variable>
		<!-- new manifestation is created when the court is prepared - it is created only for decisions of nejvyssi soud -->
		
		
		<!-- create a court -->
		<!-- Check that there is certain court -->
		<xsl:if test="string-length($court) > 2"> <!-- not just <>-->
			
			<xsl:choose>
				<xsl:when test="contains($court,'soud-dovolaci') or contains($court,'dovolaci-soud')">
					<xsl:variable name="courtFinal">&lt;http://linked.opendata.cz/resource/court/cz/nejvyssi-soud&gt;</xsl:variable>
					
					<xsl:if test="string-length($courtFinal) > 2">
						<xsl:value-of select="$courtFinal"/> a lex:Court .
						
						<xsl:value-of select="$file"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .		
						<xsl:value-of select="$decision"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .
						
						<!-- Create manifesatation jen pokud je to rozhodnuti nejvyssiho soudu -->
						<xsl:if test="contains($courtFinal,'nejvyssi-soud')">
							<xsl:value-of select="$decMani"/> a  frbr:Manifestation .
							<xsl:value-of select="$decMani"/> frbr:embodimentOf <xsl:value-of select="$decExpr"/> .
						</xsl:if>
						
					</xsl:if>
					
				</xsl:when>
				<xsl:when test="contains($court,'soud-prvni-stupne-pro') or contains($court,'soud-prvni-stupne')">
					<xsl:variable name="courtFinal"><xsl:value-of select="$soudPrvniStupen"></xsl:value-of></xsl:variable>
					
					<xsl:if test="string-length($courtFinal) > 2">
						<xsl:value-of select="$courtFinal"/> a lex:Court .
						
						<xsl:value-of select="$file"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .		
						<xsl:value-of select="$decision"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .
						
						<!-- Create manifesatation jen pokud je to rozhodnuti nejvyssiho soudu -->
						<xsl:if test="contains($courtFinal,'nejvyssi-soud')">
							<xsl:value-of select="$decMani"/> a  frbr:Manifestation .
							<xsl:value-of select="$decMani"/> frbr:embodimentOf <xsl:value-of select="$decExpr"/> .
						</xsl:if>
						
					</xsl:if>
					
				</xsl:when>
				<xsl:when test="contains($court,'odvolaci-soud') or contains($court,'soud-druhy-stupne') or contains($court,'soud-druhy-stupne-pro')">
					<xsl:variable name="courtFinal"><xsl:value-of select="$soudDruhyStupen"></xsl:value-of></xsl:variable>
					
					<xsl:if test="string-length($courtFinal) > 2">
						<xsl:value-of select="$courtFinal"/> a lex:Court .
						
						<xsl:value-of select="$file"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .		
						<xsl:value-of select="$decision"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .
						
						<!-- Create manifesatation jen pokud je to rozhodnuti nejvyssiho soudu -->
						<xsl:if test="contains($courtFinal,'nejvyssi-soud')">
							<xsl:value-of select="$decMani"/> a  frbr:Manifestation .
							<xsl:value-of select="$decMani"/> frbr:embodimentOf <xsl:value-of select="$decExpr"/> .
						</xsl:if>
						
					</xsl:if>
					
					
				</xsl:when>
				<xsl:otherwise>
					<xsl:variable name="courtFinal"><xsl:value-of select="$court"/></xsl:variable>
					
					<xsl:if test="string-length($courtFinal) > 2">
						<xsl:value-of select="$courtFinal"/> a lex:Court .
						
						<xsl:value-of select="$file"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .		
						<xsl:value-of select="$decision"/>  dcterms:creator <xsl:value-of select="$courtFinal"/> .
						
						<!-- Create manifesatation jen pokud je to rozhodnuti nejvyssiho soudu -->
						<xsl:if test="contains($courtFinal,'nejvyssi-soud')">
							<xsl:value-of select="$decMani"/> a  frbr:Manifestation .
							<xsl:value-of select="$decMani"/> frbr:embodimentOf <xsl:value-of select="$decExpr"/> .
						</xsl:if>
						
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
			
			
			
			
			
			
		</xsl:if>
		<!-- end of creating court-->
		
		
		
		
		
		
		
		
	</xsl:template>
	  
	  
	<!-- 
		**************************
		BASIC METADATA FOR USOUD 
		**************************
	-->  
	<xsl:template name="createDecisionAndFileBasicRecordUS">
		<xsl:param name="decisionWithoutBrackets"> </xsl:param>
		<xsl:param name="decisionIdentifierInURI"/> <!-- Spisova znacke (with dashes) -->
		<xsl:param name="file"> </xsl:param> 
		<xsl:param name="fileIdentifierInURI"/> <!-- Spisova znacka (with dashes). For usoud, it is the same as decisionsIdentifierInURI -->
		<xsl:param name="yearInURIFull"/> <!-- Full year, e.g., 2014. For USOUD, year in the form 14 is used in spisova znacka). 
			                               Such year should be also used when building ECLI (NSOUD) -->
		
	
		<xsl:param name="ecli"></xsl:param>
		<xsl:param name="manifestationLink"></xsl:param>
		<xsl:param name="court"></xsl:param>
		
		<xsl:variable name="decision">&lt;<xsl:value-of select="$decisionWithoutBrackets"/>&gt;</xsl:variable>
		
		<!-- Metadata for decision/file-->
		<xsl:variable name="senat"><xsl:value-of select='substring-before($decisionIdentifierInURI,"-")'/></xsl:variable>
		
		<xsl:variable name="fileKind"><xsl:value-of select='substring-before(substring-after($decisionIdentifierInURI,"-"),"-")'/></xsl:variable>
			
		<xsl:variable name="fileNumber"><xsl:value-of select='substring-before(substring-after(substring-after($decisionIdentifierInURI,"-"),"-"),"-")'/></xsl:variable>
		
		<xsl:variable name="yearInSpZn"><xsl:value-of select='substring-after(substring-after(substring-after($decisionIdentifierInURI,"-"),"-"),"-")'/></xsl:variable>
		
		<!-- year is already available -->
		
		<!-- Create new decision object -->
		<xsl:value-of select="$decision"/> a lex:Decision .
		
		<xsl:variable name="decisionIdentifierText"><xsl:value-of select='concat($senat," ",$fileKind," ",$fileNumber,"/",$yearInSpZn)'/></xsl:variable>
		<xsl:value-of select="$decision"/> dcterms:identifier "<xsl:value-of select="$decisionIdentifierText"/>" . 
		<xsl:value-of select="$decision"/> dcterms:title "<xsl:value-of select="$decisionIdentifierText"/>" . 
		
		<!-- ECLI directly taken from metadata. Available only for decisions directly obtained with metadat from nalus.usoud. 
			  Problem when trying to build directly - information about the document number typically missing in the created URI 
			  Sample: ECLI :CZ :US :2014 :1.US.672.14.1-->
		<xsl:if test="string-length($ecli) > 0"> <!-- we have the year when it was issued -->
			
				<xsl:value-of select="$decision"/> dcterms:identifier "<xsl:value-of select="$ecli"/>" . 
			
		</xsl:if>
		
		<xsl:variable name="fileIdentifierText"><xsl:value-of select='concat($senat," ",$fileKind," ",$fileNumber,"/",$yearInSpZn)'/></xsl:variable>

		<!-- Create new file object -->
		<xsl:value-of select="$file"/> a lex:File .
		<xsl:value-of select="$file"/> dcterms:identifier "<xsl:value-of select="$fileIdentifierText"/>" . 
		<xsl:value-of select="$file"/> dcterms:title "<xsl:value-of select="$fileIdentifierText"/>" . 
		<xsl:if test="matches($senat, '^[IVXLCDM]+$')"> <!-- je to rimske cislo -->
			<xsl:value-of select="$file"/> lex:senateNumber "<xsl:value-of select="$senat"/>" .
		</xsl:if>
		<!--  No fileKind for USOUD  
			<xsl:value-of select="$file"/> lex:fileKind "<xsl:value-of select="$fileKind"/>" . -->
		<xsl:value-of select="$file"/> lex:fileNumber "<xsl:value-of select="$fileNumber"/>"^^xsd:positiveInteger .
		<xsl:value-of select="$file"/> lex:fileYear "<xsl:value-of select="$yearInURIFull"/>" . <!-- ^^xsd:gYear . -->
		
		<xsl:value-of select="$decision"/> lex:belongsToFile <xsl:value-of select="$file"/> .
		
		
		<!-- Create new expression and manifestation -->	
		<xsl:variable name="decExprCore"><xsl:value-of select="$decisionWithoutBrackets"/>/expression/<xsl:value-of select='substring-after($decisionWithoutBrackets,"http://linked.opendata.cz/resource/legislation/")'></xsl:value-of>/cs</xsl:variable>
		<xsl:variable name="decExpr">&lt;<xsl:value-of select="$decExprCore"></xsl:value-of>&gt;</xsl:variable>
		
		<!-- Create new decision expression  -->
		<xsl:value-of select="$decExpr"/> a  frbr:Expression .
		<xsl:value-of select="$decExpr"/> frbr:realizationOf <xsl:value-of select="$decision"/> .
		
		<!-- Prepare new manifestation  -->
	
	    <!--	
		Na konkrétní rozhodnutí či jeho abstrakt můžete odkázat pevným odkazem. Pro text rozhodnutí je formát URL v podobě 
		http://nalus.usoud.cz/Search/GetText.aspx?sz=3-1076-07_1. Za parametrem sz se uvede spisová značka rozhodnutí v předdefinovaném formátu 
		s rozlišovacím indexem, např. pro III. ÚS 1076/07 #1 ve formátu 3-1076-07_1, pro Pl. ÚS 45/06 #2 ve formátu Pl-45-06_2, 
		pro stanovisko pléna Pl. ÚS-st 25/08 #1 ve formátu St-25-08_1. Pro pevné odkazy na abstrakt rozhodnutí použijte URL 
		ve formátu http://nalus.usoud.cz/Search/GetAbstract.aspx?sz=3-1076-07_1. Pevný odkaz lze najít ke každému rozhodnutí 
		i po kliknutí na ikonu Pevný odkaz. 
		
		Odkaz je bran z metadata. -->
		
		<xsl:if test="string-length($manifestationLink) > 0"> <!-- there is manifestation obtained from metadata -->
			<xsl:variable name="decMani">&lt;<xsl:value-of select="$manifestationLink"></xsl:value-of>&gt;</xsl:variable>
			
			<xsl:value-of select="$decMani"/> a  frbr:Manifestation .
			<xsl:value-of select="$decMani"/> frbr:embodimentOf <xsl:value-of select="$decExpr"/> .
		</xsl:if>	
		
		<!-- create a court - alwasy ustavni soud -->
		<xsl:variable name="court">&lt;http://linked.opendata.cz/resource/court/cz/ustavni-soud&gt;</xsl:variable>
		
		<xsl:value-of select="$court"/> a lex:Court .
		
		<xsl:value-of select="$file"/>  dcterms:creator <xsl:value-of select="$court"/> .		
		<xsl:value-of select="$decision"/>  dcterms:creator <xsl:value-of select="$court"/> .

		<!-- end of creating court-->
		
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
		
	
	
		
		
		
		
	
		
		<!--parse URI to get identifier for decision and parse identifier for file -->
		
		<xsl:variable name="year"><xsl:value-of select="substring(substring-after($decisionURI,'decision/'),1,4)"/></xsl:variable> 
		<xsl:variable name="IdentifierInURIForDecision"><xsl:value-of select="substring-after(substring-after($decisionURI,'decision/'), '/')" /></xsl:variable>
		<!-- in case there is also decision number, e.g. 56-Co-97-2011-29, remove the last number (-29)-->
		<xsl:variable name="IdentifierInURIForFile"><xsl:value-of select="replace($IdentifierInURIForDecision,'(^[0-9]+-[a-zA-Z]+-[0-9]+-[0-9]{4})(-[0-9]+){0,1}$', '$1')" /></xsl:variable>
		
		
		<!-- Create new file-->
		<!-- if available, the last number in: 56-Co-97-2011-29 -->
		<xsl:variable name="file">&lt;http://linked.opendata.cz/resource/legislation/cz/file/<xsl:value-of select="$year"/>/<xsl:value-of select="$IdentifierInURIForFile"/>&gt;</xsl:variable> 
		
		<!-- Check that spisova znacka has reasonable format for NS. If not, skip the generation of metadata-->
		<xsl:choose>
			<xsl:when test="matches($year,'^[0-9]{4}$') and matches($IdentifierInURIForDecision,'-US-[0-9]+-[0-9]+(-[0-9]+){0,1}$')">
				<!--USOUD-->
				<xsl:call-template name="createDecisionAndFileBasicRecordUS">
					<xsl:with-param name="decisionWithoutBrackets"><xsl:value-of select="$decisionURI"/></xsl:with-param>
					<xsl:with-param name="decisionIdentifierInURI"><xsl:value-of select="$IdentifierInURIForDecision"/></xsl:with-param>
					<xsl:with-param name="file"><xsl:value-of select="$file"/></xsl:with-param>
					<xsl:with-param name="fileIdentifierInURI"><xsl:value-of select="$IdentifierInURIForFile"/></xsl:with-param>
					<xsl:with-param name="yearInURIFull"><xsl:value-of select="$year"/></xsl:with-param>
					
					<xsl:with-param name="court"><xsl:value-of select="$court"/></xsl:with-param>
				</xsl:call-template>
			
				
				
			</xsl:when>
			  
			<xsl:when test="matches($year,'^[0-9]{4}$') and matches($IdentifierInURIForDecision,'^[0-9]+-[a-zA-Z]+-[0-9]+-[0-9]{4}(-[0-9]+){0,1}$')">
				<!--NSOUD-->
				<xsl:call-template name="createDecisionAndFileBasicRecordNS">
					<xsl:with-param name="decisionWithoutBrackets"><xsl:value-of select="$decisionURI"/></xsl:with-param>
					<xsl:with-param name="decisionIdentifierInURI"><xsl:value-of select="$IdentifierInURIForDecision"/></xsl:with-param>
					<xsl:with-param name="file"><xsl:value-of select="$file"/></xsl:with-param>
					<xsl:with-param name="fileIdentifierInURI"><xsl:value-of select="$IdentifierInURIForFile"/></xsl:with-param>
					<xsl:with-param name="yearInURIFull"><xsl:value-of select="$year"/></xsl:with-param>
					
					<xsl:with-param name="court"><xsl:value-of select="$court"/></xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise><!-- DO NOT PROCESS --></xsl:otherwise>
		</xsl:choose>
	
	</xsl:template>
	

	
	
	
	

    
	<xsl:template match="text()" />    
    
	<!-- <xsl:template match="/document/body/*">	
       <xsl:value-of select="$expression" /> lex:refersTo  <xsl:value-of select="@rdf:about" />
    </xsl:template>-->
</xsl:stylesheet>
