import asset.pipeline.grails.AssetResourceLocator
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.ProjectLogo
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UserPreferenceService
import org.apache.commons.codec.net.URLCodec
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.validator.routines.UrlValidator
import grails.web.mapping.LinkGenerator
import org.springframework.beans.factory.InitializingBean
import net.transitionmanager.service.LicenseCommonService
import net.transitionmanager.service.LicenseAdminService

import asset.pipeline.grails.AssetProcessorService
import java.sql.Timestamp
import java.text.DateFormat

class CustomTagLib implements InitializingBean {

	private static final String[] schemes = ['HTTP', 'http','HTTPS', 'https', 'FTP', 'ftp', 'FTPS',
	                                         'ftps', 'SMB', 'smb', 'FILE', 'file'] as String[]

	private static String faviconStr

	static String namespace = 'tds'
	static returnObjectForTags = ['currentMoveBundle', 'currentMoveBundleId', 'currentMoveEvent', 'currentMoveEventId',
	                              'currentPerson', 'currentPersonName', 'currentProject', 'currentProjectId',
	                              'currentProjectMoveEvents', 'dateFormat', 'isIE6', 'isIE7', 'isMobile',
	                              'minPasswordLength', 'partyGroup', 'powerType', 'preferenceValue', 'setImage',
	                              'startPage', 'timeZone', 'userLogin']

	AssetProcessorService assetProcessorService

	LinkGenerator grailsLinkGenerator
	SecurityService securityService
	UserPreferenceService userPreferenceService
	LicenseCommonService licenseCommonService
	LicenseAdminService  licenseAdminService

	/**
	 * Adjusts a date to a specified timezone and format to the default (yyyy-MM-dd  kk:mm:ss) or one specified.
	 *
	 * @attr date  the date to be formatted
	 * @attr format  the String format to use to format the date into a string (CURRENTLY NOT USED)
	 * @attr endian  the ENDIAN format to use, fallbacks into session then in default
	 */
	def convertDate = { Map attrs ->
		if (!isDateOrTimestamp(attrs.date)) return

		Date dateValue = attrs.date
		dateValue.clearTime()

		String endian = attrs.endian ?: userPreferenceService.dateFormat
		DateFormat formatter = TimeUtil.createFormatterForType(endian, TimeUtil.FORMAT_DATE)

		out << TimeUtil.formatDateTimeWithTZ(TimeUtil.defaultTimeZone, dateValue, formatter)
	}

	/*
	 * Converts a date to User's Timezone and applies formatting.
	 * @attr date  the date to be formatted
	 * @attr format  the format to use
	 */
	def convertDateTime = { Map attrs, body ->
		if (!isDateOrTimestamp(attrs.date)) return

		DateFormat formatter = TimeUtil.createFormatter(attrs.format) ?:
		                       TimeUtil.createFormatter(TimeUtil.FORMAT_DATE_TIME)

		out << TimeUtil.formatDateTime((Date) attrs.date, formatter)
	}

	def truncate = { Map attrs ->
		if (!attrs.value) return

		String value = attrs.value
		if (value.size() > 50) {
			out << '"' + value.substring(0, 50) + '.."'
		} else {
			out << '"' + value + '"'
		}
	}

	/*
	 * Convert seconds into HH:MM format
	 * value should be in seconds
	 */
	def formatIntoHHMMSS = { Map attrs ->
		if (!(attrs.value instanceof Integer)) return

		int value = attrs.value
		int hours = (int) (value / 3600)
		String formatted = hours >= 10 ? hours.toString() : '0' + hours
		int minutes = (int) ((value % 3600) / 60)
		formatted += ":" + (minutes >= 10 ? minutes : '0' + minutes)

		out << formatted
	}

	/**
	 * Outputs the elapsed duration between two times in an Ago shorthand.
	 * @param Date	a start datetime
	 * @param Date	an ending datetime
	 */
	def elapsedAgo = { Map attrs ->
		def start = attrs.start
		def end = attrs.end

		if (!(start instanceof Date) || !(end instanceof Date)) {
			out << ''
		} else {
			out << TimeUtil.ago((Date) start, (Date) end)
		}
	}

