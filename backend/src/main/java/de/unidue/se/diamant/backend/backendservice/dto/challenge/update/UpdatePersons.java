package de.unidue.se.diamant.backend.backendservice.dto.challenge.update;

import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.TeamMember;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UpdatePersons {

    @Valid
    @NotNull
    private Set<Person> persons;

    public Set<Person> getPersons() {
        if(persons == null){
            return new HashSet<Person>();
        }
        return persons;
    }
}
