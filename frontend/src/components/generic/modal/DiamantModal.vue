<template>
    <div class="modal" :class="visible ? 'is-active' : ''">
        <div class="modal-background"></div>
        <template v-if="type === 'confirm'">
            <div class="modal-card">
                <header class="modal-card-head has-background-danger">
                    <p class="modal-card-title has-text-white">{{title}}</p>
                </header>
                <section class="modal-card-body">
                    {{text}}
                </section>
                <footer class="modal-card-foot">
                    <button class="button" @click="hide">
                        {{$t('common.no')}}
                    </button>
                    <button class="button is-danger" @click="confirm">
                        {{$t('common.yes')}}
                    </button>
                </footer>
            </div>
        </template>
        <template v-else-if="type === 'loading'">
            <div class="modal-card">
                <div class="modal-content">
                    <div class="section">
                        <diamant-spinner :color="'white'"></diamant-spinner>
                        <h1 class="has-text-white has-text-centered subtitle">
                            {{$t('components.modal.loading_screen.text')}}
                        </h1>
                    </div>
                </div>
            </div>>
        </template>
        <template v-else-if="type === 'progress'">
            <div class="modal-card">
                <header class="modal-card-head"></header>
                <section class="modal-card-body">
                    <div class="section">
                        <diamant-spinner></diamant-spinner>
                        <h1 class="has-text-centered subtitle">
                            {{text}}
                        </h1>
                        <progress class="progress is-primary is-large" :value="currentStep" :max="steps">{{currentStep / steps}}%</progress>
                    </div>
                </section>
                <footer class="modal-card-foot">
                </footer>
            </div>
        </template>
        <template v-else-if="type === 'textInput'">
            <diamant-modal-card-skeleton :title="title" @cancel="hide" @confirm="confirm">
                <diamant-markdown-it-editor v-model="text" />
            </diamant-modal-card-skeleton>
        </template>
    </div>
</template>


<script>
    import {events} from "./events";
    import DiamantSpinner from "./DiamantSpinner";
    import DiamantMarkdownItEditor from "../DiamantMarkdownItEditor";
    import DiamantModalCardSkeleton from "./DiamantModalCardSkeleton";

    export default {
        name: "DiamantModal",
        components: {DiamantModalCardSkeleton, DiamantMarkdownItEditor, DiamantSpinner},
        data() {
            return {
                visible: false,
                title: '',
                text: '',
                steps: 0,
                currentStep: 0,
                type: "confirm",
                onConfirm: {}
            }
        },
        methods: {
            hide() {
                this.visible = false;
            },
            confirm() {
                // we must check if this.onConfirm is function
                if(typeof this.onConfirm === 'function') {
                    this.onConfirm();
                    this.hide();
                } else {
                    this.hide();
                }
            },
            show(params) {
                this.title = params.title;
                this.text = params.text;
                this.onConfirm = params.onConfirm;
                this.type =  'confirm';
                this.visible = true;
            }
        },
        beforeMount() {
            events.$on('show', this.show);
            events.$on('hide', this.hide);
            events.$on('showLoadingScreen', () => {
                this.type = 'loading';
                this.visible = true;
            });
            events.$on('showProgressScreen', (text, steps) => {
                this.type = 'progress';
                this.text = text;
                this.steps = steps;
                this.currentStep = 0;
                this.visible = true;
            });
            events.$on('showTextInput', (params) => {
                this.type = 'textInput';
                this.text = params.text;
                this.title = params.title;
                this.onConfirm = () => params.callback(this.text);
                this.visible = true;
            });
            events.$on('increaseStepCounter', () => {
                this.currentStep++;
            });
        }
    }
</script>

<style scoped>
    .modal-card-head {
    }
    .modal-card-foot {
        justify-content: flex-end;
        background-color: white;
        border-top: 0;
    }
</style>
