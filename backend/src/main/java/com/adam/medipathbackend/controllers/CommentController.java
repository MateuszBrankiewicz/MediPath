package com.adam.medipathbackend.controllers;


import com.adam.medipathbackend.forms.AddCommentForm;
import com.adam.medipathbackend.services.CommentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    CommentService commentService;

    @PostMapping(value = {"/add", "/add/"})
    public ResponseEntity<Map<String, Object>> add(@RequestBody AddCommentForm commentForm, 
                                                    HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            commentService.addComment(commentForm, loggedUserID);
            return new ResponseEntity<>(Map.of("message", "Comment added successfully"), 
                                        HttpStatus.CREATED);
        
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.CONFLICT);
        }
    }

    @PutMapping(value = {"/{commentid}", "/{commentid}/"})
    public ResponseEntity<Map<String, Object>> editComment(@RequestBody AddCommentForm commentForm, @PathVariable String commentid, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            commentService.editComment(commentForm, commentid, loggedUserID);
            return new ResponseEntity<>(Map.of("message", "Comment edited successfully"), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping(value = {"/{commentid}", "/{commentid}/"})
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable String commentid, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            commentService.deleteComment(commentid, loggedUserID);
            return new ResponseEntity<>(Map.of("message", "Comment deleted successfully"), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.CONFLICT);
        }
    }

    @GetMapping(value = {"/{id}", "/{id}/"})
    public ResponseEntity<Map<String, Object>> getComment(@PathVariable String id, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Map<String, Object> result = commentService.getComment(id, loggedUserID);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping(value = {"/doctor/{id}", "/doctor/{id}/"})
    public ResponseEntity<Map<String, Object>> getCommentsForDoctor(@PathVariable String id) {

        Map<String, Object> result = commentService.getCommentsForDoctor(id);
        return new ResponseEntity<>(result, HttpStatus.OK);

    }
    @GetMapping(value = {"/institution/{id}", "/institution/{id}/"})
    public ResponseEntity<Map<String, Object>> getCommentsForInstitution(@PathVariable String id) {

        Map<String, Object> result = commentService.getCommentsForInstitution(id);
        return new ResponseEntity<>(result, HttpStatus.OK);

    }

}

