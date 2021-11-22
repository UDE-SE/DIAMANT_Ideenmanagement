<template>
    <section class="section" v-if="Object.keys(idea).length > 0">
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
                <div class="column is-12" v-if="idea.currentUserPermissions != null && idea.currentUserPermissions.length > 0">
                    <editable-card :has-content-class="false" :title="$t('common.state')" :is-editable="isIdeaStateEditable"
                                   :custom-edit-text="stateChangeText" :has-custom-edit-button="true" @customEdit="changeIdeaState" >
                        <idea-state-indicator :idea-state="idea.state"/>
                        <diamant-nominate-as-winner-modal ref="nominateAsWinnerModal" @saved="loadIdea()"/>
                    </editable-card>
                </div>
                <div class="column is-12" v-if="isVotingVisible">
                    <idea-voting-view :own-voting="idea.ownVoting" :average-voting="idea.averageVoting" :can-vote="canVote" :winning-place="idea.winningPlace" @saved="loadIdea"/>
                </div>
                <div class="column is-12">
                    <div class="columns">
                        <div class="column is-6">
                            <editable-card :title="$t('pages.ideas.section_labels.general_information')"
                                           @save="(hideFunction) => this.$refs.editGeneralInformationView.save(hideFunction, loadIdea)"
                                           :is-editable="isEditable">
                                <template v-slot:editView>
                                    <edit-general-information-view :team-name="idea.teamName" :short-description="idea.shortDescription" ref="editGeneralInformationView"/>
                                </template>
                                <template v-slot:default>
                                    <div class="content is-fullheight ">
                                        <div class="field">
                                            <label class="label"
                                                   for="teamNameVisibile">{{$t('pages.ideas.section_labels.team_name')}}:</label>
                                            <div class="control">
                                                <input class="input" type="text" id="teamNameVisibile" :value="idea.teamName" readonly>
                                            </div>
                                        </div>
                                        <div class="field">
                                            <label class="label" for="shortDescriptionVisibile">{{$t('pages.ideas.section_labels.short_description')}}:</label>
                                            <div class="control has-padding">
                                                <textarea class="textarea" readonly id="shortDescriptionVisibile"
                                                          v-model="idea.shortDescription"/>
                                            </div>
                                        </div>
                                    </div>
                                </template>
                            </editable-card>
                        </div>
                        <div class="column is-6">
                            <editable-card :title="$t('pages.ideas.section_labels.team_members')"
                                           @save="(hideFunction) => this.$refs.editTeamView.save(hideFunction, loadIdea)"
                                           :is-editable="isEditable" :has-add-button-in-header-during-edit="true"
                                           :has-custom-add-text="$t('common.invite')"
                                           @add="() => this.$refs.editTeamView.showDisplayInvitationLinkDialog()">
                                <template v-slot:editView>
                                    <edit-team-view :persons="idea.teamMember" ref="editTeamView"
                                        :invitation-link="idea.invitationLinkTeammember" :challenge-visibility="idea.challengeVisibility"/>
                                </template>
                                <template v-slot:default>
                                    <diamant-user-list :persons="idea.teamMember" :show-delete-button="false" class="is-fullheight" :has-incognito-indicator="true"/>
                                </template>
                            </editable-card>
                        </div>
                    </div>
                </div>
                <div class="column is-12">
                    <editable-card :title="$t('pages.ideas.section_labels.details')"
                                   @save="(hideFunction) => this.$refs.editCanvasView.save(hideFunction, loadIdea)"
                                   :is-editable="isEditable">
                        <template v-slot:editView>
                            <edit-canvas-view :canvas-elements="idea.canvasElements" ref="editCanvasView" />
                        </template>
                        <template v-slot:default>
                            <div class="canvas-elements box is-relative">
                                <template v-for="(element, index) of idea.canvasElements">
                                    <canvas-element :element="element" :key="index"/>
                                </template>
                            </div>
                        </template>
                    </editable-card>
                </div>
                <div class="column is-12">
                    <chat-card type="TEAM" :messages="idea.teamChat" @send="(m) => sendChatMessage(m, 'TEAM')" v-if="idea.teamChat != null" />
                </div>
                <div class="column is-12">
                    <chat-card type="REFEREES" :messages="idea.refereesChat" @send="(m) => sendChatMessage(m, 'REFEREES')" v-if="idea.refereesChat != null" />
                </div>
                <div class="column is-12">
                    <chat-card type="TEAM_AND_REFEREES" :messages="idea.teamAndRefereesChat" @send="(m) => sendChatMessage(m, 'TEAM_AND_REFEREES')" v-if="idea.teamAndRefereesChat != null && idea.state !== 'DRAFT'" />
                </div>
            </div>
        </div>
    </section>
    <section v-else>
        <div class="container">
            <h2 class="subtitle">{{currentState}}</h2>
        </div>
    </section>
