package net.transitionmanager.routes

import org.apache.camel.builder.RouteBuilder

class AlertRoute extends RouteBuilder {


	@Override
	void configure() {
		from('seda:alert-queue')
			.log("Received an alert")
			.to('bean:awsService?method=receiveAlert')
	}
}
