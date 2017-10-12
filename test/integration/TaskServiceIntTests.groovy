import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdssrc.eav.EavAttributeSet
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.GormUtil
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project

class TaskServiceIntTests extends IntegrationSpec {

	def project
	def attributeSet
	def moveEvent
	def moveBundle
	def taskService

	void "test clean task data"() {
		setup:
			prepareMoveEventData()
			def whom = new Person(firstName:'Robin', lastName:'Banks').save()

		when:
			def listAssetEntityNotNull = AssetComment.findAllByAssetEntityIsNotNull()

		then:
			listAssetEntityNotNull.each { task ->
				task = taskService.setTaskStatus( task, AssetCommentStatus.STARTED, whom )
				task.actStart != null
				task.assignedTo != null
				AssetCommentStatus.STARTED == task.status
				task.actFinish == null
				0 == task.isResolved

				// Test bumping status to DONE after STARTED
//				task.previousStatus = task.status
				taskService.setTaskStatus(task, AssetCommentStatus.DONE, whom)
				!task.actStart
				!task.actFinish
				!task.assignedTo
				!task.resolvedBy
				AssetCommentStatus.DONE == task.status
				1 == task.isResolved
			}

		/*
		def eventTask = taskService.setTaskStatus( AssetComment.findByMoveEventIsNotNull(), AssetCommentStatus.STARTED, whom )
		assertNotNull eventTask.actStart
		assertNotNull eventTask.assignedTo
		assertEquals AssetCommentStatus.STARTED, eventTask.status
		assertNull eventTask.actFinish
		assertEquals 0, eventTask.isResolved

		taskService.cleanTaskData( moveEvent.id )

		AssetComment.findAllByAssetEntityIsNotNull().each { task->
			assertEquals AssetCommentStatus.PENDING, task.status
			assertNull task.actStart
			assertNull task.actStart
			assertNull task.dateResolved
			assertNull task.resolvedBy
			assertEquals 0, task.isResolved
		}

		eventTask = AssetComment.findByMoveEvent(moveEvent)
		assertEquals AssetCommentStatus.READY, eventTask.status
		assertNull eventTask.actStart
		assertNull eventTask.actStart
		assertNull eventTask.dateResolved
		assertNull eventTask.resolvedBy
		assertEquals 0, eventTask.isResolved
		*/
	}

/*
	void testDeleteTaskData() {
		setUp()
		prepareMoveEventData()
		def whom = new Person(firstName:'Robin', lastName:'Banks')
		AssetComment.findAllByAssetEntityIsNotNull().each { task->
			task = taskService.setTaskStatus( task, AssetCommentStatus.STARTED, whom )
			assertNotNull task.actStart
			assertNotNull task.assignedTo
			assertEquals AssetCommentStatus.STARTED, task.status
			assertNull task.actFinish
			assertEquals 0, task.isResolved

			// Test bumping status to DONE after STARTED
			task.previousStatus = task.status
			taskService.setTaskStatus( task, AssetCommentStatus.DONE, whom )
			assertNotNull task.actStart
			assertNotNull task.actFinish
			assertNotNull task.assignedTo
			assertNotNull task.resolvedBy
			assertEquals AssetCommentStatus.DONE, task.status
			assertEquals 1, task.isResolved
		}

		def eventTask = taskService.setTaskStatus( AssetComment.findByMoveEventIsNotNull(), AssetCommentStatus.STARTED, whom )
		assertNotNull eventTask.actStart
		assertNotNull eventTask.assignedTo
		assertEquals AssetCommentStatus.STARTED, eventTask.status
		assertNull eventTask.actFinish
		assertEquals 0, eventTask.isResolved

		taskService.deleteTaskData( moveEvent.id )

		def assetEntitys = AssetEntity.findAllByMoveBundle( moveBundle )

		assertEquals 0, AssetComment.countByAssetEntityInList(assetEntitys)

		assertNull AssetComment.findByMoveEvent(moveEvent)

		assertNull TaskDependency.read()

		assertNull CommentNote.read()
	}
*/

