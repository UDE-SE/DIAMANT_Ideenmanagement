import Vue from 'vue'
import Router from 'vue-router'
import DashboardPage from '../pages/DashboardPage.vue'

Vue.use(Router);

export default new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  scrollBehavior () {
    return { x: 0, y: 0 }
  },
  routes: [
    {
      path: '/',
      alias: '/index.html',
      name: 'dashboard',
      component: DashboardPage
    },
    {
      path: '/challenge/:id',
      name: 'challenge',
      // route level code-splitting
      // this generates a separate chunk (about.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import(/* webpackChunkName: "challengedetails" */ '../pages/ChallengeDetailsPage.vue')
    },
    {
      path: '/challenge/:id/edit',
      name: 'createChallenge',
      // route level code-splitting
      // this generates a separate chunk (about.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import(/* webpackChunkName: "createchallenge" */ '../pages/CreateChallengePage.vue')
    },
    {
      path: '/challenge/:challengeId/idea/new',
      name: 'createIdea',
      // route level code-splitting
      // this generates a separate chunk (about.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import(/* webpackChunkName: "createIdea" */ '../pages/CreateIdeaPage.vue')
    },
    {
      path: '/challenge/:challengeId/idea/:ideaId',
      name: 'idea',
      // route level code-splitting
      // this generates a separate chunk (about.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import(/* webpackChunkName: "idea" */ '../pages/IdeaDetailsPage.vue')
    },
    {
      path: '/403',
      name: '403',
      // route level code-splitting
      // this generates a separate chunk (about.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import(/* webpackChunkName: "ForbiddenPage" */ '../pages/ForbiddenPage.vue')
    },
    {
      path: '/invitation/:token',
      name: 'invitation',
      // route level code-splitting
      // this generates a separate chunk (about.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import(/* webpackChunkName: "InvitationPage" */ '../pages/InvitationPage.vue')
    },
    { path: '*', component: () => import(/* webpackChunkName: "notFound" */ '../pages/NotFound.vue') }
  ]
})