	/**
	 * Used to generate an HTML Action Button
	 * @param label - text to display in Button
	 * @param icon - CSS icon to display in button
	 * @param id - CSS id to embed into IDs
	 * @param onclick - Javascript to add to button
	 * @param tooltipText - text for tooltip on hover to add to Button
	 */
	def actionButton = { Map attrs ->
		out << HtmlUtil.actionButton(attrs.label, attrs.icon, attrs.id, attrs.onclick, attrs.tooltipText)
	}

	/**
	 * Used to output text as URL if it matches or straight text otherwise
	 * @param text - text or URL to be displayed, for URL if there is a pipe (|) character the pattern will be (label | url) (required)
	 * @param target - set the A 'target' tag appropriately (optional)
	 * @param class - when presented it will be added to the style if it is a link (optional)
	 */
	def textAsLink = { Map attrs ->
		String text = attrs.text
		String target = attrs.target ?: ''
		String css = attrs.class ?: ''
		String url
		String label
		boolean isUrl = false

		if (text) {
			UrlValidator urlValidator = new UrlValidator(schemes)

			// Attempt to split the URL from the label
			List<String> tokens = text.tokenize('|')
			url = tokens.size() > 1 ? tokens[1] : tokens[0]
			label = tokens[0]
			isUrl = urlValidator.isValid(url)
			if (!isUrl) {
				if (url.startsWith('\\\\')) {
					// Handle UNC (\\host\share\file) which needs to be converted to file
					isUrl = true
					url = 'file:' + url.replaceAll('\\\\', '/')
				} else {
					if (url ==~ /^[A-z]:\\.*/) {
						// Handle windows directory reference do a drive letter
						isUrl = true
						url = 'file://' + new URLCodec().encode(url.replace("\\", '/'))
					}
				}
			}
		}

		if (isUrl) {
			out << '<a href="' + url + '"'
			if (target) {
				out << ' target="' << target << '"'
			}
			if (css) {
				out << ' class="' << css << '"'
			}
			out << '>' << label << '</a>'
		}
		else if (text) {
			out << text
		}
	}

	/**
	 * Used to adjust a date to a specified timezone and format to the default (yyyy-MM-dd  kk:mm:ss) or one specified
	 */
	def select = { Map attrs ->
		def id = attrs.id
		def name = attrs.name
		def clazz = attrs.class
		def onchange = attrs['ng-change']
		def ngModel = attrs['ng-model']
		def ngShow = attrs['ng-show']
		def ngDisabled = attrs['ng-disabled']
		def datasource = attrs.datasource

		def from = attrs.from
		def optionKey = attrs.optionKey
		def optionValue = attrs.optionValue
		def noSelection = attrs.noSelection
		def required = attrs.required

		if (noSelection instanceof Map) {
			noSelection = noSelection.entrySet().iterator().next()
		}

		out << "<select "
		if (name) {
			out << 'name="' << name << '" '
		}
		if (id) {
			out << "id=\"$id\" "
		}
		if (clazz) {
			out << "class=\"$clazz\" "
		}
		if (onchange) {
			out << "ng-change=\"$onchange\" "
		}
		if (ngModel) {
			out << "ng-model=\"$ngModel\" "
		}
		if (ngShow) {
			out << "ng-show=\"$ngShow\" "
		}
		if (ngDisabled) {
			out << "ng-disabled=\"$ngDisabled\" "
		}
		if (required) {
			out << "required "
		}

		if (from && datasource) {
			boolean first = true
			def label

			out << "ng-init=\"$datasource=["
			from.eachWithIndex { el, i ->
				def keyValue

				if (optionKey) {
					if (optionKey instanceof Closure) {
						keyValue = optionKey(el)
					} else if (el != null && optionKey == 'id' && GormUtil.getDomainClass(el.getClass())) {
						keyValue = el.ident()
					} else {
						keyValue = el[optionKey]
					}
				}  else {
					keyValue = el
				}

				label = ""
				def targetLabel = ""
				if (optionValue) {
					if (optionValue instanceof Closure) {
						targetLabel = optionValue(el).toString()
					} else {
						targetLabel = el[optionValue].toString()
					}
				} else {
					targetLabel = el.toString()
				}

				targetLabel = targetLabel.replaceAll("'", "\\\\'")
				label = targetLabel.encodeAsHTML()


				if (!first) {
					out << ","
				}
				out << "{ v: \'$keyValue\',l:\'$label\'} "
				first = false
			}
			out << "]\""
		}

		out << ">"
		out.println()

		if (noSelection) {
			out << '<option value="' << (noSelection.key == null ? "" : noSelection.key) << '"'
			out << '>' << noSelection.value.encodeAsHTML() << '</option>'
			out.println()
		}

		if (from && datasource) {
			out << "<option ng-selected=\"{{$ngModel == item.v}}\" value={{item.v}} ng-repeat=\"item in $datasource\">{{item.l}} </option> "
		}

		out << "</select>"
	}

