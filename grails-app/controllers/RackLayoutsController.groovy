import grails.converters.JSON

import org.apache.poi.hssf.record.formula.functions.T
import org.apache.shiro.SecurityUtils

import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdssrc.grails.GormUtil
import org.apache.commons.lang.math.NumberUtils

class RackLayoutsController {
	def userPreferenceService
	def jdbcTemplate
	def supervisorConsoleService
	def sessionFactory
	def taskService
	def assetEntityService
	def securityService
	
	def static final statusDetails = ["missing":"Unknown", "cabledDetails":"Assigned","empty":"Empty","cabled":"Cabled"]
	
	def create = {
		def targetRack= ""
		def sourceRack= ""
		def bundle= ""
		def rackFilters
		def frontCheck = false
		def backCheck = true
		def wBundleCheck = true
		def woBundleCheck = true
		def wDCheck = false
		if(session.getAttribute("USE_FILTERS")=="true"){
			rackFilters = session.getAttribute( "RACK_FILTERS")
			if(rackFilters){
				targetRack = rackFilters?.targetrack ? rackFilters?.targetrack?.toString().replace("[","").replace("]","") : ""
				sourceRack = rackFilters?.sourcerack ? rackFilters?.sourcerack?.toString().replace("[","").replace("]","") : ""
				bundle = rackFilters?.moveBundle ? rackFilters?.moveBundle?.toString().replace("[","").replace("]","") : ""
				frontCheck = rackFilters?.frontView ? true : false
				backCheck = rackFilters?.backView ? true : false
				wBundleCheck = rackFilters?.bundleName ? true : false
				woBundleCheck = rackFilters?.otherBundle ? true : false
				wDCheck = rackFilters?.showCabling ? true : false
				
			}
		}
		
		def currProj = getSession().getAttribute( "CURR_PROJ" )
		def projectId = currProj.CURR_PROJ
		if ( projectId && projectId.isNumber() ) {
			def project = Project.findById( projectId )
			def moveBundleList = MoveBundle.findAllByProject( project )
			userPreferenceService.loadPreferences("CURR_BUNDLE")
			def currentBundle = getSession().getAttribute("CURR_BUNDLE")?.CURR_BUNDLE
			/* set first bundle as default if user pref not exist */
			def isCurrentBundle = true
			def subject = SecurityUtils.subject
			def models = AssetEntity.findAll('FROM AssetEntity WHERE project = ? GROUP BY model',[ project ])?.model
			def entities = assetEntityService.entityInfo( project )
			
			if(!currentBundle){
				currentBundle = moveBundleList[0]?.id?.toString()
				isCurrentBundle = false
			}
			session.removeAttribute("USE_FILTERS")
			session.removeAttribute("RACK_FILTERS")
			return [moveBundleList: moveBundleList, projectInstance:project, projectId:projectId,
					currentBundle:currentBundle, isCurrentBundle : isCurrentBundle, models:models ,servers:entities.servers, 
					applications : entities.applications, dbs : entities.dbs, files : entities.files,networks : entities.networks, rackFilters:rackFilters, targetRackFilter:targetRack,
					bundle:bundle,sourceRackFilter:sourceRack,rackLayoutsHasPermission:RolePermissions.hasPermission("rackLayouts"),
					staffRoles:taskService.getRolesForStaff(), dependencyType:entities.dependencyType, dependencyStatus:entities.dependencyStatus,
					frontCheck:frontCheck, backCheck:backCheck, wBundleCheck:wBundleCheck, woBundleCheck:woBundleCheck, wDCheck:wDCheck]
		} else {
			flash.message = 'You must have a project selected before using this feature'
			redirect(controller: "project", action: "list",params:[viewType : "list"])
		}
	}
	
