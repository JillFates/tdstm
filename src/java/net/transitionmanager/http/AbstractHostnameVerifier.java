package net.transitionmanager.http;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Hostname verifier abstract class
 */
public abstract class AbstractHostnameVerifier implements HostnameVerifier {

	/**
	 * This contains a list of 2nd-level domains that aren't allowed to
	 * have wildcards when combined with country-codes.
	 * For example: [*.co.uk].
	 * <p/>
	 * The [*.co.uk] problem is an interesting one.  Should we just hope
	 * that CA's would never foolishly allow such a certificate to happen?
	 * Looks like we're the only implementation guarding against this.
	 * Firefox, Curl, Sun Java 1.4, 5, 6 don't bother with this check.
	 */
	private final static String[] BAD_COUNTRY_2LDS =
			{"ac", "co", "com", "ed", "edu", "go", "gouv", "gov", "info",
					"lg", "ne", "net", "or", "org"};

	private final static String[] LOCALHOSTS = {"::1", "127.0.0.1",
			"localhost",
			"localhost.localdomain"};


	static {
		// Just in case developer forgot to manually sort the array.  :-)
		Arrays.sort(BAD_COUNTRY_2LDS);
		Arrays.sort(LOCALHOSTS);
	}

	protected AbstractHostnameVerifier() {
	}

	/**
	 * The javax.net.ssl.HostnameVerifier contract.
	 *
	 * @param host    'hostname' we used to create our socket
	 * @param session SSLSession with the remote server
	 * @return true if the host matched the one in the certificate.
	 */
	public boolean verify(String host, SSLSession session) {
		try {
			Certificate[] certs = session.getPeerCertificates();
			X509Certificate x509 = (X509Certificate) certs[0];
			check(new String[]{host}, x509);
			return true;
		}
		catch (SSLException e) {
			return false;
		}
	}

	public void check(String host, SSLSocket ssl) throws IOException {
		check(new String[]{host}, ssl);
	}

	public void check(String host, X509Certificate cert)
			throws SSLException {
		check(new String[]{host}, cert);
	}

	public void check(String host, String[] cns, String[] subjectAlts)
			throws SSLException {
		check(new String[]{host}, cns, subjectAlts);
	}

	public void check(String host[], SSLSocket ssl)
			throws IOException {
		if (host == null) {
			throw new NullPointerException("host to verify is null");
		}

		SSLSession session = ssl.getSession();
		if (session == null) {
			// In our experience this only happens under IBM 1.4.x when
			// spurious (unrelated) certificates show up in the server'
			// chain.  Hopefully this will unearth the real problem:
			InputStream in = ssl.getInputStream();
			in.available();
                /*
                  If you're looking at the 2 lines of code above because
                  you're running into a problem, you probably have two
                  options:

                    #1.  Clean up the certificate chain that your server
                         is presenting (e.g. edit "/etc/apache2/server.crt"
                         or wherever it is your server's certificate chain
                         is defined).

                                               OR

                    #2.   Upgrade to an IBM 1.5.x or greater JVM, or switch
                          to a non-IBM JVM.
                */

			// If ssl.getInputStream().available() didn't cause an
			// exception, maybe at least now the session is available?
			session = ssl.getSession();
			if (session == null) {
				// If it's still null, probably a startHandshake() will
				// unearth the real problem.
				ssl.startHandshake();

				// Okay, if we still haven't managed to cause an exception,
				// might as well go for the NPE.  Or maybe we're okay now?
				session = ssl.getSession();
			}
		}
		Certificate[] certs;
		try {
			certs = session.getPeerCertificates();
		} catch (SSLPeerUnverifiedException spue) {
			InputStream in = ssl.getInputStream();
			in.available();
			// Didn't trigger anything interesting?  Okay, just throw
			// original.
			throw spue;
		}
		X509Certificate x509 = (X509Certificate) certs[0];
		check(host, x509);
	}

	public void check(String[] host, X509Certificate cert)
			throws SSLException {

		String[] cns = Certificates.getCNs(cert);
		String[] subjectAlts = Certificates.getDNSSubjectAlts(cert);
		check(host, cns, subjectAlts);

	}

