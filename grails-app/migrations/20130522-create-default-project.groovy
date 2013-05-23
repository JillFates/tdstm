import org.codehaus.groovy.grails.commons.ApplicationHolder as AH


databaseChangeLog = {
	changeSet(author: "lokanada", id: "2013052 TM-1895-10") {
	grailsChange {
			change {
				def partyRelationshipService
				def partyType = PartyType.read('COMPANY')
				def partyGroup = PartyGroup.findByName('TM DEFAULT CLIENT')
				if(!partyGroup){
					partyGroup =  new PartyGroup ( name:'TM DEFAULT CLIENT', comment:'Default Client', partyType:partyType )
					if ( ! partyGroup.validate() || ! partyGroup.save(flush:true) ) {
						throw new RuntimeException('Creating partyGroup failed')
					}
				}
				def todayDate = new Date()
				def completionDate 
				def project = Project.findByProjectCode('TM_DEFAULT_PROJECT')
				if(!project){
					project = new Project(
						name:'TM DEFAULT PROJECT', projectCode:'TM_DEFAULT_PROJECT',
						workflowCode:'STD_PROCESS', startDate:todayDate,
						completionDate:todayDate, customFieldsShown:0,
						client: partyGroup
					)
					
					if ( ! project.validate() || ! project.save(flush:true) ) {
						throw new RuntimeException('Creating project failed')				
					}
				}
				def tempPersonId = project.id+1
				[2,3].each{ value->
					sql.execute("update party set party_id = ${tempPersonId} where party_id = ${value}")
					sql.execute("update person set person_id = ${tempPersonId} where person_id = ${value}")
					sql.execute("update party_relationship set party_id_from_id = ${tempPersonId} where party_id_from_id = ${value}")
					sql.execute("update party_relationship set party_id_to_id = ${tempPersonId} where party_id_to_id = ${value}")
					sql.execute("update party_role set party_id = ${tempPersonId} where party_id = ${value}")
					
					//Updating person Reference.
					sql.execute("update asset_comment set resolved_by=${tempPersonId} where resolved_by = ${value}")
					sql.execute("update asset_comment set created_by=${tempPersonId} where created_by = ${value}")
					sql.execute("update asset_comment set assigned_to_id=${tempPersonId} where assigned_to_id = ${value}")
					
					sql.execute("update asset_dependency set updated_by=${tempPersonId} where updated_by = ${value}")
					sql.execute("update asset_dependency set created_by=${tempPersonId} where created_by = ${value}")
					
					sql.execute("update comment_note set created_by_id=${tempPersonId} where created_by_id = ${value}")
					
					sql.execute("update exception_dates set person_id=${tempPersonId} where person_id = ${value}")
					
					sql.execute("update model set updated_by=${tempPersonId} where updated_by = ${value}")
					sql.execute("update model set created_by=${tempPersonId} where created_by = ${value}")
					sql.execute("update model set validated_by=${tempPersonId} where validated_by = ${value}")
					
					sql.execute("update model_sync set updated_by_id=${tempPersonId} where updated_by_id = ${value}")
					sql.execute("update model_sync set created_by_id=${tempPersonId} where created_by_id = ${value}")
					sql.execute("update model_sync set validated_by_id=${tempPersonId} where validated_by_id = ${value}")
					
					sql.execute("update move_event_news set archived_by=${tempPersonId} where archived_by = ${value}")
					sql.execute("update move_event_news set created_by=${tempPersonId} where created_by = ${value}")
					
					sql.execute("update move_event_staff set person_id=${tempPersonId} where person_id = ${value}")
					
					sql.execute("update user_login set person_id=${tempPersonId} where person_id = ${value}")
					
					sql.execute("update workflow set updated_by=${tempPersonId} where updated_by = ${value}")
					
					tempPersonId++
				}
				sql.execute("update party_group set party_group_id = 3 where party_group_id= ${partyGroup.id}")
				sql.execute("update party set party_id = 3 where party_id= ${partyGroup.id}")
				
				sql.execute("update project set project_id = 2, client_id = 3 where project_id= ${project.id}")
				sql.execute("update party_group set party_group_id = 2 where party_group_id= ${project.id}")
				sql.execute("update party set party_id = 2 where party_id= ${project.id}")
				
			}
		}
	}
}