package comfolioreader.android.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.folioreader.util.FolioReader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class EpubReaderActivity extends AppCompatActivity {

    private FolioReader folioReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epub_reader);

        folioReader = new FolioReader(this);
        folioReader.openBook("file:///android_asset/TheSilverChair.epub", R.id.containerEpub, this);


        Button btnfontSize = findViewById(R.id.btnfontsinze);
        btnfontSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folioReader.setFontSize(4);
            }
        });

        Button btnfontSizeMinus = findViewById(R.id.btnfontsinzeMinus);
        btnfontSizeMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folioReader.setFontSize(1);
            }
        });

        Button btnTema = findViewById(R.id.btnTemaDay);
        btnTema.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folioReader.setThemeChoiceDay();
            }
        });

        Button btnTemaNght = findViewById(R.id.btnTemaNight);
        btnTemaNght.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folioReader.setThemeChoiceNight();
            }
        });

        Button btnCurrentPage = findViewById(R.id.btnCurrentPage);
        btnCurrentPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folioReader.getCurrentPage();
                Toast.makeText(EpubReaderActivity.this, "Pagina Atual" + folioReader.getCurrentPage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
