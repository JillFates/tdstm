package test.helper

import com.tds.asset.AssetComment
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import org.apache.commons.lang3.RandomStringUtils

class AssetCommentTestHelper {

	/**
	 * Create a task based on the given parameters.
	 * @param project
	 * @param moveEvent
	 * @param published
	 * @return the task created.
	 */
	AssetComment createAssetComment(Project project, MoveEvent moveEvent, boolean published = true) {
		AssetComment assetComment = new AssetComment(
				project: project,
				moveEvent: moveEvent,
				comment: 'Test AssetComment-' + RandomStringUtils.randomAlphabetic(10),
				status: AssetCommentStatus.READY,
				commentType: AssetCommentType.TASK,
				duration: 0,
				isPublished: published
		).save(flush: true, failOnError: true)
		return assetComment
	}

}
