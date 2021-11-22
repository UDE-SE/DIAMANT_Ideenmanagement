import VueKeyCloak from '@dsb-norge/vue-keycloak-js'
import Vue from 'vue'
import Notification from "vue-notification";
import router from './config/router'
import i18n from './config/i18n';
import AsyncComputed from 'vue-async-computed'
import App from './App.vue'
import './registerServiceWorker'
import './config/bulmaConfig.sass'
import './../node_modules/@fortawesome/fontawesome-free/css/all.min.css';
import Modal from "@/components/generic/modal/modal";
import MQ from 'vue-match-media/src'

Vue.config.productionTip = true;

Vue.use(MQ)
Vue.use(Modal);
Vue.use(Notification);
Vue.use(AsyncComputed);

Vue.use(VueKeyCloak, {
  init: {
    onLoad: 'check-sso'
  },
  config: {
    url: process.env.VUE_APP_KEYCLOAK_URL,
	clientId: process.env.VUE_APP_CLIENT_ID,
    realm: process.env.VUE_APP_REALM,
  },
  onReady: () => {
    new Vue({
      router,
      i18n,
      mq: {
        desktop: '(min-width: 1024px)'
      },
      render: h => h(App)
    }).$mount('#app')
  }
});
