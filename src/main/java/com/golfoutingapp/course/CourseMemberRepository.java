package com.golfoutingapp.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseMemberRepository extends JpaRepository<CourseMember, Long> {
    Optional<CourseMember> findByCourseIdAndUserId(Long courseId, Long userId);
}
