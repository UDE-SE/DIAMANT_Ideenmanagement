<template>
    <div>
        <div class="field" v-show-error-on-div-directive="formErrors" data-error-field-id="newValueDTO">
            <diamant-user-list :persons="addedPersons" :show-delete-button="true" @remove="removePerson" />
        </div>
        <diamant-show-invitation-link-modal
                :type="type"
                :invitation-link="invitationLink"
                ref="invitationLinkModal"
        />
    </div>
</template>

<script>
    import ShowErrorOnDivDirective from "../../generic/ShowErrorOnDivDirective";
    import DiamantUserList from "../../generic/DiamantUserList";
    import {APIService} from "../../../until/APIService";
    import DiamantShowInvitationLinkModal from "../../generic/modal/DiamantShowInvitationLinkModal";

    export default {
        name: "UpdatePersonsView",
        components: {DiamantShowInvitationLinkModal, DiamantUserList},
        directives: {
            ShowErrorOnDivDirective,
        },
        props: ["persons", "type", "invitationLink"],
        data: function () {
            return {
                addedPersons: [],
                formErrors: []
            }
        },
        created() {
            this.addedPersons = JSON.parse(JSON.stringify(this.persons));
            this.formErrors = [];
        },
        methods: {
            save: function (finishEditMode, loadFunction) {
                let challengeId = this.$route.params.id;
                let personType = this.type === 'referees' ? 'REFEREES' : 'INVITED_USERS';
                return APIService.updatePersons(challengeId, personType, this.addedPersons)
                    .then(() => {
                        this.$notify({
                            title: this.$t('common.notification.success.title'),
                            text: this.$t('common.notification.success.saved'),
                            type: 'success'
                        });
                    })
                    .then(finishEditMode)
                    .then(loadFunction)
                    .catch(function (error) {
                        if (error && error.status === 400) {
                            this.formErrors = error.data.errors;
                        } else {
                            console.log(error);
                            this.$notify({
                                title: this.$t('common.errors.title'),
                                text: this.$t('common.errors.updated_failed'),
                                type: 'error'
                            })
                        }
                    }.bind(this));
            },
            removePerson(person, index){
                this.addedPersons.splice(index, 1);
            },
            showDisplayInvitationLinkDialog() {
                this.$refs.invitationLinkModal.show()
            },
        }
    }
</script>
