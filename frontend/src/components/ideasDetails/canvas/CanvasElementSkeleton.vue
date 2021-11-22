<template>
    <vue-drag-resize
            :w="element.position.width" :h="element.position.height"
            :x="element.position.x" :y="element.position.y"
            :parentLimitation="true" :isActive="isActive"
            @activated="isActive = true" @deactivated="isActive = false"
            @resizestop="dimensionUpdate" @dragstop="dimensionUpdate"
            :isDraggable="isDraggable" :isResizable="isDraggable"
    >
    <div class="card is-fullheight is-flex is-flex-columns" :id="elementId">
        <header class="card-header">
            <p class="card-header-title">
                {{$t('pages.ideas.canvas.element_title', {id: element.id})}}
            </p>
        </header>
        <div class="card-content is-flex-grow-1 is-clipped">
            <slot />
        </div>
        <footer class="card-footer" @touchstart="(e) => e.stopPropagation()">
            <a class="card-footer-item" v-show="!isReadyOnly && isActive && showEditButton" @click="$emit('edit')">
                <span class="icon">
                    <i class="fa fa-edit" />
                </span>
            </a>
            <a class="card-footer-item" v-show="!isReadyOnly && isActive" @click="$emit('remove')">
                <span class="icon">
                    <i class="fa fa-trash"/>
                </span>
            </a>
            <a class="card-footer-item tooltip" :data-tooltip="$t('pages.ideas.actions.copy_element_reference_to_clipboard')"
               v-show="showCopyReferenceButton" @click="copyReference">
                    <span class="icon">
                        <i class="fa fa-paste"/>
                    </span>
            </a>
        </footer>
    </div>
    </vue-drag-resize>
</template>

<script>
    import VueDragResize from 'vue-drag-resize';

    export default {
        name: "CanvasElementSkeleton",
        components: {VueDragResize},
        props: {
            element: Object,
            isReadyOnly: Boolean,
            useRandomId: Boolean
        },
        data: function () {
            return {
                isActive: false,
                dataURL: undefined
            }
        },
        methods: {
            dimensionUpdate: function (newDimension) {
                if(! this.isReadOnly) {
                    this.element.position.x = newDimension.left;
                    this.element.position.y = newDimension.top;
                    this.element.position.width = newDimension.width;
                    this.element.position.height = newDimension.height;
                }
            },
            uuidv4: function() {
                return ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c =>
                    (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
                );
            },
            copyReference: function () {
                navigator.clipboard.writeText(`<lösungs-element id="#${this.elementId}">Element ${this.element.id}</lösungs-element>`);
            }
        },
        computed: {
            showEditButton: function () {
                return !this.isReadyOnly && 'TEXT' === (this.element == null ? null : this.element.type)
            },
            elementId: function () {
                let temp = 'canvasElement' + this.element.id;
                if(this.useRandomId) {
                    temp += this.uuidv4();
                }
                return temp
            },
            showCopyReferenceButton: function () {
                return this.isActive && this.isReadyOnly && navigator.clipboard != null;
            },
            isDraggable : function () {
                return ! this.isReadyOnly && this.$mq.desktop;
            }
        },
    }
</script>

<style scoped lang="scss">
    @import "src/config/bulmaConfig";

    .boxheader {
        width: 100%;
        padding: 5px;
    }

    .card footer a {
        color: black !important;
    }
    .canvas-element .content {
        max-height: 100%;
        max-width: 100%;
    }

    .card:target {
        box-shadow: 0 0.5em 1em -0.125em #{$primary}, 0 0px 0 1px #{$primary};
    }

    @media only screen and (max-width: 1024px) {
        .vdr {
            width: 100% !important;
            height: auto !important;
            top: auto !important;
            left: auto !important;
            position: initial !important;
            touch-action: none !important;
        }

        .card {
            padding-bottom: 0.5em;
        }

    }
</style>
