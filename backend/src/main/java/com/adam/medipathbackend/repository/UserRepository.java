package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.User;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    @Query("{_id: {$oid: ?0}, isActive: true}")
    Optional<User> findActiveById(String id);

    @Query("{'email': ?0, isActive: true}")
    Optional<User> findByEmail(String email);

    @Query("{'govId': ?0, isActive: true}")
    Optional<User> findByGovID(String id);

  long count();

    @Query("{_id:{ $oid: \"?0\" }, roleCode: { $in: [2, 3, 6, 7, 14, 15] }, isActive: true }")
    Optional<User> findDoctorById(String id);

    @Query("{_id:{ $oid: \"?0\" }, roleCode: { $in: [2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15] }, isActive: true }")
    Optional<User> findDoctorOrStaffById(String id);

    @Query("{_id:{ $oid: \"?0\" }, roleCode: { $in: [8, 9, 10, 11, 12, 13, 14, 15] }, isActive: true }")
    Optional<User> findAdminById(String id);

    @Aggregation({
            "{ '$match': { 'notifications.timestamp': { $gte: ?0, $lt: ?1 }, isActive: true } }",
            "{ '$unwind': '$notifications' }",
            "{ '$match': { 'notifications.timestamp': { $gte: ?0, $lt: ?1 } } }",
            "{ '$group': { '_id': '$_id', 'notifications': { $push: '$notifications' }, 'email': { $first: '$email' }, 'firstName': { $first: '$firstName' }, 'lastName': { $first: '$lastName' }, 'roleCode': { $first: '$roleCode' }, 'isActive': { $first: '$isActive' } } }"
    })
    ArrayList<User> getUserNotificationsNow(LocalDateTime lower, LocalDateTime upper);

  @Query("{}")
  @Update("{'$pull': {'notifications': {'timestamp': { $lt: ?0 }}}}")
  void deleteOldNotifications(LocalDateTime date);

  @Query("{_id:{ $oid: \"?0\" }, roleCode: { $gt: 1 } }")
  Optional<User> findEmployeeById(String id);
}
