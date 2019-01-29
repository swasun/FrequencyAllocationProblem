#!/bin/bash

mkdir -p results/choco-2017

for filename in instances/xcsp3/*.xml; do
	basename="$(basename "${filename%.*}").txt"
	{ time -p java -server -Xmx500m -cp solvers/choco-2017/choco-parsers.jar org.chocosolver.parser.xcsp.ChocoXCSP -tl=600000 -p=4 $filename> results/choco-2017/${basename}_choco; } 2>> results/choco-2017/${basename}_choco
done
