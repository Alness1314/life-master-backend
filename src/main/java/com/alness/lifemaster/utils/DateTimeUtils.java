package com.alness.lifemaster.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateTimeUtils {
    private static final String PATTERN_DATE_ISO = "yyyy-MM-dd";
	private static final String PATTERN_TIME_ISO = "HH:mm:ss";
    private static final String PATTERN_DATETIME_ISO = "yyyy-MM-dd HH:mm:ss";

    private DateTimeUtils(){
        throw new IllegalStateException("Utility class");
    }

    public static LocalDateTime parseToLocalDateTime(String dateTime) {
		DateTimeFormatter formatterIn = DateTimeFormatter.ofPattern(PATTERN_DATETIME_ISO);
		return LocalDateTime.parse(dateTime, formatterIn);
	}


    public static LocalDate parseToLocalDate(String date) {
		DateTimeFormatter formatterIn = DateTimeFormatter.ofPattern(PATTERN_DATE_ISO);
		return LocalDate.parse(date, formatterIn);
	}

    public static String formatLocalDatetoString(LocalDate date) {
		return date.toString();
	}

    public static String getFirstDayMonth(String source) {
		DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern(PATTERN_DATE_ISO);
		LocalDate datenow = LocalDate.parse(source, localDateFormatter);
		return datenow.with(TemporalAdjusters.firstDayOfMonth()).toString();
	}

	public static String getLastDayMonth(String source) {
		DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern(PATTERN_DATE_ISO);
		LocalDate datenow = LocalDate.parse(source, localDateFormatter);
		return datenow.with(TemporalAdjusters.lastDayOfMonth()).toString();
	}

    public static List<String> getAllDaysOfMonth(String date) throws ParseException {
		List<String> dates = new ArrayList<>();
		String dateInicio = getFirstDayMonth(date);
		dates.add(getFirstDayMonth(date));
		SimpleDateFormat dFormat = new SimpleDateFormat(PATTERN_DATE_ISO);
		Date fecha = dFormat.parse(dateInicio);
		Calendar cal = Calendar.getInstance();
		cal.setTime(fecha);
		for (int i = 1; i < cal.getActualMaximum(Calendar.DATE); i++) {
			cal.add(Calendar.DATE, +1);
			dateInicio = dFormat.format(cal.getTime());
			dates.add(dateInicio);
		}

		return dates;
	}

	public static LocalTime parseToLocalTime(String timeString) {
        try {
            // Convertir la cadena a LocalTime
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_TIME_ISO);
            return LocalTime.parse(timeString, formatter);
        } catch (DateTimeParseException e) {
            // Manejo de error en caso de formato inválido
			log.error("El formato de tiempo proporcionado no es válido: {}", timeString);
            return null; // Puedes lanzar una excepción personalizada si lo prefieres
        }
    }

	public static String parseLocalTimeToString(LocalTime localTime){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_TIME_ISO);
        return localTime.format(formatter); 
	}
}
