
databaseChangeLog = {
    changeSet(author: 'ecantu', id: '20170830 TM-7117') {
        comment('Creates a PartyRelationship record so TM_DEFAULT_PROJECT has a company assigned as the owner')

        sql("""
            INSERT INTO party_relationship 
            VALUES ('PROJ_COMPANY', 2, 18, 'PROJECT', 'COMPANY', NULL, 'ENABLED') 
            ON DUPLICATE KEY UPDATE comment = NULL;
		""")
    }
}
