<template>
<span>
    <span class="icon tooltip" data-tooltip="Sichtbarkeit nicht bekannt"
          v-if="! (['OPEN', 'INTERNAL', 'INVITE', 'NEW'].includes(visibility))">
        <i class="fa fa-question" />
    </span>
    <span class="icon tooltip" :data-tooltip="$t('common.challenge_information.visibility.values.open.description')"
          v-else-if="visibility === 'OPEN'">
        <i class="fa fa-globe-europe" />
    </span>
    <span class="icon tooltip" :data-tooltip="$t('common.challenge_information.visibility.values.internal.description')"
          v-else-if="visibility === 'INTERNAL'">
        <i class="fa fa-building" />
    </span>
    <span class="icon tooltip" :data-tooltip="$t('common.challenge_information.visibility.values.invite.description')"
          v-else-if="visibility === 'INVITE'">
        <i class="fa fa-user-lock" />
    </span>
</span>
</template>
<script>
    export default {
        name: 'DiamantChallengeVisibilityIndicator',
        props: {
            visibility: String
        }
    }
</script>
