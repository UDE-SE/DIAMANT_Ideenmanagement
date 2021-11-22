package de.unidue.se.diamant.backend.backendservice.service.common.domain;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Person {

    //Vollständiger Name der Person
    @NotBlank
    private String name;

    //E-Mail-Adresse
    @NotBlank
    private String email;

    public String getEmail() {
        return email.toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        return getEmail() != null ? getEmail().equals(person.getEmail()) : person.getEmail() == null;
    }

    @Override
    public int hashCode() {
        return getEmail() != null ? getEmail().hashCode() : 0;
    }
}
