package be.c4j.springsquad.infrastructure.employee;

import be.c4j.springsquad.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Employee findByName(String name);
}
