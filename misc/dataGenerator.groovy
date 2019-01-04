import groovy.json.JsonBuilder
// Setup code for parsing parameters and switches.
def cli = new CliBuilder(usage: 'dataGenearator.groovy -[hfjcd] [startDate(yyyy-mm-dd)] [endDate(yyyy-mm-dd)]')

cli.with {
	h longOpt: 'help', 'Show usage information.'
	f longOpt: 'file-name', args: 1, argName: 'name', 'Name of the output file defaults to "output".'
	j longOpt: 'json', 'Sets the output format to json.'
	c longOpt: 'csv', 'Sets the output format to csv.'
	d longOpt: 'directory', args: 1, argName: 'directory', 'The output directory, which defaults to where the script is run.'
}

def options = cli.parse(args)
List extraArguments = options.arguments() ?: []

// Comment out for testing
if (extraArguments.size < 2) {
    cli.usage()
    return
}

if (options.h) {
	cli.usage()
	return
}

String fileName = options.f ?: 'output'
String outputDirectory = options.d ?: System.getProperty("user.dir")

String type
if (options.j) {
	type = 'json'
} else if (options.c) {
	type = 'csv'
} else {
	type = 'csv'
}

//extraArguments = ['2018-04-09', '2018-04-14'] //for testing
String dateFormat = 'yyyy-mm-dd'
Date startDate = new Date().parse(dateFormat, extraArguments[0])
Date endDate = new Date().parse(dateFormat, extraArguments[1])
//******************************************************

import groovy.transform.CompileStatic

/**
 * A class to generate data up Reporting Metrics
 * https://support.transitionmanager.com/browse/TM-10300
 */
@CompileStatic
class Dgenerator {
	String dateFormat
	String delimiter = ','
	Random randomNumberGenerator

	List<String> projects = [
			'alpha-kjkasu2l39nknsdf',
			'bravo-ipoad98anwe213sd',
			'charlie-qakuad923saiv7',
			'delta-qqhahakexr2na342'
	]

	List<String> metrics = ['APP-CNT', 'APP-VALID-CNT', 'APP-VALID_PLAN-CNT', 'SERVER-CNT']

	List<String> validations = ['Unknown', 'Validated', 'PlanReady']

	List<String> planStatuses = [
			'Unassigned',
			'Assigned',
			'Confirmed',
			'Locked',
			'Moved'
	]

	/**
	 * sets up a random instance with a seed.
	 *
	 * @param seed a seed for random, for testing.If null the seed becomes currentTimeMillis + freeMemory
	 */
	void setUpRandom(Long seed = null) {
		if (seed == null) {
			seed = System.currentTimeMillis() + Runtime.runtime.freeMemory()
		}

		if (randomNumberGenerator == null) {
			randomNumberGenerator = new Random(seed)
		}
	}

	/**
	 * A random number generator I copied from my Groovy talk on Meta programming:
	 * https://github.com/virtualdogbert/MetaProgrammingMagicRevealed/blob/master/Dgenerator.groovy
	 *
	 * @param min Lower bound for the generated number.
	 * @param max Upper bound for the generated number.
	 * @param seed An optional seed for the generator, using for testing.
	 *
	 * @return A random integer between min and max, using a seed for the generator.
	 */
	int generateRandomInt(int min, int max, Long seed = null) {
		setUpRandom(seed)
		return min + ((max - min) * randomNumberGenerator.nextDouble()) as int
	}

	/**
	 * Gets the Headers of the data for a csv file.
	 * 
	 * @param data The map data.
	 * 
	 * @return The headers from the data as a csv string
	 */
	String csvHeaders(Map<String, String> data) {
		data.keySet().join(delimiter)
	}

	/**
	 * Converts the Map data to a csv string.
	 * 
	 * @param data map of string data.
	 * 
	 * @return the data as a csv string.
	 */
	String asCSV(Map<String, String> data) {
		data.values().join(delimiter)
	}

	/**
	 * Just takes the data and converts it to a  JSON string.
	 *
	 * @param data A list of maps, that will be converted to JSON.
	 *
	 * @return A JSON string based on the data.
	 */
	String asJSON(List<Map<String, String>> data) {
		new JsonBuilder(data).toPrettyString()
	}

	/**
	 * Generates data for reporting metrics, based off a start and end date.
	 *
	 * @param startDate The date the metrics start.
	 * @param endDate The date the metrics end.
	 *
	 * @return A list of maps of data for the reporting metrics.
	 */
	List<Map> generateData(Date startDate, Date endDate) {
		List<Map> data = []
		(startDate..endDate).each { Date date ->
			String dateString = date.format(dateFormat)
			projects.each { String project ->
				metrics.each { String metric ->

					if (metric in ['APP-CNT', 'SERVER-CNT']) {
						data << [
								project   : project,
								metricCode: metric,
								date      : dateString,
								label     : 'count',
								value     : "${generateRandomInt(1, 100)}"
						]
					}

					if (metric == 'APP-VALID_PLAN-CNT') {
						//snagged this from my Rosetta code post: https://rosettacode.org/wiki/Deal_cards_for_FreeCell#Groovy
						[validations, planStatuses].combinations { String validation, String planStatus ->
							data << [
									project   : project,
									metricCode: metric,
									date      : dateString,
									label     : "${validation}:$planStatus",
									value     : "${generateRandomInt(1, 100)}"
							]
						}
					}

					if (metric == 'APP-VALID-CNT') {
						validations.each { String validation ->
							data << [
									project   : project,
									metricCode: metric,
									date      : dateString,
									label     : "$validation",
									value     : "${generateRandomInt(1, 100)}"
							]
						}
					}
				}
			}
		}

		return data
	}
}

//Code to setup and generate data
Dgenerator dgenerator = new Dgenerator(dateFormat: dateFormat)
File output = new File("$outputDirectory/${fileName}.$type")

if (output.exists()) {
	output.delete()
}

List<Map<String, String>> data = dgenerator.generateData(startDate, endDate)

//Outputs data for the correct file and format.
if (type.trim().toLowerCase() == 'csv') {
	output << "${dgenerator.csvHeaders(data[0])}\n"
	data.each { Map<String, String> row ->
		output << "${dgenerator.asCSV(row)}\n"
	}
} else {
	output << dgenerator.asJSON(data)
}
