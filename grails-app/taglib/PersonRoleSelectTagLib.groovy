import net.transitionmanager.security.RoleType
import net.transitionmanager.service.PartyRelationshipService

class PersonRoleSelectTagLib {

	static namespace = 'tds'

	PartyRelationshipService partyRelationshipService

	/**
	 * A helper tag for Selecting Roles
	 */
	def personRoleSelect = { attrs ->
		String value = attrs.remove('value')
		String isNew = attrs.remove('isNew')
		if (!attrs.id) {
			attrs.id = attrs.name
		}

		out << '<select '
		outputAttributes(attrs)
		out << '>'
		out.println()

		if (isNew == 'true') {
			out << '<option '
			writeValueAndCheckIfSelected('', null)
			out << '>Please Select</option>'
		}

		for (RoleType roleType in RoleType.list()) {
			out << '<option '
			writeValueAndCheckIfSelected(roleType.id, value)
			out << '>' << roleType.toString().encodeAsHTML() << '</option>'
			out.println()
		}

		out << '</select>'
	}

	private void writeValueAndCheckIfSelected(String roleTypeId, String value) {
		outputAttribute 'value', value
		if (roleTypeId == value) {
			out << 'selected="selected" '
		}
	}

	/**
	 * Dump out attributes in HTML compliant fashion.
	 */
	private void outputAttributes(Map<String, Object> attrs) {
		attrs.each { String name, value -> outputAttribute name, value }
	}

	private void outputAttribute(String name, value) {
		out << name << '="' << value?.toString()?.encodeAsHTML() << '" '
	}
}
