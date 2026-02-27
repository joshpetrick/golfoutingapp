package com.golfoutingapp.course;

import com.golfoutingapp.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CourseAccessService {
    private final CourseMemberRepository courseMemberRepository;

    public CourseAccessService(CourseMemberRepository courseMemberRepository) {
        this.courseMemberRepository = courseMemberRepository;
    }

    public CourseMember requireMembership(Long courseId, Long userId) {
        return courseMemberRepository.findByCourseIdAndUserId(courseId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "No access to course"));
    }

    public void requireAdmin(Long courseId, Long userId) {
        CourseMember member = requireMembership(courseId, userId);
        if (member.getRole() != CourseRole.COURSE_ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required");
        }
    }
}
