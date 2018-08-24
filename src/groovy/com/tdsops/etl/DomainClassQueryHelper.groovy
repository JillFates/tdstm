package com.tdsops.etl

import com.tds.asset.AssetEntity
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project

import java.util.Map.Entry

/**
 * This class helps in the ETL processor to prepare the correct query for find command.
 * For example it transform:
 *
 * <pre>
 *     find Application by 'id' with SOURCE.'application id' into 'id'
 * </pre>
 *
 * In to this HQL query:
 *  <pre>
 *      select D.id
 *          from AssetEntity D
 *          where  D.project = :project
 *            and D.id = : id
 *  </pre>
 */
class DomainClassQueryHelper {

	/**
	 * Static variable to define alias in every hql query used in this class
	 */
	static final String DOMAIN_ALIAS = 'D'

	/**
	 * Executes the HQL query related to the domain defined.
	 * @param domain an instance of ETLDomain used to defined the correct HQL query to be executed
	 * @param project a project instance used as a param in the HQL query
	 * @param paramsMap a map with params to be used in the HQL query
	 * @param returnIdOnly a flag to control if the method returns the result IDs (true - default) or full domain objects (false)
	 * @return a list of assets returned by an HQL query
	 */
	static List where(ETLDomain domain, Project project, Map<String, ?> paramsMap, Boolean returnIdOnly=true) {

		List<FindCondition> conditions = paramsMap.collect { Entry entry ->
			new FindCondition(entry.key, entry.value)
		}
		if(domain.isAsset()){
			return assetEntities(domain.clazz, project, conditions, returnIdOnly)
		} else {
			return nonAssetEntities(domain.clazz, project, conditions, returnIdOnly)
		}
	}

	/**
	 * Executes the HQL query related to the domain defined.
	 * @param domain an instance of ETLDomain used to defined the correct HQL query to be executed
	 * @param project a project instance used as a param in the HQL query
	 * @param conditions a list of {@code FindCondition} to be used in the HQL query
	 * @param returnIdOnly a flag to control if the method returns the result IDs (true - default) or full domain objects (false)
	 * @return a list of assets returned by an HQL query
	 */
	static List where(ETLDomain domain, Project project, List<FindCondition> conditions, Boolean returnIdOnly=true) {
		if(domain.isAsset()){
			return assetEntities(domain.clazz, project, conditions, returnIdOnly)
		} else {
			return nonAssetEntities(domain.clazz, project, conditions, returnIdOnly)
		}
	}

	/**
	 * Executes an HQL query looking for those all assets referenced by params.
	 * @param project a project instance used as a param in the HQL query
	 * @param conditions a list of {@code FindCondition}
	 * @param returnIdOnly a flag to control if the method returns the result IDs (true - default) or full domain objects (false)
	 * @return a list of assets returned by an HQL query
	 */
	static List assetEntities(Class<? extends AssetEntity> clazz, Project project, List<FindCondition> conditions, Boolean returnIdOnly=true) {

		def (hqlWhere, hqlParams) = hqlWhereAndHqlParams(project, clazz, conditions)
		String hqlJoins = hqlJoins(clazz, conditions)

		String hql = """
            select ${DOMAIN_ALIAS}${returnIdOnly ? '.id' : ''}
              from AssetEntity $DOMAIN_ALIAS
				   $hqlJoins
			 where ${DOMAIN_ALIAS}.project = :project
               and ${DOMAIN_ALIAS}.assetClass = :assetClass
			   and $hqlWhere
		""".stripIndent()

		Map args = [project: project, assetClass: AssetClass.lookup(clazz)] + hqlParams
		return AssetEntity.executeQuery(hql, args, [readOnly: true])
	}

	/**
	 * Executes an HQL query looking for those all non assets referenced by params.
	 * @param clazz class used to execute the HQL sentence
	 * @param project a project instance used as a param in the HQL query
	 * @param conditions a list of {@code FindCondition}
	 * @param returnIdOnly a flag to control if the method returns the result IDs (true - default) or full domain objects (false)
	 * @return a list of assets returned by an HQL query
	 */
	static <T> List nonAssetEntities(Class<T> clazz, Project project, List<FindCondition> conditions, Boolean returnIdOnly=true) {

		def (hqlWhere, hqlParams) = hqlWhereAndHqlParams(project, clazz, conditions)
		String hqlJoins = hqlJoins(clazz, conditions)

		if(GormUtil.isDomainProperty(clazz, 'project')){
			hqlWhere += " and ${DOMAIN_ALIAS}.project = :project \n".toString()
			hqlParams += [project: project]
		}

		String hql = """
			  select ${DOMAIN_ALIAS}${returnIdOnly ? '.id' : ''}
              from ${clazz.simpleName} $DOMAIN_ALIAS
			       $hqlJoins
             where $hqlWhere
		""".stripIndent()

		return clazz.executeQuery(hql,  hqlParams, [readOnly: true])
	}

