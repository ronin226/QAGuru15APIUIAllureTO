package tests;

import api.AuthorizationApi;
import com.codeborne.selenide.Configuration;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;
import static api.AuthorizationApi.ALLURE_TESTOPS_SESSION;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static config.UserProperties.*;
import static helpers.CustomApiListener.withCustomTemplates;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.is;
import models.lombok.StepBody;
import models.lombok.CaseBody;
import models.lombok.StepData;
import java.util.ArrayList;
import static com.codeborne.selenide.Selenide.*;
import java.util.List;


public class AllureTestopsTests {



    @BeforeAll
    static void beforeAll() {
        Configuration.baseUrl = "https://allure.autotests.cloud";
        RestAssured.baseURI = "https://allure.autotests.cloud";
        RestAssured.filters(withCustomTemplates());
    }


    @Test
    void createTestCaseWithStepsApiTest() {

        AuthorizationApi authorizationApi = new AuthorizationApi();

        String xsrfToken = authorizationApi.getXsrfToken(USER_TOKEN);
        String authorizationCookie = authorizationApi
                .getAuthorizationCookie(USER_TOKEN, xsrfToken, USERNAME, PASSWORD);

        Faker faker = new Faker();
        String testCaseName = faker.name().title();
        CaseBody caseBody = new CaseBody();
        caseBody.setName(testCaseName);

        int testCaseId = given()
                .log().all()
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookies("XSRF-TOKEN", xsrfToken,
                        ALLURE_TESTOPS_SESSION, authorizationCookie)
                .body(caseBody)
                .contentType(JSON)
                .queryParam("projectId", "1771")
                .post("/api/rs/testcasetree/leaf")
                .then()
                .log().body()
                .statusCode(200)
                .body("name", is(testCaseName))
                .body("automated", is(false))
                .body("external", is(false))
                .extract()
                .path("id");

        String stepName;
        List<StepData> stepList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            stepName = testCaseName + " Step " + (i+1);
            StepData stepData = new StepData();
            stepData.setName(stepName);
            stepData.setStepsCount(i);
            stepList.add(stepData);
        }
        StepBody stepBody = new StepBody();
        stepBody.setSteps(stepList);
        given()
                .log().all()
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookies("XSRF-TOKEN", xsrfToken,
                        ALLURE_TESTOPS_SESSION, authorizationCookie)
                .body(stepBody)
                .contentType(JSON)
                .queryParam("projectId", "1771")
                //testcase/13917/scenario
                .post("/api/rs/testcase/" + testCaseId + "/scenario")
                .then()
                .log().body()
                .statusCode(200);


        open("/favicon.ico");
        getWebDriver().manage().addCookie(new Cookie(ALLURE_TESTOPS_SESSION, authorizationCookie));

        open("/project/1771/test-cases/" + testCaseId);
        for (int i = 0; i < stepList.size(); i++) {
            $$(".TreeElement__node").get(i).shouldHave(text(stepList.get(i).getName()));
        }
    }
}