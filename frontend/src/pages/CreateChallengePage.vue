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
                            <li class="is-active"><a href="#" aria-current="page">{{$t('common.navigation.challenge')}}</a></li>
                        </ul>
                    </nav>
                </div>
            </div>
            <div class="columns">
                <div class="column is-half">
                    <div class="card has-equal-height">
                        <header class="card-header">
                            <p class="card-header-title">
                                {{$t('pages.edit_challenge_page.section_labels.general_information')}}
                            </p>
                        </header>
                        <div class="card-content">
                            <edit-challenge-general-information :challenge="newChallengeObject" :form-errors="formErrors" />
                        </div>
                    </div>
                </div>
                <div class="column is-half">
                    <div class="card has-equal-height">
                        <header class="card-header">
                            <p class="card-header-title">
                                {{$t('common.challenge_information.milestones.label')}}
                            </p>
                        </header>
                        <div class="card-content">
                            <edit-challenge-mile-stones :challenge="newChallengeObject" :form-errors="formErrors" />
                        </div>
                    </div>
                </div>
            </div>
            <div class="columns">
                <div class="column">
                    <div class="card has-equal-height">
                        <header class="card-header is-unselectable ">
                            <p class="card-header-title columns">
                                <label class="column" for="referees">
                                    {{$t('pages.edit_challenge_page.section_labels.referees')}}
                                </label>
                                <label class="column is-narrow">
                                    <button class="button is-primary" @click="showRefereesInvitationLinkDialog()">
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
                                                ref="showRefereesInvitationLinkModal"
                                                :invitation-link="null"
                                                :show-not-possible-because-not-yet-saved-message="true"
                                                type="REFEREES"
                                        />
                                        <diamant-user-list v-show-error-on-div-directive="formErrors" id="referees"
                                                           :persons="newChallengeObject.referees" @remove="removeReferee" :show-delete-button="true"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="column" v-if="'INVITE' === newChallengeObject.visibility">
                    <div class="card has-equal-height">
                        <header class="card-header is-unselectable ">
                            <p class="card-header-title columns">
                                <label class="column" for="invited_users">
                                    {{$t('pages.edit_challenge_page.section_labels.invited_users')}}
                                </label>
                                <label class="column is-narrow">
                                    <button class="button is-primary" @click="showInvitedUsersInvitationLinkDialog()">
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
                                                ref="showInvitedUsersInvitationLinkModal"
                                                :invitation-link="null"
                                                :show-not-possible-because-not-yet-saved-message="true"
                                                type="INVITED_USERS"
                                        />
                                        <diamant-user-list v-show-error-on-div-directive="formErrors" id="invited_users"
                                                           :persons="newChallengeObject.invitedUsers" @remove="removeInvitedUser" :show-delete-button="true"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="columns is-multiline">
                <div class="column is-full">
                    <div class="card">
                        <header class="card-header">
                            <p class="card-header-title">
                                {{$t('pages.edit_challenge_page.section_labels.details')}}
                            </p>
                        </header>
                        <div class="card-content">
                            <div>
                                <div class="field">
                                    <label class="label" for="description">{{$t('common.challenge_information.description')}}<required-indicator/></label>
                                    <div class="control" v-show-error-on-div-directive="formErrors" id="description">
                                        <diamant-markdown-it-editor v-model="newChallengeObject.description"/>
                                    </div>
                                </div>
                                <div class="field">
                                    <label class="label">{{$t('common.challenge_information.attachments.title')}}</label>
                                    <h2  class="subtitle is-6" v-if="(! attachments) || attachments.length < 1">
                                        {{$t('common.information_not_available', {'field': $t('common.challenge_information.attachments.title')})}}
                                    </h2>
                                    <div v-else>
                                        <table class="table is-striped">
                                            <thead>
                                            <tr>
                                                <th>{{$t('common.challenge_information.attachments.filename')}}</th>
                                                <th>{{$t('common.challenge_information.attachments.delete')}}</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <tr v-for="(file, index) of attachments" :key="index">
                                                <td>{{file.name}}</td>
                                                <td class="is-narrow has-text-centered">
                                                    <a class="delete"  @click="attachments.splice(index, 1)"/>
                                                </td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="file">
                                        <label class="file-label">
                                            <input class="file-input" type="file" multiple="multiple" @change="addFiles" >
                                            <span class="file-cta">
                                              <span class="file-icon">
                                                <i class="fas fa-upload"/>
                                              </span>
                                              <span class="file-label">
                                                {{$t('common.challenge_information.attachments.add')}}
                                              </span>
                                            </span>
                                        </label>
                                    </div>
                                </div>
                                <div class="field">
                                    <label class="label" for="awards">{{$t('common.challenge_information.awards')}}<required-indicator/></label>
                                    <div class="control" v-show-error-on-div-directive="formErrors" id="awards">
                                        <diamant-markdown-it-editor v-model="newChallengeObject.awards"/>
                                    </div>
                                </div>
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
        </div>
    </section>