	public void check(final String[] hosts, final String[] cns,
					  final String[] subjectAlts, final boolean ie6,
					  final boolean strictWithSubDomains)
			throws SSLException {
		// Build up lists of allowed hosts For logging/debugging purposes.
		StringBuffer buf = new StringBuffer(32);
		buf.append('<');
		for (int i = 0; i < hosts.length; i++) {
			String h = hosts[i];
			h = h != null ? h.trim().toLowerCase() : "";
			hosts[i] = h;
			if (i > 0) {
				buf.append('/');
			}
			buf.append(h);
		}
		buf.append('>');
		String hostnames = buf.toString();
		// Build the list of names we're going to check.  Our DEFAULT and
		// STRICT implementations of the HostnameVerifier only use the
		// first CN provided.  All other CNs are ignored.
		// (Firefox, wget, curl, Sun Java 1.4, 5, 6 all work this way).
		TreeSet names = new TreeSet();
		if (cns != null && cns.length > 0 && cns[0] != null) {
			names.add(cns[0]);
			if (ie6) {
				for (int i = 1; i < cns.length; i++) {
					names.add(cns[i]);
				}
			}
		}
		if (subjectAlts != null) {
			for (int i = 0; i < subjectAlts.length; i++) {
				if (subjectAlts[i] != null) {
					names.add(subjectAlts[i]);
				}
			}
		}
		if (names.isEmpty()) {
			String msg = "Certificate for " + hosts[0] + " doesn't contain CN or DNS subjectAlt";
			throw new SSLException(msg);
		}

		// StringBuffer for building the error message.
		buf = new StringBuffer();

		boolean match = false;
		out:
		for (Iterator it = names.iterator(); it.hasNext();) {
			// Don't trim the CN, though!
			String cn = (String) it.next();
			cn = cn.toLowerCase();
			// Store CN in StringBuffer in case we need to report an error.
			buf.append(" <");
			buf.append(cn);
			buf.append('>');
			if (it.hasNext()) {
				buf.append(" OR");
			}

			// The CN better have at least two dots if it wants wildcard
			// action.  It also can't be [*.co.uk] or [*.co.jp] or
			// [*.org.uk], etc...
			boolean doWildcard = cn.startsWith("*.") &&
					cn.lastIndexOf('.') >= 0 &&
					!isIP4Address(cn) &&
					acceptableCountryWildcard(cn);

			for (int i = 0; i < hosts.length; i++) {
				final String hostName = hosts[i].trim().toLowerCase();
				if (doWildcard) {
					match = hostName.endsWith(cn.substring(1));
					if (match && strictWithSubDomains) {
						// If we're in strict mode, then [*.foo.com] is not
						// allowed to match [a.b.foo.com]
						match = countDots(hostName) == countDots(cn);
					}
				} else {
					match = hostName.equals(cn);
				}
				if (match) {
					break out;
				}
			}
		}
		if (!match) {
			throw new SSLException("hostname in certificate didn't match: " + hostnames + " !=" + buf);
		}
	}

	public static boolean isIP4Address(final String cn) {
		boolean isIP4 = true;
		String tld = cn;
		int x = cn.lastIndexOf('.');
		// We only bother analyzing the characters after the final dot
		// in the name.
		if (x >= 0 && x + 1 < cn.length()) {
			tld = cn.substring(x + 1);
		}
		for (int i = 0; i < tld.length(); i++) {
			if (!Character.isDigit(tld.charAt(0))) {
				isIP4 = false;
				break;
			}
		}
		return isIP4;
	}

	public static boolean acceptableCountryWildcard(final String cn) {
		int cnLen = cn.length();
		if (cnLen >= 7 && cnLen <= 9) {
			// Look for the '.' in the 3rd-last position:
			if (cn.charAt(cnLen - 3) == '.') {
				// Trim off the [*.] and the [.XX].
				String s = cn.substring(2, cnLen - 3);
				// And test against the sorted array of bad 2lds:
				int x = Arrays.binarySearch(BAD_COUNTRY_2LDS, s);
				return x < 0;
			}
		}
		return true;
	}

	public static boolean isLocalhost(String host) {
		host = host != null ? host.trim().toLowerCase() : "";
		if (host.startsWith("::1")) {
			int x = host.lastIndexOf('%');
			if (x >= 0) {
				host = host.substring(0, x);
			}
		}
		int x = Arrays.binarySearch(LOCALHOSTS, host);
		return x >= 0;
	}

	/**
	 * Counts the number of dots "." in a string.
	 *
	 * @param s string to count dots from
	 * @return number of dots
	 */
	public static int countDots(final String s) {
		int count = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '.') {
				count++;
			}
		}
		return count;
	}
}