	/**
	 * Get the named parameter for an HQL query based on a field name
	 * It uses clazz parameter to detect several scenarios. <br>
	 * First of all, it validates if fieldName is an alternativeKey.
	 * See details more details about those fields on otherAlternateKeys map.<br>
	 * After that step, it validates if AssetEntity.isAssignableFrom(clazz)
	 * and if field name is a reference property in domain class<br>
	 * If that is true, then It needs to check if field name is a Person domain reference
	 * or if it clazz has an alternateKey. Having an alternateKey, it is used
	 * for domain references.
	 * <pre>
	 *  assert DomainClassQueryHelper.getNamedParameterForField(ETLDomain.Device.getClazz(), 'id') == 'id'
	 *  assert DomainClassQueryHelper.getNamedParameterForField(ETLDomain.Device.getClazz(), 'manufacturer') == 'manufacturer_name'
	 *  assert DomainClassQueryHelper.getNamedParameterForField(ETLDomain.Device.getClazz(), 'moveBundle') == 'moveBundle_name'
	 *  assert DomainClassQueryHelper.getNamedParameterForField(ETLDomain.Device.getClazz(), 'rackSource') == 'rackSource_tag'
	 *  assert DomainClassQueryHelper.getNamedParameterForField(ETLDomain.Device.getClazz(), 'locationSource') == 'roomSource_location'
	 *
	 *  assert DomainClassQueryHelper.getNamedParameterForField(ETLDomain.Room.getClazz(), 'roomName') == 'roomName'
	 *  assert DomainClassQueryHelper.getNamedParameterForField(ETLDomain.Rack.getClazz(), 'room') == 'room'
	 *  assert DomainClassQueryHelper.getNamedParameterForField(ETLDomain.Dependency.getClazz(), 'asset') == 'asset'
	 * </pre>
	 * @param clazz a domain class instance
	 * @param fieldName a String with a field name used to calculate named parameter
	 * @return a named parameter value for a HQL query.
	 * @see GormUtil#isReferenceProperty(java.lang.Class, java.lang.String)
	 * @see GormUtil#getAlternateKeyPropertyName(java.lang.Class)
	 */
	static String getNamedParameterForField(Class clazz, String fieldName) {

		if(otherAlternateKeys.containsKey(fieldName)){
			return otherAlternateKeys[fieldName].namedParameter
		}

		if(AssetEntity.isAssignableFrom(clazz) && GormUtil.isReferenceProperty(clazz, fieldName)){
			Class propertyClazz = GormUtil.getDomainClassOfProperty(clazz, fieldName)

			if(Person.isAssignableFrom(propertyClazz)){
				return fieldName
			} else {
				String alternateKey = GormUtil.getAlternateKeyPropertyName(propertyClazz)
				return "${fieldName}_${alternateKey}"
			}
		} else {
			return fieldName
		}
	}

