import Vue from 'vue'
import axios from 'axios';
const API_URL = process.env.VUE_APP_API_ENDPOINT;
export class APIService{

    constructor(){
    }

    static _getDefaultConfig() {
        return {
            headers: {
                'Authorization': `Bearer ${Vue.prototype.$keycloak.token}`,
                'Content-Type': 'application/json',
            }
        }
    }

    static _handleError(error){
        if(error.response.status === 401) {
            Vue.prototype.$keycloak.login();
        }
        if(error.response.status === 403){
            if(! Vue.prototype.$keycloak.authenticated){
                Vue.prototype.$keycloak.login();
            } else {
                window.location.href = "/403";
            }
        }
        else {
            console.error(error.response);
            throw error.response;
        }
    }

    static  saveChallenge(challenge, attachments, partUploadedCallback) {
        let challengeClone = JSON.parse(JSON.stringify(challenge));
        return axios
            .post(API_URL + 'challenge', JSON.stringify(challengeClone), APIService._getDefaultConfig())
            .then(function (response) {
                console.log(response);
                return response.data;
            })
            .then((challengeId) => partUploadedCallback().then(() => Promise.resolve(challengeId)))
            .then((challengeId) => {
                let uploadPromises = [];
                for (const attachment of attachments) {
                    uploadPromises.push(this.saveAttachmentForChallenge(challengeId, attachment, partUploadedCallback ))
                }
                return Promise.all(uploadPromises).then(() => Promise.resolve(challengeId))
            })
            .catch(function (error) {
                console.log(error.response);
                throw error.response;
            });
    }

    static saveAttachmentForChallenge(challengeId, fileHandle, partUploadedCallback) {
        let formData = new FormData();
        formData.append('attachment', fileHandle);
        return axios.post( API_URL + 'challenge/' + challengeId + '/attachment',
            formData,
            {
                headers: {
                    'Authorization': `Bearer ${Vue.prototype.$keycloak.token}`,
                    'Content-Type': 'multipart/form-data'
                }
            }
        ).then(function(response){
            console.log('Anhang erfolgreich hochgeladen: ' + response.data);
        })
        .then(() => partUploadedCallback().then(() => Promise.resolve(challengeId)))
        .catch(function(e){
            console.error('Anhang konnte nicht hochgeladen werden');
            console.error(e);
            throw e.response;
        })
    }

    static saveAttachmentForIdea(challengeId, ideaId, attachmentBusinessId, fileHandle, partUploadedCallback) {
        let formData = new FormData();
        formData.append('attachment', fileHandle);
        return axios.post( API_URL + 'challenge/' + challengeId + '/idea/' + ideaId + '/attachment/' + attachmentBusinessId ,
            formData,
            {
                headers: {
                    'Authorization': `Bearer ${Vue.prototype.$keycloak.token}`,
                    'Content-Type': 'multipart/form-data'
                }
            }
        ).then(function(response){
            console.log('Anhang erfolgreich hochgeladen: ' + response.data);
        })
            .then(() => partUploadedCallback().then(() => Promise.resolve(challengeId)))
            .catch(function(e){
                console.error('Anhang konnte nicht hochgeladen werden');
                console.error(e);
                throw e.response;
            })
    }

    static downloadAttachmentAsBlob(url) {
        let config = {};
        if(Vue.prototype.$keycloak.authenticated){
            config = APIService._getDefaultConfig();
        }
        config.responseType = 'blob'; // important,
        return axios.get(url, config);
    }

    static downloadAttachment(url, name) {
        return this.downloadAttachmentAsBlob(url)
            .then((response) => {
                const url = window.URL.createObjectURL(new Blob([response.data]));
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', name);
                document.body.appendChild(link);
                link.click();
            }
        );
    }


    static getChallengesPreview(){
        let config = null;
        if(Vue.prototype.$keycloak.authenticated){
            config = APIService._getDefaultConfig();
        }
        return axios
            .get(API_URL + 'challenge/preview', config)
            .then(function (response) {
                return response.data;
            })
            .catch((error) => APIService._handleError(error));
    }

    static getChallenge(challengeId){
        let config = null;
        if(Vue.prototype.$keycloak.authenticated){
            config = APIService._getDefaultConfig();
        }
        return axios
            .get(API_URL + 'challenge/' + challengeId, config)
            .then(function (response) {
                return response.data;
            })
            .catch((error) => APIService._handleError(error));
    }

