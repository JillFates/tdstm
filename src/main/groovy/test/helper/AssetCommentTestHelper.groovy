package test.helper

import net.transitionmanager.task.AssetComment
import net.transitionmanager.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import grails.gorm.transactions.Transactional
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.task.Task
import org.apache.commons.lang3.RandomStringUtils

@Transactional
class AssetCommentTestHelper {

	/**
	 * Create a task based on the given parameters.
	 * @param project
	 * @param moveEvent
	 * @param published
	 * @return the task created.
	 */
	Task createAssetComment(Project project, MoveEvent moveEvent, boolean published = true) {
		Task task = new Task(
				project: project,
				moveEvent: moveEvent,
				comment: 'Test AssetComment-' + RandomStringUtils.randomAlphabetic(10),
				status: AssetCommentStatus.READY,
				duration: 0,
				isPublished: published
		).save(flush: true)
		return task
	}

	/**
	 * Create a task if not exists from given name for E2EProjectSpec to persist at server DB
	 * @param: name
	 * @param: project
	 * @returm the task
	 */
	Task createTask(String taskName, Project project, Person person, MoveEvent moveEvent) {
		Task existingTask = Task.findWhere([comment: taskName, project: project])
		if (!existingTask) {
			Task assetComment = new Task(
					project: project,
					moveEvent: moveEvent,
					createdBy: person,
					comment: taskName,
					status: AssetCommentStatus.READY,
					sendNotification: true
			).save(flush: true)
			return assetComment
		}
		return existingTask
	}

	/**
	 * Creates a comment if not exists associated to an existing asset from given name for E2EProjectSpec
	 * to persist at server DB
	 * @param: list of comments
	 * @param: assetEntity
	 */
	void addCommentsToAsset(List comments, AssetEntity assetEntity, Project project) {
		AssetComment existingComment
		comments?.each{ String comment ->
			existingComment = AssetComment.all.find{
				it.comment.startsWith(comment) && it.assetEntity == assetEntity && it.commentType == AssetCommentType.COMMENT
			}
			if (!existingComment) {
				createComment(assetEntity, project, comment)
			} else {
				existingComment.delete(flush: true)
				createComment(assetEntity, project, comment)
			}
		}
	}

	/**
	 * Creates a comment associated to an asset
	 * @param: assetEntity
	 * @param: comment
	 * @returm the comment
	 */
	AssetComment createComment(AssetEntity assetEntity, Project project, String comment){
		AssetComment assetComment = new AssetComment(
				assetEntity: assetEntity,
				comment: comment,
				status: AssetCommentStatus.READY,
				commentType: AssetCommentType.COMMENT,
				project: project
		).save(flush: true)
		return assetComment
	}
}
