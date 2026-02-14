package application;

public enum BorrowStatus {
	LENT_TO("an"),
	BORROWED_FROM("von"),
	NONE("nein");

	private final String dbValue;

	BorrowStatus(String dbValue) {
		this.dbValue = dbValue;
	}

	public String getDbValue() {
		return dbValue;
	}

	public static BorrowStatus fromDbValue(String value) {
		if (value == null) return NONE;
		for (BorrowStatus status : values()) {
			if (status.dbValue.equals(value)) {
				return status;
			}
		}
		return NONE;
	}
}
