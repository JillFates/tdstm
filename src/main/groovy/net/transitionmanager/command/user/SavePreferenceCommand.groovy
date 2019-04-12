package net.transitionmanager.command.user

import com.tdsops.tm.enums.domain.UserPreferenceEnum
import net.transitionmanager.command.CommandObject
/**
 * A command object used saving user preferences.
 */
class SavePreferenceCommand implements CommandObject{
	UserPreferenceEnum code
	String             value

	static constraints = {
		value blank: true
	}
}
