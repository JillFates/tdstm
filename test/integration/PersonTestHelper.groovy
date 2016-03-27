
class PersonTestHelper {
	def personService
	Long adminPersonId = 100 

	PersonTestHelper(personService) {
		assert (personService instanceof PersonService)
		this.personService = personService
	}

	/**
	 * Creates test person assigning them to the company and team(s) specified
	 * @param byWhom - the person that is attempting to create the person
	 * @param company - the company that the person should belong to
	 * @param project - the project to assign the person to (optional default null)
	 * @param personProps - a map of person property settings to override the defaults (optional)
	 * @param teams - a list of teams to add the person to (default ['PROJ_MGR'])
	 * @return the newly created person
	 */
	Person createPerson(Person byWhom, PartyGroup company, Project project=null, Map personProps=null, List teams=['PROJ_MGR']) {
		Map personMap = [firstName:"Test ${new Date()}", lastName: 'User', active:'Y', function: teams ]

		// Apply any changes passed into the method
		if (personProps) {
			personMap << personProps
		}

		Person person = personService.savePerson(personMap, byWhom, company.id, project, true)
		assert person
		return person
	}

	/**
	 * Used to get the Admin person to use for tests
	 * @return a Person that has Administration privileges
	 */
	Person getAdminPerson() {
		Person admin = Person.get(adminPersonId)
		assert admin
		return admin
	}

}