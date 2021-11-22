package de.unidue.se.diamant.backend.backendservice.dto.idea;

public enum IdeaPermissions {
    /**
     * Darf die Inhalte der Idee ändern. Exclusive der Zustandsänderung auf den Status READY_FOR_VOTE!
     */
    EDIT,
    /**
     * Darf den Zustand der Idee in den Status READY_FOR_VOTE setzen
     */
    CHANGE_STATE_TO_READY_FOR_VOTE,

    /**
     * Darf über die Idee abstimmen
     */
    VOTE,

    /**
     * Darf die Idee begutachten
     */
    REFEREE,

    /**
     * Darf die Idee als Gewinner nominieren
     */
    NOMINATE_AS_WINNER
}
