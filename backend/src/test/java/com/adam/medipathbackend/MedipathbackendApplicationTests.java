package com.adam.medipathbackend;

import com.adam.medipathbackend.config.CORSConfig;
import com.adam.medipathbackend.config.HttpSessionConfig;
import com.adam.medipathbackend.config.MailConfig;
import com.adam.medipathbackend.controllers.CityController;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.*;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.mongo.MongoSession;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Import(HttpSessionConfig.class) // <-- Add this!
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
    private InstitutionRepository institutionRepository;

    @MockitoBean
    private PasswordResetEntryRepository preRepository;

    @MockitoBean
    private ScheduleRepository scheduleRepository;

    @MockitoBean
    private VisitRepository visitRepository;

    @MockitoBean
    private CommentRepository commentRepository;

    @MockitoBean
    private MedicalHistoryRepository medicalHistoryRepository;

    @Autowired
    private SessionRepository<MongoSession> sessionRepository;


    private List<City> cityList;

    private final String EXAMPLE_MAIL = "test@mail.com";

    private final String EXAMPLE_TOKEN = "1234567890abcdef";

    private final User exampleValidUser = new User(EXAMPLE_MAIL, "Name", "Surname", "1234567890", LocalDate.of(1900, 12, 12), new Address("Province", "City", "Street", "Number", "PostalCode"), "123456789", "$argon2id$v=19$m=60000,t=10,p=1$MOrLn9JdvWpJyJDMZ4Z9qg$zgHTASZaQH9zoTUO0bd0re+6G523ZUreKFmWQSu+f24", new UserSettings("PL", true, true, 1));

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

    @Test
    public void trySearchInvalidType() throws Exception {
        mvc.perform(post("/api/search/?type=sneed"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("unknown type"));
    }

    ArrayList<StaffDigest> exampleStaffDigests = new ArrayList<>(List.of(new StaffDigest("1", "Example", "User", new ArrayList<>(List.of("spec")), 2, "")));

    @Test
    public void trySearchDoctorSuccess() throws Exception {

        when(institutionRepository.findDoctorsByCity(".*.*", ".*")).thenReturn(exampleStaffDigests);

        mvc.perform(post("/api/search/?type=doctor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result").isNotEmpty())
                .andExpect(jsonPath("$.result[0].userId").value("1"));
    }

    @Test
    public void trySearchDoctorSpec() throws Exception {
        String[] spec = {"spec"};

        when(institutionRepository.findDoctorsByCityAndSpec(".*.*", ".*", spec)).thenReturn(exampleStaffDigests);

        mvc.perform(post("/api/search/?type=doctor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result").isNotEmpty())
                .andExpect(jsonPath("$.result[0].userId").value("1"));
    }

    ArrayList<Institution> exampleInstitutions = new ArrayList<>(List.of(new Institution("1", true,
            new Address("a", "b", "c", "d", "e"), "")));


    @Test
    public void trySearchInstitutionSpec() throws Exception {
        String[] spec = {"spec"};

        when(institutionRepository.findInstitutionByCityAndSpec(".*.*", ".*", spec)).thenReturn(exampleInstitutions);

        mvc.perform(post("/api/search/?type=institution"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result").isNotEmpty())
                .andExpect(jsonPath("$.result[0].id").value("1"));
    }

    @Test
    public void trySearchInstitution() throws Exception {
        String[] spec = {"spec"};

        when(institutionRepository.findInstitutionByCity(".*.*", ".*")).thenReturn(exampleInstitutions);

        mvc.perform(post("/api/search/?type=institution"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result").isNotEmpty())
                .andExpect(jsonPath("$.result[0].id").value("1"));
    }

    Visit exampleVisit = new Visit(new PatientDigest("1", "Example", "Patient", "123456789"),
            new DoctorDigest("2", "Example", "Doctor", new ArrayList<>(List.of("spec"))),
            new VisitTime("1", LocalDateTime.of(2025, 1, 1, 12, 12, 12),
                    LocalDateTime.of(2025, 1, 1, 13, 13, 13)),
            new InstitutionDigest("1", "TestInstitution"), "");

    @Test
    public void tryAddVisitUnauthorizedIfNoSessionId() throws Exception {

        String exampleAdd = "{" +
                "\"scheduleID\": \"schedule123\"," +
                "\"patientRemarks\": \"remarks\"" +
                "}";


        mvc.perform(post("/api/visits/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleAdd))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRepository, scheduleRepository, visitRepository);
    }

    @Test
    public void addVisit_badRequestIfMissingScheduleID() throws Exception {
        String userId = "user1";

        MongoSession session = sessionRepository.createSession();
        // 2. Set the 'id' attribute on the real Spring Session object
        session.setAttribute("id", userId);
        // 3. Save the session to the repository (MongoDB)
        sessionRepository.save(session);

        MockHttpSession httpsession = new MockHttpSession(mvc.getDispatcherServlet().getServletContext(), session.getId());
        httpsession.setAttribute("id", userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(exampleValidUser));
        String exampleAdd = "{" +
                "\"patientRemarks\": \"remarks\"" +
                "}";

        mvc.perform(post("/api/visits/add")
                        .session(httpsession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleAdd))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("missing fields in request body"))
                .andExpect(jsonPath("$.fields[0]").value("scheduleID"));



        verifyNoInteractions(userRepository, scheduleRepository, visitRepository);
    }

    @Test
    public void addVisit_badRequestIfScheduleNotFound() throws Exception {
        String userId = "user1";
        String scheduleId = "nonExistentSchedule";
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("id", userId);


        when(userRepository.findById(userId)).thenReturn(Optional.of(exampleValidUser));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        String exampleAdd = "{" +
                "\"scheduleID\": \"schedule123\"," +
                "\"patientRemarks\": \"remarks\"" +
                "}";

        mvc.perform(post("/api/visits/add")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleAdd))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("visit time is invalid or booked"));

        verify(userRepository, times(1)).findById(userId);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verifyNoInteractions(visitRepository);
    }




    @Test
    public void addVisit_badRequestIfScheduleAlreadyBooked() throws Exception {
        String userId = "user1";
        String scheduleId = "schedule123";

        String exampleAdd = "{" +
                "\"scheduleID\": \"" + scheduleId +"\"," +
                "\"patientRemarks\": \"remarks\"" +
                "}";
        MockHttpSession session = new MockHttpSession();

        session.setAttribute("id", userId);

        Schedule exampleSchedule = new Schedule(LocalDateTime.of(2025, 1, 1, 12, 0, 0),
                LocalDateTime.of(2025, 1, 1, 13, 0, 0),
                new DoctorDigest("doctor1", "Example", "doctor", new ArrayList<>()),
                new InstitutionDigest("institution1", "institutionName"));

        exampleSchedule.setBooked(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(exampleValidUser));


        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(exampleSchedule));


        mvc.perform(post("/api/visits/add")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleAdd))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("visit time is invalid or booked"));

        verify(userRepository, times(1)).findById(userId);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verifyNoInteractions(visitRepository);
    }

    @Test
    public void addVisit_successWithNotificationEnglish() throws Exception {
        String userId = "user1";
        String scheduleId = "availableSchedule";


        String exampleAdd = "{" +
                "\"scheduleID\": \"" + scheduleId +"\"," +
                "\"patientRemarks\": \"remarks\"" +
                "}";
        MockHttpSession session = new MockHttpSession();

        session.setAttribute("id", userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(exampleValidUser));


        Schedule exampleSchedule = new Schedule(LocalDateTime.of(2025, 1, 1, 12, 0, 0),
                LocalDateTime.of(2025, 1, 1, 13, 0, 0),
                new DoctorDigest("doctor1", "Example", "doctor", new ArrayList<>()),
                new InstitutionDigest("institution1", "institutionName"));

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(exampleSchedule));

        when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mvc.perform(post("/api/visits/add")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleAdd))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("success"));

        verify(userRepository, times(1)).findById(userId);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(visitRepository, times(1)).save(any(Visit.class));
        verify(scheduleRepository, times(1)).save(argThat(Schedule::isBooked));
        verify(userRepository, times(1)).save(argThat(u -> !u.getNotifications().isEmpty()));

    }


    @Test
    public void addVisit_successWithEmptyPatientRemarks() throws Exception {
        String userId = "user1";
        String scheduleId = "availableSchedule";


        String exampleAdd = "{" +
                "\"scheduleID\": \"" + scheduleId +"\"" +
                "}";
        MockHttpSession session = new MockHttpSession();

        session.setAttribute("id", userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(exampleValidUser));


        Schedule exampleSchedule = new Schedule(LocalDateTime.of(2025, 1, 1, 12, 0, 0),
                LocalDateTime.of(2025, 1, 1, 13, 0, 0),
                new DoctorDigest("doctor1", "Example", "doctor", new ArrayList<>()),
                new InstitutionDigest("institution1", "institutionName"));

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(exampleSchedule));

        when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mvc.perform(post("/api/visits/add")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleAdd))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("success"));

        verify(userRepository, times(1)).findById(userId);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(visitRepository, times(1)).save(any(Visit.class));
        verify(scheduleRepository, times(1)).save(argThat(Schedule::isBooked));
        verify(userRepository, times(1)).save(argThat(u -> !u.getNotifications().isEmpty()));
    }


}
