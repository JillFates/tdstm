/**
 * The TaskService class contains methods useful for working with Task related domain (a.k.a. AssetComment). Eventually we should migrate away from using AssetComment 
 * to persist our task functionality.
 * 
 * @author John Martin
 *
 */

import com.tdsops.tm.enums.domain.RoleTypeGroup
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentCategory

import com.tds.asset.AssetEntity
import com.tds.asset.AssetComment
import com.tds.asset.TaskDependency
import com.tdssrc.grails.GormUtil

class TaskService {

    static transactional = true

	def securityService
	
	/**
	 * This method returns a list of tasks (aka AssetComment objects) based on the parameters that form the criteria
	 */
    def getUserTasks(person, project, search=null, sortOn='c.dueDate', sortOrder='ASC, c.lastUpdated DESC' ) {
		
		// Get the user's roles for the current project
		// TODO : This should (perhaps) pass the project to getPersonRoles
		def roles = securityService.getPersonRoles(person, RoleTypeGroup.STAFF)
		def type=AssetCommentType.TASK
		
		// List of statuses that user should be able to see in when soft assigned to others and user has proper role
		def statuses = [AssetCommentStatus.PLANNED.toString(), AssetCommentStatus.PENDING.toString(), AssetCommentStatus.READY.toString() ]
		
		def sqlParams = [project:project, assignedTo:person, type:type, roles:roles, statuses:statuses]
		
		//log.error "person:${person.id}"
		// Find all Tasks assigned to the person OR assigned to a role that is not hard assigned (to someone else)
		
		search = org.apache.commons.lang.StringUtils.trimToNull(search)

		StringBuffer sql = new StringBuffer('FROM AssetComment AS c')
		if (search) {
			// Join on the AssetEntity and embed the search criteria
			sql.append(', AssetEntity AS a WHERE c.assetEntity.id=a.id AND a.assetTag=:search AND ')
			// Add the search param to the sql params
			sqlParams << [ search:search ]
		} else {
			sql.append(' WHERE ')
		}
		sql.append('c.project=:project AND c.commentType=:type ')
		sql.append('AND ( c.assignedTo=:assignedTo OR ( c.role IN (:roles) AND c.status IN (:statuses) AND c.hardAssigned != 1 ) ) ')

		// TODO : Security : getUserTasks - sortOn/sortOrder should be defined instead of allowing user to INJECT, also shouldn't the column name have the 'a.' prefix?	
		// Add the ORDER to the SQL
//		 sql.append("ORDER BY ${sortOn} ${sortOrder}")
		
		// log.error "SQL for userTasks: " + sql.toString()
		
		def allTasks = AssetComment.findAll( sql.toString(), sqlParams )
		def todoTasks = allTasks.findAll { [AssetCommentStatus.READY, AssetCommentStatus.STARTED].contains(it.status) }

		return ['all':allTasks, 'todo':todoTasks]
    }
	
	/**
	 * Used to set the status of a task, which will perform additional updated based on the state
	 * @param task
	 * @param status
	 * @return
	 */
	def setTaskStatus( task, status) {
		
		// If the current task.status or the persisted value equals the new status, then there's nutt'n to do.
		if (task.status == status || task.getPersistentValue('status') == status) {
			return
		}
		
		task.status = status

		// Now for certain statuses we'll update some other properties to boot
		
		if ([AssetCommentStatus.STARTED, AssetCommentStatus.DONE, AssetCommentStatus.TERMINATED].contains(status)) {
	 		// Get the current user's person so we can assign it appropriately
			def userPerson = securityService.getUserLoginPerson()
	
			def now =  GormUtil.convertInToGMT( "now", "EDT" )
					
			switch (status) {
				case AssetCommentStatus.STARTED:
					task.assignedTo = userPerson
					task.actStart = now
					break
					
				case AssetCommentStatus.DONE:
					taskStatusChangeEvent(task)
				case AssetCommentStatus.TERMINATED:
					task.resolvedBy = userPerson
					task.actFinish = now
					break
			}
		}
	}
 	