	/*
	 * Draw an SVG Icon from the source based on the SVG Name
	 * Also apply regex to prevent directory traversal
	 * @param name - name of the svg to show on on icons/svg
	 * @param styleClass - to have more control, it attach a class under tds-svg-icons domain to modify the element as desired
	 * @param width - default as 0 if no provided
	 * @param height - default as 0 if no provided
	 */
	def svgIcon = { Map attrs ->
		String name = attrs.name
		if (!name) {
			return
		}

		long height = NumberUtil.toPositiveLong(attrs.height, 0)
		long width = NumberUtil.toPositiveLong(attrs.width, 0)
		name = name.replaceAll(/\./, "")
		String filename = "icons/svg/${name}.svg"
		String url = assetProcessorService.asset([file:filename], grailsLinkGenerator)

		if (url) {
			out << "<svg style='" << (height > 0 ? 'height: ' + height + 'px;' : '') << ' '
			out << (width > 0 ? 'width: ' + width + 'px;' : '') << "' class='tds-svg-icons " << (attrs.styleClass ?: '') << "'"
			out << "viewBox='0 0 115 115' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'> "
			out << "<image x='0' y='0' height='110px' width='110px' fill='#1f77b4'  xmlns:xlink='http://www.w3.org/1999/xlink' "
			out << "xlink:href='${url}'></image></svg>"
		}
	}

	/**
	 * Helper Tag to render breadcrumbs to display
	 * @param title - the display name of the Page/Module
	 * @param crumbs - a map of elements what will be part of the crumbs
	 */
	def subHeader = { Map attrs ->
		String title = attrs.title
		def crumbs = attrs.crumbs
		Boolean justPlanningOptIn = BooleanUtils.toBoolean(attrs.justPlanningOptIn as String)

		out << "<!-- Content Header (Page header) -->"
		out << "<section class=\"content-header\">"
		out << "<h1> " << title
		if (justPlanningOptIn) {
			Boolean justPlanning  = BooleanUtils.toBoolean(userPreferenceService.getPreference(PREF.ASSET_JUST_PLANNING))
			out << " <span style=\"margin-left: 15px; font-size: 15px;\"><input type=\"checkbox\" id=\"justPlanning\" onclick=\"toggleJustPlanning(\$(this))\" "
			if (justPlanning) {
				out << 'checked="checked"'
			}
			out << '/><label style="font-weight: 600 !important;" for="justPlanning">&nbsp;Just Planning</label></span>'
		}
		out << '</h1>'

		Map licenseInfo = licenseAdminService.licenseInfo(securityService.getUserCurrentProject())

		if(licenseInfo.banner) {
			out << """
				<div class="breadcrumb licensing-banner-message breadcrumb-${crumbs.size}">
					<div class="callout">
						<p><strong>${licenseInfo.banner}</strong></p>
					</div>
				</div>"""
		}

		out << '<ol class="breadcrumb">'
			crumbs.each {
				out << '<li><a href="#">' << it << '</a></li>'
			}
		out << '</ol>'

		out << '</section>'
	}

