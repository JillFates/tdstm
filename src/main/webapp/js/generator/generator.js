"use strict";

var initialTickGenerator = regeneratorRuntime.mark(function initialTickGenerator(config, initialTickConfig, GraphUtil, calmTick) {
	var isLarge, tickConfig, i;
	return regeneratorRuntime.wrap(function initialTickGenerator$(_context) {
		while (1) {
			switch (_context.prev = _context.next) {
				case 0:
					// calculate the alpha that will mark the stopping point for the initial ticks
					isLarge = GraphUtil.force.nodes().size() > initialTickConfig.small.max;
					tickConfig = initialTickConfig.small;

					if (isLarge) tickConfig = initialTickConfig.large;

					GraphUtil.force.theta(tickConfig.theta);

					// yield unless we have reached the alpha limit
					GraphUtil.startForce();
					GraphUtil.force.stop();

					i = 0;

				case 7:
					if (!(i < tickConfig.ticks)) {
						_context.next = 15;
						break;
					}

					calmTick(1);

					if (!(isLarge && i % 10 == 0)) {
						_context.next = 12;
						break;
					}

					_context.next = 12;
					return;

				case 12:
					++i;
					_context.next = 7;
					break;

				case 15:

					GraphUtil.force.theta(config.theta);
					if (isLarge) calmTick(40);

					return _context.abrupt("return");

				case 18:
				case "end":
					return _context.stop();
			}
		}
	}, initialTickGenerator, this);
});
//# sourceMappingURL=generator.js.map
