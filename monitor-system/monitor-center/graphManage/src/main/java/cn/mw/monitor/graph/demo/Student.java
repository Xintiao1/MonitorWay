package cn.mw.monitor.graph.demo;

import cn.mw.monitor.graph.neo4j.Entity;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

@NodeEntity(label="Student")
class Student extends Entity {
    private String name;

    @Relationship(type = "ENROLLED")
    private Set<Enrollment> enrollments;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(Set<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }
}
