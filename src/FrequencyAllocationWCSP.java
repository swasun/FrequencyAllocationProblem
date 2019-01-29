/*******************************************************************************
 * Copyright (C) 2018 Charly Lamothe, Guillaume Ollier                         *
 *                                                                             *
 * This file is part of FrequencyAllocationProblem.                            *
 *                                                                             *
 *   Licensed under the Apache License, Version 2.0 (the "License");           *
 *   you may not use this file except in compliance with the License.          *
 *   You may obtain a copy of the License at                                   *
 *                                                                             *
 *   http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                             *
 *   Unless required by applicable law or agreed to in writing, software       *
 *   distributed under the License is distributed on an "AS IS" BASIS,         *
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 *   See the License for the specific language governing permissions and       *
 *   limitations under the License.                                            *
 *******************************************************************************/

package org.xcsp.modeler.problems;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.xcsp.modeler.api.ProblemAPI;

public class FrequencyAllocationWCSP implements ProblemAPI {

	Station[] stations;
	Interference[] interferences;
	Connection[] connection;
	int[] regions;

	public class Station {
		int num, region, delta;
		int[] transmitter;
		int[] receiver;

		public Station(int num, int region, int delta, int[] transmitter, int[] receiver) {
			this.num = num;
			this.region = region;
			this.delta = delta;
			this.transmitter = transmitter;
			this.receiver = receiver;
		}
	}

	public class Interference {
		int x, y, Delta;

		public Interference(int x, int y, int Delta) {
			this.x = x;
			this.y = y;
			this.Delta = Delta;
		}
	}

	public class Connection {
		int x, y;

		public Connection(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	/**
	 * Constraint 1:
	 * For material reasons, the gap between the two frequencies of
	 * the station i must be equal to delta i.
	 */
	public class SpacedStation {
		int num, region, tuplesNumber;
		int[] transmitter;
		int[] receiver;

		public SpacedStation(Station station) {
			this.num = station.num;
			this.region = station.region;
			this.tuplesNumber = 0;

			this.transmitter = new int[2 * station.transmitter.length];
			this.receiver = new int[2 * station.receiver.length];

			for (int i = 0; i < station.transmitter.length; i++) {
				for (int j = 0; j < station.receiver.length; j++) {
					/* The gap beteween the two frequencies of the station i must be equal to delta i. */
					if (Math.abs(station.transmitter[i] - station.receiver[j]) == station.delta) {
						this.transmitter[this.tuplesNumber] = station.transmitter[i];
						this.receiver[this.tuplesNumber] = station.receiver[j];
						this.tuplesNumber++;
					}
				}
			}
		}
	}
	
	/**
	 * Resolve the output file name, create the ouput file
	 * and initialize the writer (output stream).
	 * 
	 * @return writer the ouput stream used to write in the file.
	 * @throws IOException
	 */
	private BufferedWriter resolveWriter() throws IOException {
		/* Resolve the name personalized for the output file */
		String currentWorkName = this.resolveCurrentWorkName();
		String resultFileName = "FrequencyAllocationWCSP_" + currentWorkName + ".wcsp";
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(resultFileName));
		
		return writer;
	}
	
