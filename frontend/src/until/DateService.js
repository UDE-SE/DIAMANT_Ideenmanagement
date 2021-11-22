
export class DateService {

    static getDate() {
        let currentDate = window.localStorage.getItem("currentDate");
        if(currentDate == null) {
            return new Date();
        }
        return new Date(parseInt(currentDate));
    }

}
