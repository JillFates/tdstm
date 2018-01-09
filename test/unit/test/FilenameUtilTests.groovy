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

    @See('TM-8124, TM-8125')
    def '01. Test file name for CLIENT_PROJECT_EVENT_DATE format'() {
       given: 'a Project, Move Event and date with the corresponding values'
            MoveEvent me = new MoveEvent([project:completeProject, name: 'ERP Event'])
            aDate = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
            def params = [project:completeProject, moveEvent:me]
       expect: 'the resulting file name match the expected file naming scheme'
            'ABC_Company-Big_Move-ERP_Event-20141020_2215.xlsx' == FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, params, fileExtension, aDate)
    }

    @See('TM-8124, TM-8125')
    def '02. Test bad or incomplete properties for CLIENT_PROJECT_EVENT_DATE format'() {
        given: 'a Project and Move Event with incomplete values (projectCode is missing)'
            MoveEvent me = new MoveEvent([project:missingPropertiesProject, name: 'ERP Event'])
            aDate = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
            def params = [project:missingPropertiesProject, moveEvent:me]
        expect: 'the resulting file name is empty, and not a corrupted filename'
            '' == FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, params, fileExtension, aDate)
    }

    @See('TM-8124, TM-8125')
    def '03. Test ALL events for CLIENT_PROJECT_EVENT_DATE format'() {
        given: 'a Project with the corresponding values, with ALL events for Event_Name'
            aDate = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
            def params = [project:completeProject, allEvents: true] // the allEvents param is flagged true
        expect: 'the resulting file name match the expected file naming scheme, with ALL for Event_Name'
            'ABC_Company-Big_Move-ALL-20141020_2215.xlsx' == FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, params, fileExtension, aDate)
    }

    @See('TM-8825, TM-8125')
    def '04. Test multiple events for CLIENT_PROJECT_EVENT_DATE format'() {
       given: 'a Project with the corresponding values and with multiple events (but not ALL)'
       MoveEvent me1 = new MoveEvent([project:completeProject, name: 'M1-Hybrid'])
       MoveEvent me2 = new MoveEvent([project:completeProject, name: 'M2-Physical'])
       def events = [me1, me2]
       def date = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
       def params = [project:completeProject, moveEvent: events] // the moveEvent param receives a list with the events
       expect: 'the resulting file name match the expected file naming scheme, with the event names concatenated'
       'ABC_Company-Big_Move-M1-Hybrid-M2-Physical-20141020_2215.xlsx'== FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, params, fileExtension, date)
    }

   @See('TM-7958, TM-8097')
   def '05. Test file name for CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE format'() {
      given: 'a Project, Move Bundle and date with the corresponding values'
         MoveBundle mb = new MoveBundle([project:completeProject, name: 'ERP Assets'])
         def date = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
      def params = [project:completeProject, moveBundle:mb]
      expect: 'the resulting file name match the expected file naming scheme'
      'ABC_Company-Big_Move-ERP_Assets-20141020_2215.xlsx' == FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE, params, fileExtension, date)

   }

   @See('TM-7958, TM-8097')
   def '06. Test bad or incomplete properties for CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE format'() {
      given: 'a Project and Move Bundle with incomplete values (projectCode is missing)'
         MoveBundle mb = new MoveBundle([project:missingPropertiesProject, name: 'ERP Assets'])
         def date = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
         def params = [project:missingPropertiesProject, moveBundle:mb]
      expect: 'the resulting file name is empty, and not a corrupted filename'
         '' == FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE, params, fileExtension, date)
   }

   @See('TM-8124, TM-8125')
   def '07. Test ALL_BUNDLES for CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE format'() {
      given: 'a Project with the corresponding values and no particular move bundle, with ALL_BUNDLES for Bundle Name'
         def date = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
         def params = [project:completeProject, allBundles: true] // the allBundles param is flagged true
      expect: 'the resulting file name match the expected file naming scheme, with ALL_BUNDLES for Bundle Name'
         'ABC_Company-Big_Move-ALL_BUNDLES-20141020_2215.xlsx' == FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE, params, fileExtension, date)
   }

   @See('TM-7958, TM-8097')
   def '08. Test PLANNING for CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE format'() {
      given: 'a Project with the corresponding values and no particular move bundle, with PLANNING for Bundle Name'
         MoveBundle mb = new MoveBundle([project:completeProject, name: 'ERP Assets'])
         def date = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
         def params = [project:completeProject, moveBundle:mb, useForPlanning: true] // the useForPlanning param is flagged true
      expect: 'the resulting file name match the expected file naming scheme, with PLANNING for Bundle Name'
      'ABC_Company-Big_Move-PLANNING-20141020_2215.xlsx'== FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE, params, fileExtension, date)
   }

   @See('TM-7958, TM-8097')
   def '09. Test multiple bundles for CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE format'() {
      given: 'a Project with the corresponding values and with multiple bundles (but not ALL_BUNDLES)'
      MoveBundle mb1 = new MoveBundle([project:completeProject, name: 'M1-Hybrid'])
      MoveBundle mb2 = new MoveBundle([project:completeProject, name: 'M2-Physical'])
      def bundles = [mb1, mb2]
      def date = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
      def params = [project:completeProject, moveBundle: bundles] // the moveBundle param receives a list with the bundles
      expect: 'the resulting file name match the expected file naming scheme, with the bundle names concatenated'
      'ABC_Company-Big_Move-M1-Hybrid-M2-Physical-20141020_2215.xlsx'== FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE, params, fileExtension, date)
   }
}
