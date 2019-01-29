#!/bin/bash

intput_dir=data/cop

for filename in $intput_dir/*.json; do
	echo $filename
	basename="$(basename "${filename%.*}").json"
	cp $intput_dir/$basename $basename
	java -cp XCSP3-Java-Tools/target/xcsp3-compiler-1.0.1-SNAPSHOT.jar org.xcsp.modeler.Compiler org.xcsp.modeler.problems.FrequencyAllocationXCSP3 -data=$basename -model=m1
	java -cp XCSP3-Java-Tools/target/xcsp3-compiler-1.0.1-SNAPSHOT.jar org.xcsp.modeler.Compiler org.xcsp.modeler.problems.FrequencyAllocationXCSP3 -data=$basename -model=m2
	java -cp XCSP3-Java-Tools/target/xcsp3-compiler-1.0.1-SNAPSHOT.jar org.xcsp.modeler.Compiler org.xcsp.modeler.problems.FrequencyAllocationXCSP3 -data=$basename -model=m3
	rm $basename
done
