import groovy.json.JsonSlurper

@Grab('com.xlson.groovycsv:groovycsv:0.2')
import com.xlson.groovycsv.CsvParser

boolean printPrettyJson = true
String sourceFile = 'fieldSettingsDefines.csv'
String csvData = new File(sourceFile).text

Map map = [
	application: [
		domain: 'application',
		fields: []
	],
	device: [
		domain: 'device',
		fields: []
	],
	database: [
		domain: 'database',
		fields: []
	],
	storage: [
		domain: 'storage',
		fields: []
	]
]
// "domain","field","label","type","control"
if (!csvData) {
	println "Failed to read $sourceFile"
}
def data = new CsvParser().parse(csvData)
int order=0
String domain=''
for(line in data) {

	if (domain != line.domain) {
		order = 0
		domain = line.domain
	}
	map[line.domain].fields << [
		field: line.field,
		label: line.label,
		tip: "This field is the ${line.label}",
		udf: (line.field.startsWith('custom') ? 1 : 0 ),
		shared: 0,
		imp: 'I',
		required: (line.field == 'assetName' ? 1 : 0),
		show: 1,
		order: ++order,
		type: (line.type=='int' ? 'Number' : line.type),
		default: '',
		control: ''
	]
}

map.each { key, fieldSpec ->
	String filename = "${key}.json"
	def file = new File(filename)
	if (!file) {
		throw new RuntimeException("Unable to create file $filename")
	}
	def writter = file.newWriter()

	def builder = new groovy.json.JsonBuilder(fieldSpec)
	if (printPrettyJson) {
		writter << builder.toPrettyString()
	} else {
		writter << builder.toString()
	}
}
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

