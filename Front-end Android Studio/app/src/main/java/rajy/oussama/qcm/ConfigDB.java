package rajy.oussama.qcm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ConfigDB extends SQLiteOpenHelper {



    public static final String nomDB="data.db";
    public static final int versionDB=1;
    public ConfigDB(Context applicationContext){
        super(applicationContext,nomDB,null,versionDB);
    }


    public ConfigDB(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table module (id_module INTEGER PRIMARY KEY AUTOINCREMENT, designation TEXT, answers TEXT)");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS module");
        onCreate(db);
    }

    public Boolean InsertModule(Module mod)
    {


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("designation",mod.getDesignation());
        contentValues.put("answers",mod.getAnswers());

        long retour = db.insert("module",null,contentValues);
        if (retour==-1) return false;
        else return true;
    }


    public ArrayList getListModules(){
        ArrayList<Module> liste = new ArrayList();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cs = db.rawQuery("select * from module",null);

        cs.moveToFirst();

        while (cs.isAfterLast()==false){

            Module mod = new Module();
            mod.setId_module(cs.getInt(0));
            mod.setDesignation(cs.getString(1));
            mod.setAnswers(cs.getString(2));

            cs.moveToNext();
            liste.add(mod);
        }

        return liste;
    }

    public Boolean DeleteModule(String id){
        SQLiteDatabase db= this.getWritableDatabase();
        int retour = db.delete("module", "id=?",new String[]{id});
        if (retour ==0) return false;
        else return true;
    }

    public Boolean UpdateModule(String id,Module mod){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("designation",mod.getDesignation());

        contentValues.put("answers", mod.getAnswers());

        db.update("module",contentValues,"id=?",new String[]{id});
        return true;
    }

    public Module getModule(Integer id_module){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cs = db.rawQuery("select * from module where id_module = '"+id_module+"'", null);



        if(cs.moveToFirst()){


            Integer id = cs.getInt(0);
            String des = cs.getString(1);
            String ans = cs.getString(2);

            return new Module(id, des, ans);

        }
        return null;


    }
}
