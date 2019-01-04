databaseChangeLog = {

	changeSet(author: "lokanada", id: "20140312 TM-2526") {
		comment("update the person 'active' field to userlogin 'active'")
		
		sql("UPDATE tdstm.person SET active = 'N' where person_id in (select person_id from tdstm.user_login where active = 'N')")
		sql("UPDATE tdstm.person SET active = 'Y' where person_id in (select person_id from tdstm.user_login where active = 'Y')")
		
		sql("UPDATE tdstm.user_login SET active = 'N' where person_id in (select person_id from tdstm.person where active = 'N')")
		sql("UPDATE tdstm.user_login SET active = 'Y' where person_id in (select person_id from tdstm.person where active = 'Y')")
	}

}
