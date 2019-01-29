#!/bin/bash

input_dir=instances/wcsp
output_dir=results/toulbar2

mkdir -p $output_dir

for filename in $input_dir/*.wcsp; do
	basename="$(basename "${filename%.*}")"
	toulbar2 $input_dir/$basename.wcsp -w=$output_dir/$basename.sol > $output_dir/$basename.log
done
