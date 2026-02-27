package com.golfoutingapp.course;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public record CreateCourseRequest(@NotBlank String name, @NotBlank String timezone) {}

    @PostMapping
    public Course create(@RequestBody CreateCourseRequest req) {
        Course course = new Course();
        course.setName(req.name());
        course.setTimezone(req.timezone());
        return courseRepository.save(course);
    }
}
