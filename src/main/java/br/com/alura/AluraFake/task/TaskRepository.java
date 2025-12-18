package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    boolean existsByCourseAndStatement(Course course, String statement);

    @Query("SELECT MAX(t.order) FROM Task t WHERE t.course = :course")
    Optional<Integer> findMaxOrderByCourse(@Param("course") Course course);

    @Query("SELECT t FROM Task t WHERE t.course = :course AND t.order >= :order ORDER BY t.order DESC")
    List<Task> findByCourseAndOrderGreaterThanEqualOrderByOrderDesc(
            @Param("course") Course course,
            @Param("order") Integer order
    );

}
