package com.b1.mysawit.functional.steps;

import io.restassured.response.Response;
import net.serenitybdd.annotations.Step;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class KebunFunctionalSteps {

    private String sessionId;
    public Response lastResponse;

    @Step("Register dan login sebagai Admin")
    public void givenAdminIsLoggedIn(int port) {
        given().baseUri("http://localhost:" + port)
                .contentType("application/json")
                .body("{\"username\":\"ft_admin\",\"email\":\"ft_admin@test.com\"," +
                      "\"nama\":\"FT Admin\",\"password\":\"admin123\",\"role\":\"Admin\"}")
                .post("/api/register")
                .then().log().ifError();

        Response loginRes = given().baseUri("http://localhost:" + port)
                .contentType("application/json")
                .body("{\"email\":\"ft_admin@test.com\",\"password\":\"admin123\"}")
                .post("/api/login");

        sessionId = loginRes.cookie("JSESSIONID");
    }

    @Step("Membuat kebun baru dengan kode {0} dan koordinat {1}")
    public void whenCreateKebun(int port, String kode, String namaKebun, String koordinat) {
        lastResponse = given().baseUri("http://localhost:" + port)
                .cookie("JSESSIONID", sessionId)
                .contentType("application/json")
                .body("{\"kodeKebun\":\"" + kode + "\",\"namaKebun\":\"" + namaKebun +
                      "\",\"koordinat\":\"" + koordinat + "\"}")
                .post("/api/kebun");
    }

    @Step("Mengambil daftar kebun")
    public void whenGetAllKebun(int port) {
        lastResponse = given().baseUri("http://localhost:" + port)
                .cookie("JSESSIONID", sessionId)
                .get("/api/kebun");
    }

    @Step("Mengambil detail kebun dengan id {0}")
    public void whenGetKebunById(int port, int id) {
        lastResponse = given().baseUri("http://localhost:" + port)
                .cookie("JSESSIONID", sessionId)
                .get("/api/kebun/" + id);
    }

    @Step("Response status harus {0}")
    public void thenStatusIs(int expectedStatus) {
        assertThat(lastResponse.statusCode()).isEqualTo(expectedStatus);
    }

    @Step("Response harus mengandung kode kebun {0}")
    public void thenKodeKebunIs(String expectedKode) {
        assertThat(lastResponse.jsonPath().getString("kodeKebun")).isEqualTo(expectedKode);
    }

    @Step("Response harus mengandung luas {0} hektare")
    public void thenLuasIs(String expectedLuas) {
        String luas = lastResponse.jsonPath().getString("luasHektare");
        assertThat(Double.parseDouble(luas)).isEqualTo(Double.parseDouble(expectedLuas));
    }

    @Step("Response harus berupa list")
    public void thenResponseIsList() {
        assertThat(lastResponse.jsonPath().getList("$")).isNotNull();
    }
}
