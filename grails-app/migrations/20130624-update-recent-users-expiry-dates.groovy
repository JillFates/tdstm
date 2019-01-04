/**
 * This changelog will set expiry date to now + 365 days for users with expired accounts that have logged in during the past 30 days
 */
databaseChangeLog = {
	// This Changeset is used to set the expiry date to next year for recently logged in users with expired accounts
	changeSet(author: "Ross", id: "20130624 TM-1930-3") {
		comment('Set expiry date to now + 365 days for users with expired accounts that have logged in during the past 30 days')
		sql("UPDATE tdstm.user_login SET expiry_date = DATE_ADD(UTC_TIMESTAMP, INTERVAL 365 DAY) WHERE expiry_date < UTC_TIMESTAMP AND last_login > DATE_SUB(UTC_TIMESTAMP, INTERVAL 30 DAY)")
	}
}
