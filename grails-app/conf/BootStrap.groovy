import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.security.AESCodec
import com.tdsops.etl.ETLProcessorResult
import com.tdsops.etl.marshall.AnnotationDrivenObjectMarshaller
import com.tdsops.metaclass.CustomMethods
import com.tdssrc.grails.GormUtil
import grails.converters.JSON
import grails.util.Environment
import net.transitionmanager.domain.DataTransferSet
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventNews
import net.transitionmanager.domain.Notice
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyRelationship
import net.transitionmanager.domain.PartyRelationshipType
import net.transitionmanager.domain.PartyRole
import net.transitionmanager.domain.PartyType
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.ProjectAssetMap
import net.transitionmanager.domain.ProjectTeam
import net.transitionmanager.domain.RoleType
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.domain.UserPreference
import net.transitionmanager.domain.Workflow
import net.transitionmanager.service.AssetEntityAttributeLoaderService
import net.transitionmanager.service.QzSignService
import net.transitionmanager.service.StateEngineService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.LicenseAdminService
import org.apache.log4j.Logger

import java.lang.management.ManagementFactory

class BootStrap {
	AssetEntityAttributeLoaderService assetEntityAttributeLoaderService
	StateEngineService stateEngineService
	TaskService taskService
	QzSignService qzSignService
	LicenseAdminService licenseAdminService

	def init = { servletContext ->
		checkForBlacklistedVMParameters()

		//Check required default Config Info
		checkConfigInfo()

		//initialize exception logger filter to mute GrailsExceptionResolver
		initializeExceptionLoggerFilter()

		CustomMethods.initialize()

		// Load all of the Workflow definitions into the StateEngine service
		Workflow.list().each { wf ->
			stateEngineService.loadWorkflowTransitionsIntoMap(wf.process, 'workflow')
		}

		// Enable the ability to do Rollback to Savepoints
		ApplicationContextHolder.getBean('transactionManager').setNestedTransactionAllowed(true)

		taskService.init()

		// Registers a marshaller for JSON for the Notice domain ( to support notice as JSON )
		Notice.registerObjectMarshaller()

		// Warm up AESCodec
		AESCodec.getInstance()

		// ETLProcessorResult marshaller.
		ETLProcessorResult.registerObjectMarshaller()

		//
		// NOTHING NEEDED IN PRODUCTION SHOULD BE PLACED BELOW HERE
		//
		if (Environment.current == Environment.PRODUCTION) {
			return
		}

		// createInitialData()

		//LOAD TESTS for dev
		//testMemoryAllocation()
	}

	/**
	 * Check Config flags or alert about required information
	 */
	private checkConfigInfo(){
		//Call some methods to show error messages (if any) from Boot time
		qzSignService.getPassphrase()
		qzSignService.findPrivateKeyFile()
	}

	/**
	 * Check Config flags or alert about required information
	 */
	private initializeExceptionLoggerFilter(){

		Logger.rootLogger.allAppenders.each { appender ->
            ExceptionLoggerFilter filter = new ExceptionLoggerFilter()
            filter.loggerClass = "org.codehaus.groovy.grails.web.errors.GrailsExceptionResolver"
            filter.activateOptions()
            appender.addFilter(filter)
		}

	}

	private checkForBlacklistedVMParameters() {
		def blacklist = ["-Xnoclassgc"]

		def inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments()
		def blackListArgs = inputArguments.grep { arg ->
			return blacklist.any { el -> arg.contains(el) }
		}

		if (blackListArgs.size() > 0) {
			log.warn "*** WARNING ***\n BLACK LISTED ARGUMENTS FOUND IN THE CONFIGURATION!\n This can cause unexpected behaviour on the application, please check that you really want to do this: \n\t $blacklist\n***********************************************************"
		}
	}

	/** SOME LOAD TEST ***********/
	/**
	 * This test is to check the memory allocation and the PermGen, don't use in production
	 * related to: TM-4157 (https://support.transitionmanager.com/browse/TM-4157)
	 */
	private testMemoryAllocation() {
		//Use only in development mode
		if (Environment.current != Environment.DEVELOPMENT) return

		log.info "THREAD INICIALIZANDO!!!! LETS KILL THIS GUY"
		Thread.start {
			log.info "Generate a very Big MAP groovy String Code"
			def strMap = "["
			for (int i = 0; i < 1000; i++) {
				if (i > 0) {
					strMap += ","
				}
				strMap += "'$i':'value $i'"
			}
			strMap += "]"

			log.info "sz: ${strMap.size()}"

			for (int i = 10; i > 0; i--) {
				log.info "THREAD will start in T - $i seconds"
				Thread.sleep(1000)
			}

			for (int i = 0; i < 100000; i++) {
				//new Person(firstName:'Octavio', lastName:'Luna',title:'Dev')
				def daMap = Eval.me(strMap)
				Thread.sleep(50)
			}

			log.info "THREAD TERMINADO!"
		}
	}
}
