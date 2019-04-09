package net.transitionmanager.application

import net.transitionmanager.controller.ControllerMethods

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class WidgetController implements ControllerMethods {

	def progressBar() {
		render(view: 'progressBarDemo', model: [url: '/demo'])
	}

	def progressBarFailed() {
		render(view: 'progressBarDemo', model: [url: '/demo/failed'])
	}
}
