/**
 * Initializes user's preferences to the new timezones and dateformat
 */
databaseChangeLog = {

	changeSet(author: "dscarpa", id: "20150804 TM-4069-1") {
		comment("Initializes user's preferences to the new timezones and dateformat")

        // Initializes user's date format preference
		sql("""
            INSERT INTO user_preference(user_login_id, preference_code, value) 
            (
                SELECT ul1.user_login_id , 'CURR_DT_FORMAT', 'MM/DD/YYYY'
                FROM user_login ul1
                WHERE ul1.user_login_id not in
                (SELECT ul.user_login_id FROM user_login ul
                INNER JOIN user_preference up ON up.user_login_id = ul.user_login_id AND up.preference_code = 'CURR_DT_FORMAT') 
            )
		""")

        // Update timezones with the new timezone format
        def timezones = [
            BRST: "America/Sao_Paulo",
            BRT: "America/Sao_Paulo",
            ADT: "Atlantic/Bermuda",
            AST: "Atlantic/Bermuda",
            EDT: "America/New_York",
            EST: "America/New_York",
            CDT: "America/Chicago",
            CST: "America/Chicago",
            MDT: "America/Phoenix",
            MST: "America/Phoenix",
            PDT: "America/Los_Angeles",
            PST: "America/Los_Angeles",
            HADT: "Pacific/Honolulu",
            HAST: "Pacific/Honolulu",
            WET: "Europe/Lisbon",
            BST: "Europe/London",
            CET: "Europe/Berlin",
            WEST: "Europe/Lisbon",
            CEST: "Europe/Berlin",
            EET: "Europe/Bucharest",
            EEST: "Europe/Bucharest",
            EAT: "Africa/Nairobi",
            GST: "Asia/Muscat",
            IST: "Asia/Kolkata",
            IOT: "Asia/Almaty"
        ]

        timezones.each{ oldTimeZone, newTimeZone ->
            sql("""
                UPDATE user_preference SET value='$newTimeZone' WHERE value='$oldTimeZone' AND preference_code = 'CURR_TZ'
            """)
        }

    }
    
}
