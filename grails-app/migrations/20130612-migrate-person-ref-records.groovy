import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH

import com.tds.asset.Application
import com.tds.asset.AssetEntity

/**
 * This Changelog we have to written cause we are getting "getting-lock-wait-timeout-exceeded-try-restarting-transaction". due to 
 * some hibernate issues
 */

def ctx = AH.application.mainContext
def jdbcTemplate = ctx.jdbcTemplate
databaseChangeLog = {
	// This Changeset is used for migrate sme record to sme_id column
	changeSet(author: "lokanada", id: "20130612 TM-1904-10") {
		comment('Add "sme_id" column in Application table')
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
	changeSet(author: "lokanada", id: "20130612 TM-1904-11") {
		comment('Add "sme2_id" column in Application table')
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
	
	changeSet(author: "lokanada", id: "20130611 TM-1904-12") {
		comment('Add "app_owner_id" column in Application table')
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
 * This method we are using to Migrate record for sme , sme2 and appOwner. but we are not creating person record here
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
				
				/*Fetching person which exist in current project's company */
				def personList = Person.findAll("from Person s where s.id in \
								(select p.partyIdTo from PartyRelationship p where p.partyRelationshipType = 'STAFF'\
								 and p.partyIdFrom = $app.clientId and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' )")
				
				/*Searching person in project's company list. if found using this else create it*/						    
				def person = personList.find{it.firstName==firstName && it.lastName==lastName}
				if(person){
					if(domain=="Application")
						jdbcTemplate.execute("update application set ${column} = ${person.id} where app_id= ${app.id}")
					else
						jdbcTemplate.execute("update asset_entity set ${column} = ${person.id} where asset_entity_id= ${app.id}")
				}
		}
}

