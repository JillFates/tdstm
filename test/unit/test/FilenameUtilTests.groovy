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
import spock.lang.Specification

/**
 * Created by ecantu on 12/11/2017.
 */
@TestMixin(ControllerUnitTestMixin)
class FilenameUtilTests extends AbstractUnitSpec {

    @See('TM-8124')
    def '01. Test file name format 1'() {
        given: 'a Project and Move Event with the corresponding values'
            String fileExtension = 'xlsx'
            PartyGroup company = new PartyGroup(name:'ABC Company')
            Project project = new Project(name:'Test Project', projectCode: 'Big Movie', client: company)
            MoveEvent me = new MoveEvent([project:project, name: 'ERP Event'])
            def date = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
        expect: 'the resulting file name for format 1 match the expected file naming scheme'
            def filename = FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, [project:project, moveEvent:me], fileExtension, date)
                filename == 'ABC_Company-Big_Movie-ERP_Event-20141020_2215.xlsx'
    }

    @See('TM-8124')
    def '02. Test bad or incomplete properties for file name format 1'() {
        given: 'a Project and Move Event with incomplete values (projectCode is missing)'
            String fileExtension = 'xlsx'
            PartyGroup company = new PartyGroup(name:'ABC Company')
            Project project = new Project(name:'Test Project', client: company)
            MoveEvent me = new MoveEvent([project:project, name: 'ERP Event'])
            def date = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
        expect: 'the resulting file name for format 1 is empty, and not a corrupted filename'
            def filename = FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, [project:project, moveEvent:me], fileExtension, date)
            filename == ''
    }

    @See('TM-8124')
    def '03. Test all events for file name format 1'() {
        given: 'a Project with the corresponding values, with ALL events for Event_Name'
            String fileExtension = 'xlsx'
            PartyGroup company = new PartyGroup(name:'ABC Company')
            Project project = new Project(name:'Test Project', projectCode: 'Big Movie', client: company)
            def date = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
        expect: 'the resulting file name for format 1 match the expected file naming scheme, with ALL for Event_Name'
            def filename = FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, [project:project, allEvents: true], fileExtension, date)
            filename == 'ABC_Company-Big_Movie-ALL-20141020_2215.xlsx'
    }
}
