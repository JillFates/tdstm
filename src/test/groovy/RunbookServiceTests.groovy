import com.tds.asset.AssetComment
import com.tds.asset.TaskDependency
import com.tdsops.metaclass.CustomMethods
import grails.test.mixin.TestFor
import net.transitionmanager.service.RunbookService
import spock.lang.Specification

@TestFor(RunbookService)
class RunbookServiceTests extends Specification {

	private static final List<Map> mapData = [
		[id: 1, name: 'John',  color: 'blue'],
		[id: 2, name: 'Tom',   color: 'orange'],
		[id: 3, name: 'Dick',  color: 'blue'],
		[id: 4, name: 'Harry', color: 'green'],
		[id: 5, name: 'Sarah', color: 'white'],
		[id: 6, name: 'Tony',  color: 'green'],
	]

	// This represents the [ edge id, downstreamTaskCount, pathDuration ] for the map defined below
	private static final List<List> dataMatrix = [
		[i: '100', c: 7, d: 71],
		[i: '101', c: 6, d: 73],
		[i: '102', c: 5, d: 63],
		[i: '103', c: 2, d: 21],
		[i: '104', c: 5, d: 63],
		[i: '105', c: 4, d: 60],
		[i: '106', c: 1, d: 1],
		[i: '107', c: 7, d: 71],
		[i: '108', c: 6, d: 73],
		[i: '109', c: 1, d: 45],
		[i: '110', c: 1, d: 2],
		[i: '111', c: 1, d: 1]
	]

	private List tasks = []
	private List deps = []

	void setup() {
		tasks << new AssetComment(id: 0, taskNumber: 1000, duration: 5, comment: 'Start Move') // Start vertex
		tasks << new AssetComment(id: 1, taskNumber: 1001, duration: 8, comment: 'SD App Exchange')
		tasks << new AssetComment(id: 2, taskNumber: 1002, duration: 10, comment: 'SD App Payroll')
		tasks << new AssetComment(id: 3, taskNumber: 1003, duration: 3, comment: 'PD Srv xyzzy')
		tasks << new AssetComment(id: 4, taskNumber: 1004, duration: 20, comment: 'PD VM vsmssql01')
		tasks << new AssetComment(id: 5, taskNumber: 1005, duration: 15, comment: 'Unrack Srv xyzzy')
		tasks << new AssetComment(id: 6, taskNumber: 1006, duration: 9, comment: 'Disable monitoring') // Start vertex
		tasks << new AssetComment(id: 7, taskNumber: 1007, duration: 45, comment: 'Post Move Testing') // Sink vertex
		tasks << new AssetComment(id: 8, taskNumber: 1008, duration: 2, comment: 'Make Coffee') // Sink vertex
		tasks << new AssetComment(id: 9, taskNumber: 1009, duration: 1, comment: 'Done Move') // Sink vertex

		deps << new TaskDependency(id: 100, predecessor: tasks[0], assetComment: tasks[1], type: 'SS')
		// 1 > [3,4], 3 > 5, 5 > [7,8,9], 4 > 9
		// 8    3,20  3  15, 15  45,2,1   20  1
		// 1 > 3 > 5 > [7,8,9] (72)
		// 1 > 4 > 9 (29)
		deps << new TaskDependency(id: 101, predecessor: tasks[0], assetComment: tasks[2], type: 'SS')
		deps << new TaskDependency(id: 102, predecessor: tasks[1], assetComment: tasks[3], type: 'SS')
		deps << new TaskDependency(id: 103, predecessor: tasks[1], assetComment: tasks[4], type: 'SS')
		deps << new TaskDependency(id: 104, predecessor: tasks[2], assetComment: tasks[3], type: 'SS')
		deps << new TaskDependency(id: 105, predecessor: tasks[3], assetComment: tasks[5], type: 'SS')
		// 4 downstream tasks
		deps << new TaskDependency(id: 106, predecessor: tasks[4], assetComment: tasks[9], type: 'SS')
		deps << new TaskDependency(id: 107, predecessor: tasks[6], assetComment: tasks[1], type: 'SS')
		deps << new TaskDependency(id: 108, predecessor: tasks[6], assetComment: tasks[2], type: 'SS')
		deps << new TaskDependency(id: 109, predecessor: tasks[5], assetComment: tasks[7], type: 'SS')
		deps << new TaskDependency(id: 110, predecessor: tasks[5], assetComment: tasks[8], type: 'SS')
		deps << new TaskDependency(id: 111, predecessor: tasks[5], assetComment: tasks[9], type: 'SS')

		int id = 0
		tasks.each { t -> t.id = id++ }
		id = 100
		deps.each { d -> d.id = id++ }

		// Initialize various custom methods used by our application
		CustomMethods.initialize(true)
	}

