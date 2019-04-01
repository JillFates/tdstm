package net.transitionmanager.service

import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.MoveEventNews
import net.transitionmanager.project.Project

@Transactional
class NewsEditorService {

	SecurityService securityService

	/**
	 * Creates and saves a new MoveEventNews and returns it.
	 *
	 * @param project The project associated with the news.
	 * @param params The Params that represent the news [moveEvent.id, message, resolution, isArchived]
	 *
	 * @return The saved MoveEventNews object.
	 */
	MoveEventNews save(Project project, Long moveEventId, String message, String resolution, Integer isArchived ) {
		MoveEvent moveEvent = GormUtil.findInProject(project, MoveEvent, moveEventId, true)

		// Create the new news domain
		MoveEventNews moveEventNews = new MoveEventNews(moveEvent: moveEvent, createdBy: securityService.loadCurrentPerson())

		return saveUpdateNewsHandler(moveEventNews, message, resolution, isArchived)

	}

	/**
	 * Updates and existing MoveEventNews and returns it.
	 *
	 * @param project The project associated with the news.
	 * @param params The Params that represent the news [id, message, resolution, isArchived]
	 *
	 * @return The updated MoveEventNews object.
	 */
	MoveEventNews update(Project project, Long id, String message, String resolution, Integer isArchived) {
		MoveEventNews moveEventNews = GormUtil.findInProject(project, MoveEventNews, id, true)
		return saveUpdateNewsHandler(moveEventNews, message, resolution, isArchived)
	}

	/**
	 * Used by the saveNews and updateNews methods to perform the actual save of the new or existing MoveEventNews domain record.
	 */
	private MoveEventNews saveUpdateNewsHandler(MoveEventNews moveEventNews, String message, String resolution, Integer isArchived) {

		moveEventNews.message = message
		moveEventNews.resolution = resolution

		if (isArchived ==  1) {
			moveEventNews.isArchived = 1
			moveEventNews.archivedBy = securityService.loadCurrentPerson()
			moveEventNews.dateArchived = TimeUtil.nowGMT()
		} else {
			moveEventNews.isArchived = 0
		}

		moveEventNews.save()


	}

	/**
	 * Deletes a MoveEventNews object.
	 *
	 * @param id The Id of the MoveEventNews to delete.
	 * @param project The project associated with the MoveEventNews.
	 */
	void delete(Long id, Project project) {
		MoveEventNews moveEventNews

		// Check to make sure that the MoveEventNews id exists and is associated to the project
		if (id == null || id < 1) {
			throw new InvalidParamException('Invalid news id specified')
		} else {
			moveEventNews = GormUtil.findInProject(project, MoveEventNews, id, true)
		}

			moveEventNews.delete()
	}
}
