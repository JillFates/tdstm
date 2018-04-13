package com.tdsops.etl

import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Files
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.NumberUtil
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

		String hqlWhere = hqlWhere(fieldsSpec)

		String hql = """
            select D
              from AssetEntity D
             where D.project = :project
               and D.assetClass = :assetClass
			   and $hqlWhere """.stripIndent()

		Map args = [project: project, assetClass: AssetClass.lookup(clazz)] + hqlParams(fieldsSpec)
		return AssetEntity.executeQuery(hql, args)
	}

	/**
	 *
	 * @param clazz
	 * @param fieldsSpec
	 * @return
	 */
	static <T> List<T> nonAssetEntities(Class<T> clazz, Project project, Map<String, ?> fieldsSpec) {

		String hqlWhere = fieldsSpec.keySet().collect { String field ->
			" ${field} = :${field}\n".toString()
		}.join(' and ')

		Map<String, ?> hqlParams = fieldsSpec.collectEntries { String key, def value ->
			[(key.toString()): nonAssetEntityTransformations[key].transform(value?.toString())]
		}

		String hql = """
              from ${clazz.simpleName} D
             where D.project = :project
               and $hqlWhere """.stripIndent()

		return clazz.executeQuery(hql, [project: project] + hqlParams)
	}

	/**
	 * Executes an HQL query looking for those all asset dependencies referenced by params.
	 * @param fieldsSpec a map with params to be used in the HQL query
	 * @return a list of asset dependencies returned by an HQL query
	 */
	static List<AssetDependency> assetDependencies(Map<String, ?> fieldsSpec) {

		String hqlWhere = hqlWhere(fieldsSpec)

		String hql = """
            select D
              from AssetDependency D
             where $hqlWhere  
        """.stripIndent()

		Map<String, ?> params = hqlParams(fieldsSpec)
		return AssetDependency.executeQuery(hql, params)
	}

	/**
	 * Executes an HQL query looking for those all models referenced by params.
	 * @param fieldsSpec a map with params to be used in the HQL query
	 * @return a list of Model returned by an HQL query
	 * @see Model
	 */
	static List<Model> models(Map<String, ?> fieldsSpec) {

		String hqlWhere = hqlWhere(fieldsSpec)

		String hql = """
            select D
              from Model D
             where $hqlWhere  
        """.stripIndent()

		Map<String, ?> params = hqlParams(fieldsSpec)
		return Model.executeQuery(hql, params)
	}

	/**
	 * Executes an HQL query looking for those all manufacturers referenced by params.
	 * @param fieldsSpec a map with params to be used in the HQL query
	 * @return a list of manufacturers returned by an HQL query
	 * @see Model
	 */
	static List<Manufacturer> manufacturers(Map<String, ?> fieldsSpec) {

		String hqlWhere = hqlWhere(fieldsSpec)

		String hql = """
            select D
              from Manufacturer D
             where $hqlWhere  
        """.stripIndent()

		Map<String, ?> params = hqlParams(fieldsSpec)
		return Manufacturer.executeQuery(hql, params)
	}

	/**
	 * Helper method used to prepare the where params in a HQL query
	 * @param fieldsSpec a map with params to be used in the HQL query
	 * @return an String with the where clause prepared to be executed.
	 */
	static String hqlWhere(Map<String, ?> fieldsSpec) {
		fieldsSpec.keySet().collect { String field ->
			" ${assetEntityTransformations[field].property} = :${assetEntityTransformations[field].namedParamter}\n".
				toString()
		}.join(' and ')
	}

	/**
	 * Transforms AssetEntity query params.
	 * @param fieldsSpec a map with params to be used in the HQL query
	 * @return a map with params transformed correctly
	 * 		   based on com.tdsops.etl.DomainClassQueryHelper#assetEntityTransformations
	 */
	static Map<String, ?> hqlParams(Map<String, ?> fieldsSpec) {
		fieldsSpec.collectEntries { String key, def value ->
			[("${assetEntityTransformations[key].namedParamter}".toString()): assetEntityTransformations[key].transform(value?.
				toString())]
		}
	}

	private static final nonAssetEntityTransformations = [
		"id": [transform: { String value -> NumberUtil.toLong(value) }]
	].withDefault { String key ->
		[
			property: "D." + key,
			namedParamter: key,
			join: "",
			transform: { String value -> value?.trim() }]
	}

	//TODO: Review this with John. Where I can put those commons configurations?
	private static final Map<String, Map> assetEntityTransformations = [
		"id": [
			property: "D.id",
			namedParamter: "id",
			join: "",
			transform: { String value -> Long.parseLong(value) }
		],
		"moveBundle": [
			property: "D.moveBundle.name",
			namedParamter: "moveBundleName",
			join: "left outer join D.moveBundle",
			transform: { String value -> value?.trim() }
		],
		"project": [
			property: "D.project.description",
			namedParamter: "projectDescription",
			join: "left outer join D.project",
			transform: { String value -> value?.trim() }
		],
		"manufacturer": [
			property: "D.manufacturer.name",
			namedParamter: "manufacturerName",
			join: "left outer join D.manufacturer",
			transform: { String value -> value?.trim() }
		],
		"sme": [
			property: "D.sme.firstName",
			namedParamter: "smeFirstName",
			join: "left outer join D.sme",
			transform: { String value -> value?.trim() }
		],
		"sme2": [
			property: "D.sme2.firstName",
			namedParamter: "sme2FirstName",
			join: "left outer join D.sme2",
			transform: { String value -> value?.trim() }
		],
		"model": [
			property: "D.model.modelName",
			namedParamter: "modelModelName",
			join: "left outer join D.model",
			transform: { String value -> value?.trim() }
		],
		"appOwner": [
			property: "D.appOwner.firstName",
			namedParamter: "appOwnerFirstName",
			join: "left outer join D.appOwner",
			transform: { String value -> value?.trim() }
		]
	].withDefault { String key ->
		[
			property: "D." + key,
			namedParamter: key,
			join: "",
			transform: { String value -> value?.trim() }]
	}


}