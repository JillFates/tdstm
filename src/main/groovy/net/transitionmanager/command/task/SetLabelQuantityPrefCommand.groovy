package net.transitionmanager.command.task

import com.tdsops.tm.enums.domain.UserPreferenceEnum
import net.transitionmanager.command.CommandObject

/**
 *
 * Used in MyTask to set user preference for printername and quantity, we can get a key-value or
 * a json with a list of permissions to Change
 *
 * @param preference  The preference name.
 * @param value  The preference value.
 */
class SetLabelQuantityPrefCommand implements CommandObject {
    Map preferenceMap
    String preference
    String value

    static constraints = {
        preferenceMap nullable: true
        value nullable: true
        preference (nullable: true, inList: UserPreferenceEnum.importPreferenceKeys*.toString())
    }
}
