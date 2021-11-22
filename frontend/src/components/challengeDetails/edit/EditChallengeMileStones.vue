<template>
    <div>
        <div class="content">
            <div class="field" v-show-error-directive="formErrors">
                <label class="label" for="submissionStart">
                    <span class="icon tooltip" :data-tooltip="$t('common.challenge_information.milestones.values.submission.description')">
                        <i class="fas fa-info-circle"/>
                    </span>
                    {{$t('pages.edit_challenge_page.milestone_begin',
                    {'field': $t('common.challenge_information.milestones.values.submission.label')}
                    )}}<required-indicator/>
                </label>
                <div class="control">
                    <diamant-date-picker v-model="challenge.submissionStart" id="submissionStart" data-error-field-id="mileStones.submissionStart"/>
                </div>
            </div>
        </div>
        <div class="content">
            <div class="field" v-show-error-directive="formErrors">
                <label class="label" for="reviewStart">
                    <span class="icon tooltip" :data-tooltip="$t('common.challenge_information.milestones.values.review.description')">
                        <i class="fas fa-info-circle"/>
                    </span>
                    {{$t('pages.edit_challenge_page.milestone_begin',
                    {'field': $t('common.challenge_information.milestones.values.review.label')}
                    )}}<required-indicator/>
                </label>
                <div class="control">
                    <diamant-date-picker id="reviewStart" v-model="challenge.reviewStart" data-error-field-id="mileStones.reviewStart"/>
                </div>
            </div>
        </div>
        <div class="content">
            <div class="field">
                <label class="label checkbox" for="refactoringStart" @click="toggleRefactoringPossible">
                    <input type="checkbox" v-model="isRefactoringPossible">
                    <span class="icon tooltip" :data-tooltip="$t('common.challenge_information.milestones.values.refactoring.description')">
                        <i class="fas fa-info-circle"/>
                    </span>
                    {{$t('pages.edit_challenge_page.milestone_begin',
                    {'field': $t('common.challenge_information.milestones.values.refactoring.label')}
                    )}}<required-indicator v-if="isRefactoringPossible"/>
                </label>
                <div class="control" v-show-error-directive="formErrors">
                    <diamant-date-picker id="refactoringStart" :disabled="isRefactoringPossible === false" v-model="challenge.refactoringStart"
                                         data-error-field-id="mileStones.refactoringStart"/>
                </div>
            </div>
        </div>
        <div class="content">
            <div class="field" v-show-error-directive="formErrors">
                <label class="label" for="votingStart">
                    <span class="icon tooltip" :data-tooltip="$t('common.challenge_information.milestones.values.voting.description')">
                        <i class="fas fa-info-circle"/>
                    </span>
                    {{$t('common.challenge_information.milestones.values.voting.label')}}<required-indicator/>
                </label>
                <div class="control">
                    <diamant-date-picker id="votingStart" v-model="challenge.votingStart" data-error-field-id="mileStones.votingStart"/>
                </div>
            </div>
        </div>
        <div class="content">
            <div class="field" v-show-error-directive="formErrors">
                <label class="label" for="implementationStart">
                    <span class="icon tooltip" :data-tooltip="$t('common.challenge_information.milestones.values.implementation.description')">
                        <i class="fas fa-info-circle"/>
                    </span>
                    {{$t('common.challenge_information.milestones.values.implementation.label')}}<required-indicator/>
                </label>
                <div class="control">
                    <diamant-date-picker id="implementationStart" v-model="challenge.implementationStart" data-error-field-id="mileStones.implementationStart"/>
                </div>
            </div>
        </div>
        <div class="content">
            <div class="field" v-show-error-directive="formErrors">
                <label class="label" for="challengeEnd">
                    <span class="icon tooltip" :data-tooltip="$t('common.challenge_information.milestones.values.end.description')">
                        <i class="fas fa-info-circle"/>
                    </span>
                    {{$t('common.challenge_information.milestones.values.end.label')}}<required-indicator/>
                </label>
                <div class="control">
                    <diamant-date-picker id="challengeEnd" v-model="challenge.challengeEnd" data-error-field-id="mileStones.challengeEnd"/>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import RequiredIndicator from "../../generic/RequiredIndicator";
    import ShowErrorDirective from "../../generic/ShowErrorDirective";
    import DiamantDatePicker from "./DiamantDatePicker";

    export default {
        name: "EditChallengeMileStones",
        components: {DiamantDatePicker, RequiredIndicator},
        directives: {ShowErrorDirective},
        props: {
            challenge: {
                type: Object,
                required: true
            },
            formErrors: {
                type: Array,
                required: true
            }
        },
        data: function () {
            return {
                isRefactoringPossible: false
            }
        },
        created: function() {
            this.isRefactoringPossible = (this.challenge != null && this.challenge.refactoringStart != null);
        },
        methods: {
            toggleRefactoringPossible() {
                if(this.isRefactoringPossible) {
                    this.challenge.refactoringStart = null;
                    this.isRefactoringPossible = false;
                } else {
                    this.challenge.refactoringStart = new Date().getTime();
                    this.isRefactoringPossible = true;
                }
            },
        },
    }
</script>

<style scoped>

</style>
