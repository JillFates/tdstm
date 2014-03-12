import groovy.mock.interceptor.*
import grails.test.GrailsUnitTestCase
import org.codehaus.groovy.grails.commons.ConfigurationHolder

import org.apache.log4j.* 

import com.tds.asset.AssetComment
import com.tds.asset.TaskDependency

/**
 * Unit test cases for the RunbookService class
 */
class RunbookServiceTests extends GrailsUnitTestCase {
	
	def runbookService = new RunbookService()
	def log

	def mapData = [
		[id:1, name:'John', color:'blue'],
		[id:2, name:'Tom', color:'orange'],
		[id:3, name:'Dick', color:'blue'],
		[id:4, name:'Harry', color:'green'],
		[id:5, name:'Sarah', color:'white'],
		[id:6, name:'Tony', color:'green'],
	]
	
	// This represents the [ edge id, downstreamTaskCount, pathDuration ] for the map defined below
	def dataMatrix = [
		['100', 7, 71],
		['101', 6, 73],
		['102', 5, 63],
		['103', 2, 21],
		['104', 5, 63],
		['105', 4, 60],
		['106', 1,  1],
		['107', 7, 71],
		['108', 6, 73],
		['109', 1, 45],
		['110', 1,  2],
		['111', 1,  1]
	]

	/**
	 * This is used to load the grails-app/conf/Config.groovy which contains both configurations as well as 
	 * dynamic method injections for various object classes to be used by the application
	 */
	private void loadConfig() { 
		GroovyClassLoader classLoader = new GroovyClassLoader(this.class.classLoader) 
		ConfigSlurper slurper = new ConfigSlurper('TEST') 
		ConfigurationHolder.config = slurper.parse(classLoader.loadClass("Config")) 
	} 

	void setUp() {
		// add the super call to avoid the "NullPointerException: Cannot invoke method containsKey() on null object" when calling mockDomain 
		super.setUp() 

		// build a logger...
		BasicConfigurator.configure() 
		LogManager.rootLogger.level = Level.DEBUG
		log = LogManager.getLogger("RunbookService")

		// use groovy metaClass to put the log into your class
		RunbookService.class.metaClass.getLog << {-> log }

		//loadConfig()
		// Initialize various custom methods used by our application
		com.tdsops.metaclass.CustomMethods.initialize

	}


	def initTestData = {

		def tasks = []
		def deps = []
		tasks << new AssetComment(id:0, taskNumber: 1000, duration:5, comment:'Start Move')	// Start vertex
		tasks << new AssetComment(id:1, taskNumber: 1001, duration:8, comment:'SD App Exchange')
		tasks << new AssetComment(id:2, taskNumber: 1002, duration:10, comment:'SD App Payroll')
		tasks << new AssetComment(id:3, taskNumber: 1003, duration:3, comment:'PD Srv xyzzy')
		tasks << new AssetComment(id:4, taskNumber: 1004, duration:20, comment:'PD VM vsmssql01')
		tasks << new AssetComment(id:5, taskNumber: 1005, duration:15, comment:'Unrack Srv xyzzy')
		tasks << new AssetComment(id:6, taskNumber: 1006, duration:9, comment:'Disable monitoring')	// Start vertex
		tasks << new AssetComment(id:7, taskNumber: 1007, duration:45, comment:'Post Move Testing') // Sink vertex
		tasks << new AssetComment(id:8, taskNumber: 1008, duration:2, comment:'Make Coffee') // Sink vertex
		tasks << new AssetComment(id:9, taskNumber: 1009, duration:1, comment:'Done Move') // Sink vertex

		deps << new TaskDependency(id:100, predecessor:tasks[0], assetComment:tasks[1], type:'SS') 
		// 1 > [3,4], 3 > 5, 5 > [7,8,9], 4 > 9
		// 8    3,20  3  15, 15  45,2,1   20  1
		// 1 > 3 > 5 > [7,8,9] (72)
		// 1 > 4 > 9 (29)
		deps << new TaskDependency(id:101, predecessor:tasks[0], assetComment:tasks[2], type:'SS')
		deps << new TaskDependency(id:102, predecessor:tasks[1], assetComment:tasks[3], type:'SS')
		deps << new TaskDependency(id:103, predecessor:tasks[1], assetComment:tasks[4], type:'SS')
		deps << new TaskDependency(id:104, predecessor:tasks[2], assetComment:tasks[3], type:'SS')
		deps << new TaskDependency(id:105, predecessor:tasks[3], assetComment:tasks[5], type:'SS') // 4 downstream tasks
		deps << new TaskDependency(id:106, predecessor:tasks[4], assetComment:tasks[9], type:'SS')
		deps << new TaskDependency(id:107, predecessor:tasks[6], assetComment:tasks[1], type:'SS')
		deps << new TaskDependency(id:108, predecessor:tasks[6], assetComment:tasks[2], type:'SS')
		deps << new TaskDependency(id:109, predecessor:tasks[5], assetComment:tasks[7], type:'SS')
		deps << new TaskDependency(id:110, predecessor:tasks[5], assetComment:tasks[8], type:'SS')
		deps << new TaskDependency(id:111, predecessor:tasks[5], assetComment:tasks[9], type:'SS')

		return [tasks, deps]

	}