	/**
	 * This is invoked by the AssetComment.beforeUpdate method in order to handle any status changes
	 * that may result in the updating of other tasks successor tasks.
	 */
	def taskStatusChangeEvent( task ) {
		log.info "taskStatusChangeEvent: processing task(${task.id}): ${task}"
		def status = task.status
		
		// Let's bail out if the status isn't dirty or the status isn't STARTED or DONE
		if ( ! task.isDirty('status') ||
			task.getPersistentValue('status') == status || 
			! [AssetCommentStatus.STARTED, AssetCommentStatus.DONE].contains(status) ) {
			return
		}
		
		// Okay so we want to handle the STARTED and DONE states and update accordingly.  We are going
		// to assume that the validation logic prevented the user from changing the status if there were
		// any incomplete predecessors.
			
		// TODO: taskStatusChangeEvent : Add logic to handle READY for the SS predecessor type and correct the current code to not assume SF type
	
		//
		// Now mark any successors as Ready if all of the successors' predecessors are DONE
		//
		if ( status ==  AssetCommentStatus.DONE ) {
			def successorDependents = TaskDependency.findAllByPredecessor(task)
			log.info "taskStatusChangeEvent: Found ${successorDependents ? successorDependents.size() : '0'} successors for task ${task.id}"
			successorDependents?.each() { succDepend ->
				def dependentTask = succDepend.assetComment
				log.info "taskStatusChangeEvent: Processing dependentTask(${dependentTask.id}): ${dependentTask}"
				
				// If the Task is in the Planned or Pending state, we can check to see if it makes sense to set to READY
				if ([AssetCommentStatus.PLANNED, AssetCommentStatus.PENDING].contains(dependentTask.status)) {
					
					// Find all predecessor/ tasks for the successor, other than the current task, and make sure they are completed
					def predDependencies = TaskDependency.findByAssetCommentAndPredecessorNot(dependentTask, task)
					def makeReady=true
					log.info "taskStatusChangeEvent: dependentTask(${dependentTask.id}) predecessors> ${predDependencies}"
					predDependencies?.each() { predDependency ->
						def predTask = predDependency.assetComment
						// TODO : runbook : taskStatusChangeEvent - add the dependency type (SS,FS) logic here
						if (predTask.status != AssetCommentStatus.COMPLETED) { 
							makeReady = false
						}
					}
					if (makeReady) {
						/*
						def taskToReady = AssetComment.get(dependentTask.id)
						setTaskStatus(taskToReady, AssetCommentStatus.READY)
						if ( ! taskToReady.validate() ) {
							log.error "taskStatusChangeEvent: Failed Readying successor task [${taskToReady.id} " + GormUtil.allErrorsString(taskToReady)
						}
//							if ( taskToReady.save(flush:true) ) {
						if ( taskToReady.save() ) {
							log.info "taskStatusChangeEvent: dependentTask(${taskToReady.id}) Saved"
							return
						}
						if (! (  dependentTask.validate() && dependentTask.save(flush:true) ) ) {
							log.error "Failed Readying successor task [${taskToReady.id} " + GormUtil.allErrorsString(taskToReady)
						}
						*/
							
							
						log.info "taskStatusChangeEvent: dependentTask(${dependentTask.id}) Making READY "
						setTaskStatus(dependentTask, AssetCommentStatus.READY)
						// log.info "taskStatusChangeEvent: dependentTask(${dependentTask.id}) Making READY - Successful"
						if ( ! dependentTask.validate() ) {
							log.error "taskStatusChangeEvent: Failed Readying successor task [${dependentTask.id} " + GormUtil.allErrorsString(dependentTask)
						}
						
						// log.info "taskStatusChangeEvent: dependentTask(${dependentTask.id}) Made it by validate() "
						
						/*
						 * OK - For anybody looking at this logic and questioning it, let me first say I'm confused too.  For some 
						 * reason we do not need to invoke the save method here on the successor tasks that are being updated.  Some how
						 * GORM is handling it auto-magically. The code originally was doing the save but would get a strange error:
						 * 		[http-8080-1] ERROR errors.GrailsExceptionResolver  - Index: 1, Size: 0
						 * 		java.lang.IndexOutOfBoundsException: Index: 1, Size: 0
						 * 		at java.util.ArrayList.RangeCheck(ArrayList.java:547)
						 * I therefore have commented it out.
						 * John 8/2012
						 */
						if ( dependentTask.save(flush:true) ) {
							log.info "taskStatusChangeEvent: dependentTask(${dependentTask.id}) Saved"
							return
						} else {
						//if (! (  dependentTask.validate() && dependentTask.save(flush:true) ) ) {
							log.error "Failed Readying successor task [${dependentTask.id} " + GormUtil.allErrorsString(dependentTask)
							
							 throw new TaskCompletionException("Unable to READY task ${dependentTask} due to " +
								 GormUtil.allErrorsString(dependentTask) )
						}
					}
				} else {
					log.warn "taskStatusChangeEvent: Found dependentTask(${dependentTask.id}): ${dependentTask} at unexpected status (${dependentTask.status}"
				} 
			} // succDependencies?.each()
		}
	}


	/**
	 * Used to determine the CSS class name that should be used when presenting a task, which is based on the task's status	
	 * @param status
	 * @return String The appropriate CSS style or task_na if the status is invalid
	 */
	def getCssClassForStatus( status ) {
		def css = 'task_na'
		if (AssetCommentStatus.getList().contains(status)) {
			css = "task_${status.toLowerCase()}"
		}
		// log.error "getCssClassForStatus('${status})=${css}"
		return css 
	}

	/**
	 * Returns the HTML for a SELECT control that contains list of tasks based on a TaskDependency
	 * @param task
	 * @return
	 */
	def genSelectForTaskDependency(taskDependency, project=null, idPrefix='taskDependencyEditId', name='taskDependencyEdit') {
		def predecessor = taskDependency.predecessor
		def category = predecessor.category
		
		if (project==null) 
			project = securityService.getUserCurrentProject()
		def projectId = project.id
		
		def selectControl = new StringBuffer("""<select id="${idPrefix}_${taskDependency.id}" name="${name}">""")
		String queryForPredecessor = "FROM AssetComment a WHERE a.project=${projectId} AND a.category='${category}' AND a.commentType='${AssetCommentType.TASK}' ORDER BY a.taskNumber DESC"
		def predecessors = AssetComment.findAll(queryForPredecessor)
		predecessors.each{
			def selected = it.id == predecessor.id ?  'selected="selected"' : ''
			selectControl.append("<option value='${it.id}' ${selected}>${it.toString()}</option>")
		}
		selectControl.append("</select>")
		return selectControl
	}
	
	/**
	 * Returns Boolean value to warn status is overriding or not
	 * @param AssetComment as task
	 * @return statusWarn as can
	 */
	def canChangeStatus ( task ){
		def can = true
		if ([AssetCommentCategory.SHUTDOWN, AssetCommentCategory.PHYSICAL, AssetCommentCategory.STARTUP].contains( task.category ) && 
			! [ AssetCommentStatus.READY, AssetCommentStatus.STARTED ].contains( task.status )) {
			  can = false 
		}
		return can
	}
	
}