	/**
	 * Inject a warning icon to show the status of the License Admin if is next to expires in a pop up
	 */
	def licenseWarning = {
		// Only for environments where the License Manager is true Enabled
		def isLicenseAdminEnabled = licenseCommonService.isAdminEnabled()
		def isLicenseMananagerEnabled = licenseCommonService.isManagerEnabled()
		if(isLicenseAdminEnabled && !isLicenseMananagerEnabled) {
			String stateMessage = licenseAdminService.getLicenseStateMessage()
			if(stateMessage) {
				// Bootstrap converts the html entities into real elements
				String administerLicenseButtonURL = "onClick=&quot;location.href=&apos;/tdstm/module/license/admin/list&apos;&quot;"
				out << "<a class='licensing-error-warning btn' href=\"#\" data-html=\"true\" data-toggle=\"popover\" tabindex=\"0\"  data-trigger=\"focus\" data-content=\" <div class='license-warning-message' style='word-wrap: break-word;'> <p>" << stateMessage << "</p> </div><div class='license-warning-message-button'><button type='button' class='btn btn-primary' " <<  administerLicenseButtonURL << "  >Administer License</button></div> \"><i class=\"fa fa-fw fa-warning licensing-error-warning\"></i></a>"
			}
		}
	}

	/**
	 * Used in the Application show view to show Owner/SMEs name and if the person is not staff of the
	 * project client then it will include the name of their company as well.
	 * @param client - the company that is the client
	 * @param person - the person to output the name of
	 * @return the person's name and company if staff of project owner or partner
	 *   owner/partner staff -  Robin Banks, Acme
	 *   client staff - Jim Lockar
	 */
	def nameAndCompany = { Map attrs ->
		def client = attrs.client
		def person = attrs.person
		def personCo = person?.company

		out << (person ? person.toString() : '')
		if (client && personCo && client.id != personCo.id) {
			out << ', ' << personCo.name
		}
	}

	/**
	 * Used to construct an application URL to be used in the application
	 * @param controller - name of the controller from the ui routing to be referenced
	 * @param fragment - each fragment is composed of one or n arguments and is the module being injected in the routing
	 */
	def appURL = { attrs ->
		def controller = attrs['controller']
		def fragment = attrs['fragment']

		def urlGenerated = ''

		if (controller != '' && fragment != '') {
			urlGenerated = HtmlUtil.appUrl(controller, '', fragment, false)
		}

		out << urlGenerated
	}

	def preferenceValue = { attrs ->
		userPreferenceService.getPreference(attrs.preference)
	}

	def currentMoveBundleId = { attrs ->
		userPreferenceService.moveBundleId
	}

	def currentMoveBundle = { attrs ->
		MoveBundle.get userPreferenceService.moveBundleId
	}

	def currentMoveEventId = { attrs ->
		userPreferenceService.moveEventId
	}

	def currentMoveEvent = { attrs ->
		MoveEvent.get userPreferenceService.moveEventId
	}

	def currentProjectId = { attrs ->
		userPreferenceService.currentProjectId
	}

	def currentProject = { attrs ->
		Project.get userPreferenceService.currentProjectId
	}

	def currentRoom = { attrs ->
		Room.get userPreferenceService.getPreference(PREF.CURR_ROOM)
	}

	def currentPerson = { attrs ->
		Person.get securityService.currentPersonId
	}

	def currentPersonId = { attrs ->
		securityService.currentPersonId
	}

	def timeZone = { attrs ->
		userPreferenceService.timeZone
	}

	def dateFormat = { attrs ->
		userPreferenceService.dateFormat
	}

	def partyGroup = { attrs ->
		userPreferenceService.getPreference PREF.PARTY_GROUP
	}

	def isIE6 = { attrs ->
		request.getHeader("User-Agent").contains("MSIE 6")
	}

	def isIE7 = { attrs ->
		request.getHeader("User-Agent").contains("MSIE 7")
	}

	def isMobile = { attrs ->
		request.getHeader("User-Agent").contains("Mobile")
	}

	def userLogin = { attrs ->
		securityService.userLogin
	}

	def minPasswordLength = { attrs ->
		securityService.userLocalConfig.minPasswordLength ?: 8
	}

	def setImage = { attrs ->
		session.getAttribute('setImage') ?: securityService.userCurrentProjectId ?
				ProjectLogo.findByProject(securityService.loadUserCurrentProject())?.id : ''
	}

