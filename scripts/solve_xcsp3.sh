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

mkdir -p results/choco-2017

for filename in instances/xcsp3/*.xml; do
	basename="$(basename "${filename%.*}").txt"
	{ time -p java -server -Xmx500m -cp solvers/choco-2017/choco-parsers.jar org.chocosolver.parser.xcsp.ChocoXCSP -tl=600000 -p=4 $filename> results/choco-2017/${basename}_choco; } 2>> results/choco-2017/${basename}_choco
done
