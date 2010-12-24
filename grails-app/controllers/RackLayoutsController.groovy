import grails.converters.JSON
import org.jsecurity.SecurityUtils
import com.tdssrc.grails.GormUtil

class RackLayoutsController {
	def userPreferenceService
	def jdbcTemplate
	def supervisorConsoleService
	
	def create = {
		def currProj = getSession().getAttribute( "CURR_PROJ" )
		def projectId = currProj.CURR_PROJ
		def projectInstance = Project.findById( projectId )
		def moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
		userPreferenceService.loadPreferences("CURR_BUNDLE")
		def currentBundle = getSession().getAttribute("CURR_BUNDLE")?.CURR_BUNDLE
		/* set first bundle as default if user pref not exist */
		currentBundle = currentBundle ? currentBundle : moveBundleInstanceList[0]?.id
		[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance, currentBundle:currentBundle]
	}
	
	def save = {
		def bundleId = params.moveBundle
		def maxUSize = 42
		if(bundleId == "null") {
			return [errorMessage: "Please Select a Bundle."]
		} else {
			def includeOtherBundle = params.otherBundle
			def includeBundleName = params.bundleName
			def printQuantity = params.printQuantity
			def frontView = params.frontView
			def backView = params.backView
			def sourceRacks = []
			def targetRacks = []
			def projectId = getSession().getAttribute("CURR_PROJ").CURR_PROJ
			def rackLayout = []
			def project = Project.findById(projectId)
			def moveBundle = MoveBundle.findById(bundleId)
			def isAdmin = SecurityUtils.getSubject().hasRole("PROJ_MGR")
			if( !isAdmin ) {
				isAdmin = SecurityUtils.getSubject().hasRole("PROJECT_ADMIN")
			}
			if(request.getParameterValues("sourcerack") != ['none']) {
				def rack = request.getParameterValues("sourcerack")
				if(rack[0] == "") {
					sourceRacks = moveBundle.sourceRacks
				} else {
					rack.each {
						sourceRacks << Rack.get(new Long(it))
					}
				}
				sourceRacks = sourceRacks.sort { it.tag }
			}

			if(request.getParameterValues("targetrack") != ['none']) {
				def rack = request.getParameterValues("targetrack")
				if(rack[0] == "") {
					targetRacks = moveBundle.targetRacks
				} else {
					rack.each {
						targetRacks << Rack.get(new Long(it))
					}
				}
				targetRacks = targetRacks.sort { it.tag }
			}
			
			def racks = sourceRacks + targetRacks
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ	
			racks.each { rack ->
				def assetDetails = []
				def assetDetail = []
				def finalAssetList = []
				def racksByFilter
				if(includeOtherBundle){
					racksByFilter = rack.assets.findAll { it.project == project }.sort { rack?.source == 1 ? it.sourceRackPosition ? it.sourceRackPosition * -1 : 0 : it.targetRackPosition ? it.targetRackPosition * -1 : 0}
				} else {
					racksByFilter = rack.assets.findAll { it.moveBundle == moveBundle && it.project == project }.sort { rack?.source == 1 ? it.sourceRackPosition ? it.sourceRackPosition * -1 : 0 : it.targetRackPosition ? it.targetRackPosition * -1 : 0}
				}
				racksByFilter.each { assetEntity ->
					def overlapError = false
					def rackPosition = rack.source == 1 ? assetEntity.sourceRackPosition : assetEntity.targetRackPosition
					if(rackPosition == 0 || rackPosition == null)
						rackPosition = 1
					
					def rackSize = assetEntity.usize == 0 || assetEntity.usize == null ? 1 : assetEntity.usize.toInteger()
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
								assetEntity.usize = 1
								overlapError = true
							}
							assetDetail << [assetEntity:assetEntity, assetTag:assetEntity.assetTag + ' ~- ' + assetEntity.assetName, position:position, overlapError:overlapError, 
											rowspan:rackSize, currentHigh : position, currentLow : newLow, source:rack.source ]
						}
					} else {
						if(position > maxUSize) {
							position = maxUSize
							newLow = maxUSize
							assetEntity.usize = 1
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
						if(assetEnity.overlapError) {
							cssClass = 'rack_error'
							rackStyle = 'rack_error'
						} else if(bundleId && assetEnity.assetEntity?.moveBundle != moveBundle) {
							def currentTime = GormUtil.convertInToGMT( "now", tzId ).getTime()
							def startTime = moveBundle.startTime ? moveBundle.startTime.getTime() : 0
							if(startTime < currentTime){
								cssClass = 'rack_past'
							} else {
								cssClass = "rack_future"
							}
						} else {
							cssClass = 'rack_current'
							rackStyle = 'rack_current'
						}
						if(assetEnity.position == 0 || assetEnity.assetEntity?.usize == 0 || assetEnity.assetEntity?.usize == null ) {
							rackStyle = 'rack_error'
						}
						assetDetails << [asset:assetEnity, rack:i, cssClass:cssClass, rackStyle:rackStyle, source:rack.source ]
					} else {
						assetDetails << [asset:null, rack:i, cssClass:cssClass, rackStyle:rackStyle, source:rack.source ]
					}
				}
				def backViewRows
				def frontViewRows
				if(backView) {
					backViewRows = getRackLayout(isAdmin, assetDetails, includeBundleName, backView)
				}
				if(frontView) {
					frontViewRows = getRackLayout(isAdmin, assetDetails, includeBundleName, null)
				}
				rackLayout << [ assetDetails : assetDetails, rack : rack.tag , room : rack.room,
								frontViewRows : frontViewRows, backViewRows : backViewRows ]
			}
			return [rackLayout : rackLayout, frontView : frontView, backView : backView]
		}
	}
	
	def getRackDetails = {
		def bundle = MoveBundle.get(params.bundleId)
		/* set user pref CURR_BUNDLE */
		userPreferenceService.setPreference( "CURR_BUNDLE", params.bundleId )
		def rackDetails = [[sourceRackList:bundle.sourceRacks, targetRackList:bundle.targetRacks]]
		render rackDetails as JSON
	}

	private getRackLayout( def isAdmin, def asset, def includeBundleName, def backView){
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
							if(overlapAsset.assetType == 'Blade Chassis'){
								hasBlades = true
								bladeTable = generateBladeLayout(it, overlapAsset, isAdmin)
							}
							if(isAdmin)
								assetTag += "<a href='javascript:openAssetEditDialig(${overlapAsset?.id})' >"+trimString(assetTagValue.replace('~-','-'))+"</a> <br/>"+bladeTable+"</br>"
							else
								assetTag += trimString(assetTagValue.replace('~-','-'))+"<br/>"+bladeTable+"</br>"
						}
					} else if(overlappedAssets.size() > 0){
						overlappedAsset = overlappedAssets[0]
						moveBundle += (overlappedAsset?.moveBundle ? overlappedAsset?.moveBundle.name : "") + "<br/>"
						if(overlappedAsset.assetType == 'Blade Chassis'){
							hasBlades = true
							bladeTable = generateBladeLayout(it, overlappedAsset,isAdmin)
						}
						if(isAdmin)
							assetTag += "<a href='javascript:openAssetEditDialig(${overlappedAsset?.id})' >"+trimString(assetTagValue.replace('~-','-'))+"</a> <br/>"+bladeTable+"</br>"
						else
							assetTag += trimString(assetTagValue.replace('~-','-'))+"<br/>"+bladeTable+"</br>"
					}
				}
					
				if( hasBlades ){
					row.append("<td class='${it.rackStyle}'>${it.rack}</td><td colspan='2' rowspan='${rowspan}' class='${it.cssClass}'>${assetTag}</td>")
				} else {
					row.append("<td class='${it.rackStyle}'>${it.rack}</td><td rowspan='${rowspan}' class='${it.cssClass}'>${assetTag}</td>")
					if(includeBundleName)
						row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>${moveBundle}</td>")
					else
						row.append("<td rowspan='${rowspan}' class='${it.cssClass}'></td>")
				}
				
				if(backView) {
					if(it.cssClass != "rack_error") {
						def cablingString = "${it.asset?.assetEntity?.pduPort ? 'PDU: '+ it.asset?.assetEntity?.pduPort +' | ' : '' }"+ 
											"${it.asset?.assetEntity?.nicPort ? 'NIC: '+ it.asset?.assetEntity?.nicPort +' | ' : ''}"+
											"${it.asset?.assetEntity?.kvmDevice && it.asset?.assetEntity?.kvmDevice != 'blank / blank'? 'KVM: '+ it.asset?.assetEntity?.kvmDevice +' | ' : ''}"+
											"${it.asset?.assetEntity?.remoteMgmtPort ? 'RMgmt: '+ it.asset?.assetEntity?.remoteMgmtPort +' | ': ''}"+
											"${it.asset?.assetEntity?.fiberCabinet && it.asset?.assetEntity?.fiberCabinet != 'blank / blank / blank' ? 'Fiber: '+ it.asset?.assetEntity?.fiberCabinet +' | ' : ''}"
						
						if ( cablingString )
							cablingString = cablingString.substring( 0, cablingString.length() - 2 )
						
						if ( cablingString.length() > 90 )
							row.append("<td rowspan='${rowspan}' style='font-size:6px;' class='${it.cssClass}'><a href='#' onclick='openCablingDiv(${it.asset?.assetEntity.id}, this.innerHTML)'>${cablingString}</a></td>")
						else
							row.append("<td rowspan='${rowspan}' class='${it.cssClass}'><a href='#' onclick='openCablingDiv(${it.asset?.assetEntity.id}, this.innerHTML)'>${cablingString}</a></td>")
						
					} else {
						row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>Devices Overlap</td>")
					}
				}
			} else if(rowspan <= 1) {
				rowspan = 1
				rackStyle = it.rackStyle
				row.append("<td class='empty'>${it.rack}</td><td rowspan=1 class=${it.cssClass}>&nbsp;</td><td>&nbsp;</td>")
				if(backView)
					row.append("<td>&nbsp;</td>")
				
			} else {
				row.append("<td class='${rackStyle}'>${it.rack}</td>")
				rowspan--
			}
			// Remove right U-position number 
			//row.append("<td class='${rackStyle}'>${it.rack}</td>")
			row.append("</tr>")
			rows.append(row.toString())
		}
		return rows
	}
	/*************************************************
	 * Construct Balde layout for RackLayouts report
	 **************************************************/
	def generateBladeLayout(def assetDetails, def assetEntity, def isAdmin){
		
		def bladeTable = '<table class="bladeTable"><tr>'
		def rowspan = assetDetails.asset?.rowspan != 0 ? assetDetails.asset?.rowspan : 1
		def tdHeight = rowspan * 7
		def blades = []
		if(assetDetails.asset.source == 1)
			blades = AssetEntity.findAllWhere(project:assetEntity.project, moveBundle:assetEntity.moveBundle, assetType:'Blade', sourceBladeChassis:assetEntity.assetTag)
		else
			blades = AssetEntity.findAllWhere(project:assetEntity.project, moveBundle:assetEntity.moveBundle, assetType:'Blade', targetBladeChassis:assetEntity.assetTag)

		def fullRows = []
		for(int i = 1; i < 9; i++) {
			def matching = []
			if(assetDetails.asset.source == 1)
				matching = blades.findAll { it.sourceBladePosition == i }
			else
				matching = blades.findAll { it.targetBladePosition == i }
			
			if(matching.size() > 1)
				bladeTable += "<td class='errorBlade' style='height:${tdHeight}px'>Conflict</td>"
			else if(matching.size() == 1) {
				def blade = matching[0]
				def tag = blade.assetTag.split('')[1..-1].join('<br/>')
				def taglabel = "<div style='float:left'>"+tag.substring(0,tag.length())+"</div>"
				if(blade.assetTag.length() >= 8){
					taglabel = "<div style='float:left'>"+tag.substring(0,19)+"</div><div style='float:right'>"+tag.substring(24,43)+"</div>"
				}
				def bladeSpan = blade.bladeSize == 'Full' ? 2 : 1
				if(bladeSpan == 2){
					fullRows << i + 8
					taglabel = tag
				}
				if((bladeSpan == 2) && blades.findAll { it.sourceBladePosition == i + 8 }.size() > 0)
					bladeTable += "<td class='errorBlade' style='height:${tdHeight}px'>&nbsp;</td>"
				else if(isAdmin)
					bladeTable += "<td class='blade' rowspan='${bladeSpan}' style='height:${tdHeight}px'><a href='javascript:openAssetEditDialig(${blade.id})' title='${tag.replace('<br/>','')}'>${taglabel}</a></td>"
				else
					bladeTable += "<td class='blade' rowspan='${bladeSpan}' style='height:${tdHeight}px' title='${tag.replace('<br/>','')}'>${taglabel}</td>"
			} else
				bladeTable += "<td class='emptyBlade' style='height:${tdHeight}px'>&nbsp;</td>"
		}
		
		bladeTable += "</tr><tr>"
		
		for(int i = 9; i < 17; i++) {
			def matching = []
			if(assetDetails.asset.source == 1)
				matching = blades.findAll { it.sourceBladePosition == i }
			else
				matching = blades.findAll { it.targetBladePosition == i }
			
			if(fullRows.contains(i))
				bladeTable += ''
			else if(matching.size() > 1)
				bladeTable += "<td class='errorBlade' style='height:${tdHeight}px'>&nbsp;</td>"
			else if(matching.size() == 1) {
				def blade = matching[0]
				def tag = blade.assetTag.split('')[1..-1].join('<br/>')
				def taglabel = "<div style='float:left'>"+tag.substring(0,tag.length())+"</div>"
				if(blade.assetTag.length() >= 8){
					taglabel = "<div style='float:left;'>"+tag.substring(0,19)+"</div><div style='float:right'>"+tag.substring(24,43)+"</div>"
				}
				if(isAdmin)
					bladeTable += "<td class='blade' style='height:${tdHeight}px'><a href='javascript:openAssetEditDialig(${blade.id})' title='${tag.replace('<br/>','')}' >${taglabel}</a></td>"
				else                                 
					bladeTable += "<td class='blade' style='height:${tdHeight}px' title='${tag.replace('<br/>','')}'>${taglabel}</td>"
			} else
				bladeTable += "<td class='emptyBlade' style='height:${tdHeight}px'>&nbsp;</td>"
		}
		
		bladeTable += "</tr>"
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
		def assetEntity = assetId ? AssetEntity.get(assetId) : ""
		def assetCableMapList
		if( assetEntity ){
			assetCableMapList = AssetCableMap.findAllByFromAsset( assetEntity )
		}
		def assetCablingDetails = []
		assetCableMapList.each {
			assetCablingDetails << [model:assetEntity.model.id, id:it.id, connectorPosX:it.fromConnectorNumber.connectorPosX,
								   connectorPosY:it.fromConnectorNumber.connectorPosY, status:it.status, label:it.fromConnectorNumber.label,
								   hasImageExist:assetEntity.model.rearImage ? true : false, usize:assetEntity.model.usize ]
		}
		render assetCablingDetails as JSON
	}
	/*
	 * Update the AssetCablingMap with the date send from RackLayout cabling screen
	 */
	def updateCablingDetails = {
		def assetCableId = params.assetCableId
		if(assetCableId){
			def assetCableMap = AssetCableMap.findById( assetCableId )
			assetCableMap.status = params.status
			assetCableMap.save(flush:true)
		}
		render "success"
	}
}
