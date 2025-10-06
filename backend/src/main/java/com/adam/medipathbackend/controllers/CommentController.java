package com.adam.medipathbackend.controllers;


import com.adam.medipathbackend.forms.AddCommentForm;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.CommentRepository;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.UserRepository;
import com.adam.medipathbackend.repository.VisitRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    VisitRepository visitRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    InstitutionRepository institutionRepository;

    @PostMapping(value = {"/add", "/add/"})
    public ResponseEntity<Map<String, Object>> add(@RequestBody AddCommentForm commentForm, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if(Stream.of(1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f).noneMatch(validGrade -> validGrade == commentForm.getDoctorRating())) {
            return new ResponseEntity<>(Map.of("message", "invalid doctor rating"), HttpStatus.BAD_REQUEST);
        }
        if(Stream.of(1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f).noneMatch(validGrade -> validGrade == commentForm.getInstitutionRating())) {
            return new ResponseEntity<>(Map.of("message", "invalid institution rating"), HttpStatus.BAD_REQUEST);
        }
        Optional<Visit> optVisit = visitRepository.findById(commentForm.getVisitID());
        if(optVisit.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Visit visit = optVisit.get();
        if(!(visit.getPatient().getUserId().equals(loggedUserID))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Comment newComment = new Comment(commentForm.getDoctorRating(),
                commentForm.getInstitutionRating(),
                commentForm.getComment() == null ? "": commentForm.getComment(),
                visit.getDoctor(),
                visit.getInstitution(),
                visit.getPatient(),
                commentForm.getVisitID());
        Optional<Institution> institutionOpt = institutionRepository.findById(visit.getInstitution().getInstitutionId());
        Optional<User> doctorOpt = userRepository.findById(visit.getDoctor().getUserId());

        if(institutionOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(doctorOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        User doctor = doctorOpt.get();
        Institution institution = institutionOpt.get();

        doctor.addRating(commentForm.getDoctorRating());
        institution.addRating(commentForm.getInstitutionRating());
        institutionRepository.save(institution);
        userRepository.save(doctor);
        commentRepository.save(newComment);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping(value = {"/{commentid}", "/{commentid}/"})
    public ResponseEntity<Map<String, Object>> editComment(@RequestBody AddCommentForm commentForm, @PathVariable String commentid,  HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if(Stream.of(1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f).noneMatch(validGrade -> validGrade == commentForm.getDoctorRating())) {
            return new ResponseEntity<>(Map.of("message", "invalid doctor rating"), HttpStatus.BAD_REQUEST);
        }
        if(Stream.of(1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f).noneMatch(validGrade -> validGrade == commentForm.getInstitutionRating())) {
            return new ResponseEntity<>(Map.of("message", "invalid institution rating"), HttpStatus.BAD_REQUEST);
        }
        Optional<Comment> commentOpt = commentRepository.findById(commentid);
        if(commentOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Comment comment = commentOpt.get();
        if(!comment.getAuthor().getUserId().equals(loggedUserID)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<Institution> institutionOpt = institutionRepository.findById(comment.getInstitution().getInstitutionId());
        Optional<User> doctorOpt = userRepository.findById(comment.getDoctorDigest().getUserId());

        if(institutionOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(doctorOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        User doctor = doctorOpt.get();
        Institution institution = institutionOpt.get();

        doctor.editRating(commentForm.getDoctorRating(), comment.getDoctorRating());
        institution.editRating(commentForm.getInstitutionRating(), comment.getInstitutionRating());

        comment.setContent(commentForm.getComment());
        comment.setDoctorRating(commentForm.getDoctorRating());
        comment.setInstitutionRating(commentForm.getInstitutionRating());

        institutionRepository.save(institution);
        userRepository.save(doctor);
        commentRepository.save(comment);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = {"/{commentid}", "/{commentid}/"})
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable String commentid,  HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<Comment> commentOpt = commentRepository.findById(commentid);
        if(commentOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Comment comment = commentOpt.get();
        if(!comment.getAuthor().getUserId().equals(loggedUserID)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<Institution> institutionOpt = institutionRepository.findById(comment.getInstitution().getInstitutionId());
        Optional<User> doctorOpt = userRepository.findById(comment.getDoctorDigest().getUserId());

        if(institutionOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(doctorOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        User doctor = doctorOpt.get();
        Institution institution = institutionOpt.get();

        doctor.subtractRating(comment.getDoctorRating());
        institution.subtractRating(comment.getInstitutionRating());


        institutionRepository.save(institution);
        userRepository.save(doctor);
        commentRepository.delete(comment);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = {"/{id}", "/{id}/"})
    public ResponseEntity<Map<String, Object>> add(@PathVariable String id, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<Comment> optComment = commentRepository.findById(id);
        if(optComment.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "invalid comment id"), HttpStatus.BAD_REQUEST);
        }
        Comment comment = optComment.get();
        return new ResponseEntity<>(Map.of("comment", Map.of(
                "id", comment.getId(),
                "author", comment.getAuthor().getName() + " " + comment.getAuthor().getSurname(),
                "doctor", comment.getDoctorDigest().getDoctorName() + " " + comment.getDoctorDigest().getDoctorSurname(),
                "institution", comment.getInstitution().getInstitutionName(),
                "doctorRating", comment.getDoctorRating(),
                "institutionRating", comment.getInstitutionRating(),
                "content", comment.getContent())),
                HttpStatus.OK);
    }

    @GetMapping(value = {"/doctor/{id}", "/doctor/{id}/"})
    public ResponseEntity<Map<String, Object>> getCommentsForDoctor(@PathVariable String id) {
        if(!userRepository.existsById(id)) {
            return new ResponseEntity<>(Map.of("comments", new ArrayList<Comment>()), HttpStatus.OK);
        }
        return new ResponseEntity<>(Map.of("comments", commentRepository.getCommentsForDoctor(id)), HttpStatus.OK);
    }
    @GetMapping(value = {"/institution/{id}", "/institution/{id}/"})
    public ResponseEntity<Map<String, Object>> getCommentsForInstitution(@PathVariable String id) {
        if(!institutionRepository.existsById(id)) {
            return new ResponseEntity<>(Map.of("comments", new ArrayList<Comment>()), HttpStatus.OK);
        }
        return new ResponseEntity<>(Map.of("comments", commentRepository.getCommentsForInstitution(id)), HttpStatus.OK);
    }

}

