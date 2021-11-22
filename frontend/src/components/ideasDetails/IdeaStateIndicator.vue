<template>
    <ul class="steps has-content-centered">
        <li class="steps-segment" :class="getDateDependentCSSClasses('DRAFT')">
            <div class="steps-marker">
                <span class="icon" v-if="isFinished('DRAFT')">
                      <i class="fa fa-check" />
                </span>
            </div>
            <div class="steps-content">
                <p class="is-4 has-text-weight-semibold">{{$t('pages.ideas.state.draft.label')}}</p>
                <p class="is-6 is-italic">{{$t('pages.ideas.state.draft.description')}}</p>
            </div>
        </li>
        <li class="steps-segment" :class="getDateDependentCSSClasses('SUBMITTED')">
            <div class="steps-marker">
                <span class="icon" v-if="isFinished('SUBMITTED')">
                      <i class="fa fa-check" />
                </span>
            </div>
            <div class="steps-content">
                <p class="is-4 has-text-weight-semibold">{{$t('pages.ideas.state.submitted.label')}}</p>
                <p class="is-6 is-italic">{{$t('pages.ideas.state.submitted.description')}}</p>
            </div>
        </li>
        <li class="steps-segment" :class="getDateDependentCSSClasses('READY_FOR_VOTE')">
            <div class="steps-marker">
                <span class="icon" v-if="isFinished('READY_FOR_VOTE')">
                      <i class="fa fa-check" />
                </span>
            </div>
            <div class="steps-content">
                <p class="is-4 has-text-weight-semibold">{{$t('pages.ideas.state.ready_for_vote.label')}}</p>
                <p class="is-6 is-italic">{{$t('pages.ideas.state.ready_for_vote.description')}}</p>
            </div>
        </li>
    </ul>
</template>
<script>
    export default {
        name: 'IdeaStateIndicator',
        props: ['ideaState'],
        methods: {
            isFinished(currentMilestone){
                const possibleStates = {
                    'DRAFT': 0,
                    'SUBMITTED': 1,
                    'READY_FOR_VOTE': 2
                };
                return possibleStates[currentMilestone] <= possibleStates[this.ideaState];
            },
            isActive(currentMilestone){
                return currentMilestone === this.ideaState;
            },
            getDateDependentCSSClasses(currentMileStone) {
                if(this.isActive(currentMileStone)){
                    return "is-dashed";
                }
                if(this.isFinished(currentMileStone)){
                    return "is-success"
                }
                return "is-active is-dashed";
            }
        }
    }
</script>
<style lang="scss">
</style>
