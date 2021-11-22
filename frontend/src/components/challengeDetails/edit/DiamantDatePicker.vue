<template>
    <span>
        <input type="date" class="input" :class="Number.isNaN(value) ? 'is-danger' : ''" v-model="displayDate" ref="input"
               :data-error-field-id="dataErrorFieldId"
               :disabled="disabled">
        <span class="help has-text-danger" v-if="Number.isNaN(value)">{{$t('common.errors.invalid_date')}}</span>
    </span>
</template>

<script>
    export default {
        name: "DiamantDatePicker",
        props: {
            dataErrorFieldId: String,
            disabled: Boolean,
            value: Number,
        },
        computed: {
            displayDate: {
                get: function () {
                    if(this.value != null && ! Number.isNaN(this.value)){
                        return new Date(this.value - new Date().getTimezoneOffset() * 60 * 1000).toISOString().slice(0, 10);
                    } else {
                        return '';
                    }
                },
                set: function () {
                    let newDate = null;
                    try {
                        if(this.$refs.input.type === 'date') {
                            newDate = (this.$refs.input.valueAsNumber + new Date().getTimezoneOffset() * 60 * 1000);
                        } else {
                            newDate = (Date.parse(this.$refs.input.value) + new Date().getTimezoneOffset() * 60 * 1000);
                        }
                    } catch (e) {
                        // silently ignore error
                    }
                    this.$emit('input', newDate);

                }
            }
        }
    }
</script>

<style scoped>

</style>
