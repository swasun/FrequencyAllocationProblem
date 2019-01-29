#!/bin/bash

for filename in data/wcsp/*.json; do
	basename="$(basename "${filename%.*}")"
	echo $basename > wcsp_queue.txt
	cp $filename $basename.json
	java -cp XCSP3-Java-Tools/target/xcsp3-compiler-1.0.1-SNAPSHOT.jar org.xcsp.modeler.Compiler org.xcsp.modeler.problems.FrequencyAllocationWCSP -data=$basename.json
	rm -f $basename.json
done
