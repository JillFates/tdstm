package net.transitionmanager.integration

/**
 * TODO: Remove when tickets TM-8694 and TM-8695 were finished
 */
class ReactionTaskFacade {

	Boolean done = false

	String note = ''

	Boolean done(){
		this.done = true
	}

	Boolean isDone () {
		return done
	}

	String getNote () {
		return note
	}

	void addNote (String note) {
		this.note = note
	}
}
