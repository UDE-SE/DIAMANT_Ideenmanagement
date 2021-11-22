<template>
    <div class="content">
        <div class="field" v-show-error-directive="formErrors">
            <label class="label" for="title">{{$t('common.challenge_information.title')}}<required-indicator/></label>
            <div class="control">
                <input class="input" type="text" :placeholder="$t('common.challenge_information.title')"
                       id="title" v-model="challenge.title" maxlength="50" data-error-field-id="generalInformation.title">
            </div>
        </div>
        <div class="field" v-show-error-directive="formErrors">
            <label class="label" for="visibility">{{$t('common.challenge_information.visibility.label')}}<required-indicator/></label>
            <div class="control">
                <div class="select is-fullwidth">
                    <select id="visibility" v-model="challenge.visibility" data-error-field-id="generalInformation.visibility">
                        <option disabled value="undefined">{{$t('common.please_choose')}}</option>
                        <option value="OPEN">{{$t('common.challenge_information.visibility.values.open.label')}}</option>
                        <option value="INTERNAL">{{$t('common.challenge_information.visibility.values.internal.label')}}</option>
                        <option value="INVITE">{{$t('common.challenge_information.visibility.values.invite.label')}}</option>
                    </select>
                </div>
            </div>
        </div>
        <div class="field" v-show-error-directive="formErrors">
            <label class="label" for="shortDescription">{{$t('common.challenge_information.short_description')}}<required-indicator/></label>
            <div class="control">
                            <textarea class="textarea" :placeholder="$t('common.challenge_information.short_description')"
                                      id="shortDescription" v-model="challenge.shortDescription" rows="13" maxlength="500"
                                      data-error-field-id="generalInformation.shortDescription"/>
            </div>
        </div>
    </div>
</template>

<script>
    import RequiredIndicator from "../../generic/RequiredIndicator";
    import ShowErrorDirective from "../../generic/ShowErrorDirective";

    export default {
        name: "EditChallengeGeneralInformation",
        components: {RequiredIndicator},
        directives: {ShowErrorDirective},
        props: {
            challenge: {
                type: Object,
                required: true
            },
            formErrors: {
                type: Array,
                required: true
            }
        },
    }
</script>

<style scoped>

</style>
