package com.adam.medipathbackend;

import com.adam.medipathbackend.controllers.CityController;
import com.adam.medipathbackend.models.City;
import com.adam.medipathbackend.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class MedipathbackendApplicationTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CityRepository cityRepository;



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



}
