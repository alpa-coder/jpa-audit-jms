package be.c4j.springsquad;

import be.c4j.springsquad.config.AppConfig;
import be.c4j.springsquad.domain.Employee;
import be.c4j.springsquad.infrastructure.EmployeeRepository;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

public class App {
    public static void main(String[] args) {
        ApplicationContext context = new SpringApplicationBuilder()
                .sources(AppConfig.class)
                .run(args);

        EmployeeRepository repository = context.getBean(EmployeeRepository.class);
        createUsers(repository);
        repository.findByName("Davy");
        repository.findByName("Jeroen");

    }

    private static void createUsers(EmployeeRepository repository) {
        repository.save(new Employee("Davy"));
        repository.save(new Employee("Jeroen"));
    }
}
