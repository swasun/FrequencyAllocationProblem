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

input_dir=instances/wcsp
output_dir=results/toulbar2

mkdir -p $output_dir

for filename in $input_dir/*.wcsp; do
	basename="$(basename "${filename%.*}")"
	toulbar2 $input_dir/$basename.wcsp -w=$output_dir/$basename.sol > $output_dir/$basename.log
done