	// Test that the List.asMap method actually works
	void testAsMap() {
		def staff = mapData.asMap('id')

		expect:
		'John' == staff['1'].name
		'Tom' == staff['2'].name
		'Sarah' == staff['5'].name
		'id 666 should not exist' != staff.containsKey(666)
		6 == staff.size()
	}

	// Test that the List.asMap method actually works
	void testAsGroup() {
		when:
		def byColor = mapData.asGroup({ it.color })
		def harry = byColor['blue'].find { it.name == 'Harry' }

		then:
		byColor.size() == 4 //Should have 4 colors
		byColor.containsKey('blue') //Should contain the key "blue"
		byColor['blue'].size() == 2 //Blue should have two objects
		byColor['blue'].find({ it.name == 'John' })?.name == 'John' //Blue should contain "John"
		harry == null //Blue should not contain "Harry"
	}

	// Test that ArrayList.groupBy function is working as we hope that it would
	void testAsMapForTestData() {
		when:
		def by = deps.asGroup({ it.predecessor.id })

		then:
		by.size() == 7 //Should have 7 groups
		by.containsKey('0') //id "0" should be one of the ids in the map
		by['0'].size() == 2 //Group "0" should have 2 nodes
		by['5'].size() == 3 //Group "5" should have 3 nodes
		by['0'].find {
			it.predecessor.taskNumber == 1000
		}?.predecessor.taskNumber == 1000 //Group "0" should contain dependency "1000"
		by['0'].find { it.predecessor.taskNumber == 1014 } == null //Group "0" should not contain dependency "1014"
	}

	// Test that the processDFS is returning the proper results
	void testProcessDFS() {
		when:
		def tmp = service.createTempObject(tasks, deps)
		def dfsMap = service.processDFS(tasks, deps, tmp)

		then:
		'java.util.ArrayList' == dfsMap.starts.getClass().name //Starts type
		'java.util.ArrayList' == dfsMap.sinks.getClass().name //Sinks type
		'java.util.LinkedHashMap' == dfsMap.cyclicals.getClass().name //Cyclicals type

		dfsMap.starts.size() == 2 //Should have 2 Start vertices
		dfsMap.sinks.size() == 3 //Should have 3 Sink vertices
		dfsMap.cyclicals.size() == 0 //Should have 0 Cyclical vertices

		dfsMap['starts'].find { it.id == 0 }?.id != null //Starts vertices should contain id 0
		dfsMap['starts'].find { it.id == 6 }?.id != null //Starts vertices should contain id 6
		dfsMap['sinks'].find { it.id == 7 }?.id != null //Sinks vertices should contain id 7
		dfsMap['sinks'].find { it.id == 8 }?.id != null //Sinks vertices should contain id 8
		dfsMap['sinks'].find { it.id == 9 }?.id != null //Sinks vertices should contain id 9
	}

	// Test that the processDurations is returning the proper results
	void testProcessDurations() {
		when:
			def tmp = service.createTempObject(tasks, deps)
			def dfsMap = service.processDFS(tasks, deps, tmp)
			def durMap = service.processDurations(tasks, deps, dfsMap.sinks, tmp)

		then:

			for (Map data : dataMatrix) {
				data.c == tmp['dependencies'][durMap.edges[data.i].id].tmpDownstreamTaskCount
				data.d == tmp['dependencies'][durMap.edges[data.i].id].tmpPathDuration
			}

			tmp['tasks'][durMap.tasks[9].id].tmpMapDepth == 1 //Finish tmpMapDepth
			tmp['tasks'][durMap.tasks[5].id].tmpMapDepth == 2 //Task 1005 tmpMapDepth
			tmp['tasks'][durMap.tasks[3].id].tmpMapDepth == 3 //Task 1003 tmpMapDepth
			tmp['tasks'][durMap.tasks[2].id].tmpMapDepth == 4 //Task 1002 tmpMapDepth
			tmp['tasks'][durMap.tasks[1].id].tmpMapDepth == 4 //Task 1001 tmpMapDepth
			tmp['tasks'][durMap.tasks[0].id].tmpMapDepth == 5 //Task 1000 tmpMapDepth
	}

