package com.ninjaone.dundie_awards;

import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;

import lombok.NonNull;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;

    public DataLoader(EmployeeRepository employeeRepository, OrganizationRepository organizationRepository) {
        this.employeeRepository = employeeRepository;
        this.organizationRepository = organizationRepository;
    }

    @Override
    public void run(String... args) {
        // uncomment to reseed data
        // employeeRepository.deleteAll();
        // organizationRepository.deleteAll();

        if (employeeRepository.count() == 0) {
            @NonNull Organization organizationPikashu = Organization.builder()
                    .name("Pikashu")
                    .build();
            organizationRepository.save(organizationPikashu);

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

            Organization organizationSquanchy = Organization.builder()
                    .name("Squanchy")
                    .build();
            organizationRepository.save(organizationSquanchy);

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
