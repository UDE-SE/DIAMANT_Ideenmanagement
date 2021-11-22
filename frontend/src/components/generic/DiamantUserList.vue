<template>
    <div>
        <div>
            <table class="table is-striped">
                <tbody>
                <tr v-for="(person, index) of persons" :key="index">
                    <td v-if="hasIncognitoIndicator">
                        <span class="tooltip" :data-tooltip="$t('pages.ideas.team_members.hidden')" v-if="person.incognito">
                            <i class="fas fa-user-secret"></i>
                        </span>
                        <span class="tooltip" :data-tooltip="$t('pages.ideas.team_members.visible')" v-else>
                            <i class="fas fa-user-tie"></i>
                        </span>
                    </td>
                    <td>{{$t('common.persons.displaypattern', {'name': person.name, 'mail': person.email})}}</td>
                    <td class="is-narrow has-text-centered" v-if="showToggleIncognitoButton">
                        <a class="is-clickable icon has-text-black"  v-if="! person.incognito" @click="person.incognito = true">
                            <i class="fas fa-user-secret fa-border"></i>
                        </a>
                        <a class="is-clickable icon has-text-black"  v-else @click="person.incognito = false">
                            <i class="fas fa-user-tie fa-border"></i>
                        </a>
                    </td>
                    <td v-else></td>
                    <td class="is-narrow has-text-centered" v-if="showDeleteButton && ! isCurrentUser(person)">
                        <a class="is-clickable icon has-text-black"  @click="$emit('remove', person, index)">
                            <i class="fas fa-trash fa-border"></i>
                        </a>
                    </td>
                    <td v-else></td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script>
    export default {
        name: "DiamantUserList",
        props: {
            persons: Array,
            showDeleteButton: {
                type: Boolean,
                default: false
            },
            hasIncognitoIndicator: {
                type: Boolean,
                default: false
            },
            showToggleIncognitoButton: {
                type: Boolean,
                default: false
            }
        },
        methods:{
            isCurrentUser(person) {
                return person.email === this.$keycloak.userName;
            }
        }
    }
</script>

<style scoped>

</style>
