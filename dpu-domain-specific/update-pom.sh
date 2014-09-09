startDir=`pwd`
cd $1

echo -n "Updating pom.xml ... "
cat pom.xml | sed "s/\t/    /g" |  \
	sed \
		-e "s/$        <version>.*/        <version>1.1.1<\/version>/" \
		-e "s/$    <version>.*/    <version>1.1.0<\/version>/" \
		-e "s/<\/project>//" > pom.tmp
# append dependencies
echo -e \
'    <dependencies>\n'\
'       <dependency>\n'\
'            <groupId>cz.cuni.mff.xrg.uv</groupId>\n'\
'            <artifactId>boost-dpu</artifactId>\n'\
'            <version>[${uk.version.min},${uk.version.max})</version>\n'\
'            <scope>compile</scope>\n'\
'        </dependency>\n'\
'        <!-- compile library -->\n'\
'        <dependency>\n'\
'            <groupId>cz.cuni.mff.xrg.uv</groupId>\n'\
'            <artifactId>utils-dataunit-rdf</artifactId>\n'\
'            <version>[${uk.version.min},${uk.version.max})</version>\n'\
'            <type>jar</type>\n'\
'            <scope>compile</scope>\n'\
'        </dependency>\n'\
'        <dependency>\n'\
'            <groupId>cz.cuni.mff.xrg.uv</groupId>\n'\
'            <artifactId>utils-dataunit</artifactId>\n'\
'            <version>[${uk.version.min},${uk.version.max})</version>\n'\
'            <type>jar</type>\n'\
'            <scope>compile</scope>\n'\
'        </dependency>\n'\
'        <dependency>\n'\
'            <groupId>cz.cuni.mff.xrg.uv</groupId>\n'\
'            <artifactId>service-serialization-rdf</artifactId>\n'\
'            <version>[${uk.version.min},${uk.version.max})</version>\n'\
'            <type>jar</type>\n'\
'            <scope>compile</scope>\n'\
'        </dependency>\n'\
'        <dependency>\n'\
'            <groupId>cz.cuni.mff.xrg.uv</groupId>\n'\
'            <artifactId>service-serialization-xml</artifactId>\n'\
'            <version>[${uk.version.min},${uk.version.max})</version>\n'\
'            <scope>compile</scope>\n'\
'        </dependency>\n'\
'	</dependencies>\n'\
'</project>\n' >> pom.tmp
cat pom.tmp > pom.xml
rm pom.tmp		
echo "done"

cd $startDir
