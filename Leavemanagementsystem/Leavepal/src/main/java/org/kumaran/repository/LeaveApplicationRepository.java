package org.kumaran.repository;

import java.util.Collection;
import java.util.List;

import org.kumaran.entity.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {
    List<LeaveApplication> findByEmployeeIdOrUsernameOrEmailIdOrderByCreatedAtDesc(String employeeId, String username,
            String emailId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("delete from LeaveApplication la where la.username = :username or la.emailId = :username or la.employeeId = :employeeId")
    void deleteAllByUsernameOrEmployeeId(@org.springframework.data.repository.query.Param("username") String username,
            @org.springframework.data.repository.query.Param("employeeId") String employeeId);

    @Query("""
            select la
            from LeaveApplication la
            where lower(la.status) = lower(:status)
              and (
                la.employeeId = :employeeId
             or la.username = :username
             or la.emailId = :emailId
              )
            order by la.createdAt asc
            """)
    List<LeaveApplication> findByIdentityAndStatusOrderByCreatedAtAsc(@Param("employeeId") String employeeId,
            @Param("username") String username,
            @Param("emailId") String emailId,
            @Param("status") String status);

    @Query("""
            select la
            from LeaveApplication la
            where (
                la.employeeId in :employeeIds
             or la.username in :usernames
             or la.emailId in :emailIds
             or la.reportingManagerId in :reportingManagerIds
             or la.reportingManagerUsername in :reportingManagerUsernames
             or la.reportingManagerEmail in :reportingManagerEmails
             or la.reportingManagerName in :reportingManagerNames
            )
            order by la.createdAt desc
            """)
    List<LeaveApplication> findManagerQueueCandidates(@Param("employeeIds") Collection<String> employeeIds,
            @Param("usernames") Collection<String> usernames,
            @Param("emailIds") Collection<String> emailIds,
            @Param("reportingManagerIds") Collection<String> reportingManagerIds,
            @Param("reportingManagerUsernames") Collection<String> reportingManagerUsernames,
            @Param("reportingManagerEmails") Collection<String> reportingManagerEmails,
            @Param("reportingManagerNames") Collection<String> reportingManagerNames);
}
