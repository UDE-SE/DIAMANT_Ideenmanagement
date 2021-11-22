<template>
  <div>
    <section class="section">
      <div class="container">
        <div class="columns">
          <div class="column"></div>
          <div class="column is-narrow" v-show="$keycloak.authenticated && $keycloak.hasRealmRole('caller')">
            <router-link :to="{name: 'createChallenge', params: { id: 0 }}" class="button is-primary">
              <span class="icon is-small">
                <i class="fa fa-plus"></i>
              </span>
              <span>{{$t('pages.dashboard_page.new_challenge.title')}}</span>
            </router-link>
          </div>
        </div>
        <div class="columns is-multiline">
          <div class="column is-one-third" v-for="(challenge, index) in myChallenges" :key="index">
            <router-link :to="{name: 'challenge', params: { id: challenge.id }}">
              <challenge-preview :challenge="challenge"></challenge-preview>
            </router-link>
          </div>
        </div>
        <h2 class="subtitle" v-if="openChallenges.length === 0">{{$t('pages.dashboard_page.no_challenges_found')}}</h2>
      </div>
    </section>
  </div>
</template>

<script>
// @ is an alias to /src
import ChallengePreview from "@/components/dashboard/ChallengePreview";
import {APIService} from "../until/APIService";

export default {
  name: 'DashboardPage',
  components: {
    ChallengePreview,
  },
  data: function () {
    return {
      myChallenges: [],
      openChallenges: [],
    }
  },
  methods: {
    loadChallenges() {
      this.$modal.showLoadingScreen()
      .then(APIService.getChallengesPreview)
      .then((res) => this.myChallenges = this.openChallenges = res)
      .catch(() =>
          this.$notify({
            title: this.$t('common.errors.title'),
            text: this.$t('common.errors.loading_failed'),
            type: 'error'
          })
      )
      .finally(this.$modal.hideDelayed)
    },
  },
  created () {
    this.loadChallenges()
  },
}
</script>
<style scoped lang="sass">
  @import "src/config/bulmaConfig"

  .new_challenge div
    background-color: $grey-lighter

</style>
