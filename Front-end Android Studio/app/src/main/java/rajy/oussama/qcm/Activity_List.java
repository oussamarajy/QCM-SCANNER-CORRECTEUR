package rajy.oussama.qcm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chyface.R;

import java.util.ArrayList;

public class Activity_List extends AppCompatActivity {
ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        list = (ListView) findViewById(R.id.list);
        ConfigDB conf = new ConfigDB(getApplicationContext());
        ArrayList<String> l_noms = new ArrayList<>();

        for(int i = 0; i<conf.getListModules().size(); i++){
            Module mod = (Module) conf.getListModules().get(i);
            l_noms.add(mod.getId_module() +"- "+mod.getDesignation());

        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, l_noms);

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), l_noms.get(position), Toast.LENGTH_SHORT).show();
                String id_module = l_noms.get(position).toString();
                Intent correction = new Intent(Activity_List.this, MainActivity.class);
                correction.putExtra("id_module", id_module.substring(0,1));
                startActivity(correction);
            }
        });
    }
}