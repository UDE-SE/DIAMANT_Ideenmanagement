// we need our modal component
import DiamantModal from './DiamantModal.vue'
import {events} from "./events";

const Modal = {
    install(Vue, args = {}) {
        if (this.installed) {
            return
        }
        Vue.component('DiamantModal', DiamantModal);
        const modal = {
                show: (params) => {
                    if (typeof params === 'string') {
                        params = { title: '', text: params }
                    }

                    if (typeof params === 'object') {
                        events.$emit('show', params)
                    }
                    return Promise.resolve();
                },
                showTextInput: (params) => {
                    events.$emit('showTextInput', params);
                    return Promise.resolve();
                },
                showLoadingScreen: () => {
                    events.$emit('showLoadingScreen');
                    return Promise.resolve();
                },
                showProgressScreen: (text, steps) => {
                    events.$emit('showProgressScreen', text, steps);
                    return Promise.resolve();
                },
                increaseStepCounter: () => {
                    events.$emit('increaseStepCounter');
                    return Promise.resolve();
                },
                hide : () => {
                    events.$emit('hide');
                    return Promise.resolve();
                },
                hideDelayed : () => {
                    return new Promise(resolve => setTimeout(() => {
                        events.$emit('hide');
                        resolve();
                    }, 1000))
                }
        };
        const name = args.name || 'modal';
        Vue.prototype['$' + name] = modal;
        Vue[name] = modal;
        this.installed = true;
    }
};

export default Modal