	def save = {
		session.setAttribute( "RACK_FILTERS", params )
		List bundleId = request.getParameterValues("moveBundle")
		def maxUSize = 42
		if(bundleId == "null") {
			return [errorMessage: "Please Select a Bundle."]
		} else {
			def redirectTo = 'rack'
			def includeOtherBundle = params.otherBundle
			def includeBundleName = params.bundleName
			def printQuantity = params.printQuantity
			def frontView = params.frontView
			def backView = params.backView
			def sourceRacks = new ArrayList()
			def targetRacks = new ArrayList()
			def projectId = getSession().getAttribute("CURR_PROJ").CURR_PROJ
			def rackLayout = []
			def project = Project.findById(projectId)
			def moveBundles = MoveBundle.findAllByProject( project )
			def rackId = params.rackId
			def hideIcons = params.hideIcons
			
			if(bundleId && !bundleId.contains("all")){
				def bundlesString = bundleId.toString().replace("[","(").replace("]",")")
				moveBundles = MoveBundle.findAll("from MoveBundle m where id in ${bundlesString} ")
			}
			def rackLayoutsHasPermission = RolePermissions.hasPermission("EditAssetInRackLayout")
			/*if( !rackLayoutsHasPermission ) {
				rackLayoutsHasPermission =RolePermissions.hasPermission("rackLayouts")
			}*/
			
			if(request && request.getParameterValues("sourcerack") != ['none']) {
				List rack = request.getParameterValues("sourcerack")
				if(rack){
					if(rack.contains("none")){
						rack.remove("none")
					}
					if(rack && rack[0] == "") {
						moveBundles.each{ bundle->
							bundle.sourceRacks.each{ sourceRack->
								if( !sourceRacks.contains( sourceRack ) )
									sourceRacks.add( sourceRack )		
							}
						}
					} else {
						rack.each {
							def thisRack = Rack.get(new Long(it))
							if( !sourceRacks.contains( thisRack ) )
								sourceRacks.add( thisRack )
						}
					}
				}
				sourceRacks = sourceRacks.sort { it.tag }
			}

			if(request && request.getParameterValues("targetrack") != ['none']) {
				List rack = request.getParameterValues("targetrack")
				if(rack){
					if(rack.contains("none")){
						rack.remove("none")
					}
					if(rack && rack[0] == "") {
						moveBundles.each{ bundle->
							bundle.targetRacks.each{ targetRack->
								if( !targetRacks.contains( targetRack ) )
									targetRacks.add( targetRack	)
							}
						}
					} else {
						rack.each {
							def thisRack = Rack.get(new Long(it))
							if( !targetRacks.contains( thisRack ) )
								targetRacks.add( thisRack )
						}
					}
				}
				targetRacks = targetRacks.sort { it.tag }
			}
			
			def racks = sourceRacks + targetRacks
			if(racks.size() == 0 && rackId){
				session.setAttribute('RACK_ID',rackId)
				redirectTo = 'room'
				racks = Rack.findAllById(rackId)
				//moveBundles = []
				bundleId = request.getParameterValues("moveBundleId")
				if(bundleId && !bundleId.contains("all") && !bundleId.contains("taskReady")){
					def moveBundleId = bundleId.collect{id->Long.parseLong(id)}
					moveBundles = MoveBundle.findAllByIdInList(moveBundleId)
				}
			}
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ	
			racks.each { rack ->
				maxUSize = rack?.model?.usize ?: 42
				def assetDetails = []
				def assetDetail = []
				def finalAssetList = []
				def racksByFilter
				if(includeOtherBundle){
					racksByFilter = rack.assets.findAll { it.assetType !='Blade' && it.project == project }.sort { rack?.source == 1 ? it.sourceRackPosition ? it.sourceRackPosition * -1 : 0 : it.targetRackPosition ? it.targetRackPosition * -1 : 0}
				} else {
					racksByFilter = rack.assets.findAll { it.assetType !='Blade' && moveBundles?.id?.contains(it.moveBundle?.id) && it.project == project }.sort { rack?.source == 1 ? it.sourceRackPosition ? it.sourceRackPosition * -1 : 0 : it.targetRackPosition ? it.targetRackPosition * -1 : 0}
				}
				racksByFilter.each { assetEntity ->
				
					def overlapError = false
					def rackPosition = rack.source == 1 ? assetEntity.sourceRackPosition : assetEntity.targetRackPosition
					if(rackPosition == 0 || rackPosition == null)
						rackPosition = 1
					
					def rackSize = assetEntity?.model?.usize == 0 || assetEntity?.model?.usize == null ? 1 : assetEntity?.model?.usize?.toInteger()
					def position = rackPosition + rackSize - 1
					def newHigh = position
					def newLow = rackPosition
					if(assetDetail.size() > 0) {
						def flag = true
						assetDetail.each { asset ->
							flag = true
							def currentHigh = asset.currentHigh
							def currentLow = asset.currentLow
							def ignoreLow = (currentLow <= newLow && currentHigh >= newHigh )
							def changeBoth = (currentLow >= newLow && currentHigh <= newHigh )
							def changeLow = (currentLow >= newLow && currentHigh >= newHigh && currentLow <= newHigh)
							def changeHigh = (currentLow <= newLow && currentHigh <= newHigh && currentHigh <= newLow)
							if(position > maxUSize) {
								asset.position = maxUSize
								asset.rowspan = 1 
								asset.assetTag = asset.assetTag +"<br/>"+assetEntity.assetTag+ ' ~- ' + assetEntity.assetName
								asset.overlapError = true
								asset.cssClass = "rack_error"
								flag = false
							} else if(ignoreLow) {
								asset.position = currentHigh
								asset.rowspan = currentHigh - currentLow + 1 
								asset.assetTag = asset.assetTag +"<br/>"+assetEntity.assetTag+ ' ~- ' + assetEntity.assetName
								asset.overlapError = true
								asset.cssClass = "rack_error"
								flag = false
							} else if(changeBoth) {
								asset.currentHigh = newHigh
								asset.currentLow = newLow
								asset.position = newHigh
								asset.rowspan = newHigh - newLow + 1
								asset.assetTag = asset.assetTag +"<br/>"+assetEntity.assetTag+ ' ~- ' + assetEntity.assetName
								asset.overlapError = true
								asset.cssClass = "rack_error"
								flag = false
							} else if(changeHigh) {
								asset.currentHigh = newHigh
								asset.position = newHigh
								asset.rowspan = newHigh - currentLow  + 1
								asset.assetTag = asset.assetTag +"<br/>"+assetEntity.assetTag+ ' ~- ' + assetEntity.assetName
								asset.overlapError = true
								asset.cssClass = "rack_error"
								flag = false
							} else if(changeLow) {
								asset.currentLow = newLow
								asset.position = currentHigh
								asset.rowspan = currentHigh - newLow +1
								asset.assetTag = asset.assetTag +"<br/>"+assetEntity.assetTag+ ' ~- ' + assetEntity.assetName
								asset.overlapError = true
								asset.cssClass = "rack_error"
								flag = false
							}
						}
							
						if(flag) {
							if(position > maxUSize) {
								position = maxUSize
								newLow = maxUSize
								//assetEntity?.model?.usize = 1
								overlapError = true
							}
							assetDetail << [assetEntity:assetEntity, assetTag:assetEntity.assetTag + ' ~- ' + assetEntity.assetName, position:position, overlapError:overlapError, 
											rowspan:rackSize, currentHigh : position, currentLow : newLow, source:rack.source ]
						}
					} else {
						if(position > maxUSize) {
							position = maxUSize
							newLow = maxUSize
							//assetEntity?.model?.usize = 1
							overlapError = true
						}
						assetDetail << [assetEntity:assetEntity, assetTag:assetEntity.assetTag + ' ~- ' + assetEntity.assetName, position:position, overlapError:overlapError, 
										rowspan:rackSize, currentHigh : position, currentLow : newLow, source:rack.source ]
					}
				}
				for (int i = maxUSize; i > 0; i--) {
					def cssClass = "empty"
					def rackStyle = "rack_past"
					def assetEnitiesAtPosition = assetDetail.findAll { it.position == i }

					if(assetEnitiesAtPosition.size() > 1) {
						cssClass = 'rack_error'
						rackStyle = 'rack_error'
	            		assetDetails<<[asset:assetEnitiesAtPosition[0], rack:i, cssClass:cssClass, rackStyle:rackStyle, source:rack.source ]
					} else if(assetEnitiesAtPosition.size() == 1) {
						def assetEnity = assetEnitiesAtPosition[0]
						def currentTime = GormUtil.convertInToGMT( "now", tzId ).getTime()
						if(assetEnity.overlapError) {
							cssClass = 'rack_error'
							rackStyle = 'rack_error'
						} else if(bundleId && !moveBundles?.id?.contains(assetEnity.assetEntity?.moveBundle?.id) ) {
							def startTime = assetEnity.assetEntity?.moveBundle?.startTime ? assetEnity.assetEntity?.moveBundle?.startTime.getTime() : 0
							if(startTime < currentTime){
								cssClass = 'rack_past'
							} else {
								cssClass = "rack_future"
							}
						} else if(rackId && !moveBundles?.id?.contains(assetEnity.assetEntity?.moveBundle?.id) ) {
							def startTime = assetEnity.assetEntity?.moveBundle?.startTime ? assetEnity.assetEntity?.moveBundle?.startTime.getTime() : 0
							if(startTime < currentTime){
								cssClass = 'rack_past'
							} else {
								cssClass = "rack_future"
							}
						} else  {
							cssClass = 'rack_current'
							rackStyle = 'rack_current'
						}
						if(assetEnity.position == 0 || assetEnity.assetEntity?.model?.usize == 0 || assetEnity.assetEntity?.model?.usize == null ) {
							rackStyle = 'rack_error'
						}
						assetDetails << [asset:assetEnity, rack:i, cssClass:cssClass, rackStyle:rackStyle, source:rack.source, rackDetails:rack ]
					} else {
						assetDetails << [asset:null, rack:i, cssClass:cssClass, rackStyle:rackStyle, source:rack.source, rackDetails:rack ]
					}
				}
				def backViewRows
				def frontViewRows
				def paramsMap = [:]
				paramsMap = [ "rackLayoutsHasPermission" : rackLayoutsHasPermission, "assetDetails":assetDetails, "includeBundleName": includeBundleName,
								"backView":backView, "showCabling":params.showCabling, "hideIcons":hideIcons, "redirectTo":redirectTo, "rackId":rack.id,
								"forWhom":params.forWhom, "commit":params.commit, "bundle":moveBundles]
				
				if(backView) {
					backViewRows = getRackLayout(paramsMap)
				}
				if(frontView) {
					paramsMap["backView"] = null
					frontViewRows = getRackLayout(paramsMap)
				}
				rackLayout << [ assetDetails : assetDetails, rack : rack.tag , room : rack.room,
								frontViewRows : frontViewRows, backViewRows : backViewRows , rackId :rack.id]
			}
			def showIconPref = userPreferenceService.getPreference("ShowAddIcons")
			return [rackLayout : rackLayout, frontView : frontView, backView : backView, showIconPref:showIconPref, commit:params.commit]
		}
	}
	
