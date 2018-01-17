package test.helper

import com.tds.asset.AssetComment
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import org.apache.commons.lang3.RandomStringUtils

class AssetCommentTestHelper {

	AssetComment createAssetComment(Project project, MoveEvent moveEvent) {
		AssetComment assetComment = new AssetComment(
				project: project,
				moveEvent: moveEvent,
				comment: 'Test AssetComment-' + RandomStringUtils.randomAlphabetic(10),
				status: AssetCommentStatus.READY,
				commentType: AssetCommentType.TASK,
				duration: 0
		).save(flush: true, failOnError: true)
		return assetComment
	}

}
