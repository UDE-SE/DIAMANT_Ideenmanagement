<template>
    <div class="file has-name is-boxed is-centered" @click="downloadFile" @touchstart="(e) => e.stopPropagation()">
        <label class="file-label">
            <span class="file-cta">
                <span class="button is-loading" v-if="isDownloading">

                </span>
                <span class="file-icon is-loading" v-else>
                    <i class="fas fa-file" />
                </span>
            </span>
            <span class="file-name">
              {{element.fileName}}
            </span>
        </label>
    </div>
</template>

<script>
    import {APIService} from "../../../until/APIService";

    export default {
        name: "DownloadableFile",
        props: ['element'],
        data: function() {
            return {
                isDownloading: false
            }
        },
        methods: {
            downloadFile: function () {
                if(! this.isDownloading) {
                    this.isDownloading = true;
                    APIService
                        .downloadAttachment(this.element.content, this.element.fileName)
                        .then(function () {
                                this.isDownloading = false;
                            }.bind(this)
                        )
                }
            }
        }
    }
</script>

<style scoped>

</style>
