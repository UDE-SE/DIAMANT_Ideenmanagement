<template>
    <section class="section">
        <div class="container">
            <div class="has-text-centered">
                <h1 class="title">404</h1>
                <h2 class="subtitle">{{$t('common.errors.not_found')}}</h2>
            </div>
        </div>
    </section>
</template>
<script>
    export default {
        name: "NotFound"
    }
</script>
<style scoped>
    img {
        max-width: 100vh;
        max-height: 100vw;
    }
</style>
