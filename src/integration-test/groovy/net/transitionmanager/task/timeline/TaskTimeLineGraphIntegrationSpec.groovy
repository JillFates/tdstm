package net.transitionmanager.task.timeline

import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import spock.lang.Shared
import spock.lang.Specification
import test.helper.AssetCommentTestHelper
import test.helper.MoveBundleTestHelper
import test.helper.ProjectTestHelper

@Integration
@Rollback
class TaskTimeLineGraphIntegrationSpec extends Specification {

	@Shared
	MoveBundleTestHelper moveBundleTestHelper = new MoveBundleTestHelper()

	@Shared
	ProjectTestHelper projectTestHelper = new ProjectTestHelper()

	@Shared
	AssetCommentTestHelper assetCommentTestHelper = new AssetCommentTestHelper()

	@Shared
	Project project

	@Shared
	MoveBundle moveBundle

	void setup() {
		project = projectTestHelper.createProject()
		moveBundle = moveBundleTestHelper.createBundle(project, null)
	}

	void 'test can create a TimeLineNodeGraph using AssetComment and TaskDependency'() {

	}

}