	// Test that the List.asMap method actually works
	void testAsMap() {
		// def byColor = mapData.asGroup('color')
		def staff = mapData.asMap( 'id')

		assertEquals 'John', staff['1'].name
		assertEquals 'Tom', staff['2'].name
		assertEquals 'Sarah', staff['5'].name
		assertFalse 'id 666 should not exist', staff.containsKey('666')
		assertEquals 'Map should have 6 elements', 6, staff.size()
	}
	
	// Test that the List.asMap method actually works
	void testAsGroup() {

		// def byColor = mapData.asGroup('color')
		def byColor = mapData.asGroup({ it.color })
		def harry = byColor['blue'].find { it.name == 'Harry'}

		println "byColor=$byColor"
		println "find Harry? $harry"

		assertEquals 'Should have 4 colors', 4, byColor.size()
		assertTrue 'Should contain the key "blue"', byColor.containsKey('blue')
		assertEquals 'Blue should have two objects', 2, byColor['blue'].size()
		assertTrue  'Blue should contain "John"', ( byColor['blue'].find({ it.name == 'John' })?.name=='John' )
		assertTrue 'Blue should not contain "Harry"',  ( harry == null )

	}


	// @Test
	// Test that ArrayList.groupBy function is working as we hope that it would
	void testAsMapForTestData() {

		def tasks
		def deps
		(tasks, deps) = initTestData()

		def by = deps.asGroup { it.predecessor.id }
		// println "by=${by}"

		assertEquals 'Should have 7 groups', 7, by.size()
		assertTrue 'id "0" should be one of the ids in the map', by.containsKey('0')
		assertEquals 'Group "0" should have 2 nodes', 2, by['0'].size()
		assertEquals 'Group "5" should have 3 nodes', 3, by['5'].size()
		assertTrue 'Group "0" should contain dependency "100"', ( by['0'].find { it.id == 100}?.id == 100 )
		assertTrue 'Group "0" should not contain dependency "14"', ( by['0'].find { it.id == 14 } == null )
	}

	// @Test
	// Test that the processDFS is returning the proper results
	void testProcessDFS() {

		def tasks
		def deps
		(tasks, deps) = initTestData()

		def dfsMap = runbookService.processDFS( tasks, deps )

		assertEquals 'Starts type', 'java.util.ArrayList', dfsMap.starts.getClass().name
		assertEquals 'Sinks type', 'java.util.ArrayList', dfsMap.sinks.getClass().name
		assertEquals 'Cyclicals type', 'java.util.LinkedHashMap', dfsMap.cyclicals.getClass().name

		assertEquals 'Should have 2 Start vertices', 2, dfsMap.starts.size()
		assertEquals 'Should have 3 Sink vertices', 3, dfsMap.sinks.size()
		assertEquals 'Should have 0 Cyclical vertices', 0, dfsMap.cyclicals.size()

		[0,6].each { n -> assertEquals "Starts vertices should contain id $n", n, dfsMap['starts'].find { it.id == n }?.id }
		(7..9).each { n -> assertEquals "Sinks vertices should contain id $n", n, dfsMap['sinks'].find { it.id == n }?.id }
	}

