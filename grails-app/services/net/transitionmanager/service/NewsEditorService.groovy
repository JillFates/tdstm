package net.transitionmanager.service

import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventNews
import net.transitionmanager.domain.Project

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
	MoveEventNews save(Project project, params) {
		MoveEvent moveEvent
		Long moveEventId = NumberUtil.toLong(params['moveEvent.id'])

		// Check to make sure that the moveEvent id exists and is associated to the project
		if (moveEventId == null || moveEventId < 1) {
			throw new InvalidParamException('Invalid move event id specified')
		} else {
			// Check that the move event exists and is associated to the user's project
			moveEvent = MoveEvent.get(moveEventId)

			if (!moveEvent) {
				throw new InvalidParamException('Move event was not found')
			} else if (moveEvent.project.id != project.id) {
				securityService.reportViolation("Accessing move event ($moveEventId) not associated with project ($project.id)")
				throw new InvalidParamException('Invalid move event id specified')
			}
		}

		// Create the new news domain
		MoveEventNews moveEventNews = new MoveEventNews(moveEvent: moveEvent, createdBy: securityService.loadCurrentPerson())

		return saveUpdateNewsHandler(moveEventNews, params)

	}

	/**
	 * Updates and existing MoveEventNews and returns it.
	 *
	 * @param project The project associated with the news.
	 * @param params The Params that represent the news [id, message, resolution, isArchived]
	 *
	 * @return The updated MoveEventNews object.
	 */
	MoveEventNews update(Project project, params) {
		MoveEventNews moveEventNews

		// Check to make sure that the MoveEventNews id exists and is associated to the project
		Long id = NumberUtil.toLong(params['id'])

		if (id == null || id < 1) {
			throw new InvalidParamException('Invalid news id specified')
		} else {
			// Check that the move event news id exists and is associated to the user's project
			moveEventNews = MoveEventNews.get(id)

			if (!moveEventNews) {
				throw new InvalidParamException('News id was not found')
			} else {
				if (moveEventNews.moveEvent.project.id != project.id) {
					securityService.reportViolation("Accessing MoveEventNews ($id) not associated with project ($project.id)")
					throw new InvalidParamException('Invalid news id specified')
				}
			}
		}

		return saveUpdateNewsHandler(moveEventNews, params)
	}

	/**
	 * Used by the saveNews and updateNews methods to perform the actual save of the new or existing MoveEventNews domain record.
	 */
	private MoveEventNews saveUpdateNewsHandler(MoveEventNews moveEventNews, params) {

		moveEventNews.message = params.message
		moveEventNews.resolution = params.resolution

		if (params.isArchived == '1') {
			moveEventNews.isArchived = 1
			moveEventNews.archivedBy = securityService.loadCurrentPerson()
			moveEventNews.dateArchived = TimeUtil.nowGMT()
		} else {
			moveEventNews.isArchived = 0
		}

		moveEventNews.save(failOnError: true)


	}

	/**
	 * Deletes a MoveEventNews object, and returns any errors as a string.
	 *
	 * @param id The Id of the MoveEventNews to delete.
	 * @param project The project associated with the MoveEventNews.
	 *
	 * @return Any errors as a string or ''
	 */
	String delete(Long id, Project project) {
		String error = ''
		MoveEventNews moveEventNews

		// Check to make sure that the MoveEventNews id exists and is associated to the project
		if (id == null || id < 1) {
			error = 'Invalid news id specified'
		} else {
			// Check that the move event news id exists and is associated to the user's project
			moveEventNews = MoveEventNews.get(id)

			if (!moveEventNews) {
				error = 'News id was not found'
			} else {
				if (moveEventNews.moveEvent.project.id != project.id) {
					securityService.reportViolation("Accessing MoveEventNews ($id) not associated with project ($project.id)")
					error = 'Invalid news id specified'
				}
			}
		}

		if (!error) {
			moveEventNews.delete(failOnError: true)
		}

		return error
	}
}
