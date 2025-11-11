package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.ScheduleRepository;
import com.adam.medipathbackend.repository.UserRepository;
import com.adam.medipathbackend.services.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.*;

@RestController
@RequestMapping("/api")
public class SearchController {

    @Autowired
    SearchService searchService;

    @GetMapping(value = {"/search/{query}", "/search", "/search/"})
    public ResponseEntity<Map<String, Object>> search(@PathVariable(required = false) String query,
                                                      @RequestParam("type") String type,
                                                      @RequestParam(value = "city", defaultValue = ".*") String city,
                                                      @RequestParam(value = "specialisations", required = false)
                                                          String[] specialisations) {
        if(query == null || query.isBlank()) {
            query = ".*";
        }
        if(type.equals("institution")) {

            return new ResponseEntity<>(Map.of("result",
                    searchService.searchInstitutions(specialisations, city, query)),
                    HttpStatus.OK);

        } else if(type.equals("doctor")) {

            return new ResponseEntity<>(Map.of("result",
                    searchService.searchDoctors(specialisations, city, query)),
                    HttpStatus.OK);

        } else {
            return new ResponseEntity<>(Map.of("message", "unknown type"), HttpStatus.BAD_REQUEST);
        }

    }


}
