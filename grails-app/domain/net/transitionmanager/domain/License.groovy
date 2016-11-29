package net.transitionmanager.domain

import com.github.icedrake.jsmaz.Smaz
import groovy.json.JsonBuilder
import org.apache.commons.codec.binary.Base64

/**
 * Created by octavio on 9/20/16.
 */
class License {
	String id = UUID.randomUUID().toString()
	String instalationNum
	String email
	Environment environment
	Status status
	Type   type
	String project
	String method = ""
	int    max = 0
	Date   requestDate
	Date   validStart
	Date   validEnd
	String requestNote
	String hash

	static mapping = {
		id 			generator: 'assigned'
		requestNote type:'text'
		hash 		type:'text'
		version 	false
		tablePerHierarchy false
	}

	static constraints = {
		method 		nullable:true
		validStart 	nullable:true
		validEnd 	nullable:true
		requestNote nullable:true
		hash 		nullable:true
	}

	public boolean isActive(){
		return (hash)? true : false
	}

	public toJsonMap() {
		[
			id            : id,
			email         : email,
			environment   : environment,
			instalationNum: instalationNum,
			project       : project,
			requestDate   : requestDate,
			requestNote   : requestNote
		]
	}

	public toJsonString(){
		new JsonBuilder( toJsonMap() ).toString()
	}

	public toEncodedMessage(){
		new String(Base64.encodeBase64(Smaz.compress(toJsonString())))
	}


	/** Enumerator Helpers *******************************************/
	enum Environment {
		Engineering(1),
		Training(2),
		Demo(3),
		Production(4)

		int id
		Environment(int id) {
			this.id = id
		}

		static Environment forId(int id) {
			values().find { it.id == id }
		}
	}

	enum Type {
		SINGLE_PROJECT (1),
		MULTI_PROJECT (2)

		int id
		Type(int id) {
			this.id = id
		}

		static Type forId(int id) {
			values().find { it.id == id }
		}
	}

	enum Method {
		// Accounting based on highwater limit of the number of servers
		MAX_SERVERS (1),

		// Accounting based on average number of servers under management during the month where tokens
				// are deducted until the tokensUsed >= tokensAllocated
		TOKEN (2),

		// Used for custom licenses where there is no limits of usage
		CUSTOM (3)

		int id
		Method(int id) {
			this.id = id
		}

		static Method forId(int id) {
			values().find { it.id == id }
		}
	}

	enum Status {
		ACTIVE(1),
		EXPIRED(2),
		TERMINATED(3),
		PENDING(4)

		int id
		Status(int id) {
			this.id = id
		}

		static Status forId(int id) {
			values().find { it.id == id }
		}
	}
}
