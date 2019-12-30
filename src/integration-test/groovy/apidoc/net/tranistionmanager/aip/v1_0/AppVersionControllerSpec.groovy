package apidoc.net.tranistionmanager.aip.v1_0

import geb.spock.GebSpec
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.json.JsonSlurper
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.specification.RequestSpecification
import org.junit.Rule
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.restassured3.RestAssuredRestDocumentation

import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration

@Integration
@Rollback
class AppVersionControllerSpec extends GebSpec {

	static final String APP_VERSION_ENDPOINT = '/tdstm/api/appVersion'

	@Value('${local.server.port}')
	protected int port

	@Rule
	protected JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation('build/docs/generated-snippets')

	private RequestSpecification documentationSpec

	def setupSpec() {
	}

	def setup() {
		//set documentation specification
		this.documentationSpec = new RequestSpecBuilder()
			.addFilter(documentationConfiguration(this.restDocumentation))
			.build()
	}

	void "Test and document appVersion request (GET request, index action) to end-point: /api/appVersion"() {
		given: "A rest request for the App Version"
			RestAssured.given()
					   .header('Accept-Version', '1.0')
					   .accept(MediaType.APPLICATION_JSON_VALUE)
					   .contentType(MediaType.APPLICATION_JSON_VALUE)
					   .when()
					   .port(this.port)
					   .get(APP_VERSION_ENDPOINT)
					   .then().assertThat().statusCode(HttpStatus.OK.value())

		and: "request specification for documenting list ApplicationVersion API"
			RequestSpecification requestSpecification = RestAssured
				.given(this.documentationSpec)
				.accept(MediaType.APPLICATION_JSON_VALUE)
				.filter(
					RestAssuredRestDocumentation.document(
						'app-version-example',
						PayloadDocumentation.responseFields(
							PayloadDocumentation.fieldWithPath('status').ignored(),
							PayloadDocumentation.fieldWithPath('data').ignored(),
							PayloadDocumentation.fieldWithPath('data.version').type(JsonFieldType.STRING).description('The version of the Transition Manager'),
							)
					)
				)
		when: "GET request is made to end-point for the Application Version"
			def response = requestSpecification
				.when()
				.port(this.port)
				.get(APP_VERSION_ENDPOINT)

			def responseJsonObject = new JsonSlurper().parseText(response.body().asString())

		then: "status is OK"
			response.then()
					.assertThat()
					.statusCode(HttpStatus.OK.value())

			responseJsonObject instanceof Map

			responseJsonObject.status == 'success'
			responseJsonObject.data.version

	}

}
