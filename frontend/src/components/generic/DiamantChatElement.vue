<template>
    <div class="is-clearfix textbuble">
        <div class="box" :class="isRight ? 'is-pulled-right' : ''" >
            <article class="media">
                <div class="media-content">
                    <div class="content">
                        <p>
                            <strong>{{chatElement.author.name}}</strong> <small class="is-hidden-touch">&lt;<a :href="'mailto:'+ chatElement.author.email">{{chatElement.author.email}}</a>&gt;</small> <small>{{toDateString(chatElement.date)}}</small>
                            <br>
                            <span v-html="chatElement.text"></span>
                        </p>
                    </div>
                </div>
            </article>
        </div>
    </div>
</template>

<script>
    export default {
        name: "DiamantChatElement",
        props: {
            chatElement: Object,
        },
        methods: {
            toDateString: function (milliseconds) {
                if(milliseconds == null) {
                    return "---"
                }
                return new Date(milliseconds).toLocaleString();
            }
        },
        computed: {
            isRight: function () {
                return this.chatElement.author.email === this.$keycloak.userName;
            }
        }
    }
</script>

<style scoped>
    .textbuble{
        margin: 2em;
    }
    .box {
        width: 60%;
    }
    .box.is-pulled-right .media-content{
        text-align: end;
    }

</style>
