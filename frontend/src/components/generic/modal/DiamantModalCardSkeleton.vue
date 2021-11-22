<template>
    <div class="modal-card">
        <header class="modal-card-head">
            <p class="modal-card-title">{{title}}</p>
        </header>
        <section class="modal-card-body">
            <slot />
        </section>
        <footer class="modal-card-foot">
            <button class="button" @click="$emit('cancel')">
                {{$t('common.cancel')}}
            </button>
            <button class="button is-success" @click="$emit('confirm')">
                {{$t('common.save')}}
            </button>
        </footer>
    </div>
</template>

<script>
    export default {
        name: "DiamantModalCardSkeleton",
        props: {
            title: {
                type: String
            }
        }
    }
</script>

<style scoped>
    .modal-card-foot {
        justify-content: flex-end;
        background-color: white;
        border-top: 0;
    }
</style>
