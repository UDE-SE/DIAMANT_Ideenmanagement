<template>
    <section class="section" v-if="Object.keys(challenge).length > 0">
        <div class="container">
            <div class="columns is-multiline">
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
                <div class="column is-12">
                    <editable-card :has-header="false"
                                   @save="(hideFunction) => this.$refs.editGeneralInformationView.save(hideFunction, loadChallenge)"
                                   :is-editable="isEditable">
                        <template v-slot:editView>
                            <update-challenge-general-information-view :title="challenge.title" :short-description="challenge.shortDescription"
                                                                     :visibility="challenge.visibility" ref="editGeneralInformationView"/>
                        </template>
                        <template v-slot:default>
                            <div class="container">
                                <div class="columns">
                                    <div class="column">
                                        <h1 class="title columns is-mobile">
                                            <span class="column is-11">{{challenge.title}}</span>
                                            <span class="column is-1">
                                                <diamant-challenge-visibility-indicator :visibility="challenge.visibility"/>
                                            </span>
                                        </h1>
                                        <h2 class="subtitle is-italic">
                                            {{challenge.shortDescription}}
                                        </h2>
                                    </div>
                                </div>
                            </div>
                        </template>
                    </editable-card>
                </div>
                <div class="column is-12">
                    <editable-card :has-header="false" :has-content-class="false"
                                   @save="(hideFunction) => this.$refs.updateMilestonesView.save(hideFunction, loadChallenge)"
                                   :is-editable="isEditable">
                        <template v-slot:editView>
                            <update-mile-stones-view :submissionStart="challenge.submissionStart"
                                                     :reviewStart="challenge.reviewStart"
                                                     :refactoringStart="challenge.refactoringStart"
                                                     :votingStart="challenge.votingStart"
                                                     :implementationStart="challenge.implementationStart"
                                                     :challengeEnd="challenge.challengeEnd"
                                                    ref="updateMilestonesView"/>
                        </template>
                        <template v-slot:default>
                            <challenge-details-milestones
                                    :submissionStart="challenge.submissionStart"
                                    :reviewStart="challenge.reviewStart"
                                    :refactoringStart="challenge.refactoringStart"
                                    :votingStart="challenge.votingStart"
                                    :implementationStart="challenge.implementationStart"
                                    :challengeEnd="challenge.challengeEnd"
                            />
                        </template>
                    </editable-card>
                </div>
                <div class="column is-12">
                    <div class="columns">
                        <div class="column">
                            <editable-card :title="$t('pages.edit_challenge_page.section_labels.referees')"
                                           @save="(hideFunction) => this.$refs.editReferees.save(hideFunction, loadChallenge)"
                                           :is-editable="isEditable" :has-add-button-in-header-during-edit="true"
                                           :has-custom-add-text="$t('common.invite')"
                                           @add="() => this.$refs.editReferees.showDisplayInvitationLinkDialog()">
                                <template v-slot:editView>
                                    <update-persons-view :persons="challenge.referees" ref="editReferees"
                                                         type="referees"
                                                         :invitation-link="challenge.invitationLinkReferee"
                                    />
                                </template>
                                <template v-slot:default>
                                    <diamant-user-list :persons="challenge.referees" />
                                </template>
                            </editable-card>
                        </div>
                        <div class="column" v-if="'INVITE' === challenge.visibility">
                            <editable-card :title="$t('pages.edit_challenge_page.section_labels.invited_users')"
                                           @save="(hideFunction) => this.$refs.editInvitedUsers.save(hideFunction, loadChallenge)"
                                           :is-editable="isEditable" :has-add-button-in-header-during-edit="true"
                                           :has-custom-add-text="$t('common.invite')"
                                           @add="() => this.$refs.editInvitedUsers.showDisplayInvitationLinkDialog()">
                                <template v-slot:editView>
                                    <update-persons-view :persons="challenge.invitedUsers" ref="editInvitedUsers"
                                                         type="invited_users"
                                                         :invitation-link="challenge.invitationLinkInvitedUsers"
                                    />
                                </template>
                                <template v-slot:default>
                                    <diamant-user-list :persons="challenge.invitedUsers" />
                                </template>
                            </editable-card>
                        </div>
                    </div>
                </div>
                <div class="column is-12">
                    <editable-card :title="$t('common.challenge_information.description')"
                                   @save="(hideFunction) => this.$refs.updateDescription.save(hideFunction, loadChallenge)"
                                   :is-editable="isEditable" :has-content-class="true">
                        <template v-slot:editView>
                            <update-text-view :text="challenge.description" type="description" ref="updateDescription" />
                        </template>
                        <template v-slot:default>
                             <span v-if="! challenge.description">
                                {{$t('common.information_not_available', {'field': $t('common.challenge_information.description')})}}
                            </span>
                            <div v-else>
                                <markdown-it-vue :content="challenge.description"  />
                            </div>
                        </template>
                    </editable-card>
                </div>
                <div class="column is-12">
                    <editable-card :title="$t('common.challenge_information.attachments.title')" icon-class="fa fa-paperclip"
                                   @save="(hideFunction) => this.$refs.updateAttachmentsView.save(hideFunction, loadChallenge)"
                                   :is-editable="isEditable" :has-add-button-in-header-during-edit="true"
                                   @add="() => this.$refs.updateAttachmentsView.showAddFilesDialog()">
                        <template v-slot:editView>
                            <update-attachments-view :files="challenge.attachments" ref="updateAttachmentsView" />
                        </template>
                        <template v-slot:default>
                            <span v-if="(! challenge.attachments) || challenge.attachments.length < 1">
                            {{$t('common.information_not_available', {'field': $t('common.challenge_information.attachments.title')})}}
                        </span>
                            <div v-else>
                                <table class="table is-striped">
                                    <thead>
                                    <tr>
                                        <th>{{$t('common.challenge_information.attachments.filename')}}</th>
                                        <th>{{$t('common.challenge_information.attachments.download')}}</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr v-for="(attachment, index) of challenge.attachments" :key="index">
                                        <td>
                                            <a @click="downloadAttachment(attachment.url, attachment.name)">
                                                {{attachment.name}}
                                            </a>
                                        </td>
                                        <td class="is-narrow has-text-centered">
                                            <a @click="downloadAttachment(attachment.url, attachment.name)">
                                                <span class="icon">
                                                    <i class="fa fa-download" aria-hidden="true"/>
                                                </span>
                                            </a>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </template>
                    </editable-card>
                </div>
                <div class="column is-12">
                    <editable-card :title="$t('common.challenge_information.awards')" icon-class="fa fa-crown"
                                   @save="(hideFunction) => this.$refs.updateAwards.save(hideFunction, loadChallenge)"
                                   :is-editable="isEditable" :has-content-class="false">
                        <template v-slot:editView>
                            <update-text-view :text="challenge.awards" type="awards" ref="updateAwards" />
                        </template>
                        <template v-slot:default>
                             <span v-if="! challenge.awards">
                            {{$t('common.information_not_available', {'field': $t('common.challenge_information.awards')})}}
                        </span>
                            <div v-else>
                                <markdown-it-vue :content="challenge.awards" />
                            </div>
                        </template>
                    </editable-card>
                </div>
                <div class="column is-12">
                    <timeline-card :news="challenge.news" :can-post-updates="isEditable" @send="loadChallenge"/>
                </div>
                <div class="column is-12">
                    <div class="margin-bottom">
                        <div class="card">
                            <header class="card-header is-unselectable ">
                                <p class="card-header-title">
                                    {{$t('pages.challenge_details_page.section_labels.ideas')}}
                                </p>
                                <label class="card-header-icon no-vertical-padding" v-show="showAddIdeaButton()">
                                    <router-link :to="{name: 'createIdea', params: {'challengeId': $route.params.id}}" class="button is-primary">
                                          <span class="icon is-small">
                                            <i class="fa fa-plus"/>
                                          </span>
                                        <span>{{$t('pages.challenge_details_page.add_idea')}}</span>
                                    </router-link>
                                </label>
                            </header>
                            <div class="card-content">
                                <div class="content">
                                    <div v-if="challenge.ideas != null && challenge.ideas.length > 0" class="columns is-multiline">
                                        <div class="column is-one-third" v-for="(idea, index) in challenge.ideas" :key="index">
                                            <router-link :to="{name: 'idea', params: { challengeId: challenge.id, ideaId: idea.id}}">
                                                <idea-preview :idea="idea"/>
                                            </router-link>
                                        </div>
                                    </div>
                                    <div v-else>
                                        {{$t('pages.challenge_details_page.no_ideas_found')}} {{$keycloak.authenticated ? $t('pages.challenge_details_page.no_ideas_found_do_you_want_to_add_some') : ''}}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>
    <section class="section" v-else>
        <div class="container">
            <h2 class="subtitle">{{currentState}}</h2>
        </div>
    </section>
