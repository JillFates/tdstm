package net.transitionmanager.http

import spock.lang.See
import spock.lang.Specification

import javax.net.ssl.SSLException

@See('TM-14019')
class HostnameVerifierSpec extends Specification {

	final String[] CNs = ['*.transitionmanager.net']
	final String[] subjectAlts = ['*.tdstm.transtionaldata.com']

	final String[] hosts = ['test.transitionmanager.net','test.qa01.transitionmanager.net']
	final String[] hostsWithAlts = ['test.transitionmanager.net','test.tdstm.transitionaldata.com']
	final String[] localhost = ['localhost','localhost.localdomain','::1','127.0.0.1']
	final String[] unacceptedHosts = ['test.transitionmanager.com','test.tdstm.transitionmanager.com']
	final String[] unacceptedDeepSubDomainHosts = ['test.qa01.transitionmanager.net','test.qa01.tdstm.transtionaldata.com']

	void '1. Test DEFAULT hostname verifier'() {
		setup:
			HostnameVerifier hostnameVerifier = HostnameVerifier.DEFAULT
		when:
			hostnameVerifier.check(hosts, CNs, null)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(hostsWithAlts, CNs, subjectAlts)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(hosts, CNs, null)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(hostsWithAlts, CNs, subjectAlts)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(unacceptedHosts, CNs, subjectAlts)
		then:
			thrown(SSLException)
		when:
			hostnameVerifier.check(localhost, CNs, subjectAlts)
		then:
			thrown(SSLException)
	}

	void '2. Test DEFAULT_AND_LOCALHOST hostname verifier'() {
		setup:
			HostnameVerifier hostnameVerifier = HostnameVerifier.DEFAULT_AND_LOCALHOST
		when:
			hostnameVerifier.check(localhost, CNs, null)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(localhost, CNs, subjectAlts)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(unacceptedHosts, CNs, subjectAlts)
		then:
			thrown(SSLException)
	}

	void '3. Test STRICT hostname verifier'() {
		setup:
			HostnameVerifier hostnameVerifier = HostnameVerifier.STRICT
		when:
			hostnameVerifier.check(hosts, CNs, null)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(hostsWithAlts, CNs, subjectAlts)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(unacceptedDeepSubDomainHosts, CNs, subjectAlts)
		then:
			thrown(SSLException)
	}

	void '4. Test STRICT_IE6 hostname verifier'() {
		setup:
			HostnameVerifier hostnameVerifier = HostnameVerifier.STRICT_IE6
		when:
			hostnameVerifier.check(hosts, CNs, null)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(hostsWithAlts, CNs, subjectAlts)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(unacceptedDeepSubDomainHosts, CNs, subjectAlts)
		then:
			thrown(SSLException)
	}

	void '5. Test ALLOW_ALL hostname verifier'() {
		setup:
			HostnameVerifier hostnameVerifier = HostnameVerifier.ALLOW_ALL
		when:
			hostnameVerifier.check(hosts, CNs, null)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(hostsWithAlts, CNs, subjectAlts)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(unacceptedDeepSubDomainHosts, CNs, null)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(unacceptedDeepSubDomainHosts, CNs, subjectAlts)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(unacceptedHosts, CNs, null)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(unacceptedHosts, CNs, subjectAlts)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(localhost, CNs, null)
		then:
			noExceptionThrown()
		when:
			hostnameVerifier.check(localhost, CNs, subjectAlts)
		then:
			noExceptionThrown()
	}

}
