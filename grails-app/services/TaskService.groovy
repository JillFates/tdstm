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

import com.tds.asset.AssetComment
  
class TaskService {

    static transactional = true

	def securityService
	
	/**
	 * This method returns a list of tasks (aka AssetComment objects) based on the parameters that form the criteria
	 */
    def getUserTasks(person, project, search=null, sortOn='a.dueDate', sortOrder='ASC, a.lastUpdated DESC' ) {
		
		// Get the user's roles for the current project
		// TODO : This should (perhaps) pass the project to getPersonRoles
		def roles = securityService.getPersonRoles(person, RoleTypeGroup.STAFF)
		def type=AssetCommentType.TASK
		def statuses = [AssetCommentStatus.PLANNED.toString(), AssetCommentStatus.PENDING.toString(), AssetCommentStatus.READY.toString() ]
		def sqlParams = [project:project, assignedTo:person, type:type, roles:roles, statuses:statuses]

		// Find all Tasks assigned to the person OR assigned to a role that is not hard assigned (to someone else)
		StringBuffer sql = new StringBuffer("""FROM AssetComment a WHERE a.project=:project AND a.commentType=:type
			AND ( a.assignedTo=:assignedTo OR ( a.role IN (:roles) AND a.status IN (:statuses) AND a.hardAssigned!=1 ) )""")

		// Add the search to the query if specified		
		if (search) {
			sqlParams << [ search:search ]
			sql.append(' AND a.assetTag=:search')
		}

		// TODO : Security : getUserTasks - sortOn/sortOrder should be defined instead of allowing user to INJECT		
		// Add the ORDER to the SQL
		sql.append(" ORDER BY ${sortOn} ${sortOrder}")
		
		// log.error "SQL for userTasks: " + sql.toString()
		
		def allTasks = AssetComment.findAll( sql.toString(), sqlParams )
		def todoTasks = allTasks.findAll { [AssetCommentStatus.READY, AssetCommentStatus.STARTED].contains(it.status) }

		return ['all':allTasks, 'todo':todoTasks]
    }

	/**
	 * Used to determine the CSS class name that should be used when presenting a task, which is based on the task's status	
	 * @param status
	 * @return String The appropriate CSS style or task_na if the status is invalid
	 */
	def getCssClassForStatus( status ) {
		if (AssetCommentStatus.getList().contains(status)) {
			return "task_${status.toLowerCase()}"
		} else {	
			return 'task_na'
		}
	}

	/**
	 * Returns the HTML for a SELECT control that contains list of tasks based on a TaskDependency
	 * @param task
	 * @return
	 */
	def genSelectForTaskDependency(taskDependency, project=null, idPrefix='taskDependencyEditId', name='taskDependencySave') {
		def predecessor = taskDependency.predecessor
		def category = predecessor.category
		
		if (project==null) 
			project = securityService.getUserCurrentProject()
		def projectId = project.id
		
		def selectControl = new StringBuffer("""<select id="${idPrefix}_${taskDependency.id}" name="${name}">""")
		String queryForPredecessor = "FROM AssetComment a WHERE a.project=${projectId} AND a.category='${category}' AND a.commentType='${AssetCommentType.TASK}' "
		def predecessors = AssetComment.findAll(queryForPredecessor)
		predecessors.each{
			def selected = it.id == predecessor.id ?  'selected="selected"' : ''
			selectControl.append("<option value='${it.id}' ${selected}>${it.toString()}</option>")
		}
		selectControl.append("</select>")
		return selectControl
	}
	
}
