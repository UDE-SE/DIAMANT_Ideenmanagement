<template>
    <edit-challenge-mile-stones :challenge="challenge" :form-errors="formErrors"/>
</template>

<script>
    import {APIService} from "../../../until/APIService";
    import EditChallengeMileStones from "./EditChallengeMileStones";

    export default {
        name: "UpdateMileStonesView",
        components: {EditChallengeMileStones},
        props: ["submissionStart", "reviewStart", "refactoringStart", "votingStart", "implementationStart", "challengeEnd"],
        data: function () {
            return {
                challenge: {
                    submissionStart: null,
                    reviewStart: null,
                    refactoringStart: null,
                    votingStart: null,
                    implementationStart: null,
                    challengeEnd: null,
                },
                formErrors: []
            }
        },
        created() {
            this.challenge.submissionStart = this.submissionStart;
            this.challenge.reviewStart = this.reviewStart;
            this.challenge.refactoringStart = this.refactoringStart;
            this.challenge.votingStart = this.votingStart;
            this.challenge.implementationStart = this.implementationStart;
            this.challenge.challengeEnd = this.challengeEnd;
            this.formErrors = [];
        },
        methods: {
            save: function (finishEditMode, loadFunction) {
                let challengeId = this.$route.params.id;
                return APIService.updateChallengeMilestones(challengeId, this.challenge)
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
