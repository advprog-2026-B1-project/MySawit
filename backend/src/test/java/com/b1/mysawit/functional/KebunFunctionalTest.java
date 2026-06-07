package com.b1.mysawit.functional;

import com.b1.mysawit.functional.steps.KebunFunctionalSteps;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.annotations.Steps;
import net.serenitybdd.annotations.Title;
import net.serenitybdd.annotations.WithTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SerenityJUnit5Extension.class, SpringExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:ftdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.config.import=",
        "MYSAWIT_DELIVERY_ENABLED=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@WithTag("module:kebun")
class KebunFunctionalTest {

    @LocalServerPort
    private int port;

    @Steps
    private KebunFunctionalSteps steps;

    @BeforeEach
    void login() {
        steps.givenAdminIsLoggedIn(port);
    }

    @Test
    @Title("Admin dapat membuat kebun baru dengan koordinat persegi yang valid")
    void adminCanCreateKebunWithValidSquareCoordinates() {
        steps.whenCreateKebun(port, "KB-FT-001", "Kebun Functional Alpha",
                "[(5000,0),(5200,0),(5200,200),(5000,200)]");
        steps.thenStatusIs(201);
        steps.thenKodeKebunIs("KB-FT-001");
        steps.thenLuasIs("4.0");
    }

    @Test
    @Title("Sistem menolak kebun dengan kode yang sudah terdaftar (409)")
    void systemRejectsDuplicateKodeKebun() {
        steps.whenCreateKebun(port, "KB-FT-DUP", "Kebun Pertama",
                "[(6000,0),(6200,0),(6200,200),(6000,200)]");
        steps.thenStatusIs(201);

        steps.whenCreateKebun(port, "KB-FT-DUP", "Kebun Duplikat",
                "[(7000,0),(7200,0),(7200,200),(7000,200)]");
        steps.thenStatusIs(409);
    }

    @Test
    @Title("Sistem menolak koordinat yang bukan berbentuk persegi (400)")
    void systemRejectsNonSquareCoordinates() {
        steps.whenCreateKebun(port, "KB-FT-BAD", "Kebun Persegi Panjang",
                "[(8000,0),(8100,0),(8100,200),(8000,200)]");
        steps.thenStatusIs(400);
    }

    @Test
    @Title("Admin dapat melihat daftar semua kebun")
    void adminCanGetListOfKebun() {
        steps.whenCreateKebun(port, "KB-FT-LIST", "Kebun List",
                "[(9000,0),(9100,0),(9100,100),(9000,100)]");
        steps.thenStatusIs(201);

        steps.whenGetAllKebun(port);
        steps.thenStatusIs(200);
        steps.thenResponseIsList();
    }

    @Test
    @Title("Sistem menolak akses tanpa autentikasi (401)")
    void systemRejectsUnauthenticatedAccess() {
        steps.lastResponse = io.restassured.RestAssured.given()
                .baseUri("http://localhost:" + port)
                .get("/api/kebun")
                .then().extract().response();
        steps.thenStatusIs(401);
    }
}
