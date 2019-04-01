package net.transitionmanager.application

import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.HtmlUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.ProjectService

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class CommonController implements ControllerMethods {

	AssetEntityService assetEntityService
	ControllerService controllerService
	ProjectService projectService

	// TODO: This should be deleted
	def index() { }

	@HasPermission(Permission.UserGeneralAccess)
	def tmLinkableUrl() {
		String errMsg
		try {
			def jsonData = request.JSON
			def linkableUrl = jsonData.linkableUrl
			def isLinkableUrl = HtmlUtil.isMarkupURL(linkableUrl)
			if(!isLinkableUrl){
				errMsg = "The format of the linkable URL is invalid."
			}
		}catch(e){
			e.printStackTrace()
			errMsg = "There's been an error validating the Linkable Url."
		}
		if (errMsg) {
			renderErrorJson(errMsg)
		}else{
			renderSuccessJson([])
		}
	}
}
