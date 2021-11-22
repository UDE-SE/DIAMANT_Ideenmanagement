let ShowErrorOnDivDirective = {
    bind(el){
        if(el.tagName !== 'DIV' ){
            throw Error("Direktive muss an einem div verwendet werden");
        }
    },
    update(el, binding, vnode){
        let field = el.dataset.errorFieldId || el.getAttribute("id");
        el.classList.remove("is-danger-control");
        if(el.parentNode.getElementsByClassName("help is-danger").length > 0) {
            el.parentNode.getElementsByClassName("help is-danger")[0].remove();
        }

        if(binding.value && binding.value instanceof Array){
            for (let i = 0; i < binding.value.length; i++) {
                const error = binding.value[i];
                if(error && error.field === field){
                    if(! el.classList.contains("is-danger-control")) {
                        el.classList.add("is-danger-control");
                        let warningElement = document.createElement("p");
                        warningElement.classList.add("help");
                        warningElement.classList.add("is-danger");
                        if(error.code === 'NotBlank' || error.code === 'NotNull' || error.code === 'NotEmpty') {
                            warningElement.innerHTML = vnode.context.$t('common.errors.not_blank')
                        } else {
                            warningElement.innerHTML = error.defaultMessage;
                        }
                        el.parentNode.appendChild(warningElement);
                        return;
                    }
                }
            }
        }
    }
};

export default ShowErrorOnDivDirective;
