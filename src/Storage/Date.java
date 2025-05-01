package Storage;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
/**
 * Class to Just to keep track of Date
 */

public class Date {
	private int year;
	private int month;
	private int day;
	
	public Date(int year, int month, int day) {
		super();
		this.year = year;
		this.month = month;
		this.day = day;
	}
	/**
	 * Calculating days between two dates
	 * Assuming the first date is today and the second is the date it expires 
	 * @param D1
	 * @param D2
	 * @return
	 */
	public static int ExpiryDate(Date D1,Date D2) {
		LocalDate today = LocalDate.of(D1.getYear(),D1.getMonth(),D1.getDay());
		LocalDate expiry = LocalDate.of(D2.getYear(),D2.getMonth(),D2.getDay());
		int days = (int)ChronoUnit.DAYS.between(today, expiry);
		return days;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public int getDay() {
		return day;
	}
}