	/**
	 * Get a property name the hql where sentence.
	 * First of all it checks if there is an option alternateKey in otherAlternateKeys map.
	 * If not, it needs to detect if fieldName belongs to a property for AssetEntity hierarchy
	 * (Device, Application, Database and Storage). <br>
	 * <pre>
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Device.getClazz(), 'id') == 'D.id'
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Device.getClazz(), 'manufacturer') == 'D.manufacturer.name'
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Device.getClazz(), 'moveBundle') == 'D.moveBundle.name'
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Device.getClazz(), 'rackSource') == 'D.rackSource.tag'
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Device.getClazz(), 'locationSource') == 'D.roomSource.location'
	 *
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Room.getClazz(), 'roomName') == 'D.roomName'
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Rack.getClazz(), 'room') == 'D.room'
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Dependency.getClazz(), 'asset') == 'D.asset'
	 * </pre>
	 * @param clazz
	 * @param fieldName
	 * @return a property value for a HQL query.
	 * @see GormUtil#isReferenceProperty(java.lang.Class, java.lang.String)
	 * @see GormUtil#getAlternateKeyPropertyName(java.lang.Class)
	 */
	static String getPropertyForField(Class clazz, String fieldName) {

		if(otherAlternateKeys.containsKey(fieldName)){
			return otherAlternateKeys[fieldName].property
		}

		if(AssetEntity.isAssignableFrom(clazz) && GormUtil.isReferenceProperty(clazz, fieldName)){
			Class propertyClazz = GormUtil.getDomainClassOfProperty(clazz, fieldName)
			if(Person.isAssignableFrom(propertyClazz)){
				return SqlUtil.personFullName(fieldName, DOMAIN_ALIAS)
			} else {
				String alternateKey = GormUtil.getAlternateKeyPropertyName(propertyClazz)
				return "${DOMAIN_ALIAS}.${fieldName}.${alternateKey}"
			}

		} else {
			return "${DOMAIN_ALIAS}.${fieldName}"
		}
	}

	/**
	 * Get the join field name for a domain reference.
	 * First of all it checks if there is a join option in otherAlternateKeys map.
	 * If not, then It checks if field name is a reference property using GormUtils.
	 * <pre>
	 *  assert DomainClassQueryHelper.getJoinForField(ETLDomain.Device.getClazz(), 'id') == ''
	 *  assert DomainClassQueryHelper.getJoinForField(ETLDomain.Device.getClazz(), 'manufacturer') == 'left outer join D.manufacturer'
	 *  assert DomainClassQueryHelper.getJoinForField(ETLDomain.Device.getClazz(), 'moveBundle') == 'left outer join D.moveBundle'
	 *  assert DomainClassQueryHelper.getJoinForField(ETLDomain.Device.getClazz(), 'rackSource') == 'left outer join D.rackSource'
	 *  assert DomainClassQueryHelper.getJoinForField(ETLDomain.Device.getClazz(), 'locationSource') == 'left outer join D.roomSource'
	 *
	 *  assert DomainClassQueryHelper.getJoinForField(ETLDomain.Room.getClazz(), 'roomName') == ''
	 *  assert DomainClassQueryHelper.getJoinForField(ETLDomain.Rack.getClazz(), 'room') == 'left outer join D.room'
	 *  assert DomainClassQueryHelper.getJoinForField(ETLDomain.Dependency.getClazz(), 'asset') == 'left outer join D.asset'
	 * </pre>
	 * @param clazz
	 * @param fieldName
	 * @return a join content based on field name and clazz params
	 * @see GormUtil#isReferenceProperty(java.lang.Class, java.lang.String)
	 */
	static String getJoinForField(Class clazz, String fieldName) {

		if(otherAlternateKeys.containsKey(fieldName)){
			return otherAlternateKeys[fieldName].join
		}

		if(GormUtil.isReferenceProperty(clazz, fieldName)){
			return "left outer join ${DOMAIN_ALIAS}.${fieldName}".toString()
		} else {
			return ''
		}
	}

	/**
	 * Prepare el hql where and the hql params for an HQL query.
	 * @param clazz
	 * @param  conditions a list of {@code FindCondition} use for preparing where and params in the HQL sentence.
	 * @return a alist with 2 values: first the where sentence part for an HQL query using Clazz
	 *          and second the hql params for an HQL query..
	 * @see DomainClassQueryHelper#getPropertyForField(java.lang.Class, java.lang.String)
	 * @see DomainClassQueryHelper#getNamedParameterForField(java.lang.Class, java.lang.String)
	 */
	static List hqlWhereAndHqlParams(Project project, Class clazz, List<FindCondition> conditions) {

		Map<String, ?> hqlParams = [:]

		String hqlWhere = conditions.collect { FindCondition condition ->

			String property = getPropertyForField(clazz, condition.propertyName)
			String namedParameter = getNamedParameterForField(clazz, condition.propertyName)

			if (shouldQueryByReferenceId(clazz, condition.propertyName, condition.value) ) {

				def (String where, Object hqlParam) = buildSentenceAndHqlParam(condition, property, namedParameter)
				hqlParams[namedParameter] = hqlParam

				Class propertyClazz = GormUtil.getDomainClassOfProperty(clazz, condition.propertyName)
				if(GormUtil.isDomainProperty(propertyClazz, 'project')){
					where += " and ${property}.project =:${namedParameter}_project \n"
					hqlParams[namedParameter + '_project'] = project
				}
				return where

			} else {

				def (String where, Object hqlParam) = buildSentenceAndHqlParam(condition, property, namedParameter)
				if(condition.propertyName== 'id' && NumberUtil.isPositiveLong(condition.value)) {
					hqlParams[namedParameter] = NumberUtil.toPositiveLong(condition.value)
				} else {
					hqlParams[namedParameter] = hqlParam
				}

				return where
			}

		}.join(' and ')

		return [hqlWhere, hqlParams]
	}

