package net.bndy.ad.framework.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQLiteForEntityProvider extends SQLiteProvider {

    private static final String TAG = SQLiteForEntityProvider.class.getSimpleName();

    private Class<?>[] clazzes;

    public SQLiteForEntityProvider(Context context, String dbName, Class<?>... clazzes) {
        super(context, dbName, 1, "");
        this.clazzes = clazzes;
        if (clazzes.length == 0) {
            Log.w(TAG, "No entity types are persistent to store");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Class<?> clazz : this.clazzes) {
            String sql = getSQLTable(clazz);
            if (sql != null) {
                db.execSQL("DROP TABLE IF EXISTS " + clazz.getSimpleName());
                db.execSQL(sql);
            }
        }
    }

    private String getSQLTable(Class<?> clazz) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("CREATE TABLE " + clazz.getSimpleName() + "(");

        List<Field> fields = this.getFields(clazz);
        for (Field field : fields) {
            DbIgnore dbIgnore = field.getAnnotation(DbIgnore.class);
            if (dbIgnore!= null && dbIgnore.value()) {
                continue;
            }
            String sqliteType = SQLiteTypeMapping.toSQLiteType(field.getType());
            stringBuffer.append(field.getName() + " " + sqliteType);
            if (field != fields.get(fields.size() - 1)) {
                stringBuffer.append(",");
            }
        }

        stringBuffer.append(")");
        return stringBuffer.toString();
    }

    private List<Field> getFields(Class<?> entity) {
        List<Field> result = new ArrayList<>();
        Field[] fields = entity.getDeclaredFields();
        for (Field field : fields) {
            // filter all fields generated by compiler, like $change
            if (!field.isSynthetic()) {
                result.add(field);
            }
        }
        return result;
    }

    public <T> long insert(T entity) {
        SQLiteDatabase db = this.getWritableDatabase();

        String tableName = entity.getClass().getSimpleName();
        ContentValues values = new ContentValues();
        List<Field> fields = this.getFields(entity.getClass());
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object val = field.get(entity);
                if (val != null) {
                    values.put(field.getName(), val.toString());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        long rowId = db.insert(tableName, null, values);
        db.close();

        return rowId;
    }

    public int update(String tableName, Map<String, Object> updates, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        for(String key: updates.keySet()) {
            values.put(key, updates.get(key) != null ? updates.get(key).toString() : "");
        }
        int result = db.update(tableName, values, whereClause, whereArgs);
        db.close();

        return result;
    }

    public <T> int update(T entity, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();

        String tableName = entity.getClass().getSimpleName();
        ContentValues values = new ContentValues();
        List<Field> fields = this.getFields(entity.getClass());
        for (Field field : fields) {
            try {
                values.put(field.getName(), field.get(entity).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        int result = db.update(tableName, values, whereClause, whereArgs);
        db.close();

        return result;
    }

    public int delete(Class<?> clazz, String whereClause, String[] whereArgs) {
        SQLiteDatabase db= this.getWritableDatabase();
        int result = db.delete(clazz.getSimpleName(), whereClause, whereArgs);
        db.close();
        return result;
    }
}
