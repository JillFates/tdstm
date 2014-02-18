import org.apache.shiro.SecurityUtils
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

import com.tds.asset.AssetCableMap
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tds.asset.AssetEntity
import com.tdsops.common.sql.SqlUtil
import com.tdssrc.grails.WebUtil

//import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
//import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class ModelService {

    static transactional = true

   	// Services and objects to be injected by IoC
	def sessionFactory
	def assetEntityAttributeLoaderService
	def dataSource
    def jdbcTemplate
	def assetEntityService

   /**
	 * @param fromModel : instance of the model that is being merged
	 * @param toModel : instance of toModel 
	 * @return : updated assetCount
	 */
	def mergeModel(fromModel, toModel){
		//	Revise Asset, and any other records that may point to this model
		def fromModelAssets = AssetEntity.findAllByModel( fromModel )
		def assetUpdated =0 // assetUpdated flag to count the assets updated by merging models .
		
		fromModelAssets.each{ assetEntity->
			assetEntity.model = toModel
			assetEntity.assetType = toModel.assetType
			if(assetEntity.save(flush:true)){
				assetUpdated++
			}
			assetEntityAttributeLoaderService.updateModelConnectors( assetEntity )
		}
		
		// Delete model associated record
		AssetCableMap.executeUpdate("delete AssetCableMap where assetFromPort in (from ModelConnector where model = ${fromModel.id})")
		AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus='${AssetCableStatus.UNKNOWN}',assetTo=null,
												assetToPort=null where assetToPort in (from ModelConnector where model = ${fromModel.id})""")
		ModelConnector.executeUpdate("delete ModelConnector where model = ?",[fromModel])
		
		
		// Coping data from other models into any blank field in the target model.
		def modelDomain = new DefaultGrailsDomainClass( Model.class )
		modelDomain.properties.each{
			def prop = it.name
			// Restricting few fields to be updated e.g. 'id', 'modelName', 'modelConnectors', 'racks' .
			def notToUpdate = ['beforeDelete','beforeInsert', 'beforeUpdate','id', 'modelName', 'modelConnectors', 'racks']
			if(it.isPersistent() && !toModel."${prop}" && !notToUpdate.contains(prop)){
				toModel."${prop}" = fromModel."${prop}"
			}
		}
		if(!toModel.save(flush:true)){
			toModel.errors.allErrors.each{println it}
		}
		
		// Add to the AKA field list in the target record
		def toModelAlias = ModelAlias.findAllByModel(toModel).name
		if(!toModelAlias.contains(fromModel.modelName)){
			def fromModelAlias = ModelAlias.findAllByModel(fromModel)
			ModelAlias.executeUpdate("delete from ModelAlias mo where mo.model = ${fromModel.id}")
			
			fromModelAlias.each{
				toModel.findOrCreateAliasByName(it.name, true)
			}
			//merging fromModel as AKA of toModel
			toModel.findOrCreateAliasByName(fromModel.modelName, true)
			
			// Delete model record
			fromModel.delete()
			sessionFactory.getCurrentSession().flush();
			
			def principal = SecurityUtils.subject?.principal
			if( principal ){
				def user = UserLogin.findByUsername( principal )
				def person = user.person
				def bonusScore = person.modelScoreBonus ? person.modelScoreBonus:0
				if(user){
					person.modelScoreBonus = bonusScore+10
					person.modelScore = person.modelScoreBonus + person.modelScore
					person.save(flush:true)
				}
			}
			/**/
		} else {
			//	Delete model record
			fromModel.delete()
			sessionFactory.getCurrentSession().flush();
		}
		// Return to model list view with the flash message "Merge completed."
		return assetUpdated
	}

	/* Used generate the content used to populate the list view
	 * @param Map filterParams a map of all the aliases the user can filter by, and the value the user has entered in each field
	 * @param String sortColumn a string containing the alias of the field to sort by
	 * @param String sortOrder a string containing the order to sort by. Should be either 'asc' or 'desc'
	 * @return List results the list of rows selected by the query
	 */
	def listOfFilteredModels(filterParams, sortColumn, sortOrder) {
		def instanceList
		def namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource)
		
		// Cut the list of fields to filter by down to only the fields the user has entered text into
		def queryParams = [:] 
		filterParams.each { k, v -> if (v?.trim()) queryParams.put(k,v) }

		// These values are mapped to real columns in the database, so they can be used in the WHERE clause
		def aliasValuesBase = [ 
			'modelName':'m.name', 'manufacturer':'man.name','sourceTDSVersion':'m.sourcetdsversion', 'sourceTDS':'m.sourcetds', 'modelStatus':'m.model_status','modelId':'m.model_id'
		]
		def modelPref= assetEntityService.getExistingPref('Model_Columns')
		def modelPrefVal = modelPref.collect{it.value}
		
		modelPrefVal.each{
			def dbValue = WebUtil.splitCamelCase(it)
			if(!(it in [ 'modelConnectors' , 'createdBy', 'updatedBy', 'validatedBy','modelScope','sourceURL']))
				aliasValuesBase << [(it): ('m.'+dbValue)]
			if(it=='createdBy')
				aliasValuesBase << [(it): ('CONCAT(CONCAT(p.first_name, " "), IFNULL(p.last_name,""))')]
			if(it=='updatedBy')
				aliasValuesBase << [(it): ('CONCAT(CONCAT(p1.first_name, " "), IFNULL(p1.last_name,""))')]
			if(it=='validatedBy')
				aliasValuesBase << [(it): ('CONCAT(CONCAT(p2.first_name, " "), IFNULL(p2.last_name,""))')]
			if(it=='modelScope')
				aliasValuesBase << [(it): ('pr.project_code')]
			if(it=='sourceURL')
				aliasValuesBase << [(it): ('m.sourceurl')]
		}
		// These values are mapped to derived columns, so they will be used in the HAVING clause if included in the filter
		def aliasValuesAggregate = [ 'noOfConnectors':'COUNT(DISTINCT mc.model_connectors_id)', 'assetsCount':'COUNT(DISTINCT ae.asset_entity_id)' ]
		
		// If the user is sorting by a valid column, order by that one instead of the default
		sortColumn = ( sortColumn && filterParams.containsKey(sortColumn) ) ? sortColumn : "man.name, m.name"
		
		def query = new StringBuffer("SELECT ")
		
		// Add all the columns to the query 
		def comma = false
		(aliasValuesBase + aliasValuesAggregate).each {
			query.append("${ comma ?', ':'' }${it.getValue()} AS ${it.getKey()}")
			comma = true
		}
		
		// Perform all the needed table joins
		query.append(""" FROM model m 
			LEFT OUTER JOIN model_connector mc on mc.model_id = m.model_id 
			LEFT OUTER JOIN model_sync ms on ms.model_id = m.model_id 
			LEFT OUTER JOIN manufacturer man on man.manufacturer_id = m.manufacturer_id 
			LEFT OUTER JOIN asset_entity ae ON ae.model_id = m.model_id 
			LEFT OUTER JOIN person p ON p.person_id = m.created_by
			LEFT OUTER JOIN person p1 ON p1.person_id = m.updated_by
			LEFT OUTER JOIN person p2 ON p2.person_id = m.validated_by
			LEFT OUTER JOIN project pr ON pr.project_id = m.model_scope_id""" )
				
		// Handle the filtering by each column's text field for base columns
		def firstWhere = true
		aliasValuesBase.each { k, v ->
			if (queryParams.containsKey(k) ) {
				query.append(" ${firstWhere ? ' WHERE' : ' AND'} ${v} ")

				def aggVal = queryParams[k]
				def expr = 'LIKE'
				(aggVal, expr) = SqlUtil.parseExpression(aggVal, expr)
				if (expr.contains('LIKE')) {
					query.append("$expr CONCAT('%',:${k},'%')")
				} else {
					query.append("$expr :${k}")					
				}
				queryParams[k] = aggVal
				firstWhere = false				
			}
		}

		// Group the models by
		query.append(" GROUP BY modelId ")

		// Handle the filtering by each column's text field for aggregate columns
		def firstHaving = true
		aliasValuesAggregate.each { k, v -> 
			if (queryParams.containsKey(k)) {

				// TODO : refactor the query expression parsing <,> into reusable function as it could be used in a number of places

				// Handle <, >, <= or >= options on the numeric filter
				def aggVal = queryParams[k]
				def expr = '='
				(aggVal, expr) = SqlUtil.parseExpression(aggVal, expr)
				if (aggVal.isNumber()) {
					// Need to save the query param without the expression
					queryParams[k] = aggVal
					query.append(" ${firstHaving ? ' HAVING' : ' AND'} ${v} $expr :${k}")
					firstHaving = false
				}
			}
		}

		// Sort by the specified field
		query.append(" ORDER BY ${sortColumn} ${sortOrder} ")
		
		
		// Perform the query and store the results in a list
		if (queryParams.size() > 0)
			instanceList = namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)
		else
			instanceList = jdbcTemplate.queryForList(query.toString())
		
		return instanceList
	}

}