</template>

<script>
    import ChallengeDetailsMilestones from "../components/challengeDetails/ChallengeDetailsMilestones";
    import TimelineCard from "../components/challengeDetails/TimelineCard";
    import EditableCard from "../components/generic/EditableCard";
    import {APIService} from "../until/APIService";
    import DiamantChallengeVisibilityIndicator from "../components/generic/DiamantChallengeVisibilityIdicator";
    import MarkdownItVue from "markdown-it-vue"
    import IdeaPreview from "../components/challengeDetails/IdeaPreview";
    import {DateService} from "../until/DateService";
    import DiamantUserList from "../components/generic/DiamantUserList";
    import UpdateChallengeGeneralInformationView
        from "../components/challengeDetails/edit/UpdateChallengeGeneralInformationView";
    import UpdateMileStonesView from "../components/challengeDetails/edit/UpdateMileStonesView";
    import UpdatePersonsView from "../components/challengeDetails/edit/UpdatePersonsView";
    import UpdateTextView from "../components/challengeDetails/edit/UpdateTextView";
    import UpdateAttachmentsView from "../components/challengeDetails/edit/UpdateAttachmentsView";

    export default {
        name: "ChallengeDetailsPage",
        components: {
            UpdateAttachmentsView,
            UpdateTextView,
            UpdatePersonsView,
            UpdateMileStonesView,
            UpdateChallengeGeneralInformationView,
            DiamantUserList,
            DiamantChallengeVisibilityIndicator,
            EditableCard, TimelineCard, ChallengeDetailsMilestones, IdeaPreview, MarkdownItVue},
        data: function () {
            return {
                currentState: '...',
                challenge: {}
            }
        },
        methods: {
            loadChallenge() {
                this.currentState = this.$t('components.modal.loading_screen.text');
                this.$modal.showLoadingScreen()
                    .then(() => APIService.getChallenge(this.$route.params.id))
                    .then((res) => this.challenge = res)
                    .catch(
                        function() {
                            this.$notify({
                                title: this.$t('common.errors.title'),
                                text: this.$t('common.errors.loading_failed'),
                                type: 'error'
                            });
                            this.currentState = this.$t('common.errors.loading_failed');
                        }.bind(this)
                    )
                    .finally(this.$modal.hideDelayed)
            },
            downloadAttachment(url, name){
                APIService.downloadAttachment(url, name);
            },
            showAddIdeaButton(){
                let now = DateService.getDate().getTime();
                return this.$keycloak.authenticated && this.challenge.submissionStart < now  && now < this.challenge.reviewStart;
            }
        },
        computed: {
            isEditable() {
                return this.challenge != null && this.challenge.editPermission;
            },
        },
        created () {
            this.loadChallenge()
        },
    }
</script>
<style lang="scss">

    .no-vertical-padding{
        padding-bottom: 0;
        padding-top: 0;
    }
</style>

