<template>
    <div class="EasyMDEContainer">
        <div class="editor-toolbar">
            <button type="button" @click="tryDropText">
                <span>
                   <i class="fas fa-heading"/>
                </span>
            </button>
            <button type="button" @click="() => tryDropImageOrAttachment('IMAGE')">
                <span>
                   <i class="far fa-file-image"/>
                </span>
            </button>
            <button type="button" @click="() => tryDropImageOrAttachment('ATTACHMENT')">
                <span>
                   <i class="far fa-file-alt"/>
                </span>
            </button>
        </div>
        <div class="CodeMirror cm-s-easymde CodeMirror-wrap">
            <div class="is-relative" id="idea_canvas">
                <EditableCanvasElement v-for="e of elements.filter(elm => elm.newElement)" :key="'o' + e.id" :element="e" @remove="removeElement(e)" @edit="edit(e)" :use-random-id="true"/>
                <CanvasElement v-for="e of elements.filter(elm => !elm.newElement)" :key="'n' + e.id" :element="e" @remove="removeElement(e)" @edit="edit(e)" :is-editable="true" :use-random-id="true"/>
            </div>
        </div>
    </div>
</template>

<script>
    import EditableCanvasElement from "../ideasDetails/canvas/EditableCanvasElement";
    import CanvasElement from "../ideasDetails/canvas/CanvasElement";
    import {PositionUtils} from "./PositionUtils";

    export default {
        name: "IdeaCanvas",
        props: {
            elements: {
                type: Array
            },
        },
        components: {CanvasElement, EditableCanvasElement},
        methods: {
            tryDropText: function(){
                this.$modal.showTextInput({
                    title: this.$t('pages.ideas.new.add_text_canvas_element'),
                    text: '',
                    callback: (text) => {
                        let newCanvasElement = {
                            id: this.findNextId(),
                            type: 'TEXT',
                            content: text,
                            newElement: true,
                            position: PositionUtils.findNewPosition(null, this.elements),
                        };
                        this.elements.push(newCanvasElement);
                    }
                });
            },
            tryDropImageOrAttachment(type){
                let input = document.createElement('input');
                input.style.display = "none";
                input.type = 'file';
                this.$el.append(input);
                if(type === 'IMAGE'){
                    input.accept = "image/*";
                }
                input.addEventListener('change', (e) => {
                    let file = e.target.files[0];
                    let maxSize = process.env.VUE_APP_MAX_ATTACHMENT_SIZE_IN_MB;
                    if (file.size <= maxSize * (1024 * 1024)) {
                        let newCanvasElement = {
                            id: this.findNextId(),
                            type: type,
                            content: file,
                            newElement: true,
                            position: PositionUtils.findNewPosition(null, this.elements),
                        };
                        this.elements.push(newCanvasElement);
                    } else {
                        this.$notify({
                            title: this.$t('common.errors.title'),
                            text: this.$t('common.errors.file_too_big', {'size': maxSize}),
                            type: 'error'
                        })
                    }
                });
                input.click();
            },
            findNextId() {
                let highest = 0;
                for (const element of this.elements) {
                    let elementIdAsNumber = Number.parseInt(element.id);
                    if (elementIdAsNumber > highest) {
                        highest = elementIdAsNumber;
                    }
                }
                return ++highest;
            },
            removeElement: function (element) {
                this.$delete(this.elements, this.elements.indexOf(element));
            },
            edit: function (element) {
                if ('TEXT' === element.type) {
                    this.$modal.showTextInput({
                        title: this.$t('pages.ideas.new.add_text_canvas_element'),
                        text: element.content,
                        callback: (text) => {
                            element.content = text;
                        }
                    });
                }
            }
        },
    }
</script>

<style scoped>
    #idea_canvas {
        min-height: 500px;
        resize: vertical;
        overflow: auto;
    }
</style>
