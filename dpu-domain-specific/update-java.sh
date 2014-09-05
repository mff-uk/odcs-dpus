startDir=`pwd`
cd $1

find . -name *.java -print0 | xargs -I{} -0 $startDir/update-java-file.sh {}

cd $startDir

