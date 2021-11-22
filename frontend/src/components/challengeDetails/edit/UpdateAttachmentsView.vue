<template>
    <div>
        <h2  class="subtitle is-6" v-if="(! currentFiles) || currentFiles.length < 1">
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
                <tr v-for="(file, index) of currentFiles" :key="index">
                    <td>{{file.name}}</td>
                    <td class="is-narrow has-text-centered">
                        <a class="delete"  @click="currentFiles.splice(index, 1)"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <input type="file" style="visibility: hidden; position: absolute;" multiple="multiple" @change="addFiles" ref="fileChooser">
    </div>
</template>

<script>
    import {APIService} from "../../../until/APIService";

    export default {
        name: "UpdateAttachmentsView",
        props: ['files'],
        data: function () {
            return {
                currentFiles: []
            }
        },
        created() {
            this.currentFiles = [];
            for (const file of this.files) {
                this.currentFiles.push({name: file.name, id: file.url.substring(file.url.lastIndexOf('/') + 1)});
            }
            this.formErrors = [];
        },
        methods: {
            save: function (finishEditMode, loadFunction) {
                let challengeId = this.$route.params.id;
                let increaseProgressbar = this.$modal.increaseStepCounter;
                return this.$modal.showProgressScreen(this.$t('pages.edit_challenge_page.save_attachments_modal'), (this.currentFiles.length +1))
                    .then(() => APIService.updateChallengeAttachments(challengeId, this.currentFiles, increaseProgressbar))
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
            addFiles($event) {
                for (const file of $event.target.files) {
                    let maxSize = process.env.VUE_APP_MAX_ATTACHMENT_SIZE_IN_MB;
                    if(file.size <= maxSize * (1024*1024)) {
                        this.currentFiles.push(file);
                    } else {
                        this.$notify({
                            title: this.$t('common.errors.title'),
                            text: this.$t('common.errors.file_too_big', {'size': maxSize}),
                            type: 'error'
                        })
                    }
                }
            },
            showAddFilesDialog() {
                this.$refs.fileChooser.click();
            }
        }

    }
</script>

<style scoped>

</style>
