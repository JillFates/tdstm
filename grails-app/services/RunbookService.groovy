/**
 * The RunbookService class contains methods useful for management of Runbook process and optimization
 * 
 * @author John Martin
 *
 */

import com.tds.asset.AssetComment
import com.tds.asset.TaskDependency

import com.tdssrc.grails.TimeUtil


class RunbookService {
	
	/** 
	 * Used to load all runbook related tasks associated with an event
	 * @param moveEvent the event to retrieve tasks for
	 * @return List<AssetCommet> a list of tasks
	 */ 
	List getEventTasks(MoveEvent moveEvent) {
		def tasks = []
		if (moveEvent) {
			tasks = AssetComment.findAllByMoveEventAndCategoryInList(moveEvent, AssetComment.moveDayCategories)
		}

		return tasks
	}

	/**
	 * Used to get the list of task dependencies for a given list of tasks
	 * @param List<AssetComment> a list of tasks
	 * @return List<TaskDependency> a list of the dependencies associated to the tasks
	 */ 
	List getTaskDependencies(List tasks) {
		def dependencies = []
		tasks.each { t ->
			t.taskDependencies.each { d -> 
				dependencies << d
			}
		}
		return dependencies
	}

	/**
	 * processDFS performs a Depth First Search front-to-back through the directed graph of tasks in order to build up a number of datapoints 
	 * needed to properly navigate the graph. The process will return a number of arrays that can be then be used to navigate front-to-back
	 * and back-to-front through the graph.
	 * @param List<AssetComment> a list of tasks
	 * @param List<TaskDependency> a list of task dependencies associated with the task list
	 * @return Map array that contains the various data elements
	 *		List sinks 		array of sink vertices (final vertices with no successors)
	 *		List starts  	array of start vertices (vertices with no predecessors)
	 *		Map cyclicals 	map of any found cyclical references where the key is the task id and the value being the stack of tasks in the reference
	 *		Integer elapsed amount of time it took to perform the process
	 * @throws RuntimeException - an exception will be thrown under a few conditions including potential inifinite loops and missing data
	 */
	def processDFS(tasks, dependencies) {

		def e = new Date()

		if (tasks.size() == 0 || dependencies.size() == 0)
			throw new RuntimeException('processDFS() invoked without necessary vertices or edges')

		//log.debug "processDFS() invoked for ${tasks.size()} tasks"

		// a safety valve use to break out of the recursion if we get into infinite loop
		def tick = 0
		def maxRecursions = tasks.size() * 2

		// We need to mark all of the nodes as not having been explored yet so that the DFS will know when it's been before
		tasks.each { 
			it.metaClass.setProperty('tmpBeenExplored', false) 	// Flag to indicate that the task was previously explored
			it.metaClass.setProperty('tmpIsStartVertex', true) 	// Flag that the task is a start vertex
		}

		// Convert the task list into map by their ids
		def nodes = tasks.asMap('id')

		// Get map by node id that contains a list the adjacent edges
		def nodeEdges = dependencies.asGroup { it.predecessor.id }
		def edgesBySucc = dependencies.asGroup { it.successor.id }

		// nodeEdges.each { k,v -> log.debug "processDFS() nodeEdges[$k] = edge.id ${v.id}, edge.pred=${v.predecessor.id}, edge.successor=${v.successor.id}" }

		// We need a stack for the recursive process through the map
		def stack = []

		// This will be used to reference any cyclic maps that we encounter
		def cyclicalMaps = [:]

		// A list of vertexes (vertexes that don't have subsequent nodes/tasks) 
		def sinkVertices = []

		def depth = 0
		def vertexId = 0

		// The closure that we'll use to walk through the list of nodes
		def dfsOfGraph 
		dfsOfGraph = { n ->
			if (++tick > maxRecursions) {
				log.debug "#### Short circuited the recursion as it seems we're in an infinite loop (maxRecursions=$maxRecursions)"
				throw new RuntimeException('processDFS() Exceeded the maximum number of recursions (2x # of tasks)')
			}

			depth++

			def vertex = nodes[n.toString()]

			// Check to see if we have entered into a cyclical map reference
			if (stack.size() > 0 && stack.find { it == n } ) {
				//log.debug "dfsOfGraph() - found cyclical reference(1) with ${vertex}"
				cyclicalMaps[n] = stack.clone()
			}

			// Mark vertices that are know not to be start vertices
			if ( edgesBySucc.containsKey(vertex.id.toString()) ) {
				vertex.tmpIsStartVertex = false
				//log.debug "cleared out tmpIsStartVertex for $n"	
			}			

			// Throw the current node onto the stack
			stack.push(n)

			// Mark the node that we've explored it
			vertex.tmpBeenExplored = true
			vertex.metaClass.setProperty('vertexId', ++vertexId)

			//log.debug "dfsOfGraph() for node ${vertex}, depth $depth, vertexId $vertexId"

			// Find the adject nodes if any
			if (nodeEdges.containsKey(n.toString())) {
				nodeEdges[n.toString()].each { edges ->
					edges.each { edge -> 
						//log.debug "dfsOfGraph() processing edge $edge"
						// Check the node that the edge points to and see if we've already visited it
						if (! nodes[edge.assetComment.id.toString()].tmpBeenExplored) {
							dfsOfGraph(edge.assetComment.id)
						} else {
							//log.debug "dfsOfGraph() Bumped into a previously explore vertex ${nodes[edge.assetComment.id]}"

							// Check to see if this vertex is in the stack and if so, we are in a cyclical reference so save it
							if (stack.find { it == edge.assetComment.id }) {
								//log.debug "dfsOfGraph() found cyclical reference(2) with ${nodes[edge.assetComment.id]}"
								cyclicalMaps[n] = stack.clone()
							}
						}
					}
				}
			} else {
				// We hit a sink vertex so save it 
				if (! sinkVertices.find { it.id == n}) {
					//log.debug "dfsOfGraph()  Found Sink Vertex ${vertex}"
					sinkVertices << vertex
				}
			}

			stack.pop()
			depth--
		}

		// We will start with the first task which typicall is the very first node in the directed map
		def startNode = tasks[0].id

		// This loop will perform DFS into the map until there are no elements remaining in the list
		def loops = 0
		while ( startNode != null ) {
			//log.debug "dfsOfGraph() In while loop $loops"

			dfsOfGraph(startNode)

			// Search for any nodes that haven't been explored yet
			startNode = nodes.find { k,v -> v.tmpBeenExplored == false }?.value
			if (startNode) {
				startNode = startNode.id
			}

			// Make sure we don't blow up because we're stuck in an infinite loop (1,000 if an artifical number we won't exceed)
			if (++loops == 1000 && startNode) {
				//log.debug "** We broke out of the loop prematurely"
				throw new RuntimeException('Exceeded the maximum looping (1,000) set for the processDFS')
			}
		}

		// Find all of the start vertices, those that don't have any successors and stuff them into an array
		def startVertices = []
		nodes.each { k,v -> if (v.tmpIsStartVertex) startVertices << v}

		def elapsed = TimeUtil.elapsed(e)

		log.debug "processDFS() processed moveEvent ${tasks[0].moveEvent} - ${tasks.size()} tasks and ${dependencies.size()} dependencies; \n" + 
			"startVertices=$startVertices \n" +
			"sinkVertices=$sinkVertices \n looped $loops times; recursed $tick times; took ${elapsed} to process"


		return [ 'sinks': sinkVertices, 'starts': startVertices, 'cyclicals': cyclicalMaps, 'elapsed': elapsed ]
	}

