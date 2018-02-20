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
 *     find Application by id with SOURCE.'application id'
 * </pre>
 *
 * In to this HQL query:
 *  <pre>
 *      select AE
 *          from AssetEntity AE
 *          where  AE.project = :project
 *            and AE.id = : id
 *  </pre>
 */

//TODO: DMC Refactor DomainClassQueryHelper
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
				assets = nonAssetEntities(Manufacturer, project, fieldsSpec)
				break
			case { MoveBundle.isAssignableFrom(it) }:
				assets = nonAssetEntities(MoveBundle, project, fieldsSpec)
				break
			case { Model.isAssignableFrom(it) }:
				assets = nonAssetEntities(Model, project, fieldsSpec)
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
            select AE
              from AssetEntity AE
             where AE.project = :project
               and AE.assetClass = :assetClass
			   and $hqlWhere """.stripIndent()

		return AssetEntity.executeQuery(hql, [project: project, assetClass: AssetClass.lookup(clazz)] + hqlParams(fieldsSpec))
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
            select AE
              from AssetDependency AE
             where $hqlWhere  
        """.stripIndent()

		Map<String, ?> params = hqlParams(fieldsSpec)
		return AssetDependency.executeQuery(hql, params)
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
			property: "AE." + key,
			namedParamter: key,
			join: "",
			transform: { String value -> value?.trim() }]
	}

	//TODO: Review this with John. Where I can put those commons configurations?
	private static final Map<String, Map> assetEntityTransformations = [
		"id": [
			property: "AE.id",
			namedParamter: "id",
			join: "",
			transform: { String value -> Long.parseLong(value) }
		],
		"moveBundle": [
			property: "AE.moveBundle.name",
			namedParamter: "moveBundleName",
			join: "left outer join AE.moveBundle",
			transform: { String value -> value?.trim() }
		],
		"project": [
			property: "AE.project.description",
			namedParamter: "projectDescription",
			join: "left outer join AE.project",
			transform: { String value -> value?.trim() }
		],
		"manufacturer": [
			property: "AE.manufacturer.name",
			namedParamter: "manufacturerName",
			join: "left outer join AE.manufacturer",
			transform: { String value -> value?.trim() }
		],
		"sme": [
			property: "AE.sme.firstName",
			namedParamter: "smeFirstName",
			join: "left outer join AE.sme",
			transform: { String value -> value?.trim() }
		],
		"sme2": [
			property: "AE.sme2.firstName",
			namedParamter: "sme2FirstName",
			join: "left outer join AE.sme2",
			transform: { String value -> value?.trim() }
		],
		"model": [
			property: "AE.model.modelName",
			namedParamter: "modelModelName",
			join: "left outer join AE.model",
			transform: { String value -> value?.trim() }
		],
		"appOwner": [
			property: "AE.appOwner.firstName",
			namedParamter: "appOwnerFirstName",
			join: "left outer join AE.appOwner",
			transform: { String value -> value?.trim() }
		]
	].withDefault { String key ->
		[
			property: "AE." + key,
			namedParamter: key,
			join: "",
			transform: { String value -> value?.trim() }]
	}


}