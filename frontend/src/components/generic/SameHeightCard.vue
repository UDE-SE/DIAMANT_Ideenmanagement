<template>
    <div class="card has-equal-height">
        <header class="card-header">
            <span class="card-header-title">
                <slot name="header-text" />
            </span>
            <span class="card-header-icon" >
                <slot name="header-icon" />
            </span>
        </header>
        <div class="card-content">
            <slot />
        </div>
        <footer class="card-footer" v-if="!! $slots.footer" :class="hasNoFooterBorder ? 'is-borderless-top' : ''">
            <slot name="footer" />
        </footer>
    </div>
</template>

<script>
    export default {
        name: "SameHeightCard",
        props: ['hasNoFooterBorder']
    }
</script>

<style scoped lang="scss">

    .has-equal-height {
        display: flex;
        flex-direction: column;
        height: 100%;
    }

    .has-equal-height .card-footer{
        margin-top: auto;
    }

    .has-equal-height .card-header-title{
        white-space: nowrap;
        text-overflow: ellipsis;
        overflow: hidden;
        display: inherit;
    }

    .has-equal-height .card-footer .card-footer-item.is-right {
        justify-content: flex-end;
    }

    .is-borderless-top{
        border-top: none;
    }

</style>
