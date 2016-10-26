package net.transitionmanager.service

import com.tds.asset.AssetCableMap
import com.tds.asset.AssetEntity
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.WebUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.ModelAlias
import net.transitionmanager.domain.ModelConnector
import net.transitionmanager.domain.UserLogin
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class ModelService implements ServiceMethods {

	private static final List<String> notToUpdate = ['beforeDelete', 'beforeInsert', 'beforeUpdate', 'id',
	                                                 'modelName', 'modelConnectors', 'racks']

	AssetEntityAttributeLoaderService assetEntityAttributeLoaderService
	AssetEntityService assetEntityService
	JdbcTemplate jdbcTemplate
	NamedParameterJdbcTemplate namedParameterJdbcTemplate
	SecurityService securityService

   /**
	 * @param fromModel : instance of the model that is being merged
	 * @param toModel : instance of toModel
	 * @return : updated assetCount
	 */
	@Transactional
	def mergeModel(Model fromModel, Model toModel){
		//	Revise Asset, and any other records that may point to this model
		int assetUpdated = 0 // assetUpdated flag to count the assets updated by merging models .

		for (AssetEntity assetEntity in AssetEntity.findAllByModel(fromModel)) {
			assetEntity.model = toModel
			assetEntity.assetType = toModel.assetType
			if (assetEntity.save()) {
				assetUpdated++
			}
			assetEntityAttributeLoaderService.updateModelConnectors(assetEntity)
		}

		// Delete model associated record
		AssetCableMap.executeUpdate('''
				DELETE AssetCableMap
				WHERE assetFromPort IN (FROM ModelConnector
				                        WHERE model=:fromModel)''',
				[fromModel: fromModel])
		AssetCableMap.executeUpdate('''
				UPDATE AssetCableMap
				SET cableStatus=:status, assetTo=null, assetToPort=null
				WHERE assetToPort IN (FROM ModelConnector
				                      WHERE model=:fromModel)''',
				[status: AssetCableStatus.UNKNOWN, fromModel: fromModel])
		ModelConnector.executeUpdate('DELETE ModelConnector WHERE model=?', [fromModel])

		GormUtil.copyUnsetValues(toModel, fromModel, notToUpdate)
		save toModel

		// Add to the AKA field list in the target record
		def toModelAlias = ModelAlias.findAllByModel(toModel).name
		if (!toModelAlias.contains(fromModel.modelName)){
			def fromModelAlias = ModelAlias.findAllByModel(fromModel)
			ModelAlias.executeUpdate('DELETE ModelAlias WHERE model=?', [fromModel])

			fromModelAlias.each {
				toModel.findOrCreateAliasByName(it.name, true)
			}
			//merging fromModel as AKA of toModel
			toModel.findOrCreateAliasByName(fromModel.modelName, true)

			// Delete model record
			fromModel.delete()

			String principal = securityService.currentUsername
			if (principal) {
				def user = UserLogin.findByUsername(principal)
				def person = user.person
				int bonusScore = person.modelScoreBonus ?: 0
				if (user) {
					person.modelScoreBonus = bonusScore+10
					person.modelScore = person.modelScoreBonus + person.modelScore
					person.save(flush:true)
				}
			}
			/**/
		} else {
			//	Delete model record
			fromModel.delete()
		}
		// Return to model list view with the flash message "Merge completed."
		return assetUpdated
	}

	/* Used generate the content used to populate the list view
	 * @param Map filterParams a map of all the aliases the user can filter by, and the value the user has entered in each field
	 * @param sortColumn  the alias of the field to sort by
	 * @param sortOrder  the order to sort by. Should be either 'asc' or 'desc'
	 * @return List results the list of rows selected by the query
	 */
	def listOfFilteredModels(Map filterParams, String sortColumn, String sortOrder) {

		// Cut the list of fields to filter by down to only the fields the user has entered text into
		def queryParams = [:]
		filterParams.each { k, v -> if (v?.trim()) queryParams[k] = v }

		// These values are mapped to real columns in the database, so they can be used in the WHERE clause
		Map<String, String> aliasValuesBase = [
			modelName: 'm.name', manufacturer: 'man.name',  sourceTDSVersion: 'm.sourcetdsversion',
			sourceTDS: 'm.sourcetds', modelStatus:'m.model_status', modelId: 'm.model_id']
		Map modelPref = assetEntityService.getExistingPref('Model_Columns')
		def modelPrefVal = modelPref.collect { it.value }

		modelPrefVal.each {
			def dbValue = WebUtil.splitCamelCase(it)
			if (!(it in [ 'modelConnectors' , 'createdBy', 'updatedBy', 'validatedBy','modelScope','sourceURL'])) {
				aliasValuesBase[it] = 'm.' + dbValue
			}
			if (it == 'createdBy') {
				aliasValuesBase[it] = 'CONCAT(CONCAT(p.first_name, " "), IFNULL(p.last_name,""))'
			}
			else if (it == 'updatedBy') {
				aliasValuesBase[it] = 'CONCAT(CONCAT(p1.first_name, " "), IFNULL(p1.last_name,""))'
			}
			else if (it == 'validatedBy') {
				aliasValuesBase[it] = 'CONCAT(CONCAT(p2.first_name, " "), IFNULL(p2.last_name,""))'
			}
			else if (it == 'modelScope') {
				aliasValuesBase[it] = 'pr.project_code'
			}
			else if (it == 'sourceURL') {
				aliasValuesBase[it] = 'm.sourceurl'
			}
		}

		// These values are mapped to derived columns, so they will be used in the HAVING clause if included in the filter
		def aliasValuesAggregate = [noOfConnectors: 'COUNT(DISTINCT mc.model_connectors_id)',
		                            assetsCount: 'COUNT(DISTINCT ae.asset_entity_id)']

		// If the user is sorting by a valid column, order by that one instead of the default
		sortColumn = sortColumn && filterParams.containsKey(sortColumn) ? sortColumn : "man.name, m.name"

		def query = new StringBuffer("SELECT ")

		// Add all the columns to the query
		def comma = false
		(aliasValuesBase + aliasValuesAggregate).each {
			query.append("${comma ? ', ' : ''}$it.value AS $it.key")
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
			LEFT OUTER JOIN project pr ON pr.project_id = m.model_scope_id""")

		// Handle the filtering by each column's text field for base columns
		def firstWhere = true
		aliasValuesBase.each { k, v ->
			if (queryParams.containsKey(k)) {
				query.append(" ${firstWhere ? ' WHERE' : ' AND'} $v ")

				def aggVal = queryParams[k]
				def expr = 'LIKE'
				(aggVal, expr) = SqlUtil.parseExpression(aggVal, expr)
				if (expr.contains('LIKE')) {
					query.append("$expr CONCAT('%',:$k,'%')")
				} else {
					query.append("$expr :$k")
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
					query.append(" ${firstHaving ? ' HAVING' : ' AND'} $v $expr :$k")
					firstHaving = false
				}
			}
		}

		query << ' ORDER BY ' << sortColumn << ' ' << sortOrder

		if (queryParams) {
			namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)
		}
		else {
			jdbcTemplate.queryForList(query.toString())
		}
	}
}
