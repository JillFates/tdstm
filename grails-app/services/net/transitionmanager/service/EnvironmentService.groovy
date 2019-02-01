package net.transitionmanager.service

import grails.util.Metadata
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.io.Resource

import java.security.MessageDigest

class EnvironmentService implements InitializingBean, ServiceMethods {

	static transactional = false

	String version
	String build
	String buildHash

	void afterPropertiesSet() {
		Resource resource = grailsApplication.mainContext.getResource(grailsApplication.config.tdsops.buildFile)
		if (resource) {
			build = (resource?.inputStream.text.trim()) ?: ""
		}
		version = Metadata.current['info.app.version']

		String versionBuild = "${version}_${build}"
		buildHash = MessageDigest.getInstance( "MD5" ).digest( versionBuild.bytes ).encodeHex().toString()
	}

	// gets the version and build data as a string
	String getVersionText() {
		String versionText = "Version $version"
		if(build) {
			versionText = "$versionText ($build)"
		}
		return versionText
	}
}
