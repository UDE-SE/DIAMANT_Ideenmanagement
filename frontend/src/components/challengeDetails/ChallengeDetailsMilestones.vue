<template>
    <div class="">
        <ul class="steps has-content-centered">
            <li class="steps-segment" :class="getDateDependentCSSClasses(submissionStart, reviewStart)">
                <div class="steps-marker">
                    <span class="icon" v-if="isPastDate(submissionStart)">
                          <i class="fa fa-check" />
                    </span>
                </div>
                <div class="steps-content tooltip is-tooltip-multiline" :data-tooltip="$t('common.challenge_information.milestones.values.submission.description')">
                    <p class="is-size-5 has-text-weight-semibold">{{$t('common.challenge_information.milestones.values.submission.label')}}</p>
                    <p class="is-size-6">{{submissionStart | toDateString}}</p>
                </div>
            </li>
            <li class="steps-segment" :class="getDateDependentCSSClasses(reviewStart, refactoringStart > 0 ? refactoringStart : reviewStart)">
                <div class="steps-marker">
                    <span class="icon" v-if="isPastDate(reviewStart)">
                          <i class="fa fa-check" />
                    </span>
                </div>
                <div class="steps-content tooltip is-tooltip-multiline" :data-tooltip="$t('common.challenge_information.milestones.values.review.description')">
                    <p class="is-size-5 has-text-weight-semibold">{{$t('common.challenge_information.milestones.values.review.label')}}</p>
                    <p class="is-size-6">{{reviewStart | toDateString}}</p>
                </div>
            </li>
            <li class="steps-segment" v-if="refactoringStart > 0" :class="getDateDependentCSSClasses(refactoringStart, votingStart)">
                <div class="steps-marker">
                    <span class="icon" v-if="isPastDate(refactoringStart)">
                          <i class="fa fa-check" />
                    </span>
                </div>
                <div class="steps-content tooltip is-tooltip-multiline" :data-tooltip="$t('common.challenge_information.milestones.values.refactoring.description')">
                    <p class="is-size-5 has-text-weight-semibold">{{$t('common.challenge_information.milestones.values.refactoring.label')}}</p>
                    <p class="is-size-6">{{refactoringStart | toDateString}}</p>
                </div>
            </li>
            <li class="steps-segment" :class="getDateDependentCSSClasses(votingStart, implementationStart)">
                <div class="steps-marker">
                    <span class="icon" v-if="isPastDate(votingStart)">
                          <i class="fa fa-check" />
                    </span>
                </div>
                <div class="steps-content tooltip is-tooltip-multiline" :data-tooltip="$t('common.challenge_information.milestones.values.voting.description')">
                    <p class="is-size-5 has-text-weight-semibold">{{$t('common.challenge_information.milestones.values.voting.label')}}</p>
                    <p class="is-size-6">{{votingStart | toDateString}}</p>
                </div>
            </li>
            <li class="steps-segment" :class="getDateDependentCSSClasses( implementationStart, challengeEnd)">
                <div class="steps-marker">
                    <span class="icon">
                        <i class="fa fa-crown" />
                    </span>
                </div>
                <div class="steps-content tooltip is-tooltip-multiline" :data-tooltip="$t('common.challenge_information.milestones.values.implementation.description')">
                    <p class="is-size-5 has-text-weight-semibold">{{$t('common.challenge_information.milestones.values.implementation.label')}}</p>
                    <p class="is-size-6">{{implementationStart | toDateString}}</p>
                </div>
            </li>
            <li class="steps-segment" :class="getDateDependentCSSClasses(challengeEnd, 99999999999999999999)">
                <div class="steps-marker">
                    <span class="icon">
                        <i class="fa fa-flag" />
                    </span>
                </div>
                <div class="steps-content tooltip is-tooltip-multiline" :data-tooltip="$t('common.challenge_information.milestones.values.end.description')">
                    <p class="is-size-5 has-text-weight-semibold">{{$t('common.challenge_information.milestones.values.end.label')}}</p>
                    <p class="is-size-6">{{challengeEnd | toDateString}}</p>
                </div>
            </li>
        </ul>
    </div>
</template>
<script>
    import {DateService} from "../../until/DateService";

    export default {
        name: 'ChallengeDetailsMilestones',
        props: {
            submissionStart: Number,
            reviewStart: Number,
            refactoringStart: Number,
            votingStart: Number,
            implementationStart: Number,
            challengeEnd: Number
        },
        filters: {
            toDateString: function (milliseconds) {
                if(milliseconds == null) {
                    return "---"
                }
                return new Date(milliseconds).toLocaleDateString()
            }
        },
        methods: {
            isPastDate(date){
                return date != null && date > 0 && DateService.getDate().getTime() > date;
            },
            getDateDependentCSSClasses(currentMileStone, nextMileStone) {
                if(this.isPastDate(nextMileStone)){
                    return "is-complete is-success"
                }
                if(this.isPastDate(currentMileStone)){
                    return "is-dashed"
                }
                return "is-active is-dashed";
            }
        }
    }
</script>
<style lang="scss">
</style>
