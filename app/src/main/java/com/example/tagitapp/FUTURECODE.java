package com.example.tagitapp;

public class FUTURECODE {
//    private static final String LECTURE_TABLE_NAME = "LECTURE_TABLE";
//    public static final String L_ID = "_LID";
//    public static final String LECTURE_DATE_KEY = "LECTURE_DATE";
//    public static final String CLASS_ID_KEY = "CLASS_ID";
//
//    private static final String CREATE_LECTURE_TABLE =
//            "CREATE TABLE " + LECTURE_TABLE_NAME +
//                    "( " +
//                    L_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
//                    CLASS_ID_KEY + " INTEGER NOT NULL, " +
//                    LECTURE_DATE_KEY + " DATE NOT NULL, " +
//                    "FOREIGN KEY (" + CLASS_ID_KEY + ") REFERENCES " + CLASS_TABLE_NAME + "(" + C_ID + ")" +
//                    ");";
//
//    private static final String CREATE_STATUS_TABLE = "CREATE TABLE " + STATUS_TABLE_NAME +
//            "( " +
//            STATUS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
//            S_ID + " INTEGER NOT NULL, " +
//            L_ID + " INTEGER NOT NULL, " +
//            DATE_KEY + " DATE NOT NULL, " +
//            STATUS_KEY + " TEXT NOT NULL, " +
//            "UNIQUE ( " + S_ID + "," + L_ID + "," + DATE_KEY + "), " +
//            "FOREIGN KEY (" + S_ID + ") REFERENCES " + STUDENT_TABLE_NAME + "( " + S_ID + ")," +
//            "FOREIGN KEY (" + L_ID + ") REFERENCES " + LECTURE_TABLE_NAME + "( " + L_ID + ")" +
//            ");";
//
//    long addLecture(long classId, String date) {
//        SQLiteDatabase database = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(CLASS_ID_KEY, classId);
//        values.put(LECTURE_DATE_KEY, date);
//        return database.insert(LECTURE_TABLE_NAME, null, values);
//    }
//
//    long addStatus(long sid, long lid, String date, String status) {
//        SQLiteDatabase database = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(L_ID, lid);
//        values.put(S_ID, sid);
//        values.put(DATE_KEY, date);
//        values.put(STATUS_KEY, status);
//        return database.insert(STATUS_TABLE_NAME, null, values);
//    }
//
//    // Insert a new lecture instance before marking attendance for it
//    long lectureId = dbHelper.addLecture(classId, date);
//    // Mark attendance for the specific lecture instance
//    long statusId = dbHelper.addStatus(studentId, lectureId, date, attendanceStatus);

}
