package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.ArrayList;

public interface CommentRepository extends MongoRepository<Comment, String> {

    @Query("{'author.userId': ?0}")
    ArrayList<Comment> getCommentsForUser(String userid);


}
