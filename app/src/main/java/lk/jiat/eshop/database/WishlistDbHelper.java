package lk.jiat.eshop.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.eshop.model.Product;

public class WishlistDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "wishlist.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "wishlist";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PRODUCT_ID = "product_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_IMAGE_URL = "image_url";

    public WishlistDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PRODUCT_ID + " TEXT UNIQUE, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_PRICE + " REAL, " +
                COLUMN_IMAGE_URL + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addToWishlist(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_ID, product.getProductId());
        values.put(COLUMN_TITLE, product.getTitle());
        values.put(COLUMN_PRICE, product.getPrice());
        
        String img = (product.getImages() != null && !product.getImages().isEmpty()) 
                     ? product.getImages().get(0) : "";
        values.put(COLUMN_IMAGE_URL, img);

        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    public void removeFromWishlist(String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_PRODUCT_ID + " = ?", new String[]{productId});
    }

    public boolean isWishlisted(String productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_PRODUCT_ID + " = ?", 
                new String[]{productId}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public List<Product> getAllWishlistItems() {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                Product p = new Product();
                p.setProductId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)));
                p.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                p.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)));
                
                List<String> imgs = new ArrayList<>();
                imgs.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)));
                p.setImages(imgs);
                
                list.add(p);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
