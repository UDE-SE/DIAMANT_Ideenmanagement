<template>
    <div>
        <div class="field" v-show-error-on-div-directive="formErrors" data-error-field-id="newValueDTO">
            <diamant-user-list :persons="team" :show-delete-button="true" @remove="removeTeamMember"
                :has-incognito-indicator="true" :show-toggle-incognito-button="true"
            />
        </div>
        <diamant-show-invitation-link-modal
                type="TEAM"
                :invitation-link="invitationLink"
                :challenge-visibility="challengeVisibility"
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
        name: "EditTeamView",
        components: {DiamantShowInvitationLinkModal, DiamantUserList},
        directives: {
            ShowErrorOnDivDirective,
        },
        props: ["persons", "invitationLink" ,"challengeVisibility"],
        data: function () {
            return {
                team: [],
                formErrors: []
            }
        },
        created() {
            this.team = JSON.parse(JSON.stringify(this.persons));
            this.formErrors = [];
        },
        methods: {
            save: function (finishEdit, loadIdeaFunction) {
                let challengeId = this.$route.params.challengeId;
                let ideaId = this.$route.params.ideaId;
                return APIService.updateIdeaTeam(challengeId, ideaId, this.team)
                    .then(() => {
                        this.$notify({
                            title: this.$t('common.notification.success.title'),
                            text: this.$t('common.notification.success.saved'),
                            type: 'success'
                        });
                    })
                    .then(finishEdit)
                    .then(loadIdeaFunction)
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
            removeTeamMember(person, index){
                this.team.splice(index, 1);
            },
            showDisplayInvitationLinkDialog() {
                this.$refs.invitationLinkModal.show()
            },
        }
    }
</script>

<style scoped>

</style>
