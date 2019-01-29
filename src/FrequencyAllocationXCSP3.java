/*************************************************************************************
 * MIT License                                                                       *
 *                                                                                   *
 * Copyright (C) 2018 Charly Lamothe, Guillaume Ollier                               *
 *                                                                                   *
 * This file is part of FrequencyAllocationProblem.                                  *
 *                                                                                   *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy    *
 *   of this software and associated documentation files (the "Software"), to deal   *
 *   in the Software without restriction, including without limitation the rights    *
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell       *
 *   copies of the Software, and to permit persons to whom the Software is           *
 *   furnished to do so, subject to the following conditions:                        *
 *                                                                                   *
 *   The above copyright notice and this permission notice shall be included in all  *
 *   copies or substantial portions of the Software.                                 *
 *                                                                                   *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR      *
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,        *
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE     *
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER          *
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,   *
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE   *
 *   SOFTWARE.                                                                       *
 *************************************************************************************/

package org.xcsp.modeler.problems;

import org.xcsp.common.IVar.Var;
import org.xcsp.modeler.api.ProblemAPI;

import java.util.Arrays;
import java.util.stream.Stream;

class FrequencyAllocationXCSP3 implements ProblemAPI {
	Station stations[];
	int regions[];
	Interference interferences[];
	Connection connection[];

	public class Station {
		public int num, region, delta, transmitter[], receiver[];

		public Station(int num, int region, int delta, int transmitter[], int receiver[]) {
			this.num = num;
			this.region = region;
			this.delta = delta;
			this.transmitter = transmitter;
			this.receiver = receiver;
		}
	}

	public class Interference {
		public int x, y, Delta;

		public Interference(int x, int y, int Delta) {
			this.x = x;
			this.y = y;
			this.Delta = Delta;
		}
	}

	public class Connection {
		public int x, y;

		public Connection(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	@Override
	public void model() {
		Var[] transmitters_var = array("T", size(stations.length), i -> dom(stations[i].transmitter), "T[i] is the model used for the ith station transmitter");
		Var[] receivers_var = array("R", size(stations.length), i -> dom(stations[i].receiver), "R[i] is the model used for the ith station receiver");

		/**
		 * Constraint 1:
		 * For material reasons, the gap between the two frequencies of
		 * the station i must be equal to delta i.
		 */
		forall(range(stations.length), i -> {
			/* equal() already use intension() */
			equal(stations[i].delta, dist(transmitters_var[i], receivers_var[i]));
		});

		/**
		 * Constraint 2:
		 * Two different stations have a gap
		 * greater or equal.
		 * If two stations are close to each other,
		 * the used frequencies by these two stations
		 * must be sufficiently spaced in order to avoid
		 * interferences.
		 * We set $\delta_{ij}$ the minimum gap to respect
		 * between the stations i and j.
		 */
		forall(range(interferences.length), i -> {
			/* Add an intension constraint */
			intension(and(
					ge(dist(transmitters_var[interferences[i].x], receivers_var[interferences[i].y]), interferences[i].Delta),
					ge(dist(transmitters_var[interferences[i].y], receivers_var[interferences[i].x]), interferences[i].Delta),
					ge(dist(transmitters_var[interferences[i].y], transmitters_var[interferences[i].x]), interferences[i].Delta),
					ge(dist(receivers_var[interferences[i].y], receivers_var[interferences[i].x]), interferences[i].Delta)
			));
		});

		/**
		 * Constraint 3:
		 * For each region, we want to limit
		 * the number of different frequencies used.
		 * We set $n_i$ the maximum number of different
		 * frequencies used for the region i.
		 */

		/* 2 because we have maximum two time the number of stations (if all the stations are in the same region) */
		Var [] tmp = new Var[2 * stations.length];
		for (int i = 0; i < regions.length; i++) {
			int maxFrequences = regions[i];
			int sizeTmp = 0;

			/* We check in each station if the region is here */
			for (int j = 0; j < stations.length; j++) {
				if (stations[j].region == i) {
					/* If it's the case, we add the variables of the transmitter and receiver stations of the station */
					tmp[sizeTmp] = transmitters_var[j];
					tmp[sizeTmp + 1] = receivers_var[j];
					sizeTmp += 2;
				}
			}

			/* We a create a final array from the array of the frequencies T/R of the used stations */
			Var [] F = new Var[sizeTmp]; /* the set of the present variables in the region */
			System.arraycopy(tmp, 0, F, 0, sizeTmp);

			/* The number of maximum frequencies is <= the maximum allowed frequencies */
			nValues(F, LE, maxFrequences);
		}

		/**
		 * Constraint 4:
		 * In order to communicate with the other stations,
		 * each station must have two frequencies.
		 *
		 * We make the connection between the stations thanks
		 * to the connections array.
		 */
		for (Connection connection : connection) {
			/* We create an intension constraint for each specified connection in the file */
			intension(
				and(
					/* The transmitting frequency of x must be equal to the receiving frequency of y */
					eq(transmitters_var[connection.x], receivers_var[connection.y]),
					/* The transmitting frequency of y must be equal to the receiving frequency of x */
					eq(transmitters_var[connection.y], receivers_var[connection.x])
				)
			);
		}

		/* All the frequencies, used for the models 1 and 3 */
		Var [] frequencies = Stream.concat(Arrays.stream(transmitters_var), Arrays.stream(receivers_var))
				.toArray(Var[]::new);

		/**
		 * Model 1:
		 * Minimize the number of used frequencies (for example,
		 * in order to keep the available frequencies for future
		 * needs).
		 */
		if (modelVariant("m1")) {
			/**
			 * We define a variable nFrequencies with n the number of stations
			 * so that the maximum number of different frequencies we can obtain.
			 */
			Var nFrequencies = var("nFrequencies", dom(range(stations.length)));
			/**
			 * Our number of different frequencies must be equal to our
			 * variable. Thus, it reduce our domain of frequencies to the number
			 * of different frequencies.
			 */
			nValues(frequencies, EQ, nFrequencies);
			/* We minimize this number */
			minimize(MINIMUM,nFrequencies);
			
			/**
			 * Problem of this strategy :
			 * If it instantiate this variable near the end,
			 * it will iterate many large the sub-trees for nothing.
			 */
		}

		/**
		 * Model 2:
		 * Use the lowest frequencies, if possible.
		 *
		 * The maximum variables (MAXIMUM) are the lowest possibles (minimize).
		 */
		if (modelVariant("m2")) {
			minimize(SUM, new Var[][]{transmitters_var, receivers_var});
		}

		/**
		 * Model 3:
		 * Minimize the width of the used frequency bands (that is, the gap between the lowest
		 * and the highest frequency).
		 */
		if (modelVariant("m3")) {
			Var spanFrequencies = var("span", dom(range(100000)));

			equal(spanFrequencies, sub(max(frequencies), min(frequencies)));

			minimize(MINIMUM, spanFrequencies);
		}
	}
}
