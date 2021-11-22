package de.unidue.se.diamant.backend.backendservice.service.idea.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import lombok.*;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TeamMember extends Person {
    public static final String INCOGNITO_USER_NAME = "Unsichtbarer Nutzer";

    private Boolean incognito;

    public TeamMember(@NotBlank String name, @NotBlank String email, Boolean incognito) {
        super(name, email);
        this.incognito = incognito;
    }

    @JsonIgnore
    public boolean isIncognitoNullSafe(){
        return incognito == null ? false : incognito;
    }

    public static TeamMember fromPerson(Person person){
        return fromPerson(person, false);
    }

    public static TeamMember fromPerson(Person person, boolean incognito){
        return new TeamMember(person.getName(), person.getEmail(), incognito);
    }
}
