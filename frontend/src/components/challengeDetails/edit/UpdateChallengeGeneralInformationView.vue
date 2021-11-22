<template>
    <edit-challenge-general-information :challenge="challenge" :form-errors="formErrors" />
</template>

<script>
    import {APIService} from "../../../until/APIService";
    import EditChallengeGeneralInformation from "./EditChallengeGeneralInformation";

    export default {
        name: "UpdateChallengeGeneralInformationView",
        components: {EditChallengeGeneralInformation},
        props: ["title", "visibility", "shortDescription"],
        data: function () {
            return {
                challenge: {
                    title: '',
                    visibility: '',
                    shortDescription: ''
                },
                formErrors: []
            }
        },
        created() {
            this.challenge.title = this.title;
            this.challenge.visibility = this.visibility;
            this.challenge.shortDescription = this.shortDescription;
            this.formErrors = [];
        },
        methods: {
            save: function (finishEditMode, loadFunction) {
                let challengeId = this.$route.params.id;
                return APIService.updateChallengeGeneralInformation(challengeId, this.challenge)
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
        }
    }
</script>

<style scoped>

</style>
