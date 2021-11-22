<template>
    <div class="card is-fullheight">
        <header class="card-header is-unselectable">
            <p class="card-header-title">
            <span v-show="headerIcon != null">
                <span class="icon">
                    <i :class="headerIcon" aria-hidden="true" />
                </span>
                <span>&nbsp;</span>
            </span>
                {{headerTitle}}
            </p>
            <span class="card-header-icon" aria-label="show content">
        </span>
        </header>
        <div class="card-content">
            <div class="content">
                <diamant-chat-element v-for="(message, index) of sortedMessage" :key="index" :chat-element="message"/>
                <div v-if="messages == null || messages.length === 0">
                    <h2 class="subtitle is-6">{{$t('pages.ideas.chats.no_messages')}}</h2>
                </div>
            </div>
        </div>
        <footer class="card-footer">
            <div class="field is-grouped card-footer-item">
                <p class="control is-expanded">
                    <textarea class="textarea" :placeholder="placeholder_message" rows="2" v-model="newMessage"></textarea>
                </p>
                <p class="control is-fullheight">
                    <a class="button is-primary is-fullheight" @click="sendMessage()">
                        <span class="icon">
                            <i class="fas fa-envelope-open-text"></i>
                        </span>
                        {{$t('pages.ideas.chats.send')}}
                        <br/>
                    </a>
                </p>
            </div>
        </footer>
    </div>
</template>

<script>
    import DiamantChatElement from "../generic/DiamantChatElement";

    export default {
        name: "ChatCard",
        components: {DiamantChatElement},
        props: {
            type: {
                type: String,
                required: true
            },
            messages: Array
        },
        data: function () {
            return {
                newMessage: ''
            }
        },
        methods: {
          sendMessage() {
              this.$emit('send', this.newMessage);
              this.newMessage = '';
          }
        },
        computed: {
            headerIcon: function () {
                switch (this.type) {
                    case 'TEAM':
                        return 'fas fa-users';
                    case 'REFEREES':
                        return 'far fa-eye';
                    case 'TEAM_AND_REFEREES':
                        return 'fas fa-user-plus';
                }
                return ''
            },
            headerTitle: function() {
                return this.$t(`pages.ideas.chats.${this.type.toLowerCase()}.title`)
            },
            placeholder_message: function() {
                return this.$t(`pages.ideas.chats.${this.type.toLowerCase()}.new_message_placeholder`)
            },
            sortedMessage: function () {
                if(this.messages == null){
                    return [];
                }
                return this.messages.slice().sort((m1, m2) => m1.date - m2.date);
            }
        }
    }
</script>

<style scoped lang="scss">

</style>
