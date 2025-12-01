package com.ninjaone.dundie_awards;

import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.model.Role;
import com.ninjaone.dundie_awards.model.User;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import com.ninjaone.dundie_awards.repository.UserRepository;

import lombok.NonNull;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@SuppressWarnings("null")
public class DataLoader implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(EmployeeRepository employeeRepository, 
                     OrganizationRepository organizationRepository,
                     UserRepository userRepository,
                     PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // uncomment to reseed data
        // employeeRepository.deleteAll();
        // organizationRepository.deleteAll();

        // Create admin user if it doesn't exist
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                    .enabled(true)
                    .build();
            userRepository.save(adminUser);
        }

        if (employeeRepository.count() == 0) {
            @NonNull Organization organizationPikashu = organizationRepository.save(Organization.builder()
                    .name("Pikashu")
                    .build());

            employeeRepository.save(Employee.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .organization(organizationPikashu)
                    .dundieAwards(0)
                    .build());
            employeeRepository.save(Employee.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .organization(organizationPikashu)
                    .dundieAwards(0)
                    .build());
            employeeRepository.save(Employee.builder()
                    .firstName("Creed")
                    .lastName("Braton")
                    .organization(organizationPikashu)
                    .dundieAwards(0)
                    .build());

            @NonNull Organization organizationSquanchy = organizationRepository.save(Organization.builder()
                    .name("Squanchy")
                    .build());

            employeeRepository.save(Employee.builder()
                    .firstName("Michael")
                    .lastName("Scott")
                    .organization(organizationSquanchy)
                    .dundieAwards(0)
                    .build());
            employeeRepository.save(Employee.builder()
                    .firstName("Dwight")
                    .lastName("Schrute")
                    .organization(organizationSquanchy)
                    .dundieAwards(0)
                    .build());
            employeeRepository.save(Employee.builder()
                    .firstName("Jim")
                    .lastName("Halpert")
                    .organization(organizationSquanchy)
                    .dundieAwards(0)
                    .build());
            employeeRepository.save(Employee.builder()
                    .firstName("Pam")
                    .lastName("Beesley")
                    .organization(organizationSquanchy)
                    .dundieAwards(0)
                    .build());
        }
    }
}
