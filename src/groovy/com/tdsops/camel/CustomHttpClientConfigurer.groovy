package com.tdsops.camel

import org.apache.camel.component.http4.HttpClientConfigurer
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.HttpClientConnectionManager
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.BasicHttpClientConnectionManager

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

/**
 * Custom HTTP Client Configurer for Camel
 * Used in conjunction with httpClientConfigurer HttpEndpoint Options like
 *
 * http://url:port/path?httpClientConfigurer=customHttpClientConfigurer
 */
class CustomHttpClientConfigurer implements HttpClientConfigurer {

	@Override
	void configureHttpClient(HttpClientBuilder httpClientBuilder) {
		configureTrustStore(httpClientBuilder)
	}

	/**
	 * Trust Store logic for verifying certificates from root ca by java itself
	 * Security note: Use this only for non-production environments and end-points
	 *
	 * @param httpClientBuilder
	 */
	private void configureTrustStore(HttpClientBuilder httpClientBuilder) {
		TrustManager[] trustAllCerts = [
				new X509TrustManager() {
					X509Certificate[] getAcceptedIssuers() { return [] }

					void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

					void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
				}
		] as TrustManager[]

		SSLContext sslContext = SSLContext.getInstance('TLS')
		sslContext.init(null, trustAllCerts, new SecureRandom())
		SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)

		final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
				.register('http', PlainConnectionSocketFactory.getSocketFactory())
				.register('https', connectionSocketFactory)
				.build();

		HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry)
		httpClientBuilder.setSslcontext(sslContext)
		httpClientBuilder.setSSLSocketFactory(connectionSocketFactory)
		httpClientBuilder.setConnectionManager(ccm)
	}
}
