<template>
    <div>
        <diamant-markdown-it-editor  v-model="editedText"
                                     v-show-error-on-div-directive="formErrors"
                                     data-error-field-id="newValueDTO"/>
    </div>
</template>

<script>
    import ShowErrorOnDivDirective from "../../generic/ShowErrorOnDivDirective";
    import {APIService} from "../../../until/APIService";
    import DiamantMarkdownItEditor from "../../generic/DiamantMarkdownItEditor";

    export default {
        name: "UpdateTextView",
        components: {DiamantMarkdownItEditor},
        directives: {
            ShowErrorOnDivDirective,
        },
        props: ["text", "type"],
        data: function () {
            return {
                editedText: [],
                formErrors: []
            }
        },
        created() {
            this.editedText = this.text;
            this.formErrors = [];
        },
        methods: {
            save: function (finishEditMode, loadFunction) {
                let challengeId = this.$route.params.id;
                let personType = this.type === 'awards' ? 'AWARDS' : 'DESCRIPTION';
                return APIService.updateText(challengeId, personType, this.editedText)
                    .then(() => {
                        this.$notify({
                            title: this.$t('common.notification.success.title'),
                            text: this.$t('common.notification.success.saved'),
                            type: 'success'
                        });
                    })
                    .then(finishEditMode)
                    .then(loadFunction)
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
            },
        }
    }
</script>