	/**
	 * The value stored by UserService.updateLastLogin() - the UserLogin's Person's first name.
	 */
	def currentPersonName = { attrs ->
		session.getAttribute('LOGIN_PERSON')?.name ?: ''
	}

	def startPage = { attrs ->
		userPreferenceService.getPreference(PREF.START_PAGE)
	}

	def powerType = { attrs ->
		powerTypePref
	}

	def powerTypeShort = { attrs ->
		powerTypePref != 'Watts' ? 'Amps' : 'W'
	}

	/**
	 * Converts watts to amps if that's the user's preferred format.
	 * @attr power  the Rack power
	 * @attr powerProperty  if specified, get the power value from this property of a new Rack
	 * @attr blankZero  if true, render a blank string if zero (defaults to false)
	 */
	def rackPower = { attrs ->
		String powerProperty = attrs.powerProperty
		int rackPower = ((Integer) attrs.power) ?: 0
		if (rackPower && powerProperty) {
			throwTagError 'Cannot specify both power and powerProperty'
		}

		if (powerProperty) {
			rackPower = ((int) new Rack()[powerProperty]) ?: 0
		}

		int converted = powerTypePref == 'Watts' ? Math.round(rackPower) : (rackPower / 120).toFloat().round(1)
		def result = (attrs.blankZero && converted == 0) ? '' : converted
		out << result
	}

	/**
	 * Used to render the Group Id value along with the tooltip and link to the Dependency Analyzer for that Group Id.
	 * @attr groupId  The group id the asset belongs to. If empty, the result is an empty string.
	 * @attr tab  The tab that the logic should switch to after the page is loaded. If not present, it defaults to "map".
	 * @attr assetName  Not used yet, reserved for future feature.
	 */
	def showDependencyGroup = { attrs ->
		if (!attrs.groupId) {
			out << " "
		} else {
			def value = dependencyGroupValueWithTooltip(attrs.groupId)
			def tabName = attrs.tab ?: 'map'
			def url = g.link( mapping: "dependencyConsoleMap", params: [subsection: tabName, groupId:attrs.groupId, assetName: attrs.assetName], value)
			out << ':' + url
		}
	}

	/**
	 * Used to render label Enabled/Disabled on true/false value passed
	 * Example:
	 * <tds:enabledDisabled value="true"/>  ==> Returns: Enabled
	 * <tds:enabledDisabled value="false"/> ==> Returns: Disabled
	 */
	def enabledDisabled = { attrs ->
		if (attrs.value != null) {
			if (attrs.value == true) {
				out << "Enabled"
			} else {
				out << "Disabled"
			}
		} else {
			out << ""
		}
	}

	/**
	 * Used to render label Yes/No based on true/false value passed
	 * Example:
	 * <tds:YesNo value="true"/>  ==> Returns: Yes
	 * <tds:YesNo value="false"/> ==> Returns: No
	 */
	def yesNo = { attrs ->
		if (attrs.value != null) {
			out << (attrs.value == true ? 'Yes' : 'No')
		} else {
			out << ""
		}
	}

	def currentProjectMoveEvents = { attrs ->
		MoveEvent.findAllByProject(securityService.loadUserCurrentProject())
	}

	private String getPowerTypePref() {
		userPreferenceService.getPreference(PREF.CURR_POWER_TYPE) ?: 'Watts'
	}

	private boolean isDateOrTimestamp(dateValue) {
		Class c = dateValue?.getClass()
		c == Date || c == Timestamp
	}

	void afterPropertiesSet() {

	}

	/**
	 * Returns an HTML span with tooltip, with the dependency group element inside.
	 * @return  The group Id element with tooltip
	 */
	private String dependencyGroupValueWithTooltip(def groupId ) {
		StringBuilder span = new StringBuilder('')
		span.append("<span ")
		span.append( "data-toggle='popover' ")
		span.append( "data-trigger='hover' ")
		span.append( "data-content='Click to view group in Dependency Analyzer' ")
		span.append(" >\n")
		span.append(groupId)
		span.append("\n </span>\n")
		return span.toString()
	}
}