	/**
	 * computes the accumulated durations of each edge through the graph to ultimately determine the critical paths
	 * using a breath first search through the directed graph from the end to the front
	 * @param	List<AssetComment>	List of all tasks in the map
	 * @param	List<TaskDependency>	List of the edges in the map
	 * @param	List<AssetComment>	List of start vertices
	 * @param	List<AssetComment>	List of the sink vertices
	 * @return 	?
	 */
	def processDurations( tasks, dependencies, sinks) {

		def e = new Date()
		def msg = ''

		// We need to mark all of the nodes as not having been explored yet so that the walk through the map knows we've been here before
		tasks.each { 
			it.metaClass.setProperty('tmpMaxPathDuration', 0)	// The max pathDuration of all of the tasks successor edges
			it.metaClass.setProperty('tmpDownstreamTasks', [:]) // The number of tasks downstream from the current task
			it.metaClass.setProperty('tmpMapDepth', 0)			// The level/depth up from the furthest out sink vector
			// Todo - check for the existence of tmpBeenExplored and only add if not there before
			it.tmpBeenExplored = false
		}

		dependencies.each {
			it.metaClass.setProperty('tmpPathDuration', 0) 			// Will track the maximum pathDuration to the sink vector(s)
			it.metaClass.setProperty('tmpDownstreamTaskCount', 0) 	// Count of tasks downstream on this edge to the sink vector(s)
			it.metaClass.setProperty('tmpDownstreamTasks', [:]) 	// List of the downstream tasks
		}

		// Convert the lists into maps by their ids
		def taskMap = tasks.asMap('id')
		def edges = dependencies.asMap('id')

		// Get map by task ids of successor and predecessor tasks
		// def edgesByPred = dependencies.groupBy { it.predecessor.id }
		def edgesBySucc = dependencies.asGroup { it.successor.id }


		// Let's walk backwards through the graph to set the durations based on a completely optimal graph with no
		// resource constraints. We'll loop through each of the sink vectors and update the durations appropriately.
		def sinkSize = sinks.size()
		def s=0
		while ( s < sinkSize ) {
			def sink = taskMap[sinks[s].id.toString()]

			// Track the depth into the task
			sink.tmpMapDepth = 1

			// Preload the duration along the current path
			// sink.tmpMaxPathDuration = sink.duration ?: 0
			// This will be used to track the tasks that we walked over for this particular sink vertex
			def walked = []

			// Create a queue that we'll use to push and pull from using FIFO
			java.util.LinkedList queue = new java.util.LinkedList()
			queue.add(sink.id)
			
			// log.debug "sinks[0] tmpBeenExplored? ${sinks[0].tmpBeenExplored} : ${sinks[0]}"

			log.info "processDurations() Processing Sink ${s+1} of ${sinkSize} - $sink"

			// Need to clear out the tmpBeenExplored each time, first time they were injected into the object
			if (s > 0) {
				tasks.each { it.tmpBeenExplored = false }
			}

			def ticks = tasks.size() * 2
			while ( queue.size() > 0 ) {
				// Pull the first task off the front of the queue
				def taskId = queue.poll()
				def task = taskMap[taskId.toString()]

				def tmpMapDepth = task.tmpMapDepth + 1

				// Safety valve so we don't get in an infinite loop
				if (--ticks == 0) {
					log.error "processDurations() exceeded loop count"
					throw new RuntimeException('Exceeded excepted loop count') 
				}

				if (task.tmpBeenExplored) {
					//log.debug "processDurations() Already explored task $task"
					continue
				}

				task.tmpBeenExplored = true

				if (edgesBySucc.containsKey(task.id.toString())) {
					//log.debug "processDurations() $task has ${edgesBySucc[task.id.toString()].size()} pred, tmpMaxPathDuration=${task.tmpMaxPathDuration}"

					// TODO : change duration to look up durationInMinutes
					// def duration = task.durationInMinutes() ?: 0
					def duration = task.duration ?: 0

					edgesBySucc[task.id.toString()].each { ebs -> 

						ebs.each { edge ->

							// Set the time it will take to finish all the remaining tasks along this current path 
							// This will be the max time of any forked path downstream
							edge.tmpPathDuration = task.tmpMaxPathDuration + ( task.duration ?: 0 )

							// Set the predeccessor task's tmpMaxPathDuration to this edge if it is the longest route
							if (edge.tmpPathDuration > edge.predecessor.tmpMaxPathDuration) {
								edge.predecessor.tmpMaxPathDuration = edge.tmpPathDuration
							}

							// Set the predecessor's tmpMapDepth one higher than the successor. If there were multiple paths
							// to a task, we'll use the shortest path
							if (edge.predecessor.tmpMapDepth < tmpMapDepth)
								edge.predecessor.tmpMapDepth = tmpMapDepth

							// Merge the downstream tasks from the current task plus add the current task to the upstream task
							if (task.tmpDownstreamTasks) {
								edge.predecessor.tmpDownstreamTasks << task.tmpDownstreamTasks
							}
							edge.predecessor.tmpDownstreamTasks << [ (task.id):task]

							// Put each predecessor into the queue if it hasn't already been processed
							if (! queue.contains( edge.predecessor.id ))
								queue.add(edge.predecessor.id)							
						}

					}

				}
			}

			// tick our sink iterator
			s++
		}

		// Update all of the edges with the total 
		edges.each { k,v -> v.tmpDownstreamTaskCount = v.successor.tmpDownstreamTasks.size() + 1 }

		def elapsed = TimeUtil.elapsed(e)
		log.debug "processDurations() took $elapsed"

		return [tasks: tasks, 'edges': edges, elapsed: elapsed]

	}

