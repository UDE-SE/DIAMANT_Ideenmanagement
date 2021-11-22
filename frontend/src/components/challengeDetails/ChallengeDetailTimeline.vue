<template>
    <div class="container">
        <span v-if="! news">{{$t('common.information_not_available', {'field': $t('common.challenge_information.news')})}}</span>
        <div class="timeline is-centered" v-else>
            <template v-for="(element, index) of sortedAndWithHiddenEventsIfNeeded">
                <template v-if="element.type === 'MILESTONE'">
                    <header class="timeline-header" :key="index">
                        <span class="tag is-medium is-primary">{{getDescription(element.content)}}</span>
                    </header>
                </template>
                <template v-else-if="element.type === 'HIDDEN'">
                    <div class="timeline-item" :key="index">
                        <div class="timeline-marker is-hidden"></div>
                    </div>
                </template>
                <template v-else>
                    <div class="timeline-item" :key="index">
                        <div class="timeline-marker is-icon is-32x32">
                            <i class="far fa-newspaper" v-if="element.type === 'UPDATE'"></i>
                            <i class="far fa-calendar-alt" v-if="element.type === 'PLANING'"></i>
                            <i class="fas fa-vial" v-if="element.type === 'PILOTING'"></i>
                            <i class="fas fa-flag-checkered" v-if="element.type === 'SUCCESS'"></i>
                            <i class="fas fa-times" v-if="element.type === 'NOT_IMPLEMENTED'"></i>
                        </div>
                        <div class="timeline-content">
                            <div class="box speech-bubble">
                                <p class="heading">{{element.date | toDateString}} <span class="tag" :class="labelColor(element.type)">{{labelText(element.type)}}</span></p>
                                <p>{{element.content}}</p>
                            </div>
                        </div>
                    </div>
                </template>
            </template>
        </div>
    </div>
</template>
<script>
    export default {
        name: 'ChallengeDetailTimeline',
        props: {
            news: Array
        },
        filters: {
            toDateString: function (milliseconds) {
                if(milliseconds == null ) {
                    return "---"
                }
                return new Date(milliseconds).toLocaleDateString()
            }
        },
        methods: {
            getDescription(content) {
                let strippedContent = content.replace('Start', '').replace('challenge', '').toLowerCase();
                return this.$t('common.challenge_information.milestones.values.' + strippedContent + '.label');
            },
            labelText(type){
                switch (type) {
                   case "UPDATE": return this.$t('pages.challenge_details_page.timeline.type.values.update');
                   case "PLANING": return this.$t('pages.challenge_details_page.timeline.type.values.planing');
                   case "PILOTING": return this.$t('pages.challenge_details_page.timeline.type.values.piloting');
                   case "SUCCESS": return this.$t('pages.challenge_details_page.timeline.type.values.success');
                   case "NOT_IMPLEMENTED": return this.$t('pages.challenge_details_page.timeline.type.values.not_implemented');
                }
            },
            labelColor(type) {
                switch (type) {
                    case "UPDATE": return "is-light";
                    case "PLANING": return "is-info";
                    case "PILOTING": return "is-warning";
                    case "SUCCESS": return "is-success";
                    case "NOT_IMPLEMENTED": return "is-danger";
                }
            }
        },
        computed: {
             sortedAndWithHiddenEventsIfNeeded: function () {
                if(! this.news){
                    return [];
                }
                let temp = this.news;
                //temp.sort((a, b) => b.date - a.date);
                 for (let i = 0; i < temp.length; i++) {
                     if(i > 0) {
                         if(temp[i - 1].type === "MILESTONE" && temp[i].type === "MILESTONE") {
                             temp.splice(i, 0, {type: "HIDDEN"})
                         }
                     }
                 }
                return temp;
            },
        }
    }
</script>
<style scoped lang="scss">
    @import "../../config/bulmaConfig";

    .speech-bubble:before {
        content: '';
        position: absolute;
        top: 1rem;
        width: 0;
        height: 0;
        border: 20px solid transparent;
        border-top: 0;
        margin-top: -10px;
    }
    .timeline-item:nth-of-type(2n) .speech-bubble:before {
        right: 0;
        border-right: 0;
        border-left-color: $primary;
        margin-right: -20px;
    }

    .timeline-item:nth-of-type(2n+1) .speech-bubble:before {
        left: 0;
        border-left: 0;
        border-right-color: $primary;
        margin-left: -20px;
    }
    .speech-bubble {
        position: relative;
    }

</style>