    static saveIdea(challengeId, newIdeaObject, partUploadedCallback) {
        let ideaClone = JSON.parse(JSON.stringify(newIdeaObject));
        for (const canvasElement of ideaClone.canvasElements) {
            if('TEXT' !== canvasElement.type){
                canvasElement.content = 'file..'
            }
        }
        return axios
            .post(API_URL + 'challenge/' + challengeId + '/idea', JSON.stringify(ideaClone), APIService._getDefaultConfig())
            .then(function (response) {
                return response.data;
            })
            .then((ideaId) => partUploadedCallback().then(() => Promise.resolve(ideaId)))
            .then((ideaId) => {
                let uploadPromises = [];
                for (const attachment of newIdeaObject.canvasElements) {
                    if('TEXT' !== attachment.type) {
                        uploadPromises.push(this.saveAttachmentForIdea(challengeId, ideaId, attachment.id, attachment.content, partUploadedCallback ))
                    }
                }
                return Promise.all(uploadPromises).then(() => Promise.resolve(ideaId))
            })
            .catch(function (error) {
                console.error(error.response);
                throw error.response;
            });

    }

    static updateIdeaGeneralInformation(challengeId, ideaId, teamName, shortDescription) {
        let postObject = {
            field: 'GENERAL_INFORMATION',
            newValueDTO: {
                teamName: teamName,
                shortDescription: shortDescription
            }
        };
        return axios
            .put(API_URL + 'challenge/' + challengeId + '/idea/' + ideaId + '/update',
                JSON.stringify(postObject),
                APIService._getDefaultConfig())
            .then(function (response) {
                return response.data;
            })
            .catch(function (error) {
                console.error(error.response);
                throw error.response;
            });
    }

    static updateCanvas(challengeId, ideaId, canvasElements, partUploadedCallback) {
        let canvasElementsClone = JSON.parse(JSON.stringify(canvasElements));
        for (const canvasElement of canvasElementsClone) {
            if('TEXT' !== canvasElement.type){
                canvasElement.content = 'file..'
            }
        }

        let postObject = {
            field: 'CANVAS',
            newValueDTO: {
                canvasElements: canvasElementsClone
            }
        };
        return axios
            .put(API_URL + 'challenge/' + challengeId + '/idea/' + ideaId + '/update',
                JSON.stringify(postObject),
                APIService._getDefaultConfig())
            .then(function (response) {
                return response.data;
            })
            .then((ideaId) => partUploadedCallback().then(() => Promise.resolve(ideaId)))
            .then((ideaId) => {
                let uploadPromises = [];
                for (const attachment of canvasElements) {
                    if('TEXT' !== attachment.type && attachment.newElement) {
                        uploadPromises.push(this.saveAttachmentForIdea(challengeId, ideaId, attachment.id, attachment.content, partUploadedCallback ))
                    }
                }
                return Promise.all(uploadPromises).then(() => Promise.resolve(ideaId))
            })
            .catch(function (error) {
                console.error(error.response);
                throw error.response;
            });
    }

    static getIdea(challengeId, ideaId) {
        let config = null;
        if(Vue.prototype.$keycloak.authenticated){
            config = APIService._getDefaultConfig();
        }
        return axios
            .get(API_URL + 'challenge/' + challengeId + '/idea/' + ideaId, config)
            .then(function (response) {
                return response.data;
            })
            .catch((error) => APIService._handleError(error));
    }

    static updateStateOfIdea(challengeId, ideaId, newState){
        let postObject = {
            field: "STATE",
            newValueDTO: newState
        };
        return axios
            .put(API_URL + 'challenge/' + challengeId + '/idea/' + ideaId + '/update', JSON.stringify(postObject), APIService._getDefaultConfig())
            .then(function (response) {
                return response.data;
            })
            .catch(function (error) {
                console.log(error.response);
                throw error.response;
            });
    }

    static saveVoting(challengeId, ideaId, voting){
        let postObject = {
            vote: voting
        };
        return axios
            .post(API_URL + 'challenge/' + challengeId + '/idea/' + ideaId + '/vote', JSON.stringify(postObject), APIService._getDefaultConfig())
            .then(function (response) {
                return response.data;
            })
            .catch(function (error) {
                console.log(error.response);
                throw error.response;
            });
    }

