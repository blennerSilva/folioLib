package comfolioreader.android.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.folioreader.ShowInterfacesControls;
import com.folioreader.util.FolioReader;

public class EpubReaderActivity extends AppCompatActivity {

    private FolioReader folioReader;
    private Button btnfontSize;
    private Button btnfontSizeMinus;
    private Button btnTema;
    private Button btnTemaNght;
    private Button btnCurrentPage;
    private boolean isToolbarVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epub_reader);

        folioReader = new FolioReader(this);
        folioReader.openBook(getFilesDir().getAbsolutePath() + "/" + "blenner.epub", R.id.containerEpub, this);
        folioReader.setCurrentPage(0);
        btnfontSize = findViewById(R.id.btnfontsinze);
        btnfontSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folioReader.setFontSize(4);
            }
        });

        btnfontSizeMinus = findViewById(R.id.btnfontsinzeMinus);
        btnfontSizeMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folioReader.setFontSize(1);
            }
        });

        btnTema = findViewById(R.id.btnTemaDay);
        btnTema.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folioReader.setThemeChoiceDay();
            }
        });

        btnTemaNght = findViewById(R.id.btnTemaNight);
        btnTemaNght.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folioReader.setThemeChoiceNight();
            }
        });

        btnCurrentPage = findViewById(R.id.btnCurrentPage);
        btnCurrentPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folioReader.getCurrentPage();
                Toast.makeText(EpubReaderActivity.this, "Pagina Atual" + folioReader.getCurrentPage(), Toast.LENGTH_SHORT).show();
            }
        });

        folioReader.showInterfaceControls(new ShowInterfacesControls() {
            @Override
            public void showInterfaceControls() {
                if (isToolbarVisible) {
                    hideButtons();
                } else {
                    makeButtonsVisible();
                }
            }
        });

    }

    private void makeButtonsVisible() {
        btnfontSizeMinus.setVisibility(View.VISIBLE);
        btnfontSize.setVisibility(View.VISIBLE);
        btnfontSizeMinus.setVisibility(View.VISIBLE);
        btnTema.setVisibility(View.VISIBLE);
        btnTemaNght.setVisibility(View.VISIBLE);
        btnCurrentPage.setVisibility(View.VISIBLE);
        isToolbarVisible = true;
    }

    private void hideButtons() {
        btnfontSizeMinus.setVisibility(View.GONE);
        btnfontSize.setVisibility(View.GONE);
        btnfontSizeMinus.setVisibility(View.GONE);
        btnTema.setVisibility(View.GONE);
        btnTemaNght.setVisibility(View.GONE);
        btnCurrentPage.setVisibility(View.GONE);
        isToolbarVisible = false;
    }

}
