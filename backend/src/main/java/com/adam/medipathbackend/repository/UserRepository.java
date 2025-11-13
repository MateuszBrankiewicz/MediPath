package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.Notification;
import com.adam.medipathbackend.models.User;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

  @Query("{'email': ?0}")
  Optional<User> findByEmail(String email);

  @Query("{'govId': ?0}")
  Optional<User> findByGovID(String id);

  long count();

  @Aggregation({
      "{$project: { fullname: { $concat: [\"$name\", \" \", \"$surname\"]}, isActive: 1, roleCode: 1, employers: 1}}",
      "{ $match: { roleCode: { $in: [2, 3, 6, 7, 14, 15] }, isActive: true, fullname: { $regex: /?0/i }}}" })
  ArrayList<User> findDoctorsByName(String name);

  @Aggregation({ "{$project: { name: 1, surname: 1, specialisations: 1}}",
      "{$match: { specialisations: { $elemMatch: { $regex: /?0/i }}}}" })
  ArrayList<User> findDoctorsBySpec(String name);

  @Query("{_id:{ $oid: \"?0\" }, roleCode: { $in: [2, 3, 6, 7, 14, 15] } }")
  Optional<User> findDoctorById(String id);

  @Query("{_id:{ $oid: \"?0\" }, roleCode: { $in: [2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15] } }")
  Optional<User> findDoctorOrStaffById(String id);

  @Query("{_id:{ $oid: \"?0\" }, roleCode: { $in: [8, 9, 10, 11, 12, 13, 14, 15] } }")
  Optional<User> findAdminById(String id);

  @Aggregation({
      "{ '$unwind': '$notifications' }",
      "{ '$match': { 'notifications.timestamp': { $gte: ?0, $lt: ?1 } } }"
  })
  ArrayList<User> getUserNotificationsNow(LocalDateTime lower, LocalDateTime upper);

  @Query("{}")
  @Update("{'$pull': {'notifications': {'timestamp': { $lt: ?0 }}}}")
  void deleteOldNotifications(LocalDateTime date);

  @Query("{_id:{ $oid: \"?0\" }, roleCode: { $gt: 1 } }")
  Optional<User> findEmployeeById(String id);
}
