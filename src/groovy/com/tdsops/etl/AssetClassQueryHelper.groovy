package com.tdsops.etl

import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import net.transitionmanager.domain.Project

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
class AssetClassQueryHelper {

	/**
	 * Executes the HQL query related to the domain defined.
	 * @param domain an instance of ETLDomain used to defined the correct HQL query to be executed
	 * @param project a project instance used as a param in the HQL query
	 * @param fieldsSpec a map with params to be used in the HQL query
	 * @return a list of assets returned by an HQL query
	 */
	static List where(ETLDomain domain, Project project, Map<String, ?> fieldsSpec) {

		List assets = []

		switch (domain) {
			case ETLDomain.Application ||
					ETLDomain.Device ||
					ETLDomain.Database ||
					ETLDomain.Storage:
				assets = assetEntities(project, fieldsSpec)
				break
			case ETLDomain.Dependency:
				assets = assetDependencies(fieldsSpec)
				break
		}

		return assets
	}
	/**
	 * Executes an HQL query looking for those all assets referenced by params.
	 * @param project a project instance used as a param in the HQL query
	 * @param fieldsSpec a map with params to be used in the HQL query
	 * @return a list of assets returned by an HQL query
	 */
	static List<? extends AssetEntity> assetEntities(Project project, Map<String, ?> fieldsSpec) {

		String hqlWhere = hqlWhere(fieldsSpec)

		String hql = """
            select AE
              from AssetEntity AE
             where  AE.project = :project and $hqlWhere  
        """.stripIndent()

		return AssetEntity.executeQuery(hql, [project: project] + hqlParams(fieldsSpec))
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

		return AssetDependency.executeQuery(hql, hqlParams(fieldsSpec))
	}

	/**
	 * Helper method used to prepare the where params in a HQL query
	 * @param fieldsSpec a map with params to be used in the HQL query
	 * @return an String with the where clause prepared to be executed.
	 */
	static String hqlWhere(Map<String, ?> fieldsSpec) {
		fieldsSpec.keySet().collect { String field ->
			" ${assetEntityTransformations[field].property} = :${assetEntityTransformations[field].namedParamter}\n".toString()
		}.join(' and ')
	}

	/**
	 * Transforms AssetEntity query params.
	 * @param fieldsSpec a map with params to be used in the HQL query
	 * @return a map with params transformed correctly
	 * 		   based on com.tdsops.etl.AssetClassQueryHelper#assetEntityTransformations
	 */
	static Map<String, ?> hqlParams(Map<String, ?> fieldsSpec) {
		fieldsSpec.collectEntries { String key, def value ->
			[("${assetEntityTransformations[key].namedParamter}".toString()): assetEntityTransformations[key].transform(value?.toString())]
		}
	}

	//TODO: Review this with John. Where I can put those commons configurations?
	private static final Map<String, Map> assetEntityTransformations = [
			"id"          : [
					property     : "AE.id",
					namedParamter: "id",
					join         : "",
					transform    : { String value -> Long.parseLong(value) }
			],
			"moveBundle"  : [
					property     : "AE.moveBundle.name",
					namedParamter: "moveBundleName",
					join         : "left outer join AE.moveBundle",
					transform    : { String value -> value?.trim() }
			],
			"project"     : [
					property     : "AE.project.description",
					namedParamter: "projectDescription",
					join         : "left outer join AE.project",
					transform    : { String value -> value?.trim() }
			],
			"manufacturer": [
					property     : "AE.manufacturer.name",
					namedParamter: "manufacturerName",
					join         : "left outer join AE.manufacturer",
					transform    : { String value -> value?.trim() }
			],
			"sme"         : [
					property     : "AE.sme.firstName",
					namedParamter: "smeFirstName",
					join         : "left outer join AE.sme",
					transform    : { String value -> value?.trim() }
			],
			"sme2"        : [
					property     : "AE.sme2.firstName",
					namedParamter: "sme2FirstName",
					join         : "left outer join AE.sme2",
					transform    : { String value -> value?.trim() }
			],
			"model"       : [
					property     : "AE.model.modelName",
					namedParamter: "modelModelName",
					join         : "left outer join AE.model",
					transform    : { String value -> value?.trim() }
			],
			"appOwner"    : [
					property     : "AE.appOwner.firstName",
					namedParamter: "appOwnerFirstName",
					join         : "left outer join AE.appOwner",
					transform    : { String value -> value?.trim() }
			]
	].withDefault { String key ->
		[
				property     : "AE." + key,
				namedParamter: key,
				join         : "",
				transform    : { String value -> value?.trim() }]
	}


}