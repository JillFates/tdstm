package version.v4_6_0

import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.GormUtil

databaseChangeLog = {
	changeSet(author: "jmartin", id: "20181115 TM-12988-1") {
		comment("Delete any preference code that is not defined in UserPreferenceEnum")

		grailsChange {
            change {
				List<String> prefCodes = UserPreferenceEnum.values()*.name()
				String codes = GormUtil.asQuoteCommaDelimitedString(prefCodes)
				String statement = "DELETE FROM user_preference WHERE preference_code not in ($codes)"
				sql.execute( statement )
			}
		}
	}
}
