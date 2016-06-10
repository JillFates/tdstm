import net.transitionmanager.utils.Profiler

class UserController {

	/** 
	 * Used to toggle the profiler session variable on/off for performance troubleshooting
	 */
	def profilerToggle(){
		def value = session[Profiler.KEY_NAME]
		if (value) {
			session.removeAttribute(Profiler.KEY_NAME)
		} else {
			session[Profiler.KEY_NAME] = Profiler.KEY_NAME
		}

		render "The Profiler is: " + isProfilerSet()
	}

	/** 
	 * Used to show the state of the profiler session variable
	 */
	def profilerStatus(){
		render "The Profiler is: " + isProfilerSet()
	}

	private isProfilerSet(){
		return (session[Profiler.KEY_NAME] == Profiler.KEY_NAME) ? "ENABLED" : "DISABLED"
	}

}
