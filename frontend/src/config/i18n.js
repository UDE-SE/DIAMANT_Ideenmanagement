import Vue from 'vue'
import VueI18n from "vue-i18n";

Vue.use(VueI18n);

const messages = {
    de: {
        common: {
            navigation: {
                dashboard: 'Dashboard',
                challenge: 'Challenge',
                idea: 'Lösungsvorschlag'
            },
            challenge_information: {
                title: 'Titel',
                description: "Beschreibung",
                attachments: {
                    title: "Anhänge",
                    filename: "Dateiname",
                    download: "Runterladen",
                    delete: "Löschen",
                    add: "Anhang hinzufügen"
                },
                short_description: 'Kurzbeschreibung',
                awards: "Preise",
                news: "News",
                visibility: {
                    label: 'Sichtbarkeit',
                    values: {
                        new: {
                            label: 'Neu',
                            description: 'Neue Challenge erstellen'
                        },
                        open: {
                            label: 'Öffentlich',
                            description: 'Auch für Externe sichtbar',
                        },
                        internal: {
                            label: 'Nur für Mitarbeiter',
                            description: 'Nur für Mitarbeiter des eigenen Unternehmens sichtbar',
                        },
                        invite: {
                            label: 'Nur für eingeladene Teilnehmer',
                            description: 'Nur für eingeladene Teilnehmer sichtbar',
                        }
                    }
                },
                milestones: {
                    label: 'Meilensteine',
                    values: {
                        submission: {
                            label: 'Einreichungsphase',
                            description: 'Die Challenge wurde erstellt. Ideen können eingereicht werden.'
                        },
                        review: {
                            label: 'Begutachtungsphase',
                            description: 'Die Einreichungsfrist ist abgelaufen. Eingereichte Ideen werden begutachtet.'
                        },
                        refactoring: {
                            label: 'Überarbeitungsphase',
                            description: 'Ideen wurden begutachtet und Hinweise für die Überarbeitung hinzugefügt.'
                        },
                        voting: {
                            label: 'Abstimmphase',
                            description: 'Ideen können nicht mehr bearbeitet werden. Über die nominierten Ideen kann abgestimmt werden.'
                        },
                        implementation: {
                            label: 'Siegerehrung',
                            description: 'Der Sieger der Challenge wird festgelegt und bekannt gegeben.'
                        },
                        end: {
                            label: 'Ende der Challenge',
                            description: 'Die Challenge wurde beendet. Die Umsetzungsphase im Unternehmen ist abgelaufen.'
                        }
                    }
                },
            },
            information_not_available: "{field} nicht verfügbar",
            yes: 'Ja',
            no: 'Nein',
            save: 'Speichern',
            cancel: 'Abbrechen',
            add: 'Hinzufügen',
            invite: 'Einladen',
            please_choose: 'Bitte wählen...',
            required: 'Pflichtfeld',
            edit: 'Bearbeiten',
            close: 'Schließen',
            invitation: {
                team: {
                    title: 'In das Team einladen',
                    not_yet_possible: 'Andere Personen können Ihrem Team erst nach dem Speichern der Idee beitreten. Anschließend steht an dieser Stelle ein Link, den Sie an andere Personen weiterleiten und mit dem diese Ihrem Team beitreten können.',
                    link_explanation: 'Mit dem folgenden Link können andere Personen Ihrem Team beitreten. Senden Sie den Link einfach an die Personen, mit denen Sie zusammenarbeiten möchten (z.B. per Mail).'
                },
                referees: {
                    title: 'Als Gutachter einladen',
                    not_yet_possible: 'Andere Personen können dem Gutachtergremium erst nach dem Speichern der Challenge beitreten. Anschließend steht an dieser Stelle ein Link, den Sie an andere Personen weiterleiten und mit dem diese Gutachter der Challenge werden können.',
                    link_explanation: 'Mit dem folgenden Link können andere Personen dem Gutachtergremium der Challenge beitreten. Senden Sie den Link einfach an die Personen, mit denen Sie zusammenarbeiten möchten (z.B. per Mail).'
                },
                invited_users: {
                    title: 'Als Teilnehmer einladen',
                    not_yet_possible: 'Andere Personen können dem Teilnehmerkreis erst nach dem Speichern der Challenge beitreten. Anschließend steht an dieser Stelle ein Link, den Sie an andere Personen weiterleiten und mit dem diese Teilnehmer der Challenge werden können.',
                    link_explanation: 'Mit dem folgenden Link können andere Personen dem Teilnehmerkreis der Challenge beitreten. Senden Sie den Link einfach an die Personen, mit denen Sie zusammenarbeiten möchten (z.B. per Mail).'
                },
                invitation_link: 'Einladungslink: ',
                copy_to_clipboard: 'In die Zwischenablage speichern',
                link_expiry_date: 'Der Link ist gültig bis: {expiryDate}.',
                visibility_warning: {
                    internal: 'Bitte beachten Sie, dass nur Mitglieder Ihres Unternehmens den Einladungslink akzeptieren können!',
                    invite: 'Bitte beachten Sie, dass nur Personen, die auch von dem Ersteller der Challenge eingeladen wurden, den Einladungslink akzeptieren können!'
                }
            },
            persons: {
                displaypattern: "{name} <<{mail}>>",
                title: 'Nutzer suchen',
                invite: 'Einladen',
                invite_and_add: 'Einladen und hinzufügen',
                search: 'Suchen',
                no_results: 'Keine Suchergebnisse vorhanden...',
                search_failed: 'Es gab einen Fehler bei der Suche :(',
                invite_failed: 'Bein Einladen der Person ist ein Fehler aufgetreten :(',
                already_added: 'Die Person wurde bereits hinzugefügt',
                not_possible: 'Hinzufügen ist Aufgrund der Sichtbarkeit der Challenge nicht möglich',
                first_name: 'Vorname',
                last_name: 'Nachname',
                email: 'Email-Adresse',
                successful_invited: '{name} wurde erfolgreich eingeladen'
            },
            errors: {
                title: 'Fehler..',
                not_blank: 'Das Feld darf nicht leer sein!',
                loading_failed: 'Es gab einen Fehler beim Laden der Daten',
                attachment_loading_failed: 'Es gab einen Fehler beim Laden des Anhangs bzw. Bildes',
                file_too_big: 'Die Datei konnte nicht hinzugefügt werden, weil größer als die maximale Größe von {size} MB ist.',
                updated_failed: 'Das Speichern der Änderungen ist fehlgeschlagen.',
                send_message_failed: 'Verschicken der Nachricht fehlgeschlagen',
                forbidden: 'Sie haben nicht die notwendigen Rechte, um diese Seite anzuzeigen.',
                not_found: 'Seite nicht gefunden :(',
                invalid_date: 'Das eingegebene Datum ist nicht gültig',
                invitation_accept_failed: 'Das Annehmen der Einladung ist leider fehlgeschlagen :('
            },
            notification: {
                success: {
                    title: 'Erfolgreich',
                    saved: 'Erfolgreich gespeichert'
                }
            },
            cancel_changes: 'Wollen sie die Änderungen wirklich verwerfen?',
            idea: {
                permissions: {
                    values: {
                        edit: {
                            description: "Bearbeitung"
                        },
                        referee: {
                            description: "Begutachtung"
                        },
                        change_state: {
                            description: "Nominierung für die öffentliche Bewertung"
                        },
                        nominate_as_winner: {
                            description: "Markierung als Sieger"
                        },
                        vote: {
                            description: "Bewertung der Idee"
                        }
                    }
                },
                voting: {
                    title: 'Bewertung',
                    own: 'Eigene Bewertung',
                    average: 'Ø Bewertung'
                }
            },
            state: 'Status'
        },
        pages: {
            dashboard_page: {
                my_challenges: {
                    label: "Meine Challenges"
                },
                all_challenges: {
                    label: "Alle Challenges"
                },
                no_challenges_found: "Keine Challenges vorhanden...Bitte melden Sie sich an, um Challenges zu sehen.",
                new_challenge: {
                    title: "Challenge erstellen",
                }
            },
            edit_challenge_page: {
                section_labels: {
                    general_information: 'Allgemeine Informationen',
                    details: 'Details der Challenge',
                    referees: 'Gutachtende',
                    invited_users: 'Eingeladene Teilnehmer',
                },
                milestone_begin: 'Beginn der {field}',
                save_modal: 'Challenge wird gespeichert',
                save_attachments_modal: 'Anhänge wird gespeichert',
                save_modal_error: 'Fehler beim Speichern der Challenge',
            },
            challenge_details_page: {
                section_labels: {
                    ideas: 'Lösungsvorschläge'
                },
                no_ideas_found: 'Keine Lösungsvorschläge vorhanden.',
                no_ideas_found_do_you_want_to_add_some: 'Hast Du eine Idee?',
                add_idea: 'Neuer Vorschlag',
                timeline: {
                    type: {
                        label: 'Art',
                        values: {
                            update: 'Update',
                            planing: 'Planungsphase',
                            piloting: 'Pilotierungsphase',
                            success: 'Umgesetzt',
                            not_implemented: 'Archiviert'
                        }
                    },
                    date: 'Datum des Updates',
                    content: 'Text',
                    send: 'Update senden'
                }
            },
            ideas: {
                section_labels: {
                    general_information: 'Allgemeine Informationen',
                    team_name: 'Team Name',
                    team_members: 'Teammitglieder',
                    short_description: 'Kurzbeschreibung',
                    details: 'Lösungsdetails'
                },
                team_members: {
                    hidden: "Teammitgliedschaft verborgen",
                    visible: "Teammitgliedschaft öffentlich"
                },
                new: {
                    save_modal: 'Lösungsvorschlag wird gespeichert',
                    save_modal_error: 'Fehler beim Speichern des Lösungsvorschlags',
                    add_text_canvas_element: 'Text-Element hinzufügen'
                },
                state: {
                    draft: {
                        label: 'Entwurf',
                        description: 'Vorschlag wurde gespeichert und kann von dem Team bearbeitet werden.'
                    },
                    submitted: {
                        label: 'Eingereicht',
                        description: 'Vorschlag wurde für das Review durch die Gutachter freigegeben.'
                    },
                    ready_for_vote: {
                        label: 'Abstimmung',
                        description: 'Vorschlag wurde von den Gutachtern für die öffentliche Abstimmung nominiert.'
                    }
                },
                winner: {
                    label: 'Ergebnis: ',
                    0: 'Die Idee hat leider nicht gewonnen',
                    1: 'Die Idee hat den ersten Platz belegt!',
                    2: 'Die Idee hat den zweiten Platz belegt!',
                    3: 'Die Idee hat den dritten Platz belegt!'
                },
                canvas: {
                    element_title: 'Lösungselement {id}'
                },
                actions: {
                    submit: {
                        label: 'Einreichen',
                        modal: {
                            title: 'Idee einreichen',
                            description: 'Durch das Einreichen der Idee wird diese für die Gutachtende sichtbar und kann für die öffentliche Abstimmung ausgewählt werden. Sie können die Idee anschließend jedoch nicht mehr zurückziehen. Möchten Sie die Idee jetzt einreichen?'
                        }
                    },
                    nominate_for_vote: {
                        label: 'Für das Voting freischalten',
                        modal: {
                            title: 'Idee für das öffentliche Voting freischalten',
                            description: 'Durch das Freischalten wird die Idee in der öffentlichen Abstimmungsphase sichtbar. Möchten Sie die Idee jetzt freischalten?'
                        }
                    },
                    nominate_as_winner: {
                        label: 'Als Sieger markieren',
                        modal: {
                            title: 'Idee als Sieger der Challenge markieren',
                            description: 'Welchen Platz hat die Idee erreicht?',
                            options: {
                                label: 'Platzierung',
                                no: 'Nicht gewonnen',
                                first: 'Erster Platz',
                                second: 'Zweiter Platz',
                                third: 'Dritter Platz'
                            }
                        }
                    },
                    copy_element_reference_to_clipboard: "Link zum Element kopieren"
                },
                chats: {
                    team: {
                        title: 'Team-Chat',
                        new_message_placeholder: 'Sag was zu Deinem Team'
                    },
                    referees: {
                        title: 'Gutachtende-Chat',
                        new_message_placeholder: 'Sag was zu den anderen Gutachtenden'
                    },
                    team_and_referees: {
                        title: 'Team und Gutachtende',
                        new_message_placeholder: 'Sag was zum Team und den Gutachtenden'
                    },
                    no_messages: 'Noch keine Nachrichten. Hast Du etwas zu sagen?',
                    send: 'Abschicken',
                }
            },
            invitation: {
                title: 'Einladung',
                invalid_url: "Der Einladungslink ist leider nicht gültig.",
                referee: "Sie wurden von {CREATOR} eingeladen, als Gutachter der Challenge '{ADDITIONAL_INFORMATION}' teilzunehmen.",
                invited_user: "Sie wurden von {CREATOR} eingeladen, Ideen für die Challenge '{ADDITIONAL_INFORMATION}' einzureichen.",
                team_member: "Sie wurden von {CREATOR} eingeladen, in seinem Team '{ADDITIONAL_INFORMATION}' mitzuarbeiten.",
                expiry_date: 'Ihre Einladung ist gültig bis {exp}.',
                accept_question: 'Möchten Sie die Einladung annehmen?',
                accept: 'Einladung annehmen',
                waring_login_title: 'Hinweis',
                waring_login: 'Sie müssen angemeldet sein, um die Einladung anzunehmen. ',
                waring_login_register: 'Sollten Sie noch keinen Account besitzen, können Sie auf dieser Seite einen anlegen: ',
                accept_successful: 'Die Einladung wurde erfolgreich angenommen',
                error_403: {
                    title: 'Fehler',
                    explanation: 'Die Annahme der Einladung ist fehlgeschlagen. Wahrscheinlich verhindern die Sichtbarkeitseinstellungen der Challenge, dass Sie diese sehen können. Ihre Teilnahme ist somit leider nicht möglich. :('
                },
                error_expired: {
                    title: 'Fehler',
                    explanation: "Ihr Einladungslink ist nicht mehr gültig. Sie können '{CREATOR}' bitten, Ihnen eine neue Einladung zu senden."
                }
            }
        },
        components: {
            challenge_preview: {
                start: 'Start: '
            },
            navbar: {
                actions: {
                    login: 'Anmelden',
                    register: 'Registrieren',
                    logout: 'Abmelden',
                }
            },
            modal: {
                loading_screen: {
                    text: 'Daten werden geladen...'
                }
            }
        }
    },
    en: {
        common: {
            navigation: {
                dashboard: 'Dashboard',
                challenge: 'Challenge',
                idea: 'Solution proposal'
            },
            challenge_information: {
                title: 'Title',
                description: 'Description',
                attachments: {
                    title: 'Attachments',
                    filename: 'File name',
                    download: "Download",
                    delete: "Delete",
                    add: "Add attachment"
                },
                short_description: 'Short description',
                awards: 'Awards',
                news: 'News',
                visibility: {
                    label: 'Visibility',
                    values: {
                        new: {
                            label: 'New',
                            description: 'Create new challenge'
                        },
                        open: {
                            label: 'Public',
                            description: 'Visible for everyone'
                        },
                        internal: {
                            label: 'Only for employees',
                            description: 'Only visible to employees of own company',
                        },
                        invite: {
                            label: 'Only for invited participants',
                            description: 'Visible to invited participants only',
                        },
                    }
                },
                milestones: {
                    label: 'Milestones',
                    values: {
                        submission: {
                            label: 'Submission phase',
                            description: 'The challenge has been created. Ideas can be submitted.'
                        },
                        review: {
                            label: 'Review phase',
                            description: 'The submission period has ended. Submitted ideas are under review.'
                        },
                        refactoring: {
                            label: 'Review phase',
                            description: 'Ideas have been reviewed and notes for revision have been added.'
                        },
                        voting: {
                            label: 'Voting phase',
                            description: 'Ideas can no longer be edited. The nominated ideas can be voted on.'
                        },
                        implementation: {
                            label: 'Award ceremony',
                            description: 'The winner of the challenge will be determined and announced.'
                        },
                        end: {
                            label: 'End of the challenge',
                            description: 'The challenge has ended. The implementation phase in the company has ended.'
                        }
                    }
                },
            },
            information_not_available: '{field} not available',
            yes: 'yes',
            no: 'No',
            save: 'Save',
            cancel: 'Cancel',
            add: 'Add',
            invite: 'Invite',
            please_choose: 'Please choose...',
            required: 'Required field',
            edit: 'Edit',
            close: 'Close',
            invitation: {
                team: {
                    title: 'Invite to team',
                    not_yet_possible: 'Other people can join your team only after saving the idea. After that, a link will be displayed at this place, that can be forwarded to other people, which they can use to join your team.',
                    link_explanation: 'The following link allows other people to join your team. Just send the link to the people you want to collaborate with (e.g. by mail).'
                },
                referees: {
                    title: 'Invite as reviewer',
                    not_yet_possible: 'Other people can join the reviewer panel only after saving the challenge. After that, a link will be displayed at this place, that can be forwarded to other people and they can use to become reviewers of the challenge.',
                    link_explanation: 'The following link allows other people to join the reviewer panel of the challenge. Just send the link to the people you want to collaborate with (e.g. by mail).'
                },
                invited_users: {
                    title: 'Invite as participant',
                    invited_users: {
                        title: 'Invite as participant',
                        not_yet_possible: 'Other people can join the group of participants only after saving the challenge. After that, a link will be displayed at this place, that can be forwarded to other people and they can use to become participants of the challenge.',
                        link_explanation: 'The following link will allow other people to join the challenge\'s participant circle. Just send the link to the people you want to collaborate with (e.g. by mail).'
                    },
                },
                invitation_link: 'Invitation link: ',
                copy_to_clipboard: 'Save to clipboard',
                link_expiry_date: 'The link is valid until: {expiryDate}',
                visibility_warning: {
                    internal: 'Please note that only members of your company can accept the invitation link!',
                    invite: 'Please note that only people who have also been invited by the creator of the challenge can accept the invitation link!'
                }
            },
            persons: {
                displaypattern: "{name} <<{mail}>>",
                title: 'Search users',
                invite: 'Invite',
                invite_and_add: 'Invite and add',
                search: 'Search',
                no_results: 'No search results available...',
                search_failed: 'There was an error in the search :(',
                invite_failed: 'An error occurred while inviting the person :(',
                already_added: 'The person has already been added',
                not_possible: 'Adding is not possible due to the visibility of the challenge',
                first_name: 'First name',
                last_name: 'Last name',
                email: 'Email address',
                successful_invited: '{name} was invited successfully'
            },
            errors: {
                title: 'Error',
                not_blank: 'The field must not be empty!',
                loading_failed: 'There was an error loading the data',
                attachment_loading_failed: 'There was an error loading the attachment or image',
                file_too_big: 'The file could not be added because larger than the maximum size of {size} MB.',
                updated_failed: 'Failed to save the changes',
                send_message_failed: 'Sending the message failed',
                forbidden: 'You do not have the necessary rights to view this page',
                not_found: 'Page not found :(',
                invalid_date: 'The date entered is not valid',
                invitation_accept_failed: 'Unfortunately, accepting the invitation failed :('
            },
            notification: {
                success: {
                    title: 'Successful',
                    saved: 'Successfully saved'
                }
            },
            cancel_changes: 'Are you sure you want to cancel the changes?',
            idea: {
                permissions: {
                    values: {
                        edit: {
                            description: "Edit"
                        },
                        referee: {
                            description: "Review"
                        },
                        change_state: {
                            description: "Nomination for public review"
                        },
                        nominate_as_winner: {
                            description: "Mark as winner"
                        },
                        vote: {
                            description: "Rating of the idea"
                        }
                    }
                },
                voting: {
                    title: 'Evaluation',
                    own: 'Own rating',
                    average: 'Ø rating'
                }
            },
            state: 'Status'
        },
        pages: {
            dashboard_page: {
                my_challenges: {
                    label: "My Challenges"
                },
                all_challenges: {
                    label: "All Challenges"
                },
                no_challenges_found: "No challenges available...Please sign in to see challenges.",
                new_challenge: {
                    title: "Create Challenge"
                }
            },
            edit_challenge_page: {
                section_labels: {
                    general_information: 'General information',
                    details: 'Challenge details',
                    referees: 'Referees',
                    invited_users: 'Invited participants',
                },
                milestone_begin: 'Start of {field}',
                save_modal: 'Challenge will be saved',
                save_attachments_modal: 'Attachments will be saved',
                save_modal_error: 'Error saving the challenge',

            },
            challenge_details_page: {
                section_labels: {
                    ideas: 'Suggested solutions'
                },
                no_ideas_found: 'No solution suggestions available',
                no_ideas_found_do_you_want_to_add_some: 'Do you have any ideas?',
                add_idea: 'New suggestion',
                timeline: {
                    type: {
                        label: 'Kind',
                        values: {
                            update: 'Update',
                            planing: 'Planning phase',
                            piloting: 'Piloting phase',
                            success: 'Implemented',
                            not_implemented: 'Archived'
                        }
                    },
                    date: 'Date of update',
                    content: 'Text',
                    send: 'Send update'
                }
            },
            ideas: {
                section_labels: {
                    general_information: 'General information',
                    team_name: 'Team name',
                    team_members: 'Team members',
                    short_description: 'Short description',
                    details: 'Solution details'
                },
                team_members: {
                    hidden: 'Team membership hidden',
                    visible: 'Team membership public'
                },
                new: {
                    save_modal: 'Saving solution proposal',
                    save_modal_error: 'Error saving solution proposal',
                    add_text_canvas_element: 'Add text element'
                },
                state: {
                    draft: {
                        label: 'Draft',
                        description: 'Proposal has been saved and can be edited by the team.',
                    },
                    submitted: {
                        label: 'Submitted',
                        description: 'Proposal has been approved for review by the reviewers.'
                    },
                    ready_for_vote: {
                        label: 'Voting',
                        description: 'Proposal has been nominated for public vote by the reviewers.'
                    }
                },
                winner: {
                    label: 'Result: ',
                    0: 'Unfortunately, the idea did not win',
                    1: 'The idea has won the first place!',
                    2: 'The idea has taken the second place!',
                    3: 'The idea took third place!'
                },
                canvas: {
                    element_title: 'Solution element {id}'
                },
                actions: {
                    submit: {
                        label: 'Submit',
                        modal: {
                            title: 'Submit idea',
                            description: 'By submitting the idea, it becomes visible to the reviewers and can be selected for the public vote. However, you cannot withdraw the idea afterwards. Would you like to submit the idea now?'
                        }
                    },
                    nominate_for_vote: {
                        label: 'Nominate for voting',
                        modal: {
                            title: 'Nominate idea for public voting',
                            description: 'Nominating the idea will make it visible in the public voting phase. Would you like to nominate the idea now?'
                        },
                    },
                    nominate_as_winner: {
                        label: 'Mark as winner',
                        modal: {
                            title: 'Mark idea as winner of the challenge',
                            description: 'What place did the idea get?',
                            options: {
                                label: 'Placement',
                                no: 'Not won',
                                first: 'First place',
                                second: 'Second place',
                                third: 'Third place'
                            }
                        }
                    },
                    copy_element_reference_to_clipboard: 'Copy link to element'
                },
                chats: {
                    team: {
                        title: 'Team chat',
                        new_message_placeholder: 'Say something to your team'
                    },
                    referees: {
                        title: 'Reviewer chat',
                        new_message_placeholder: 'Say something to the other reviewers'
                    },
                    team_and_referees: {
                        title: 'Team and reviewers',
                        new_message_placeholder: 'Say something to the team and the reviewers'
                    },
                    no_messages: 'No messages yet. Do you have anything to say?',
                    send: 'Send',
                },
            },
            invitation: {
                title: 'Invitation',
                invalid_url: "Sorry, the invitation link is not valid.",
                referee: "You have been invited by {CREATOR} to participate as a referee of the challenge '{ADDITIONAL_INFORMATION}'.",
                invited_user: "You have been invited by {CREATOR} to submit ideas for the challenge '{ADDITIONAL_INFORMATION}'.",
                team_member: "You have been invited by {CREATOR} to join his team '{ADDITIONAL_INFORMATION}'.",
                expiry_date: 'Your invitation is valid until {exp}.',
                accept_question: 'Do you want to accept the invitation?',
                accept: 'Accept invitation',
                waring_login_title: 'Notice',
                waring_login: 'You must be logged in to accept the invitation. ',
                waring_login_register: 'If you don\'t have an account yet, you can create one on this page: ',
                accept_successful: 'The invitation has been accepted successfully',
                error_403: {
                    title: 'Error',
                    explanation: 'The acceptance of the invitation failed. Probably the visibility settings of the challenge prevent you from seeing it. Your participation is therefore unfortunately not possible. :('
                },
                error_expired: {
                    title: 'Error',
                    explanation: 'Your invitation link is no longer valid. You can ask  \'{CREATOR}\' to send you a new invitation.',
                }
            }
        },
        components: {
            challenge_preview: {
                start: 'Start: '
            },
            navbar: {
                actions: {
                    login: 'Log in',
                    register: 'Register',
                    logout: 'Logout',
                }
            },
            modal: {
                loading_screen: {
                    text: 'Data is being loaded...'
                }
            }
        }
    }
};

export default new VueI18n({
    locale: 'en',
    fallbackLocale: 'de',
    messages
});
