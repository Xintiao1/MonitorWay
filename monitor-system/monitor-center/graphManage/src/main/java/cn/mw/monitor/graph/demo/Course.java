package cn.mw.monitor.graph.demo;

import cn.mw.monitor.graph.neo4j.Entity;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

@NodeEntity(label="Class")
class Course extends Entity {
    private String name;

    /*
    @Relationship(type= "SUBJECT_TAUGHT")
    Subject subject;

    @Relationship(type= "TEACHES_CLASS", direction=Relationship.INCOMING)
    Teacher teacher;
     */

    @Relationship(type= "ENROLLED", direction=Relationship.INCOMING)
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
