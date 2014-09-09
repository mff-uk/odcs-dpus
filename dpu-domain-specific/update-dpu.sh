if [ $# -eq 0 ]
then
	echo "Usage:"
	echo "update-dpu.sh dpuRoot"
	echo "	dpuRoot - path to the DPUs root dir ie. pom.xml"
    exit
fi

./update-java.sh $1
./update-pom.sh $1