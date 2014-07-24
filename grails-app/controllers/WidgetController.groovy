
class WidgetController {

	def progressBar = {
		render(view: 'progressBarDemo', model: ['url' : '/demo'])
	}

	def progressBarFailed = {
		render(view: 'progressBarDemo', model: ['url' : '/demo/failed'])
	}
}