    static sendChatMessage(challengeId, ideaId, type, message){
        let url = API_URL + 'challenge/' + challengeId + '/idea/' + ideaId + '/chat/';
        if(type === 'TEAM'){
            url += 'team';
        }
        if(type === 'REFEREES') {
            url += 'referees';
        }
        if(type === 'TEAM_AND_REFEREES') {
            url += 'teamandreferees';
        }
        return axios
            .post(url, message, APIService._getDefaultConfig())
            .then(function (response) {
                return response.data;
            })
            .catch(function (error) {
                console.log(error.response);
                throw error.response;
            });
    }

    static updateIdeaTeam(challengeId, ideaId, team) {
        let postObject = {
            field: 'TEAM_MEMBER',
            newValueDTO:{
                teamMember: team
            }
        };
        return axios
            .put(API_URL + 'challenge/' + challengeId + '/idea/' + ideaId + '/update',
                JSON.stringify(postObject),
                APIService._getDefaultConfig())
            .then(function (response) {
                return response.data;
            })
            .catch(function (error) {
                console.error(error.response);
                throw error.response;
            });
    }

    static updateIdeaWinningState(challengeId, ideaId, newWinningState) {
        let postObject = {
            field: 'WINNING_PLACE',
            newValueDTO:newWinningState
        };
        return axios
            .put(API_URL + 'challenge/' + challengeId + '/idea/' + ideaId + '/update',
                JSON.stringify(postObject),
                APIService._getDefaultConfig())
            .then(function (response) {
                return response.data;
            })
            .catch(function (error) {
                console.error(error.response);
                throw error.response;
            });
    }

    static _updateChallenge(challengeId, postObject){
        return axios
            .put(API_URL + 'challenge/' + challengeId + '/update',
                JSON.stringify(postObject),
                APIService._getDefaultConfig())
            .then(function (response) {
                return response.data;
            })
            .catch(function (error) {
                console.error(error.response);
                throw error.response;
            });
    }

    static updateChallengeGeneralInformation(challengeId, update) {
        return this._updateChallenge(challengeId, {field: 'GENERAL_INFORMATION', newValueDTO: update})
    }

    static updateChallengeMilestones(challengeId, update) {
        return this._updateChallenge(challengeId, {field: 'MILESTONES', newValueDTO: update})
    }

    static updatePersons(challengeId, type, update) {
        return this._updateChallenge(challengeId, {field: type, newValueDTO: {persons: update}})
    }

    static updateText(challengeId, type, editedText) {
        return this._updateChallenge(challengeId, {field: type, newValueDTO: editedText})
    }

    static updateChallengeAttachments(challengeId, files, partUploadedCallback) {
        let remainingFiles = [];
        for (const file of files) {
            if(file.id) {
                remainingFiles.push(file.id)
            }
        }
        let postObject = {
            field: 'ATTACHMENTS',
            newValueDTO: {
                remainingAttachments: remainingFiles
            }
        };
        return axios
            .put(API_URL + 'challenge/' + challengeId + '/update',
                JSON.stringify(postObject),
                APIService._getDefaultConfig())
            .then(function (response) {
                return response.data;
            })
            .then((ideaId) => partUploadedCallback().then(() => Promise.resolve(ideaId)))
            .then((ideaId) => {
                let uploadPromises = [];
                for (const attachment of files) {
                    if(! attachment.id) {
                        uploadPromises.push(this.saveAttachmentForChallenge(challengeId, attachment, partUploadedCallback ))
                    }
                }
                return Promise.all(uploadPromises).then(() => Promise.resolve(ideaId))
            })
            .catch(function (error) {
                console.error(error.response);
                throw error.response;
            });

    }

    static addNewsItem(challengeId, newItem) {
        return this._updateChallenge(challengeId, {field: 'NEW_NEWS_ENTRY', newValueDTO: newItem})
    }

    static getSseEventSource(challengeId, ideaId){
        return new EventSource(API_URL + 'challenge/' + challengeId + '/idea/' +ideaId + '/sse');
    }

    static getChat(challengeId, ideaId, type) {
        let config = null;
        if(Vue.prototype.$keycloak.authenticated){
            config = APIService._getDefaultConfig();
        }
        return axios
            .get(API_URL + 'challenge/' + challengeId + '/idea/' + ideaId + '/chat/' + type, config)
            .then(function (response) {
                return response.data;
            })
            .catch(function (error) {
                console.log(error.response);
                throw error.response;
            });
    }

    static acceptInvitation(token) {
        return axios.post( API_URL + 'invitation/accept' ,
            token,
            APIService._getDefaultConfig()
        ).then(function (response) {
            return response.data;
        })
        .catch(function (error) {
            console.error(error.response);
            throw error.response;
        });
    }
}
