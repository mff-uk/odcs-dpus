<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://xrg.cz/link/sourceOfLaw/1" xpath-default-namespace="http://xrg.cz/link/sourceOfLaw/1">
	
	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
	
	<!-- Spisova znacka (taken from meta elem section) -->
	<xsl:variable name="metadataText"><xsl:value-of select='/document/body/meta/text()'/></xsl:variable>
	
	<xsl:variable name="spisovaZnackaTemp"><xsl:value-of select='substring-after($metadataText,"Spisová značka :")'/></xsl:variable>
	<xsl:variable name="spisovaZnackaBase"><xsl:value-of select='normalize-space(substring-before($spisovaZnackaTemp,"Datum"))'/></xsl:variable> <!-- Contains spisovou znacku as in the meta section, normalized spaces-->
	<!-- Spisova znacka for use as value of dcterms:identier/title -->
	<!-- <xsl:variable name="spisovaZnackaIdentifier"><xsl:value-of select='lower-case(concat(substring($spisovaZnackaBase,1,string-length($spisovaZnackaBase)-5),"/",substring($spisovaZnackaBase,string-length($spisovaZnackaBase)-3)))'/></xsl:variable>
		-->
	
	<xsl:variable name="spisovaZnackaBaseWithDashTemp"><xsl:value-of select='replace($spisovaZnackaBase," ","-")'/></xsl:variable>
	<xsl:variable name="spisovaZnackaBaseWithDash"><xsl:value-of select='replace($spisovaZnackaBaseWithDashTemp,"/","-")'/></xsl:variable>	
	
	<!-- Spisova znacka for use in URI -->
	<xsl:variable name="spisovaZnackainURI"><xsl:value-of select='$spisovaZnackaBaseWithDash'/></xsl:variable>
	
	
	
	<!-- Spisova znacka for use as value of dcterms:identier/title -->
	<!--<xsl:variable name="spisovaZnackaIdentifier"><xsl:value-of select='concat(substring($spisovaZnackaBase,1,string-length($spisovaZnackaBase)-5),"/",substring($spisovaZnackaBase,string-length($spisovaZnackaBase)-3))'/></xsl:variable>-->
	

	<!-- Year for use in URI -->
	<xsl:variable name="yearSpisZnacka"><xsl:value-of select='substring($spisovaZnackainURI,string-length($spisovaZnackainURI)-3)'/></xsl:variable>
	
	

	
	<!-- URI of the decision -->
	<xsl:variable name="decisionURI">
		<xsl:text>legislation/cz/decision/</xsl:text>
		<xsl:value-of select="$yearSpisZnacka" />
		<xsl:text>/</xsl:text>
		<xsl:value-of select="$spisovaZnackainURI" />
	</xsl:variable>
	
	<!-- URI of the decision expression -->
	<xsl:variable name="decisionExpressionURI">
		<xsl:value-of select="$decisionURI" />
		<xsl:text>/expression</xsl:text>
	</xsl:variable>
	
	<xsl:variable name="decisionExpressionSection">
		<xsl:value-of select="$decisionURI" />
		<xsl:text>/expression/section/1</xsl:text>
	</xsl:variable>
	
	<xsl:variable name="decisionExpressionParagraph-prefix">
		<xsl:value-of select="$decisionExpressionSection" />
		<xsl:text>/paragraph/</xsl:text>
	</xsl:variable>
			
	<xsl:variable name="vocab">
		<xsl:text>http://linked.opendata.cz/resource/</xsl:text>
	</xsl:variable>		
	
	<xsl:template match="content">
		<!-- article holds the decision expression resource -->
		<article typeof="frbr:Expression sdo:Publication" prefix="frbr: http://purl.org/vocab/frbr/core# dcterms: http://purl.org/dc/terms/ sdo: http://salt.semanticauthoring.org/ontologies/sdo# sao: http://salt.semanticauthoring.org/ontologies/sao# lex: http://purl.org/lex#" xml:base="http://linked.opendata.cz/resource/">
			<!--<xsl:attribute name="vocab" select="$vocab" />-->
			<xsl:attribute name="resource" select="$decisionExpressionURI" />
			
						
				<!-- start a new section, decision has only one section-->
				<span rel="sdo:hasSection">
					<section typeof="sdo:Section" >
						<xsl:attribute name="about" select="$decisionExpressionSection" />
						<xsl:apply-templates mode="section" />
					</section>
				</span>

			
			<footer>
				<xsl:apply-templates mode="footer" />
			</footer>			
		</article>
	
	</xsl:template>
		
	<xsl:template match="paragraph" mode="section">
		<!-- article holds the decision expression resource -->
		
		<!-- id for the paragraph-->
		<xsl:variable name="id">
			<xsl:number></xsl:number>
		</xsl:variable>
		
		<span rel="sdo:hasParagraph">
		<paragraph typeof="sdo:Paragraph" property="dcterms:title">
			    <xsl:attribute name="about" select="concat($decisionExpressionParagraph-prefix, $id)" />
			    
				<xsl:apply-templates mode="section" />
			</paragraph>		
		</span>
	</xsl:template>
		
	<xsl:template match="institution | act | judgment" mode="section">
		<span property="sdo:hasTextChunk" typeof="sdo:TextChunk">
			<xsl:attribute name="resource" select="concat($decisionExpressionURI, '/textchunk/', ./@id)" />
			<xsl:apply-templates  mode="section" />
		</span>
	</xsl:template>
	
	<xsl:template match="institution" mode="footer">
		<div>
			<xsl:attribute name="about" select="concat($decisionExpressionURI, '/textchunk/', ./@id)" />
			<div property="sdo:hasAnnotation" typeof="sao:Annotation">
				<xsl:attribute name="resource" select="concat($decisionExpressionURI, '/textchunk/', ./@id, '/annotation/1')" />
				
				<xsl:if test="matches(./@rdf:about, $vocab)">
					<div property="sao:hasTopic" typeof="lex:Court">
						<xsl:attribute name="resource" select="substring-after(./@rdf:about, $vocab)" />
						<span property="dcterms:title">
							<xsl:value-of select="./@name" />
						</span>
					</div>
				</xsl:if>
			</div>
		</div>
		<xsl:apply-templates mode="footer" />
	</xsl:template>
	
	<xsl:template match="act" mode="footer">
		<div>
			<xsl:attribute name="about" select="concat($decisionExpressionURI, '/textchunk/', ./@id)" />
			<xsl:variable name="id" select="./@id" />
			<xsl:for-each select="tokenize(./@rdf:about, '\s+')">
				<xsl:variable name="thingURI" select="substring-after(., $vocab)" />
				<xsl:variable name="thingIdentity" select="substring-after($thingURI, 'legislation/cz/act/')" />
				
				<xsl:variable name="year" select="substring-before($thingIdentity, '/')" />
				
				<xsl:variable name="after-year" select="substring-after($thingIdentity, '/')" />

				<xsl:choose>
					<xsl:when test="not(contains($after-year, '/'))">
						<xsl:variable name="number" select="$after-year" />
						<xsl:variable name="title">
							<xsl:value-of select="$number" />
							<xsl:text>/</xsl:text>
							<xsl:value-of select="$year" />
							<xsl:text> Sb.</xsl:text>
						</xsl:variable>
						<div property="sdo:hasAnnotation" typeof="sao:Annotation">
							<xsl:attribute name="resource" select="concat($decisionExpressionURI, '/textchunk/', $id, '/annotation/1')" />
							
							<xsl:if test="string-length($thingURI) &gt; 0">
								    <xsl:choose>
								    	<xsl:when test="contains($thingURI, 'section')">
								    		<!-- is is part of the work-->
									    		<div property="sao:hasTopic" typeof="frbr:Work">
									    			<xsl:attribute name="resource" select="$thingURI" />
									    			<span property="dcterms:title">
									    				<xsl:value-of select="$title" />
									    			</span>
									    		</div>
								    	</xsl:when>
								    	<xsl:otherwise>
								    		<div property="sao:hasTopic" typeof="lex:Act">
								    			<xsl:attribute name="resource" select="$thingURI" />
								    			<span property="dcterms:title">
								    				<xsl:value-of select="$title" />
								    			</span>
								    		</div>
								    	</xsl:otherwise>
								    </xsl:choose>
								
							</xsl:if>
						</div>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="number" select="substring-before($after-year, '/')" />
						<xsl:variable name="after-number" select="substring-after($after-year, '/section/')" />
						<xsl:variable name="sections" select="tokenize($after-number, '/')" />				
						<xsl:variable name="namePrefix">					
							<xsl:if test="$sections[1]">
								<xsl:text>§</xsl:text>
								<xsl:value-of select="$sections[1]" />
								<xsl:if test="$sections[2]">
									<xsl:text> odst. </xsl:text>
									<xsl:value-of select="$sections[2]" />
									<xsl:if test="$sections[3]">
										<xsl:text> písm. </xsl:text>
										<xsl:value-of select="$sections[3]" />
										<xsl:text>)</xsl:text>
									</xsl:if>
								</xsl:if>
							</xsl:if>	
						</xsl:variable>
						<xsl:variable name="title">
							<xsl:if test="string-length($namePrefix) &gt; 0">
								<xsl:value-of select="$namePrefix" />
								<xsl:text>, </xsl:text>
							</xsl:if>
							<xsl:value-of select="$number" />
							<xsl:text>/</xsl:text>
							<xsl:value-of select="$year" />
							<xsl:text> Sb.</xsl:text>
						</xsl:variable>
						
						<div property="sdo:hasAnnotation" typeof="sao:Annotation">
							<xsl:attribute name="resource" select="concat($decisionExpressionURI, '/textchunk/', $id, '/annotation/1')" />
							<xsl:if test="string-length($thingURI) &gt; 0">
								
								<xsl:choose>
									<xsl:when test="contains($thingURI, 'section')">
										<!-- is is part of the work-->
										<div property="sao:hasTopic" typeof="frbr:Work">
											<xsl:attribute name="resource" select="$thingURI" />
											<span property="dcterms:title">
												<xsl:value-of select="$title" />
											</span>
										</div>
									</xsl:when>
									<xsl:otherwise>
										<div property="sao:hasTopic" typeof="lex:Act">
											<xsl:attribute name="resource" select="$thingURI" />
											<span property="dcterms:title">
												<xsl:value-of select="$title" />
											</span>
										</div>
									</xsl:otherwise>
								</xsl:choose>
								
								
								<!--
								<div property="sao:hasTopic" typeof="lex:Act">
									<xsl:attribute name="resource" select="$thingURI" />
									<span property="dcterms:title">
										<xsl:value-of select="$title" />
									</span>
								</div>
								-->
							</xsl:if>
						</div>
						
					</xsl:otherwise>
				</xsl:choose>
				
			</xsl:for-each>
		</div>
		<xsl:apply-templates mode="footer" />
	</xsl:template>
	
	<xsl:template match="judgment" mode="footer">
		<div>
			<xsl:attribute name="about" select="concat($decisionExpressionURI, '/textchunk/', ./@id)" />
			<xsl:variable name="id" select="./@id" />
			<xsl:for-each select="tokenize(./@rdf:about, '\s+')">
				<xsl:variable name="thingURI" select="substring-after(., $vocab)" />
				<xsl:variable name="thingIdentity" select="substring-after($thingURI, 'legislation/cz/decision/')" />
				
				<xsl:variable name="year" select="substring-before($thingIdentity, '/')" />
				
				<xsl:variable name="after-year" select="substring-after($thingIdentity, '/')" />
				
				
				
						<xsl:variable name="number" select="$after-year" />
						<xsl:variable name="title">
							<xsl:value-of select="replace($number,'-', ' ')" />
						</xsl:variable>
						<div property="sdo:hasAnnotation" typeof="sao:Annotation">
							<xsl:attribute name="resource" select="concat($decisionExpressionURI, '/textchunk/', $id, '/annotation/1')" />
							
							<xsl:if test="string-length($thingURI) &gt; 0">
								<div property="sao:hasTopic" typeof="lex:Decision">
									<xsl:attribute name="resource" select="$thingURI" />
									<span property="dcterms:title">
										<xsl:value-of select="$title" />
									</span>
								</div>
							</xsl:if>
						</div>
				
			</xsl:for-each>
		</div>
		<xsl:apply-templates mode="footer" />
	</xsl:template>
	
	<xsl:template match="*" mode="footer">
		<xsl:apply-templates mode="footer" />
	</xsl:template>	
	
	<xsl:template match="meta" mode="section"> <!-- no recursive processing further, so that content of elem meta is not processed-->
		
	</xsl:template>	
	
	<xsl:template match="text()" mode="footer" />

	<xsl:template match="text()" mode="section"><xsl:value-of select="." /></xsl:template>
	
	<xsl:template match="text()" />
	
</xsl:stylesheet>
