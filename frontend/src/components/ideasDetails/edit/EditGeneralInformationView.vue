<template>
    <div>
        <div class="field" v-show-error-directive="formErrors">
            <label class="label" :for="componentBasedId('teamName')">{{$t('pages.ideas.section_labels.team_name')}}
                <required-indicator/>
            </label>
            <div class="control">
                <input class="input" type="text"
                       :placeholder="$t('pages.ideas.section_labels.team_name')"
                       data-error-field-id="teamName"
                       :id="componentBasedId('teamName')" v-model="changingTeamName">
            </div>
        </div>
        <div class="field" v-show-error-directive="formErrors">
            <label class="label" :for="componentBasedId('shortDescription')">{{$t('pages.ideas.section_labels.short_description')}}
                <required-indicator/>
            </label>
            <div class="control">
                <textarea class="textarea" :id="componentBasedId('shortDescription')" v-model="changingShortDescription" data-error-field-id="shortDescription"/>
            </div>
        </div>
    </div>
</template>

<script>
    import RequiredIndicator from "../../generic/RequiredIndicator";
    import ShowErrorDirective from "../../generic/ShowErrorDirective";
    import {APIService} from "../../../until/APIService";
    import DiamantComponentIdGeneratorMixin from "../../generic/DiamantComponentIdGeneratorMixin";

    export default {
        name: "EditGeneralInformationView",
        components: {RequiredIndicator},
        mixins: [DiamantComponentIdGeneratorMixin],
        directives: {
            ShowErrorDirective,
        },
        props: ["teamName", "shortDescription"],
        data: function () {
            return {
                changingTeamName: '',
                changingShortDescription: '',
                formErrors: []
            }
        },
        created() {
            this.changingShortDescription = this.shortDescription;
            this.changingTeamName = this.teamName;
            this.formErrors = [];
        },
        methods: {
            save: function (finishEditMode, loadIdeaFunction) {
                let challengeId = this.$route.params.challengeId;
                let ideaId = this.$route.params.ideaId;
                return APIService.updateIdeaGeneralInformation(challengeId, ideaId, this.changingTeamName, this.changingShortDescription)
                    .then(() => {
                        this.$notify({
                            title: this.$t('common.notification.success.title'),
                            text: this.$t('common.notification.success.saved'),
                            type: 'success'
                        });
                    })
                    .then(finishEditMode)
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
        }
    }
</script>

<style scoped>

</style>