</template>

<script>
    import RequiredIndicator from "../components/generic/RequiredIndicator";
    import ShowErrorDirective from "../components/generic/ShowErrorDirective";
    import ShowErrorOnDivDirective from "../components/generic/ShowErrorOnDivDirective";
    import {APIService} from "../until/APIService";
    import DiamantMarkdownItEditor from "../components/generic/DiamantMarkdownItEditor";
    import EditChallengeMileStones from "../components/challengeDetails/edit/EditChallengeMileStones";
    import EditChallengeGeneralInformation from "../components/challengeDetails/edit/EditChallengeGeneralInformation";
    import DiamantUserList from "../components/generic/DiamantUserList";
    import DiamantShowInvitationLinkModal from "../components/generic/modal/DiamantShowInvitationLinkModal";

    export default {
        name: "CreateChallengePage",
        components: {
            DiamantShowInvitationLinkModal,
            DiamantUserList,
            EditChallengeGeneralInformation,
            EditChallengeMileStones,
            DiamantMarkdownItEditor,
            RequiredIndicator

        },
        directives: {
            ShowErrorDirective,
            ShowErrorOnDivDirective
        },
        data: function () {
            return {
                savedChallengeId: 'notYetSet',
                newChallengeObject: {
                    submissionStart: new Date().getTime(),
                    reviewStart: this.getDateXDaysInTheFuture(1).getTime(),
                    refactoringStart: null,
                    votingStart: this.getDateXDaysInTheFuture(2).getTime(),
                    implementationStart: this.getDateXDaysInTheFuture(3).getTime(),
                    challengeEnd: this.getDateXDaysInTheFuture(4).getTime(),
                    description: undefined,
                    awards: undefined,
                    referees: [],
                    invitedUsers: []
                },
                isRefactoringPossible: false,
                attachments: [],
                formErrors: []
            }
        },
        created() {
            this.newChallengeObject.referees.push({name: this.$keycloak.fullName, email: this.$keycloak.userName});
        },
        methods: {
            save() {
                let increaseProgressbar = this.$modal.increaseStepCounter;
                this.$modal.showProgressScreen(this.$t('pages.edit_challenge_page.save_modal'), (1 + this.attachments.length))
                .then(() => APIService.saveChallenge(this.newChallengeObject, this.attachments, increaseProgressbar))
                .then((newId) => {
                    this.savedChallengeId = newId;
                    this.$notify({
                        title: this.$t('common.notification.success.title'),
                        text: this.$t('common.notification.success.saved'),
                        type: 'success'
                    });
                    let that = this;
                    setTimeout(function() {
                        that.$router.push({name: 'challenge', params: { id: that.savedChallengeId }});
                    },1500);
                })
                .catch(function (error) {
                    if(error && error.status === 400){
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
                        this.$router.push({name: 'dashboard'});
                    }
                });
            },
            addFiles($event) {
                for (const file of $event.target.files) {
                    let maxSize = process.env.VUE_APP_MAX_ATTACHMENT_SIZE_IN_MB;
                    if(file.size <= maxSize * (1024*1024)) {
                        this.attachments.push(file);
                    } else {
                        this.$notify({
                            title: this.$t('common.errors.title'),
                            text: this.$t('common.errors.file_too_big', {'size': maxSize}),
                            type: 'error'
                        })
                    }
                }
            },
            showRefereesInvitationLinkDialog() {
                this.$refs.showRefereesInvitationLinkModal.show();
            },
            removeReferee(person, index){
                this.newChallengeObject.referees.splice(index, 1);
            },
            showInvitedUsersInvitationLinkDialog() {
                this.$refs.showInvitedUsersInvitationLinkModal.show();
            },
            removeInvitedUser(person, index){
                this.newChallengeObject.invitedUsers.splice(index, 1);
            },
            getDateXDaysInTheFuture(x){
                let result = new Date();
                result.setDate(result.getDate() + x);
                return result;
            }
        }
    }
</script>

<style scoped>

    .has-equal-height {
        display: flex;
        flex-direction: column;
        height: 100%;
    }
</style>
