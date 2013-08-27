<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://xrg.cz/link/sourceOfLaw/1" xpath-default-namespace="http://xrg.cz/link/sourceOfLaw/1">
	<xsl:output method="text" encoding="UTF-8" indent="yes"/>


    <!-- Spisova znacka (taken from file name) -->
	<xsl:variable name="path"><xsl:value-of select="base-uri()" /></xsl:variable>
	<xsl:variable name="spisovaZnackaBase"><xsl:value-of select='substring($path,0,string-length($path)-3)'/></xsl:variable>
	
	<xsl:variable name="spisovaZnackaBase2"><xsl:value-of select='substring($spisovaZnackaBase,string-length(substring-before($spisovaZnackaBase,"rozhodnuti"))+12)'/></xsl:variable>
	<xsl:variable name="spisovaZnackaBase3"><xsl:value-of select='replace($spisovaZnackaBase2,"_","-")'/></xsl:variable>	
	
	<!-- Spisova znacka for use in URI -->
	<xsl:variable name="spisovaZnacka"><xsl:value-of select='lower-case($spisovaZnackaBase3)'/></xsl:variable>
	
	<!-- Spisova znacka for use as value of dcterms:identier/title -->
	<xsl:variable name="spisovaZnackaIdentifierTemp"><xsl:value-of select='replace($spisovaZnackaBase2,"_"," ")'/></xsl:variable>
	<xsl:variable name="spisovaZnackaIdentifier"><xsl:value-of select='concat(substring($spisovaZnackaIdentifierTemp,1,string-length($spisovaZnackaIdentifierTemp)-5),"/",substring($spisovaZnackaIdentifierTemp,string-length($spisovaZnackaIdentifierTemp)-3))'/></xsl:variable>
	
	<!-- ECLI -->
	<xsl:variable name="spisovaZnackaECLITemp"><xsl:value-of select='upper-case(replace($spisovaZnackaBase3,"-","."))'/></xsl:variable>
	<xsl:variable name="spisovaZnackaECLI">ECLI:CZ:NS:<xsl:value-of select="$yearDate"/>:<xsl:value-of select="$spisovaZnackaECLITemp"/>.1</xsl:variable>
	
	<!-- Year for use in URI -->
	<xsl:variable name="yearSpisZnacka"><xsl:value-of select='substring($spisovaZnacka,string-length($spisovaZnacka)-3)'/></xsl:variable>
	
	
	<!-- Decision metadata -->
		<!-- Date when issued -->
		<xsl:variable name="bodyText"><xsl:copy-of select='/document/body'/></xsl:variable>
		<xsl:variable name="decDate1"><xsl:value-of select='substring-after($bodyText,"Datum rozhodnutí:")'/></xsl:variable>
		<xsl:variable name="decDate2"><xsl:value-of select='replace(normalize-space(substring($decDate1,0,12)),"/","-")'/></xsl:variable>	
		<xsl:variable name="decDate"><xsl:value-of select='$decDate2'/></xsl:variable>
		
		<xsl:variable name="yearDate"><xsl:value-of select='substring($decDate,7)'/></xsl:variable> 
		<xsl:variable name="month"><xsl:value-of select='substring($decDate,4,2)'/></xsl:variable>
		<xsl:variable name="day"><xsl:value-of select='substring($decDate,1,2)'/></xsl:variable>
		<xsl:variable name="normDecDate"><xsl:value-of select='$yearDate'/>-<xsl:value-of select='$month'/>-<xsl:value-of select='$day'/></xsl:variable>
		<!-- TODO date better-->
	
		<!-- lex:decisionKind = USNESENI. Takes everything after Typ rozhodnuti :, but before space (elem </paragraph> does not occur in the bodyText, only text is extracted.-->
	    <xsl:variable name="decisionKindTemp"><xsl:value-of select='normalize-space(substring-after($bodyText,"Typ rozhodnutí :"))'/></xsl:variable>
	<xsl:variable name="decisionKind"><xsl:value-of select='substring-before($decisionKindTemp," ")'/></xsl:variable>
	 
	
		<!-- lex:decisionCategory = D, see decisionKind-->
	
	<xsl:variable name="decisionCategoryTemp"><xsl:value-of select='normalize-space(substring-after($bodyText,"Kategorie rozhodnutí :"))'/></xsl:variable>
	<xsl:variable name="categoryCategory"><xsl:value-of select='substring-before($decisionCategoryTemp," ")'/></xsl:variable>

	

	<!-- Variables Decision (Work, Expre, Mani) & File-->
	
	<!--<xsl:variable name="decCore"><xsl:value-of select="lower-case($spisovaZnacka)"/></xsl:variable>-->
	<xsl:variable name="decPrefix">http://linked.opendata.cz/resource/legislation/cz/decision/<xsl:value-of select="$yearSpisZnacka"/>/<xsl:value-of select="$spisovaZnacka"/></xsl:variable>
	
	<xsl:variable name="decision">&lt;<xsl:value-of select="$decPrefix"/>&gt;</xsl:variable>
	<xsl:variable name="decExpr">&lt;<xsl:value-of select="$decPrefix"/>/expression&gt;</xsl:variable>
	
	<xsl:variable name="decExprSection">&lt;<xsl:value-of select="$decPrefix"/>/expression/section&gt;</xsl:variable>
	<xsl:variable name="decExprParaCore">&lt;<xsl:value-of select="$decPrefix"/>/expression/section/paragraph/</xsl:variable> <!-- para number should be add -->
	
	<xsl:variable name="filename"><xsl:value-of select='substring($path,string-length(substring-before($path, "rozhodnuti"))+1)'/></xsl:variable>
	<xsl:variable name="decMani">&lt;<xsl:value-of select="$decPrefix"/>/manifestation&gt;</xsl:variable>
	


	
	<xsl:template match="/" >
		
		
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
		

        <!-- OBJECTS FOR THE PROCESSED DECISION -->

	
	
        <!-- Create new decision object -->
		<xsl:value-of select="$decision"/> a lex:Decision .
		<xsl:if test="contains($normDecDate,'20')"> <!-- test that the date was extracted -->
		    <xsl:value-of select="$decision"/> dcterms:issued "<xsl:value-of select="$normDecDate"/>"^^xsd:date .
		</xsl:if>
	    <xsl:value-of select="$decision"/> dcterms:identifier "<xsl:value-of select="$spisovaZnackaECLI"/>" . 
		<xsl:value-of select="$decision"/> dcterms:identifier "<xsl:value-of select="$spisovaZnackaIdentifier"/>" . 
		<xsl:value-of select="$decision"/> dcterms:title "<xsl:value-of select="$spisovaZnackaIdentifier"/>" . 
		<xsl:value-of select="$decision"/> lex:decisionKind &lt;http://linked.opendata.cz/resource/legislation/cz/decision-kind/supreme-court/<xsl:value-of select="replace(lower-case($decisionKind),' ','-')"/>&gt; .
		&lt;http://linked.opendata.cz/resource/legislation/cz/decision-kind/supreme-court/<xsl:value-of select="replace(lower-case($decisionKind),' ','-')"/>&gt; skos:prefLabel "<xsl:value-of select="$decisionKind"/>" . 
		<xsl:value-of select="$decision"/> lex:decisionCategory &lt;http://linked.opendata.cz/resource/legislation/cz/decision-category/supreme-court/<xsl:value-of select="replace(lower-case($categoryCategory),' ','-')"/>&gt; .  
		&lt;http://linked.opendata.cz/resource/legislation/cz/decision-category/supreme-court/<xsl:value-of select="replace(lower-case($categoryCategory),' ','-')"/>&gt; skos:prefLabel "<xsl:value-of select="$categoryCategory"/>" . 
		
		<xsl:variable name="subjectTemp"><xsl:value-of select='substring-after($bodyText,"Heslo :")'/></xsl:variable>
		<xsl:variable name="subjectTemp2"><xsl:value-of select='substring-before($subjectTemp,"Dotčené předpisy :")'/></xsl:variable>
		
		<xsl:call-template name="parseSubjects">
			<xsl:with-param name="pText"><xsl:value-of select="$subjectTemp2"/></xsl:with-param>
			<xsl:with-param name="decision"><xsl:value-of select="$decision"/></xsl:with-param>
		</xsl:call-template>
	
		<!-- Create new decision expression and manifestation -->
		<xsl:value-of select="$decExpr"/> a  frbr:Expression .
		<xsl:value-of select="$decExpr"/> frbr:realizationOf <xsl:value-of select="$decision"/> .
		
		<xsl:value-of select="$decMani"/> a  frbr:Manifestation .
		<xsl:value-of select="$decMani"/> frbr:embodimentOf <xsl:value-of select="$decExpr"/> .
		<!-- TODO THis should be RDFa representation -->
		<xsl:value-of select="$decMani"/> dcterms:source """<xsl:value-of select="$filename"/>""" .
	
	   <!-- create file for the processed decision -->
	<xsl:variable name="file">&lt;http://linked.opendata.cz/resource/legislation/cz/file/<xsl:value-of select="$yearSpisZnacka"/>/<xsl:value-of select="$spisovaZnacka"/>&gt;</xsl:variable>
	
	
	   <xsl:call-template name="createFileFor">
	   	<xsl:with-param name="spisovaZnacka"><xsl:value-of select="$spisovaZnacka"/></xsl:with-param>
	   	<xsl:with-param name="identifier"><xsl:value-of select="$spisovaZnackaIdentifier"/></xsl:with-param>
	   	<xsl:with-param name="yearSpisZnacka"><xsl:value-of select="$yearSpisZnacka"/></xsl:with-param>
	   	<xsl:with-param name="file"><xsl:value-of select="$file"/></xsl:with-param>
	   </xsl:call-template>
	  
	    <xsl:value-of select="$decision"/> lex:belongsToFile <xsl:value-of select="$file"/> .
	
	    <!-- Create new court  -->
	
		<!-- Court -selects the first institution, which is always the institution responsible for the processed decision-->
		<xsl:variable name="court">&lt;<xsl:value-of select='(/document/body/paragraph/institution/@rdf:about)[1]'/>&gt;</xsl:variable>
	
		<xsl:value-of select="$court"/> a lex:Court .
	    <xsl:value-of select="$file"/>  dcterms:creator <xsl:value-of select="$court"/> .
	
		<xsl:value-of select="$decision"/>  dcterms:creator <xsl:value-of select="$court"/> .
		
	
	    <!-- Create new decision expression and manifestation -->
	    <xsl:value-of select="$decExpr"/> a  sdo:Publication .
	    <xsl:value-of select="$decExpr"/>	sdo:hasSection <xsl:value-of select="$decExprSection"/> .
	<xsl:value-of select="$decExprSection"/> a  sdo:Section .
	
	   
	
		
	    <!-- Following should be called for individual paragraphs -->
       <xsl:for-each select="//paragraph">	
       
	    	
					<xsl:variable name="decExprPara"><xsl:value-of select="$decExprParaCore"/><xsl:number></xsl:number>&gt;</xsl:variable>
		   
       	
			    	
			    	
						<xsl:value-of select="$decExprSection"/> sdo:hasParagraph <xsl:value-of select="$decExprPara"/>. 
						<xsl:value-of select="$decExprPara"/> a sdo:Paragraph .
						<xsl:value-of select="$decExprPara"/> dcterms:description  """<xsl:value-of select="."/>""" .
       					
			    
			    	
					<xsl:call-template name="extractActsCitations">
						<xsl:with-param name="paragraphURI"><xsl:value-of select="$decExprPara"/></xsl:with-param>
						<xsl:with-param name="paragraph"><xsl:copy-of select="."/></xsl:with-param>
					</xsl:call-template>
					
					<xsl:call-template name="extractJudgmentsCitations">
						<xsl:with-param name="paragraphURI"><xsl:value-of select="$decExprPara"/></xsl:with-param>
						<xsl:with-param name="paragraph"><xsl:copy-of  select="."/></xsl:with-param>
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
				<xsl:value-of select="$decision"/> dcterms:subject "<xsl:value-of select="normalize-space($pText)"/>" .
				<!--<xsl:copy-of select="normalize-space($pText)"/>-->
				
				
				
			</xsl:when>
			<xsl:otherwise>
				<!--<xsl:if test="string-length(normalize-space(substring-before($pText, ','))) > 0">
					 <!-\- Take everything before the comma -\->
				     <xsl:value-of select="$decision"/> dcterms:subject "<xsl:value-of select="normalize-space(substring-before($pText, ','))"/>" .
				</xsl:if>
				-->
				<xsl:value-of select="$decision"/> dcterms:subject "<xsl:value-of select="normalize-space(substring-before($pText, ','))"/>" .
				
				
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
	  
	  
	<xsl:template name="createFileFor">
		<xsl:param name="spisovaZnacka"/>
		<xsl:param name="identifier"/>
		<xsl:param name="yearSpisZnacka"/>
		<xsl:param name="file"> </xsl:param>
		
		<!-- File metadata -->
		<xsl:variable name="senat"><xsl:value-of select='substring-before($spisovaZnacka,"-")'/></xsl:variable>
		<xsl:variable name="fileKind"><xsl:value-of select='substring-before(substring-after($spisovaZnacka,"-"),"-")'/></xsl:variable>
		<xsl:variable name="fileNumber"><xsl:value-of select='substring-before(substring-after(substring-after($spisovaZnacka,"-"),"-"),"-")'/></xsl:variable>
		<!--<xsl:variable name="fileYear"><xsl:value-of select='substring-after(substring-after(substring-after($spisovaZnacka,"-"),"-"),"-")'/></xsl:variable>-->
		

		
		<xsl:value-of select="$file"/> a lex:File .
		<xsl:value-of select="$file"/> dcterms:identifier "<xsl:value-of select="$identifier"/>" . 
		<xsl:value-of select="$file"/> dcterms:title "<xsl:value-of select="$identifier"/>" . 
		<xsl:value-of select="$file"/> lex:senateNumber "<xsl:value-of select="$senat"/>" .
		<xsl:value-of select="$file"/> lex:fileKind "<xsl:value-of select="$fileKind"/>" . 
		<xsl:value-of select="$file"/> lex:fileNumber "<xsl:value-of select="$fileNumber"/>"^^xsd:positiveInteger .
		<xsl:value-of select="$file"/> lex:fileYear "<xsl:value-of select="$yearSpisZnacka"/>"^^xsd:gYear . 
		
		<!-- ustavni soud! <judgment id="60" label="C38" name="" rdf:about="http://linked.opendata.cz/resource/legislation/cz/judgment/2003/ii-us-279-03" refers_to="33">sp. zn. II. ÚS 279/03</judgment>
		-->
		
	</xsl:template>
	  
	  
	<xsl:template name="textChunkCreation">
		<xsl:param name="paragraphURI"/>
		<xsl:param name="uri"/>
		<xsl:param name="id"/>
		<xsl:param name="generatedID"/>
		
		<xsl:if test="string-length($uri)>2"> <!-- uri might be only <>, in that case we should skip-->
			
			<xsl:variable name="textChunk">&lt;http://linked.opendata.cz/resource/legislation/cz/decision/<xsl:value-of select="$yearSpisZnacka"/>/<xsl:value-of select="$spisovaZnacka"/>/textchunk/<xsl:value-of select="$id"/>&gt;</xsl:variable>	
			<xsl:variable name="anot">&lt;http://linked.opendata.cz/resource/legislation/cz/decision/<xsl:value-of select="$yearSpisZnacka"/>/<xsl:value-of select="$spisovaZnacka"/>/annotation/<xsl:value-of select="$generatedID"/>&gt;</xsl:variable>	
			
			
			<xsl:value-of select="$paragraphURI"/> sdo:hasTextChunk <xsl:value-of select="$textChunk"/> .
			<xsl:value-of select="$textChunk"/> a sdo:TextChunk .
			<xsl:value-of select="$textChunk"/> dcterms:identifier "<xsl:value-of select="$id"/>" .
			<xsl:value-of select="$textChunk"/> sdo:hasAnnotation <xsl:value-of select="$anot"/> .
			<xsl:if test="starts-with($uri, '&lt;http://')">
				<xsl:value-of select="$anot"/> sao:hasTopic <xsl:value-of select="$uri"/> .
			</xsl:if>
		</xsl:if>
		
	</xsl:template>
	  
	  
	<xsl:template name="extractActsCitations">
		<xsl:param name="paragraphURI"></xsl:param>
		<xsl:param name="paragraph"></xsl:param>
		
		<xsl:for-each select="act">	
			
			<xsl:variable name="actId"><xsl:value-of select='@id'/></xsl:variable>
			<xsl:variable name="generatedID"><xsl:value-of select="generate-id()"/></xsl:variable>
		
			<!-- @rdf:about may contain list of URIs-->
			<xsl:for-each select="tokenize(@rdf:about, ' ')">
				
				
				
				<xsl:call-template name="textChunkCreation">
					<xsl:with-param name="uri">&lt;<xsl:value-of select="."/>&gt;</xsl:with-param>
					<xsl:with-param name="id"><xsl:value-of select="$actId"/></xsl:with-param>
					<xsl:with-param name="generatedID"><xsl:value-of select="$generatedID"/></xsl:with-param>
					<xsl:with-param name="paragraphURI"><xsl:value-of select="$paragraphURI"/></xsl:with-param>
				</xsl:call-template>
				
			
				
			</xsl:for-each>
		
			
			
		</xsl:for-each>
		
	</xsl:template>
	
	
	
	<xsl:template name="extractJudgmentsCitations">
		<xsl:param name="paragraphURI"></xsl:param>
		<xsl:param name="paragraph"></xsl:param>
		
		<xsl:for-each select="judgment">
			
			<xsl:variable name="judgmentId"><xsl:value-of select='@id'/></xsl:variable>
			<xsl:variable name="generatedID"><xsl:value-of select="generate-id()"/></xsl:variable>
			<xsl:variable name="refersTo"><xsl:value-of select='@refers_to'/></xsl:variable>
			<xsl:variable name="root" select="/"/>
			
			<!-- @rdf:about may contain list of URIs-->
			<xsl:for-each select="tokenize(@rdf:about, ' ')">
				
			    
			
				<xsl:call-template name="textChunkCreation">
					<xsl:with-param name="uri">&lt;<xsl:value-of select="."/>&gt;</xsl:with-param>
					<xsl:with-param name="id"><xsl:value-of select="$judgmentId"/></xsl:with-param>
					<xsl:with-param name="generatedID"><xsl:value-of select="$generatedID"/></xsl:with-param>
					<xsl:with-param name="paragraphURI"><xsl:value-of select="$paragraphURI"/></xsl:with-param>
				</xsl:call-template>
			
			    
				<xsl:call-template name="createObjectsForJudgment">
					<xsl:with-param name="decisionURI"><xsl:value-of select="."/></xsl:with-param>
					<xsl:with-param name="refersTo"><xsl:value-of select="$refersTo"/></xsl:with-param>
					<xsl:with-param name="root"><xsl:value-of select="$root"/></xsl:with-param>
					
				</xsl:call-template>
				
			</xsl:for-each>
			
			
		</xsl:for-each>
		
	</xsl:template>
	
	<xsl:template name="createObjectsForJudgment">
		<xsl:param name="decisionURI"></xsl:param>
		<xsl:param name="refersTo"></xsl:param>
		<xsl:param name="root"></xsl:param>
	

		
		<!-- prepare for creation of new objects for decision, expr, man, file -->
		<xsl:variable name="decision">&lt;<xsl:value-of select="$decisionURI"/>&gt;</xsl:variable>
		<xsl:variable name="decExpr">&lt;<xsl:value-of select="$decisionURI"/>/expression&gt;</xsl:variable>
		<xsl:variable name="decMani">&lt;<xsl:value-of select="$decisionURI"/>/manifestation&gt;</xsl:variable>
		
		
		<!-- Create new decision  -->
		<xsl:value-of select="$decision"/> a lex:Decision .
		<!-- not available <xsl:value-of select="$decision"/> dcterms:issued "<xsl:value-of select="$normDecDate"/>"^^xsd:dateTime . -->
		<!-- TODO identifier for refered judgments? title? <xsl:value-of select="$decision"/> dcterms:identifier "<xsl:value-of select="$decCore"/>" . -->
		
		<!-- Create new decision expression and manifestation -->
		<xsl:value-of select="$decExpr"/> a  frbr:Expression .
		<!-- Publication and sections/paragraphs created only for the main processed decision <xsl:value-of select="$decExpr"/> a  sdo:Publication .-->
		<xsl:value-of select="$decExpr"/> frbr:realizationOf <xsl:value-of select="$decision"/> .
		
		<xsl:value-of select="$decMani"/> a  frbr:Manifestation .
		<xsl:value-of select="$decMani"/> frbr:embodimentOf <xsl:value-of select="$decExpr"/> .
		
		<!-- Create new file-->
		<!-- <xsl:variable name="fileYear"><xsl:value-of select="substring(substring-after($decisionURI,'decision/'),1,4)"/></xsl:variable> 
		<xsl:variable name="file">&lt;http://linked.opendata.cz/resource/legislation/cz/file/<xsl:value-of select="$fileYear"/>/<xsl:value-of select="$decisionURI"/>&gt;</xsl:variable> -->
		<!-- TODO HACK CORRECTLY: judgment -> decision -->
		<xsl:variable name="file">&lt;http://linked.opendata.cz/resource/legislation/cz/file/<xsl:value-of select="substring-after($decisionURI,'decision/')"/>&gt;</xsl:variable>
		
		<xsl:value-of select="$file"/> a lex:File .
		
		<!-- TODO metadata for file?
		<xsl:variable name="identifier"><xsl:value-of select="substring-after(substring-after($decisionURI,'decision/'),'/')"/></xsl:variable>
		<xsl:call-template name="createFileFor">
			<xsl:with-param name="spisovaZnacka"><xsl:value-of select="$spisovaZnacka"/></xsl:with-param>
			<xsl:with-param name="identifier"><xsl:value-of select="$decCore"/></xsl:with-param>
			<xsl:with-param name="yearSpisZnacka"><xsl:value-of select="$fileYear"/></xsl:with-param>
			<xsl:with-param name="file"><xsl:value-of select="$file"/></xsl:with-param>
		</xsl:call-template>
		-->
		
		<xsl:value-of select="$decision"/> lex:belongsToFile <xsl:value-of select="$file"/> .
		
		
		<!-- court -->
		<xsl:if test="string-length($refersTo) > 0">
			<!-- <xsl:variable name="refersToVar"><xsl:value-of select="$refersTo"/></xsl:variable>-->
			<xsl:variable name="court">&lt;<xsl:value-of select="$root/document/body/institution[@id=$refersTo]/@rdf:about"/>&gt;</xsl:variable>
			
			<!-- Create new court  -->
			<xsl:if test="string-length($court) > 2"> <!-- not just <>-->
				<xsl:value-of select="$court"/> a lex:Court .
				
				<xsl:value-of select="$file"/>  dcterms:creator <xsl:value-of select="$court"/> .		
				<xsl:value-of select="$decision"/>  dcterms:creator <xsl:value-of select="$court"/> .
			</xsl:if>
			
		</xsl:if> 
	</xsl:template>
	
	
	
	
	

	
	
	
	
	
	
	
	

	
	
	<!-- judikat
	<xsl:template match="/document/body">
		
	
		<xsl:for-each select="act|judgment|act_cnr|act_federal|notice|institution">
			<xsl:value-of select="$expression"/> lex:refersTo  <xsl:value-of select="@rdf:about"/>
		</xsl:for-each>
		<xsl:value-of select="$expression"/> dcterms:description "
      
       
   
    <xsl:apply-templates/>
    "@cs .
    </xsl:template>
 -->
    
	<xsl:template match="text()" />    
    
	<!-- <xsl:template match="/document/body/*">	
       <xsl:value-of select="$expression" /> lex:refersTo  <xsl:value-of select="@rdf:about" />
    </xsl:template>-->
</xsl:stylesheet>
