package de.unidue.se.diamant.backend.backendservice.service.time;

import de.unidue.se.diamant.backend.backendservice.service.time.domain.DateTime;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Log
public class TimeService {
    private static final String DATE_TIME_ID = "DiamantDateTime";
    private TimeRepository timeRepository;

    @Autowired
    public TimeService(TimeRepository timeRepository) {
        this.timeRepository = timeRepository;
    }

    public void setDateTime(Date dateTime) {
        if (dateTime == null) {
            timeRepository.deleteById(DATE_TIME_ID);
        } else {
            timeRepository.save(new DateTime(DATE_TIME_ID, dateTime));
        }
    }

    public Date getTime() {
        DateTime timeInDateBase = timeRepository.findById(DATE_TIME_ID).orElse(null);
        if(timeInDateBase != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            log.warning(String.format("Nutze das Datum aus der Datenbank: %s", simpleDateFormat.format(timeInDateBase.getCurrentTime())));
            return timeInDateBase.getCurrentTime();
        }
        return new Date();
    }

}
