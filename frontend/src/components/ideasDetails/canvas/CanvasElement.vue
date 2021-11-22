<template>
    <canvas-element-skeleton :element="element" :is-ready-only="! isEditable" :useRandomId="useRandomId" @edit="$emit('edit')" @remove="$emit('remove')">
        <template v-if="'TEXT' === element.type">
            <div class="content is-clipped">
                <markdown-it-vue :content="element.content"/>
            </div>
        </template>
        <template v-if="'ATTACHMENT' === element.type">
            <downloadable-file :element="element" />
        </template>
        <template v-if="'IMAGE' === element.type">
            <img :src="dataURL" class="canvasElementImage" :alt="element.fileName"/>
        </template>
    </canvas-element-skeleton>
</template>

<script>
    import MarkdownItVue from "markdown-it-vue"
    import {APIService} from "../../../until/APIService";
    import CanvasElementSkeleton from "./CanvasElementSkeleton";
    import DownloadableFile from "./DownloadableFile";

    export default {
        name: "CanvasElement",
        components: {DownloadableFile, CanvasElementSkeleton, MarkdownItVue},
        props: {
            'element': {
                type: Object
            },
            isEditable: {
                type: Boolean,
                default: false
            },
            useRandomId: Boolean
        },
        data: function () {
            return {
                isActive: false,
                dataURL: undefined
            }
        },
        asyncComputed: {
            dataURL() {
                if('IMAGE' === (this.element == null ? null : this.element.type)){
                    return new Promise((resolve, reject) => {
                        let reader = new FileReader();
                        reader.onload = () => {
                            resolve(reader.result);
                        };
                        reader.onerror = reject;
                        APIService.downloadAttachmentAsBlob(this.element.content)
                            .then((blob) => {
                                reader.readAsDataURL(blob.data);
                            })
                            .catch(
                                function(error) {
                                    this.$notify({
                                        title: this.$t('common.errors.title'),
                                        text: this.$t('common.errors.attachment_loading_failed'),
                                        type: 'error'
                                    });
                                    console.error(error);
                                }.bind(this)
                            )
                    })
                }
                return Promise.resolve('no data url');
            }
        }
    }
</script>

<style scoped lang="scss">
    .canvasElementImage {
        background-repeat: no-repeat;
        background-position: 50% 50%;
        max-height: 100%;
        max-width: 100%;
        background-size: contain;
    }

    .flex-grow-1 {
        flex: 1;
    }
</style>
