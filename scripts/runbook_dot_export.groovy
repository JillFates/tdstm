import groovy.sql.Sql
import java.lang.RuntimeException
import groovy.time.TimeCategory
import groovy.time.TimeDuration

//
// USER DEFINED PER RUN OF APP
//
//def dbHost = 'dev02.tdsops.net'
//def dbHost = 'dev01.tdsops.net'
def dbHostList = ['localhost':'localhost', 'dev':'dev01.tdsops.net', 'prod':'dev02.tdsops.net']
def dbHost

if (this.args.size() != 3) {
	println "\nIncorrect number of arguments, expecting 3\n"
	println "\nUsage: groovy runbook_dot_export.groovy DbHost ProjectID moveEvent\n"
	return
}

if (dbHostList.containsKey(this.args[0]) ) {
	dbHost = dbHostList.get(this.args[0])
} else {
	def dblist 
	println "\nInvalid DbHost specified - options are: ${dbHostList.keySet()}\n"
	return
}
def sql = Sql.newInstance("jdbc:mysql://${dbHost}/tdstm", 'tdstm', 'tdstm', 'com.mysql.jdbc.Driver')

def projectId = this.args[1]
if (! projectId.isNumber()) {
	println "ProjectID must be numeric"
	return
}
def projectName = sql.firstRow("SELECT project_code FROM project WHERE project_id=${projectId}")?.project_code
if (! projectName) {
	println "Unable to find project with id $projectId"
	return
}

def eventId = this.args[2]
if (! eventId.isNumber()) {
	println "Move Event Id must be numeric"
	return
}
def eventName = sql.firstRow("SELECT name FROM move_event WHERE project_id=${projectId} AND move_event_id=${eventId}")?.name
if (! eventName ) {
	println "Unable to find move event ${eventId}"
	return
}
/*
def importFile = new File(this.args[3])
if ( ! importFile.exists() || ! importFile.canRead() ) {
	println "Unable to open file ${this.args[2]} for reading"
	return
}
*/

/*
// Prompt that the user wants to proceed
def input
System.in.withReader {
    print "Export runbook into $dbHost for project $projectName, are you sure (Y/N)? "
    input = it.readLine()
}
if (input != 'Y') return
*/

def now = new Date().format('yyyy-MM-dd H:m:s')

// println "\nRUNBOOK DATA IMPORT\n\ndbHost ${dbHost}\nproject ${projectId}\n\n"


def query = """
SELECT 
  t.task_number, 
  GROUP_CONCAT(s.task_number SEPARATOR ',') AS successors,
  IFNULL(a.asset_name,'') as asset, 
  '' AS step,
  '' AS ref,
  '' AS application,
  t.comment as task, 
  t.role,
  -- IF(t.hard_assigned=1,t.role,'') as hard_assign, 
  IFNULL(CONCAT(first_name,' ', last_name),'') as hard_assign,
  '' AS notes,
  t.duration
  -- IFNULL(t.est_start,'') AS est_start
FROM asset_comment t
LEFT OUTER JOIN task_dependency d ON d.predecessor_id=t.asset_comment_id
LEFT OUTER JOIN asset_comment s ON s.asset_comment_id=d.asset_comment_id
LEFT OUTER JOIN asset_entity a ON t.asset_entity_id=a.asset_entity_id
LEFT OUTER JOIN person ON t.owner_id=person.person_id
WHERE t.project_id=${projectId} AND t.move_event_id=${eventId} AND
  t.category IN ('moveday','shutdown','physical','startup')
GROUP BY t.task_number
"""
//AND t.task_number < 70

// println "\n\n$query\n\n"

println """#
# TDS Runbook for Project ${projectName}, Move Event ${eventName}
# Exported on ${now}
# This is  .DOT file format of the project tasks
#
digraph runbook {
	graph [rankdir=LR, fontsize=8, margin=0.001];
"""

sql.eachRow( query ) {
//	AND t.task_number < 40
//    println "Record: $it"
    def task = "${it.task_number}:${it.task}"
    task = (task.size() > 35) ? task[0..34] : task 
	println "\t${it.task_number} [label=\"${task}\"];"
	def successors = it.successors
	if (successors) {
		successors = (successors as Character[]).join('')
//		println "successors-a: ${successors}"
		successors = successors.split(',')
		successors.each { s -> 
			if (s.size() > 0) {
				println "\t${it.task_number} -> ${s};"
			}
		}
	}	
}

println "}"
				
