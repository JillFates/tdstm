package net.transitionmanager.command.assetentity

import net.transitionmanager.command.CommandObject
import com.tdsops.tm.enums.domain.UserPreferenceEnum

/**
 *
 * A command object used in setting import preferences.
 * (ImportApplication,ImportServer,ImportDatabase, ImportStorage,ImportRoom,ImportRack,ImportDependency)
 *
 * @param preference  The preference name.
 * @param value  The preference value.
 */
class SetImportPreferencesCommand implements CommandObject {
    String preference
    String value

    static constraints = {
        preference inList: UserPreferenceEnum.importPreferenceKeys*.toString()
    }
}