	// Test that the processDFS and processDurations properly handle an cicular references within the map
	void testForCyclicalMapping() {
		when:
		// Add a cyclical reference
		deps << new TaskDependency(id: 666, predecessor: tasks[5], assetComment: tasks[2], type: 'SS')

		def tmp = service.createTempObject(tasks, deps)
		def dfsMap = service.processDFS(tasks, deps, tmp)

		then:
		dfsMap.starts.size() == 2 //Should have 2 Start vertices
		dfsMap.sinks.size() == 3 //Should have 3 Sink vertices
		dfsMap.cyclicals.size() == 1 //Should have 1 Cyclical vertex

		when:
		def durMap = service.processDurations(tasks, deps, dfsMap.sinks, tmp)

		// println "What tasks does edge 105 have? ${durMap.edges['105'].successor.tmpDownstreamTasks}"
		// Run the same process as before and we shouldn't see any differences
		def m = [
			[i: '103', c: 2, d:21],   // We added the earlier reference
			[i: '109', c: 1, d:45]   // Shouldn't of changed
		]

		then:
		for (Map data: m) {
			data.c == tmp['dependencies'][durMap.edges[data.i].id].tmpDownstreamTaskCount
			data.d == tmp['dependencies'][durMap.edges[data.i].id].tmpPathDuration
		}
	}

	// Test that the determineUniqueGraphs method properly matches up the start vertices with the sink vertices
	void testDetermineUniqueGraphs() {
		when:
		def tmp = service.createTempObject(tasks, deps)

		def dfsMap = service.processDFS(tasks, deps, tmp)
		def durMap = service.processDurations(tasks, deps, dfsMap.sinks, tmp)
		def graphs = service.determineUniqueGraphs(dfsMap.starts, dfsMap.sinks, tmp)

		// {starts=[0, 6], sinks=[7, 8, 9], maxPathDuration=73, maxDownstreamTaskCount=8}

		then:
		graphs.size() == 1 //Returns a list with one group
		graphs[0].starts == [0, 6]
		graphs[0].sinks == [7, 8, 9]
		graphs[0].maxPathDuration == 82
		graphs[0].maxDownstreamTaskCount == 8
	}

	// Test that the determineUniqueGraphs method properly matches up the start vertices with the sink vertices when there are multiple graphs in the vertices
	void testDetermineUniqueGraphsMultiple() {
		when:
		// Add a second separate set of tasks
		def ltid = tasks.size() - 1
		def asset
		def dep

		asset = new AssetComment(taskNumber: 1050, duration: 90, comment: 'Separate map Start task') // start vertex
		asset.id = ltid + 1
		tasks << asset

		asset = new AssetComment(taskNumber: 1051, duration: 7, comment: 'Separate map Middle task')
		asset.id = ltid + 2
		tasks << asset

		asset = new AssetComment(taskNumber: 1052, duration: 12, comment: 'Separate map Sink task') // start vertex
		asset.id = ltid + 3
		tasks << asset

		dep = new TaskDependency(predecessor: tasks[ltid + 1], assetComment: tasks[ltid + 2], type: 'SS')
		dep.id = 200
		deps << dep

		dep = new TaskDependency(predecessor: tasks[ltid + 2], assetComment: tasks[ltid + 3], type: 'SS')
		dep.id = 201
		deps << dep

		// Add an additional starting vector that is shorter so we can see the counts
		asset = new AssetComment(taskNumber: 1060, duration: 120, comment: 'Change tire on truck')
		asset.id = ltid + 4
		tasks << asset

		dep = new TaskDependency(predecessor: tasks[ltid + 4], assetComment: tasks[5], type: 'SS')
		dep.id = 210
		deps << dep

		def tmp = service.createTempObject(tasks, deps)
		def dfsMap = service.processDFS(tasks, deps, tmp)
		def durMap = service.processDurations(tasks, deps, dfsMap.sinks, tmp)

		def graphs = service.determineUniqueGraphs(dfsMap.starts, dfsMap.sinks, tmp)

		// {starts=[0, 6], sinks=[7, 8, 9], maxPathDuration=73, maxDownstreamTaskCount=8}

		then:
		graphs.size() == 2 //Returns a list with one group
		[0, 6, ltid + 4] == graphs[0].starts //1st Starts
		[7, 8, 9] == graphs[0].sinks //1st Sinks
		180 == graphs[0].maxPathDuration //
		8 == graphs[0].maxDownstreamTaskCount //1st maxDownstreamTaskCount

		[ltid + 1] == graphs[1].starts //2nd Starts
		[ltid + 3] == graphs[1].sinks //2nd Sinks
		109 == graphs[1].maxPathDuration //2nd maxPathDuration
		2 == graphs[1].maxDownstreamTaskCount //2nd maxDownstreamTaskCount
	}

