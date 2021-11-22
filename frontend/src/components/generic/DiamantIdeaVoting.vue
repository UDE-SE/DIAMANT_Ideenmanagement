<template>
    <fieldset class="starability-heartbeat" v-if="isEditable">
        <input type="radio" id="no-rate" class="input-no-rate" name="rating" value="0" checked="checked" aria-label="No rating."/>
        <template v-for="item in 5">
            <input type="radio" :id="'rating-'+ item" name="rating" :value="item" :checked="item === value" @change="$emit('changed', item)" :key="'input-' + item">
            <label class="touchable" :for="'rating-'+ item" :title="item" :key="'label-' + item">{{item}}</label>
        </template>
    </fieldset>
    <p class="starability-result" :data-rating="roundedValue" v-else />
</template>

<script>
    export default {
        name: "DiamantIdeaVoting",
        props: {
            isEditable: Boolean,
            value: Number
        },
        computed: {
            roundedValue() {
                return Math.round(this.value);
            }
        }
    }
</script>

<style scoped type="scss">
    @import "~starability/starability-css/starability-all.css";

</style>
