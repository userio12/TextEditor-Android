package com.aantik.cbasefoundation;

import com.aantik.texteditor.config.EditorConstants;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.aantik.texteditor.EditView;

/*
 * Created by aanti
 * 8/24/2026 3:32 AM
 *
 *   ⋆    ႔ ႔
 *     ᠸ^ ^ ⸝⸝
 *       |、˜〵
 *       じしˍ,)⁐̤ᐷ
 *
 * Fox Mode 🍺
 */

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {

    LinearLayout mL;
    EditView a;
    LinearLayout mBar;
    String[] symbols = {
            "(",
            ")",
            "{",
            "}",
            "[",
            "]",
            "<",
            ">",
            "\"",
            "'",
            ";",
            ":",
            "=",
            "+",
            "-",
            "*",
            "/",
            "\\",
            "|",
            "&",
            "!",
            "?",
            "#",
            "$",
            "%",
            "^",
            "_",
            "~",
            "Tab"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mL = findViewById(R.id.mLayer);
        mBar = findViewById(R.id.symbolBar);

        a = new EditView(this);
        a.setText("/*" +
                "\n * HM Manager Text Editor (1.5 by Aantik)" +
                "\n * HM: www.hm2.bd" +
                "\n * HM Store: store.hm2.bd" +
                "\n *" +
                "\n * HM Manager-এর শক্তিশালী Text Editor।" +
                "\n * দ্রুত, স্মুথ এবং বড় ফাইল সম্পাদনার জন্য তৈরি।" +
                "\n * কোড ও সাধারণ টেক্সট এডিটিং সহজ করার জন্য প্রয়োজনীয়" +
                "\n * ফিচার এবং অপ্টিমাইজেশন যুক্ত করা হয়েছে।" +
                "\n *" +
                "\n * কোনো Bug বা সমস্যা পেলে রিপোর্ট করুন।" +
                "\n * Bug রিপোর্ট করার সময় সমস্যাটি কীভাবে ঘটেছে তার বিস্তারিত" +
                "\n * এবং সম্ভব হলে Screenshot বা প্রয়োজনীয় তথ্য যুক্ত করুন।" +
                "\n *" +
                "\n * HM Manager ব্যবহার করার জন্য ধন্যবাদ।" +
                "\n */" +
                "\n" +
                "\n" +
                "\n" +
                "Hello! Test received loud and clear ....... (^_^)");

        a.setWordWrap(true);

        mL.addView(a);

        _mSymbols();
    }

    private void _mSymbols() {

        for (final String symbol : symbols) {

            TextView mSymbol = new TextView(this);

            mSymbol.setText(symbol);
            mSymbol.setTextColor(EditorConstants.COLOR_TEXT);
            mSymbol.setTextSize(18);

            mSymbol.setGravity(Gravity.CENTER);

            mSymbol.setPadding(dp(10), 0, dp(10), 0);

            mSymbol.setBackgroundColor(EditorConstants.COLOR_GUTTER);

            mSymbol.setFocusable(false);
            mSymbol.setFocusableInTouchMode(false);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                    );

            mSymbol.setLayoutParams(params);

            mSymbol.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ("Tab".equals(symbol)) {
                        a.insert("\t");
                    } else {
                        a.insert(symbol);
                    }
                }
            });
            mBar.setBackgroundColor(EditorConstants.COLOR_GUTTER);
            mBar.addView(mSymbol);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_undo) {
            /// TODO: ADD SOON
            Toast.makeText(getApplicationContext(),"soon", Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_redo) {
            /// TODO: ADD SOON
            Toast.makeText(getApplicationContext(),"soon", Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.wrp) {
            a.setWordWrap(!a.isWordWrap());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    int dp(int dp)
    {
        int a = (int) (dp* getResources().getDisplayMetrics().density + 0.5f);
        return a;
    }
}
