let DiamantComponentIdGeneratorMixin = {
    data: function() {
      return {
          componentId: ''
      }
    },
    created: function () {
        this.componentId = ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c => (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16));
    },
    methods: {
        componentBasedId(label){
            return `${this.componentId}-${label}`
        }
    }
};

export default DiamantComponentIdGeneratorMixin;