</template>

<script>
    import {APIService} from "../until/APIService";
    import CanvasElement from "../components/ideasDetails/canvas/CanvasElement";
    import IdeaStateIndicator from "../components/ideasDetails/IdeaStateIndicator";
    import EditableCard from "../components/generic/EditableCard";
    import DiamantUserList from "../components/generic/DiamantUserList";
    import ChatCard from "../components/ideasDetails/ChatCard";
    import EditGeneralInformationView from "../components/ideasDetails/edit/EditGeneralInformationView";
    import EditTeamView from "../components/ideasDetails/edit/EditTeamView";
    import IdeaVotingView from "../components/ideasDetails/IdeaVotingView";
    import EditCanvasView from "../components/ideasDetails/edit/EditCanvasView";
    import DiamantNominateAsWinnerModal from "../components/generic/modal/DiamantNominateAsWinnerModal";

    export default {
        name: "IdeaDetailsPage",
        components: {
            DiamantNominateAsWinnerModal,
            EditCanvasView,
            IdeaVotingView,
            EditTeamView,
            EditGeneralInformationView,
            ChatCard,
            DiamantUserList,
            EditableCard, IdeaStateIndicator, CanvasElement},
        data: function () {
            return {
                currentState: '...',
                idea: {}
            }
        },
        methods: {
            loadIdea() {
                this.currentState = this.$t('components.modal.loading_screen.text');
                return this.$modal.showLoadingScreen()
                    .then(() => APIService.getIdea(this.$route.params.challengeId, this.$route.params.ideaId))
                    .then((res) => this.idea = res)
                    .catch(
                        function () {
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
            downloadAttachment(url, name) {
                APIService.downloadAttachment(url, name);
            },
            changeIdeaState() {
                if('DRAFT' === this.idea.state) {
                    this.$modal.show({
                        title: this.$t('pages.ideas.actions.submit.modal.title'),
                        text: this.$t('pages.ideas.actions.submit.modal.description'),
                        onConfirm: () => this.updateStateOfIdea('SUBMITTED')
                    });
                } else if('SUBMITTED' === this.idea.state) {
                    this.$modal.show({
                        title: this.$t('pages.ideas.actions.nominate_for_vote.modal.title'),
                        text: this.$t('pages.ideas.actions.nominate_for_vote.modal.description'),
                        onConfirm: () => this.updateStateOfIdea('READY_FOR_VOTE')
                    })
                } else if(this.idea.currentUserPermissions != null && this.idea.currentUserPermissions.includes('NOMINATE_AS_WINNER')) {
                    this.$refs.nominateAsWinnerModal.show(this.idea.winningPlace);
                }
            },
            updateStateOfIdea(newState){
                APIService.updateStateOfIdea(this.$route.params.challengeId, this.$route.params.ideaId, newState)
                    .then(() => {
                        this.$notify({
                            title: this.$t('common.notification.success.title'),
                            text: this.$t('common.notification.success.saved'),
                            type: 'success'
                        });
                        this.loadIdea()
                    })
                    .catch(function () {
                        this.$notify({
                            title: this.$t('common.errors.title'),
                            text: this.$t('common.errors.updated_failed'),
                            type: 'error'
                        })
                    }.bind(this));
            },
            sendChatMessage(message, type){
                APIService.sendChatMessage(this.$route.params.challengeId, this.$route.params.ideaId, type, message)
                    .then((messages) => {
                        this.$notify({
                            title: this.$t('common.notification.success.title'),
                            text: this.$t('common.notification.success.saved'),
                            type: 'success'
                        });
                        switch (type) {
                            case 'TEAM':
                                this.idea.teamChat = messages;
                                break;
                            case 'REFEREES':
                                this.idea.refereesChat = messages;
                                break;
                            case 'TEAM_AND_REFEREES':
                                this.idea.teamAndRefereesChat = messages;
                                break;
                        }
                    })
                    .catch(function () {
                        this.$notify({
                            title: this.$t('common.errors.title'),
                            text: this.$t('common.errors.send_message_failed'),
                            type: 'error'
                        })
                    }.bind(this));
            },
        },
        computed: {
            isEditable() {
                return this.idea.currentUserPermissions != null && this.idea.currentUserPermissions.includes('EDIT');
            },
            canVote() {
                return this.idea.currentUserPermissions != null && this.idea.currentUserPermissions.includes('VOTE');
            },
            isVotingVisible(){
                return this.canVote || this.idea.averageVoting != null;
            },
            isIdeaStateEditable(){
                return !!(this.idea.currentUserPermissions != null &&
                    (
                        ('DRAFT' === this.idea.state && this.idea.currentUserPermissions.includes('EDIT')) ||
                        ('SUBMITTED' === this.idea.state && this.idea.currentUserPermissions.includes('CHANGE_STATE_TO_READY_FOR_VOTE')) ||
                        this.idea.currentUserPermissions.includes('NOMINATE_AS_WINNER')
                    ));
            },
            stateChangeText() {
                if(this.idea.currentUserPermissions == null){
                    return null;
                }
                if('DRAFT' === this.idea.state && this.idea.currentUserPermissions.includes('EDIT')){
                    return this.$t('pages.ideas.actions.submit.label');
                }
                else if('SUBMITTED' === this.idea.state && this.idea.currentUserPermissions.includes('CHANGE_STATE_TO_READY_FOR_VOTE')){
                    return this.$t('pages.ideas.actions.nominate_for_vote.label')
                }
                else if(this.idea.currentUserPermissions.includes('NOMINATE_AS_WINNER')){
                    return this.$t('pages.ideas.actions.nominate_as_winner.label')
                }
                return null;
            },
        },
        created() {
            this.loadIdea();
            let sseEventSource = APIService.getSseEventSource(this.$route.params.challengeId, this.$route.params.ideaId);
            sseEventSource.onmessage = function(event){
                if(event.data === 'TEAM_CHAT' && this.idea.teamChat != null) {
                    APIService.getChat(this.$route.params.challengeId, this.$route.params.ideaId, 'team')
                        .then((resp) => this.idea.teamChat = resp)
                }
                if(event.data === 'REFEREES_CHAT' && this.idea.refereesChat != null) {
                    APIService.getChat(this.$route.params.challengeId, this.$route.params.ideaId, 'referees')
                        .then((resp) => this.idea.refereesChat = resp);
                }
                if(event.data === 'TEAM_AND_REFEREES_CHAT' && this.idea.teamAndRefereesChat != null) {
                    APIService.getChat(this.$route.params.challengeId, this.$route.params.ideaId, 'teamandreferees')
                        .then((resp) => this.idea.teamAndRefereesChat = resp)
                }
            }.bind(this);
        },
    }
</script>

<style scoped>
    .canvas-elements {
        min-height: 500px;
        resize: vertical;
        overflow: auto;
    }
</style>
