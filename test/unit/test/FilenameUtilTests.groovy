package test

import com.tdsops.tm.enums.FilenameFormat
import com.tdssrc.grails.FilenameUtil
import com.tdssrc.grails.TimeUtil
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import net.transitionmanager.domain.MoveBundle
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

    @See('TM-8124, TM-8125')
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

    @See('TM-8124, TM-8125')
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

    @See('TM-8124, TM-8125')
    def '03. Test ALL_EVENTS for file name format 1'() {
        given: 'a Project with the corresponding values and no particular move event'
            String fileExtension = 'xlsx'
            PartyGroup company = new PartyGroup(name:'ABC Company')
            Project project = new Project(name:'Test Project', projectCode: 'Big Move', client: company)
            def date = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
        expect: 'when passing allEvents parameter, the resulting file name for format 1 match the expected file naming scheme with ALL_EVENTS for Event_Name'
            def filename = FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, [project:project, allEvents: true], fileExtension, date)
            filename == 'ABC_Company-Big_Move-ALL_EVENTS-20141020_2215.xlsx'
    }

   @See('TM-7958, TM-8097')
   def '04. Test file name format 2'() {
      given: 'a Project and Move Bundle with the corresponding values'
         String fileExtension = 'xlsx'
         PartyGroup company = new PartyGroup(name:'ABC Company')
         Project project = new Project(name:'Test Project', projectCode: 'Big Move', client: company)
         MoveBundle mb = new MoveBundle([project:project, name: 'ERP Assets'])
         def date = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
      expect: 'the resulting file name for format 2 match the expected file naming scheme'
         def filename = FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE, [project:project, moveBundle:mb], fileExtension, date)
         filename == 'ABC_Company-Big_Move-ERP_Assets-20141020_2215.xlsx'
   }

   @See('TM-7958, TM-8097')
   def '05. Test bad or incomplete properties for file name format 2'() {
      given: 'a Project and Move Bundle with incomplete values (projectCode is missing)'
         String fileExtension = 'xlsx'
         PartyGroup company = new PartyGroup(name:'ABC Company')
         Project project = new Project(name:'Test Project', client: company)
         MoveBundle mb = new MoveBundle([project:project, name: 'ERP Assets'])
         def date = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
      expect: 'the resulting file name for format 2 is empty, and not a corrupted filename'
         def filename = FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE, [project:project, moveBundle:mb], fileExtension, date)
         filename == ''
   }

   @See('TM-8124, TM-8125')
   def '06. Test ALL_BUNDLES for file name format 2'() {
      given: 'a Project with the corresponding values and no particular move bundle'
         String fileExtension = 'xlsx'
         PartyGroup company = new PartyGroup(name:'ABC Company')
         Project project = new Project(name:'Test Project', projectCode: 'Big Move', client: company)
         def date = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
      expect: 'when passing allBundles parameter, the resulting file name for format 2 match the expected file naming scheme with ALL_BUNDLES for Bundle_Name'
         def filename = FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE, [project:project, allBundles: true], fileExtension, date)
         filename == 'ABC_Company-Big_Move-ALL_BUNDLES-20141020_2215.xlsx'
   }

   @See('TM-7958, TM-8097')
   def '07. Test PLANNING for file name format 2'() {
      given: 'a Project and Move Bundle with the corresponding values'
         String fileExtension = 'xlsx'
         PartyGroup company = new PartyGroup(name:'ABC Company')
         Project project = new Project(name:'Test Project', projectCode: 'Big Move', client: company)
         MoveBundle mb = new MoveBundle([project:project, name: 'ERP Assets'])
         def date = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
      expect: 'when passing useForPlanning parameter, the resulting file name for format 2 match the expected file naming scheme with PLANNING for Bundle_Name'
         def filename = FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE, [project:project, moveBundle:mb, useForPlanning: true], fileExtension, date)
         filename == 'ABC_Company-Big_Move-PLANNING-20141020_2215.xlsx'
   }
}
