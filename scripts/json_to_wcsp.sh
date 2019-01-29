 ###############################################################################
 # Copyright (C) 2018 Charly Lamothe, Guillaume Ollier                         #
 #                                                                             #
 # This file is part of FrequencyAllocationProblem.                            #
 #                                                                             #
 #   Licensed under the Apache License, Version 2.0 (the "License");           #
 #   you may not use this file except in compliance with the License.          #
 #   You may obtain a copy of the License at                                   #
 #                                                                             #
 #   http://www.apache.org/licenses/LICENSE-2.0                                #
 #                                                                             #
 #   Unless required by applicable law or agreed to in writing, software       #
 #   distributed under the License is distributed on an "AS IS" BASIS,         #
 #   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  #
 #   See the License for the specific language governing permissions and       #
 #   limitations under the License.                                            #
 ###############################################################################

#!/bin/bash

for filename in data/wcsp/*.json; do
	basename="$(basename "${filename%.*}")"
	echo $basename > wcsp_queue.txt
	cp $filename $basename.json
	java -cp XCSP3-Java-Tools/target/xcsp3-compiler-1.0.1-SNAPSHOT.jar org.xcsp.modeler.Compiler org.xcsp.modeler.problems.FrequencyAllocationWCSP -data=$basename.json
	rm -f $basename.json
done