	/**
	 * Resolve the work name from the file 'wcsp_queue.txt' which contains
	 * the work name.
	 * 
	 * @return The work name which will be used in the file name.
	 */
	private String resolveCurrentWorkName() {
		String currentWorkName = "";
		final String WORKS_QUEUE_FILE_NAME = "wcsp_queue.txt";
		
		try {
			File file = new File(WORKS_QUEUE_FILE_NAME);
			if (!file.exists()) {
				return "unknown";
			}
			FileInputStream fis;
			byte[] data = new byte[(int) file.length()];
			fis = new FileInputStream(file);
			fis.read(data);
			fis.close();
			String content = new String(data, "UTF-8");
			return content.replace("\n", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return currentWorkName;
	}
	
	/**
	 * Compute the stations spaced apart (Constraint 1).
	 * 
	 * @return the array of spaced stations.
	 */
	private SpacedStation[] computeSpacedStations() {
		int stationsNumber = this.stations.length;
		SpacedStation[] spacedStations = new SpacedStation[stationsNumber];

		for (int i = 0; i < stationsNumber; i++) {
			spacedStations[i] = new SpacedStation(this.stations[i]);
		}
		
		return spacedStations;
	}

	/**
	 * Compute the problem header and wrote it in the output
	 * file.
	 * 
	 * The header is composed of:
	 * - the problem name
	 * - the number of variables (stations number)
	 * - the maximum size used by all the domains (2 x the size of an transmitter/receiver)
	 * - the number of cost functions (the interferences number + the connections number)
	 * - the global initial upper bound of the problem (the number of cost functions + 1)
	 * 
	 * @throws IOException
	 **/
	private void computeHeader(BufferedWriter writer) throws IOException {
		String problemName = new String("FrequencyAllocationProblem");
		int variablesNumber = stations.length;
		int maxDomainSize = 0;
		for (Station station : stations) {
			if (2 * station.transmitter.length > maxDomainSize) {
				maxDomainSize = 2 * station.transmitter.length;
			}
			if (2 * station.receiver.length > maxDomainSize) {
				maxDomainSize = 2 * station.receiver.length;
			}
		}

		int costFunctionNumber = interferences.length + connection.length;
		int UB = interferences.length + connection.length + 1;
		writer.write(String.format("%s %d %d %d %d\n", problemName, variablesNumber, maxDomainSize, costFunctionNumber, UB));
	}

	/**
	 * Write the size of each domain of the problem
	 * in the file.
	 * 
	 * The size has already been calculated in SpacedStation()
	 * and is record in the variable tuplesNumber.
	 * 
	 * @param spacedStations  les stations espacées par delta.
	 * @param writer          le flux de sortie.
	 * @throws IOException
	 */
	private void computeDomainSizes(SpacedStation[] spacedStations, BufferedWriter writer) throws IOException {
		for (SpacedStation spacedStation : spacedStations) {
			writer.write(String.format("%d ", spacedStation.tuplesNumber));
		}
		writer.newLine();
	}
	
	/**
     * Calcule la contrainte d'interférences (C2).	
	 * 
	 * C2 : "Deux stations différentes peuvent donc avoir le même écart comme des
	 * écarts différents (supérieur) Si deux stations sont proches l'une de l'autre,
	 * les fréquences utilisées par ces stations doivent être suffisamment espacées
	 * pour éviter les interférences. On notera $\delta_{ij}$ l'écart minimum à
	 * garantir entre les fréquences des stations i et j."
	 * 
	 * @param spacedStations
	 * @param writer
	 * @throws IOException
	 */
	private void computeInterferenceConstraint(SpacedStation[] spacedStations, BufferedWriter writer) throws IOException {
		for (Interference interference : this.interferences) {
			computeInterferenceConstraintIteration(interference, spacedStations, writer);
		}
	}
	
	/**
	 * 
	 * @param interference
	 * @param spacedStations
	 * @param writer
	 * @throws IOException
	 */
	private void computeInterferenceConstraintIteration(Interference interference, SpacedStation[] spacedStations, BufferedWriter writer) throws IOException {
		StringBuilder contentSb = new StringBuilder();

		int tuplesNumber = computeInterferenceConstraintContent(interference, spacedStations, contentSb);
		String header = computeInterferenceConstraintHeader(interference, tuplesNumber);

		writer.write(header);
		writer.write(contentSb.toString());
	}
	
	/**
	 * 
	 * @param interference
	 * @param tuplesNumber
	 * @return
	 */
	private String computeInterferenceConstraintHeader(Interference interference, int tuplesNumber) {
		return String.format("2 %d %d 1 %d\n", interference.x, interference.y, tuplesNumber);
	}

	/**
	 * 
	 * @param interference
	 * @param spacedStations
	 * @param sb
	 * @return
	 */
	private int computeInterferenceConstraintContent(Interference interference, SpacedStation[] spacedStations, StringBuilder sb) {
		SpacedStation sx = spacedStations[interference.x];
		SpacedStation sy = spacedStations[interference.y];
		int tuplesNumber = 0;

		for (int i = 0; i < sx.tuplesNumber; i++) {
			for (int j = 0; j < sy.tuplesNumber; j++) {
				if (Math.abs(sx.transmitter[i] - sy.transmitter[j]) < interference.Delta ||
					Math.abs(sx.transmitter[i] - sy.receiver[j]) < interference.Delta ||
					Math.abs(sx.receiver[i] - sy.transmitter[j]) < interference.Delta ||
					Math.abs(sx.receiver[i] - sy.receiver[j]) < interference.Delta) {
					continue;
				}

				sb.append(String.format("%d %d 0\n", i, j));
				tuplesNumber++;
			}
		}

		return tuplesNumber;
	}
	
	/**
	 * Constraint 4:
	 * In order to communicate with the other stations,
	 * each station must have two frequencies.
	 *
	 * We make the connection between the stations thanks
	 * to the connections array.	 
	 * 
	 * @param spacedStations
	 * @param writer
	 * @throws IOException
	 */
	private void computeConnectionConstraint(SpacedStation[] spacedStations, BufferedWriter writer) throws IOException {
		for (Connection connection : this.connection) {
			computeConnectionConstraintIteration(connection, spacedStations, writer);
		}
	}
	
	/**
	 * 
	 * @param connection
	 * @param spacedStations
	 * @param writer
	 * @throws IOException
	 */
	private void computeConnectionConstraintIteration(Connection connection, SpacedStation[] spacedStations, BufferedWriter writer) throws IOException {
		StringBuilder contentSb = new StringBuilder();

		int tuplesNumber = computeConnectionConstraintIterationContent(connection, spacedStations, contentSb);
		String header = computeConnectionConstraintIterationHeader(connection, tuplesNumber);

		writer.write(header);
		writer.write(contentSb.toString());
	}

	/**
	 * 
	 * @param connection
	 * @param spacedStations
	 * @param sb
	 * @return
	 */
	private int computeConnectionConstraintIterationContent(Connection connection, SpacedStation[] spacedStations, StringBuilder sb) {
		SpacedStation sx = spacedStations[connection.x];
		SpacedStation sy = spacedStations[connection.y];

		int tuplesNumber = 0;

		for (int i = 0; i < sx.tuplesNumber; i++) {
			for (int j = 0; j < sy.tuplesNumber; j++) {
				if ((sx.transmitter[i] != sy.receiver[j]) ||
					(sx.receiver[i] != sy.transmitter[j])) {
					continue;
				}

				sb.append(String.format("%d %d 0\n", i, j));
				tuplesNumber++;
			}
		}

		return tuplesNumber;
	}

	/**
	 * 
	 * @param connection
	 * @param tuplesNumber
	 * @return
	 */
	private String computeConnectionConstraintIterationHeader(Connection connection, int tuplesNumber) {
		return String.format("2 %d %d 1 %d\n", connection.x, connection.y, tuplesNumber);
	}

	public void model() {
		SpacedStation[] spacedStations = this.computeSpacedStations();

		try {
			BufferedWriter writer = this.resolveWriter();
			
			this.computeHeader(writer);
			
			this.computeDomainSizes(spacedStations, writer);

			this.computeInterferenceConstraint(spacedStations, writer);
			
			this.computeConnectionConstraint(spacedStations, writer);

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.exit(0);
	}
}
