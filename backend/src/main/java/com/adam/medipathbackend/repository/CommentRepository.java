package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.ArrayList;

public interface CommentRepository extends MongoRepository<Comment, String> {

    @Query("{'author.userId': ?0}")
    ArrayList<Comment> getCommentsForUser(String userid);

    @Query("{'doctorDigest.userId': ?0}")
    ArrayList<Comment> getCommentsForDoctor(String doctorid);

    @Query("{'institution.institutionId': ?0}")
    ArrayList<Comment> getCommentsForInstitution(String institutionid);
}
