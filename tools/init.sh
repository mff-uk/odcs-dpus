#!/bin/bash

# Build base.
echo "building base projects .."
cd ../base 
 cd uk-pom-dpu
  mvn clean install
  cd ..

# Build libs.
echo "building libs .."
cd ../libs
 cd service-external
  mvn clean install
  cd ..

# Go back to tools.
cd ../tools
echo "done"
