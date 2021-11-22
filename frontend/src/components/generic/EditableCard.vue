<template>
    <div class="card is-fullheight is-flex is-flex-columns" :class=" isEditing ? 'blink' :''">
        <header class="card-header is-unselectable"  v-if="hasHeader">
            <p class="card-header-title">
            <span v-show="iconClass != null">
                <span class="icon">
                    <i :class="iconClass" aria-hidden="true" />
                </span>
                <span>&nbsp;</span>
            </span>
                {{title}}
            </p>
            <span class="card-header-icon no-vertical-padding" v-if="isEditing && hasAddButtonInHeaderDuringEdit">
                <a class=" button is-primary" @click="$emit('add')">
                    <span class="icon is-small">
                        <i class="fa fa-plus"/>
                    </span>
                    <span>{{this.hasCustomAddText || $t('common.add')}}</span>
                </a>
            </span>
        </header>
        <div class="card-content is-flex-grow-1">
            <div :class="hasContentClass ? 'content' : ''" >
                <template v-if="! isEditing">
                    <slot />
                </template>
                <template v-else>
                    <slot name="editView" />
                </template>
            </div>
        </div>
        <footer class="card-footer" v-if="isEditable" :class="isEditing ? 'is-editing' : ''">
            <template v-if="hasCustomEditButton">
                <span class="card-footer-item" />
                <span class="card-footer-item" />
                <span class="card-footer-item" />
                <a class="card-footer-item" @click="$emit('customEdit')">
                        <span class="icon">
                            <i class="fas fa-edit" />
                        </span>
                    <span>
                        {{customEditText || $t('common.edit')}}
                    </span>
                </a>
            </template>
            <template v-else>
                <template v-if="! isEditing">
                    <span class="card-footer-item" />
                    <span class="card-footer-item" />
                    <span class="card-footer-item" />
                    <a class="card-footer-item" @click="triggerEdit">
                        <span class="icon">
                            <i class="fas fa-edit" />
                        </span>
                        <span>
                            {{$t('common.edit')}}
                        </span>
                    </a>
                </template>
                <template v-else>
                    <span class="card-footer-item" />
                    <span class="card-footer-item" />
                    <a class="card-footer-item" @click="hide">
                        <span class="icon">
                            <i class="fas fa-ban" />
                        </span>
                            <span>
                            {{$t('common.cancel')}}
                        </span>
                    </a>
                    <a class="card-footer-item has-text-success" @click="$emit('save', hide)">
                        <span class="icon">
                            <i class="fas fa-check" />
                        </span>
                            <span>
                            {{$t('common.save')}}
                        </span>
                    </a>
                </template>
            </template>
        </footer>
    </div>
</template>

<script>
    export default {
        name: "EditableCard",
        props: {
            title: String,
            iconClass: String,
            hasHeader: {
                type: Boolean,
                default: true
            },
            isEditable: Boolean,
            hasContentClass: {
                type: Boolean,
                default: true
            },
            customEditText: String,
            hasCustomEditButton: {
                type: Boolean,
                default: false
            },
            hasAddButtonInHeaderDuringEdit: {
                type: Boolean,
                default: false
            },
            hasCustomAddText: {
                type: String,
                default: null
            }
        },
        data: function () {
            return {
                isEditing: false
            }
        },
        methods: {
            hide() {
                this.isEditing = false;
            },
            triggerEdit(){
                this.isEditing = true;
            },
        }
    }
</script>

<style scoped lang="scss">
    @import "../../config/bulmaConfig";
    .card-footer a:not(.has-text-success) {
        color: black !important;
    }
    .card-footer .card-footer-item {
        border-right: 0;
    }
    .card-footer.is-editing .card-footer-item:nth-last-child(2), .card-footer-item:last-child {
        border-left: 1px solid #dbdbdb;
    }

    .no-vertical-padding{
        padding-bottom: 0;
        padding-top: 0;
    }

    @keyframes blinking {
        100%{
            background-color: lightgrey;
            box-shadow: 0 0.5em 1em -0.125em $primary, 0 0px 0 1px $primary;
        }
    }

    .blink {
        /* NAME | TIME | ITERATION */
        animation: blinking 0.75s 1;
    }

</style>
