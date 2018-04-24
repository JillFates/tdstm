package com.tdsops.etl

import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Files
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room

/**
 * This class helps in the ETL processor to prepare the correct query for find command.
 * For example it transform:
 *
 * <pre>
 *     find Application of id by id with SOURCE.'application id'
 * </pre>
 *
 * In to this HQL query:
 *  <pre>
 *      select D
 *          from AssetEntity D
 *          where  D.project = :project
 *            and D.id = : id
 *  </pre>
 */
class DomainClassQueryHelper {


	static final String DOMAIN_ALIAS = 'D'
	/**
	 * Executes the HQL query related to the domain defined.
	 * @param domain an instance of ETLDomain used to defined the correct HQL query to be executed
	 * @param project a project instance used as a param in the HQL query
	 * @param fieldsSpec a map with params to be used in the HQL query
	 * @return a list of assets returned by an HQL query
	 * @trows an exception if domain is not correctly
	 */
	static List where(ETLDomain domain, Project project, Map<String, ?> fieldsSpec) {

		List assets

		switch(domain.clazz){
			case { AssetEntity.isAssignableFrom(it) }:
				assets = assetEntities(domain.clazz, project, fieldsSpec)
				break
			case { AssetDependency.isAssignableFrom(it) }:
				assets = assetDependencies(fieldsSpec)
				break
			case { Rack.isAssignableFrom(it) }:
				assets = nonAssetEntities(Rack, project, fieldsSpec)
				break
			case { Room.isAssignableFrom(it) }:
				assets = nonAssetEntities(Room, project, fieldsSpec)
				break
			case { Manufacturer.isAssignableFrom(it) }:
				assets = manufacturers(fieldsSpec)
				break
			case { MoveBundle.isAssignableFrom(it) }:
				assets = nonAssetEntities(MoveBundle, project, fieldsSpec)
				break
			case { Model.isAssignableFrom(it) }:
				assets = models(fieldsSpec)
				break
			case { Person.isAssignableFrom(it) }:
				assets = nonAssetEntities(Person, project, fieldsSpec)
				break
			case { Files.isAssignableFrom(it) }:
				assets = nonAssetEntities(Files, project, fieldsSpec)
				break
			default:
				throw ETLProcessorException.incorrectDomain(domain)
		}

		return assets
	}
	/**
	 * Executes an HQL query looking for those all assets referenced by params.
	 * @param project a project instance used as a param in the HQL query
	 * @param fieldsSpec a map with params to be used in the HQL query
	 * @return a list of assets returned by an HQL query
	 */
	static List<? extends AssetEntity> assetEntities(Class<? extends AssetEntity> clazz, Project project, Map<String, ?> fieldsSpec) {

		String hqlWhere = hqlWhere(clazz, fieldsSpec)
		String hqlJoins = hqlJoins(clazz, fieldsSpec)

		String hql = """
            select $DOMAIN_ALIAS
              from AssetEntity $DOMAIN_ALIAS
				   $hqlJoins
             where ${DOMAIN_ALIAS}.project = :project
               and ${DOMAIN_ALIAS}.assetClass = :assetClass
			   and $hqlWhere
			""".stripIndent()

		Map args = [project: project, assetClass: AssetClass.lookup(clazz)] + hqlParams(clazz, fieldsSpec)
		return AssetEntity.executeQuery(hql, args)
	}

	/**
	 *
	 * @param clazz
	 * @param fieldsSpec
	 * @return
	 */
	static <T> List<T> nonAssetEntities(Class<T> clazz, Project project, Map<String, ?> fieldsSpec) {

		String hqlWhere = hqlWhere(clazz, fieldsSpec)
		String hqlJoins = hqlJoins(clazz, fieldsSpec)
		Map<String, ?> hqlParams = hqlParams(clazz, fieldsSpec)

		String hql = """
			  select $DOMAIN_ALIAS	
              from ${clazz.simpleName} $DOMAIN_ALIAS
			       $hqlJoins
             where ${DOMAIN_ALIAS}.project = :project
               and $hqlWhere 
		""".stripIndent()

		return clazz.executeQuery(hql, [project: project] + hqlParams)
	}

