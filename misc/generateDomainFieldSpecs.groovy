import groovy.json.JsonSlurper

@Grab('com.xlson.groovycsv:groovycsv:0.2')
import com.xlson.groovycsv.CsvParser

boolean printPrettyJson = true
String sourceFile = 'fieldSettingsDefines.csv'
String csvData = new File(sourceFile).text

Map map = [
	APPLICATION: [
		domain: 'application',
		version: 1,
		fields: []
	],
	DEVICE: [
		domain: 'device',
		version: 1,
		fields: []
	],
	DATABASE: [
		domain: 'database',
		version: 1,
		fields: []
	],
	STORAGE: [
		domain: 'storage',
		version: 1,
		fields: []
	]
]

// "domain","field","label","type","control"
if (!csvData) {
	println "Failed to read $sourceFile"
}
def data = new CsvParser().parse(csvData)
// int order=0
String domain=''
String mapKey=''
for (line in data) {
	if (domain != line.domain) {
		// order = 0
		domain = line.domain
		mapKey = domain.toUpperCase()
	}
	if (line.order == '-1') {
		// Skip properties marked to be removed
		continue
	}

	Map spec = [
		field: line.field,
		label: line.label,
		tip: "This field is the ${line.label}",
		udf: (line.field.startsWith('custom') ? 1 : 0 ),
		default: '',
		shared: 0,
		show: (line.show == 'true' ? 1 : 0 ),
		control: line.control,
		order: line.order.toLong(),
		imp: 'I',
		constraints: [
			required: (line.field == 'assetName' ? 1 : 0),
		]
	]

	// Put in some example data
	if (line.label == 'Plan Strategy') {
		switch(mapKey) {
			case 'APPLICATION':
				spec.constraints.values=['Rehost', 'Refactor', 'Revise', 'Rebuild', 'Replace', 'Retire']
				break
			case 'DATABASE':
				spec.constraints.values=['Replication', 'Backup/Restore', 'Log Shipping', 'LUN replication']
				break
			case 'DEVICE':
				spec.constraints.values=['Lift&Shift', 'Rebuild', 'P2V', 'V2C']
				break
			case 'STORAGE':
				spec.constraints.values=['Replication', 'Backup/Restore', 'Mirror']
				break
		}
	}

	map[mapKey].fields.add(spec)

}

// Write the whole map out
String filename = "combined.json"
def file
def writter
def builder


file = new File(filename)
if (!file) {
	throw new RuntimeException("Unable to create file $filename")
}
writter = file.newWriter()

builder = new groovy.json.JsonBuilder(map)
if (printPrettyJson) {
	writter << builder.toPrettyString()
} else {
	writter << builder.toString()
}
writter.flush()
writter.close()
println "Created $filename"

/*
// Write out the individual maps
map.each { key, fieldSpec ->
	filename = "${key}.json"
	file = new File(filename)
	if (!file) {
		throw new RuntimeException("Unable to create file $filename")
	}
	writter = file.newWriter()

	builder = new groovy.json.JsonBuilder(fieldSpec)
	if (printPrettyJson) {
		writter << builder.toPrettyString()
	} else {
		writter << builder.toString()
	}
	writter.flush()
	writter.close()
	println "Created $filename"
}
*/

/*
String jsonText = '''{
	"person": {
		"name": "John",
		"age": 52
	}
}
'''

def slurper = new groovy.json.JsonSlurper()
def jsonMap = slurper.parseText(jsonText)
println jsonMap.getClass().getName()


def builder = new groovy.json.JsonBuilder(jsonMap)
println "json text: " + builder.toString()
println ""
*/

