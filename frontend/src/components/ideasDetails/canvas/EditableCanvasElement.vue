<template>
    <canvas-element-skeleton :element="element" :is-ready-only="false" :useRandomId="useRandomId" @edit="$emit('edit')" @remove="$emit('remove')" >
        <template v-if="'TEXT' === element.type">
            <div class="content is-clipped">
                <markdown-it-vue :content="element.content"/>
            </div>
        </template>
        <template v-if="'ATTACHMENT' === element.type">
            <div class="file has-name is-boxed is-centered">
                <label class="file-label">
                        <span class="file-cta">
                          <span class="file-icon">
                            <i class="fas fa-file"/>
                          </span>
                        </span>
                    <span class="file-name">
                          {{element.content.name}}
                        </span>
                </label>
            </div>
        </template>
        <template v-if="'IMAGE' === element.type">
            <img :src="dataURL" class="canvasElementImage" :alt="element.content.name"/>
        </template>
    </canvas-element-skeleton>
</template>

<script>
    import MarkdownItVue from "markdown-it-vue"
    import CanvasElementSkeleton from "./CanvasElementSkeleton";

    export default {
        name: "EditableCanvasElement",
        components: {CanvasElementSkeleton, MarkdownItVue},
        props: {
            'element': {
                type: Object
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
                        reader.readAsDataURL(this.element.content);
                    })
                }
                return Promise.resolve('no data url');
            }
        }
    }
</script>

<style scoped>
    .canvas-element .content {
        max-height: 100%;
        max-width: 100%;
    }
    .canvasElementImage {
        background-repeat: no-repeat;
        background-position: 50% 50%;
        max-height: 100%;
        max-width: 100%;
        background-size: contain;
    }

    .file :hover {
        cursor: default !important;
    }
</style>
