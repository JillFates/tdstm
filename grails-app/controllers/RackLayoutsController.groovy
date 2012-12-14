import grails.converters.JSON

import org.apache.poi.hssf.record.formula.functions.T
import org.jsecurity.SecurityUtils

import com.tds.asset.AssetCableMap
import com.tds.asset.AssetEntity
import com.tdssrc.grails.GormUtil
import com.tds.asset.Application
import com.tds.asset.Database
import com.tds.asset.Files
import com.tds.asset.AssetOptions

class RackLayoutsController {
	def userPreferenceService
	def jdbcTemplate
	def supervisorConsoleService
	def sessionFactory
	def taskService
	
	def static final statusDetails = ["missing":"Unknown", "cabledDetails":"Assigned","empty":"Empty","cabled":"Cabled"]
	
	def create = {
		def targetRack= ""
		def sourceRack= ""
		def bundle= ""
		def rackFilters
		def useCheck = false
		if(session.getAttribute("USE_FILTERS")=="true"){
		    useCheck = true
			rackFilters = session.getAttribute( "RACK_FILTERS")
			if(rackFilters){
				targetRack = rackFilters?.targetrack ? rackFilters?.targetrack?.toString().replace("[","").replace("]","") : ""
				sourceRack = rackFilters?.sourcerack ? rackFilters?.sourcerack?.toString().replace("[","").replace("]","") : ""
				bundle = rackFilters?.moveBundle ? rackFilters?.moveBundle?.toString().replace("[","").replace("]","") : ""
			}
		}
		
		def currProj = getSession().getAttribute( "CURR_PROJ" )
		def projectId = currProj.CURR_PROJ
		def projectInstance = Project.findById( projectId )
		def moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
		userPreferenceService.loadPreferences("CURR_BUNDLE")
		def currentBundle = getSession().getAttribute("CURR_BUNDLE")?.CURR_BUNDLE
		/* set first bundle as default if user pref not exist */
		def isCurrentBundle = true
		def subject = SecurityUtils.subject
		def models = AssetEntity.findAll('FROM AssetEntity WHERE project = ? GROUP BY model',[ projectInstance ])?.model
		def servers = AssetEntity.findAllByAssetTypeAndProject('Server',projectInstance)
		def applications = Application.findAllByAssetTypeAndProject('Application',projectInstance)
		def dbs = Database.findAllByAssetTypeAndProject('Database',projectInstance)
		def files = Files.findAllByAssetTypeAndProject('Files',projectInstance)
		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
		if(!currentBundle){
			currentBundle = moveBundleInstanceList[0]?.id?.toString()
			isCurrentBundle = false
		}
		session.removeAttribute("USE_FILTERS")
		session.removeAttribute("RACK_FILTERS")
		return [moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance, projectId:projectId,
				currentBundle:currentBundle, isCurrentBundle : isCurrentBundle, models:models ,servers:servers, 
				applications : applications, dbs : dbs, files : files, rackFilters:rackFilters, targetRackFilter:targetRack,
				bundle:bundle,sourceRackFilter:sourceRack,rackLayoutsHasPermission:RolePermissions.hasPermission("rackLayouts"),useCheck:useCheck,
				staffRoles:taskService.getRolesForStaff(), dependencyType:dependencyType, dependencyStatus:dependencyStatus]
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
				moveBundles = []
				bundleId = request.getParameterValues("moveBundleId")
				if(bundleId && !bundleId.contains("all")){
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
				if(backView) {
					backViewRows = getRackLayout(rackLayoutsHasPermission, assetDetails, includeBundleName, backView, params.showCabling, hideIcons, redirectTo)
				}
				if(frontView) {
					frontViewRows = getRackLayout(rackLayoutsHasPermission, assetDetails, includeBundleName, null, params.showCabling, hideIcons, redirectTo)
				}
				rackLayout << [ assetDetails : assetDetails, rack : rack.tag , room : rack.room,
								frontViewRows : frontViewRows, backViewRows : backViewRows ]
			}
			return [rackLayout : rackLayout, frontView : frontView, backView : backView]
		}
	}
	
	def getRackDetails = {
		def bundleIds = params.bundles
		def moveBundles
		if(bundleIds.contains('all')){
			def projectId = getSession().getAttribute("CURR_PROJ").CURR_PROJ
			moveBundles = MoveBundle.findAllByProject( Project.get( projectId ) )
		} else {
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

	private getRackLayout( def rackLayoutsHasPermission, def asset, def includeBundleName, def backView, def showCabling, def hideIcons, def redirectTo ){
		def rows = new StringBuffer()
		def rowspan = 1
		def cssClass = "empty"
		def rackStyle = ""
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
					if(location == 1)
						overlappedAssets = AssetEntity.findAllWhere( project:assetEntity.project, assetTag : tagValue, sourceRack: assetEntity.sourceRack )
					else 
						overlappedAssets = AssetEntity.findAllWhere( project:assetEntity.project, assetTag : tagValue, targetRack: assetEntity.targetRack )
					if(overlappedAssets.size() > 1) {
						overlappedAssets.each{ overlapAsset ->
							moveBundle += (overlapAsset?.moveBundle ? overlapAsset?.moveBundle.name : "") + "<br/>"
							if(overlapAsset.model && overlapAsset.model.assetType == 'Blade Chassis' && (!backView || showCabling != 'on')){
								hasBlades = true
								bladeTable = generateBladeLayout(it, overlapAsset, rackLayoutsHasPermission, hideIcons, redirectTo)
							}
							assetTag += """<a href="javascript:getEntityDetails('${redirectTo}','${overlapAsset?.assetType}',${overlapAsset?.id})" >"""+trimString(assetTagValue.replace('~-','-'))+"</a>" 
							if(hasBlades){
								assetTag += "<br/>"+bladeTable
							}
						}
					} else if(overlappedAssets.size() > 0){
						overlappedAsset = overlappedAssets[0]
						moveBundle += (overlappedAsset?.moveBundle ? overlappedAsset?.moveBundle.name : "") + "<br/>"
						if(overlappedAsset.model && overlappedAsset.model.assetType == 'Blade Chassis' && (!backView || showCabling != 'on') ){
							hasBlades = true
							bladeTable = generateBladeLayout(it, overlappedAsset,rackLayoutsHasPermission, hideIcons, redirectTo)
						}
						cabling = !assetTag.contains("Devices Overlap") && showCabling == 'on' ? generateCablingLayout( overlappedAsset, backView ) : ""
						assetTag += """<a href="javascript:getEntityDetails('${redirectTo}','${overlappedAsset?.assetType}',${overlappedAsset?.id})" >"""+trimString(assetTagValue.replace('~-','-'))+"</a>&nbsp;"
						if(hasBlades){
							assetTag += "<br/>"+bladeTable
						}
					}
				}
				if(backView) {
					if(cabling != "" && it.cssClass != "rack_error"){
						def assetCables = AssetCableMap.findByFromAsset(it.asset?.assetEntity)
						if( hasBlades && showCabling != 'on'){
							row.append("<td class='${it.rackStyle}'>${it.rack}</td><td colspan='2' rowspan='${rowspan}' class='${it.cssClass}'>${assetTag}</td>")
							if ( assetCables )
								row.append("<td rowspan='${rowspan}' class='${it.cssClass}'><a href='#' onclick='openCablingDiv(${it.asset?.assetEntity.id})'>view</a></td>")
							else
								row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>&nbsp;</td>")
						} else {
							row.append("<td class='${it.rackStyle}'>${it.rack}</td><td rowspan='${rowspan}' colspan='3' class='${it.cssClass}'>")
							row.append("<table style='border:0;' cellpadding='0' cellspacing='0'><tr><td style='border:0;'>${assetTag}</td>")
							
							if(includeBundleName)
								row.append("<td style='border:0;'>${moveBundle}</td>")
							else
								row.append("<td style='border:0;'>&nbsp;</td>")
							if ( assetCables )
								row.append("<td style='border:0;'><a href='#' onclick='openCablingDiv(${it.asset?.assetEntity.id})'>view</a></td></tr>")
							else
								row.append("<td style='border:0;'>&nbsp;</td></tr>")
								
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
							def assetCables = AssetCableMap.findByFromAsset(it.asset?.assetEntity)
							if ( assetCables )
								row.append("<td rowspan='${rowspan}' class='${it.cssClass}'><a href='#' onclick='openCablingDiv(${it.asset?.assetEntity.id})'>view</a></td>")
							else
								row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>&nbsp;</td>")
							
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
				if(hideIcons == "on"){
				row.append("""<div class="rack_menu"><img src="../i/rack_add2.png">
							<ul>
								<li><a href="javascript:createAssetPage('Server','${it.source}','${it.rackDetails.tag}','${it.rackDetails.room?.roomName}','${it.rackDetails.location}','${it.rack}')">Create asset  </a></li>
								<li><a href="javascript:listDialog('','','asc','${it.source}','${it.rackDetails.tag}','${it.rackDetails.room?.roomName}','${it.rackDetails.location}','${it.rack}')">Assign asset </a></li>
								<li><a href="javascript:listDialog('all','','asc','${it.source}','${it.rackDetails.tag}','${it.rackDetails.room?.roomName}','${it.rackDetails.location}','${it.rack}')">Reassign asset </a></li>
							</ul></img></div>&nbsp;</td><td>&nbsp;</td>""")
				} else { 
					row.append("&nbsp;</td><td>&nbsp;</td>")
				}
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
	def generateBladeLayout(def assetDetails, def assetEntity, def rackLayoutsHasPermission, def hideIcons, def redirectTo){
		
		def bladeTable = '<table class="bladeTable"><tr>'
		def rowspan = assetDetails.asset?.rowspan != 0 ? assetDetails.asset?.rowspan : 1
		def tdHeight = rowspan * 6
		def blades = []
		if(assetDetails.asset.source == 1)
			blades = AssetEntity.findAllWhere(project:assetEntity.project, moveBundle:assetEntity.moveBundle, assetType:'Blade', sourceBladeChassis:assetEntity.assetTag)
		else
			blades = AssetEntity.findAllWhere(project:assetEntity.project, moveBundle:assetEntity.moveBundle, assetType:'Blade', targetBladeChassis:assetEntity.assetTag)

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
						bladeTable += """<td class='blade' rowspan='${bladeSpan}' style='height:${tdHeight}px'><a href="javascript:getEntityDetails('${redirectTo}','Blade',${blade.id})" title='${tag.replace('<br/>','')}'>${taglabel}</a></td>"""
				} else {
					if(hideIcons == 'on'){
						bladeTable += """<td class='emptyBlade' style='height:${tdHeight}px'>
									<div class="rack_menu"><img src="../i/rack_add2.png"/>
										<ul>
											<li><a href="javascript:createBladeDialog('${assetDetails.source}','${assetEntity.assetTag}','${i}','${assetEntity.manufacturer.id}','Blade','${assetEntity.id}','${assetEntity.moveBundle.id}')">Create asset  </a></li>
											<li><a href="javascript:listBladeDialog('${assetDetails.source}','${assetEntity.assetTag}','${i}','assign')">Assign asset </a></li>
											<li><a href="javascript:listBladeDialog('${assetDetails.source}','${assetEntity.assetTag}','${i}','reassign')">Reassign asset </a></li>
										</ul>
									</div></td>"""
					} else {
						bladeTable += "<td class='emptyBlade' style='height:${tdHeight}px'>&nbsp;</td>"
					}
				}
			}
			bladeTable += k == chassisRows ? "</tr>" : "</tr><tr>"
		}
		
		bladeTable += '</table>'
	}
	/********************************************
	 * Trim Name if over 30 characters and add "...".
	 ********************************************/
	def private trimString( name ){
		def trimmedVal = name
		def length = name.length()
		if(length > 30){
			trimmedVal = trimmedVal.substring(0,30)+"..."
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
		def assetId = params.assetId
		def assetEntity = assetId ? AssetEntity.get(assetId) : null
		def assetCableMapList
		def title =  ""
		if( assetEntity ){
			assetCableMapList = AssetCableMap.findAllByFromAsset( assetEntity )
			title = assetEntity.assetName+" ( "+assetEntity.model.manufacturer+" / "+assetEntity.model+" )"
		}
		def assetCablingDetails = []
		assetCableMapList.each {
			def rackUposition = it.toConnectorNumber ? it.toAssetRack+"/"+it.toAssetUposition+"/"+it.toConnectorNumber.label : ""
			def toAssetId
			def toTitle =  ""
			if(it.fromConnectorNumber.type == "Power"){
				rackUposition = it.toPower ? it.toAssetRack+"/"+it.toAssetUposition+"/"+it.toPower : ""
			} else if(it.toAsset){
				toAssetId = it.toAsset.id
				toTitle = it.toAsset.assetName+" ( "+it.toAsset.model.manufacturer+" / "+it.toAsset.model+" )"
			}
			
			assetCablingDetails << [model:assetEntity.model.id, id:it.id, connector : it.fromConnectorNumber.connector, 
									type:it.fromConnectorNumber.type, connectorPosX:it.fromConnectorNumber.connectorPosX,
									labelPosition:it.fromConnectorNumber.labelPosition, color : it.color ? it.color : "",
									connectorPosY:it.fromConnectorNumber.connectorPosY, status:it.status,displayStatus:statusDetails[it.status], 
									label:it.fromConnectorNumber.label, hasImageExist:assetEntity.model.rearImage && assetEntity.model?.useImage ? true : false,
									usize:assetEntity?.model?.usize, rackUposition : rackUposition, toAssetId : toAssetId, toTitle:toTitle, title:title]
		}
		render assetCablingDetails as JSON
	}
	/*
	 * Update the AssetCablingMap with the date send from RackLayout cabling screen
	 */
	def updateCablingDetails = {
		def assetId = params.asset
		def assetCableId = params.assetCable
		
		if(assetCableId){
			def currProj = getSession().getAttribute( "CURR_PROJ" )
			def projectId = currProj.CURR_PROJ
			def project = Project.get( projectId )
			def actionType = params.actionType
			def status = "missing"
			def toAssetRack
			def toAssetUposition
			def toConnector
			def toAsset
			def toPower
			def connectorType = params.connectorType
			def assetCableMap = AssetCableMap.findById( assetCableId )
		
			if(connectorType != "Power"){
				def fromAssetCableMap = AssetCableMap.findByToAssetAndToConnectorNumber( assetCableMap.fromAsset, assetCableMap.fromConnectorNumber )
				if(fromAssetCableMap){
					AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing', toAsset=null,toConnectorNumber=null,toAssetRack=null,toAssetUposition=null, color=null
												where toAsset = ? and toConnectorNumber = ?""",[assetCableMap.fromAsset, assetCableMap.fromConnectorNumber])
			}
			}
			switch(actionType){
				case "emptyId" : status = "empty" ; break;
				case "cabledId" : status = "cabled"; break;
				case "assignId" : 
					status = "cabledDetails"; 
					if(connectorType != "Power"){
						toAssetRack = params.rack
						toAssetUposition = params.uposition
						def rack = Rack.findWhere(tag:toAssetRack,source:0,project:project)
						def assetEntity = rack?.targetAssets?.find{it.targetRackPosition == Integer.parseInt(params.uposition)}
						def modelConnectors
						if(assetEntity?.model){
							modelConnectors = ModelConnector.findAllByModel(assetEntity?.model)
							toAsset = assetEntity 
							toConnector = modelConnectors?.find{it.label.equalsIgnoreCase(params.connector) }
							AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
															toConnectorNumber=null,toAssetRack=null,toAssetUposition=null, color=null
															where toAsset = ? and toConnectorNumber = ? """,[toAsset, toConnector])
						}
					} else {
						toAsset = assetCableMap.fromAsset
						toAssetRack = assetCableMap?.fromAsset?.rackTarget?.tag
						toAssetUposition = 0
						toConnector = null
						toPower = params.staticConnector
					}
					break;
			}
			sessionFactory.getCurrentSession().flush();
	    	sessionFactory.getCurrentSession().clear();
			assetCableMap.status = status
			assetCableMap.toAsset = toAsset
			assetCableMap.toConnectorNumber = toConnector
			assetCableMap.toAssetRack = toAssetRack
			assetCableMap.toAssetUposition = connectorType != "Power" ? toAsset?.targetRackPosition : toAssetUposition
			assetCableMap.toPower = toPower
			assetCableMap.color = params.color
			if(assetCableMap.save(flush:true) && toAsset && connectorType != "Power"){
				def toAssetCableMap = AssetCableMap.findByFromAssetAndFromConnectorNumber( toAsset, toConnector )
				toAssetCableMap.status = status
				toAssetCableMap.toAsset = assetCableMap.fromAsset
				toAssetCableMap.toConnectorNumber = assetCableMap.fromConnectorNumber
				toAssetCableMap.toAssetRack = assetCableMap.fromAsset?.rackTarget?.tag
				toAssetCableMap.toAssetUposition = assetCableMap.fromAsset?.targetRackPosition
				toAssetCableMap.color = params.color
				if(!toAssetCableMap.save(flush:true)){
					def etext = "Unable to create toAssetCableMap" +
	                GormUtil.allErrorsString( toAssetCableMap )
					println etext
				}
			} else {
				def etext = "Unable to create FromAssetCableMap" +
                GormUtil.allErrorsString( assetCableMap )
				println etext
			}
		}
		def assetCable = [assetId : assetId, assetTag : AssetEntity.get(assetId)?.assetTag ]
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
				def assetCableMapList = AssetCableMap.findAllByFromAsset( assetEntity )
				assetCableMapList.each {assetCable->
					cableDiagram.append("""<div style="top:${assetCable.fromConnectorNumber.connectorPosY / 2}px ;left:${assetCable.fromConnectorNumber.connectorPosX}px ">
												<div>
													<img src="../i/cabling/${assetCable.status}.png"/>
												</div>
												<div class="connector_${assetCable.fromConnectorNumber.labelPosition}">
													<span>${assetCable.fromConnectorNumber.label}</span>
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
}
