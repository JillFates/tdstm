var initialTickGenerator = function* (config, initialTickConfig, GraphUtil, calmTick) {
	// calculate the alpha that will mark the stopping point for the initial ticks
	var isLarge = GraphUtil.force.nodes().size() > initialTickConfig.small.max
	var tickConfig = initialTickConfig.small
	if (isLarge)
		tickConfig = initialTickConfig.large
	
	GraphUtil.force.theta(tickConfig.theta)
	
	// yield unless we have reached the alpha limit
	GraphUtil.startForce()
	GraphUtil.force.stop()
	
	for (var i = 0; i < tickConfig.ticks; ++i) {
		calmTick(1)
		if (isLarge && i%10 == 0)
			yield
	}
	
	GraphUtil.force.theta(config.theta)
	if (isLarge)
		calmTick(40)
	
	return
}