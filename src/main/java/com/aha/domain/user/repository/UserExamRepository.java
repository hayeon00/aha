package com.aha.domain.user.repository;

import com.aha.domain.user.entity.User;
import com.aha.domain.user.entity.UserExam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserExamRepository extends JpaRepository<UserExam, Long> {

    List<UserExam> findByUser(User user);

    Optional<UserExam> findByUserAndIsMainTrue(User user);

    boolean existsByUserAndExam_Id(User user, Long examId);

    void deleteByUser(User user);
}