package com.adam.medipathbackend.services;

import com.adam.medipathbackend.forms.AddCommentForm;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Stream;

@Service
public class CommentService {
    @Autowired
    VisitRepository visitRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    InstitutionRepository institutionRepository;

     
    public void addComment(AddCommentForm commentForm, String loggedUserID) throws IllegalArgumentException, IllegalAccessException, IllegalStateException {

        if (Stream.of(1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f)
                .noneMatch(validGrade -> validGrade == commentForm.getDoctorRating()))
            throw new IllegalArgumentException("Invalid doctor rating");

        if (Stream.of(1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f)
                .noneMatch(validGrade -> validGrade == commentForm.getInstitutionRating()))
            throw new IllegalArgumentException("Invalid institution rating");

        Optional<Visit> optVisit = visitRepository.findById(commentForm.getVisitID());
        if (optVisit.isEmpty()) throw new IllegalAccessException("Visit not found");

        Visit visit = optVisit.get();
        if (!visit.getPatient().getUserId().equals(loggedUserID)) throw new IllegalAccessException("User not authorized to comment on this visit");

        Comment newComment = new Comment(commentForm.getDoctorRating(), commentForm.getInstitutionRating(),
                commentForm.getComment() == null ? "" : commentForm.getComment(),
                visit.getDoctor(), visit.getInstitution(), visit.getPatient(), commentForm.getVisitID());

        Optional<Institution> institutionOpt = institutionRepository.findById(visit.getInstitution().getInstitutionId());
        Optional<User> doctorOpt = userRepository.findById(visit.getDoctor().getUserId());

        if (institutionOpt.isEmpty()) throw new IllegalArgumentException("Institution not found");
        if (doctorOpt.isEmpty()) throw new IllegalArgumentException("Doctor not found");

        User doctor = doctorOpt.get();
        Institution institution = institutionOpt.get();

        doctor.addRating(commentForm.getDoctorRating());
        institution.addRating(commentForm.getInstitutionRating());

        institutionRepository.save(institution);
        userRepository.save(doctor);
        commentRepository.save(newComment);
    }

     
    public void editComment(AddCommentForm commentForm, String commentid, String loggedUserID) throws IllegalArgumentException, IllegalAccessException, IllegalStateException {

        if (Stream.of(1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f)
                .noneMatch(validGrade -> validGrade == commentForm.getDoctorRating()))
            throw new IllegalArgumentException("Invalid doctor rating");

        if (Stream.of(1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f)
                .noneMatch(validGrade -> validGrade == commentForm.getInstitutionRating()))
            throw new IllegalArgumentException("Invalid institution rating");

        Optional<Comment> commentOpt = commentRepository.findById(commentid);
        if (commentOpt.isEmpty()) throw new IllegalAccessException("Comment not found");

        Comment comment = commentOpt.get();
        if (!comment.getAuthor().getUserId().equals(loggedUserID)) throw new IllegalAccessException("User not authorized to edit this comment");

        Optional<Institution> institutionOpt = institutionRepository.findById(comment.getInstitution().getInstitutionId());
        Optional<User> doctorOpt = userRepository.findById(comment.getDoctorDigest().getUserId());

        if (institutionOpt.isEmpty()) throw new IllegalArgumentException("Institution not found");
        if (doctorOpt.isEmpty()) throw new IllegalArgumentException("Doctor not found");

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
    }

     
    public void deleteComment(String commentid, String loggedUserID) throws IllegalArgumentException, IllegalAccessException, IllegalStateException {

        Optional<Comment> commentOpt = commentRepository.findById(commentid);
        if (commentOpt.isEmpty()) throw new IllegalAccessException("Comment not found");

        Comment comment = commentOpt.get();
        if (!comment.getAuthor().getUserId().equals(loggedUserID)) throw new IllegalAccessException("User not authorized to delete this comment");

        Optional<Institution> institutionOpt = institutionRepository.findById(comment.getInstitution().getInstitutionId());
        Optional<User> doctorOpt = userRepository.findById(comment.getDoctorDigest().getUserId());

        if (institutionOpt.isEmpty()) throw new IllegalArgumentException("Institution not found");
        if (doctorOpt.isEmpty()) throw new IllegalArgumentException("Doctor not found");

        User doctor = doctorOpt.get();
        Institution institution = institutionOpt.get();

        doctor.subtractRating(comment.getDoctorRating());
        institution.subtractRating(comment.getInstitutionRating());

        institutionRepository.save(institution);
        userRepository.save(doctor);
        commentRepository.delete(comment);
    }

     
    public Map<String, Object> getComment(String id, String loggedUserID) throws IllegalArgumentException, IllegalAccessException {

        Optional<Comment> optComment = commentRepository.findById(id);
        if (optComment.isEmpty()) throw new IllegalArgumentException("Invalid comment id");

        Comment comment = optComment.get();

        return Map.of("comment", Map.of(
                "id", comment.getId(),
                "author", comment.getAuthor().getName() + " " + comment.getAuthor().getSurname(),
                "doctor", comment.getDoctorDigest().getDoctorName() + " " + comment.getDoctorDigest().getDoctorSurname(),
                "institution", comment.getInstitution().getInstitutionName(),
                "doctorRating", comment.getDoctorRating(),
                "institutionRating", comment.getInstitutionRating(),
                "content", comment.getContent(),
                "createdAt", comment.getCreatedAt().toString()
        ));
    }

     
    public Map<String, Object> getCommentsForDoctor(String id) {

        if (!userRepository.existsById(id)) {
            return Map.of("comments", new ArrayList<Comment>());
        }

        return Map.of("comments", commentRepository.getCommentsForDoctor(id).stream().map(comment -> Map.of(
                "id", comment.getId(),
                "author", comment.getAuthor().getName() + " " + comment.getAuthor().getSurname(),
                "doctor", comment.getDoctorDigest().getDoctorName() + " " + comment.getDoctorDigest().getDoctorSurname(),
                "institution", comment.getInstitution().getInstitutionName(),
                "doctorRating", comment.getDoctorRating(),
                "institutionRating", comment.getInstitutionRating(),
                "content", comment.getContent(),
                "createdAt", comment.getCreatedAt().toString()
        )));
    }

     
    public Map<String, Object> getCommentsForInstitution(String id) {

        if (!institutionRepository.existsById(id)) {
            return Map.of("comments", new ArrayList<Comment>());
        }

        return Map.of("comments", commentRepository.getCommentsForInstitution(id).stream().map(comment -> Map.of(
                "id", comment.getId(),
                "author", comment.getAuthor().getName() + " " + comment.getAuthor().getSurname(),
                "doctor", comment.getDoctorDigest().getDoctorName() + " " + comment.getDoctorDigest().getDoctorSurname(),
                "institution", comment.getInstitution().getInstitutionName(),
                "doctorRating", comment.getDoctorRating(),
                "institutionRating", comment.getInstitutionRating(),
                "content", comment.getContent(),
                "createdAt", comment.getCreatedAt().toString()
        )));
    }
}
