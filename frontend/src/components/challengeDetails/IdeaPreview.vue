<template>
    <same-height-card>
        <template v-slot:header-text>
            <span class="icon is-small" v-if="idea.winningPlace > 0">
                <i class="fas fa-medal" :style="idea.winningPlace === 1 ? 'color: gold' : idea.winningPlace === 2 ? 'color: silver' : 'color: brown'"/>
            </span>
            {{idea.teamName}}
        </template>
        <template v-slot:header-icon>
            <idea-permissions-indicator :permission="idea.currentUserPermissions" />
        </template>
        <div class="content">
            {{idea.shortDescription}}
        </div>
        <template v-slot:footer v-if="isVotingVisible">
            <span class="card-footer-item tooltip" :data-tooltip="$t('common.idea.voting.own')">
                <span>
                    <span class="icon is-small">
                        <i class="fa fa-user"/>
                    </span>
                    <span> : </span>
                    <diamant-vote-result :value="idea.ownVoting" />
                </span>
            </span>
            <span class="card-footer-item tooltip" :data-tooltip="$t('common.idea.voting.average')">
                <span>
                    <span class="icon is-small">
                    <i class="fa fa-users"/>
                </span>
                <span> : </span>
                <diamant-vote-result :value="idea.averageVoting" />
                </span>
            </span>
        </template>
    </same-height-card>
</template>

<script>
    import IdeaPermissionsIndicator from "./IdeaPermissionsIndicator";
    import DiamantVoteResult from "../generic/DiamantVoteResult";
    import SameHeightCard from "../generic/SameHeightCard";
    export default {
        name: "IdeaPreview",
        components: {SameHeightCard, DiamantVoteResult, IdeaPermissionsIndicator},
        props: {
            idea: Object
        },
        computed: {
            isVotingVisible(){
                return (this.idea.currentUserPermissions != null && this.idea.currentUserPermissions.includes('VOTE')) ||
                            this.idea.averageVoting != null;
            }
        }
    }
</script>
