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
    public void tryRegisterUserMissingStreet() throws Exception {

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
                "\"number\": \"5a\"," +
                "\"password\": \"passwordpassword\"" +
                "}";

        mvc.perform(post("/api/users/register").contentType("application/json").content(exampleUser))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid fields in request body"))
                .andExpect(jsonPath("$.fields[0]").value("Street"));
    }
    @Test
    public void tryRegisterUserMissingData() throws Exception {

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());
        String exampleUser = "{}";

        mvc.perform(post("/api/users/register").contentType("application/json").content(exampleUser))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid fields in request body"))
                .andExpect(jsonPath("$.fields[0]").value("Name"))
                .andExpect(jsonPath("$.fields[1]").value("Surname"))
                .andExpect(jsonPath("$.fields[2]").value("Email"))
                .andExpect(jsonPath("$.fields[3]").value("City"))
                .andExpect(jsonPath("$.fields[4]").value("Province"))
                .andExpect(jsonPath("$.fields[5]").value("Street"))
                .andExpect(jsonPath("$.fields[6]").value("Number"))
                .andExpect(jsonPath("$.fields[7]").value("PostalCode"))
                .andExpect(jsonPath("$.fields[8]").value("BirthDate"))
                .andExpect(jsonPath("$.fields[9]").value("GovID"))
                .andExpect(jsonPath("$.fields[10]").value("Phone"))
                .andExpect(jsonPath("$.fields[11]").value("Password"));
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



}
