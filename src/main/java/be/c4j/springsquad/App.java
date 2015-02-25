package be.c4j.springsquad;

import be.c4j.springsquad.domain.Employee;
import be.c4j.springsquad.infrastructure.employee.EmployeeRepository;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.List;

public class App {

    private static List<String> names = Arrays.asList(
            "Davy Van Roy",
            "Stefanie Jacobs",
            "Amélie Van Roy",
            "Lucas Van Roy"
    );

    public static void main(String[] args) {
        ConfigurableApplicationContext context = createContext(args);
        auditUsers(context);
    }

    private static ConfigurableApplicationContext createContext(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder()
                .sources(AppConfig.class)
                .run(args);
        context.registerShutdownHook();
        return context;
    }

    private static void auditUsers(ConfigurableApplicationContext context) {
        EmployeeRepository repository = context.getBean(EmployeeRepository.class);
        createUsers(repository);
        readUsers(repository);
    }

    private static void createUsers(EmployeeRepository repository) {
        names.stream()
                .map(Employee::new)
                .forEach(repository::save);
    }

    private static void readUsers(EmployeeRepository repository) {
        names.stream()
                .forEach(repository::findByName);
    }
}
