<template>
    <div class="card is-fullheight">
        <header class="card-header is-unselectable is-clickable" @click="toggleVisibility" v-if="hasHeader">
            <p class="card-header-title">
            <span v-show="iconClass != null">
                <span class="icon">
                    <i :class="iconClass" aria-hidden="true" />
                </span>
                <span>&nbsp;</span>
            </span>
                {{title}}
            </p>
            <span class="card-header-icon" aria-label="show content">
          <span class="icon">
            <i class="fas" :class="contentVisible ? 'fa-angle-up' : 'fa-angle-down'" aria-hidden="true" />
          </span>
        </span>
        </header>
        <slot />
    </div>
</template>

<script>
    export default {
        name: "CollapsibleCardSkeletonWithOnlyHeader",
        props: {
            title: String,
            iconClass: String,
            hasHeader: {
                type: Boolean,
                default: true
            },
        },
        data: function () {
            return {
                contentVisible: true
            }
        },
        methods: {
            toggleVisibility() {
                let newVisibility = ! this.contentVisible;
                this.$emit('headerclick', newVisibility);
                this.contentVisible = newVisibility;
            }
        },
    }
</script>

<style scoped lang="scss">
    @import "../../config/bulmaConfig";
    .is-clickable {
        cursor: pointer !important;
    }
    .margin-bottom {
        margin-bottom: 10px;
    }
</style>
