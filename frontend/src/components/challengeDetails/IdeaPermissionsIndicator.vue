<template>
    <span class="column is-narrow">
        <span class="icon tooltip" :data-tooltip="$t('common.idea.permissions.values.edit.description')" v-if="permission != null && permission.includes('EDIT')">
            <i class="fa fa-edit"/>
        </span>
        <span class="icon tooltip" :data-tooltip="$t('common.idea.permissions.values.change_state.description')"
              v-if="permission != null && permission.includes('CHANGE_STATE_TO_READY_FOR_VOTE')">
            <i class="fas fa-check"/>
        </span>
        <span class="icon tooltip" :data-tooltip="$t('common.idea.permissions.values.vote.description')"
              v-if="permission != null && permission.includes('VOTE')">
            <i class="fa fa-star"/>
        </span>
        <span class="icon tooltip" :data-tooltip="$t('common.idea.permissions.values.referee.description')"
              v-if="permission != null && permission.includes('REFEREE')">
            <i class="fa fa-eye"/>
        </span>
        <span class="icon tooltip" :data-tooltip="$t('common.idea.permissions.values.nominate_as_winner.description')"
              v-if="permission != null && permission.includes('NOMINATE_AS_WINNER')">
            <i class="fas fa-medal"/>
        </span>
    </span>
</template>

<script>
    export default {
        name: "IdeaPermissionsIndicator",
        props: ['permission']
    }
</script>

<style scoped>

</style>
