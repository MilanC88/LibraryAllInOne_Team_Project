package com.library.steps;

import com.library.utility.LibraryAPI_Util;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIStepDefs {



    /********** US01 **********/

    //Global variables:
    RequestSpecification givenPart = RestAssured.given().log().uri();
    Response response;
    ValidatableResponse thenPart;
    JsonPath jp;

    String expectedID;

    Map<String, Object> randomData=new HashMap<>();

    @Given("I logged Library api as a {string}")
    public void i_logged_library_api_as_a(String role) {
        //OPT1
        //givenPart.header("x-library-token", LibraryAPI_Util.getToken("librarian10@library","libraryUser"));

        //OPT2
        //givenPart.header("x-library-token", LibraryAPI_Util.getToken(ConfigurationReader.getProperty("librarian_username"),ConfigurationReader.getProperty("librarian_password")));

        //OPT3 -->Go to LibraryAPI_Util line 45 --> getToken(userType) Method
        givenPart.header("x-library-token", LibraryAPI_Util.getToken(role));

        givenPart.log().all();
    }

    @Given("Accept header is {string}")
    public void accept_header_is(String acceptHeader) {
        givenPart.accept(acceptHeader);

    }

    @When("I send GET request to {string} endpoint")
    public void i_send_get_request_to_endpoint(String endpoint) {

        //it has already declared globally:
        response = givenPart.when().get(endpoint);

        //We will need jsonPath object at next steps:
        jp = response.jsonPath();

    }

    @Then("status code should be {int}")
    public void status_code_should_be(int expectedStatusCode) {
        // OPT - 1
        Assert.assertEquals(expectedStatusCode, response.statusCode());

        // OPT - 2
        thenPart = response.then();

        thenPart.statusCode(expectedStatusCode);


    }

    @Then("Response Content type is {string}")
    public void response_content_type_is(String expectedContentType) {

        // OPT - 1
        Assert.assertEquals(expectedContentType, response.contentType());

        // OPT - 2
        thenPart.contentType(expectedContentType);
    }

    @Then("Each {string} field should not be null")
    public void each_field_should_not_be_null(String path) {

        // OPT - 1
        thenPart.body(path, Matchers.everyItem(Matchers.notNullValue()));

        // OPT - 2
        List<String> allData = jp.getList(path);
        for (String eachData : allData) {
            Assert.assertNotNull(eachData);
        }
    }

    /********** US02 **********/


    @Given("Path param {string} is {string}")
    public void path_param_is(String pathParam, String value) {
        givenPart.pathParam(pathParam, value);
        expectedID = value; //declare globally
    }

    @Then("{string} field should be same with path param")
    public void field_should_be_same_with_path_param(String path) {
        String actualID = jp.getString(path);
        Assert.assertEquals(expectedID, actualID);
    }

    @Then("following fields should not be null")
    public void following_fields_should_not_be_null(List<String> allPaths) {

        for (String eachPath : allPaths) {
            thenPart.body(eachPath, Matchers.notNullValue());
        }

    }

}
