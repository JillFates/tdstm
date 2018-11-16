import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetEntityPlanStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.ModelConnector
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UserPreferenceService
import org.apache.commons.lang.math.NumberUtils
import org.springframework.jdbc.core.JdbcTemplate

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
@Slf4j(value='logger', category='grails.app.controllers.RackLayoutsController')
class RackLayoutsController implements ControllerMethods {

	AssetEntityService assetEntityService
	ControllerService controllerService
	JdbcTemplate jdbcTemplate
	TaskService taskService
	UserPreferenceService userPreferenceService

	/**
	 * Used to generate the Rack Elevation criteria form that users access to generation elevations
	 */
	@HasPermission(Permission.RackView)
	def create() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		String targetRack = ""
		String sourceRack = ""
		String bundle = ""
		def rackFilters
		boolean frontCheck = false      // Render front view checkbox
		boolean backCheck = true      // Render back view checkbox
		boolean wBundleCheck = true      // with Bundle Names checkbox
		boolean woBundleCheck = true
		// with "other" Bundle names checkbox - include assets from other bundles not in the filter
		boolean wDCheck = false      // Render with diagrams / cabling (THIS IS NOT BEING USED CURRENTLY)

		if (session.getAttribute("USE_FILTERS") == "true") {
			rackFilters = session.getAttribute("RACK_FILTERS")
			if (rackFilters) {
				targetRack = rackFilters?.targetrack ? rackFilters?.targetrack?.toString().replace("[", "").replace("]", "") : ""
				sourceRack = rackFilters?.sourcerack ? rackFilters?.sourcerack?.toString().replace("[", "").replace("]", "") : ""
				bundle = rackFilters?.moveBundle ? rackFilters?.moveBundle?.toString().replace("[", "").replace("]", "") : ""
				frontCheck = rackFilters?.frontView
				backCheck = rackFilters?.backView
				wBundleCheck = rackFilters?.bundleName
				woBundleCheck = rackFilters?.otherBundle
				wDCheck = rackFilters?.showCabling
			}
		}

		List moveBundleList = MoveBundle.findAllByProject(project)
		def currentBundle = userPreferenceService.moveBundleId
		boolean isCurrentBundle = true
		if (!currentBundle) {
			currentBundle = moveBundleList[0]?.id?.toString()
			isCurrentBundle = false
		}

		// Map entities = assetEntityService.entityInfo( project )

		session.removeAttribute("USE_FILTERS")
		session.removeAttribute("RACK_FILTERS")

