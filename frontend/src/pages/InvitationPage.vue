<template>
    <section class="section">
        <div class="container">
            <div v-if="invalidToken">
                <h1 class="title">{{$t('common.errors.title')}}</h1>
                <h2 class="subtitle">{{$t('pages.invitation.invalid_url')}}</h2>
            </div>
            <div v-else>
                <div class="card">
                    <header class="card-header">
                        <p class="card-header-title">
                            {{$t('pages.invitation.title')}}
                        </p>
                    </header>
                    <div class="card-content">
                        <div class="content">
                            <span v-if="'REFEREE' === tokenInformation.TYPE">{{$t('pages.invitation.referee', tokenInformation)}} </span>
                            <span v-if="'INVITED_USER' === tokenInformation.TYPE">{{$t('pages.invitation.invited_user', tokenInformation)}} </span>
                            <span v-if="'TEAM_MEMBER' === tokenInformation.TYPE">{{$t('pages.invitation.team_member', tokenInformation)}} </span>
                            <br />
                            <span>{{$t('pages.invitation.expiry_date', {exp: new Date(tokenInformation.exp * 1000).toLocaleString()})}}</span>
                            <br />
                            <br />
                            <article class="message is-warning" v-show="! $keycloak.authenticated">
                                <div class="message-header">
                                    <p>{{$t('pages.invitation.waring_login_title')}}</p>
                                </div>
                                <div class="message-body">
                                    <p>{{$t('pages.invitation.waring_login')}}</p>
                                    <p>{{$t('pages.invitation.waring_login_register')}}
                                        <a class="link" @click="$keycloak.register()">
                                            {{$t('components.navbar.actions.register')}}
                                        </a>
                                    </p>
                                </div>
                            </article>
                            <article class="message is-danger" v-show="errorExpired">
                                <div class="message-header">
                                    <p>{{$t('pages.invitation.error_expired.title')}}</p>
                                </div>
                                <div class="message-body">
                                    <p>{{$t('pages.invitation.error_expired.explanation', tokenInformation)}}</p>
                                </div>
                            </article>
                            <article class="message is-danger" v-show="error403">
                                <div class="message-header">
                                    <p>{{$t('pages.invitation.error_403.title')}}</p>
                                </div>
                                <div class="message-body">
                                    <p>{{$t('pages.invitation.error_403.explanation')}}</p>
                                </div>
                            </article>
                        </div>
                    </div>
                    <footer class="card-footer" v-show="$keycloak.authenticated && ! error403 && !errorExpired">
                        <a href="#" class="card-footer-item"></a>
                        <a href="#" class="card-footer-item"></a>
                        <a class="card-footer-item has-text-success"  @click="accept">
                        <span class="icon">
                            <i class="fas fa-check" />
                        </span>
                        <span>
                            {{$t('pages.invitation.accept')}}
                        </span>
                        </a>
                    </footer>
                </div>
            </div>
        </div>
    </section>
</template>

<script>
    import {APIService} from "../until/APIService";

    export default {
        name: "InvitationPage",
        data: function() {
            return {
                tokenInformation: {},
                invalidToken: true,
                error403: false,
                errorExpired: false
            }
        },
        created() {
            try {
                this.tokenInformation = this.parseJwt(this.$route.params.token);
                this.invalidToken = !this.tokenInformation.TYPE || !this.tokenInformation.CHALLENGE_ID || !this.tokenInformation.exp
                    || !this.tokenInformation.CREATOR || !this.tokenInformation.ADDITIONAL_INFORMATION;
                if(this.tokenInformation.TYPE === 'TEAM_MEMBER' && !this.tokenInformation.IDEA_ID) {
                    this.invalidToken = true;
                }
                if(new Date(this.tokenInformation.exp * 1000) < new Date()) {
                    this.errorExpired = true;
                }
            } catch (e) {
                console.log("ungültiger Token");
                console.log(e);
            }
        },
        methods: {
            parseJwt (token) {
                let base64Url = token.split('.')[1];
                let base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
                let jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                    return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
                }).join(''));

                return JSON.parse(jsonPayload);
            },
            accept() {
                this.$modal.showLoadingScreen()
                    .then(() => APIService.acceptInvitation(this.$route.params.token))
                    .then(() => {
                        this.$notify({
                            title: this.$t('common.notification.success.title'),
                            text: this.$t('pages.invitation.accept_successful'),
                            type: 'success'
                        });
                        setTimeout(function() {
                            if (this.tokenInformation.TYPE === 'TEAM_MEMBER') {
                                this.$router.push({name: 'idea', params: { challengeId: this.tokenInformation.CHALLENGE_ID, ideaId: this.tokenInformation.IDEA_ID}});
                            } else {
                                this.$router.push({name: 'challenge', params: { id: this.tokenInformation.CHALLENGE_ID }});
                            }
                        }.bind(this),500);
                    })
                    .catch(function (error) {
                        if(error.status === 403) {
                            this.error403 = true;
                        }
                        console.log(error);
                        this.$notify({
                            title: this.$t('common.errors.title'),
                            text: this.$t('common.errors.invitation_accept_failed'),
                            type: 'error'
                        })
                    }.bind(this))
                    .finally(this.$modal.hideDelayed);
            }
        },
    }
</script>

<style scoped>
    .card-footer a:not(.has-text-success) {
        color: black !important;
    }
    .card-footer .card-footer-item {
        border-right: 0;
    }
    .card-footer.is-editing .card-footer-item:nth-last-child(2), .card-footer-item:last-child {
        border-left: 1px solid #dbdbdb;
    }
</style>
