package net.transitionmanager.http;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.cert.X509Certificate;

/**
 * ************************************************************************
 * Copied from the not-yet-commons-ssl project at http://juliusdavies.ca/commons-ssl/
 * As the above project is accepted into Apache and its JARs become available in
 * the Maven 2 repos, we will have to switch to using the JARs instead
 * ************************************************************************
 * <p/>
 * Interface for checking if a hostname matches the names stored inside the
 * server's X.509 certificate.  Correctly implements
 * javax.net.ssl.HostnameVerifier, but that interface is not recommended.
 * Instead we added several check() methods that take SSLSocket,
 * or X509Certificate, or ultimately (they all end up calling this one),
 * String.  (It's easier to supply JUnit with Strings instead of mock
 * SSLSession objects!)
 * </p>
 Our check() methods throw exceptions if the name is
 * invalid, whereas javax.net.ssl.HostnameVerifier just returns true/false.
 * <p/>
 * We provide the HostnameVerifier.DEFAULT, HostnameVerifier.STRICT, and
 * HostnameVerifier.ALLOW_ALL implementations.  We also provide the more
 * specialized HostnameVerifier.DEFAULT_AND_LOCALHOST, as well as
 * HostnameVerifier.STRICT_IE6.  But feel free to define your own
 * implementations!
 * <p/>
 * Inspired by Sebastian Hauer's original StrictSSLProtocolSocketFactory in the
 * HttpClient "contrib" repository.
 *
 * @author Julius Davies
 * @author <a href="mailto:hauer@psicode.com">Sebastian Hauer
 * @since 8-Dec-2006
 *
 * <code>
 *     https://alvinalexander.com/java/jwarehouse/axis2-1.3/modules/kernel/src/org/apache/axis2/transport/nhttp/HostnameVerifier.java.shtml
 * </code>
 */
public interface HostnameVerifier extends javax.net.ssl.HostnameVerifier {
	boolean verify(String host, SSLSession session);

	void check(String host, SSLSocket ssl) throws IOException;

	void check(String host, X509Certificate cert) throws SSLException;

	void check(String host, String[] cns, String[] subjectAlts)
			throws SSLException;

	void check(String[] hosts, SSLSocket ssl) throws IOException;

	void check(String[] hosts, X509Certificate cert) throws SSLException;

	/**
	 * Checks to see if the supplied hostname matches any of the supplied CNs
	 * or "DNS" Subject-Alts.  Most implementations only look at the first CN,
	 * and ignore any additional CNs.  Most implementations do look at all of
	 * the "DNS" Subject-Alts. The CNs or Subject-Alts may contain wildcards
	 * according to RFC 2818.
	 *
	 * @param cns         CN fields, in order, as extracted from the X.509
	 *                    certificate.
	 * @param subjectAlts Subject-Alt fields of type 2 ("DNS"), as extracted
	 *                    from the X.509 certificate.
	 * @param hosts       The array of hostnames to verify.
	 * @throws SSLException If verification failed.
	 */
	void check(String[] hosts, String[] cns, String[] subjectAlts)
			throws SSLException;

	/**
	 * The DEFAULT HostnameVerifier works the same way as Curl and Firefox.
	 * <p/>
	 * The hostname must match either the first CN, or any of the subject-alts.
	 * A wildcard can occur in the CN, and in any of the subject-alts.
	 * <p/>
	 * The only difference between DEFAULT and STRICT is that a wildcard (such
	 * as "*.foo.com") with DEFAULT matches all subdomains, including
	 * "a.b.foo.com".
	 */
	public final static HostnameVerifier DEFAULT =
			new AbstractHostnameVerifier() {
				public final void check(final String[] hosts, final String[] cns,
										final String[] subjectAlts)
						throws SSLException {
					check(hosts, cns, subjectAlts, false, false);
				}

				public final String toString() {
					return "DEFAULT";
				}
			};

	/**
	 * The DEFAULT_AND_LOCALHOST HostnameVerifier works like the DEFAULT
	 * one with one additional relaxation:  a host of "localhost",
	 * "localhost.localdomain", "127.0.0.1", "::1" will always pass, no matter
	 * what is in the server's certificate.
	 */
	public final static HostnameVerifier DEFAULT_AND_LOCALHOST =
			new AbstractHostnameVerifier() {
				public final void check(final String[] hosts, final String[] cns,
										final String[] subjectAlts)
						throws SSLException {
					if (isLocalhost(hosts[0])) {
						return;
					}
					check(hosts, cns, subjectAlts, false, false);
				}

				public final String toString() {
					return "DEFAULT_AND_LOCALHOST";
				}
			};

	/**
	 * The STRICT HostnameVerifier works the same way as java.net.URL in Sun
	 * Java 1.4, Sun Java 5, Sun Java 6.  It's also pretty close to IE6.
	 * This implementation appears to be compliant with RFC 2818 for dealing
	 * with wildcards.
	 * <p/>
	 * The hostname must match either the first CN, or any of the subject-alts.
	 * A wildcard can occur in the CN, and in any of the subject-alts.  The
	 * one divergence from IE6 is how we only check the first CN.  IE6 allows
	 * a match against any of the CNs present.  We decided to follow in
	 * Sun Java 1.4's footsteps and only check the first CN.
	 * <p/>
	 * A wildcard such as "*.foo.com" matches only subdomains in the same
	 * level, for example "a.foo.com".  It does not match deeper subdomains
	 * such as "a.b.foo.com".
	 */
	public final static HostnameVerifier STRICT =
			new AbstractHostnameVerifier() {
				public final void check(final String[] host, final String[] cns,
										final String[] subjectAlts)
						throws SSLException {
					check(host, cns, subjectAlts, false, true);
				}

				public final String toString() {
					return "STRICT";
				}
			};

	/**
	 * The STRICT_IE6 HostnameVerifier works just like the STRICT one with one
	 * minor variation:  the hostname can match against any of the CN's in the
	 * server's certificate, not just the first one.  This behaviour is
	 * identical to IE6's behaviour.
	 */
	public final static HostnameVerifier STRICT_IE6 =
			new AbstractHostnameVerifier() {
				public final void check(final String[] host, final String[] cns,
										final String[] subjectAlts)
						throws SSLException {
					check(host, cns, subjectAlts, true, true);
				}

				public final String toString() {
					return "STRICT_IE6";
				}
			};

	/**
	 * The ALLOW_ALL HostnameVerifier essentially turns hostname verification
	 * off.  This implementation is a no-op, and never throws the SSLException.
	 */
	public final static HostnameVerifier ALLOW_ALL =
			new AbstractHostnameVerifier() {
				public final void check(final String[] host, final String[] cns,
										final String[] subjectAlts) {
					// Allow everything - so never blowup.
				}

				public final String toString() {
					return "ALLOW_ALL";
				}
			};

}
