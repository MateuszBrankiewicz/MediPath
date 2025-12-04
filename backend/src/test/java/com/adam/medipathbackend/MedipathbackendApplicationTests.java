package com.adam.medipathbackend;

import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.CityRepository;
import com.adam.medipathbackend.repository.PasswordResetEntryRepository;
import com.adam.medipathbackend.repository.ScheduleRepository;
import com.adam.medipathbackend.repository.UserRepository;
import com.adam.medipathbackend.services.ScheduleService;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;

import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class MedipathbackendApplicationTests {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("user", "admin"))
            .withPerMethodLifecycle(false);

    @DynamicPropertySource
    static void configureMailHost(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> greenMail.getSmtp().getBindTo());
        registry.add("spring.mail.port", () -> greenMail.getSmtp().getPort());
    }

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> "mongodb://localhost:27017/test");
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetEntryRepository preRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ScheduleService scheduleService;

    private final String EXAMPLE_MAIL = "test@mail.com";

    private final String EXAMPLE_TOKEN = "1234567890abcdef";

    private final User exampleValidUser = new User(EXAMPLE_MAIL, "Name", "Surname", "1234567890", LocalDate.of(1900, 12, 12), new Address("Province", "City", "Street", "Number", "PostalCode"), "123456789", "$argon2id$v=19$m=60000,t=10,p=1$MOrLn9JdvWpJyJDMZ4Z9qg$zgHTASZaQH9zoTUO0bd0re+6G523ZUreKFmWQSu+f24", new UserSettings("PL", true, true, 1));

    private Argon2PasswordEncoder argon2PasswordEncoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);

    private final String TEST_PASSWORD = "passwordpassword";

    @BeforeEach
    void setUp() {
        mongoTemplate.getCollectionNames().stream()
                .filter(name -> !name.startsWith("system."))
                .forEach(name -> mongoTemplate.dropCollection(name));
    }

    @Test
    void contextLoads() {
    }

    @Test
    public void givenCities_whenGetAllCities_ReturnAll() throws Exception {


        cityRepository.saveAll(List.of(
                new City("Lublin"),
                new City("Lubin"),
                new City("Warszawa")
        ));


        mvc.perform(get("/api/cities").contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0].name").value("Lublin"))
                .andExpect(jsonPath("$[1].name").value("Lubin"))
                .andExpect(jsonPath("$[2].name").value("Warszawa"));

    }
    @Test
    public void givenCities_whenGetRegex_ReturnFittingRegex() throws Exception {


        cityRepository.saveAll(List.of(
                new City("Lublin"),
                new City("Lubin"),
                new City("Warszawa")
        ));


        mvc.perform(get("/api/cities/Lub").contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Lublin"))
                .andExpect(jsonPath("$[1].name").value("Lubin"));

    }
    @Test
    public void givenCities_WhenGetInvalid_ReturnNone() throws Exception {

        cityRepository.saveAll(List.of(
                new City("Lublin"),
                new City("Lubin"),
                new City("Warszawa")
        ));


        mvc.perform(get("/api/cities/Abcd").contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }




    @Test
    public void whenRegisterUser_ThenCreated() throws Exception {

        String exampleUser = "{" +
                "\"name\": \"TestName\"," +
                "\"surname\": \"TestSurname\"," +
                "\"email\": \"" + EXAMPLE_MAIL + "\"," +
                "\"govID\": \"90010112340\"," +
                "\"birthDate\": \"01-01-1990\"," +
                "\"province\": \"Abcd\"," +
                "\"city\": \"citycity\"," +
                "\"postalCode\": \"01-001\"," +
                "\"phoneNumber\": \"123456789\"," +
                "\"street\": \"aaa st.\"," +
                "\"number\": \"5a\"," +
                "\"password\": \"" + TEST_PASSWORD + "\"" +
                "}";

        mvc.perform(post("/api/users/register").contentType("application/json").content(exampleUser))
                .andExpect(status().isCreated());
    }

    @Test
    public void givenExistingUser_WhenRegisterDuplicateMail_ThenConflict() throws Exception {

        User duplicate = new User(EXAMPLE_MAIL, "Example", "Example", "123",
                LocalDate.of(2000, 1, 1),
                new Address("Province", "City", "Street", "Number", "00-000"),
                "123456789", "hash",
                new UserSettings("PL", false, false, 1));
        userRepository.save(duplicate);

        String exampleUser = "{" +
                "\"name\": \"TestName\"," +
                "\"surname\": \"TestSurname\"," +
                "\"email\": \"" + EXAMPLE_MAIL + "\"," +
                "\"govID\": \"90010112340\"," +
                "\"birthDate\": \"01-01-1990\"," +
                "\"province\": \"Abcd\"," +
                "\"city\": \"citycity\"," +
                "\"postalCode\": \"01-001\"," +
                "\"phoneNumber\": \"123456789\"," +
                "\"street\": \"aaa st.\"," +
                "\"number\": \"5a\"," +
                "\"password\": \"" + TEST_PASSWORD + "\"" +
                "}";

        mvc.perform(post("/api/users/register")
                        .contentType("application/json").content(exampleUser))
                .andExpect(status().isConflict());
    }

    @Test
    public void tryRegisterUserMissingName() throws Exception {

        String exampleUser = "{" +
                "\"name\": \"TestName\"," +
                "\"email\": \"" + EXAMPLE_MAIL + "\"," +
                "\"govID\": \"90010112340\"," +
                "\"birthDate\": \"01-01-1990\"," +
                "\"province\": \"Abcd\"," +
                "\"city\": \"citycity\"," +
                "\"street\": \"aaa st.\"," +
                "\"postalCode\": \"01-001\"," +
                "\"phoneNumber\": \"123456789\"," +
                "\"number\": \"5a\"," +
                "\"password\": \"" + TEST_PASSWORD + "\"" +
                "}";

        mvc.perform(post("/api/users/register").contentType("application/json").content(exampleUser))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("missing fields in request body: [surname]"));
    }
    @Test
    public void tryRegisterUserMissingData() throws Exception {

        String exampleUser = "{}";

        mvc.perform(post("/api/users/register").contentType("application/json").content(exampleUser))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("missing fields in request body: [name, surname, email, city, " +
                                "province, number, postalCode, birthDate, govID, phoneNumber, password]"));
    }
    @Test
    public void tryRegisterUserWithDuplicateGovID() throws Exception {

        User duplicate = new User("mail@mail.co.uk", "Example", "Example", "1234567890",
                LocalDate.of(2000, 1, 1),
                new Address("Province", "City", "Street", "Number", "00-000"),
                "123456789", "hash",
                new UserSettings("PL", false, false, 1));
        userRepository.save(duplicate);

        String exampleUser = "{" +
                "\"name\": \"TestName\"," +
                "\"surname\": \"TestSurname\"," +
                "\"email\": \"" + EXAMPLE_MAIL + "\"," +
                "\"govID\": \"1234567890\"," +
                "\"birthDate\": \"01-01-1990\"," +
                "\"province\": \"Abcd\"," +
                "\"city\": \"citycity\"," +
                "\"postalCode\": \"01-001\"," +
                "\"street\": \"aaa st.\"," +
                "\"phoneNumber\": \"1234567890\"," +
                "\"number\": \"5a\"," +
                "\"password\": \"" + TEST_PASSWORD + "\"" +
                "}";

        mvc.perform(post("/api/users/register").contentType("application/json").content(exampleUser))
                .andExpect(status().isConflict());
    }
    @Test
    public void tryLogInSuccess() throws Exception {

        String passwordHash = argon2PasswordEncoder.encode(TEST_PASSWORD);
        User validUser = new User(EXAMPLE_MAIL, "Example", "Example", "1234567890",
                LocalDate.of(2000, 1, 1),
                new Address("Province", "City", "Street", "Number", "00-000"),
                "123456789", passwordHash,
                new UserSettings("PL", false, false, 1));
        userRepository.save(validUser);



        String exampleLogin = "{" +
                "\"email\": \"" + EXAMPLE_MAIL + "\"," +
                "\"password\": \"" + TEST_PASSWORD + "\"" +
                "}";

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(exampleLogin))
                .andExpect(status().isOk());
    }

    @Test
    public void tryLogInInvalidEmail() throws Exception {


        String exampleLogin = "{" +
                "\"email\": \"badtest@mail.com\"," +
                "\"password\": \"" + TEST_PASSWORD + "\"" +
                "}";

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(exampleLogin))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid email or password"));
    }
    @Test
    public void tryLogInValidEmailInvalidPassword() throws Exception {

        String passwordHash = argon2PasswordEncoder.encode(TEST_PASSWORD);
        User validUser = new User(EXAMPLE_MAIL, "Example", "Example", "1234567890",
                LocalDate.of(2000, 1, 1),
                new Address("Province", "City", "Street", "Number", "00-000"),
                "123456789", passwordHash,
                new UserSettings("PL", false, false, 1));
        userRepository.save(validUser);

        String exampleLogin = "{" +
                "\"email\": \"" + EXAMPLE_MAIL + "\"," +
                "\"password\": \"invalid\"" +
                "}";

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(exampleLogin))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid email or password"));
    }
    @Test
    public void tryLogInNoPassword() throws Exception {

        String exampleLogin = "{" +
                "\"email\": \"" + EXAMPLE_MAIL + "\"" +
                "}";

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(exampleLogin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("missing fields in request body: [password]"));
    }

    @Test
    public void tryLogInNoData() throws Exception {

        String exampleLogin = "{}";

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(exampleLogin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("missing fields in request body: [email, password]"));
    }


    @Test
    public void tryResetMailNoParam() throws Exception {


        mvc.perform(get("/api/users/resetpassword"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("missing address in request parameters"));

    }

    @Test
    public void tryResetMailValid() throws Exception {

        //when(userRepository.findByEmail(EXAMPLE_MAIL)).thenReturn(Optional.of(exampleValidUser));

        User validUser = new User(EXAMPLE_MAIL, "Example", "Example", "1234567890",
                LocalDate.of(2000, 1, 1),
                new Address("Province", "City", "Street", "Number", "00-000"),
                "123456789", "password",
                new UserSettings("PL", false, false, 1));
        userRepository.save(validUser);

        mvc.perform(get("/api/users/resetpassword?address=" + EXAMPLE_MAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("password reset mail has been sent, if the account exists"));

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];

            assertEquals(1, receivedMessage.getAllRecipients().length);
            assertEquals(EXAMPLE_MAIL, receivedMessage.getAllRecipients()[0].toString());
            assertEquals("MediPath <service@medipath.com>", receivedMessage.getFrom()[0].toString());
            assertEquals("Prośba o zmianę hasła Medipath", receivedMessage.getSubject());
        });

    }


    @Test
    public void tryResetMailSecondaryNoData() throws Exception {

        String exampleLogin = "{}";

        mvc.perform(post("/api/users/resetpassword").contentType(MediaType.APPLICATION_JSON).content(exampleLogin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("missing fields in request body token password "));
    }

    @Test
    public void tryResetMailSecondaryValidToken() throws Exception {

        String passwordHash = argon2PasswordEncoder.encode(TEST_PASSWORD);
        User validUser = new User(EXAMPLE_MAIL, "Example", "Example", "1234567890",
                LocalDate.of(2000, 1, 1),
                new Address("Province", "City", "Street", "Number", "00-000"),
                "123456789", passwordHash,
                new UserSettings("PL", false, false, 1));
        userRepository.save(validUser);

        PasswordResetEntry passwordResetEntry = new PasswordResetEntry(EXAMPLE_MAIL, EXAMPLE_TOKEN);
        preRepository.save(passwordResetEntry);

        String exampleLogin = "{" +
                "\"token\": \"" + EXAMPLE_TOKEN +"\"," +
                "\"password\": \"anotherpassword\"" +
                "}";

        mvc.perform(post("/api/users/resetpassword").contentType(MediaType.APPLICATION_JSON).content(exampleLogin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("password reset successfully"));
    }
    @Test
    public void tryResetMailSecondaryInvalidToken() throws Exception {

        String exampleLogin = "{" +
                "\"token\": \""+ EXAMPLE_TOKEN +"\"," +
                "\"password\": \"anotherpassword\"" +
                "}";

        mvc.perform(post("/api/users/resetpassword").contentType(MediaType.APPLICATION_JSON).content(exampleLogin))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.message").value("token invalid or expired"));
    }


    @Test
    public void givenSchedulesInPast_WhenDeleteOldSchedules_ThenLessSchedules() {

        LocalDateTime now = LocalDateTime.now().withNano(0);

        List<Schedule> oldSchedulesToDelete = List.of(
                new Schedule(now.minusDays(1).withHour(15).withMinute(30), now.minusDays(1).withHour(15).withMinute(45),
                        new DoctorDigest("d1", "Example", "Doctor", new ArrayList<>()),
                        new InstitutionDigest("i1", "ExampleInstitution")),
                new Schedule(now.minusDays(2).withHour(18).withMinute(0), now.minusDays(2).withHour(18).withMinute(30),
                        new DoctorDigest("d2", "Doctor", "Second", new ArrayList<>()),
                        new InstitutionDigest("i2", "AnotherInstitution"))
        );
        Schedule oldBookedSchedule = new Schedule(now.minusDays(1).withHour(15).withMinute(30), now.minusDays(1).withHour(15).withMinute(45),
                new DoctorDigest("d3", "Another", "Doctor", new ArrayList<>()),
                new InstitutionDigest("i1", "ExampleInstitution"));
        oldBookedSchedule.setBooked(true);
        oldBookedSchedule.setVisitId("visit1");

        List<Schedule> newSchedules = List.of(
                new Schedule(now.plusDays(1).withHour(15).withMinute(30), now.plusDays(1).withHour(15).withMinute(45),
                        new DoctorDigest("d1", "Example", "Doctor", new ArrayList<>()),
                        new InstitutionDigest("i1", "ExampleInstitution")),
                new Schedule(now.plusDays(2).withHour(18).withMinute(0), now.plusDays(2).withHour(18).withMinute(30),
                        new DoctorDigest("d2", "Doctor", "Second", new ArrayList<>()),
                        new InstitutionDigest("i2", "AnotherInstitution"))
        );
        scheduleRepository.saveAll(oldSchedulesToDelete);
        scheduleRepository.save(oldBookedSchedule);
        scheduleRepository.saveAll(newSchedules);
        assertEquals(5, scheduleRepository.count());

        scheduleService.pruneOldSchedules();

        assertEquals(3, scheduleRepository.count());

        List<Schedule> retrievedSchedules = scheduleRepository.findAll();
        assertEquals(retrievedSchedules.getFirst().getId(), oldBookedSchedule.getId());
        assertEquals(retrievedSchedules.get(1).getId(), newSchedules.getFirst().getId());
        assertEquals(retrievedSchedules.getLast().getId(), newSchedules.getLast().getId());

    }

}