	// Test that the findCriticalStartTask method properly determines the critical task to start with from the graph list of tasks
	void testFindCriticalStartTask() {
		when:
		def tmp = service.createTempObject(tasks, deps)

		def dfsMap = service.processDFS(tasks, deps, tmp)
		def durMap = service.processDurations(tasks, deps, dfsMap.sinks, tmp)
		def graphs = service.determineUniqueGraphs(dfsMap.starts, dfsMap.sinks, tmp)

		def tasksMap = tasks.asMap('id')

		def task = service.findCriticalStartTask(tasksMap, graphs[0], tmp)

		then:
		6 == task.id
	}

	// Test that the findCriticalPath method properly determines the critical path edge based on the longest duration
	void testFindCriticalPath() {
		when:
		def tmp = service.createTempObject(tasks, deps)

		def dfsMap = service.processDFS(tasks, deps, tmp)
		def durMap = service.processDurations(tasks, deps, dfsMap.sinks, tmp)
		def graphs = service.determineUniqueGraphs(dfsMap.starts, dfsMap.sinks, tmp)

		def edgesByPred = deps.asGroup { it.predecessor.id }

		and:
		def edges = service.findCriticalPath(tasks[6], edgesByPred, tmp)
		def edge = edges[0]

		then:
		edge != null //Edge should not be null
		108 == edge.id //Critical edge should be
		edge != null
		108 == edge.id

		when:
		edges = service.findCriticalPath(tasks[5], edgesByPred, tmp)
		edge = edges[0]

		then:
		edge != null
		109 == edge.id

		when:
		edges = service.findCriticalPath(tasks[8], edgesByPred, tmp)
		edge = edges[0]

		then:
		edge == null
	}

	// Test that the computeStartTimes method properly calculates the various estStart, earliest and latest starts for each task in the graph(s)
	void testComputeStartTimes() {
		when:
		def tmp = service.createTempObject(tasks, deps)

		def dfsMap = service.processDFS(tasks, deps, tmp)
		def durMap = service.processDurations(tasks, deps, dfsMap.sinks, tmp)

		def graphs = service.determineUniqueGraphs(dfsMap.starts, dfsMap.sinks, tmp)

		//println "Before computeStartTimes call"
		//tasks.each { t -> println "Task ${t.taskNumber}/${t.id} duration=${t.duration}, estStart=${tmp['tasks'][t.id].tmpEstimatedStart}, earliest=${tmp['tasks'][t.id].tmpEarliestStart}, latest=${tmp['tasks'][t.id].tmpLatestStart}, CP=${tmp['tasks'][t.id].tmpCriticalPath}"}

		def startTime = 0
		def estFinish = service.computeStartTimes(startTime, tasks, deps, dfsMap.starts, dfsMap.sinks, graphs, tmp)

		//println "After computeStartTimes call"
		//tasks.each { t -> println "Task ${t.taskNumber}/${t.id} duration=${t.duration}, estStart=${tmp['tasks'][t.id].tmpEstimatedStart}, earliest=${tmp['tasks'][t.id].tmpEarliestStart}, latest=${tmp['tasks'][t.id].tmpLatestStart}, CP=${tmp['tasks'][t.id].tmpCriticalPath}"}

		// Task id, estStart, earliest, latest, is Critical Path
		// Estimated Start (6) expected:<0> but was:<44>
		def startTimes = [
			[id:0, estStart: 0, earliest: 0,latest: 48, criticalPath: false],
			[id:1, estStart: 0, earliest: 9,latest: 53, criticalPath: false],
			[id:2, estStart: 9, earliest: 9,latest: 9, criticalPath: true],
			[id:3, estStart: 19, earliest: 19,latest: 19, criticalPath: true],
			[id:4, estStart: 0, earliest: 17,latest: 61, criticalPath: false],
			[id:5, estStart: 22, earliest: 22,latest: 22, criticalPath: true],
			[id:6, estStart: 0, earliest: 0,latest: 0, criticalPath: true],   // Start vertice of the true Critical Path
			[id:7, estStart: 37, earliest: 37,latest: 37, criticalPath: true],
			[id:8, estStart: 0, earliest: 37,latest: 80, criticalPath: false],
			[id:9, estStart: 0, earliest: 37,latest: 81, criticalPath: false],
		]

		then:
		82 == estFinish //estFinish should be zero

		for(Map st: startTimes){
			st.estStart == tmp['tasks'][tasks[st.id].id].tmpEstimatedStart
			st.earliest == tmp['tasks'][tasks[st.id].id].tmpEarliestStart
			st.latest == tmp['tasks'][tasks[st.id].id].tmpLatestStart
			st.criticalPath == tmp['tasks'][tasks[st.id].id].tmpCriticalPath
		}
	}
}
