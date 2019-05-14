package com.tdsops.etl

import com.tds.asset.AssetEntity
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import groovy.json.internal.LazyMap
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
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
@Slf4j
@CompileStatic(TypeCheckingMode.SKIP)
class DomainClassQueryHelper {

	/**
	 * Static variable to define alias in every hql query used in this class
	 */
	static final String DOMAIN_ALIAS = 'D'

	/**
	 * Static variable to define Manufacturer alias in every hql query used in this class
	 */
	static final String MANUFACTURER_ALIAS = 'MFG_ALIAS'

	/**
	 * Static variable to define Model alias in every hql query used in this class
	 */
	static final String MODEL_ALIAS = 'MDL_ALIAS'

	/**
	 * Static map of fields that require special alternate key query joins
	 */
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
	/**
	 * Defines find results message in a constant variable.
	 */
	public static final String	FIND_RESULTS_MULTIPLE_RECORDS = 'The find/elseFind command(s) found multiple records'

	/**
	 * Executes the HQL query related to the domain defined.
	 * @param domain an instance of ETLDomain used to defined the correct HQL query to be executed
	 * @param project a project instance used as a param in the HQL query
	 * @param paramsMap a map with params to be used in the HQL query
	 * @param returnIdOnly a flag to control if the method returns the result IDs (true - default) or full domain objects (false)
	 * @return a list of Entities returned by an HQL query
	 */
	@CompileStatic
	static List where(ETLDomain domain, Project project, Map<String, ?> paramsMap, Boolean returnIdOnly=true) {
		List<FindCondition> conditions = paramsMap.collect { Entry entry ->
			new FindCondition(entry.key, entry.value)
		}

		return where(domain, project, conditions, returnIdOnly)
	}

	/**
	 * Executes the HQL query related to the domain defined.
	 * @param domain an instance of ETLDomain used to defined the correct HQL query to be executed
	 * @param project a project instance used as a param in the HQL query
	 * @param conditions a list of {@code FindCondition} to be used in the HQL query
	 * @param returnIdOnly a flag to control if the method returns the result IDs (true - default) or full domain objects (false)
	 * @return a list of assets returned by an HQL query
	 */
	@CompileStatic
	static List where(ETLDomain domain, Project project, List<FindCondition> conditions, Boolean returnIdOnly=true) {

		List<Object> results = []
		if (!skipQuery(conditions)) {
			if (domain.isAsset()) {
				results = assetEntities(domain.clazz, project, conditions, returnIdOnly)
			} else {
				results = nonAssetEntities(domain.clazz, project, conditions, returnIdOnly)
			}
		}

 		return results
	}

