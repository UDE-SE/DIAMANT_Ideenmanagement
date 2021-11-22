<template>
    <editable-card :title="$t('common.idea.voting.title')" :is-editable="false" icon-class="fas fa-star">
        <div class="columns" v-if="!canVote">
            <div class="column">
                <div class="notification" :style="'background-color: ' + (winningPlace === 1 ? 'gold' :
                                                                            winningPlace === 2 ? 'silver' :
                                                                            winningPlace === 3 ? 'brown' : '')"
                                        :class="winningPlace > 0 ? 'has-text-white' : ''">
                    {{$t('pages.ideas.winner.label')}} {{$t('pages.ideas.winner.' + winningPlace)}}
                </div>
            </div>
        </div>
        <div class="columns">
            <div class="column is-expanded">
                <div class="field">
                    <label class="label" for="averageVoting">{{$t('common.idea.voting.average')}}:</label>
                    <div class="control">
                        <template v-if="innerAverageVoting == null">
                            <input class="input is-static" type="text" id="averageVoting" value="--" readonly >
                        </template>
                        <template v-else>
                            <diamant-idea-voting id="averageVoting" :value="innerAverageVoting" :is-editable="false"/>
                        </template>
                    </div>
                </div>
            </div>
            <div class="column is-narrow">
                <div class="field is-narrow">
                    <label class="label" for="ownVoting">{{$t('common.idea.voting.own')}}:</label>
                    <div class="control">
                        <diamant-idea-voting id="ownVoting" :value="innerOwnVoting || 0" :is-editable="canVote" @changed="saveVote"/>
                    </div>
                </div>
            </div>
        </div>
    </editable-card>
</template>

<script>
    import DiamantIdeaVoting from "../generic/DiamantIdeaVoting";
    import EditableCard from "../generic/EditableCard";
    import {APIService} from "../../until/APIService";
    export default {
        name: "IdeaVotingView",
        components: {EditableCard, DiamantIdeaVoting},
        props: ["ownVoting", "averageVoting", "canVote", "winningPlace"],
        data: function () {
            return {
                innerOwnVoting: 0,
                innerAverageVoting: 0
            }
        },
        created() {
            this.innerOwnVoting = this.ownVoting;
            this.innerAverageVoting = this.averageVoting;
        },
        methods: {
            saveVote(value){
                APIService.saveVoting(this.$route.params.challengeId, this.$route.params.ideaId, value)
                    .then(() => {
                        this.$notify({
                            title: this.$t('common.notification.success.title'),
                            text: this.$t('common.notification.success.saved'),
                            type: 'success'
                        });
                    })
                    .then(() => this.$emit('saved'))
                    .catch(function () {
                        this.$notify({
                            title: this.$t('common.errors.title'),
                            text: this.$t('common.errors.updated_failed'),
                            type: 'error'
                        })
                    }.bind(this));
            },
        },
    }
</script>

<style scoped>

</style>
