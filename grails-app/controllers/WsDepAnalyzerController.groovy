// import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import com.tdsops.common.security.spring.HasPermission
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import com.tdssrc.grails.NumberUtil
import net.transitionmanager.service.InvalidParamException
import grails.converters.JSON
import org.springframework.jdbc.core.JdbcTemplate

/**
 * Controller used primarily for the Dependency Analyzer
 */
// @GrailsCompileStatic
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsDepAnalyzerController')
class WsDepAnalyzerController implements ControllerMethods {

	JdbcTemplate jdbcTemplate

	/** Returns a list of people that are app owners or SMEs associated to applications in a dependency group
	 * @param depGroup - the dependency group number or 'onePlus'
	 * @return List of persons as JSON
	 */
	@HasPermission(Permission.DepAnalyzerView)
	def peopleAssociatedToDepGroup () {
		// validate the parameters
		def depGroup = NumberUtil.isLong(params.depGroup) ? NumberUtil.toInteger(params.depGroup) : (params.depGroup == 'onePlus' ? 'onePlus' : null)
		String projectId = securityService.getUserCurrentProjectId()

		StringBuilder query = new StringBuilder("""
			SELECT DISTINCT p.person_id AS personId,
			CONCAT(CONCAT( IF(p.first_name = null, '', p.first_name), IF(STRCMP(' ', CONCAT(' ', p.middle_name)) = 0, '', CONCAT(' ', p.middle_name)) ),
				IF(STRCMP(' ', CONCAT(' ', p.last_name)) = 0, '', CONCAT(' ', p.last_name))) AS name
			FROM asset_dependency_bundle adb
			LEFT OUTER JOIN asset_entity ae ON ae.asset_entity_id = adb.asset_id
			LEFT OUTER JOIN application app ON app.app_id = ae.asset_entity_id
			RIGHT OUTER JOIN person p ON p.person_id = app.sme_id OR p.person_id = app.sme2_id
			WHERE adb.project_id = ${projectId}
		""")
		if (depGroup == 'onePlus')
			query.append(' AND adb.dependency_bundle > 0')
		else if (depGroup != null)
			query.append(' AND adb.dependency_bundle = ' + depGroup)

		List personData = jdbcTemplate.queryForList(query.toString())

		render personData as JSON
	}

	/**
	 * Returns the list of the ids of the assets that should be highlighted by the given filter
	 * @param depGroup - the dependency group number or 'onePlus'
	 * @param nameFilter
	 * @param personFilter
	 * @param isRegex
	 * @return A list of assets found by filter as JSON
	 */
	@HasPermission(Permission.DepAnalyzerView)
	def filteredAssetList () {
		// validate the parameters
		String nameFilter = params.nameFilter ? params.nameFilter.toUpperCase() : ''
		boolean isRegex = params.isRegex == 'true'
		String projectId = securityService.getUserCurrentProjectId()

		Long personId = NumberUtil.toPositiveLong(params.personId, null)
		def depGroup = params.depGroup
		if (depGroup != 'onePlus') {
			depGroup = NumberUtil.toPositiveLong(params.depGroup, null)
		}

		StringBuilder query = new StringBuilder("""
			SELECT DISTINCT ae.asset_entity_id AS assetId, ae.asset_name AS assetName FROM asset_entity ae
			LEFT OUTER JOIN application app ON app.app_id = ae.asset_entity_id
			INNER JOIN asset_dependency_bundle adb ON adb.asset_id = ae.asset_entity_id
			LEFT OUTER JOIN person p ON p.person_id IN (app.sme_id, app.sme2_id, ae.app_owner_id)
			WHERE adb.project_id = ${projectId}
		""")
		if (depGroup == 'onePlus') {
			query.append(' AND adb.dependency_bundle > 0')
		} else if (depGroup != null) {
			query.append(' AND adb.dependency_bundle = ' + depGroup )
		}

		if (personId) {
			query.append(' AND p.person_id = ' + personId)
		}

		// TODO : SECURITY - SQL INJECTION

		List<Map> groupAssets = jdbcTemplate.queryForList(query.toString())

		List assetList = []
		java.util.regex.Pattern nameRegex
		try {
			nameRegex = ~"${nameFilter}"
		} catch (e) {
			throw new InvalidParamException('The search was an invalid regex expression')
		}

		for (asset in groupAssets) {
			if (isRegex && asset.assetName.matches(nameRegex)) {
				assetList << asset.assetId
			} else if (!isRegex && asset.assetName.toUpperCase().contains(nameFilter)) {
				assetList << asset.assetId
			}
		}

		// send the results back to the client as JSON
		render assetList as JSON
	}
}