	/**
	 * Executes an HQL query looking for those all asset dependencies referenced by params.
	 * @param fieldsSpec a map with params to be used in the HQL query
	 * @return a list of asset dependencies returned by an HQL query
	 */
	static List<AssetDependency> assetDependencies(Map<String, ?> fieldsSpec) {

		String hqlWhere = hqlWhere(AssetDependency, fieldsSpec)
		String hqlJoins = hqlJoins(AssetDependency, fieldsSpec)

		String hql = """
            select $DOMAIN_ALIAS
              from AssetDependency $DOMAIN_ALIAS
				   $hqlJoins
             where $hqlWhere  
        """.stripIndent()

		Map<String, ?> params = hqlParams(AssetDependency, fieldsSpec)
		return AssetDependency.executeQuery(hql, params)
	}

	/**
	 * Executes an HQL query looking for those all models referenced by params.
	 * @param fieldsSpec a map with params to be used in the HQL query
	 * @return a list of Model returned by an HQL query
	 * @see Model
	 */
	static List<Model> models(Map<String, ?> fieldsSpec) {

		String hqlWhere = hqlWhere(Model, fieldsSpec)
		String hqlJoins = hqlJoins(Model, fieldsSpec)

		String hql = """
            select $DOMAIN_ALIAS
              from Model $DOMAIN_ALIAS
				   $hqlJoins
             where $hqlWhere  
        """.stripIndent()

		Map<String, ?> params = hqlParams(Model, fieldsSpec)
		return Model.executeQuery(hql, params)
	}

	/**
	 * Executes an HQL query looking for those all manufacturers referenced by params.
	 * @param fieldsSpec a map with params to be used in the HQL query
	 * @return a list of manufacturers returned by an HQL query
	 * @see Model
	 */
	static List<Manufacturer> manufacturers(Map<String, ?> fieldsSpec) {

		String hqlWhere = hqlWhere(Manufacturer, fieldsSpec)
		String hqlJoins = hqlJoins(Manufacturer, fieldsSpec)

		String hql = """
            select $DOMAIN_ALIAS
              from Manufacturer $DOMAIN_ALIAS
				   $hqlJoins
             where $hqlWhere  
        """.stripIndent()

		Map<String, ?> params = hqlParams(Model, fieldsSpec)
		return Manufacturer.executeQuery(hql, params)
	}

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

	static String getJoinForField(Class clazz, String fieldName) {

		if(otherAlternateKeys.containsKey(fieldName)){
			return otherAlternateKeys[fieldName].join
		}

		if(GormUtil.isReferenceProperty(clazz, fieldName)){
			return " left outer join ${DOMAIN_ALIAS}.${fieldName}".toString()
		} else {
			return ''
		}
	}

	static String getOperatorForField(Class clazz, String fieldName){

		String operator = '= '

		if(AssetEntity.isAssignableFrom(clazz) &&
			GormUtil.isDomainProperty(clazz, fieldName) &&
				GormUtil.isReferenceProperty(clazz, fieldName)){
			Class propertyClazz = GormUtil.getDomainClassOfProperty(clazz, fieldName)
			if(Person.isAssignableFrom(propertyClazz)){
				operator = 'like'
			}
		}

		return operator
	}

	static String hqlWhere(Class clazz, Map<String, ?> fieldsSpec) {
		String s = fieldsSpec.keySet().collect { String field ->
			String property = getPropertyForField(clazz, field)
			String namedParameter = getNamedParameterForField(clazz, field)
			String comparator = getOperatorForField(clazz, field)
			" ${property} ${comparator} :${namedParameter}\n"
		}.join(' and ')

		return s
	}

	static Map<String, ?> hqlParams(Class clazz, Map<String, ?> fieldsSpec) {
		return fieldsSpec.collectEntries { String key, def value ->
			String namedParameter = getNamedParameterForField(clazz, key)
			[(namedParameter): value]
		}
	}

	static String hqlJoins(Class clazz, Map<String, ?> fieldsSpec) {
		return fieldsSpec.collect { def item ->
			getJoinForField(clazz, item.key)
		}.join('  ')
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