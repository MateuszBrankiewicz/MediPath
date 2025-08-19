package com.adam.medipathbackend;

import com.adam.medipathbackend.config.CORSConfig;
import com.adam.medipathbackend.config.HttpSessionConfig;
import com.adam.medipathbackend.config.MailConfig;
import com.adam.medipathbackend.controllers.CityController;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.CityRepository;
import com.adam.medipathbackend.repository.PasswordResetEntryRepository;
import com.adam.medipathbackend.repository.UserRepository;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
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

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CityRepository cityRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordResetEntryRepository preRepository;

    private List<City> cityList;

    private final String EXAMPLE_MAIL = "test@mail.com";

    private final String EXAMPLE_TOKEN = "1234567890abcdef";

    private final User exampleValidUser = new User(EXAMPLE_MAIL, "Name", "Surname", "1234567890", LocalDate.of(1900, 12, 12), new Address("Province", "City", "Street", "Number", "PostalCode"), "123456789", "$argon2id$v=19$m=60000,t=10,p=1$MOrLn9JdvWpJyJDMZ4Z9qg$zgHTASZaQH9zoTUO0bd0re+6G523ZUreKFmWQSu+f24", new UserSettings("PL", true, true));

    @BeforeEach
    public void createTestData() {
        this.cityList = new ArrayList<>();
        this.cityList.add(new City( "Lublin"));
        this.cityList.add(new City( "Lubin"));
        this.cityList.add(new City( "Warszawa"));
    }
    @Test
    public void getAllCities() throws Exception {

        when(cityRepository.findAll()).thenReturn(cityList);

        mvc.perform(get("/api/cities").contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0].name").value("Lublin"))
                .andExpect(jsonPath("$[1].name").value("Lubin"))
                .andExpect(jsonPath("$[2].name").value("Warszawa"));

    }
    @Test
    public void getRegexCities() throws Exception {

        when(cityRepository.findAll("Lub")).thenReturn(cityList.subList(0, 2));

        mvc.perform(get("/api/cities/Lub").contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Lublin"))
                .andExpect(jsonPath("$[1].name").value("Lubin"));

    }
    @Test
    public void getInvalidCity() throws Exception {

        when(cityRepository.findAll("Abcd")).thenReturn(new ArrayList<>());

        mvc.perform(get("/api/cities/Abcd").contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    public void tryRegisterUser() throws Exception {

        when(userRepository.findByEmail(EXAMPLE_MAIL)).thenReturn(Optional.empty());

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
                "\"password\": \"passwordpassword\"" +
                "}";

        mvc.perform(post("/api/users/register").contentType("application/json").content(exampleUser))
                .andExpect(status().isCreated());
    }

    @Test
    public void tryRegisterUserDuplicateEmail() throws Exception {

        when(userRepository.findByEmail(EXAMPLE_MAIL)).thenReturn(Optional.of(exampleValidUser));
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
                "\"password\": \"passwordpassword\"" +
                "}";

        mvc.perform(post("/api/users/register").contentType("application/json").content(exampleUser))
                .andExpect(status().isConflict());
    }

    @Test
    public void tryRegisterUserMissingName() throws Exception {

        when(userRepository.findByEmail(EXAMPLE_MAIL)).thenReturn(Optional.empty());
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
                "\"password\": \"passwordpassword\"" +
                "}";

        mvc.perform(post("/api/users/register").contentType("application/json").content(exampleUser))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("missing fields in request body"))
                .andExpect(jsonPath("$.fields[0]").value("surname"));
    }
    @Test
    public void tryRegisterUserMissingData() throws Exception {

        when(userRepository.findByEmail(EXAMPLE_MAIL)).thenReturn(Optional.empty());
        String exampleUser = "{}";

        mvc.perform(post("/api/users/register").contentType("application/json").content(exampleUser))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("missing fields in request body"))
                .andExpect(jsonPath("$.fields[0]").value("name"))
                .andExpect(jsonPath("$.fields[1]").value("surname"))
                .andExpect(jsonPath("$.fields[2]").value("email"))
                .andExpect(jsonPath("$.fields[3]").value("city"))
                .andExpect(jsonPath("$.fields[4]").value("province"))
                .andExpect(jsonPath("$.fields[5]").value("number"))
                .andExpect(jsonPath("$.fields[6]").value("postalCode"))
                .andExpect(jsonPath("$.fields[7]").value("birthDate"))
                .andExpect(jsonPath("$.fields[8]").value("govID"))
                .andExpect(jsonPath("$.fields[9]").value("phoneNumber"))
                .andExpect(jsonPath("$.fields[10]").value("password"));
    }
    @Test
    public void tryRegisterUserWithDuplicateGovID() throws Exception {

        when(userRepository.findByEmail(EXAMPLE_MAIL)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EXAMPLE_MAIL)).thenReturn(Optional.of(exampleValidUser));

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
                "\"password\": \"passwordpassword\"" +
                "}";

        mvc.perform(post("/api/users/register").contentType("application/json").content(exampleUser))
                .andExpect(status().isConflict());
    }
    @Test
    public void tryLogInSuccess() throws Exception {
        when(userRepository.findByEmail(EXAMPLE_MAIL)).thenReturn(Optional.of(exampleValidUser));

        String exampleLogin = "{" +
                "\"email\": \"" + EXAMPLE_MAIL + "\"," +
                "\"password\": \"passwordpassword\"" +
                "}";

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(exampleLogin))
                .andExpect(status().isOk());
    }

    @Test
    public void tryLogInInvalidEmail() throws Exception {

        when(userRepository.findByEmail("badtest@mail.com")).thenReturn(Optional.empty());

        String exampleLogin = "{" +
                "\"email\": \"badtest@mail.com\"," +
                "\"password\": \"passwordpassword\"" +
                "}";

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(exampleLogin))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid email or password"));
    }
    @Test
    public void tryLogInValidEmailInvalidPassword() throws Exception {

        when(userRepository.findByEmail(EXAMPLE_MAIL)).thenReturn(Optional.of(exampleValidUser));

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
                .andExpect(jsonPath("$.message").value("missing fields in request body"))
                .andExpect(jsonPath("$.fields[0]").value("password"));
    }

    @Test
    public void tryLogInNoData() throws Exception {

        String exampleLogin = "{}";

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(exampleLogin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("missing fields in request body"))
                .andExpect(jsonPath("$.fields[0]").value("email"))
                .andExpect(jsonPath("$.fields[1]").value("password"));
    }


    @Test
    public void tryResetMailNoParam() throws Exception {


        mvc.perform(get("/api/users/resetpassword"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("missing email in request parameters"));

    }

    @Test
    public void tryResetMailValid() throws Exception {

        when(userRepository.findByEmail(EXAMPLE_MAIL)).thenReturn(Optional.of(exampleValidUser));


        mvc.perform(get("/api/users/resetpassword?address=test@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("password reset mail has been sent, if the account exists"));

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];

            assertEquals(1, receivedMessage.getAllRecipients().length);
            assertEquals(EXAMPLE_MAIL, receivedMessage.getAllRecipients()[0].toString());
            assertEquals("MediPath <service@medipath.com>", receivedMessage.getFrom()[0].toString());
            assertEquals("Password reset request", receivedMessage.getSubject());
        });

    }


    @Test
    public void tryResetMailSecondaryNoData() throws Exception {

        String exampleLogin = "{}";

        mvc.perform(post("/api/users/resetpassword").contentType(MediaType.APPLICATION_JSON).content(exampleLogin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("missing fields in request body"))
                .andExpect(jsonPath("$.fields[0]").value("token"))
                .andExpect(jsonPath("$.fields[1]").value("password"));
    }

    @Test
    public void tryResetMailSecondaryValidToken() throws Exception {


        when(preRepository.findValidToken(EXAMPLE_TOKEN)).thenReturn(Optional.of(new PasswordResetEntry(EXAMPLE_MAIL, EXAMPLE_TOKEN)));
        when(userRepository.findByEmail(EXAMPLE_MAIL)).thenReturn(Optional.of(exampleValidUser));


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

        when(preRepository.findValidToken(EXAMPLE_TOKEN)).thenReturn(Optional.empty());

        String exampleLogin = "{" +
                "\"token\": \""+ EXAMPLE_TOKEN +"\"," +
                "\"password\": \"anotherpassword\"" +
                "}";

        mvc.perform(post("/api/users/resetpassword").contentType(MediaType.APPLICATION_JSON).content(exampleLogin))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.message").value("token invalid or expired"));
    }

}
