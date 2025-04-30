package com.library.steps;

import com.library.utility.LibraryAPI_Util;
import io.cucumber.java.en.And;
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

import java.util.List;

import static org.hamcrest.Matchers.*;

public class APIStepDefs {

    /*---------- US01 ----------*/
    //Global variables:
    RequestSpecification givenPart = RestAssured.given().log().uri();
    Response response;
    ValidatableResponse thenPart;
    JsonPath jp;

    String expectedID;

    @Given("I logged Library api as a {string}")
    public void i_logged_library_api_as_a(String role){
        givenPart.header("x-library-token", LibraryAPI_Util.getToken(role));

        givenPart.log().all();
    }

    @Given("Accept header is {string}")
    public void accept_header_is(String acceptHeader) {
        givenPart.accept(acceptHeader);
    }

    @When("I send GET request to {string} endpoint")
    public void i_send_get_request_to_endpoint(String endpoint) {
        response = givenPart.when().get(endpoint);
        jp = response.jsonPath();
    }

    @Then("status code should be {int}")
    public void status_code_should_be(int expectedStatusCode) {
        thenPart = response.then();
        thenPart.statusCode(expectedStatusCode);
    }

    @Then("Response Content type is {string}")
    public void response_content_type_is(String expectedContentType) {
        thenPart.contentType(expectedContentType);
    }

    @Then("Each {string} field should not be null")
    public void each_field_should_note_be_null(String path) {
        thenPart.body(path, Matchers.everyItem(notNullValue()));
    }

    // << --------US 02------- >>

    @Given("Path param {string} is {string}")
    public void path_param_is(String pathParam, String value) {
        givenPart.pathParam(pathParam,value);
        expectedID = value;

    }

    @Then("{string} field should be same with path param")
    public void field_should_be_same_with_path_param(String path) {
        String actualID = jp.getString(path);
        Assert.assertEquals(expectedID,actualID);


    }

    @Then("following fields should not be null")
    public void following_fields_should_not_be_null(List<String>allPaths) {
        for (String eachPath : allPaths) {
            thenPart.body(eachPath,notNullValue());
        }

    }





}
