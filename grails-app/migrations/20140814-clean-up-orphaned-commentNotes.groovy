/**
 * This migration script Deletes orphaned commentNode records.
 */

databaseChangeLog = {
	changeSet(author: "jdanahy", id: "20140814 TM-2884-1") {
		comment('Deletes orphaned commentNode records.')
		sql('DELETE n FROM comment_note AS n LEFT OUTER JOIN asset_comment c ON c.asset_comment_id = n.asset_comment_id WHERE c.asset_comment_id IS NULL;')
	}
}
