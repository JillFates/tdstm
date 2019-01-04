import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.NumberUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.dependency.analyzer.FilteredAssetsCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.DependencyAnalyzerService
import org.springframework.jdbc.core.JdbcTemplate
/**
 * Controller used primarily for the Dependency Analyzer
 */
@Secured('isAuthenticated()')
class WsDepAnalyzerController implements ControllerMethods {

	JdbcTemplate jdbcTemplate
	DependencyAnalyzerService dependencyAnalyzerService

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
	 * Returns the list of the ids of the assets that should be highlighted, by the given filter
	 *
	 * @param FilteredAssetsCommand command object holding the following parameters:
	 * nameFilter
	 * isRegex
	 * personId
	 * depGroup - the dependency group number or 'onePlus'.
	 * tagIds tags that should be used as a filter.
	 * tagMatch how the tags ids should be combined in an OR(ANY) or with an AND(All).
	 *
	 * @return A list of assets found by filter as JSON.
	 */
	@HasPermission(Permission.DepAnalyzerView)
	def filteredAssetList () {
		FilteredAssetsCommand filter = populateCommandObject(FilteredAssetsCommand)
		validateCommandObject(filter)

		String projectId = securityService.getUserCurrentProjectId()

		List<Map> groupAssets =  dependencyAnalyzerService.getAssets(filter, projectId)
		List assetList = dependencyAnalyzerService.filterAssets(filter.nameFilter.toUpperCase(), groupAssets, filter.isRegex)

		render assetList as JSON
	}
}
