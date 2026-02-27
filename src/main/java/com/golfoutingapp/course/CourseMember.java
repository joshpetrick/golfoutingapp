package com.golfoutingapp.course;

import com.golfoutingapp.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "course_members")
public class CourseMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private Course course;
    @ManyToOne(optional = false) private User user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseRole role;

    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public CourseRole getRole() { return role; }
    public void setRole(CourseRole role) { this.role = role; }
}
