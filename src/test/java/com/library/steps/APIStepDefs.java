package com.library.steps;

import com.library.pages.LoginPage;
import com.library.pages.BookPage;
import com.library.utility.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apiguardian.api.API;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class APIStepDefs {

    /*---------- US01 ----------*/
    //Global variables:
    RequestSpecification givenPart = RestAssured.given().log().uri();
    Response response;
    ValidatableResponse thenPart;
    JsonPath jp;

    String expectedID;
    Map<String, Object> randomData = new HashMap<>();
    BookPage bookPage = new BookPage();

    @Given("I logged Library api as a {string}")
    public void i_logged_library_api_as_a(String role) {
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
        givenPart.pathParam(pathParam, value);
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

    /********** US03 **********/

    @Given("Request Content Type header is {string}")
    public void request_content_type_header_is(String contentType) {
        givenPart.contentType(contentType);
    }

    @Given("I create a random {string} as request body")
    public void i_create_a_random_as_request_body(String type) {
        if (type.equalsIgnoreCase("book")) {
            randomData = LibraryAPI_Util.getRandomBookMap();
            givenPart.formParams(randomData);
        }
    }

    @When("I send POST request to {string} endpoint")
    public void i_send_post_request_to_endpoint(String endpoint) {
        response = givenPart.post(endpoint); // Save the response!
        jp = response.jsonPath(); // Initialize jsonPath object for later use
    }

    @Then("the field value for {string} path should be equal to {string}")
    public void the_field_value_for_path_should_be_equal_to(String path, String expectedValue) {
        String actualValue = jp.getString(path);
        Assert.assertEquals(expectedValue, actualValue);
    }

    @Then("{string} field should not be null")
    public void field_should_not_be_null(String path) {
        Assert.assertNotNull(path + " is null!", jp.getString(path));
    }

    @Then("UI, Database and API created book information must match")
    public void ui_database_and_api_created_book_information_must_match() {

        String bookId = jp.getString("book_id"); // book_id from API response

        /** Retrieving Data from API request **/

        String expectedTitle = randomData.get("name").toString();
        String expectedAuthor = randomData.get("author").toString();
        String expectedIsbn = randomData.get("isbn").toString();

        /**  Data Base **/
        String query = DatabaseHelper.getBookByIdQuery(bookId);
        DB_Util.runQuery(query);
        Map<String, Object> dbData = DB_Util.getRowMap(1);

        String dbIsbn = dbData.get("isbn").toString();
        String dbTitle = dbData.get("name").toString();
        String dbAuthor = dbData.get("author").toString();

        Assert.assertEquals("DB Title not matching", expectedTitle, dbTitle);
        Assert.assertEquals("DB Author not matching", expectedAuthor, dbAuthor);
        Assert.assertEquals("DB ISBN not matching", expectedIsbn, dbIsbn);

        /** UI verification **/

        bookPage.search.sendKeys(expectedTitle);

        BrowserUtil.waitFor(1);

        List<String> uiBookRow = BrowserUtil.getElementsText(bookPage.allRows.get(0).findElements(By.tagName("td")));

        String uiIsbn = uiBookRow.get(1);
        String uiTitle = uiBookRow.get(2);
        String uiAuthor = uiBookRow.get(3);

        Assert.assertEquals("UI Title not matching", expectedTitle, uiTitle);
        Assert.assertEquals("UI Author not matching", expectedAuthor, uiAuthor);
        Assert.assertEquals("UI ISBN not matching", expectedIsbn, uiIsbn);

    }

    /********** US04 - 1 **********/
    //No need any steps

    /********** US04 - 2 **********/

    @Then("created user information should match with Database")
    public void created_user_information_should_match_with_database() {

        int id = jp.getInt("user_id");

        String query="select full_name,email,user_group_id,status,start_date,end_date,address " +
                "from users where id="+id;

        DB_Util.runQuery(query);


        Map<String, Object> actualData = DB_Util.getRowMap(1);

        // Expected --> API --> randomData --> map

        String password= (String) randomData.remove("password");


        Assert.assertEquals(randomData,actualData);

        // Add password into randomData
        randomData.put("password",password);

        System.out.println("randomData = " + randomData);
        System.out.println("actualData = " + actualData);

    }

    @Then("created user should be able to login Library UI")
    public void created_user_should_be_able_to_login_library_ui() throws InterruptedException {

        LoginPage loginPage=new LoginPage();

        String email = (String) randomData.get("email");

        String password = (String) randomData.get("password");

        loginPage.login(email,password);

        BookPage bookPage=new BookPage();
        BrowserUtil.waitForVisibility(bookPage.accountHolderName,15);

    }
    @Then("created user name should appear in Dashboard Page")
    public void created_user_name_should_appear_in_dashboard_page() {

        BookPage bookPage=new BookPage();

        String uiFullName = bookPage.accountHolderName.getText();

        String apiFullName = (String) randomData.get("full_name");

        Assert.assertEquals(apiFullName,uiFullName);

    }


    //05

    String token;

    @Given("I logged Library api with credentials {string} and {string}")
    public void i_logged_library_api_with_credentials_and(String email, String password) {

        token = LibraryAPI_Util.getToken(email, password);

    }

    @Given("I send {string} information as request body")
    public void i_send_information_as_request_body(String key) {


        givenPart.formParam(key, token);


    }


}