		[backCheck: backCheck, bundle: bundle, currentBundle: currentBundle, frontCheck: frontCheck,
		 isCurrentBundle: isCurrentBundle, moveBundleList: moveBundleList, rackFilters: rackFilters,
		 rackLayoutsHasPermission: securityService.hasPermission(Permission.RackCreate), sourceRackFilter: sourceRack,
		 targetRackFilter: targetRack, wBundleCheck: wBundleCheck, wDCheck: wDCheck, woBundleCheck: woBundleCheck]
	}

	/**
	 * Used to generate multiple rack elevation diagrams
	 */
	@HasPermission(Permission.RackView)
	def generateElevations() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		session.setAttribute("RACK_FILTERS", params)
		List bundleIds = request.getParameterValues("moveBundle")
		def maxUSize = 42

		if (bundleIds == "null") {
			return [errorMessage: "Please select a bundle"]
		}

		String tzId = userPreferenceService.timeZone

		def model = assetEntityService.getDeviceModelForList(project, session, params, tzId)

		def redirectTo = 'rack'
		def includeOtherBundle = params.otherBundle
		def includeBundleName = params.bundleName
		def frontView = params.frontView
		def backView = params.backView
		def rackLayout = []
		List<MoveBundle> moveBundles = MoveBundle.findAllByProject(project)
		def rackId = params.rackId
		def hideIcons = params.hideIcons

		boolean printView = params.viewMode == 'Print View'
		boolean generateView = params.viewMode == 'Generate'

		if (bundleIds && !bundleIds.contains("all")) {
			def bundlesString = bundleIds.toString().replace("[", "(").replace("]", ")")
			moveBundles = MoveBundle.findAll("from MoveBundle where id in ${bundlesString} ")
		}
		def rackLayoutsHasPermission = securityService.hasPermission(Permission.RackLayoutModify)

		List<Rack> sourceRacks = findRacks('sourcerack', moveBundles, true)
		List<Rack> targetRacks = findRacks('targetrack', moveBundles, false)
		def racks = sourceRacks + targetRacks
		if (racks.size() == 0 && rackId) {
			session.setAttribute('RACK_ID', rackId)
			redirectTo = 'room'
			racks = Rack.getAll(rackId).findAll()
			//moveBundles = []
			bundleIds = request.getParameterValues("moveBundleId")
			if (bundleIds && !bundleIds.contains("all") && !bundleIds.contains("taskReady")) {
				moveBundles = MoveBundle.getAll(bundleIds*.toLong()).findAll()
			}
		}

		racks.each { rack ->
			maxUSize = rack?.model?.usize ?: 42
			def assetDetails = []
			def assetDetail = []
			def racksByFilter
			if (includeOtherBundle) {
				racksByFilter = rack.assets.findAll { !it.isaBlade() && it.project == project }
						.sort {
					rack?.source == 1 ? it.sourceRackPosition ? it.sourceRackPosition * -1 : 0 : it.targetRackPosition ? it.targetRackPosition * -1 : 0
				}
			}
			else {
				racksByFilter = rack.assets.findAll {
					!it.isaBlade() && moveBundles*.id.contains(it.moveBundle?.id) && it.project == project
				}
						.sort {
					rack?.source == 1 ? it.sourceRackPosition ? it.sourceRackPosition * -1 : 0 : it.targetRackPosition ? it.targetRackPosition * -1 : 0
				}
			}
			racksByFilter.each { assetEntity ->

				def overlapError = false
				def rackPosition = rack.source == 1 ? assetEntity.sourceRackPosition : assetEntity.targetRackPosition
				if (rackPosition == 0 || rackPosition == null) {
					rackPosition = 1
				}

				def rackSize = assetEntity?.model?.usize == 0 || assetEntity?.model?.usize == null ? 1 : assetEntity?.model?.usize?.toInteger()
				def position = rackPosition + rackSize - 1
				def newHigh = position
				def newLow = rackPosition
				if (assetDetail.size() > 0) {
					def flag = true
					assetDetail.each { asset ->
						flag = true
						def currentHigh = asset.currentHigh
						def currentLow = asset.currentLow
						def ignoreLow = (currentLow <= newLow && currentHigh >= newHigh)
						def changeBoth = (currentLow >= newLow && currentHigh <= newHigh)
						def changeLow = (currentLow >= newLow && currentHigh >= newHigh && currentLow <= newHigh)
						def changeHigh = (currentLow <= newLow && currentHigh <= newHigh && currentHigh <= newLow)
						if (position > maxUSize) {
							asset.position = maxUSize
							asset.rowspan = 1
							asset.assetTag = asset.assetTag + "<br/>" + assetEntity.assetTag + ' ~- ' + assetEntity.assetName
							asset.overlapError = true
							asset.cssClass = "rack_error"
							flag = false
						}
						else if (ignoreLow) {
							asset.position = currentHigh
							asset.rowspan = currentHigh - currentLow + 1
							asset.assetTag = asset.assetTag + "<br/>" + assetEntity.assetTag + ' ~- ' + assetEntity.assetName
							asset.overlapError = true
							asset.cssClass = "rack_error"
							flag = false
						}
						else if (changeBoth) {
							asset.currentHigh = newHigh
							asset.currentLow = newLow
							asset.position = newHigh
							asset.rowspan = newHigh - newLow + 1
							asset.assetTag = asset.assetTag + "<br/>" + assetEntity.assetTag + ' ~- ' + assetEntity.assetName
							asset.overlapError = true
							asset.cssClass = "rack_error"
							flag = false
						}
						else if (changeHigh) {
							asset.currentHigh = newHigh
							asset.position = newHigh
							asset.rowspan = newHigh - currentLow + 1
							asset.assetTag = asset.assetTag + "<br/>" + assetEntity.assetTag + ' ~- ' + assetEntity.assetName
							asset.overlapError = true
							asset.cssClass = "rack_error"
							flag = false
						}
						else if (changeLow) {
							asset.currentLow = newLow
							asset.position = currentHigh
							asset.rowspan = currentHigh - newLow + 1
							asset.assetTag = asset.assetTag + "<br/>" + assetEntity.assetTag + ' ~- ' + assetEntity.assetName
							asset.overlapError = true
							asset.cssClass = "rack_error"
							flag = false
						}
					}

					if (flag) {
						if (position > maxUSize) {
							position = maxUSize
							newLow = maxUSize
							//assetEntity?.model?.usize = 1
							overlapError = true
						}
						assetDetail << [assetTag: assetEntity.assetTag + ' ~- ' + assetEntity.assetName, position: position,
						                assetEntity: assetEntity, overlapError: overlapError, rowspan: rackSize,
						                currentHigh: position, currentLow: newLow, source: rack.source]
					}
				}
				else {
					if (position > maxUSize) {
						position = maxUSize
						newLow = maxUSize
						//assetEntity?.model?.usize = 1
						overlapError = true
					}
					assetDetail << [assetEntity: assetEntity, assetTag: assetEntity.assetTag + ' ~- ' + assetEntity.assetName,
					                position: position, overlapError: overlapError, rowspan: rackSize,
					                currentHigh: position, currentLow: newLow, source: rack.source]
				}
			}
			for (int i = maxUSize; i > 0; i--) {
				def cssClass = "empty"
				def rackStyle = "rack_past"
				def assetEnitiesAtPosition = assetDetail.findAll { it.position == i }

				if (assetEnitiesAtPosition.size() > 1) {
					cssClass = 'rack_error'
					rackStyle = 'rack_error'
					assetDetails << [asset: assetEnitiesAtPosition[0], rack: i, cssClass: cssClass, rackStyle: rackStyle, source: rack.source]
				}
				else if (assetEnitiesAtPosition.size() == 1) {
					def assetEnity = assetEnitiesAtPosition[0]
					def currentTime = TimeUtil.nowGMT().getTime()
					if (assetEnity.overlapError) {
						cssClass = 'rack_error'
						rackStyle = 'rack_error'
					}
					else if (bundleIds && !moveBundles*.id.contains(assetEnity.assetEntity?.moveBundle?.id)) {
						def startTime = assetEnity.assetEntity?.moveBundle?.startTime?.time ?: 0
						if (startTime < currentTime) {
							cssClass = 'rack_past'
						}
						else {
							cssClass = "rack_future"
						}
					}
					else if (rackId && !moveBundles*.id.contains(assetEnity.assetEntity?.moveBundle?.id)) {
						def startTime = assetEnity.assetEntity?.moveBundle?.startTime?.time ?: 0
						if (startTime < currentTime) {
							cssClass = 'rack_past'
						}
						else {
							cssClass = "rack_future"
						}
					}
					else if (assetEnity.assetEntity?.planStatus != AssetEntityPlanStatus.MOVED) {
						if (rack.source == 0) {
							cssClass = 'rack_current_dashed'
							rackStyle = 'rack_current_dashed'
						}
						else {
							cssClass = 'rack_current'
							rackStyle = 'rack_current'
						}
					}
					else {
						cssClass = 'rack_current'
						rackStyle = 'rack_current'
					}
					if (assetEnity.position == 0 || assetEnity.assetEntity?.model?.usize == 0 || assetEnity.assetEntity?.model?.usize == null) {
						rackStyle = 'rack_error'
					}
					if (!(assetEnity.assetEntity?.planStatus == AssetEntityPlanStatus.MOVED && rack.source == 1)) {
						assetDetails << [asset: assetEnity, rack: i, cssClass: cssClass, rackStyle: rackStyle, source: rack.source, rackDetails: rack]
					}
				}
				else {
					assetDetails << [asset: null, rack: i, cssClass: cssClass, rackStyle: rackStyle, source: rack.source, rackDetails: rack]
				}
			}
			def backViewRows
			def frontViewRows
			def paramsMap = [rackLayoutsHasPermission: rackLayoutsHasPermission, assetDetails: assetDetails,
			                 rackId: rack.id, backView: backView, includeBundleName: includeBundleName,
			                 showCabling: params.showCabling, hideIcons: hideIcons, redirectTo: redirectTo,
			                 forWhom: params.forWhom, bundle: moveBundles, printView: printView]

			if (backView) {
				backViewRows = retrieveRackLayout(paramsMap)
			}
			if (frontView) {
				paramsMap.remove('backView')
				frontViewRows = retrieveRackLayout(paramsMap)
			}
			rackLayout << [assetDetails : assetDetails, rack: rack.tag, room: rack.room,
			               frontViewRows: frontViewRows, backViewRows: backViewRows, rackId: rack.id]
		}
		def showIconPref = userPreferenceService.getPreference(PREF.SHOW_ADD_ICONS)

		model.putAll([
				rackLayout  : rackLayout,
				frontView   : frontView,
				backView    : backView,
				showIconPref: showIconPref,
				generateView: generateView,
				printView   : printView
		])

		return model
	}

	@HasPermission(Permission.RackView)
	def retrieveRackDetails() {
		def bundleIds = params.bundles
		def moveBundles = []
		if (bundleIds.contains('all')) {
			moveBundles = MoveBundle.findAllByProject(securityService.loadUserCurrentProject())
		}
		else if (bundleIds) {
			moveBundles = MoveBundle.findAll("from MoveBundle m where m.id in ($bundleIds)")
		}

		def sourceRacks = []
		def targetRacks = []

		moveBundles.each { moveBundle ->
			moveBundle.sourceRacks.each {
				if (!sourceRacks.contains([id: it.id, location: it.location, room: it.room?.roomName, tag: it.tag])) {
					sourceRacks.add([id: it.id, location: it.location, room: it.room?.roomName, tag: it.tag])
				}
			}
			moveBundle.targetRacks.each {
				if (!targetRacks.contains([id: it.id, location: it.location, room: it.room?.roomName, tag: it.tag])) {
					targetRacks.add([id: it.id, location: it.location, room: it.room?.roomName, tag: it.tag])
				}
			}
		}

		renderAsJson([[sourceRackList: sourceRacks, targetRackList: targetRacks]])
	}

	/**
	 * Used to generate the HTML that represents a Rack
	 */
	private retrieveRackLayout(paramsMap) {
		def rows = new StringBuilder()
		def rowspan = 1
		def cssClass = "empty"
		def rackStyle = ""
		def showIconPref = userPreferenceService.getPreference(PREF.SHOW_ADD_ICONS)

		def rackLayoutsHasPermission = paramsMap.rackLayoutsHasPermission
		def asset = paramsMap.assetDetails
		def includeBundleName = paramsMap.includeBundleName
		def backView = paramsMap.backView
		def showCabling = paramsMap.showCabling
		def hideIcons = paramsMap.hideIcons
		def redirectTo = paramsMap.redirectTo
		def rackId = paramsMap.rackId
		def forWhom = paramsMap.forWhom
		boolean printView = paramsMap.printView
		String disconnectImgUrl = HtmlUtil.resource([dir: "icons", file: "disconnect.png"])
		asset.each {
			def row = new StringBuilder("<tr>")
			if (it.asset) {
				rowspan = it.asset?.rowspan != 0 ? it.asset?.rowspan : 1
				rackStyle = it.rackStyle
				def location = it.source
				def assetEntity = it.asset?.assetEntity
				def assetTagsList = (it.asset?.assetTag).split("<br/>")
				def moveBundle = ""
				StringBuilder assetTag = new StringBuilder('')

				if (it.cssClass == "rack_error") {
					assetTag.append("Devices Overlap:<br />")
				}

				def hasBlades = false
				def cabling = ""

				def srcTrg = 'Source'
				def queryParams = [project: assetEntity.project, assetClass: AssetClass.DEVICE]
				if (location == 1) {
					queryParams.roomName = assetEntity.getSourceRoomName()
					queryParams.rackName = assetEntity.getSourceRackName()
				}
				else {
					srcTrg = 'Target'
					queryParams.roomName = assetEntity.getTargetRoomName()
					queryParams.rackName = assetEntity.getTargetRackName()
				}
				def query = "FROM AssetEntity AS a \
					JOIN a.room$srcTrg AS room \
					JOIN a.rack$srcTrg as rack \
					WHERE a.project=:project AND a.assetClass=:assetClass AND (a.assetType IS NULL OR a.assetType<>'Blade') \
					AND room.roomName=:roomName AND rack.tag=:rackName \
					AND a.assetTag=:tag"

				assetTagsList.each { assetTagValue ->
					def index = assetTagValue.indexOf('~-')
					def tagValue
					if (index != -1) {
						tagValue = assetTagValue?.substring(0, index)
					}
					else {
						tagValue = assetTagValue
					}
					def overlappedAsset
					def overlappedAssets
					def bladeTable = ""
					def bladeLayoutMap = [
							asset     : it,
							bundle    : paramsMap.bundle,
							forWhom   : forWhom,
							hideIcons : hideIcons,
							permission: rackLayoutsHasPermission,
							printView : printView,
							rackId    : rackId,
							redirectTo: redirectTo
					]
					queryParams.tag = tagValue
					overlappedAssets = AssetEntity.findAll(query, queryParams)
					logger.debug '**** overlappedAssets = {} --- {}', overlappedAssets.getClass().name, overlappedAssets

					// TODO : JPM 9/2014 : This block of code is 99% duplicate except for the cabling assignment
					def overlappedAssetsSize = overlappedAssets.size()
					if (overlappedAssetsSize) {
						overlappedAssets.each { r ->

							def overlapAsset = r[0]

							String title = "Tag: ${overlapAsset.assetTag}\nName: ${overlapAsset.assetName}"
							if (overlapAsset.model) {
								title += "\nModel: ${overlapAsset.model.modelName}"
							}

							moveBundle += (overlapAsset?.moveBundle?.name ?: "") + "<br/>"
							if (overlapAsset.model && overlapAsset.model.assetType == 'Blade Chassis' && (!backView || showCabling != 'on')) {
								hasBlades = true
								bladeLayoutMap << ['overlappedAsset': overlapAsset]
								bladeTable = generateBladeLayout(bladeLayoutMap)
							}
							if (overlappedAssetsSize > 1) {
								cabling = ((assetTag.indexOf("Devices Overlap") == -1) && showCabling == 'on' ? generateCablingLayout(overlappedAsset, backView) : "")
							}

							if (printView) {
								assetTag.append(StringUtil.ellipsis(assetTagValue.replace('~-', '-'), 22))
							}
							else {
								assetTag.append('<a title="' + title + '" href="javascript:')
								if (forWhom) {
									assetTag.append("editAudit('roomAudit','${it.source}','${overlapAsset.assetClass}',${overlapAsset?.id})")
								}
								else {
									assetTag.append("EntityCrud.showAssetDetailView('${overlapAsset.assetClass}',${overlapAsset?.id})")
								}
								assetTag.append('">' + StringUtil.ellipsis(assetTagValue.replace('~-', '-'), 22) + '</a>')
							}

							if (hasBlades) {
								assetTag.append("<br/>" + bladeTable)
							}
						}
					}
				}
				if (backView) {

					def taskAnchors = ""
					if (!printView) {
						def tasks = AssetComment.findAllByAssetEntityAndStatusInList(it.asset?.assetEntity,
							[AssetCommentStatus.STARTED, AssetCommentStatus.READY, AssetCommentStatus.HOLD])
						tasks.each {
							taskAnchors += """<a href='#' class='${taskService.getCssClassForRackStatus(it.status)}' title='${
								it.taskNumber + ':' + it.comment
							}'
								onclick=\"javascript:showAssetComment(${
								it.id
							},'show')\" > &nbsp;&nbsp;&nbsp;&nbsp; </a> &nbsp;"""
						}
					}

					if (cabling != "" && it.cssClass != "rack_error") {
						def assetCables = AssetCableMap.findByAssetFrom(it.asset?.assetEntity)
						if (hasBlades && showCabling != 'on') {
							row.append("<td class='${it.rackStyle}' ${it.rackStyle == 'rack_error' ? 'title=\"Device has no defined model, size is unknown\"' : ''}>${it.rack}</td><td colspan='2' rowspan='${rowspan}' class='${it.cssClass}'>${assetTag.toString()}</td>")
							if (!printView && assetCables) {
								row.append("""<td rowspan='${rowspan}' class='${it.cssClass}'><a href='#'
									onclick='openCablingDiv(${it.asset?.assetEntity.id})'></a> <img src="${disconnectImgUrl}"/>
									&nbsp${taskAnchors}</td>""")
							}
							else {
								row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>&nbsp;${taskAnchors}</td>")
							}
						}
						else {
							row.append("<td class='${it.rackStyle}' ${it.rackStyle == 'rack_error' ? 'title=\"Device has no defined model, size is unknown\"' : ''}>${it.rack}</td><td rowspan='${rowspan}' colspan='3' class='${it.cssClass}'>")
							row.append("<table style='border:0;' cellpadding='0' cellspacing='0'><tr><td style='border:0;'>${assetTag.toString()}</td>")

							if (includeBundleName) {
								row.append("<td style='border:0;'>${moveBundle}</td>")
							}
							else {
								row.append("<td style='border:0;'>&nbsp;</td>")
							}

							if (!printView && assetCables) {
								row.append("<td style='border:0;'><a href='#' onclick='openCablingDiv(${it.asset?.assetEntity.id})'> <img src='${disconnectImgUrl}'/> &nbsp; ${taskAnchors}</a></td></tr>")
							}
							else {
								row.append("<td style='border:0;'>&nbsp;${taskAnchors}</td></tr>")
							}

							row.append("<tr><td colspan='3' style='border:0;'>${cabling}</td></tr></table></td>")
						}
					}
					else {
						if (hasBlades && showCabling != 'on') {
							row.append("<td class='${it.rackStyle}' ${it.rackStyle == 'rack_error' ? 'title=\"Device has no defined model, size is unknown\"' : ''}>${it.rack}</td><td colspan='2' rowspan='${rowspan}' class='${it.cssClass}'>${assetTag.toString()}</td>")
						}
						else {
							row.append("<td class='${it.rackStyle}' ${it.rackStyle == 'rack_error' ? 'title=\"Device has no defined model, size is unknown\"' : ''}>${it.rack}</td><td rowspan='${rowspan}' class='${it.cssClass}'>${assetTag.toString()}${cabling}</td>")
							if (includeBundleName) {
								row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>${moveBundle}</td>")
							}
							else {
								row.append("<td rowspan='${rowspan}' class='${it.cssClass}'></td>")
							}
						}
						if (it.cssClass != "rack_error") {
							def assetCables = AssetCableMap.findByAssetFrom(it.asset?.assetEntity)
							if (!printView && assetCables) {
								row.append("<td rowspan='${rowspan}' class='${it.cssClass}'><a href='#' onclick='openCablingDiv(${it.asset?.assetEntity.id})'> <img src='${disconnectImgUrl}' height='12' width='12' title='Cabling'/> ${taskAnchors}</a></td>")
							}
							else {
								row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>&nbsp; ${taskAnchors}</td>")
							}

						}
						else {
							row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>Devices Overlap</td>")
						}
					}
				}
				else {
					if (hasBlades) {
						row.append("<td class='${it.rackStyle}' ${it.rackStyle == 'rack_error' ? 'title=\"Device has no defined model, size is unknown\"' : ''}>${it.rack}</td><td colspan='2' rowspan='${rowspan}' class='${it.cssClass}'>${assetTag.toString()}</td>")
					}
					else if (cabling != "") {
						row.append("<td class='${it.rackStyle}' ${it.rackStyle == 'rack_error' ? 'title=\"Device has no defined model, size is unknown\"' : ''}>${it.rack}</td><td rowspan='${rowspan}' colspan='2' class='${it.cssClass}'>")
						row.append("<table style='border:0;' cellpadding='0' cellspacing='0'><tr><td style='border:0;'>${assetTag.toString()}</td>")
						if (includeBundleName) {
							row.append("<td style='border:0;'>${moveBundle}</td></tr>")
						}
						else {
							row.append("<td style='border:0;'>&nbsp;</td></tr>")
						}
						row.append("<tr><td colspan='2' style='border:0;'>${cabling}</td></tr></table></td>")

					}
					else {
						row.append("<td class='${it.rackStyle}' ${it.rackStyle == 'rack_error' ? 'title=\"Device has no defined model, size is unknown\"' : ''}>${it.rack}</td><td rowspan='${rowspan}' class='${it.cssClass}'>${assetTag.toString()}</td>")
						if (includeBundleName) {
							row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>${moveBundle}</td>")
						}
						else {
							row.append("<td rowspan='${rowspan}' class='${it.cssClass}'></td>")
						}
					}
				}
			}
			else if (rowspan <= 1) {
				rowspan = 1
				rackStyle = it.rackStyle
				row.append("<td class='empty' nowrap>${it.rack}</td><td rowspan=1 class=${it.cssClass}>")
				if (!printView) {
					def roomParameter = it.rackDetails.room?.id
					def rackParameter = it.rackDetails.id
					def rackAdd2ImgUrl = HtmlUtil.resource([dir: "i", file: "rack_add2.png"])
					row.append("""<div ${showIconPref ? '' : 'style="display:none"'}  class="rack_menu create_${
						rackId
					}"><img src="${rackAdd2ImgUrl}">
						<ul>
							<li><a href="javascript:${
						forWhom ? "createAuditPage" : "EntityCrud.showAssetCreateView"
					}('DEVICE','${it.source}','${rackParameter}','${roomParameter}','${it.rackDetails.location}','${it.rack}')">Create asset  </a></li>
							<li><a href="javascript:listDialog('','','asc','${it.source}','${it.rackDetails.id}','${
						it.rackDetails.room?.id
					}','${it.rackDetails.location}','${it.rack}')">Assign asset </a></li>
							<li><a href="javascript:listDialog('all','','asc','${it.source}','${it.rackDetails.id}','${
						it.rackDetails.room?.id
					}','${it.rackDetails.location}','${it.rack}')">Reassign asset </a></li>
						</ul></img></div>&nbsp;""")
				}
				row.append("</td><td>&nbsp;</td>")
				if (backView) {
					row.append("<td>&nbsp;</td>")
				}

			}
			else {
				row.append("<td class='${rackStyle}'>${it.rack}</td>")
				rowspan--
			}
			// Remove right U-position number
			//row.append("<td class='${it.rackStyleUpos}'>${it.rack}</td>")
			row.append("</tr>")
			rows.append(row.toString())

		}
		return rows
	}

	/**
	 * Used to generate the HTML widget for a single Blade Chassis's layout for RackLayouts report
	 */
	private String generateBladeLayout(Map bladeLayoutMap) {
		//logger.debug 'generateBladeLayout() - bladeLayoutMap={}', bladeLayoutMap

		def assetDetails = bladeLayoutMap.asset
		def assetEntity = bladeLayoutMap.overlappedAsset
		def rackId = bladeLayoutMap.rackId
		def forWhom = bladeLayoutMap.forWhom
		def bundles = bladeLayoutMap.bundle
		boolean printView = bladeLayoutMap.printView

		def showIconPref = userPreferenceService.getPreference(PREF.SHOW_ADD_ICONS)
		StringBuilder bladeTable = new StringBuilder('<table class="bladeTable"><tr>')
		def rowspan = assetDetails.asset?.rowspan != 0 ? assetDetails.asset?.rowspan : 1
		def tdHeight = rowspan * 6
		def blades
		def chassis = AssetEntity.read(assetEntity.id)

		if (assetDetails.asset.source == 1) {
			blades = AssetEntity.findAllWhere(project: assetEntity.project, assetType: 'Blade', sourceChassis: chassis)
		}
		else {
			blades = AssetEntity.findAllWhere(project: assetEntity.project, assetType: 'Blade', targetChassis: chassis)
		}

		// Filter out the blades based on the bundles selected
		blades = blades.findAll { it?.moveBundle?.id in bundles?.id }

		def fullRows = []
		def assetRoom
		def assetLocation

		def chassisRows = 1
		if (assetEntity.model?.bladeRows) {
			chassisRows = assetEntity.model.bladeRows
		}

		def bladesPerRow = 8
		if (assetEntity.model.bladeCount) {
			bladesPerRow = (assetEntity.model.bladeCount / chassisRows).intValue()
		}

		// # of chars to display of the blade label in chassis view
		def bladeLabelCharsToDisplay = assetEntity.model.bladeLabelCount

		for (int k = 1; k <= chassisRows; k++) {
			int initialColumn = (k - 1) * bladesPerRow + 1
			for (int i = initialColumn; i <= k * bladesPerRow; i++) {
				def matching = []
				if (assetDetails.asset.source == 1) {
					matching = blades.findAll { it.sourceBladePosition == i }
				}
				else {
					matching = blades.findAll { it.targetBladePosition == i }
				}

				int bladeCount = matching.size()
				if (fullRows.contains(i)) {
					// What is a full row???
					bladeTable.append('')
				}
				else if (bladeCount > 1) {
					// Multiple blades are assigned to the same slot so set the the title to display all of the blades tag/name in the hover
					String title = "Overlap Conflict"
					matching.each {
						title += "\nTag: ${it.assetTag} Name: ${it.assetName}"
					}
					String label = 'Conflict'.split('')[1..-1].join('<br>')
					bladeTable.append("<td class='errorBlade' style='height:${tdHeight}px'>")
					//bladeTable.append("<a href=\"#\" title=\"$title\" style=\"text-decoration: none;\">$label</a></td>")
					bladeTable.append("<label title=\"$title\" style=\"text-decoration: none;\">$label</label></td>")
				}
				else if (bladeCount == 1) {
					def blade = matching[0]

					// Determine how many rows a blade spans
					def bladeSpan = blade.model?.bladeHeight == 'Full' ? chassisRows : 1
					if (bladeSpan == chassisRows) {
						for (int y = i; y <= chassisRows * bladesPerRow; y += bladesPerRow) {
							fullRows << y + bladesPerRow
						}
					}

					String title = "Tag: ${blade.assetTag}\nName: ${blade.assetName}"
					if (blade.model) {
						title += "\nModel: ${blade.model.modelName}"
					}

					int charsToDisplay = bladeLabelCharsToDisplay * bladeSpan

					def tag = blade.assetTag
					// def tag = "${blade.assetName}"
					if (tag.length() >= charsToDisplay) {
						tag = tag.substring(0, charsToDisplay)
					}
					tag = tag.split('')[1..-1].join('<br>')
					def taglabel = "<div>$tag</div>"

					def hasError
					// TODO : JPM 10/2014 : The has logic error issue might be because it is useing bladeLabelCount - need to investigate
					if (assetDetails.asset.source == 1) {
						hasError = blades.findAll { it.sourceBladePosition == i + bladeLabelCharsToDisplay }.size() > 0
					}
					else {
						hasError = blades.findAll { it.targetBladePosition == i + bladeLabelCharsToDisplay }.size() > 0
					}

					if ((bladeSpan == 2) && hasError) {
						bladeTable.append("<td class='errorBlade' style='height:${tdHeight}px'>&nbsp;</td>")
					}
					else {
						bladeTable.append("""<td class="blade" rowspan="$bladeSpan" style="height:${tdHeight}px">""")
					}
					if (printView) {
						bladeTable.append(taglabel)
					}
					else {
						bladeTable.append('<a style="text-decoration: none;"href="javascript:')
						if (forWhom) {
							bladeTable.append("editAudit('roomAudit','${assetDetails.source}','${blade.assetClass}',${blade.id})")
						}
						else {
							bladeTable.append("EntityCrud.showAssetDetailView('${blade.assetClass}',${blade.id})")
						}
						bladeTable.append("\" title=\"${title}\">${taglabel}</a>")
					}
					bladeTable.append("</td>")
				}
				else {
					bladeTable.append("<td class='emptyBlade' style='height:${tdHeight}px'>")
					if (!printView) {
						if (assetDetails.asset.source == 1) {
							assetRoom = assetEntity.roomSource?.id
							assetLocation = assetEntity.getSourceLocationName()
						}
						else {
							assetRoom = assetEntity.roomTarget?.id
							assetLocation = assetEntity.getTargetLocationName()
						}

						def rackAdd2ImgUrl = HtmlUtil.resource([dir: "i", file: "rack_add2.png"])
						bladeTable.append("""<div ${showIconPref ? '' : 'style="display:none"'} class="rack_menu create_${
							rackId
						}"><img src="${rackAdd2ImgUrl}"/>
							<ul>
								<li><a href="javascript:${
							forWhom ? 'createBladeAuditPage' : 'EntityCrud.showAssetCreateView'
						}('DEVICE','${assetDetails.source}','${assetEntity?.id}','${assetRoom}','${assetLocation}', '${
							i
						}', true, '${assetEntity.manufacturer?.id}','Blade','${assetEntity.moveBundle?.id}')">Create asset</a></li>
								<li><a href="javascript:listBladeDialog('${assetDetails.source}','${assetEntity.id}','${
							i
						}','assign','${assetRoom}','${assetLocation}')">Assign asset </a></li>
								<li><a href="javascript:listBladeDialog('${assetDetails.source}','${assetEntity.id}','${
							i
						}','reassign','${assetRoom}','${assetLocation}')">Reassign asset </a></li>
							</ul>
						</div>""")
					}
					bladeTable.append("</td>")
				}
			}

			// Close off the row and start a new one if we're not on the last chassis row
			bladeTable.append('</tr>')
			if (k < chassisRows) {
				bladeTable.append('<tr>')
			}

		}

		bladeTable.append('</table>')

		return bladeTable.toString()
	}

	@HasPermission(Permission.RackView)
	def modelTemplate() {
		return [params: params]
	}

	/*
	 * Return AssetCableMap record details to display at RackLayout cabling screen
	 */
	@HasPermission(Permission.RackView)
	def retrieveCablingDetails() {

		Project project = securityService.userCurrentProject
		def moveBundleList = MoveBundle.findAllByProject(project)
		def currentBundle = userPreferenceService.moveBundleId
		/* set first bundle as default if user pref not exist */
		List<Model> models = AssetEntity.withCriteria{
			projections{
				distinct("model")
			}
			and{
				eq("project", project)
			}
		}
		if (!currentBundle) {
			currentBundle = moveBundleList[0]?.id?.toString()
		}
		def roomType = params.roomType

		def assetId = params.assetId
		def assetEntity = assetId ? AssetEntity.get(assetId) : null
		def assetCableMapList
		def title = ""
		if (assetEntity) {
			if (roomType == 'T') {
				assetCableMapList = assetEntityService.createOrFetchTargetAssetCables(assetEntity)
			}
			else {
				assetCableMapList = AssetCableMap.findAllByAssetFromAndAssetLoc(assetEntity, 'S')
			}

			title = assetEntity.assetName + " ( " + assetEntity?.model?.manufacturer + " / " + assetEntity.model + " )"
		}
		boolean isTargetRoom = assetEntity.roomTarget
		def assetCablingDetails = []
		def assetCablingMap = [:]
		def assetRows = [:]
		assetCableMapList.each {
			def connectorLabel = it.assetToPort?.label ?: ''
			def toAssetId
			def toTitle = ""
			def powerA = 'power'
			def powerB = 'nonPower'
			if (it.assetFromPort.type == "Power") {
				connectorLabel = it.toPower ?: ''
				powerA = 'nonPower'
				powerB = 'power'
			}
			else if (it.assetTo) {
				toAssetId = it.assetTo.id
				toTitle = it.assetTo.assetName + " ( " + it.assetTo.model?.manufacturer + " / " + it.assetTo.model + " )"
			}
			if (it.assetLoc == 'S') {
				def sourceRack = it.assetFrom?.getSourceRackName()
				title += sourceRack ? " ( " + sourceRack + " / " + it.assetFrom?.sourceRackPosition + " )" : ""
			}
			else {
				title += " ( " + it.assetFrom?.getTargetRackName() + " / " + it.assetFrom?.targetRackPosition + " )"
			}
			assetCablingDetails << [
					color        : it.cableColor ? it.cableColor : "",
					comment      : it.cableComment ?: '',
					connector    : it.assetFromPort.connector,
					connectorPosX: it.assetFromPort.connectorPosX,
					connectorPosY: it.assetFromPort.connectorPosY,
					fromAssetId  : (it.assetTo ? it.assetTo?.assetName + "/" + connectorLabel : ''),
					hasImageExist: (assetEntity.model?.rearImage && assetEntity.model?.useImage) as Boolean,
					id           : it.id,
					label        : it.assetFromPort.label,
					labelPosition: it.assetFromPort.labelPosition,
					model        : assetEntity.model?.id,
					rackUposition: connectorLabel,
					status       : it.cableStatus, displayStatus: it.cableStatus,
					title        : title,
					toAssetId    : toAssetId,
					toAssetPortId: it.assetToPort?.id,
					toTitle      : toTitle,
					type         : it.assetFromPort.type,
					usize        : assetEntity?.model?.usize
			]

			assetCablingMap[it.id] = [cableId: it.id, color: it.cableColor, connectorId: it.assetToPort?.id ?: "null",
			                          fromAsset: (it.assetTo ? it.assetTo?.assetName + "/" + connectorLabel : ''),
			                          type: it.assetFromPort.type, toTitle: toTitle, fromAssetId: it.assetTo?.id ?: 'null',
			                          asset: it.assetTo?.assetName ?: '', label: it.assetFromPort.label,
			                          length: it.cableLength ?: '', locRoom: it.assetLoc == 'S' ? 'Current' : 'Target',
			                          powerA: powerA, powerB: powerB, rackUposition: connectorLabel, roomType: it.assetLoc,
			                          status: it.cableStatus, comment: it.cableComment ?: '', title: title,
			                          type: it.assetFromPort.type]
			assetRows[it.id] = 'h'
		}
		render(template: 'cabling',
		       model: [assetCablingDetails: assetCablingDetails, assetCablingMap: (assetCablingMap as JSON),
		               assetId: assetId, assetRows: (assetRows as JSON), currentBundle: currentBundle,
		               isTargetRoom: isTargetRoom, models: models, roomType: roomType]
		)
	}

	/*
	 * Return modelConnectorList to display at connectors dropdown in  cabling screen
	 */
	@HasPermission(Permission.RackView)
	def retrieveAssetModelConnectors() {
		def jsonInput = request.JSON
		def roomType = jsonInput.roomType
		def assetId = jsonInput.asset
		Project project = securityService.userCurrentProject
		def assetEntity = assetId ? AssetEntity.get(assetId) : null
		def currRoomRackAssets
		if (roomType == 'T') {
			currRoomRackAssets = AssetEntity.findAllByRoomTargetAndProject(assetEntity.roomTarget, project)?.findAll {
				it.model?.modelConnectors?.type?.contains(jsonInput.type)
			}
		}
		else {
			currRoomRackAssets = AssetEntity.findAllByRoomSourceAndProject(assetEntity.roomSource, project)?.findAll {
				it.model?.modelConnectors?.type?.contains(jsonInput.type)
			}
		}
		def currRackAssets = currRoomRackAssets.findAll {
			it.rackSource?.id == assetEntity.rackSource?.id || it.rackTarget?.id == assetEntity.rackTarget?.id
		}
		def sortedAssets = currRackAssets.sort { it.assetName } + (currRoomRackAssets - currRackAssets).sort {
			it.assetName
		}

		def modelConnectorMap = [:]
		currRoomRackAssets.each { asset ->
			def modelConnectMapList = []
			def modelConnectList = AssetCableMap.findAllByAssetFromAndAssetLoc(asset, roomType)?.findAll {
				it.assetFromPort.type == jsonInput.type
			}
			modelConnectList.each {
				modelConnectMapList << ['value': it.assetFromPort.id, 'text': it.assetFromPort.label]
			}
			modelConnectorMap << [(asset.id): modelConnectMapList]
		}

		renderAsJson(connectors: modelConnectorMap, assets: sortedAssets)
	}

	/*
	 * Update the AssetCablingMap with the date send from RackLayout cabling screen
	 */
	@HasPermission(Permission.RackEdit)
	def updateCablingDetails() {
		def jsonInput = request.JSON
		def assetCableId = jsonInput.assetCable
		def assetCableMap
		def toCableId
		if (assetCableId) {
			def actionType = jsonInput.actionType
			def status = jsonInput.status ?: AssetCableStatus.UNKNOWN
			def toConnector
			def assetTo
			def toPower
			def connectorType = jsonInput.connectorType
			assetCableMap = AssetCableMap.get(assetCableId)

			if (connectorType != "Power") {
				int count = AssetCableMap.countByAssetToAndAssetToPortAndAssetLoc(
						assetCableMap.assetFrom, assetCableMap.assetFromPort, jsonInput.roomType)
				if (count) {
					AssetCableMap.executeUpdate('''
						update AssetCableMap
						set cableStatus=?, assetTo=null, assetToPort=null, cableColor=null
						where assetTo = ? and assetToPort = ? and assetLoc=?
					''', [status, assetCableMap.assetFrom, assetCableMap.assetFromPort, jsonInput.roomType])
				}
			}
			switch (actionType) {
				case "emptyId": status = AssetCableStatus.EMPTY; break
				case "cabledId": status = AssetCableStatus.CABLED; break
				case "assignId":
					if (connectorType != "Power") {
						if (jsonInput.assetFromId != 'null') {
							def assetEntity = AssetEntity.get(jsonInput.assetFromId)
							if (assetEntity?.model) {
								assetTo = assetEntity
								toConnector = ModelConnector.get(jsonInput.modelConnectorId)
								toCableId = AssetCableMap.findByAssetToAndAssetToPortAndAssetLoc(assetTo, toConnector, jsonInput.roomType)
								AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus=?,assetTo=null,
																assetToPort=null, cableColor=null
																where assetTo = ? and assetToPort = ? and assetLoc=?""", [status, assetTo, toConnector, jsonInput.roomType])
							}
						}
					}
					else {
						assetTo = assetCableMap.assetFrom
						toConnector = null
						toPower = jsonInput.staticConnector
					}
					break
			}

			GormUtil.flushAndClearSession()
			assetCableMap.cableStatus = status
			assetCableMap.assetTo = assetTo
			assetCableMap.assetToPort = toConnector
			assetCableMap.toPower = toPower
			assetCableMap.cableColor = jsonInput.color
			assetCableMap.cableLength = NumberUtils.toDouble(jsonInput.cableLength.toString(), 0).round()
			assetCableMap.cableComment = jsonInput.cableComment
			assetCableMap.assetLoc = jsonInput.roomType
			if (assetCableMap.save(flush: true)) {
				if (assetTo && connectorType != "Power") {
					def toAssetCableMap = AssetCableMap.find("from AssetCableMap where assetFrom=? and assetFromPort=? and assetLoc=?",
							[assetTo, toConnector, jsonInput.roomType])
					toAssetCableMap.cableStatus = status
					toAssetCableMap.assetTo = assetCableMap.assetFrom
					toAssetCableMap.assetToPort = assetCableMap.assetFromPort
					toAssetCableMap.cableColor = jsonInput.color
					toAssetCableMap.cableLength = NumberUtils.toDouble(jsonInput.cableLength.toString(), 0).round()
					toAssetCableMap.cableComment = jsonInput.cableComment
					toAssetCableMap.assetLoc = jsonInput.roomType
					if (!toAssetCableMap.save(flush: true)) {
						def etext = "Unable to create toAssetCableMap" +
								GormUtil.allErrorsString(toAssetCableMap)
						println etext
					}
				}
			}
			else {
				def etext = "Unable to create FromAssetCableMap" + GormUtil.allErrorsString(assetCableMap)
				println etext
			}
		}
		def connectorLabel = assetCableMap.assetToPort ? assetCableMap.assetToPort.label : ""
		def powerA = 'power'
		def powerB = 'nonPower'
		if (assetCableMap.assetFromPort.type == "Power") {
			connectorLabel = assetCableMap.toPower ? assetCableMap.toPower : ""
			powerA = 'nonPower'
			powerB = 'power'
		}

		renderAsJson(label: assetCableMap.assetFromPort.label, type: assetCableMap.assetFromPort.type,
				color: assetCableMap.cableColor, length: assetCableMap.cableLength ?: '', powerA: powerA, powerB: powerB,
				asset: assetCableMap.assetTo?.assetName ?: '', status: assetCableMap.cableStatus,
				comment: assetCableMap.cableComment ?: '', fromAssetId: assetCableMap.assetTo?.id ?: '',
				fromAsset: (assetCableMap.assetTo ? assetCableMap.assetTo.assetName + "/" + connectorLabel : ''),
				rackUposition: connectorLabel, connectorId: assetCableMap.assetToPort ? assetCableMap.assetToPort.id : "",
				toCableId: toCableId?.id, locRoom: assetCableMap.assetLoc == 'S' ? 'Current' : 'Target')
	}

	/*
	 *  Provide the Rack auto complete details and connector, uposition validation
	 */
	@HasPermission(Permission.RackView)
	def retrieveAutoCompleteDetails() {
		Project project = securityService.userCurrentProject
		def data
		def field = params.field
		def value = params.value
		switch (field) {
			case "rack":
				data = Rack.executeQuery('select distinct r.tag from Rack r where r.source = 0 and r.project=?', [project])
				break
			case "isValidRack":
				data = Rack.findAllWhere(tag: value, source: 0, project: project)
				break
			case "uposition":
				def rack = Rack.findWhere(tag: params.rack, source: 0, project: project)
				data = rack?.targetAssets?.targetRackPosition
				break
			case "isValidUposition":
				def rack = Rack.findWhere(tag: params.rack, source: 0, project: project)
				data = rack?.targetAssets?.findAll { it.targetRackPosition == Integer.parseInt(params.value) }
				break
			case "connector":
				def rack = Rack.findWhere(tag: params.rack, source: 0, project: project)
				def assetEntity = rack?.targetAssets?.findAll {
					it.targetRackPosition == Integer.parseInt(params.uposition)
				}
				if (assetEntity?.model[0]) {
					data = ModelConnector.findAllByModel(assetEntity?.model[0])?.label
				}
				break
			case "isValidConnector":
				def rack = Rack.findWhere(tag: params.rack, source: 0, project: project)
				def assetEntity = rack?.targetAssets?.findAll {
					it.targetRackPosition == Integer.parseInt(params.uposition)
				}
				def modelConnectors
				if (assetEntity?.model[0]) {
					modelConnectors = ModelConnector.findAllByModel(assetEntity?.model[0])
				}
				data = modelConnectors?.findAll { it.label.equalsIgnoreCase(params.value) }
				break
		}

		renderAsJson(data ?: [])
	}

	/*
	 *  Generate Cabling diagram for given asset
	 */
	@HasPermission(Permission.RackView)
	def generateCablingLayout(assetEntity, backView) {

		def cableDiagram = ""
		if (assetEntity.model && ModelConnector.findByModel(assetEntity.model)) {
			if (backView) {
				cableDiagram = new StringBuilder("<table style='border:0;' cellpadding='0' cellspacing='0'><tr><td style='border:0;padding:0;'>")
				if (assetEntity.model.rearImage && assetEntity.model.useImage == 1) {
					cableDiagram.append("<div class='cablingPanel' style='height:auto;background-color:#FFF'>")
					cableDiagram.append("<img src=\'${createLink(controller: 'model', action: 'retrieveRearImage', id: assetEntity?.model?.id)}\' />")
				}
				else {
					cableDiagram.append("<div class='cablingPanel' style='height: " + (assetEntity?.model?.usize ? assetEntity?.model?.usize * 30 : 30) + "px'>")
				}
				def assetCableMapList = AssetCableMap.findAllByAssetFrom(assetEntity)
				assetCableMapList.each { assetCable ->
					cableDiagram.append("<div style=\"top:${assetCable.assetFromPort.connectorPosY / 2}px ;left:${assetCable.assetFromPort.connectorPosX}px\">")
					def cableStatusImgUrl = HtmlUtil.createLinkToResource([dir: "i/cabling", file: "${assetCable.cableStatus}.png", absolute: true])
					cableDiagram.append("<div><img src=\"${cableStatusImgUrl}\"/></div>")
					cableDiagram.append("<div class=\"connector_${assetCable.assetFromPort.labelPosition}\"><span>${assetCable.assetFromPort.label}</span> </div>")
					cableDiagram.append('</div>')
				}
				cableDiagram.append("</div></td></tr></table>")
			}
			else {
				if (assetEntity.model.frontImage) {
					cableDiagram = new StringBuilder("<table style='border:0;' cellpadding='0' cellspacing='0'><tr><td style='border:0;padding:0;'>")
					cableDiagram.append("<div class='cablingPanel' style='height:auto;background-color:#FFF'>")
					cableDiagram.append("<img src=\'${createLink(controller: 'model', action: 'retrieveFrontImage', id: assetEntity?.model?.id)}\' />")
					cableDiagram.append("</div></td></tr></table>")
				}
			}
		}
		return cableDiagram
	}

	/**
	 * Saves 'ShowAddIcons' Preference
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def savePreference() {
		def preference = params.preference
		if (params.add == "true") {
			userPreferenceService.setPreference(preference, "true")
		}
		else {
			userPreferenceService.removePreference(preference)
		}

		render true
	}

	/**
	 * Assigning power automatically  through the devices in the rack connecting each to power.
	 * If the model has one power connector it goes to A.
	 * If two connectors, the second connects to B and so on.
	 * Set the color for the power connection to black.
	 * If the connector is already connected to power, don't change that one.
	 * In some cases a pair of devices might be connected to opposite power sources (one A and the other B power)
	 *
	 * @param rackId - id of requested rack.
	 * @return -  flash message
	 */
	@HasPermission(Permission.RackEdit)
	def assignPowers() {
		def rack
		if (params.roomId) {
			Rack.findAllByRoom(Room.load(params.roomId), [sort: "tag"]).each { r ->
				rack = rackService.assignPowerForRack(r.id)
			}
		}
		else {
			rack = rackService.assignPowerForRack(params.rackId)
		}
		render "Rack ${rack.tag} wired"
	}

	/**
	 * this action is used to get info. of racks power cabling
	 * @param : moveBundle[] : list of multiple bundle
	 * @param : sourcerack[] : list of source racks
	 * @param : targetrack[] : list of target racks
	 * @return : json list
	 */
	@HasPermission(Permission.RackView)
	def retrieveAssignedCables() {
		List<String> bundleIds = request.getParameterValues("moveBundle[]")
		Project project = securityService.userCurrentProject
		List<MoveBundle> moveBundles = MoveBundle.findAllByProject(project)
		def rackId = params.rackId
		def racks = []
		if (!rackId) {
			if (bundleIds && !bundleIds.contains("all")) {
				moveBundles = MoveBundle.getAll(bundleIds*.toLong()).findAll()
			}

			List<Rack> sourceRacks = findRacks('sourcerack[]', moveBundles, true)
			List<Rack> targetRacks = findRacks('targetrack[]', moveBundles, false)
			racks = sourceRacks + targetRacks
		}
		else if (racks.size() == 0 && rackId) {
			racks = Rack.getAll(rackId).findAll()
		}

		def resultMap = [:]
		racks.each { racksObj ->
			def flag = "notAssigned"
			racksObj.assets.each { asset ->
				def assetCablePowerList = AssetCableMap.findAllByAssetFrom(asset).findAll {
					it.assetFromPort.type == "Power"
				}
				resultMap[racksObj.id] = flag
				assetCablePowerList.each { assetCablePower ->
					if (assetCablePower.toPower) {
						flag = "Assigned"
						resultMap[racksObj.id] = flag
					}
				}
			}
		}

		renderAsJson(rackIds: racks.id, data: resultMap)
	}

	private List<Rack> findRacks(String paramName, List<MoveBundle> moveBundles, boolean source) {
		List<Rack> racks = []
		if (request.getParameterValues(paramName) != ['none']) {
			List<String> rackIds = request.getParameterValues(paramName)
			if (rackIds) {
				if (rackIds.contains("none")) {
					rackIds.remove("none")
				}
				if (rackIds[0] == "") {
					moveBundles.each { MoveBundle bundle ->
						(source ? bundle.sourceRacks : bundle.targetRacks).each { Rack rack ->
							if (!racks.contains(rack)) {
								racks.add(rack)
							}
						}
					}
				}
				else {
					for (Rack rack in Rack.getAll(rackIds*.toLong()).findAll()) {
						if (!racks.contains(rack)) {
							racks.add(rack)
						}
					}
				}
			}
			racks.sort { it.tag }
		}
		racks
	}
}
