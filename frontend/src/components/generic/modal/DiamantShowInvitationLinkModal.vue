<template>
    <div class="modal" :class="visible ? 'is-active' : ''">
        <div class="modal-background"></div>
        <div class="modal-card">
            <header class="modal-card-head">
                <p class="modal-card-title">{{$t(l18nPrefix + '.title')}}</p>
            </header>
            <section class="modal-card-body">
                <div v-if="showNotPossibleBecauseNotYetSavedMessage">
                    <p>{{$t(l18nPrefix + '.not_yet_possible')}}</p>
                </div>
                <div v-else>
                    <p>{{$t(l18nPrefix + '.link_explanation')}}</p>
                    <p v-if="'TEAM' === type">
                        <em v-if="'INTERNAL' === challengeVisibility">{{$t('common.invitation.visibility_warning.internal')}}</em>
                        <em v-if="'INVITE' === challengeVisibility">{{$t('common.invitation.visibility_warning.invite')}}</em>
                    </p>
                    <p>{{$t('common.invitation.link_expiry_date', {expiryDate: expiryDate})}}</p>
                    <label class="label" for="field">{{$t('common.invitation.invitation_link')}}</label>
                    <div class="field has-addons" @click="copyToClipboard">
                        <div class="control is-expanded">
                            <input class="input" type="text"  id="field" :value="invitationLink">
                        </div>
                        <div class="control">
                            <a class="button is-primary tooltip has-tooltip-left" :data-tooltip="$t('common.invitation.copy_to_clipboard')">
                                <span class="icon is-small">
                                    <i class="fas fa-clipboard"></i>
                                </span>
                            </a>
                        </div>
                    </div>
                </div>
            </section>
            <footer class="modal-card-foot">
                <button class="button" @click="hide">
                    {{$t('common.close')}}
                </button>
            </footer>
        </div>
    </div>
</template>

<script>
    export default {
        name: "DiamantShowInvitationLinkModal",
        props: {
            type: String,
            challengeVisibility: String,
            invitationLink: String,
            showNotPossibleBecauseNotYetSavedMessage: Boolean
        },
        data: function () {
            return {
                visible: false,
            };
        },
        computed: {
            l18nPrefix: function () {
                return 'common.invitation.' + this.type.toLocaleLowerCase()
            },
            expiryDate: function () {
                if (this.invitationLink) {
                    let token = this.invitationLink.substring(this.invitationLink.lastIndexOf("/"));
                    try {
                        return new Date(this.parseJwt(token).exp * 1000).toLocaleString();
                    } catch (e) {
                        console.log(e)
                    }
                }
                return '--'
            }
        },
        methods: {
            show: function () {
                this.visible = true;
                this.firstName = '';
                this.lastName = '';
                this.email = '';
                this.formErrors = [];
            },
            hide: function () {
                this.visible = false;
            },
            parseJwt (token) {
                let base64Url = token.split('.')[1];
                let base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
                let jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                    return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
                }).join(''));

                return JSON.parse(jsonPayload);
            },
            copyToClipboard: function () {
                navigator.clipboard.writeText(this.invitationLink);
            }
        }
    }
</script>

<style scoped>
    .modal-card-foot {
        justify-content: flex-end;
    }
</style>
