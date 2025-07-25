package com.adam.medipathbackend;

import com.adam.medipathbackend.controllers.CityController;
import com.adam.medipathbackend.models.City;
import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.repository.CityRepository;
import com.adam.medipathbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class MedipathbackendApplicationTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CityRepository cityRepository;

    @MockitoBean
    private UserRepository userRepository;

    private List<City> cityList;

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

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());

        String exampleUser = "{" +
                "\"name\": \"TestName\"," +
                "\"surname\": \"TestSurname\"," +
                "\"email\": \"test@mail.com\"," +
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

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(new User("test@mail.com", "A", "B", "1234567890", LocalDate.of(1900, 12, 12), "A", "A", "A", "A", "A", "A", "A")));
        String exampleUser = "{" +
                "\"name\": \"TestName\"," +
                "\"surname\": \"TestSurname\"," +
                "\"email\": \"test@mail.com\"," +
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

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());
        String exampleUser = "{" +
                "\"name\": \"TestName\"," +
                "\"email\": \"test@mail.com\"," +
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

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());
        String exampleUser = "{}";

        mvc.perform(post("/api/users/register").contentType("application/json").content(exampleUser))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("missing fields in request body"))
                .andExpect(jsonPath("$.fields[0]").value("name"))
                .andExpect(jsonPath("$.fields[1]").value("surname"))
                .andExpect(jsonPath("$.fields[2]").value("email"))
                .andExpect(jsonPath("$.fields[3]").value("city"))
                .andExpect(jsonPath("$.fields[4]").value("province"))
                .andExpect(jsonPath("$.fields[5]").value("street"))
                .andExpect(jsonPath("$.fields[6]").value("number"))
                .andExpect(jsonPath("$.fields[7]").value("postalCode"))
                .andExpect(jsonPath("$.fields[8]").value("birthDate"))
                .andExpect(jsonPath("$.fields[9]").value("govID"))
                .andExpect(jsonPath("$.fields[10]").value("phoneNumber"))
                .andExpect(jsonPath("$.fields[11]").value("password"));
    }
    @Test
    public void tryRegisterUserWithDuplicateGovID() throws Exception {

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());
        when(userRepository.findByGovID("1234567890")).thenReturn(Optional.of(new User("test@mail.com", "A", "B", "1234567890", LocalDate.of(1900, 12, 12), "A", "A", "A", "A", "A", "A", "A")));

        String exampleUser = "{" +
                "\"name\": \"TestName\"," +
                "\"surname\": \"TestSurname\"," +
                "\"email\": \"test@mail.com\"," +
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
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(new User("test@mail.com", "A", "B", "1234567890", LocalDate.of(1900, 12, 12), "A", "A", "A", "A", "A", "A", "$argon2id$v=19$m=60000,t=10,p=1$MOrLn9JdvWpJyJDMZ4Z9qg$zgHTASZaQH9zoTUO0bd0re+6G523ZUreKFmWQSu+f24")));

        String exampleLogin = "{" +
                "\"email\": \"test@mail.com\"," +
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

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(new User("test@mail.com", "A", "B", "1234567890", LocalDate.of(1900, 12, 12), "A", "A", "A", "A", "A", "A", "$argon2id$v=19$m=60000,t=10,p=1$MOrLn9JdvWpJyJDMZ4Z9qg$zgHTASZaQH9zoTUO0bd0re+6G523ZUreKFmWQSu+f24")));

        String exampleLogin = "{" +
                "\"email\": \"test@mail.com\"," +
                "\"password\": \"invalid\"" +
                "}";

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(exampleLogin))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid email or password"));
    }
    @Test
    public void tryLogInNoPassword() throws Exception {

        String exampleLogin = "{" +
                "\"email\": \"test@mail.com\"" +
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



}
