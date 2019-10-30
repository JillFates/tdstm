package test.helper

import com.tdsops.tm.enums.domain.AssetCommentType
import net.transitionmanager.project.Project
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskDependency
import org.apache.commons.lang3.RandomStringUtils

class TaskTestHelper {
	AssetComment createTask (String taskComment, Project project) {
		String comment = taskComment ?: RandomStringUtils.randomAlphabetic(10)
		def task = new AssetComment()
		task.with {
			comment = comment
			commentType = AssetCommentType.TASK
			project = project
		}
		task.save(flush: true)
		return task
	}
	
	TaskDependency createDependency (AssetComment task1, AssetComment task2) {
		def dependency = new TaskDependency()
		dependency.with {
			assetComment = task1
			predecessor = task2
		}
		dependency.save(flush: true)
		return dependency
	}
}