	/**
	 * Defines if a list of conditions contains a null reference.
	 * If some of the {@code FindCondition#value} contains a null value, then queery must be skipped.
	 * @param conditions a list of {@code FindCondition}
	 * @return true if {@code FindCondition#value} contains a null value, otherwise return false
	 */
	@CompileStatic
	private static boolean skipQuery(List<FindCondition> conditions) {
		// Scan the criteria values for NULL or LazyMap and ignore the query if such (see TM-12374)
		boolean skipQuery = false
		for (condition in conditions) {
			if (
			(![FindOperator.isNull, FindOperator.isNotNull].contains(condition.operator))
				&& (condition.value == null || (condition.value instanceof LazyMap))
			) {
				skipQuery = true
				break
			}
		}
		return skipQuery
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
		String hqlSelect = hqlSelect(returnIdOnly)

		String hql = """
            select $hqlSelect
              from AssetEntity $DOMAIN_ALIAS
				   $hqlJoins
			 where ${DOMAIN_ALIAS}.project = :project
               and ${DOMAIN_ALIAS}.assetClass = :assetClass
			   and $hqlWhere
		""".stripIndent()

		Map args = [project: project, assetClass: AssetClass.lookup(clazz)] + hqlParams

		log.debug 'assetEntities() hql={}, args={}', hql, args

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
	static <T> List nonAssetEntities(Class<T> clazz, Project project, List<FindCondition> conditions, Boolean returnIdOnly = true) {

		def (hqlWhere, hqlParams) = hqlWhereAndHqlParams(project, clazz, conditions)
		String hqlJoins = hqlJoins(clazz, conditions)
		String hqlSelect = hqlSelect(returnIdOnly)

		if (GormUtil.isDomainProperty(clazz, 'project')) {
			hqlWhere += " and ${DOMAIN_ALIAS}.project = :project \n".toString()
			hqlParams += [project: project]
		}

		String hql = """
			  select $hqlSelect
              from ${clazz.simpleName} $DOMAIN_ALIAS
			       $hqlJoins
             where $hqlWhere
		""".stripIndent()

		log.debug 'nonAssetEntities() hql={}, params={}', hql, hqlParams

		return clazz.executeQuery(hql, hqlParams, [readOnly: true])
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
	static String getNamedParameterForField(Class clazz, FindCondition condition) {

		if (otherAlternateKeys.containsKey(condition.propertyName)) {
			return otherAlternateKeys[condition.propertyName].namedParameter
		}

		if (AssetEntity.isAssignableFrom(clazz) && GormUtil.isReferenceProperty(clazz, condition.propertyName)) {

			Class propertyClazz = GormUtil.getDomainClassOfProperty(clazz, condition.propertyName)

			if (Person.isAssignableFrom(propertyClazz)) {
				return condition.propertyName
			} else if (NumberUtil.isaNumber(condition.value)) {
				return "${condition.propertyName}_id"
			} else {
				String alternateKey = GormUtil.getAlternateKeyPropertyName(propertyClazz)
				if (alternateKey) {
					return "${condition.propertyName}_${alternateKey}"
				} else {
					throw new RuntimeException("${condition.propertyName} field does not have alternate key for class ${clazz}".toString())
				}
			}

		} else {
			return condition.propertyName
		}
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
	 * @param domain
	 * @param condition
	 * @return a named parameter value for a HQL query.
	 * @see DomainClassQueryHelper#getNamedParameterForField(java.lang.Class, com.tdsops.etl.FindCondition)
	 */
	@CompileStatic
	static String getNamedParameterForField(ETLDomain domain, FindCondition condition) {
		return getNamedParameterForField(domain.clazz, condition)
	}

	/**
	 * Get a property name the hql where sentence.
	 * @param domain
	 * @param condition
	 * @return a property value for a HQL query.
	 * @see DomainClassQueryHelper#getPropertyForField(java.lang.Class, com.tdsops.etl.FindCondition)
	 */
	@CompileStatic
	static String getPropertyForField(ETLDomain domain, FindCondition condition) {
		return getPropertyForField(domain.getClazz(), condition)
	}

	/**
	 * Get a property name the hql where sentence.
	 * First of all it checks if there is an option alternateKey in otherAlternateKeys map.
	 * If not, it needs to detect if fieldName belongs to a property for AssetEntity hierarchy
	 * (Device, Application, Database and Storage). <br>
	 * <pre>
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Device.getClazz(), new FindCondition('id', 123l)) == 'D.id'
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Device.getClazz(), new FindCondition('manufacturer, 'IBM')') == 'D.manufacturer.name'
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Device.getClazz(), new FindCondition('manufacturer, 123l)') == 'D.manufacturer.id'
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Device.getClazz(), new FindCondition('moveBundle', 'FooBar')) == 'D.moveBundle.name'
	 *
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Room.getClazz(), new FindCondition('roomName', 'FuBar')) == 'D.roomName'
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Rack.getClazz(), new FindCondition('room', 'FuBar')) == 'D.room'
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Dependency.getClazz(), new FindCondition('asset', 'Fubar')) == 'D.asset.assetName'
	 *  assert DomainClassQueryHelper.getPropertyForField(ETLDomain.Dependency.getClazz(), new FindCondition('asset', 123l)) == 'D.asset.id'
	 * </pre>
	 * @param clazz
	 * @param fieldName
	 * @return a property value for a HQL query.
	 * @see GormUtil#isReferenceProperty(java.lang.Class, java.lang.String)
	 * @see GormUtil#getAlternateKeyPropertyName(java.lang.Class)
	 */
	@CompileStatic
	static String getPropertyForField(Class clazz, FindCondition condition) {

		if (otherAlternateKeys.containsKey(condition.propertyName)) {
			return otherAlternateKeys[condition.propertyName].property
		}

		if (AssetEntity.isAssignableFrom(clazz) && GormUtil.isReferenceProperty(clazz, condition.propertyName)) {
			return getPropertyForAssetClassAndField(clazz, condition)
		} else {
			return getPropertyForNonAssetClassAndField(clazz, condition)
		}
	}

	/**
	 * Get a property name the hql where sentence for a non AssetEntity domain instance.
	 * // 1) If fieldName is Reference
	 * // 2) 	If reference value is Long, then fieldName.id
	 * // 3)	else if reference getAlternateKeyPropertyName
	 * // 4)    else if reference is Alias (Model || Manufacturer) //TODO:dcorrea. I need to add this logic in other ticket
	 * // 5) else fieldName
	 * @param clazz
	 * @param condition
	 * @return a property value for a HQL query.
	 */
	@CompileStatic
	static String getPropertyForNonAssetClassAndField(Class clazz, FindCondition condition) {

		if (GormUtil.isReferenceProperty(clazz, condition.propertyName)) {

			if (NumberUtil.isaNumber(condition.value)) {
				return "${DOMAIN_ALIAS}.${condition.propertyName}.id"
			}

			Class propertyClazz = GormUtil.getDomainClassOfProperty(clazz, condition.propertyName)
			String alternateKey = GormUtil.getAlternateKeyPropertyName(propertyClazz)
			if (alternateKey) {
				return "${DOMAIN_ALIAS}.${condition.propertyName}.${alternateKey}"
			} else {
				//TODO: dcorrea 26/09/2018 add logic for Alias table in case of Model or Manufacturer
				throw new RuntimeException("${propertyClazz?.simpleName} does not have alternate key".toString())
			}
		}

		return "${DOMAIN_ALIAS}.${condition.propertyName}"
	}

	/**
	 * Get a property name the hql where sentence for an AssetEntity domain instance
	 * @param clazz
	 * @param condition
	 * @return a property value for a HQL query.
	 */
	@CompileStatic
	static String getPropertyForAssetClassAndField(Class clazz, FindCondition condition) {
		Class propertyClazz = GormUtil.getDomainClassOfProperty(clazz, condition.propertyName)
		if (Person.isAssignableFrom(propertyClazz)) {
			return SqlUtil.personFullName(condition.propertyName, DOMAIN_ALIAS)
		} else if(NumberUtil.isaNumber(condition.value)){
			return "${DOMAIN_ALIAS}.${condition.propertyName}.id"
		} else {
			String alternateKey = GormUtil.getAlternateKeyPropertyName(propertyClazz)
			if(alternateKey){
				return "${DOMAIN_ALIAS}.${condition.propertyName}.${alternateKey}"
			} else {
				throw new RuntimeException("${condition.propertyName} field does not have alternate key for class ${clazz}".toString())
			}
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
	@CompileStatic
	static String getJoinForField(Class clazz, String fieldName) {

		if (otherAlternateKeys.containsKey(fieldName)) {
			return otherAlternateKeys[fieldName].join
		}

		if (GormUtil.isReferenceProperty(clazz, fieldName)) {
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
	 * @see DomainClassQueryHelper#getPropertyForField(java.lang.Class, com.tdsops.etl.FindCondition)
	 * @see DomainClassQueryHelper#getNamedParameterForField(java.lang.Class, com.tdsops.etl.FindCondition)
	 */
	static List hqlWhereAndHqlParams(Project project, Class clazz, List<FindCondition> conditions) {

		Map<String, ?> hqlParams = [:]

		String hqlWhere = conditions.collect { FindCondition condition ->

			String property = getPropertyForField(clazz, condition)
			String namedParameter = getNamedParameterForField(clazz, condition)

			if (shouldQueryByReferenceId(clazz, condition.propertyName, condition.value)) {

				String where = buildSentenceAndHqlParam(clazz, condition, property, namedParameter, hqlParams)
				// If user is trying to use find command by id, automatically we convert that value to a long
				if (property.endsWith('.id') && NumberUtil.isaNumber(condition.value)) {
					hqlParams[namedParameter] = NumberUtil.toPositiveLong(hqlParams[namedParameter], 0)
				}

				Class propertyClazz = GormUtil.getDomainClassOfProperty(clazz, condition.propertyName)

				// Why is Project being queried ONLY if query is by reference ID?
				if (GormUtil.isDomainProperty(propertyClazz, 'project')) {
					where += " and ${DOMAIN_ALIAS}.${condition.propertyName}.project =:${namedParameter}_project \n"
					hqlParams[namedParameter + '_project'] = project
				}
				return where

			} else {

				String where = buildSentenceAndHqlParam(clazz, condition, property, namedParameter, hqlParams)
				// If user is trying to use find command by id, automatically we convert that value to a long
				if (condition.propertyName == 'id' && NumberUtil.isPositiveLong(condition.value)) {
					hqlParams[namedParameter] = NumberUtil.toPositiveLong(hqlParams[namedParameter], 0)
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
	@CompileStatic
	static private boolean shouldQueryByReferenceId(clazz, field, value) {
		boolean should = NumberUtil.isaNumber(value) && field != 'id' && GormUtil.isReferenceProperty(clazz, field)
		// log.debug 'shouldQueryByReferenceId() value={}, type={}, result={}', value, value.getClass().getName(), should
		return should
	}

	/**
	 * Prepares all the necessary joins clause based on paramsMap and clazz parameters.
	 * @param clazz
	 * @param conditions a list of {@code FindCondition}
	 * @return a string content with join clause for an HQL query
	 */
	@CompileStatic
	static String hqlJoins(Class clazz, List<FindCondition> conditions) {
		return conditions.collect { FindCondition condition ->
			getJoinForField(clazz, condition.propertyName)
		}.join('  ')
	}

	/**
	 * Creates select part in an HQL sentence.
	 * Check if results should only contains IDs instead of Gorm domain instances
	 * Validate if It is necessary to add distinct in HQL sentence based on if it has Aliases added
	 *
	 * @param returnIdOnly boolean valu that define if select should return only IDs
	 * @return a String with final HQL sentence content
	 */
	@CompileStatic
	static String hqlSelect(Boolean returnIdOnly){

		String select = DOMAIN_ALIAS

		if(returnIdOnly){
			select += '.id'
		}

		return select
	}

	/**
	 * <p>It builds the final sentence for an HQL using property, namedParameter and the correct SQL operator</p>
	 * <p>IT also calculate the necessary named parameters to complete the HQL sentnce</p>
	 * <pre>
	 *
	 * </pre>
	 * @param clazz - domain class
	 * @param condition a {@code FindCondition} parameter to be used in the HQl sentence
	 * @param namedParameter a named parameter param for the HQL sentence
	 * @param hqlParams
	 * @return a HQL sentence defined by a property operator and named parameter
	 */
	static String buildSentenceAndHqlParam(
		Class clazz,
		FindCondition condition,
		String property,
		String namedParameter,
		Map<String, ?> hqlParams) {

		// Small numbers loaded from JSON are Integer and need to be forced to Long for HQL
		if (property.endsWith('.id')) {
			condition.value = NumberUtil.toPositiveLong(condition.value, 0)
		}

		Class propertyClazz = GormUtil.getDomainPropertyType(clazz, condition.propertyName)
		Object value = transformValueToDomainPropertyClass(condition.value, propertyClazz)
		String sentence

		switch (condition.operator){
			case FindOperator.eq:
				sentence = " ${property} = :${namedParameter}\n"
				hqlParams[namedParameter] = value
				break
			case FindOperator.ne:
				sentence = " ${property} != :${namedParameter}\n"
				hqlParams[namedParameter] = value
				break
			case FindOperator.nseq: //TODO: Review it with John
				sentence = " ${property} != :${namedParameter}\n"
				hqlParams[namedParameter] = value
				break
			case FindOperator.lt:
				sentence = " ${property} < :${namedParameter}\n"
				hqlParams[namedParameter] = value
				break
			case FindOperator.le:
				sentence = " ${property} <= :${namedParameter}\n"
				hqlParams[namedParameter] = value
				break
			case FindOperator.gt:
				sentence = " ${property} > :${namedParameter}\n"
				hqlParams[namedParameter] = value
				break
			case FindOperator.ge:
				sentence = " ${property} >= :${namedParameter}\n"
				hqlParams[namedParameter] = value
				break
			case FindOperator.like:
				sentence = " ${property} like :${namedParameter}\n"
				hqlParams[namedParameter] = value
				break
			case FindOperator.notLike:
				sentence = " ${property} not like :${namedParameter}\n"
				hqlParams[namedParameter] = value
				break
			case FindOperator.contains:
				sentence = " ${property} like :${namedParameter}\n"
				hqlParams[namedParameter] = '%' + value + '%'
				break
			case FindOperator.notContains:
				sentence = " ${property} not like :${namedParameter}\n"
				hqlParams[namedParameter] = '%' + value + '%'
				break
			case FindOperator.inList:
				sentence = " ${property} in :${namedParameter}\n"
				hqlParams[namedParameter] = value
				break
			case FindOperator.notInList:
				sentence = " ${property} not in :${namedParameter}\n"
				hqlParams[namedParameter] = value
				break
			case FindOperator.between:
				sentence = " ${property} between :${namedParameter}_first and :${namedParameter}_last\n"
				hqlParams[namedParameter + '_first'] = value.first()
				hqlParams[namedParameter + '_last'] = value.last()
				break
			case FindOperator.notBetween:
				sentence = " ${property} not between :${namedParameter}_first and :${namedParameter}_last\n"
				hqlParams[namedParameter + '_first'] = value.first()
				hqlParams[namedParameter + '_last'] = value.last()
				break
			case FindOperator.isNull:
				sentence = " ${property} is null\n"
				break
			case FindOperator.isNotNull:
				sentence = " ${property} is not null\n"
				break
			default:
				throw new RuntimeException("Incorrect FindOperator. Use: ${FindOperator.values()}")
		}

		return checkAndAddAliases(clazz, property, namedParameter, sentence)
	}

	/**
	 * <p>When user adds a value in a find command, this function can transform the original value
	 * defined by user adjusting correctly its type.</p>
	 * <pre>
	 *   find Device by 'description' eq 2 into 'id'
	 * </pre>
	 * <p>In this case {@code Device#description} field is String type. This method converts value 2 in "2"
	 * in order to avoid errors in find command<p>
	 * <p>If user tries to use a Collection, then this method transform correctly each one of the collection items</p>
	 * <pre>
	 * 	find Device by 'description' inList [2, 3, 4] into 'id'
	 * </pre>
	 * <p>In this case {@code Device#description} find command is used in a inList query. In this case,
	 * this method transforms [2, 3, 4] in ["2", "3", "4"] <p>
	 * @param value an Object or a collection defined by user in find command
	 * @param propertyClazz domain property class
	 * @return value parameter transformed by propertyClazz parameter
	 * @see AssetEntity#description
	 */
	static Object transformValueToDomainPropertyClass(Object value, Class propertyClazz) {

		Boolean isCollection = Collection.isAssignableFrom(value.getClass())

		if (propertyClazz == String) {
			return isCollection ? value.collect { it.toString() } : value.toString()
		} else if (propertyClazz == Long) {
			return isCollection ? value.collect { NumberUtil.toLongNumber(it) } : NumberUtil.toLongNumber(value)
		} else if (propertyClazz == Double) {
			return isCollection ? value.collect { NumberUtil.toDoubleNumber(it) } : NumberUtil.toDoubleNumber(value)
		}
		return value
	}

	/**
	 * <p>Prepare HQL manufacturer alias sentence.</p>
	 * It can be used in different scenarios:
	 * <pre>
	 *     find Manufacturer by name eq '...' into '..'
	 *     find Device by manufacturer eq '...' into '..'
	 * </pre>
	 * @param field original hql sentence
	 * @param sentence original hql sentence
	 * @param namedParameter
	 * @return an HQL sentence improved with Manufacturer alias
	 */
	@CompileStatic
	static String hqlManufacturerAlias(String field, String sentence, String namedParameter) {
		return """
			( ${sentence} or
				${field} in (
					select ${MANUFACTURER_ALIAS}.manufacturer
					  from ManufacturerAlias ${MANUFACTURER_ALIAS}
					 where ${MANUFACTURER_ALIAS}.name = :${namedParameter}
					)
			)"""
	}

	/**
	 * <p>Prepare HQL manufacturer alias sentence.</p>
	 * It can be used in 2 different scenarios:
	 * <pre>
	 *     find Device by model eq '...' into '..'
	 *     find Model by modelName eq '...' into '..'
	 * </pre>
	 * @param field original hql sentence
	 * @param sentence original hql sentence
	 * @param namedParameter
	 * @return an HQL sentence improved with Model alias
	 */
	@CompileStatic
	static String hqlModelAlias(String field, String sentence, String namedParameter) {
		return """
			( ${sentence} or
				${field} in (
					select ${MODEL_ALIAS}.model
					  from ModelAlias ${MODEL_ALIAS}
					 where ${MODEL_ALIAS}.name = :${namedParameter}
				)
			)"""
	}

	/**
	 * <p>Add aliases query parameters for Manufacturer and Model domains</p>
	 * <p>If a ETL find command contains a manufacturer name</p>
	 * <pre>
	 * 	find Device by 'manufacturer' eq '....' into 'id'
	 * </pre>
	 * <p>Then it adds the following hql content:</p>
	 * <pre>
	 *   select D
	 * 	   from AssetEntity D
	 * 	   left outer join D.manufacturer
	 * 	  where D.project = :project
	 * 		and D.assetClass = :assetClass
	 * 		and ( D.manufacturer.name = :manufacturer_name
	 * 			or D.manufacturer in (
	 * 		    	select MFG_ALIAS.manufacturer
	 * 		      	  from ManufacturerAlias MFG_ALIAS
	 * 		      	 where MFG_ALIAS.name = :manufacturer_name )
	 * </pre>
	 * @param clazz
	 * @param property
	 * @param namedParameter
	 * @param sentence
	 * @return a String with ModelAlias and ManufacturerAlias applied in an HQL sentence
	 */
	@CompileStatic
	static String checkAndAddAliases(Class clazz, String property, String namedParameter, String sentence) {

		if (clazz == AssetEntity.class) {

			if ( property == "${DOMAIN_ALIAS}.manufacturer.name" ) {
				/**
				 * find Device by manufacturer eq '...' into '..'
				 */
				return hqlManufacturerAlias('D.manufacturer', sentence, namedParameter)
			}

			if ( property == "${DOMAIN_ALIAS}.model.modelName" ) {
				/**
				* find Device by model eq '...' into '..'
				*/
				return hqlModelAlias('D.model', sentence, namedParameter)
			}

		} else if ( clazz in Manufacturer ) {

			if ( property == "${DOMAIN_ALIAS}.name" ) {
				/**
				* find Manufacturer by name eq '...' into '..'
				*/
				return hqlManufacturerAlias('D', sentence, namedParameter)
			}

		} else if ( clazz in Model ) {

			if (property == "${DOMAIN_ALIAS}.modelName" ){
				/**
				 * find Model by modelName eq '...' into '..'
				 */
				return hqlModelAlias('D', sentence, namedParameter)
			}

			if ( "${DOMAIN_ALIAS}.manufacturer.name" ) {
				/**
				 * find Model by manufacturer eq '...' into '..'
				 */
				return hqlManufacturerAlias('D.manufacturer', sentence, namedParameter)
			}
		}

		return sentence
	}

}