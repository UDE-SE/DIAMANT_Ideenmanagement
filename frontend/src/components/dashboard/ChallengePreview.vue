<template>
    <same-height-card :hasNoFooterBorder="true">
        <template v-slot:header-text>
                {{challenge.title}}
        </template>
        <template v-slot:header-icon>
            <diamant-challenge-visibility-indicator :visibility="challenge.visibility"/>
        </template>
        <div class="content">
            {{challenge.shortDescription}}
        </div>
        <template v-slot:footer v-if="challenge.submissionStart != null">
            <span class="card-footer-item is-italic is-right">
                {{$t('components.challenge_preview.start')}}{{challenge.submissionStart | toDateString}}
            </span>
        </template>
    </same-height-card>
</template>

<script>
    import DiamantChallengeVisibilityIndicator from "../generic/DiamantChallengeVisibilityIdicator";
    import SameHeightCard from "../generic/SameHeightCard";

    export default {
        name: "ChallengePreview",
        components: {SameHeightCard, DiamantChallengeVisibilityIndicator},
        props: {
            challenge: Object
        },
        filters: {
            toDateString: function (milliseconds) {
                return new Date(milliseconds).toLocaleDateString()
            }
        }
    }
</script>
