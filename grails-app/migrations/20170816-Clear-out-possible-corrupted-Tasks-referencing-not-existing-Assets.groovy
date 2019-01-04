import com.tdsops.tm.enums.domain.AssetCommentType

databaseChangeLog = {
	changeSet(author: 'ecantu', id: '20170816 TM-6847-v2') {
		comment('Null out asset references of Tasks and remove any comments associated to deleted assets')

		// Null out Asset references in Tasks that were orphaned
		sql("""
			UPDATE asset_comment
			SET asset_entity_id=null
			WHERE
				comment_type = '${AssetCommentType.TASK}'
				AND asset_entity_id NOT IN (SELECT asset_entity_id FROM asset_entity)
		""")

		// Delete Asset Comments that are orphaned
		sql("""
			DELETE FROM asset_comment
			WHERE
				asset_entity_id IS NOT NULL
				AND comment_type <> '${AssetCommentType.TASK}'
				AND asset_entity_id NOT IN (SELECT asset_entity_id FROM asset_entity)
		""")
	}
}
