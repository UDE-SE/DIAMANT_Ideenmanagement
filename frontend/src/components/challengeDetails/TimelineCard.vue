<template>
    <div class="card is-fullheight">
        <header class="card-header is-unselectable">
            <p class="card-header-title">
                {{$t('common.challenge_information.news')}}
            </p>
        </header>
        <div class="card-content">
            <div class="content">
                <challenge-detail-timeline :news="news" />
            </div>
        </div>
        <footer class="card-footer" v-if="canPostUpdates">
            <div class="column">
                <div class="field is-horizontal">
                    <div class="field-label is-normal">
                        <label class="label" for="type">{{$t('pages.challenge_details_page.timeline.type.label')}}</label>
                    </div>
                    <div class="field-body">
                        <div class="field ">
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select id="type" v-model="type">
                                        <option value="UPDATE">{{$t('pages.challenge_details_page.timeline.type.values.update')}}</option>
                                        <option value="PLANING">{{$t('pages.challenge_details_page.timeline.type.values.planing')}}</option>
                                        <option value="PILOTING">{{$t('pages.challenge_details_page.timeline.type.values.piloting')}}</option>
                                        <option value="SUCCESS">{{$t('pages.challenge_details_page.timeline.type.values.success')}}</option>
                                        <option value="NOT_IMPLEMENTED">{{$t('pages.challenge_details_page.timeline.type.values.not_implemented')}}</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="field is-horizontal" v-show-error-directive="formErrors">
                    <div class="field-label">
                        <label class="label" for="date">{{$t('pages.challenge_details_page.timeline.date')}}</label>
                    </div>
                    <div class="field-body">
                        <div class="field is-fullwidth">
                            <div class="control">
                                <diamant-date-picker v-model="date" id="date" data-error-field-id="date" />
                            </div>
                        </div>
                    </div>
                </div>
                <div class="field is-horizontal" v-show-error-directive="formErrors">
                    <div class="field-label">
                        <label class="label" for="content">{{$t('pages.challenge_details_page.timeline.content')}}</label>
                    </div>
                    <div class="field-body">
                        <div class="field is-fullwidth">
                            <div class="control">
                                <textarea class="textarea" id="content" rows="2" v-model="content"></textarea>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="field is-grouped is-grouped-right">
                    <p class="control">
                        <a class="button is-primary" @click="sendMessage()">
                            <span class="icon">
                                <i class="fas fa-envelope-open-text"></i>
                            </span>
                            <span>{{$t('pages.challenge_details_page.timeline.send')}}</span>
                        </a>
                    </p>
                </div>
            </div>
        </footer>
    </div>
</template>

<script>
    import ChallengeDetailTimeline from "./ChallengeDetailTimeline";
    import DiamantDatePicker from "./edit/DiamantDatePicker";
    import {APIService} from "../../until/APIService";
    import ShowErrorDirective from "../generic/ShowErrorDirective";

    export default {
        name: "TimelineCard",
        components: {DiamantDatePicker, ChallengeDetailTimeline},
        directives: {ShowErrorDirective},
        props: ["news", "canPostUpdates"],
        data: function () {
            return {
                type: 'UPDATE',
                date: new Date().getTime(),
                content: '',
                formErrors: []
            }
        },
        methods: {
            sendMessage() {
                let newItem = {
                    date: this.date,
                    content: this.content,
                    type: this.type
                };
                let challengeId = this.$route.params.id;
                return APIService.addNewsItem(challengeId, newItem)
                    .then(() => {
                        this.$notify({
                            title: this.$t('common.notification.success.title'),
                            text: this.$t('common.notification.success.saved'),
                            type: 'success'
                        })
                    })
                    .then(() => this.$emit('send'))
                    .catch(function (error) {
                        if (error && error.status === 400) {
                            this.formErrors = error.data.errors;
                        } else {
                            console.log(error);
                            this.$notify({
                                title: this.$t('common.errors.title'),
                                text: this.$t('common.errors.updated_failed'),
                                type: 'error'
                            })
                        }
                    }.bind(this));
            }
        }
    }
</script>

<style scoped>

</style>