	/**
	 * Used to determine the unique graphs (separate directed graphs) where groups of start vertices share intersecting sink vertices.
	 * For example if start A references sink X,Y, B references Y,Z and C references Z then they are all in  the same group or map.
	 * If D references sink R,Q it would be a separate group/map   
	 * @param List start vertices
	 * @param List sink vertices
	 * @return An array of maps containing grouping of starts and their shared sink vertices along with some other metrics include
	 *    starts - the ids of the start vertices
	 *    sinks - the ids of the sink vertices in the group
	 *    maxPathDuration - the maximum path duration for the group
	 *    maxDownstreamTaskCount - the maximum Downstream Task count for the group
	 */
	List<Map> determineUniqueGraphs(starts, sinks) {
		def sinkIds = sinks*.id 	// List of the sink vertex ids
		def startSinks = [:]		// This will contain the list of sink vertices that are downstream for each start vertex

		// log.debug "findVennSinkVertices() sinkIds has ${sinks.size()} tasks, IDS: $sinkIds"
		starts.each { start ->
			// log.debug "findVennSinkVertices() start $start contains ${start.tmpDownstreamTasks.size()} downstream tasks"
			def idsFound = []
			start.tmpDownstreamTasks.each { k,t -> 
				// log.debug "checking $k for $t id ${t.id} in sinkIds"
				if ( sinkIds.contains(t?.id) )
					idsFound << t.id 
			}
			startSinks.putAt(start.id, idsFound?.sort() )
		}

		// Now create graps that contain the unions of each start vectors' sink vertices where at least one sink vertex is shared
		// between each of the starts. This will basically create a grap for each separate map if there are more than one. We don't 
		// expect there to be more than one but you never know...
		def graps = []
		def processed = []
		def maxDur
		def maxDST
		def grapSinks
		def grapStarts = []

		def x=0
		def s=starts.size()
		while (x < s) {

			log.debug "findVennSinkVertices() Starting outer loop x=$x"
			def startId = starts[x].id

			// Skip over any starts that have already been processed
			if (processed.contains(startId)) {
				x++
				continue
			}

			maxDur = starts[x].tmpMaxPathDuration + starts[x].duration
			maxDST = starts[x].tmpDownstreamTasks.size()
			log.debug "findVennSinkVertices() Initial maxDur=$maxDur, maxDST=$maxDST" 
			// Initialize this set of sink ids that will be in this grap
			grapSinks = startSinks[startId]	

			// Initials this set of start ids that will be in this grap
			grapStarts << startId

			def y=x+1
			while (y < s) {
				def nextId = starts[y].id

				// Skip over any that have already been grapped/processed
				if ( processed.contains(nextId)) {
					y++
					continue
				}
				log.debug "findVennSinkVertices() Starting inner loop y=$y"

				// Lets see if any of this start's sink vertices intersect with the sinks already in this grap
				if (org.apache.commons.collections.CollectionUtils.intersection(grapSinks, startSinks[nextId]).size() > 0) {
					// Add the start and it's sink vertices ids to the two arrays
					grapStarts << nextId
					grapSinks = org.apache.commons.collections.CollectionUtils.union(grapSinks, startSinks[nextId])

					// track that we've processed this start vertex
					processed << nextId
		
					log.debug "findVennSinkVertices() task:${starts[y].taskNumber} maxDur=${starts[y].tmpMaxPathDuration}, maxDST=${starts[y].tmpDownstreamTasks.size()}" 

					// Track the max values for path duration and # of tasks downstream
					def yDur = starts[y].tmpMaxPathDuration + starts[y].duration
					if (maxDur < yDur)
						maxDur = yDur
					if (maxDST < starts[y].tmpDownstreamTasks.size())
						maxDST = starts[y].tmpDownstreamTasks.size()

					// If we are 2 or more beyond x, then we need to start at the top otherwise we can skip to the next 
					y = (y > (x+1)) ? x+1 : y+1
					continue	
				}
				y++
			}
			graps << [starts:grapStarts, sinks:grapSinks, maxPathDuration:maxDur, maxDownstreamTaskCount:maxDST]
			grapStarts = []
			x++
		}

		// Catch the last grap if there was one no added to the grap
		if (grapStarts.size() > 0)
			graps << [starts:grapStarts, sinks:grapSinks, maxPathDuration:maxDur, maxDownstreamTaskCount:maxDST]

		log.debug "findVennSinkVertices() graps= $graps"
		return graps
	}

}