package test

import com.tdsops.tm.enums.FilenameFormat
import com.tdssrc.grails.FilenameUtil
import com.tdssrc.grails.TimeUtil
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.project.Project
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
			expect: 'the resulting file name match the expected file naming scheme, with just the number of events for the EVENT part'
				'ABC_Company-Big_Move-2_events-20141020_2215.xlsx'== FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, params, fileExtension, date)
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
			expect: 'the resulting file name match the expected file naming scheme, with just the number of bundles for the PROJECT_BUNDLE part'
				'ABC_Company-Big_Move-2_bundles-20141020_2215.xlsx'== FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE, params, fileExtension, date)
		}

		@See('TM-7872')
		def '10. Test file name for PROJECT_VIEW_DATE format'() {
			given: 'a Project and View Name with the corresponding values'
				def viewName = 'My Applications'
				aDate = TimeUtil.parseDateTime('10/20/2014 10:15 PM')
				def params = [project:completeProject, viewName: viewName]
			expect: 'the resulting file name match the expected file naming scheme'
				'Big_Move-My_Applications-20141020_2215.xlsx' == FilenameUtil.buildFilename(FilenameFormat.PROJECT_VIEW_DATE, params, fileExtension, aDate)
		}

		@See('TM-7872')
		def '11. Test file name for PROJECT_VIEW_DATE format, without date and extension'() {
			given: 'a Project and View Name with the corresponding values, and we use the excludeDate param'
			def viewName = 'My Applications'
			def params = [project:completeProject, viewName: viewName, excludeDate: true]
			expect: 'the resulting file name match the expected file naming scheme'
			'Big_Move-My_Applications' == FilenameUtil.buildFilename(FilenameFormat.PROJECT_VIEW_DATE, params)
		}

	def '12. Test sanitation for safeFilename method'() {
		// while not touching the ASCII printible characters. This will remove the typical CR, LF, BS
		// along with Unicode Control characters, Line and Paragraph separators, etc {

		expect:
		FilenameUtil.safeFilename(value) == result

		where:
		value               | result
		" abcdefghijklm "   | 'abcdefghijklm'
		" nopqrstuvwxyz "   | 'nopqrstuvwxyz'
		" ABCDEFGHIJKLM "   | 'ABCDEFGHIJKLM'
		" NOPQRSTUVWXYZ "   | 'NOPQRSTUVWXYZ'
		" 01234567890 "     | '01234567890'
		"!@#\$%^&*()-_=+`~" | '_____^__()-____~'
		"',.<>/?\\"         | '\',._____'
		" CR\r. "           | 'CR_.'
		" LF\n. "           | 'LF_.'
		" FF\f. "           | 'FF_.'
		" TAB\t. "          | 'TAB_.'
		" DQuote\". "       | 'DQuote_.'
		' SQuote\'. '       | 'SQuote\'.'
		" \t White\t. \t "  | 'White_.'
		" .\bBACKSPACE. "   | '.~BACKSPACE.'
		" .\u2028LineSep"   | '.~LineSep'
		" .\u2029ParaSep"   | '.~ParaSep'
		" .\u00000000. "    | '.~0000.'
		" .\u00090009. "    | '._0009.'
		" .\u00850085. "    | '.~0085.'
		" [\u007f007f] "    | '[~007f]'
		" [\u008f008f] "    | '[~008f]'
	}

		@See('TM-9050')
		def '13. Test sanitation for invalid filename characters for Windows and Linux filesystems'() {

		expect:
		FilenameUtil.safeFilename(value) == result

		where:
		value		|	result
		'abc#d'		|	'abc_d'
		'abcd<<'	|	'abcd__'
		'ab$cd'		|	'ab_cd'
		'ab%cd'		|	'ab_cd'
		'ab>cd'		|	'ab_cd'
		'ab!cd'		|	'ab_cd'
		'ab`cd'		|	'ab_cd'
		'ab&cd'		|	'ab_cd'
		'ab*cd'		|	'ab_cd'
		'ab“cd'		|	'ab_cd'
		'ab|cd'		|	'ab_cd'
		'ab{cd'		|	'ab_cd'
		'ab?cd'		|	'ab_cd'
		'ab”cd'		|	'ab_cd'
		'ab}cd'		|	'ab_cd'
		'ab/cd'		|	'ab_cd'
		'ab:cd'		|	'ab_cd'
		'ab\\bcd'	|	'ab_cd'
		'ab=cd'		|	'ab_cd'
		'ab@cd'		|	'ab_cd'
		'ab+cd'		|	'ab_cd'
		'ab"cd'		|	'ab_cd'

	}
}