	// @Test
	// Test that the processDurations is returning the proper results
	void testProcessDurations() {

		def tasks
		def deps
		(tasks, deps) = initTestData()

		def dfsMap = runbookService.processDFS( tasks, deps )

		def durMap = runbookService.processDurations( tasks, deps, dfsMap.sinks) 

		dataMatrix.each { i, c, d -> 
			assertEquals "downstreamTaskCount for edge $i", c, durMap.edges[i].tmpDownstreamTaskCount
			assertEquals "pathDuration for edge $i", d, durMap.edges[i].tmpPathDuration
		}

		assertEquals 'Finish tmpMapDepth', 1, durMap.tasks[9].tmpMapDepth 
		assertEquals 'Task 1005 tmpMapDepth', 2, durMap.tasks[5].tmpMapDepth 
		assertEquals 'Task 1003 tmpMapDepth', 3, durMap.tasks[3].tmpMapDepth 
		assertEquals 'Task 1002 tmpMapDepth', 4, durMap.tasks[2].tmpMapDepth 
		assertEquals 'Task 1001 tmpMapDepth', 4, durMap.tasks[1].tmpMapDepth 
		assertEquals 'Task 1000 tmpMapDepth', 5, durMap.tasks[0].tmpMapDepth 

	}
	
	// @Test
	// Test that the processDFS and processDurations properly handle an cicular references within the map
	void testForCyclicalMapping() {

		def tasks
		def deps
		(tasks, deps) = initTestData()

		// Add a cyclical reference
		deps << new TaskDependency(id:666, predecessor:tasks[5], assetComment:tasks[2], type:'SS')

		def dfsMap = runbookService.processDFS( tasks, deps )

		assertEquals 'Should have 2 Start vertices', 2, dfsMap.starts.size()
		assertEquals 'Should have 3 Sink vertices', 3, dfsMap.sinks.size()
		assertEquals 'Should have 1 Cyclical vertex', 1, dfsMap.cyclicals.size()

		def durMap = runbookService.processDurations( tasks, deps, dfsMap.sinks) 

		// println "What tasks does edge 105 have? ${durMap.edges['105'].successor.tmpDownstreamTasks}"
		// Run the same process as before and we shouldn't see any differences
		def m = [
			['103', 2, 21],	// We added the earlier reference
			['109', 1, 45] 	// Shouldn't of changed
		]
		m.each { i, c, d -> 
			assertEquals "downstreamTaskCount for edge $i", c, durMap.edges[i].tmpDownstreamTaskCount
			assertEquals "pathDuration for edge $i", d, durMap.edges[i].tmpPathDuration
		}

	}

	// @Test
	// Test that the determineUniqueGraphs method properly matches up the start vertices with the sink vertices
	void testDetermineUniqueGraphs() {

		def tasks
		def deps
		(tasks, deps) = initTestData()

		def dfsMap = runbookService.processDFS( tasks, deps )
		def durMap = runbookService.processDurations( tasks, deps, dfsMap.sinks) 
		def graphs = runbookService.determineUniqueGraphs(dfsMap.starts, dfsMap.sinks)

		// {starts=[0, 6], sinks=[7, 8, 9], maxPathDuration=73, maxDownstreamTaskCount=8}

		assertEquals 'Returns a list with one group', 1, graphs.size()
		assertEquals 'Starts', [0,6], graphs[0].starts
		assertEquals 'Sinks', [7,8,9], graphs[0].sinks
		assertEquals 'maxPathDuration', 82, graphs[0].maxPathDuration
		assertEquals 'maxDownstreamTaskCount', 8, graphs[0].maxDownstreamTaskCount
	}

	// @Test
	// Test that the determineUniqueGraphs method properly matches up the start vertices with the sink vertices when there are multiple graphs in the vertices
	void testDetermineUniqueGraphsMultiple() {

		def tasks
		def deps
		(tasks, deps) = initTestData()

		// Add a second separate set of tasks
		def ltid = tasks.size() - 1
		tasks << new AssetComment(id:ltid+1, taskNumber: 1050, duration:90, comment:'Separate map Start task') // start vertex
		tasks << new AssetComment(id:ltid+2, taskNumber: 1051, duration:7, comment:'Separate map Middle task')
		tasks << new AssetComment(id:ltid+3, taskNumber: 1052, duration:12, comment:'Separate map Sink task') // start vertex
		deps << new TaskDependency(id:200, predecessor:tasks[ltid+1], assetComment:tasks[ltid+2], type:'SS') 
		deps << new TaskDependency(id:201, predecessor:tasks[ltid+2], assetComment:tasks[ltid+3], type:'SS') 

		// Add an additional starting vector that is shorter so we can see the counts
		tasks << new AssetComment(id:ltid+4, taskNumber: 1060, duration:120, comment:'Change tire on truck')
		deps << new TaskDependency(id:210, predecessor:tasks[ltid+4], assetComment:tasks[5], type:'SS') 

		def dfsMap = runbookService.processDFS( tasks, deps )
		def durMap = runbookService.processDurations( tasks, deps, dfsMap.sinks) 

		def graphs = runbookService.determineUniqueGraphs(dfsMap.starts, dfsMap.sinks)

		// {starts=[0, 6], sinks=[7, 8, 9], maxPathDuration=73, maxDownstreamTaskCount=8}

		assertEquals 'Returns a list with one group', 2, graphs.size()
		assertEquals '1st Starts', [0, 6, ltid+4], graphs[0].starts
		assertEquals '1st Sinks', [7,8,9], graphs[0].sinks
		assertEquals '1st maxPathDuration', 180, graphs[0].maxPathDuration
		assertEquals '1st maxDownstreamTaskCount', 8, graphs[0].maxDownstreamTaskCount

		assertEquals '2nd Starts', [ltid+1], graphs[1].starts
		assertEquals '2nd Sinks', [ltid+3], graphs[1].sinks
		assertEquals '2nd maxPathDuration', 109, graphs[1].maxPathDuration
		assertEquals '2nd maxDownstreamTaskCount', 2, graphs[1].maxDownstreamTaskCount


	}

