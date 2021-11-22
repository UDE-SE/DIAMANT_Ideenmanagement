<template>
    <div>
        <idea-canvas :elements="innerCanvasElements" v-show-error-on-div-directive="formErrors" data-error-field-id="canvasElements"/>
    </div>
</template>

<script>
    import IdeaCanvas from "../../newIdea/IdeaCanvas";
    import ShowErrorOnDivDirective from "../../generic/ShowErrorOnDivDirective";
    import {APIService} from "../../../until/APIService";
    export default {
        name: "EditCanvasView",
        components: {IdeaCanvas},
        directives: {
            ShowErrorOnDivDirective,
        },
        props: ["canvasElements"],
        data: function () {
            return {
                innerCanvasElements: [],
                formErrors: []
            }
        },
        created() {
            this.innerCanvasElements = JSON.parse(JSON.stringify(this.canvasElements));
            this.formErrors = [];
        },
        methods: {
            save: function (finishEditMode, loadIdeaFunction) {
                let challengeId = this.$route.params.challengeId;
                let ideaId = this.$route.params.ideaId;
                let increaseProgressbar = this.$modal.increaseStepCounter;
                return this.$modal.showProgressScreen(this.$t('pages.ideas.new.save_modal'), (this.innerCanvasElements.length))
                    .then(() => APIService.updateCanvas(challengeId, ideaId, this.innerCanvasElements, increaseProgressbar))
                    .then(() => {
                        this.$notify({
                            title: this.$t('common.notification.success.title'),
                            text: this.$t('common.notification.success.saved'),
                            type: 'success'
                        });
                    })
                    .then(loadIdeaFunction)
                    .then(finishEditMode)
                    .catch(function (error) {
                        if (error && error.status === 400) {
                            this.formErrors = error.data.errors;
                            console.log(this.formErrors)
                        } else {
                            console.log(error);
                            this.$notify({
                                title: this.$t('common.errors.title'),
                                text: this.$t('common.errors.updated_failed'),
                                type: 'error'
                            })
                        }
                    }.bind(this))
                    .finally(() => this.$modal.hide())
            },
        }
    }
</script>

<style scoped>

</style>
