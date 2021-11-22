<template>
    <span :aria-label="value">
        <template v-if="value != null">
            <span class="icon is-small has-text-success" v-for="i in roundedValue" :key="i" >
                <i class="fa fa-star"/>
            </span>
        </template>
        <template v-else>
            <span> -- </span>
        </template>
    </span>
</template>

<script>
    export default {
        name: "DiamantVoteResult",
        props: {value: Number},
        computed: {
            roundedValue() {
                return Math.round(this.value);
            }
        }
    }
</script>

<style scoped>

</style>
