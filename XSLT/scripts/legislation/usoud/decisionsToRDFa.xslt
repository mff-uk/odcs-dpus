<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://xrg.cz/link/sourceOfLaw/1" xpath-default-namespace="http://xrg.cz/link/sourceOfLaw/1">
	
	<xsl:output method="xml" encoding="UTF-8" indent="no"/>
	
	
	<!--  soud prvniho stupne, prvni institution, which contains "okresni" or "obvodni" in URI -->
	<xsl:variable name="soudPrvniStupen"><xsl:value-of select="//paragraph[institution[ contains(@rdf:about,'obvodni') or contains(@rdf:about,'okresni')  ]][1]/institution[contains(@rdf:about,'obvodni') or contains(@rdf:about,'okresni') ][1]/@rdf:about"></xsl:value-of></xsl:variable>
	<xsl:variable name="soudPrvniStupenName"><xsl:value-of select="//paragraph[institution[ contains(@rdf:about,'obvodni') or contains(@rdf:about,'okresni')  ]][1]/institution[contains(@rdf:about,'obvodni') or contains(@rdf:about,'okresni') ][1]/@name"></xsl:value-of></xsl:variable>
	
	
	<!--  soud druheho stupne, prvni institution, which contains "vrchni" or "krajsky" or "mestsky" in URI -->
	<xsl:variable name="soudDruhyStupen"><xsl:value-of select="//paragraph[institution[ contains(@rdf:about,'vrchni') or contains(@rdf:about,'krajsky') or contains(@rdf:about,'mestsky') ]][1]/institution[ contains(@rdf:about,'vrchni') or contains(@rdf:about,'krajsky') or contains(@rdf:about,'mestsky') ][1]/@rdf:about"></xsl:value-of></xsl:variable>
	<xsl:variable name="soudDruhyStupenName"><xsl:value-of select="//paragraph[institution[ contains(@rdf:about,'vrchni') or contains(@rdf:about,'krajsky') or contains(@rdf:about,'mestsky') ]][1]/institution[ contains(@rdf:about,'vrchni') or contains(@rdf:about,'krajsky') or contains(@rdf:about,'mestsky') ][1]/@name"></xsl:value-of></xsl:variable>
	
	
	
	
	<!-- Decision metadata (taken from meta elem section) -->
	
	
	<!-- Spisova znacka (with spaces and '/'-->
	<xsl:variable name="spisovaZnackaText"><xsl:value-of select='document/metadata/table//td[preceding-sibling::node()[contains(text(),"Spisová značka")]]/normalize-space(string())'/></xsl:variable>
	
	<!-- Spisova znacka for use in URI -->
	<xsl:variable name="spisovaZnackaBaseWithDashTemp"><xsl:value-of select='replace(replace(replace($spisovaZnackaText,"\.","-")," ","-"),"Ú","U")'/></xsl:variable>
	<xsl:variable name="spisovaZnackaBaseWithDash"><xsl:value-of select='replace($spisovaZnackaBaseWithDashTemp,"/","-")'/></xsl:variable>	
	<xsl:variable name="spisovaZnackainURI"><xsl:value-of select='$spisovaZnackaBaseWithDash'/></xsl:variable>
	
	
	<!-- Year for use in URI -->
	<!-- year must be taken from datum podani  
	<xsl:variable name="yearSpisZnacka"><xsl:value-of select='substring($spisovaZnackaInURI,string-length($spisovaZnackaInURI)-3)'/></xsl:variable> -->
	<xsl:variable name="decDatePodani"><xsl:value-of select="/document/metadata/table//td[preceding-sibling::node()[contains(text(),'Datum podání')]]/normalize-space(string())"/></xsl:variable>
	<xsl:variable name="yearSpisZnacka"><xsl:value-of select='normalize-space(substring-after(substring-after($decDatePodani,"."),"."))'/></xsl:variable>
	
	
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
		<xsl:text>/expression/</xsl:text>
		<xsl:value-of select='substring-after($decisionURI,"legislation/")'></xsl:value-of>
		<xsl:text>/cs</xsl:text>
	</xsl:variable>
	
	<xsl:variable name="decisionExpressionSection">
		<xsl:value-of select="$decisionExpressionURI" />
		<xsl:text>/section/1</xsl:text>
	</xsl:variable>
	
	<xsl:variable name="decisionExpressionParagraph-prefix">
		<xsl:value-of select="$decisionExpressionSection" />
		<xsl:text>/paragraph/</xsl:text>
	</xsl:variable>
			
	<xsl:variable name="vocab">
		<xsl:text>http://linked.opendata.cz/resource/</xsl:text>
	</xsl:variable>		
	
	<xsl:template match="content">
		
		<!-- Check that spisova znacka has reasonable format. If not, skip the generation of the xslt-->
		<xsl:if test="matches($yearSpisZnacka,'^[0-9]{4}$') and matches($spisovaZnackainURI,'-US-[0-9]+-[0-9]+(-[0-9]+){0,1}$')"> <!-- test that spisova znacka has reasonable format-->
			
		
			<!-- article holds the decision expression resource -->
			<article typeof="frbr:Expression sdo:Publication" prefix="frbr: http://purl.org/vocab/frbr/core# dcterms: http://purl.org/dc/terms/ sdo: http://salt.semanticauthoring.org/ontologies/sdo# sao: http://salt.semanticauthoring.org/ontologies/sao# lex: http://purl.org/lex#" xml:base="http://linked.opendata.cz/resource/">
				<!--<xsl:attribute name="vocab" select="$vocab" />-->
				<xsl:attribute name="resource" select="$decisionExpressionURI" />
				
							
					<!-- start a new section, decision has only one section-->
					<span rel="sdo:hasSection">
						<section typeof="sdo:Section" >
							<xsl:attribute name="about" select="$decisionExpressionSection" />
							<span xmlns:sdo="http://salt.semanticauthoring.org/ontologies/sdo#" property="sdo:hasOrderNumber"><xsl:attribute  name="content" select="1"></xsl:attribute></span>
							<xsl:apply-templates mode="section" />
						</section>
					</span>
	
				
				<footer>
					<xsl:apply-templates mode="footer" />
				</footer>			
			</article>
		</xsl:if>
	
	</xsl:template>
		
	<xsl:template match="paragraph" mode="section">
		<!-- article holds the decision expression resource -->
		
		<!-- id for the paragraph-->
		<xsl:variable name="id">
			<xsl:number></xsl:number>
		</xsl:variable>
		
		<span rel="sdo:hasParagraph">
		<paragraph typeof="sdo:Paragraph" property="dcterms:description">
			<xsl:attribute name="about" select="concat($decisionExpressionParagraph-prefix, $id)" /><span xmlns:sdo="http://salt.semanticauthoring.org/ontologies/sdo#" property="sdo:hasOrderNumber"><xsl:attribute  name="content" select="$id"></xsl:attribute></span><xsl:apply-templates mode="section" /></paragraph>		
		</span>
	</xsl:template>
		
	<xsl:template match="institution | act | judgment" mode="section">
		<xsl:variable name="textChunkValue"><xsl:value-of select="."></xsl:value-of></xsl:variable>
		<span property="sdo:hasTextChunk" typeof="sdo:TextChunk">
			<xsl:attribute name="resource" select="concat($decisionExpressionURI, '/textchunk/', ./@id)" />
 			<span property="dcterms:description"><xsl:apply-templates  mode="section"/></span>	
		</span>	
	</xsl:template>
	
	<xsl:template match="institution" mode="footer">
		<div>
			<xsl:attribute name="about" select="concat($decisionExpressionURI, '/textchunk/', ./@id)" />
			<div property="sdo:hasAnnotation" typeof="sao:Annotation">
				<xsl:attribute name="resource" select="concat($decisionExpressionURI, '/textchunk/', ./@id, '/annotation/1')" />
				
				<xsl:if test="matches(./@rdf:about, $vocab)">
					
						
						<!-- adjust the court!! -->
						
						<xsl:variable name="court"><xsl:value-of select="substring-after(./@rdf:about, $vocab)"></xsl:value-of></xsl:variable>
							<!-- Check that there is certain court -->
							<xsl:if test="string-length($court) > 2"> <!-- not just <>-->
								
								<xsl:choose>
									<xsl:when test="contains($court,'soud-dovolaci') or contains($court,'dovolaci-soud')">
										<xsl:variable name="courtFinal">court/cz/nejvyssi-soud</xsl:variable>
										
										<xsl:if test="string-length($courtFinal) > 2">
										<div property="sao:hasTopic" typeof="lex:Court">
											<xsl:attribute name="resource" select="$courtFinal" />
											<span property="dcterms:title">Nejvyšší soud</span>
									    </div>
										</xsl:if>
										
									</xsl:when>
									<xsl:when test="contains($court,'soud-prvni-stupne-pro') or contains($court,'soud-prvni-stupne')">
										<xsl:variable name="courtFinal"><xsl:value-of select="substring-after($soudPrvniStupen, $vocab)"></xsl:value-of></xsl:variable>
										
										<xsl:if test="string-length($courtFinal) > 2">
											<div property="sao:hasTopic" typeof="lex:Court">
											<xsl:attribute name="resource" select="$courtFinal" />
													<span property="dcterms:title">
													<xsl:value-of select="$soudPrvniStupenName" />
													</span>
											</div>	
										</xsl:if>
										
									</xsl:when>
									<xsl:when test="contains($court,'odvolaci-soud') or contains($court,'soud-druhy-stupne') or contains($court,'soud-druhy-stupne-pro')">
										<xsl:variable name="courtFinal"><xsl:value-of select="substring-after($soudDruhyStupen, $vocab)"></xsl:value-of></xsl:variable>
										
										<xsl:if test="string-length($courtFinal) > 2">
											<div property="sao:hasTopic" typeof="lex:Court">
												<xsl:attribute name="resource" select="$courtFinal" />
												<span property="dcterms:title">
													<xsl:value-of select="$soudDruhyStupenName" />
												</span>
											</div>
										</xsl:if>
										
										
									</xsl:when>
									<xsl:otherwise>
										<xsl:variable name="courtFinal"><xsl:value-of select="$court"/></xsl:variable>
										
										<xsl:if test="string-length($courtFinal) > 2">
											<div property="sao:hasTopic" typeof="lex:Court">
												<xsl:attribute name="resource" select="$courtFinal" />
												<span property="dcterms:title">
													<xsl:value-of select="./@name" />
												</span>
											</div>
										</xsl:if>
										
										
										
									</xsl:otherwise>
								</xsl:choose>
		
							</xsl:if>
							
							
							
						
						
						
						<!-- ORIG, but now it is used in the switch above <xsl:attribute name="resource" select="substring-after(./@rdf:about, $vocab)" /> 
						
						<span property="dcterms:title">
							<xsl:value-of select="./@name" />
						</span>
						-->
						
					
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
							<!--<xsl:value-of select="replace($number,'-', ' ')" />-->
							<xsl:value-of select="$number" />
						</xsl:variable>
						<div property="sdo:hasAnnotation" typeof="sao:Annotation">
							<xsl:attribute name="resource" select="concat($decisionExpressionURI, '/textchunk/', $id, '/annotation/1')" />
							
							<xsl:if test="string-length($thingURI) &gt; 0">
								
									
									
										<!-- adjust title of the decision -->
											<!-- Metadata for decision/file-->
										<xsl:variable name="decisionIdentifierInURI"><xsl:value-of select='substring-after(substring-after($thingURI,"decision/"),"/")'/></xsl:variable>
											<xsl:variable name="senat"><xsl:value-of select='substring-before($decisionIdentifierInURI,"-")'/></xsl:variable>
											<xsl:variable name="fileKind"><xsl:value-of select='substring-before(substring-after($decisionIdentifierInURI,"-"),"-")'/></xsl:variable>
											<xsl:variable name="fileNumber"><xsl:value-of select='substring-before(substring-after(substring-after($decisionIdentifierInURI,"-"),"-"),"-")'/></xsl:variable>
										
											<!-- is the decision identifier cislo jednaci? if yes, take the last number (decisionNumber) in: 56-Co-97-2011-29 -->
											<xsl:variable name="decisionNumber"><xsl:value-of select='substring-after(substring-after(substring-after(substring-after($decisionIdentifierInURI,"-"),"-"),"-"),"-")'/></xsl:variable>
											
											
											<xsl:choose>
												<xsl:when test="string-length($decisionNumber) > 0"> <!-- cislo jednaci is available -->
													<xsl:variable name="year"><xsl:value-of select='substring-before(substring-after(substring-after(substring-after($decisionIdentifierInURI,"-"),"-"),"-"),"-")'/></xsl:variable>
													<xsl:variable name="decisionIdentifierText"><xsl:value-of select='concat($senat," ",$fileKind," ",$fileNumber,"/",$year, "-", $decisionNumber)'/></xsl:variable>
													
													<xsl:choose>
														<!-- NSOUD -->
														<xsl:when test="matches($year,'^[0-9]{4}$') and matches($decisionIdentifierText,'^[0-9]+ [a-zA-Z]+ [0-9]+/[0-9]{4}(-[0-9]+){0,1}$')"> 
															<!-- value on the output -->
															<div property="sao:hasTopic" typeof="lex:Decision">
																<xsl:attribute name="resource" select="$thingURI" />
																<span property="dcterms:title">
																	<xsl:value-of select="$decisionIdentifierText" />
																</span>
															</div>
														
														</xsl:when>
														<!-- USOUD -->
														<xsl:when test="matches($year,'^[0-9]{2}$') and matches($decisionIdentifierText,'US [0-9]+/[0-9]+$')"> 
															<!-- value on the output -->
															<div property="sao:hasTopic" typeof="lex:Decision">
																<xsl:attribute name="resource" select="$thingURI" />
																<span property="dcterms:title">
																	<xsl:value-of select="$decisionIdentifierText" />
																</span>
															</div>
															
														</xsl:when>
														<xsl:otherwise>
															<!-- identifier not valid, do not output title taken from the identifier, do not output typeof-->
															<div property="sao:hasTopic">
																<xsl:attribute name="resource" select="$thingURI" />
																<!--<span property="dcterms:title">
																	<xsl:value-of select="$title" />
																</span>-->
															</div>
														</xsl:otherwise>
													</xsl:choose>
												
													
													
												</xsl:when>
												<xsl:otherwise>
													<xsl:variable name="year"><xsl:value-of select='substring-after(substring-after(substring-after($decisionIdentifierInURI,"-"),"-"),"-")'/></xsl:variable>
													<xsl:variable name="decisionIdentifierText"><xsl:value-of select='concat($senat," ",$fileKind," ",$fileNumber,"/",$year)'/></xsl:variable>
													
													
													<xsl:choose>
														<!--NSOUD -->
														<xsl:when test="matches($year,'^[0-9]{4}$') and matches($decisionIdentifierText,'^[0-9]+ [a-zA-Z]+ [0-9]+/[0-9]{4}(-[0-9]+){0,1}$')"> 
															<!-- value on the output -->
															<div property="sao:hasTopic" typeof="lex:Decision">
																<xsl:attribute name="resource" select="$thingURI" />
																<span property="dcterms:title">
																	<xsl:value-of select="$decisionIdentifierText" />
																</span>
															</div>
															
														</xsl:when>
														<!-- USOUD -->
														<xsl:when test="matches($year,'^[0-9]{2}$') and matches($decisionIdentifierText,'US [0-9]+/[0-9]+$')"> 
															<!-- value on the output -->
															<div property="sao:hasTopic" typeof="lex:Decision">
																<xsl:attribute name="resource" select="$thingURI" />
																<span property="dcterms:title">
																	<xsl:value-of select="$decisionIdentifierText" />
																</span>
															</div>
														</xsl:when>	
														<xsl:otherwise>
															<!-- identifier not valid, do not output title taken from the identifier, do not output typeof-->
															<div property="sao:hasTopic">
																<xsl:attribute name="resource" select="$thingURI" />
																<!--<span property="dcterms:title">
																	<xsl:value-of select="$title" />
																</span>-->
															</div>
														</xsl:otherwise>
													</xsl:choose>
													
												
												</xsl:otherwise>
											</xsl:choose>
											
										
											
											
										<!-- end of title adjustment -->
										<!-- Original value <xsl:value-of select="$title" /> -->
								
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
