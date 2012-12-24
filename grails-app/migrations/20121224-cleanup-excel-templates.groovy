/**
 * This set of Database change to delete eav_attribute and associated tables data to cleanup excel template
 *  for import and export .
 */

databaseChangeLog = {	
	changeSet(author: "lokanada", id: "20121224 TM-1154.1") {
		comment("""delete eav_attribute and associated tables data where attribute_code in 
					('SourceTeamMt, TargetTeamMt, SourceTeamLog, TargetTeamLog, SourceTeamSa, TargetTeamSa, SourceTeamDba, TargetTeamDba, App Owner, App SME')""")
		sql("""DELETE FROM data_transfer_attribute_map 
							WHERE eav_attribute_id IN ( SELECT attribute_id FROM eav_attribute
												WHERE attribute_code IN ('sourceTeamMt','targetTeamMt','sourceTeamLog','targetTeamLog',
																		 'sourceTeamSa','targetTeamSa','sourceTeamDba','targetTeamDba',
				 														 'appOwner','appSme')
											)"""
			)
		sql("""DELETE FROM data_transfer_value 
							WHERE eav_attribute_id IN ( SELECT attribute_id FROM eav_attribute 
													WHERE attribute_code IN ('sourceTeamMt','targetTeamMt','sourceTeamLog','targetTeamLog',
																			 'sourceTeamSa','targetTeamSa','sourceTeamDba','targetTeamDba',
																			 'appOwner','appSme')
													)"""
			)
		
		sql("""DELETE FROM eav_attribute
							WHERE attribute_code IN ('sourceTeamMt','targetTeamMt','sourceTeamLog','targetTeamLog',
													 'sourceTeamSa','targetTeamSa','sourceTeamDba','targetTeamDba',
													 'appOwner','appSme')"""
			)
	}
}