	/* Test SetUp */
    private setup() {
		log.info "***********DA SETUP***********************************************"
		project = new Project(name:"VM", projectCode: "VM", workflowCode:"STD_PROCESS" ).save()
		def entityType = new EavEntityType( entityTypeCode:'AssetEntity', domainName:'AssetEntity', isAuditable:1  ).save()
		attributeSet = new EavAttributeSet( attributeSetName:'Server', entityType:entityType, sortOrder:10 ).save()
    }

	private prepareMoveEventData(){
		moveEvent = new MoveEvent(name:"Example 1", project:project, inProgress:'false').save()
		moveBundle = new MoveBundle(name:'Example 1', moveEvent:moveEvent, project:project).save()
		// Create assets for Bundle
		AssetType.values().each {
			def assetEntity = new AssetEntity(
					assetName:it.toString(),
					assetType:it.toString(),
					assetTag:'TAG-'+it.toString(),
					moveBundle:moveBundle,
					project:project,
					attributeSet:attributeSet
			)
			if ( !assetEntity.validate() || !assetEntity.save() ) {
				def etext = "Unable to create assetEntity" +
							GormUtil.allErrorsString( assetEntity )
				println etext
			}
		}

		// Create Tasks for AssetEntity
		AssetEntity.list()  // <==
/*				.each { assetEntity->
			def assetComment = new AssetComment(
					comment:"Sample for "+assetEntity.toString(),
					commentType:'issue',
					assetEntity :assetEntity
			)
			if ( !assetComment.validate() || !assetComment.save() ) {
				def etext = "Unable to create assetComment" +
				GormUtil.allErrorsString( assetComment )
				println etext
			}
		}

		def taskForEvent = new AssetComment(
				comment:"Sample for "+moveEvent.toString(),
				commentType:'issue',
				moveEvent :moveEvent
		)
		if ( !taskForEvent.validate() || !taskForEvent.save() ) {
			def etext = "Unable to create taskForEvent" +
			GormUtil.allErrorsString( taskForEvent )
			println etext
		}

		// Create Task Dependency for all Assets tasks as event tasks as predecessor
		AssetComment.findAllByAssetEntityIsNotNull().each { task ->
			def taskDependency = new TaskDependency(
					predecessor:taskForEvent,
					assetComment:task
			)
			if ( !taskDependency.validate() || !taskDependency.save() ) {
				def etext = "Unable to create taskDependency" +
				GormUtil.allErrorsString( taskDependency )
				println etext
			}
 	 	}
 	 	*/
	}

	void "test get cart quantities logging messages are written without error"() {
		given:
		Project project = new Project(name:"VM", projectCode: "VM", workflowCode:"STD_PROCESS", completionDate: new Date() ).save()
		MoveEvent moveEvent = new MoveEvent(name:"Example 1", project:project, inProgress:'false').save()
		MoveBundle moveBundle = new MoveBundle(name:'Example 1', moveEvent:moveEvent, project:project).save()
		EavEntityType entityType = new EavEntityType( entityTypeCode:'TestAssetEntity', domainName:'TestAssetEntity', isAuditable:1  ).save()
		EavAttributeSet attributeSet = new EavAttributeSet( attributeSetName:'Server', entityType:entityType, sortOrder:10 ).save()
		AssetEntity assetEntity = new AssetEntity(
				assetName: 'Test asset entity',
				assetType: 'Test asset type',
				assetTag:'TAG-test',
				moveBundle: moveBundle,
				project: project,
				attributeSet: attributeSet,
				cart: 'Test cart name'
		)
		AssetComment assetComment = new AssetComment(
				comment: "Sample for "+assetEntity.toString(),
				commentType: 'issue',
				assetEntity: assetEntity,
				status: AssetCommentStatus.DONE,
				role: 'CLEANER'
		)

		when:
		assetEntity.save()
		assetComment.save()

		then:
		null != assetEntity

		and:
		def cartQuantities = taskService.getCartQuantities(moveEvent, assetEntity.cart)

		then:
		null != cartQuantities

	}
}