	// @Test
	// Test that the findCriticalStartTask method properly determines the critical task to start with from the graph list of tasks
	void testFindCriticalStartTask() {

		def tasks
		def deps
		(tasks, deps) = initTestData()

		def dfsMap = runbookService.processDFS( tasks, deps )
		def durMap = runbookService.processDurations( tasks, deps, dfsMap.sinks) 
		def graphs = runbookService.determineUniqueGraphs(dfsMap.starts, dfsMap.sinks)

		def tasksMap = tasks.asMap('id')

		def task = runbookService.findCriticalStartTask(tasksMap, graphs[0])

		assertEquals "Critical start task should be", 6, task.id
	}

	// @Test
	// Test that the findCriticalPath method properly determines the critical path edge based on the longest duration
	void testFindCriticalPath() {

		def tasks
		def deps
		(tasks, deps) = initTestData()

		def dfsMap = runbookService.processDFS( tasks, deps )
		def durMap = runbookService.processDurations( tasks, deps, dfsMap.sinks) 
		def graphs = runbookService.determineUniqueGraphs(dfsMap.starts, dfsMap.sinks)

		def edgesByPred = deps.asGroup { it.predecessor.id }
		def edge = runbookService.findCriticalPath(tasks[6], edgesByPred)

		assertTrue 'Edge should not be null', edge != null
		assertEquals "Critical edge should be", 108, edge.id
	}

	// @Test
	// Test that the computeStartTimes method properly calculates the various estStart, earliest and latest starts for each task in the graph(s)
	void testComputeStartTimes() {

		def tasks
		def deps
		(tasks, deps) = initTestData()

		def dfsMap = runbookService.processDFS( tasks, deps )
		def durMap = runbookService.processDurations( tasks, deps, dfsMap.sinks) 

		def graphs = runbookService.determineUniqueGraphs(dfsMap.starts, dfsMap.sinks)

		def startTime = 0
		def estFinish = runbookService.computeStartTimes(startTime, tasks, deps, dfsMap.starts, dfsMap.sinks, graphs)


		tasks.each { t -> println "Task ${t.taskNumber}/${t.id} duration=${t.duration}, estStart=${t.tmpEstimatedStart}, earliest=${t.tmpEarliestStart}, latest=${t.tmpLatestStart}, CP=${t.tmpCriticalPath}"}

		// id, estStart, earliest, latest, is Critical Path
		def startTimes = [
			[0,  0,  0, 48, false],
			[1,  0,  9, 53, false],
			[2,  9,  9 , 9, true],
			[3, 19, 19, 19, true],
			[4,  0, 17, 61, false],
			[5, 22, 22, 22, true],
			[6,  0,  0, 44, true],	// Start of CP
			[7, 37, 37, 37, true],
			[8,  0, 37, 80, false],
			[9,  0, 37, 81, false],
		]

		assertEquals 'estFinish should be zero', 82, estFinish

		// Check the times and critical path of all tasks
		startTimes.each { id, estStart, earliest, latest, criticalPath ->
			def task = tasks[id] 
			assertEquals "Estimated Start ($id)", estStart, tasks[id].tmpEstimatedStart
			assertEquals "Earliest Start ($id)", earliest, tasks[id].tmpEarliestStart
			assertEquals "Estimated Start ($id)", latest, tasks[id].tmpLatestStart
			assertEquals "Critical Path ($id)", criticalPath, tasks[id].tmpCriticalPath
		}

	}
	
}