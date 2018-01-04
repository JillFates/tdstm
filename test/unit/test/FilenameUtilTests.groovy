package test

import com.tdsops.tm.enums.FilenameFormat
import com.tdssrc.grails.FilenameUtil
import com.tdssrc.grails.TimeUtil
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Project
import spock.lang.See
import spock.lang.Shared

/**
 * Created by ecantu on 12/11/2017.
 */
@TestMixin(ControllerUnitTestMixin)
class FilenameUtilTests extends AbstractUnitSpec {

   @Shared
   String fileExtension = 'xlsx'
   @Shared
   Date aDate
   @Shared
   PartyGroup company
   @Shared
   Project completeProject
   @Shared
   Project missingPropertiesProject


   def setupSpec() {
      // a Company
      company = new PartyGroup(name:'ABC Company')
      // a Project with all the required properties for file naming
      completeProject = new Project(name:'Test Project', projectCode: 'Big Move', client: company)
      // a Project with some required properties missing (projectCode is not present)
      missingPropertiesProject = new Project(name:'Test Project', client: company)
   }

    @See('TM-8124')
    def '01. Test file name for CLIENT_PROJECT_EVENT_DATE format'() {
       given: 'a Project, Move Event and date with the corresponding values'
            MoveEvent me = new MoveEvent([project:completeProject, name: 'ERP Event'])
            aDate = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
            def params = [project:completeProject, moveEvent:me]
       expect: 'the resulting file name for CLIENT_PROJECT_EVENT_DATE format match the expected file naming scheme'
            'ABC_Company-Big_Move-ERP_Event-20141020_2215.xlsx' == FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, params, fileExtension, aDate)
    }

    @See('TM-8124')
    def '02. Test bad or incomplete properties for CLIENT_PROJECT_EVENT_DATE format'() {
        given: 'a Project and Move Event with incomplete values (projectCode is missing)'
            MoveEvent me = new MoveEvent([project:missingPropertiesProject, name: 'ERP Event'])
            aDate = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
            def params = [project:missingPropertiesProject, moveEvent:me]
        expect: 'the resulting file name for CLIENT_PROJECT_EVENT_DATE format is empty, and not a corrupted filename'
            '' == FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, params, fileExtension, aDate)
    }

    @See('TM-8124')
    def '03. Test ALL events for CLIENT_PROJECT_EVENT_DATE format'() {
        given: 'a Project with the corresponding values, with ALL events for Event_Name'
            aDate = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
            def params = [project:completeProject, allEvents: true] // the allEvents param is flagged true
        expect: 'the resulting file name for CLIENT_PROJECT_EVENT_DATE match the expected file naming scheme, with ALL for Event_Name'
            'ABC_Company-Big_Move-ALL-20141020_2215.xlsx' == FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, params, fileExtension, aDate)
    }
}
