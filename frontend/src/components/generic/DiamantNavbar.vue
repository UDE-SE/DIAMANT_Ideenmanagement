<template>
    <nav class="navbar has-shadow" role="navigation" aria-label="main navigation">
        <div class="container">
            <div class="navbar-brand">
                <router-link to="/" class="navbar-item">
                    <img src="../../assets/logo.png" alt="logo">
                    <span class="subtitle">
                        DIAMANT
                    </span>
                </router-link>
            </div>
            <div class="navbar-menu is-active">
                <div class="navbar-end">
                    <div class="navbar-item has-dropdown is-hoverable" v-show="$keycloak.authenticated">
                        <div class="navbar-link">
                            {{$keycloak.fullName || $keycloak.userName }}
                        </div>
                        <div class="navbar-dropdown ">
                            <a class="navbar-item" @click="logout">
                                <div>
                              <span class="icon is-small">
                                <i class="fa fa-sign-out-alt"></i>
                              </span>
                                    &nbsp;{{$t('components.navbar.actions.logout')}}
                                </div>
                            </a>
                        </div>
                    </div>
                    <div class="navbar-item" v-show="! $keycloak.authenticated">
                        <div class="buttons">
                            <a class="button is-primary" @click="$keycloak.register()">
                                {{$t('components.navbar.actions.register')}}
                            </a>
                            <a class="button is-primary" @click="$keycloak.login()">
                                {{$t('components.navbar.actions.login')}}
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </nav>
</template>
<script>
    export default {
        name: 'DiamantNavbar',
        methods: {
            logout() {
                this.$router.push({name: 'dashboard'});
                this.$keycloak.logoutFn()
            }
        }
    }
</script>
