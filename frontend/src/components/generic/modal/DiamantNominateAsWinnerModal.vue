<template>
    <div class="modal" :class="visible ? 'is-active' : ''">
        <div class="modal-background"></div>
        <div class="modal-card">
            <header class="modal-card-head">
                <p class="modal-card-title">{{$t('pages.ideas.actions.nominate_as_winner.modal.title')}}</p>
            </header>
            <section class="modal-card-body">
                <p>{{$t('pages.ideas.actions.nominate_as_winner.modal.description')}}</p>
                <div class="field">
                    <label class="label sr-only" for="winning_state">{{$t('pages.ideas.actions.nominate_as_winner.modal.options.label')}}</label>
                    <div class="control is-expanded">
                        <div class="select is-fullwidth">
                            <select v-model="winningState" id="winning_state">
                                <option value="NOT_WON">{{$t('pages.ideas.actions.nominate_as_winner.modal.options.no')}}</option>
                                <option value="WINNER_1">{{$t('pages.ideas.actions.nominate_as_winner.modal.options.first')}}</option>
                                <option value="WINNER_2">{{$t('pages.ideas.actions.nominate_as_winner.modal.options.second')}}</option>
                                <option value="WINNER_3">{{$t('pages.ideas.actions.nominate_as_winner.modal.options.third')}}</option>
                            </select>
                        </div>
                    </div>
                </div>
            </section>
            <footer class="modal-card-foot">
                <button class="button" @click="hide">
                    {{$t('common.close')}}
                </button>
                <button class="button is-primary" @click="save">
                    {{$t('common.save')}}
                </button>
            </footer>
        </div>
    </div>
</template>

<script>
    import {APIService} from "../../../until/APIService";

    export default {
        name: "DiamantNominateAsWinnerModal",
        data: function () {
            return {
                visible: false,
                winningState: 0
            };
        },
        methods: {
            show: function(currentPlace) {
                this.visible = true;
                this.winningState = 'NOT_WON';
                if(currentPlace === 1){
                    this.winningState ='WINNER_1';
                }
                if(currentPlace === 2){
                    this.winningState ='WINNER_2';
                }
                if(currentPlace === 3){
                    this.winningState ='WINNER_3';
                }
            },
            hide: function () {
                this.visible = false;
            },
            save: function() {
                APIService.updateIdeaWinningState(this.$route.params.challengeId, this.$route.params.ideaId, this.winningState)
                    .then(() => {
                        this.$notify({
                            title: this.$t('common.notification.success.title'),
                            text: this.$t('common.notification.success.saved'),
                            type: 'success'
                        });
                        this.hide();
                    })
                    .then(() => this.$emit('saved'))
                    .catch(function () {
                        this.$notify({
                            title: this.$t('common.errors.title'),
                            text: this.$t('common.errors.updated_failed'),
                            type: 'error'
                        })
                    }.bind(this));
            },
        }
    }
</script>

<style scoped>
    .modal-card-foot {
        justify-content: flex-end;
    }
</style>
