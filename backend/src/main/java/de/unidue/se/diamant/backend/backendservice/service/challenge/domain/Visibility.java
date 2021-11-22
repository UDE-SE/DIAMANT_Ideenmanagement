package de.unidue.se.diamant.backend.backendservice.service.challenge.domain;

public enum Visibility {
    /**
     * Nur Mitglieder des eigenen Unternehmens können an der Challenge teilnehmen.
     */
    INTERNAL,

    /**
     * Die Challenge ist öffentlicht. Auch unternehmens-externe können teilnehmen.
     */
    OPEN,

    /**
     * Nur eingeladene Nutzer können teilnehmen.
     */
    INVITE
}
