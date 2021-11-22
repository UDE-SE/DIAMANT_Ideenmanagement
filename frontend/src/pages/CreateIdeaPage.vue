<template>
    <section class="section">
        <div class="container">
            <div class="columns">
                <div class="column is-12">
                    <nav class="breadcrumb" aria-label="breadcrumbs">
                        <ul>
                            <li>
                                <router-link :to="{name: 'dashboard'}">
                                    {{$t('common.navigation.dashboard')}}
                                </router-link>
                            </li>
                            <li>
                                <router-link :to="{name: 'challenge', params: {id: $route.params.challengeId}}">
                                    {{$t('common.navigation.challenge')}}
                                </router-link>
                            </li>
                            <li class="is-active"><a href="#" aria-current="page">{{$t('common.navigation.idea')}}</a></li>
                        </ul>
                    </nav>
                </div>
            </div>
            <div class="columns is-multiline">
                <div class="column is-12">
                    <div class="card has-equal-height">
                        <header class="card-header">
                            <p class="card-header-title">
                                {{$t('pages.ideas.section_labels.general_information')}}
                            </p>
                        </header>
                        <div class="card-content">
                            <div class="content">
                                <div class="field" v-show-error-directive="formErrors">
                                    <label class="label" for="teamName">{{$t('pages.ideas.section_labels.team_name')}}
                                        <required-indicator/>
                                    </label>
                                    <div class="control">
                                        <input class="input" type="text"
                                               :placeholder="$t('pages.ideas.section_labels.team_name')"
                                               id="teamName" v-model="newIdeaObject.teamName">
                                    </div>
                                </div>
                                <div class="field" v-show-error-directive="formErrors">
                                    <label class="label" for="shortDescription">{{$t('pages.ideas.section_labels.short_description')}}
                                        <required-indicator/>
                                    </label>
                                    <div class="control">
                                        <textarea class="textarea" id="shortDescription" v-model="newIdeaObject.shortDescription" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="column is-12">
                    <div class="card has-equal-height">
                        <header class="card-header is-unselectable ">
                            <p class="card-header-title columns">
                                <label class="column" for="teamMember">
                                    {{$t('pages.ideas.section_labels.team_members')}}
                                </label>
                                <label class="column is-narrow">
                                    <button class="button is-primary" @click="showInvitationLinkDialog()">
                                        {{$t('common.invite')}}
                                    </button>
                                </label>
                            </p>
                        </header>
                        <div class="card-content">
                            <div class="content">
                                <div class="field" >
                                    <div class="control">
                                        <diamant-show-invitation-link-modal
                                            ref="showInvitationLinkModal"
                                            :invitation-link="null"
                                            :show-not-possible-because-not-yet-saved-message="true"
                                            type="TEAM"
                                        />
                                        <diamant-user-list v-show-error-on-div-directive="formErrors" id="teamMember"
                                                           :persons="newIdeaObject.teamMember" @remove="removeTeamMember" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="column is-12">
                    <div class="card has-equal-height">
                        <header class="card-header">
                            <p class="card-header-title">
                                {{$t('pages.ideas.section_labels.details')}}
                                <required-indicator/>
                            </p>
                        </header>
                        <div class="card-content">
                            <idea-canvas :elements="newIdeaObject.canvasElements" v-show-error-on-div-directive="formErrors" id="canvasElements" />
                        </div>
                    </div>
                </div>
            </div>
            <div class="column is-full">
                <div class="field is-grouped is-pulled-right">
                    <p class="control">
                        <a class="button" @click="showCancelModal">
                            {{$t('common.cancel')}}
                        </a>
                    </p>
                    <p class="control">
                        <a class="button is-success" @click="save">
                            {{$t('common.save')}}
                        </a>
                    </p>
                </div>
            </div>
        </div>
    </section>
</template>

<script>
    import RequiredIndicator from "../components/generic/RequiredIndicator";
    import ShowErrorDirective from "../components/generic/ShowErrorDirective";
    import ShowErrorOnDivDirective from "../components/generic/ShowErrorOnDivDirective";
    import {APIService} from "../until/APIService";
    import IdeaCanvas from "../components/newIdea/IdeaCanvas";
    import DiamantUserList from "../components/generic/DiamantUserList";
    import DiamantShowInvitationLinkModal from "../components/generic/modal/DiamantShowInvitationLinkModal";

    export default {
        name: "CreateIdeaPage",
        components: {DiamantShowInvitationLinkModal, DiamantUserList, IdeaCanvas, RequiredIndicator},
        directives: {
            ShowErrorDirective,
            ShowErrorOnDivDirective
        },
        data: function () {
            return {
                newIdeaObject: {
                    canvasElements: [],
                    teamMember: []
                },
                showAbortDialog: false,
                showSuccessDialog: false,
                canvasObjects: [],
                formErrors: [],
                editorConfig: {
                    autoDownloadFontAwesome: false,
                    spellChecker: false
                },
            }
        },
        created() {
            this.newIdeaObject.teamMember.push({name: this.$keycloak.fullName, email: this.$keycloak.userName});
        },
        methods: {
            save() {
                let increaseProgressbar = this.$modal.increaseStepCounter;
                this.$modal.showProgressScreen(this.$t('pages.ideas.new.save_modal'), (1 + this.newIdeaObject.canvasElements.length))
                    .then(() => APIService.saveIdea(this.$route.params.challengeId, this.newIdeaObject, increaseProgressbar))
                    .then((newId) => {
                        this.$notify({
                            title: this.$t('common.notification.success.title'),
                            text: this.$t('common.notification.success.saved'),
                            type: 'success'
                        });
                        setTimeout(function() {
                            this.$router.push({name: 'idea', params: { challengeId: this.$route.params.challengeId, ideaId: newId }});
                        }.bind(this),1500);
                    })
                    .catch(function (error) {
                        if (error && error.status === 400) {
                            this.formErrors = error.data.errors;
                        } else {
                            console.log(error);
                            this.$notify({
                                title: this.$t('common.errors.title'),
                                text: this.$t(error),
                                type: 'error'
                            })
                        }
                    }.bind(this))
                    .finally(this.$modal.hideDelayed)
            },
            showCancelModal() {
                this.$modal.show({
                    title: this.$t('common.cancel'),
                    text: this.$t('common.cancel_changes'),
                    onConfirm: () => {
                        this.$router.push({name: 'challenge', params: {id: this.$route.params.challengeId}});
                    }
                });
            },
            showInvitationLinkDialog() {
                this.$refs.showInvitationLinkModal.show();
            },
            removeTeamMember(person, index){
                this.newIdeaObject.teamMember.splice(index, 1);
            },
        }
    }
</script>

<style>

</style>
