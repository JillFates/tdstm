import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH

import com.tdssrc.grails.GormUtil

/**
 * This Changelog is written to add column for sme, sme2 and app_owner to use person reference. 
 */

def ctx = AH.application.mainContext
def jdbcTemplate = ctx.jdbcTemplate
databaseChangeLog = {
	// This Changeset is used for migrate sme record to sme_id column
	changeSet(author: "lokanada", id: "20130611 TM-1904-7") {
		comment('Add "sme_id" column in Application table')
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'application', columnName:'sme_id' )
			}
	    }
		sql("""ALTER TABLE `application` ADD COLUMN `sme_id` BIGINT(20),
				ADD CONSTRAINT `FK_APPLICATION_SME_ID` FOREIGN KEY `FK_APPLICATION_SME_ID` (`sme_id`)
				REFERENCES `person` (`person_id`)
				ON DELETE RESTRICT
				ON UPDATE RESTRICT
		""")
		
		grailsChange {
			change { 
				def appList = sql.rows("""SELECT ap.app_id as id, ap.sme as sme, p.client_id as clientId, p.project_code as projCode
										  from application ap
										  left join  asset_entity ae on ap.app_id = ae.asset_entity_id
										  left join  project p on ae.project_id = p.project_id
										  where (sme !='' or sme is not null) 
							 """)
				migrateRecord(appList, 'sme_id','sme', 'Application' , jdbcTemplate)
				
			}
		}
	
	}
	
	// This Changeset is used for migrate sme2 record to sme2_id column
	changeSet(author: "lokanada", id: "20130611 TM-1904-8") {
		comment('Add "sme2_id" column in Application table')
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'application', columnName:'sme2_id' )
			}
		}
		sql("""ALTER TABLE `application` ADD COLUMN `sme2_id` BIGINT(20),
				ADD CONSTRAINT `FK_APPLICATION_SME2_ID` FOREIGN KEY `FK_APPLICATION_SME2_ID` (`sme2_id`)
				REFERENCES `person` (`person_id`)
				ON DELETE RESTRICT
				ON UPDATE RESTRICT
		""")
		
		grailsChange {
			change {
				def appList = sql.rows("""SELECT ap.app_id as id, ap.sme2 as sme2, p.client_id as clientId, p.project_code as projCode
										  from application ap
										  left join  asset_entity ae on ap.app_id = ae.asset_entity_id
										  left join  project p on ae.project_id = p.project_id
										  where (sme2 !='' or sme2 is not null) 
							 """)
				migrateRecord(appList, 'sme2_id', 'sme2', 'Application' , jdbcTemplate)
			}
		}
	
	}
	
	// This Changeset is used for migrate app_owner record to app_owner_id column
	
	changeSet(author: "lokanada", id: "20130611 TM-1904-9") {
		comment('Add "app_owner_id" column in Application table')
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'asset_entity', columnName:'app_owner_id' )
			}
		}
		sql("""ALTER TABLE `asset_entity` ADD COLUMN `app_owner_id` BIGINT(20),
				ADD CONSTRAINT `FK_ASSET_ENTITY_app_owner_id` FOREIGN KEY `FK_ASSET_ENTITY_app_owner_id` (`app_owner_id`)
				REFERENCES `person` (`person_id`)
				ON DELETE RESTRICT
				ON UPDATE RESTRICT
		""")
		
		grailsChange {
			change {
				def appList = sql.rows("""SELECT a.asset_entity_id as id, a.app_owner as appOwner, p.project_code as projCode,
								p.client_id as clientId from asset_entity  a left join  project p  
								on a.project_id = p.project_id 
								where app_owner !='' """)
				migrateRecord(appList, 'app_owner_id', 'appOwner', 'assetEntity', jdbcTemplate)
			}
		}
	
	}
}

/**
 * This method we are using to Migrate record for sme , sme2 and appOwner. but we are not migrating data here so here we are 
 * creating person only if person does not exist.
 * @param record : list of application where sme , sme2 and appOwner exist
 * @param column : column is name of column in table (e.g. app_owner)
 * @param prop : property of column in domain
 * @param domain : Domain name
 * @param jdbcTemplate 
 * @return void
 */
def migrateRecord(record, column, prop, domain, jdbcTemplate){
		record.each{app->
			def name = app."${prop}"?.trim() // fullname which is there in sme , sme2 and appOwner fields .
			def firstName
			def lastName
			
			/* If ',' exist in full name assuming pair as lastName,FirstName else assuming as FirstName LastName else
				assuming as firstName only. 
			*/			
			if(name.contains(",")){
				def splittedName = name.split(",")
				firstName = splittedName[1].trim()
				lastName = splittedName[0].trim()
			} else if(StringUtils.containsAny(name, " ")){
				def splittedName = name.split("\\s+")
				firstName = splittedName[0].trim()
				lastName = splittedName[1].trim()
			} else {
				firstName = name.trim()
			}
			
			/*Fetching all person which exist in current project's company */
			def personList = Person.findAll("from Person s where s.id in \
							(select p.partyIdTo from PartyRelationship p where p.partyRelationshipType = 'STAFF'\
							 and p.partyIdFrom = $app.clientId and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' )")
			
			/*Searching person in project's company list. if found using found person else create a new person*/						    
			def person = personList.find{it.firstName==firstName && it.lastName==lastName}
			
			if(!person && firstName){
				/*If person does not exist we are creating person with firstName and lastName and with current asset's project's comapny*/
				
				person = new Person('firstName':firstName, 'lastName':lastName, 'staffType':'Contractor')
				if(!person.save(insert:true, flush:true)){
					throw new RuntimeException('migrateRecord Unable to create Person'+GormUtil.allErrorsString( person ))
				}
				def partyRelationshipType = PartyRelationshipType.findById( "STAFF" )
				def roleTypeFrom = RoleType.findById( "COMPANY" )
				def roleTypeTo = RoleType.findById( "STAFF" )
				
				def partyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType,
					'partyIdFrom.id' :app.clientId, roleTypeCodeFrom:roleTypeFrom, partyIdTo:person,
					roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" )
				.save( insert:true, flush:true )
			}
			
			if(person){
				/*
				 * TODO : while running this block of code getting "getting-lock-wait-timeout-exceeded-try-restarting-transaction"  error,
				 * so used another migration script '20130612-migrate-person-ref-records.groovy' to migrate person records.
				 */	
							
				/*	if(domain=="Application")
						jdbcTemplate.execute("update application set ${column} = ${person.id} where app_id= ${app.id}")
					else
						jdbcTemplate.execute("update asset_entity set ${column} = ${person.id} where asset_entity_id= ${app.id}")
				*/
				
			}
		}
}

