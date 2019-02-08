package test.helper

import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.SettingService
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.jdbc.core.JdbcTemplate

class ApplicationTestHelper {

	Application createApplication(AssetClass assetClass, Project project, MoveBundle moveBundle) {
		Application application = new Application(
			project: project,
			moveBundle: moveBundle,
			assetClass: assetClass,
			assetName: 'Test AssetEntity-' + RandomStringUtils.randomAlphabetic(10)
		)

		application.save(flush: true, failOnError: true)

		return application
	}

	/**
	 * Create Application Asset if not exists from given Map for E2EProjectSpec to persist at server DB
	 * @param: [REQUIRED] assetData = [name: String, planStatus: String, environment: String]
	 * @param: project
	 * @param: moveBundle
	 * @returm the application
	 */
	Application createApplication(Map assetData, Project project, MoveBundle moveBundle) {
		Application existingApplication = Application.findWhere([assetName: assetData.name, project: project])
		if(!existingApplication) {
			Application application
			Application.withTransaction {
				application = new Application(
						project: project,
						moveBundle: moveBundle,
						assetName: assetData.name,
						planStatus: assetData.planStatus ? assetData.planStatus : 'Unassigned',
						environment: assetData.environment ? assetData.environment : 'Production'
				)
				application.save(flush: true)
			}
			return application
		} else {
			return existingApplication
		}
	}
}