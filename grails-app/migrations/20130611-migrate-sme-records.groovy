import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH

import com.tds.asset.Application
import com.tds.asset.AssetEntity

/**
 * This Changelog is written to add column for sme, sme2 and app_owner to use person reference. 
 */

def ctx = AH.application.mainContext
def jdbcTemplate = ctx.jdbcTemplate
databaseChangeLog = {
	// This Changeset is used for migrate sme record to sme_id column
	changeSet(author: "lokanada", id: "20130611 TM-1904-11") {
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
				def appList = sql.rows("""SELECT ap.app_id as id, ap.sme as sme, p.client_id as clientId
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
	changeSet(author: "lokanada", id: "20130611 TM-1904-12") {
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
				def appList = sql.rows("""SELECT ap.app_id as id, ap.sme2 as sme2, p.client_id as clientId
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
	
	changeSet(author: "lokanada", id: "20130611 TM-1904-13") {
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
				def appList = sql.rows("""SELECT a.asset_entity_id as id, a.app_owner as appOwner,
								p.client_id as clientId from asset_entity  a left join  project p  
								on a.project_id = p.project_id 
								where app_owner !='' """)
				migrateRecord(appList, 'app_owner_id', 'appOwner', 'assetEntity', jdbcTemplate)
			}
		}
	
	}
}

/**
 * This method we are using to Migrate record for sme , sme2 and appOwner. 
 * @param record : list of application where sme , sme2 and appOwner exist
 * @param column : column is name of column in table (e.g. app_owner)
 * @param prop : property of column in domain
 * @param domain : Domain name
 * @param jdbcTemplate 
 * @return void
 */
def migrateRecord(record, column, prop, domain, jdbcTemplate){
		record.each{app->
			def sme = app."${prop}"?.trim()
				def firstName
				def lastName
				def splittedName
				if(sme.contains(",")){
					splittedName = sme.split(",")
					firstName = splittedName[1].trim()
					lastName = splittedName[0].trim()
				} else if(StringUtils.containsAny(sme, " ")){
					splittedName = sme.split("\\s+")
					firstName = splittedName[0].trim()
					lastName = splittedName[1].trim()
				} else {
					firstName = sme.trim()
				}
				
				def person = Person.findByFirstNameAndLastName(firstName, lastName)
				
				if(!person && firstName){
					person = new Person('firstName':firstName, 'lastName':lastName, 'staffType':'Contractor')
					if(!person.save(insert:true, flush:true)){
						person.errors.allErrors.each {
							 println it
						}
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
					//TODO : while running this block of code getting "getting-lock-wait-timeout-exceeded-try-restarting-transaction"  error,
					//so trying to fix it .
					
					/*if(domain=="Application")
						jdbcTemplate.execute("update application set ${column} = ${personId} where app_id= ${app.id}")
					else
						jdbcTemplate.execute("update asset_entity set ${column} = ${person.id} where asset_entity_id= ${app.id}")
					*/
				}
		}
}

