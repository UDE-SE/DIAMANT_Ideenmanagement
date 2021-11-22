package de.unidue.se.diamant.backend.backendservice.service.idea.domain;

public enum IdeaState {
    /**
     * Idee ist in der Erstell-Phase
     */
    DRAFT,
    /**
     * Idee wurde für das Review durch die Gutachter freigegeben
     */
    SUBMITTED,
    /**
     * Idee wurde durch die Gutachter für das Voting freigegeben
     */
    READY_FOR_VOTE

}
