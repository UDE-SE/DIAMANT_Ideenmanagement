<template>
    <div class="full-height">
        <diamant-navbar/>
        <notifications/>
        <div id="app">
            <router-view/>
        </div>
        <diamant-modal/>
        <footer class="footer">
            <div class="content has-text-centered">
                <p>
                    &copy; 2020 Universität Duisburg-Essen | Fakultät für Wirtschaftswissenschaften <span class="is-hidden">Version {{version}}</span>
                </p>
            </div>
        </footer>
    </div>
</template>

<style lang="scss">
    @import '~easymde/dist/easymde.min.css';
    @import "config/bulmaConfig";

    .modal-card .modal-card-body.is-titleless {
        border-top-left-radius: 6px;
        border-top-right-radius: 6px;
    }

    .is-danger-control {
        border: 1px solid $red;
        border-radius: 4px;
    }

    .is-flex-grow-1 {
        flex: 1;
    }

    .is-flex-columns {
        flex-direction: column;
    }

    .card-header {
        box-shadow: 0 1px $primary !important;
    }

    .is-fullheight {
        height: 100% !important;
    }

    .is-clickable {
        cursor: pointer !important;
    }

    #app > .section {
        padding-left: 0;
        padding-right: 0;
    }

    .modal {
        max-width: 100vw;
    }

    .full-height {
        display: flex;
        min-height: 100vh;
        flex-direction: column;
    }

    #app {
        flex: 1;
    }

</style>
<script>
    import DiamantNavbar from "./components/generic/DiamantNavbar";

    export default {
        components: {DiamantNavbar},
        computed: {
            version: function () {
                return process.env.VUE_APP_VERSION;
            }
        }
    }
</script>
