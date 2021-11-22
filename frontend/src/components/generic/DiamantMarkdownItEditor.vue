<template>
    <vue-easymde v-model="inputVal" :configs="config"></vue-easymde>
</template>

<script>
    import VueEasymde from "vue-easymde";

    export default {
        name: "DiamantMarkdownItEditor",
        components: {VueEasymde},
        props: {value: String},
        data() {
            return {
                config: {
                    autoDownloadFontAwesome: false,
                    spellChecker: false,
                    toolbar: ['bold', 'italic', 'heading', '|', 'quote', 'unordered-list', 'ordered-list' , '|', 'link', 'image',  '|', 'guide']
                }
            }
        },
        computed: {
            inputVal: {
                get() {
                    return this.value;
                },
                set(val) {
                    this.$emit('input', val);
                }
            }
        }
    }
</script>

<style scoped>

</style>
