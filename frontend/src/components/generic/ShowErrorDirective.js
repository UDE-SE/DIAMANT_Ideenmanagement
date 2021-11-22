let ShowErrorDirective = {
    bind(el){
        if((el.getElementsByTagName("input").length + el.getElementsByTagName("textarea").length + el.getElementsByTagName("select").length)!== 1 ){
            throw Error("Es muss genau ein input element geben..");
        }
    },
    update(el, binding, vnode){
        let inputElement = el.getElementsByTagName("input")[0];
        if(inputElement == null){
            inputElement = el.getElementsByTagName("textarea")[0]
        }
        if(inputElement == null){
            inputElement = el.getElementsByTagName("select")[0]
        }
        let field = inputElement.dataset.errorFieldId || inputElement.getAttribute("id");
        field = field.split(".").pop();

        inputElement.classList.remove("is-danger");
        if(inputElement.tagName === 'SELECT'){
            el.getElementsByClassName('select')[0].classList.remove("is-danger");
        }
        if(el.getElementsByClassName("help is-danger").length > 0) {
            el.getElementsByClassName("help is-danger")[0].remove();
        }

        if(binding.value && binding.value instanceof Array){
            for (let i = 0; i < binding.value.length; i++) {
                const error = binding.value[i];
                if(error && error.field.split(".").pop() === field){
                    if(inputElement.tagName === 'SELECT'){
                        inputElement = el.getElementsByClassName('select')[0];
                    }
                    if(! inputElement.classList.contains("is-danger")) {
                        inputElement.classList.add("is-danger");
                        let warningElement = document.createElement("p");
                        warningElement.classList.add("help");
                        warningElement.classList.add("is-danger");
                        if(error.code === 'NotBlank' || error.code === 'NotNull') {
                            warningElement.innerHTML = vnode.context.$t('common.errors.not_blank')
                        } else {
                            warningElement.innerHTML = error.defaultMessage;
                        }
                        el.appendChild(warningElement);
                        return;
                    }
                }
            }
        }
    }
};

export default ShowErrorDirective;