	def getRackDetails = {
		def bundleIds = params.bundles
		def moveBundles = []
		if(bundleIds.contains('all')){
			def projectId = getSession().getAttribute("CURR_PROJ").CURR_PROJ
			moveBundles = MoveBundle.findAllByProject( Project.get( projectId ) )
		} else if( bundleIds ){
			moveBundles = MoveBundle.findAll( "from MoveBundle m where m.id in ($bundleIds)" )
		}
		
		def sourceRacks = new ArrayList()
		def targetRacks = new ArrayList()
		
		moveBundles.each{moveBundle ->
			moveBundle.sourceRacks.each{
				if( !sourceRacks.contains([id:it.id,location:it.location,room:it.room?.roomName,tag:it.tag]) )
					sourceRacks.add([id:it.id,location:it.location,room:it.room?.roomName,tag:it.tag])
			}
			moveBundle.targetRacks.each{
				if( !targetRacks.contains([id:it.id,location:it.location,room:it.room?.roomName,tag:it.tag]) )
					targetRacks.add([id:it.id,location:it.location,room:it.room?.roomName,tag:it.tag])
			}
		}
		
		def rackDetails = [[sourceRackList:sourceRacks, targetRackList:targetRacks]]
		render rackDetails as JSON
	}

	private getRackLayout( paramsMap ){
		def rows = new StringBuffer()
		def rowspan = 1
		def cssClass = "empty"
		def rackStyle = ""
		def showIconPref = userPreferenceService.getPreference("ShowAddIcons")
		
		def rackLayoutsHasPermission = paramsMap.rackLayoutsHasPermission
		def asset = paramsMap.assetDetails
		def includeBundleName = paramsMap.includeBundleName
		def backView = paramsMap.backView
		def showCabling = paramsMap.showCabling
		def hideIcons =  paramsMap.hideIcons
		def redirectTo = paramsMap.redirectTo
		def rackId = paramsMap.rackId
		def forWhom = paramsMap.forWhom
		def commit = paramsMap.commit
		
		asset.each {
			def row = new StringBuffer("<tr>")
			if(it.asset) {
				rowspan = it.asset?.rowspan != 0 ? it.asset?.rowspan : 1
				rackStyle = it.rackStyle
				def location = it.source
				def assetEntity = it.asset?.assetEntity
				def assetTagsList = (it.asset?.assetTag).split("<br/>")
				def moveBundle = "" 
				def assetTag = ""
				if(it.cssClass == "rack_error")
					assetTag += "Devices Overlap:<br />"
				def hasBlades = false
				def cabling = ""
				assetTagsList.each{ assetTagValue ->
					def index = assetTagValue.indexOf('~-')
					def tagValue
					if (index != -1) {
						tagValue = assetTagValue?.substring(0,index)
					} else {
						tagValue = assetTagValue
					}
					def overlappedAsset
					def overlappedAssets
					def bladeTable = ""
					def bladeLayoutMap = ['asset':it, 'permission':rackLayoutsHasPermission, 'hideIcons':hideIcons, 'redirectTo':redirectTo ,
											'rackId':rackId, 'commit':commit, 'forWhom':forWhom, "bundle": paramsMap.bundle]
					if(location == 1)
						overlappedAssets = AssetEntity.findAllWhere( project:assetEntity.project, assetTag : tagValue, sourceRack: assetEntity.sourceRack )
					else 
						overlappedAssets = AssetEntity.findAllWhere( project:assetEntity.project, assetTag : tagValue, targetRack: assetEntity.targetRack )
					if(overlappedAssets.size() > 1) {
						overlappedAssets.each{ overlapAsset ->
							moveBundle += (overlapAsset?.moveBundle ? overlapAsset?.moveBundle.name : "") + "<br/>"
							if(overlapAsset.model && overlapAsset.model.assetType == 'Blade Chassis' && (!backView || showCabling != 'on')){
								hasBlades = true
								bladeLayoutMap << ['overlappedAsset':overlapAsset]
								bladeTable = generateBladeLayout(bladeLayoutMap)
							}
							assetTag += """<a href="javascript:${forWhom ? "editAudit('roomAudit','${it.source}'" : "getEntityDetails('"+redirectTo+"'"},'${overlapAsset?.assetType}',${overlapAsset?.id})" >"""+trimString(assetTagValue.replace('~-','-'))+"</a>"
							if(hasBlades){
								assetTag += "<br/>"+bladeTable
							}
						}
					} else if(overlappedAssets.size() > 0){
						overlappedAsset = overlappedAssets[0]
						moveBundle += (overlappedAsset?.moveBundle ? overlappedAsset?.moveBundle.name : "") + "<br/>"
						if(overlappedAsset.model && overlappedAsset.model.assetType == 'Blade Chassis' && (!backView || showCabling != 'on') ){
							hasBlades = true
							bladeLayoutMap << ['overlappedAsset':overlappedAsset]
							bladeTable = generateBladeLayout(bladeLayoutMap)
						}
						cabling = !assetTag.contains("Devices Overlap") && showCabling == 'on' ? generateCablingLayout( overlappedAsset, backView ) : ""
						assetTag += """<a href="javascript:${forWhom ? "editAudit('roomAudit','${it.source}'" : "getEntityDetails('"+redirectTo+"'"},'${overlappedAsset?.assetType}',${overlappedAsset?.id})" >"""+trimString(assetTagValue.replace('~-','-'))+"</a>&nbsp;"
						
						if(hasBlades){
							assetTag += "<br/>"+bladeTable
						}
					}
				}
				if(backView) {
					def tasks = AssetComment.findAllByAssetEntityAndStatusInList(it.asset?.assetEntity, [AssetCommentStatus.STARTED, AssetCommentStatus.READY, AssetCommentStatus.HOLD])
					def taskAnchors = ""
					tasks.each{
						taskAnchors+="""<a href='#' class='${taskService.getCssClassForRackStatus(it.status)}' title='${it.taskNumber+':'+it.comment}' 
							onclick=\"javascript:showAssetComment(${it.id},'show')\" > &nbsp;&nbsp;&nbsp;&nbsp; </a> &nbsp;"""
					}
					if(cabling != "" && it.cssClass != "rack_error"){
						def assetCables = AssetCableMap.findByAssetFrom(it.asset?.assetEntity)
						if( hasBlades && showCabling != 'on'){
							row.append("<td class='${it.rackStyle}'>${it.rack}</td><td colspan='2' rowspan='${rowspan}' class='${it.cssClass}'>${assetTag}</td>")
							if ( assetCables ){
								row.append("""<td rowspan='${rowspan}' class='${it.cssClass}'><a href='#' 
										onclick='openCablingDiv(${it.asset?.assetEntity.id})'>cable</a> 
										&nbsp${taskAnchors}</td>""")
							}else
								row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>&nbsp;${taskAnchors}</td>")
						} else {
							row.append("<td class='${it.rackStyle}'>${it.rack}</td><td rowspan='${rowspan}' colspan='3' class='${it.cssClass}'>")
							row.append("<table style='border:0;' cellpadding='0' cellspacing='0'><tr><td style='border:0;'>${assetTag}</td>")
							
							if(includeBundleName)
								row.append("<td style='border:0;'>${moveBundle}</td>")
							else
								row.append("<td style='border:0;'>&nbsp;</td>")
							if ( assetCables )
								row.append("<td style='border:0;'><a href='#' onclick='openCablingDiv(${it.asset?.assetEntity.id})'>cable &nbsp; ${taskAnchors}</a></td></tr>")
							else
								row.append("<td style='border:0;'>&nbsp;${taskAnchors}</td></tr>")
								
							row.append("<tr><td colspan='3' style='border:0;'>${cabling}</td></tr></table></td>")	
						}
					} else {
						if( hasBlades && showCabling != 'on'){
							row.append("<td class='${it.rackStyle}'>${it.rack}</td><td colspan='2' rowspan='${rowspan}' class='${it.cssClass}'>${assetTag}</td>")
						} else {
							row.append("<td class='${it.rackStyle}'>${it.rack}</td><td rowspan='${rowspan}' class='${it.cssClass}'>${assetTag}${cabling}</td>")
							if(includeBundleName)
								row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>${moveBundle}</td>")
							else
								row.append("<td rowspan='${rowspan}' class='${it.cssClass}'></td>")
						}
						if(it.cssClass != "rack_error") {
							def assetCables = AssetCableMap.findByAssetFrom(it.asset?.assetEntity)
							if ( assetCables )
								row.append("<td rowspan='${rowspan}' class='${it.cssClass}'><a href='#' onclick='openCablingDiv(${it.asset?.assetEntity.id})'>cable ${taskAnchors}</a></td>")
							else
								row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>&nbsp; ${taskAnchors}</td>")
							
						} else {
							row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>Devices Overlap</td>")
						}
					}
				} else {
					if( hasBlades ){
						row.append("<td class='${it.rackStyle}'>${it.rack}</td><td colspan='2' rowspan='${rowspan}' class='${it.cssClass}'>${assetTag}</td>")
					} else if(cabling != ""){
						row.append("<td class='${it.rackStyle}'>${it.rack}</td><td rowspan='${rowspan}' colspan='2' class='${it.cssClass}'>")
						row.append("<table style='border:0;' cellpadding='0' cellspacing='0'><tr><td style='border:0;'>${assetTag}</td>")
						if(includeBundleName)
							row.append("<td style='border:0;'>${moveBundle}</td></tr>")
						else
							row.append("<td style='border:0;'>&nbsp;</td></tr>")
						row.append("<tr><td colspan='2' style='border:0;'>${cabling}</td></tr></table></td>")
						
					} else {
						row.append("<td class='${it.rackStyle}'>${it.rack}</td><td rowspan='${rowspan}' class='${it.cssClass}'>${assetTag}</td>")
						if(includeBundleName)
							row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>${moveBundle}</td>")
						else
							row.append("<td rowspan='${rowspan}' class='${it.cssClass}'></td>")
					}
				}
			} else if(rowspan <= 1) {
				rowspan = 1
				rackStyle = it.rackStyle
				row.append("<td class='empty' nowrap>${it.rack}</td><td rowspan=1 class=${it.cssClass}>")
				if (commit!="Print View") {
				def roomParameter = forWhom ? it.rackDetails.room?.roomName : it.rackDetails.room?.id
				def rackParameter = forWhom ? it.rackDetails?.tag : it.rackDetails.id
				row.append("""<div ${showIconPref ? '' : 'style="display:none"'}  class="rack_menu create_${rackId}"><img src="../i/rack_add2.png">
							<ul>
								<li><a href="javascript:${forWhom ? "createAuditPage" : "createAssetPage"}('Server','${it.source}','${rackParameter}','${roomParameter}','${it.rackDetails.location}','${it.rack}')">Create asset  </a></li>
								<li><a href="javascript:listDialog('','','asc','${it.source}','${it.rackDetails.id}','${it.rackDetails.room?.id}','${it.rackDetails.location}','${it.rack}')">Assign asset </a></li>
								<li><a href="javascript:listDialog('all','','asc','${it.source}','${it.rackDetails.id}','${it.rackDetails.room?.id}','${it.rackDetails.location}','${it.rack}')">Reassign asset </a></li>
							</ul></img></div>&nbsp;""")
				}
				row.append("</td><td>&nbsp;</td>")
				if(backView)
					row.append("<td>&nbsp;</td>")
				
			} else {
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
	/*************************************************
	 * Construct Balde layout for RackLayouts report
	 **************************************************/
	def generateBladeLayout(bladeLayoutMap){
		
		def assetDetails = bladeLayoutMap.asset
		def assetEntity = bladeLayoutMap.overlappedAsset
		def rackLayoutsHasPermission = bladeLayoutMap.permission
		def hideIcons = bladeLayoutMap.hideIcons
		def redirectTo = bladeLayoutMap.redirectTo
		def rackId = bladeLayoutMap.rackId
		def commit = bladeLayoutMap.commit
		def forWhom = bladeLayoutMap.forWhom
		def bundles = bladeLayoutMap.bundle
		
		def showIconPref = userPreferenceService.getPreference("ShowAddIcons")
		def bladeTable = '<table class="bladeTable"><tr>'
		def rowspan = assetDetails.asset?.rowspan != 0 ? assetDetails.asset?.rowspan : 1
		def tdHeight = rowspan * 6
		def blades = []
		if(assetDetails.asset.source == 1)
			blades = AssetEntity.findAllWhere(project:assetEntity.project, assetType:'Blade', sourceBladeChassis:assetEntity.assetTag).findAll{it?.moveBundle?.id in bundles?.id}
		else
			blades = AssetEntity.findAllWhere(project:assetEntity.project, assetType:'Blade', targetBladeChassis:assetEntity.assetTag).findAll{it?.moveBundle?.id in bundles?.id}

		def fullRows = []
		def chassisRows = assetEntity.model.bladeRows
		def bladesPerRow = (assetEntity.model.bladeCount / chassisRows ).intValue()
		def bladeLabelCount = assetEntity.model.bladeLabelCount

		for(int k = 1; k <= chassisRows; k++){
			int initialColumn = (k-1)*bladesPerRow + 1
			for(int i = initialColumn; i <= k*bladesPerRow; i++){
				def matching = []
				if(assetDetails.asset.source == 1)
					matching = blades.findAll { it.sourceBladePosition == i }
				else
					matching = blades.findAll { it.targetBladePosition == i }
				
				if(fullRows.contains(i))
					bladeTable += ''
				else if(matching.size() > 1)
					bladeTable += "<td class='errorBlade' style='height:${tdHeight}px'>Conflict</td>"
				else if(matching.size() == 1) {
					def blade = matching[0]
					def tag = blade.assetTag
					if(tag.length() >= bladeLabelCount){
						tag = tag.substring(0,bladeLabelCount)
					}
					tag = tag.split('')[1..-1].join('<br/>')
					def taglabel = "<div>"+tag.substring(0,tag.length())+"</div>"
					def bladeSpan = blade.model?.bladeHeight == 'Full' ? chassisRows : 1
					if(bladeSpan == chassisRows){
						for(int y = i; y <= chassisRows*bladesPerRow; y += bladesPerRow ){
							fullRows << y + bladesPerRow
						}
					}
					def hasError = assetDetails.asset.source == 1 ? blades.findAll { it.sourceBladePosition == i + bladeLabelCount }.size() > 0 : blades.findAll { it.targetBladePosition == i + bladeLabelCount }.size() > 0
					if((bladeSpan == 2) &&  hasError )
						bladeTable += "<td class='errorBlade' style='height:${tdHeight}px'>&nbsp;</td>"
					else
						bladeTable += """<td class='blade' rowspan='${bladeSpan}' style='height:${tdHeight}px'><a href="javascript:${forWhom ? "editAudit('roomAudit','${assetDetails.source}'" : "getEntityDetails('${redirectTo}'" },'Blade',${blade.id})" title='${tag.replace('<br/>','')}'>${taglabel}</a></td>"""
				} else {
					bladeTable += "<td class='emptyBlade' style='height:${tdHeight}px'>"
					if(commit !="Print View"){
						bladeTable += """<div ${showIconPref ? '' : 'style="display:none"'} class="rack_menu create_${rackId}"><img src="../i/rack_add2.png"/>
							<ul>
								<li><a href="javascript:${forWhom ? 'createBladeAuditPage' : 'createBladeDialog'}('${assetDetails.source}','${assetEntity.assetTag}','${i}','${assetEntity.manufacturer?.id}','Blade','${assetEntity?.id}','${assetEntity.moveBundle?.id}')">Create asset  </a></li>
								<li><a href="javascript:listBladeDialog('${assetDetails.source}','${assetEntity.assetTag}','${i}','assign')">Assign asset </a></li>
								<li><a href="javascript:listBladeDialog('${assetDetails.source}','${assetEntity.assetTag}','${i}','reassign')">Reassign asset </a></li>
							</ul>
						</div>"""
					}
					bladeTable += "</td>"
				}
			}
			bladeTable += k == chassisRows ? "</tr>" : "</tr><tr>"
		}
		
		bladeTable += '</table>'
	}
	/********************************************
	 * Trim Name if over 22, trim to 20 characters and add "...".
	 ********************************************/
	def private trimString( name ){
		def trimmedVal = name
		def length = name.length()
		if(length > 22){
			trimmedVal = trimmedVal.substring(0,20)+"..."
		}
		return trimmedVal
	}
	def modelTemplate = {
			return [params:params]
	}
	/*
	 * Return AssetCableMap record details to display at RackLayout cabling screen
	 */
	def getCablingDetails = {
			def project = securityService.getUserCurrentProject()
			def moveBundleList = MoveBundle.findAllByProject( project )
			userPreferenceService.loadPreferences("CURR_BUNDLE")
			def currentBundle = getSession().getAttribute("CURR_BUNDLE")?.CURR_BUNDLE
			/* set first bundle as default if user pref not exist */
			def isCurrentBundle = true
			def models = AssetEntity.findAll('FROM AssetEntity WHERE project = ? GROUP BY model',[ project ])?.model
			
			if(!currentBundle){
				currentBundle = moveBundleList[0]?.id?.toString()
				isCurrentBundle = false
			}
			def roomType = params.roomType 
			
		def assetId = params.assetId
		def assetEntity = assetId ? AssetEntity.get(assetId) : null
		def assetCableMapList
		def title =  ""
		if( assetEntity ){
			if(roomType =='T'){
				assetCableMapList = assetEntityService.createOrFetchTargetAssetCables(assetEntity)
			}else {
				assetCableMapList = AssetCableMap.findAllByAssetFromAndAssetLoc( assetEntity , 'S')
			}
			
			title = assetEntity.assetName+" ( "+assetEntity?.model?.manufacturer+" / "+assetEntity.model+" )"
		}
		def isTargetRoom = assetEntity.roomTarget ? true :false
		def dependencyStatus = AssetCableStatus.list
		def assetCablingDetails = []
		def assetCablingMap =[:]
		def modelConnectorMap =[:]
		def assetRows =[:]
		assetCableMapList.each {
			def connectorLabel = it.assetToPort ? it.assetToPort.label : ""
			def toAssetId
			def toTitle =  ""
			def powerA = 'power'
			def powerB = 'nonPower'
			if(it.assetFromPort.type == "Power"){
				connectorLabel = it.toPower ? it.toPower : ""
				powerA = 'nonPower'
				powerB = 'power'
			} else if(it.assetTo){
				toAssetId = it.assetTo.id
				toTitle = it.assetTo.assetName+" ( "+it.assetTo.model?.manufacturer+" / "+it.assetTo.model+" )"
			}
			if(it.assetLoc == 'S'){
				def sourceRack =it.assetFrom?.sourceRack
				title += sourceRack ? " ( "+sourceRack+" / "+it.assetFrom?.sourceRackPosition+" )" : ""
			}else{
				title += " ( "+it.assetFrom?.targetRack+" / "+it.assetFrom?.targetRackPosition+" )"
			}
			assetCablingDetails << [model:assetEntity.model?.id, id:it.id, connector : it.assetFromPort.connector, 
									type:it.assetFromPort.type, connectorPosX:it.assetFromPort.connectorPosX,
									labelPosition:it.assetFromPort.labelPosition, color : it.cableColor ? it.cableColor : "",
									connectorPosY:it.assetFromPort.connectorPosY, status:it.cableStatus,displayStatus:it.cableStatus, comment:it.cableComment?:'',
									label:it.assetFromPort.label, hasImageExist:assetEntity.model?.rearImage && assetEntity.model?.useImage ? true : false,
									usize:assetEntity?.model?.usize, rackUposition : connectorLabel, toAssetId : toAssetId, toAssetPortId: it.assetToPort?.id,
									fromAssetId :(it.assetTo? it.assetTo?.assetName+"/"+connectorLabel:''), toTitle:toTitle, title:title]
			
			assetCablingMap <<[(it.id):[label : it.assetFromPort.label, color :it.cableColor, type:it.assetFromPort.type, length:it.cableLength?:'',
									status:it.cableStatus,comment:it.cableComment?:'', fromAssetId :it.assetTo? it.assetTo?.id :'null',asset :it.assetTo? it.assetTo?.assetName :'',
									fromAsset:(it.assetTo? it.assetTo?.assetName+"/"+connectorLabel:'') ,toTitle:toTitle, title:title, rackUposition : connectorLabel , 
									connectorId: it.assetToPort ? it.assetToPort?.id : "null",type:it.assetFromPort.type, cableId:it.id, 
									locRoom: (it.assetLoc=='S') ? 'Current' : 'Target', roomType:it.assetLoc, powerA:powerA, powerB:powerB]]
			
			assetRows << [(it.id):'h']
		}
		render(template:"cabling", model:[assetCablingDetails:assetCablingDetails, currentBundle:currentBundle, assetCablingMap:(assetCablingMap as JSON),
										  models:models,assetRows:(assetRows as JSON),isTargetRoom:isTargetRoom, 
										  assetId:assetId, roomType:roomType])
	}
	/*
	 * Return modelConnectorList to display at connectors dropdown in  cabling screen
	 */
	def getAssetModelConnectors = {
		def jsonInput = request.JSON
		def roomType = jsonInput.roomType
		def assetId =jsonInput.asset
		def project = securityService.getUserCurrentProject()
		def assetEntity = assetId ? AssetEntity.get(assetId) : null
		def currRoomRackAssets = []
		if( roomType == 'T' ){
			currRoomRackAssets = AssetEntity.findAllByRoomTargetAndProject(assetEntity.roomTarget, project)?.findAll{it.model?.modelConnectors?.type?.contains(jsonInput.type)}
		} else {
			currRoomRackAssets = AssetEntity.findAllByRoomSourceAndProject(assetEntity.roomSource, project)?.findAll{it.model?.modelConnectors?.type?.contains(jsonInput.type)}
		}
		def currRackAssets = currRoomRackAssets.findAll{it.rackSource?.id == assetEntity.rackSource?.id || it.rackTarget?.id == assetEntity.rackTarget?.id}
		def sortedAssets = currRackAssets.sort{ it.assetName } + (currRoomRackAssets-currRackAssets).sort{ it.assetName }
		
		def modelConnectorMap =[:]
		currRoomRackAssets.each{asset ->
			def modelConnectMapList=[]
			def modelConnectList = AssetCableMap.findAllByAssetFromAndAssetLoc(asset,roomType)?.findAll{it.assetFromPort.type == jsonInput.type }
			modelConnectList.each{
				modelConnectMapList << ['value': it.assetFromPort.id, 'text': it.assetFromPort.label]
			}
			modelConnectorMap << [(asset.id):modelConnectMapList]
		}
		def output = ['connectors': modelConnectorMap, 'assets' : sortedAssets]
		render output as JSON
	}
	/*
	 * Update the AssetCablingMap with the date send from RackLayout cabling screen
	 */
	def updateCablingDetails = {
		def jsonInput = request.JSON
		def assetId = NumberUtils.toDouble(jsonInput.assetId,0).round() 
		def assetCableId = jsonInput.assetCable
		def assetCableMap
		def toCableId
		if(assetCableId){
			def currProj = getSession().getAttribute( "CURR_PROJ" )
			def projectId = currProj.CURR_PROJ
			def project = Project.get( projectId )
			def actionType = jsonInput.actionType
			def status = jsonInput.status?: AssetCableStatus.UNKNOWN
			def toConnector
			def assetTo
			def toPower
			def connectorType = jsonInput.connectorType
			assetCableMap = AssetCableMap.findById( assetCableId )
		
			if(connectorType != "Power"){
				def fromAssetCableMap = AssetCableMap.find("from AssetCableMap where assetTo=? and assetToPort=? and assetLoc=?",
													[assetCableMap.assetFrom,assetCableMap.assetFromPort,jsonInput.roomType])
				if(fromAssetCableMap){
					AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus=?, assetTo=null,assetToPort=null, cableColor=null
												where assetTo = ? and assetToPort = ? and assetLoc=?""",[status, assetCableMap.assetFrom, assetCableMap.assetFromPort, jsonInput.roomType])
			}
			}
			switch(actionType){
				case "emptyId" : status = AssetCableStatus.EMPTY ; break;
				case "cabledId" : status = AssetCableStatus.CABLED ; break;
				case "assignId" : 
					if(connectorType != "Power"){
						if(jsonInput.assetFromId !='null'){
							def assetEntity = AssetEntity.findById(jsonInput.assetFromId)
							def modelConnectors
							if(assetEntity?.model){
								assetTo = assetEntity 
								toConnector = ModelConnector.findById( jsonInput.modelConnectorId )
								toCableId = AssetCableMap.find("from AssetCableMap where assetTo=? and assetToPort=? and assetLoc=?",
													[assetTo,toConnector,jsonInput.roomType])
								AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus=?,assetTo=null,
																assetToPort=null, cableColor=null
																where assetTo = ? and assetToPort = ? and assetLoc=?""",[status, assetTo, toConnector,jsonInput.roomType])
							}
						}
					} else {
						assetTo = assetCableMap.assetFrom
						toConnector = null
						toPower = jsonInput.staticConnector
					}
					break;
			}
			sessionFactory.getCurrentSession().flush();
	    	sessionFactory.getCurrentSession().clear();
			assetCableMap.cableStatus = status
			assetCableMap.assetTo = assetTo
			assetCableMap.assetToPort = toConnector
			assetCableMap.toPower = toPower
			assetCableMap.cableColor = jsonInput.color
			assetCableMap.cableLength = NumberUtils.toDouble(jsonInput.cableLength.toString(),0).round() 
			assetCableMap.cableComment = jsonInput.cableComment
			assetCableMap.assetLoc= jsonInput.roomType
			if(assetCableMap.save(flush:true)){
				if(assetTo && connectorType != "Power"){
					def toAssetCableMap = AssetCableMap.find("from AssetCableMap where assetFrom=? and assetFromPort=? and assetLoc=?",
													[assetTo,toConnector,jsonInput.roomType])
					toAssetCableMap.cableStatus = status
					toAssetCableMap.assetTo = assetCableMap.assetFrom
					toAssetCableMap.assetToPort = assetCableMap.assetFromPort
					toAssetCableMap.cableColor = jsonInput.color
					toAssetCableMap.cableLength = NumberUtils.toDouble(jsonInput.cableLength.toString(),0).round() 
					toAssetCableMap.cableComment = jsonInput.cableComment
					toAssetCableMap.assetLoc= jsonInput.roomType
					if(!toAssetCableMap.save(flush:true)){
						def etext = "Unable to create toAssetCableMap" +
		                GormUtil.allErrorsString( toAssetCableMap )
						println etext
					}
				}
			} else {
				def etext = "Unable to create FromAssetCableMap" +
                GormUtil.allErrorsString( assetCableMap )
				println etext
			}
		}
		def connectorLabel = assetCableMap.assetToPort ? assetCableMap.assetToPort.label : ""
		def powerA = 'power'
		def powerB = 'nonPower'
		if(assetCableMap.assetFromPort.type == "Power"){
			connectorLabel = assetCableMap.toPower ? assetCableMap.toPower : ""
			powerA = 'nonPower'
			powerB = 'power'
		}
		def assetCable = [ label : assetCableMap.assetFromPort.label, type:assetCableMap.assetFromPort.type, color :assetCableMap.cableColor, length:assetCableMap.cableLength?:'', asset :assetCableMap.assetTo? assetCableMap.assetTo?.assetName :'',
									status:assetCableMap.cableStatus,comment:assetCableMap.cableComment?:'', fromAssetId :assetCableMap.assetTo? assetCableMap.assetTo?.id :'',
									fromAsset:(assetCableMap.assetTo? assetCableMap.assetTo?.assetName+"/"+connectorLabel:'') , rackUposition : connectorLabel , 
									connectorId: assetCableMap.assetToPort ? assetCableMap.assetToPort.id : "" , toCableId:toCableId?.id, 
									locRoom: (assetCableMap.assetLoc=='S') ? 'Current' : 'Target', powerA:powerA,powerB:powerB]
		render assetCable as JSON
	}
	/*
	 *  Provide the Rack auto complete details and connector, uposition validation 
	 */
	def getAutoCompleteDetails = {
		def currProj = getSession().getAttribute( "CURR_PROJ" )
		def projectId = currProj.CURR_PROJ
		def project = Project.get( projectId )
		def data
		def field = params.field
		def value = params.value
		switch(field){
			case "rack" :
				data = Rack.executeQuery( "select distinct r.tag from Rack r where r.source = 0 and r.project = $projectId " )
				break;
			case "isValidRack":
				data = Rack.findAllWhere(tag:value,source:0,project:project)
				break;
			case "uposition":
				def rack = Rack.findWhere(tag:params.rack,source:0,project:project)
				data = rack?.targetAssets?.targetRackPosition
				break;
			case "isValidUposition":
				def rack = Rack.findWhere(tag:params.rack,source:0,project:project)
				data = rack?.targetAssets?.findAll{it.targetRackPosition == Integer.parseInt(params.value)} 
				break;
			case "connector":
				def rack = Rack.findWhere(tag:params.rack,source:0,project:project)
				def assetEntity = rack?.targetAssets?.findAll{it.targetRackPosition == Integer.parseInt(params.uposition)}
				def modelConnectors
				if(assetEntity?.model[0])
					data = ModelConnector.findAllByModel(assetEntity?.model[0])?.label
				break;
			case "isValidConnector":
				def rack = Rack.findWhere(tag:params.rack,source:0,project:project)
				def assetEntity = rack?.targetAssets?.findAll{it.targetRackPosition == Integer.parseInt(params.uposition)}
				def modelConnectors
				if(assetEntity?.model[0])
					modelConnectors = ModelConnector.findAllByModel(assetEntity?.model[0])
				data = modelConnectors?.findAll{it.label.equalsIgnoreCase(params.value) }
				break;
		}
		if(!data)
			data = []
		render data as JSON
	}
	/*
	 *  Generate Cabling diagram for given asset
	 */
	def generateCablingLayout( assetEntity, backView ){
		
		def cableDiagram =  ""
		if(assetEntity.model && ModelConnector.findByModel( assetEntity.model )){
			if(backView){
				cableDiagram = new StringBuffer("<table style='border:0;' cellpadding='0' cellspacing='0'><tr><td style='border:0;padding:0;'>")
				if(assetEntity.model.rearImage && assetEntity.model.useImage == 1){
					cableDiagram.append("<div class='cablingPanel' style='height:auto;background-color:#FFF'>")
					cableDiagram.append("<img src=\'${createLink(controller:'model', action:'getRearImage', id:assetEntity?.model?.id)}\' />")
				} else {
					cableDiagram.append("<div class='cablingPanel' style='height: "+(assetEntity?.model?.usize ? assetEntity?.model?.usize*30 : 30)+"px'>")
				}
				def assetCableMapList = AssetCableMap.findAllByAssetFrom( assetEntity )
				assetCableMapList.each {assetCable->
					cableDiagram.append("""<div style="top:${assetCable.assetFromPort.connectorPosY / 2}px ;left:${assetCable.assetFromPort.connectorPosX}px ">
												<div>
													<img src="../i/cabling/${assetCable.cableStatus}.png"/>
												</div>
												<div class="connector_${assetCable.assetFromPort.labelPosition}">
													<span>${assetCable.assetFromPort.label}</span>
										 		</div>
											</div>
										""")
				}
				cableDiagram.append("</div></td></tr></table>")
			} else {
				if( assetEntity.model.frontImage ){
					cableDiagram = new StringBuffer("<table style='border:0;' cellpadding='0' cellspacing='0'><tr><td style='border:0;padding:0;'>")
					cableDiagram.append("<div class='cablingPanel' style='height:auto;background-color:#FFF'>")
					cableDiagram.append("<img src=\'${createLink(controller:'model', action:'getFrontImage', id:assetEntity?.model?.id)}\' />")
					cableDiagram.append("</div></td></tr></table>")
				}
			}
		}
		return cableDiagram
	}
	/**
	 * This action is used for saving 'ShowAddIcons' Preference 
	 */
	def savePreference = {
		def preference = params.preference
		if(params.add == "true"){
			userPreferenceService.setPreference(preference, "true")
		} else {
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
	 *  @param rackId - id of requested rack.
	 *  @return -  flash message
	*/
	def assignPowers = {
		def rack
		if (params.roomId){
			def roomInstance = Room.read(params.roomId)
			def rackInstanceList = Rack.findAllByRoom(roomInstance , [sort:"tag"])
			rackInstanceList.each{ r ->
				rack = assignPowerForRack(r.id)
			}
		}else {
			rack = assignPowerForRack(params.rackId)
		}
		render "Rack ${rack.tag} wired"
	}
	/**
	 *This method is used  give power connection to a selected rack.
	 *  @param rackId - id of requested rack.
	 *  @return -  rack
	*/
	def assignPowerForRack(rackId){
		def rack = Rack.read(rackId)
		def rackAssets = rack.assets
		def toPowers = ["A","B","C"]
		rackAssets.each { asset->
			def assetCablePowerList = AssetCableMap.findAllByAssetFrom( asset ).findAll{it.assetFromPort.type == "Power"}
			assetCablePowerList = assetCablePowerList.size() > 3 ? assetCablePowerList[0..2] : assetCablePowerList
			assetCablePowerList.eachWithIndex{ assetCablePower, i ->
				if(!assetCablePower.toPower){
					assetCablePower.assetTo = assetCablePower.assetFrom
					assetCablePower.assetToPort = null
					assetCablePower.toPower = toPowers[i]
					assetCablePower.cableColor = 'Black'
					assetCablePower.cableStatus = 'cabledDetails'
					
					if(!assetCablePower.save(flush:true)){
						assetCablePower.errors.allErrors.each { println it }
					}
				}
			}
		}
		return rack
	}
	
	/**
	 * this action is used to get info. of racks power cabling
	 * @param : moveBundle[] : list of multiple bundle
	 * @param : sourcerack[] : list of source racks
	 * @param : targetrack[] : list of target racks
	 * @return : json list
	 */
	def getAssignedCables = {
		List bundleId = request.getParameterValues("moveBundle[]")
		def sourceRacks = new ArrayList()
		def targetRacks = new ArrayList()
		def project = securityService.getUserCurrentProject()
		def moveBundles = MoveBundle.findAllByProject( project )
		def rackId = params.rackId
		def racks = []
		if(!rackId) {
			if(bundleId && !bundleId.contains("all")){
				def bundlesString = bundleId.toString().replace("[","(").replace("]",")")
				moveBundles = MoveBundle.findAll("from MoveBundle m where id in ${bundlesString} ")
			}
			
			if(request && request.getParameterValues("sourcerack[]") != ['none']) {
				List rack = request.getParameterValues("sourcerack[]")
				if(rack){
					if(rack.contains("none")){
						rack.remove("none")
					}
					if(rack && rack[0] == "") {
						moveBundles.each{ bundle->
							bundle.sourceRacks.each{ sourceRack->
								if( !sourceRacks.contains( sourceRack ) )
									sourceRacks.add( sourceRack )
							}
						}
					} else {
						rack.each {
							def thisRack = Rack.get(new Long(it))
							if( !sourceRacks.contains( thisRack ) )
								sourceRacks.add( thisRack )
						}
					}
				}
				sourceRacks = sourceRacks.sort { it.tag }
			}
	
			if(request && request.getParameterValues("targetrack[]") != ['none']) {
				List rack = request.getParameterValues("targetrack[]")
				if(rack){
					if(rack.contains("none")){
						rack.remove("none")
					}
					if(rack && rack[0] == "") {
						moveBundles.each{ bundle->
							bundle.targetRacks.each{ targetRack->
								if( !targetRacks.contains( targetRack ) )
									targetRacks.add( targetRack	)
							}
						}
					} else {
						rack.each {
							def thisRack = Rack.get(new Long(it))
							if( !targetRacks.contains( thisRack ) )
								targetRacks.add( thisRack )
						}
					}
				}
				targetRacks = targetRacks.sort { it.tag }
			}
			racks = sourceRacks + targetRacks
		} else if(racks.size() == 0 && rackId){
			racks = Rack.findAllById(rackId)
		}
		def resultMap = [:]
		racks.each{ racksObj->
			def flag = "notAssigned"
			racksObj.assets.each { asset->
				def assetCablePowerList = AssetCableMap.findAllByAssetFrom( asset ).findAll{it.assetFromPort.type == "Power"}
				resultMap << [(racksObj.id) : flag]
				assetCablePowerList.each {assetCablePower->
					if(assetCablePower.toPower) {
						flag = "Assigned"
						resultMap << [(racksObj.id) : flag]
					}
				}
			}
		}
		def data = ['rackIds':racks.id,'data':resultMap]
		render  data as JSON
	}
}
