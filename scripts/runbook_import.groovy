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

if (this.args.size() != 4) {
	println "\nUsage: grails import_runbook.groovy DbHost ProjectID moveBundle ImportFile\n"
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

def projectName = sql.firstRow("SELECT project_code FROM project WHERE project_id=${projectId}")
if (! projectName) {
	println "Unable to find project with id $projectId"
	return
}

def moveBundleCode = this.args[2]
def moveBundle = sql.firstRow("""SELECT workflow_id AS id, move_event_id AS meId
	FROM move_bundle m
 	JOIN workflow w ON w.process=m.workflow_code
	WHERE project_id=${projectId} AND name=${moveBundleCode}""")
if (! moveBundle) {
	println "Unable to find move bundle ${moveBundleCode}"
}
def workflowId=moveBundle.id
def moveEventId=moveBundle.meId

def importFile = new File(this.args[3])
if ( ! importFile.exists() || ! importFile.canRead() ) {
	println "Unable to open file ${this.args[2]} for reading"
	return
}

// Prompt that the user wants to proceed
def input
System.in.withReader {
    print "Import runbook into $dbHost for project $projectName, are you sure (Y/N)? "
    input = it.readLine()
}
if (input != 'Y') return

def now = new Date().format('yyyy-MM-dd H:m:s')

// println "\nRUNBOOK DATA IMPORT\n\ndbHost ${dbHost}\nproject ${projectId}\n\n"

def runbook = []
def lastMoveBundleId
def moveEvents = [:]
def taskList = [:]
def dependencies = []
def taskCount=0
def predecessorCount=0
def status
def estStart
def estFinish
def task = sql.dataSet('asset_comment')
def taskDependency = sql.dataSet('task_dependency')

// The starting taskId  that needs to be set before running the script in order to have proper sequence #
int taskId = sql.firstRow("SELECT MAX(asset_comment_id) AS id FROM asset_comment").id + 1

// Import the runbook int an array of associative records
def isHeader=true
importFile.splitEachLine("\t") { fields ->
	
	if (isHeader) {
		isHeader=false
	} else {
		def assetId = fields[4]
		def taskNum = fields[9]
		def predecessors = fields[10]
		def stepCode = fields[11]
		def role = fields[2]
		def moveEventCode = fields[5]
		def assignedTo = fields[0]
		def hardAssigned = fields[1]
		def category = fields[12].toLowerCase()
		def priority = fields[13]
		
		priority = priority == '' ? 3 : priority

		use (TimeCategory) {		
			estStart = new Date().parse("yyyy-M-d H:m:s", fields[6]) + 4.hours
			estFinish = new Date().parse("yyyy-M-d H:m:s", fields[7]) + 4.hours
		}

		// Tasks that have predecessor 0 will default to Ready
		if (predecessors == '0' || predecessors == '' ) {
			status = 'Ready'
			predecessors = ''
		} else {
			status = 'Pending'
		}

		// Relational integrity checks
		if (assignedTo) {
			def person = sql.firstRow("SELECT concat(last_name,',',first_name) FROM person where person_id=${assignedTo}")
			if (! person) {
				throw new RuntimeException("Unable to find person id (${assignedTo}) for task ${taskNum}") 
			}
		}
		if (assetId) {
			def asset = sql.firstRow("SELECT asset_tag FROM asset_entity WHERE asset_entity_id = ${assetId}")
			if (! asset) {
				throw new RuntimeException("Unable to find asset id (${assetId}) for task ${taskNum}") 				
			}
		}
		def existingTask = sql.firstRow("SELECT task_number FROM asset_comment WHERE project_id=${projectId} AND task_number=${taskNum}")
		if (existingTask) {
			throw new RuntimeException( "Task number ${taskNum} already exists for this project") 							
		}
		
		// Keep track of all of the taskNum and what Id they map to, then track their predecessors so we can generate them afterward
		taskList[taskNum] = taskId
		if (predecessors) {
			dependencies << [ taskId:taskId, predecessors:predecessors ]
		} 
		
		// println "assetId:${assetId} taskNum:${taskNum} stepCode:${stepCode} role:${role}"

		/*
		if (assetId && ! workflowId) {			
			// Find the moveBundle and subsequently the Workflow id associated with the step code and the workflow 
			def moveBundleId = sql.firstRow("SELECT move_bundle_id FROM asset_entity WHERE asset_entity_id = ${assetId}").move_bundle_id
			if (moveBundleId != lastMoveBundleId) {
				lastMoveBundleId = moveBundleId
				workflowId = sql.firstRow("SELECT workflow_id AS id FROM move_bundle m JOIN workflow w ON w.process=m.workflow_code "+
					"WHERE move_bundle_id=${moveBundleId}").id
			}
			moveEventId = null
		} else {
			assetId = null
		}
		
		
		// Look up the moveEventId and projectId
		if (! moveEvents.containsKey(moveEventCode) ) {
			moveEventId = sql.firstRow("SELECT move_event_id AS id FROM move_event WHERE project_id=${projectId} AND name=${moveEventCode}").id			
			moveEvents << [ "${moveEventCode}":moveEventId ]
		}
		moveEventId = moveEvents.get("${moveEventCode}")
		*/

		// Set the assetId to null if it doesn't have a value
		assetId = assetId ?: null
	
		// Lookup the workflow transition id from the workflow id and step code
		def workflowTransitionId=null
		if (stepCode) {
			def s = "SELECT workflow_transition_id AS id FROM workflow_transition WHERE workflow_id=${workflowId} AND trans_id=${stepCode}"
			def wfResult = sql.firstRow(s)
			if (wfResult) {
				workflowTransitionId = wfResult.id				
			} else {
				throw new RuntimeException("Unable to find StepCode (${stepCode}) using workflowId (${workflowId}) for Task number ${taskNum}") 											
			}
		}

		task.add(
			asset_comment_id: taskId,
			version:1,
			comment_type: 'issue', 
			created_by: 100, 
			date_created: now,
			last_updated: now, 
			is_resolved: 0,
			must_verify: 0,
			status: status, 
			workflow_override: 0, 
			category: category,
			project_id: projectId, 
			duration_scale: 'm',
			display_option: 'U',
			//
			// From import 
			//
			assigned_to_id: assignedTo, 
			hard_assigned: hardAssigned,
			role: role, 
			COMMENT: fields[3],
			asset_entity_id: assetId,
			move_event_id: moveEventId, 
			est_start: estStart,
			est_finish: estFinish, 
			duration: fields[8], 
			task_number: taskNum, 
			workflow_transition_id: workflowTransitionId,
			priority: priority
		)
	
		//println "assignedTo:${assignedTo} asset_comment_id:${taskId} assetId:${assetId} workflowId:${workflowId}, workflowTransitionId:${workflowTransitionId}" +
		//   "taskNum:${taskNum} stepCode:${stepCode} role:${role} title:${fields[3]} est_start:${fields[6]}"
		taskCount++   
		taskId++
		println "$taskCount - task: $taskNum role:$role asset:$assetId stepCode:$stepCode"

	}
}	// read file

// Generate the TaskDependency relationships
dependencies.each() { dependency ->
	def predTasks = dependency.predecessors.tokenize('|')
	predTasks.each() { predNum ->
		def predecessorId = taskList[predNum]
		println "asset_comment_id:${dependency.taskId} predecessorId:${predecessorId} predecessorNum:${predNum}" 
		taskDependency.add(version:1, asset_comment_id:dependency.taskId, type:'FS', predecessor_id:predecessorId)
	}
	predecessorCount++
}

println "Created ${taskCount} tasks and ${predecessorCount} dependencies"				