	/**
	 * Used to determine if the field to be queried is a reference and the value is the ID
	 * @param clazz - domain class
	 * @param field - field name
	 * @param value - value to query with
	 * @return true if should query using the ID
	 */
	static private boolean shouldQueryByReferenceId(clazz, field, value) {
		return (value instanceof Long) && field != 'id' && GormUtil.isReferenceProperty(clazz, field)
	}

	/**
	 * Prepares all the necessary joins clause based on paramsMap and clazz parameters.
	 * @param clazz
	 * @param conditions a list of {@code FindCondition}
	 * @return a string content with join clause for an HQL query
	 */
	static String hqlJoins(Class clazz, List<FindCondition> conditions) {
		return conditions.collect { FindCondition condition ->
			getJoinForField(clazz, condition.propertyName)
		}.join('  ')
	}


	/**
	 * It builds the final sentence for an HQL using property, namedParameter and the correct SQL operator
	 * <pre>
	 *
	 *
	 * </pre>
	 * @param condition a {@code FindCondition} parameter to be used in the HQl sentence
	 * @param namedParameter a named parameter param for the HQL sentence
	 * @return a HQL sentence defined by a property operator and named parameter
	 */
	static List buildSentenceAndHqlParam(FindCondition condition, String property, String namedParameter){

		String sentence
		Object hqlParam = condition.value
		switch (condition.operator){
			case FindOperator.eq:
				sentence = " ${property} = :${namedParameter}\n"
				break
			case FindOperator.ne:
				sentence = " ${property} != :${namedParameter}\n"
				break
			case FindOperator.nseq: //TODO: Review it with John
				sentence = " ${property} != :${namedParameter}\n"
				break
			case FindOperator.lt:
				sentence = " ${property} < :${namedParameter}\n"
				break
			case FindOperator.le:
				sentence = " ${property} <= :${namedParameter}\n"
				break
			case FindOperator.gt:
				sentence = " ${property} > :${namedParameter}\n"
				break
			case FindOperator.ge:
				sentence = " ${property} >= :${namedParameter}\n"
				break
			case FindOperator.like:
				sentence = " ${property} like :${namedParameter}\n"
				break
			case FindOperator.notLike:
				sentence = " ${property} not like :${namedParameter}\n"
				break
			case FindOperator.contains:
				sentence = " ${property} like :${namedParameter}\n"
				hqlParam = '%' + condition.value + '%'
				break
			case FindOperator.notContains:
				sentence = " ${property} not like :${namedParameter}\n"
				hqlParam = '%' + condition.value + '%'
				break
			case FindOperator.inList:
				sentence = " ${property} in :${namedParameter}\n"
				break
			case FindOperator.notInList:
				sentence = " ${property} not in :${namedParameter}\n"
				break
			case FindOperator.between:
				sentence = " ${property} between :${namedParameter}\n"
				break
			case FindOperator.notBetween:
				sentence = " ${property} not between :${namedParameter}\n"
				break


			default:
				throw new RuntimeException("Incorrect FindOperator. Use: ${FindOperator.values()}")

		}
		return [sentence, hqlParam]
	}


	static Map otherAlternateKeys = [
		locationSource: [
			property: DOMAIN_ALIAS + '.roomSource.location',
			namedParameter: 'roomSource_location',
			join: 'left outer join ' + DOMAIN_ALIAS + '.roomSource'
		],
		locationTarget: [
			property: DOMAIN_ALIAS + '.roomTarget.location',
			namedParameter: 'roomTarget_location',
			join: 'left outer join ' + DOMAIN_ALIAS + '.roomTarget'
		],
	]
}