import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.Color
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.command.dependency.analyzer.FilteredAssetsCommand
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagAsset
import net.transitionmanager.service.DependencyAnalyzerService
import net.transitionmanager.service.FileSystemService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.TagAssetService
import net.transitionmanager.service.TagService
import spock.lang.Shared
import spock.lang.Specification
import test.helper.AssetEntityTestHelper

@Integration
@Rollback
class DependencyAnalyzerServiceIntegrationSpec extends  Specification{
	TagService                tagService
	TagAssetService           tagAssetService
	DependencyAnalyzerService dependencyAnalyzerService

	@Shared
	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()

	@Shared
	FileSystemService fileSystemService

	@Shared
	test.helper.MoveBundleTestHelper moveBundleTestHelper = new test.helper.MoveBundleTestHelper()

	@Shared
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()

	@Shared
	Project project

	@Shared
	Project otherProject

	/**
	 * A move bundle that is usedForPlanning = 1
	 */
	@Shared
	MoveBundle moveBundle

	/**
	 * A move bundle that is usedForPlanning = 0
	 */
	@Shared
	MoveBundle moveBundle2

	/**
	 * a device in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetEntity device

	/**
	 * a device in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetEntity device2

	/**
	 * a device in moveBundle2(usedForPlanning = 0)
	 */
	@Shared
	AssetEntity device3

	@Shared
	Map context

	@Shared
	Tag tag1

	@Shared
	Tag tag2

	@Shared
	Tag tag3

	@Shared
	TagAsset tagAsset1

	@Shared
	TagAsset tagAsset2

	@Shared
	TagAsset tagAsset3

	@Shared
	TagAsset tagAsset4

	@Shared
	Date now

	void setup() {
		project = projectTestHelper.createProject()
		otherProject = projectTestHelper.createProject()
		moveBundle = moveBundleTestHelper.createBundle(project, null)
		moveBundle2 = moveBundleTestHelper.createBundle(otherProject, null)

		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device3 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, otherProject, moveBundle2)

		AssetDependency dep1 = new AssetDependency(asset: device, dependent: device2).save(flush: true)
		Integer dependencyBundle = 4
		AssetDependencyBundle adb1 = new AssetDependencyBundle(project: project, asset: device, dependencySource: 'the source', dependencyBundle: dependencyBundle).save(flush: true)
		AssetDependencyBundle adb2 = new AssetDependencyBundle(project: project, asset: device2, dependencySource: 'the source', dependencyBundle: dependencyBundle).save(flush: true)

		tag1 = new Tag(name: 'grouping assets', description: 'This is a description', color: Color.Green, project: project).save(flush: true)
		tag2 = new Tag(name: 'some assets', description: 'Another description', color: Color.Blue, project: project).save(flush: true)
		tag3 = new Tag(name: 'other', description: 'Yet another description', color: Color.Red, project: otherProject).save(flush: true)

		tagAsset1 = new TagAsset(tag: tag1, asset: device).save(flush: true)
		tagAsset2 = new TagAsset(tag: tag1, asset: device2).save(flush: true)
		tagAsset3 = new TagAsset(tag: tag2, asset: device2).save(flush: true)
		tagAsset4 = new TagAsset(tag: tag3, asset: device3).save(flush: true)

		tagService.securityService = [
			getUserCurrentProject  : { -> project },
			getUserCurrentProjectId: { -> "$project.id".toString() }
		] as SecurityService

		tagAssetService.securityService = [
			getUserCurrentProject  : { -> project },
			getUserCurrentProjectId: { -> "$project.id".toString() }
		] as SecurityService

		now = TimeUtil.nowGMT().clearTime()
	}

	void 'Test getAssets no tags'() {
		when: 'Getting a list of tagAssets by asset'
			FilteredAssetsCommand filter = new FilteredAssetsCommand(depGroup: '4')
			List<Map> assets = dependencyAnalyzerService.getAssets(filter, "$project.id")

		then: 'a list of tagAssets are returned for the asset'
			assets.size() == 2

			assets[0].assetId == device.id
			assets[1].assetId == device2.id
	}

	void 'Test getAssets tag1'() {
		when: 'Getting a list of tagAssets by asset'
			FilteredAssetsCommand filter = new FilteredAssetsCommand(depGroup: '4', tagIds: [tag1.id])
			List<Map> assets = dependencyAnalyzerService.getAssets(filter, "$project.id")

		then: 'a list of tagAssets are returned for the asset'
			assets.size() == 2

			assets[0].assetId == device.id
			assets[1].assetId == device2.id
	}

	void 'Test getAssets tag2'() {
		when: 'Getting a list of tagAssets by asset'
			FilteredAssetsCommand filter = new FilteredAssetsCommand(depGroup: '4', tagIds: [tag2.id])
			List<Map> assets = dependencyAnalyzerService.getAssets(filter, "$project.id")

		then: 'a list of tagAssets are returned for the asset'
			assets.size() == 1

			assets[0].assetId == device2.id
	}


	void 'Test getAssets tag3'() {
		when: 'Getting a list of tagAssets by asset'
			FilteredAssetsCommand filter = new FilteredAssetsCommand(depGroup: '4', tagIds: [tag3.id])
			List<Map> assets = dependencyAnalyzerService.getAssets(filter, "$project.id")

		then: 'a list of tagAssets are returned for the asset'
			assets == []
	}


}