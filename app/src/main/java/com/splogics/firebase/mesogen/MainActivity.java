package com.splogics.firebase.mesogen;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.waspar.calculator.CalculatorDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    ImageButton imageButtonGenerate;
    Button buttonSelectCubeSize50, buttonSelectCubeSize150, buttonInputData, buttonHelp, buttonExportData, buttonShowInputData;
    Button buttonCubeSizeOther, buttonShowReport;
    Spinner spinnerSelectIterationsBlock;
    int numSieves = 0, coordIterationsBlock;
    int numSegments = 0;
    int numberOfSieveDatasets =0;

    float voidsPercentage, voidsMaxSize, voidsMinSize;

    long startTime, endTime;

    String[] oldFileList;

    int numberOfParticlesGenerated=0;

    SQLiteDatabase sqLiteDatabaseMesoData;
    int rowIndexSieveData = 0;
    String apdlFileNameToExport, cadFileNameToExport, rawFileNameToExport;
    String modelNameToShowData="";
    boolean showDataFlag = false, showFileListFlag = false;
    boolean shapeCircular = false, includeVoidsFlag = false;

    float leftX = 0, rightX=0, topY=0, bottomY=0, frontZ=0, backZ= 0;
    float specimenLength=0, specimenWidth = 0, specimenDepth = 0;
    float aggTotalWeight, aggSpGravity, maxAggSize, minAggSize, maxPercentPass, minPercentPass;
    float specimenVolume, aggVolFraction, radiusCorrection;
    EditText editTextRadiusCorrection, editTextAggVolFraction;
    EditText editTextMixRatioCement, editTextMixRatioSand, editTextMixRatioAggregate;
    EditText editTextVoidsPercentage, editTextVoidsMinSize, editTextVoidsMaxSize;
    CheckBox checkBoxIncludeVoids;

    String fileNameToExport = "", basicFileName = "";

    TextView textViewGeneratedCount, textViewPlacedCount, textViewProgress;
    TextView textViewVoidsGeneratedCount, textViewVoidsPlacedCount;
    TextView textViewSpecimenLength, textViewSpecimenWidth, textViewSpecimenDepth, textViewTimeElapsedPlacing, textViewTimeRemainingPlacing;

    TextView textViewVoidsPercentageHeader, textViewVoidsMaxSizeHeader, textViewVoidsMinSizeHeader;

    ArrayList<Float> sieveList, percentPassList, ratioList, voidsInputDataList;
    ArrayList<Aggregate> aggregateList ;
    ArrayList<Float> aggregateRadiusList;
    ArrayList<Float> voidsRadiusList;
    ArrayList<Aggregate> voidsList;
    ArrayList<Integer> numberOfAggregatesPlacedInSegment, numberOfAggregatesGeneratedInSegment;
    ArrayList<Float> aggVolumeGeneratedInSegmentList, aggVolumePlacedInSegmentList;

    Random random = new Random();

    ProgressBar progressBarPlacing;
    File fileToBeShared;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        DatabaseOpenHelper databaseOpenHelperMesoData = new DatabaseOpenHelper(MainActivity.this);
        sqLiteDatabaseMesoData = databaseOpenHelperMesoData.getReadableDatabase();

        sieveList = new ArrayList<>();
        percentPassList = new ArrayList<>();
        ratioList = new ArrayList<>();
        voidsInputDataList = new ArrayList<>();

        aggregateRadiusList = new ArrayList<>();
        voidsRadiusList = new ArrayList<>();
        aggregateList = new ArrayList<>();
        voidsList = new ArrayList<>();

        fileToBeShared = new File(getApplicationContext().getFilesDir().toString());

        aggTotalWeight = 4.56f * 1000;
        aggSpGravity = 2.6f;

        editTextRadiusCorrection = findViewById(R.id.edit_text_radius_correction);
        editTextAggVolFraction = findViewById(R.id.edit_text_agg_vol_ratio);

        textViewGeneratedCount = findViewById(R.id.text_view_generated_count);
        textViewPlacedCount = findViewById(R.id.text_view_placed_count);
        textViewSpecimenDepth = findViewById(R.id.text_view_specimen_depth);
        textViewSpecimenWidth = findViewById(R.id.text_view_specimen_width);
        textViewSpecimenLength = findViewById(R.id.text_view_specimen_length);
        textViewTimeElapsedPlacing = findViewById(R.id.text_view_time_elapsed);
        textViewTimeRemainingPlacing = findViewById(R.id.text_view_time_remaining);

        textViewVoidsGeneratedCount = findViewById(R.id.text_view_voids_generated_count);
        textViewVoidsPlacedCount = findViewById(R.id.text_view_voids_placed_count);

        buttonSelectCubeSize50 = findViewById(R.id.button_cube_50);
        buttonSelectCubeSize150 = findViewById(R.id.button_cube_150);
        buttonCubeSizeOther = findViewById(R.id.button_cube_other);
        spinnerSelectIterationsBlock = findViewById(R.id.spinner_iterations);
        buttonInputData = findViewById(R.id.button_input_data);
        buttonHelp = findViewById(R.id.button_help);
        buttonShowInputData = findViewById(R.id.button_show_data);
        buttonExportData = findViewById(R.id.button_export_data);
        buttonShowReport = findViewById(R.id.button_Report);

        buttonSelectCubeSize50.setOnClickListener(v -> {
            specimenLength = 50;
            specimenWidth = 50;
            specimenDepth = 50;

            textViewSpecimenLength.setText(String.format("%s%s", specimenLength, getString(R.string.unit_mm)));
            textViewSpecimenWidth.setText(String.format("%s%s", specimenWidth, getString(R.string.unit_mm)));
            textViewSpecimenDepth.setText(String.format("%s%s", specimenDepth, getString(R.string.unit_mm)));
        });

        buttonSelectCubeSize150.setOnClickListener(v -> {
            specimenLength = 150;
            specimenWidth = 150;
            specimenDepth = 150;

            textViewSpecimenLength.setText(String.format("%s%s", specimenLength, getString(R.string.unit_mm)));
            textViewSpecimenWidth.setText(String.format("%s%s", specimenWidth, getString(R.string.unit_mm)));
            textViewSpecimenDepth.setText(String.format("%s%s", specimenDepth, getString(R.string.unit_mm)));
        });

        buttonShowInputData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogShowModels();
                showDataFlag = true;
                showFileListFlag = false;
            }
        });
        buttonCubeSizeOther.setOnClickListener(v -> dialogOtherSize());

        imageButtonGenerate = findViewById(R.id.button_generate);
        progressBarPlacing = findViewById(R.id.progress_bar_placing);
        progressBarPlacing.setVisibility(View.GONE);
        textViewProgress = findViewById(R.id.text_view_progress);

        buttonInputData.setOnClickListener(v -> {
            numberOfSieveDataSets();
            showDataFlag = false;
        });
        buttonShowReport.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            private boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.generated_number_item:
                        showReportGenerated();
                        break;
                    case R.id.placed_number_item:
                        showReportPlaced();
                        break;
                    case R.id.generated_volume_item:
                        showReportGeneratedVolume();
                        break;
                    case R.id.placed_volume_item:
                        showReportPlacedVolume();
                        break;
                }
                return true;
            }

            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, buttonShowReport);
                popup.getMenuInflater()
                        .inflate(R.menu.particle_report_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(this::onMenuItemClick);
                popup.show();
            }
        });

        buttonHelp.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, buttonHelp);
            popup.getMenuInflater()
                    .inflate(R.menu.help_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.help_calculator:
                        Context mContextCalc = MainActivity.this;
                        CalculatorDialog calculatorDialog = new CalculatorDialog()
                                .setAlertRadius(30)
                                .setTextButton("")
                                .setBackgroundColor(getResources().getColor(R.color.black))
                                .setButtonsTextColor(getResources().getColor(R.color.colorWhite));

                        calculatorDialog.show(getSupportFragmentManager() , "");
                        break;
                    case R.id.help_item:
                        Context mContext = MainActivity.this;
                        AlertDialog.Builder helpAlert = new AlertDialog.Builder(mContext);
                        helpAlert.setTitle(R.string.app_name);
                        WebView wv = new WebView(mContext);
                        wv.setWebViewClient(new MyWebViewClient());
                        wv.loadUrl("file:///android_asset/help.html");
                        helpAlert.setView(wv);
                        helpAlert.setPositiveButton("OK", (dialog, id) -> {
                        });
                        helpAlert.show();
                        break;
                    case R.id.about_item:
                        Context maboutContext = MainActivity.this;
                        AlertDialog.Builder alert = new AlertDialog.Builder(maboutContext);
                        alert.setTitle(R.string.app_name);
                        WebView abtWebView = new WebView(maboutContext);
                        abtWebView.setWebViewClient(new MyWebViewClient());
                        abtWebView.loadUrl("file:///android_asset/about.html");
                        alert.setView(abtWebView);
                        alert.setPositiveButton("OK", (dialog, id) -> {
                        });
                        alert.show();
                        break;
                    case R.id.disclaimer_item:
                        Context mContext1 = MainActivity.this;
                        AlertDialog.Builder alert1 = new AlertDialog.Builder(mContext1);
                        alert1.setTitle(R.string.app_name);
                        WebView wv1 = new WebView(mContext1);
                        wv1.setWebViewClient(new MyWebViewClient());
                        wv1.loadUrl("file:///android_asset/disclaimer.html");
                        alert1.setView(wv1);
                        alert1.setPositiveButton("OK", (dialog, id) -> {
                        });
                        alert1.show();
                        break;
                }
                return true;
            });
            popup.show();
        });

        buttonExportData.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, buttonExportData);
            popup.getMenuInflater()
                    .inflate(R.menu.export_data_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                    if(item.getItemId()==R.id.export_apdl_file_item){
                    fileNameToExport = apdlFileNameToExport;
                }else if(item.getItemId()==R.id.export_cad_file_item){
                    fileNameToExport = cadFileNameToExport;
                }else if(item.getItemId()==R.id.export_raw_data_file_item){
                    fileNameToExport = rawFileNameToExport;
                }else if(item.getItemId()==R.id.export_old_data_file_item) {
                    dialogShowOldFiles();
                }
                if(item.getItemId()!=R.id.export_old_data_file_item){
                    sendFileAsEmail();
                }
                return true;
            });
            popup.show();
        });

        imageButtonGenerate.setOnClickListener(v -> {
            float aggVolFractionTemp = (ratioList.get(ratioList.size() - 1))/(ratioList.get(0) + ratioList.get(1) + ratioList.get(2));
            if(aggVolFractionTemp != Float.parseFloat(editTextAggVolFraction.getText().toString())){
                AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                        MainActivity.this);
                alertDialog2.setTitle("Use calculated or current value ?");
                alertDialog2.setMessage("You want to use current or the calculated one ?");
                alertDialog2.setPositiveButton("Current",
                        (dialog, which) -> {
                            aggVolFraction = Float.parseFloat(editTextAggVolFraction.getText().toString());
                            editTextAggVolFraction.setText(String.valueOf(aggVolFraction));
                            placeAggregates();
                        });
                alertDialog2.setNegativeButton("Calculated", (dialog, which) ->{
                    aggVolFraction = aggVolFractionTemp;
                    editTextAggVolFraction.setText(String.valueOf(aggVolFraction));
                    placeAggregates();
                });
                alertDialog2.show();
            }else{
                placeAggregates();
            }
        });
    }

    public void sendFileAsEmail(){
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, fileNameToExport + " : MesoGen Data");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Pls find the data for "+fileNameToExport+" generated using MesoGen");

        ArrayList<Uri> uris = new ArrayList<>();
        File shareFile = new File(this.getFilesDir(),fileNameToExport);
        Uri contentUri = FileProvider.getUriForFile(this, "com.splogics.firebase.mesogen.Fileprovider", shareFile);
        uris.add(contentUri);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        shareIntent.setType("text/*");

        // Grant temporary read permission to the content URI
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        String msgStr = "Share...";
        startActivity(Intent.createChooser(shareIntent, msgStr));
    }

    private void dialogOtherSize() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.dialog_set_size, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextSpecimenLength = dialogView.findViewById(R.id.edit_text_length);
        final EditText editTextSpecimenWidth = dialogView.findViewById(R.id.edit_text_width);
        final EditText editTextSpecimenDepth = dialogView.findViewById(R.id.edit_text_depth);
        final RadioButton radioButtonRectangular = dialogView.findViewById(R.id.checked_text_view_rectangular);
        final RadioButton radioButtonCircular = dialogView.findViewById(R.id.checked_text_view_circular);

        final Button buttonSetSize = dialogView.findViewById(R.id.button_set_specimen_size);
        final Button buttonSetSizeCancel = dialogView.findViewById(R.id.button_update_back);
        
       radioButtonCircular.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               editTextSpecimenWidth.setVisibility(View.VISIBLE);
               editTextSpecimenLength.setHint("Length in mm");
               shapeCircular = false;
           }
       });
       
       radioButtonRectangular.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               if(radioButtonCircular.isChecked()){
                   editTextSpecimenWidth.setVisibility(View.GONE);
                   editTextSpecimenLength.setHint("Diameter in mm");
                   shapeCircular = true;
               }
           }
       });

        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonSetSize.setOnClickListener(view -> {
            String length =editTextSpecimenLength.getText().toString().trim();
            String width =editTextSpecimenWidth.getText().toString().trim();
            String depth =editTextSpecimenDepth.getText().toString().trim();

            if(length.isEmpty()){
                editTextSpecimenLength.setError("Length is required");
                if(shapeCircular){
                    editTextSpecimenLength.setError("Diameter is required");
                }
                editTextSpecimenLength.requestFocus();
                return;
            }
            if(depth.isEmpty()){
                editTextSpecimenDepth.setError("Depth is required");
                editTextSpecimenDepth.requestFocus();
                return;
            }

            if(width.isEmpty() && radioButtonRectangular.isChecked()){
                editTextSpecimenWidth.setError("Width is required");
                editTextSpecimenWidth.requestFocus();
                return;
            }

            specimenLength = Float.parseFloat( length);
            if(!shapeCircular){
                specimenWidth = Float.parseFloat( width);
            }else {
                specimenWidth = specimenLength;
            }
            specimenDepth = Float.parseFloat( depth);

            textViewSpecimenLength.setText(String.format("%s%s", specimenLength, getString(R.string.unit_mm)));
            textViewSpecimenWidth.setText(String.format("%s%s", specimenWidth, getString(R.string.unit_mm)));
            textViewSpecimenDepth.setText(String.format("%s%s", specimenDepth, getString(R.string.unit_mm)));
            b.dismiss();

        });

        buttonSetSizeCancel.setOnClickListener(view -> b.dismiss());
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void dialogInputData() {
        rowIndexSieveData = 1;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.dialog_input_data, null);
        dialogBuilder.setView(dialogView);

        textViewVoidsPercentageHeader = dialogView.findViewById(R.id.text_view_voids_percentage_header);
        textViewVoidsMaxSizeHeader = dialogView.findViewById(R.id.text_view_voids_max_size_header);
        textViewVoidsMinSizeHeader = dialogView.findViewById(R.id.text_view_voids_min_size_header);

        editTextMixRatioAggregate= dialogView.findViewById(R.id.edit_text_mix_ratio_aggregate);
        editTextMixRatioSand = dialogView.findViewById(R.id.edit_text_mix_ratio_sand);
        editTextMixRatioCement = dialogView.findViewById(R.id.edit_text_mix_ratio_cement);

        checkBoxIncludeVoids = dialogView.findViewById(R.id.check_box_voids);
        editTextVoidsPercentage = dialogView.findViewById(R.id.edit_text_voids_percentage);
        editTextVoidsMaxSize = dialogView.findViewById(R.id.edit_text_voids_max_size);
        editTextVoidsMinSize = dialogView.findViewById(R.id.edit_text_voids_min_size);

        checkBoxIncludeVoids.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                includeVoidsFlag = true;

                textViewVoidsMaxSizeHeader.setVisibility(View.VISIBLE);
                textViewVoidsPercentageHeader.setVisibility(View.VISIBLE);
                textViewVoidsMinSizeHeader.setVisibility(View.VISIBLE);

                editTextVoidsPercentage.setVisibility(View.VISIBLE);
                editTextVoidsMaxSize.setVisibility(View.VISIBLE);
                editTextVoidsMinSize.setVisibility(View.VISIBLE);
            }else{
                includeVoidsFlag = false;

                textViewVoidsMaxSizeHeader.setVisibility(View.GONE);
                textViewVoidsPercentageHeader.setVisibility(View.GONE);
                textViewVoidsMinSizeHeader.setVisibility(View.GONE);

                editTextVoidsPercentage.setVisibility(View.GONE);
                editTextVoidsMaxSize.setVisibility(View.GONE);
                editTextVoidsMinSize.setVisibility(View.GONE);
            }
        });

        ArrayList<String> tempShowSieveListArray = new ArrayList<>();
        ArrayList<String> tempShowCumPassListArray = new ArrayList<>();

        if(showDataFlag){
            Cursor mixRatioCursor = sqLiteDatabaseMesoData.rawQuery("SELECT * FROM mixRatioTable WHERE modelName ='"+ modelNameToShowData + "'", null);
            mixRatioCursor.moveToFirst();
            editTextMixRatioCement.setText(mixRatioCursor.getString(2));
            editTextMixRatioSand.setText(mixRatioCursor.getString(3));
            editTextMixRatioAggregate.setText(mixRatioCursor.getString(4));
            mixRatioCursor.close();

            Cursor cubeSizeCursor = sqLiteDatabaseMesoData.rawQuery("SELECT * FROM cubeSizeTable WHERE modelName ='"+ modelNameToShowData + "'", null);
            cubeSizeCursor.moveToFirst();
            specimenLength = Float.parseFloat (cubeSizeCursor.getString(2));
            specimenWidth = Float.parseFloat (cubeSizeCursor.getString(3));
            specimenDepth = Float.parseFloat (cubeSizeCursor.getString(4));

            textViewSpecimenLength.setText(String.format("%s%s", specimenLength, getString(R.string.unit_mm)));
            textViewSpecimenWidth.setText(String.format("%s%s", specimenWidth, getString(R.string.unit_mm)));
            textViewSpecimenDepth.setText(String.format("%s%s", specimenDepth, getString(R.string.unit_mm)));
            cubeSizeCursor.close();

            tempShowSieveListArray = new ArrayList<>();
            tempShowCumPassListArray = new ArrayList<>();
            Cursor sieveDataCursor = sqLiteDatabaseMesoData.rawQuery("SELECT * FROM sieveDataTable WHERE modelName ='"+ modelNameToShowData + "'", null);
            sieveDataCursor.moveToFirst();
            do {
                tempShowSieveListArray.add(sieveDataCursor.getString(2));
                tempShowCumPassListArray.add(sieveDataCursor.getString(3));
            } while(sieveDataCursor.moveToNext());
            numberOfSieveDatasets = sieveDataCursor.getCount();
            sieveDataCursor.close();
        }

        if(ratioList.size() !=0){
            editTextMixRatioCement.setText(ratioList.get(0).toString().trim());
            editTextMixRatioSand.setText(ratioList.get(1).toString().trim());
            editTextMixRatioAggregate.setText(ratioList.get(2).toString().trim());
        }

        if(voidsList.size() !=0){
            editTextVoidsPercentage.setText(voidsList.get(0).toString().trim());
            editTextVoidsMaxSize.setText(voidsList.get(1).toString().trim());
            editTextVoidsMinSize.setText(voidsList.get(2).toString().trim());
        }

        Cursor voidsDataCursor = sqLiteDatabaseMesoData.rawQuery("SELECT * FROM voidsTable WHERE modelName ='"+ modelNameToShowData + "'", null);
        if(voidsDataCursor.getCount()!=0){
            checkBoxIncludeVoids.setChecked(true);
        }
        voidsDataCursor.moveToFirst();
        do {
            //editTextVoidsPercentage.setText(voidsDataCursor.getString(2));
            //editTextVoidsMaxSize.setText(voidsDataCursor.getString(3));
            //editTextVoidsMinSize.setText(voidsDataCursor.getString(4));
        } while(voidsDataCursor.moveToNext());
        voidsDataCursor.close();

        final Button buttonSaveInputData = dialogView.findViewById(R.id.button_save_input_data);
        final Button buttonSetSizeCancel = dialogView.findViewById(R.id.button_save_data_back);
        final ImageView imageButtonAddSieveData,imageButtonDeleteSieveData;

        imageButtonAddSieveData = dialogView.findViewById(R.id.image_view_button_add_sieve_data);
        imageButtonDeleteSieveData = dialogView.findViewById(R.id.image_button_delete_sieve_data);

        final AlertDialog b = dialogBuilder.create();
        b.show();

        final TableLayout tableSieveData = dialogView.findViewById(R.id.table_layout_sieve_data);

        for (int i =0; i<=numberOfSieveDatasets-1; i++){
            TableRow tableRow = new TableRow(getApplicationContext());

            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            tableRow.setWeightSum(1f);

            TableRow.LayoutParams layoutParamsViewsLarge = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, .4f);

            TextView txtSieveDataSerial = new TextView(getApplicationContext());
            txtSieveDataSerial.setText(String.format("%d", rowIndexSieveData ));
            txtSieveDataSerial.setWidth(90);
            tableRow.addView(txtSieveDataSerial, 0);

            final EditText editTextSieveSize = new EditText(getApplicationContext());
            editTextSieveSize.requestFocus();
            editTextSieveSize.setInputType(InputType.TYPE_CLASS_PHONE);
            editTextSieveSize.setLayoutParams(layoutParamsViewsLarge);
            tableRow.addView(editTextSieveSize,1);

            final EditText editTextCumPercentPass = new EditText(getApplicationContext());

            editTextCumPercentPass.setLayoutParams(layoutParamsViewsLarge);
            editTextCumPercentPass.setInputType(InputType.TYPE_CLASS_PHONE);
            tableRow.addView(editTextCumPercentPass,2);

            CheckBox checkBox = new CheckBox(getApplicationContext());
            tableRow.addView(checkBox, 3);
            checkBox.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

            tableSieveData.addView(tableRow);

            if(showDataFlag & i <=tempShowCumPassListArray.size()-1){
                editTextSieveSize.setText("");
                editTextCumPercentPass.setText("");
                editTextSieveSize.setText(tempShowSieveListArray.get(i));
                editTextCumPercentPass.setText(tempShowCumPassListArray.get(i));
            }

            rowIndexSieveData++;
        }
        imageButtonAddSieveData.setOnClickListener(v -> {

        });
        imageButtonDeleteSieveData.setOnClickListener(v -> {

            int rowCount = tableSieveData.getChildCount();

            List<Integer> deleteRowNumberList = new ArrayList<>();

            for(int i =0 ;i < rowCount;i++)
            {
                View rowView = tableSieveData.getChildAt(i);

                if(rowView instanceof TableRow)
                {
                    TableRow tableRow = (TableRow)rowView;

                    int columnCount = tableRow.getChildCount();

                    for(int j = 0;j<columnCount;j++)
                    {
                        View columnView = tableRow.getChildAt(j);
                        if(columnView instanceof CheckBox)
                        {
                            CheckBox checkboxView = (CheckBox)columnView;
                            if(checkboxView.isChecked())
                            {
                                deleteRowNumberList.add(i);
                                break;
                            }
                        }
                    }
                }
            }
            if(deleteRowNumberList.size()<=1) {
                for (int rowNumber : deleteRowNumberList) {
                    tableSieveData.removeViewAt(rowNumber);
                }
                if(rowIndexSieveData>1){
                    rowIndexSieveData -= 1;
                }else if(rowIndexSieveData <=0)
                    rowIndexSieveData = 1;
            }
        });
        buttonSaveInputData.setOnClickListener((View view) -> {

            ratioList.clear();
            ratioList.add(0,Float.parseFloat(editTextMixRatioCement.getText().toString().trim()));
            ratioList.add(1,Float.parseFloat(editTextMixRatioSand.getText().toString().trim()));
            ratioList.add(2,Float.parseFloat(editTextMixRatioAggregate.getText().toString().trim()));

            voidsPercentage = Float.parseFloat((editTextVoidsPercentage.getText().toString().trim()));
            voidsMaxSize = Float.parseFloat((editTextVoidsMaxSize.getText().toString().trim()));
            voidsMinSize = Float.parseFloat((editTextVoidsMinSize.getText().toString().trim()));

            voidsInputDataList.clear();
            voidsInputDataList.add(0,voidsPercentage);
            voidsInputDataList.add(1,voidsPercentage);
            voidsInputDataList.add(2,voidsPercentage);

            aggVolFraction = (ratioList.get(ratioList.size() - 1))/(ratioList.get(0) + ratioList.get(1) + ratioList.get(2));
            editTextAggVolFraction.setText(String.valueOf(aggVolFraction));

            sieveList.clear();
            percentPassList.clear();
            int rowCountSieveDataTable = tableSieveData.getChildCount();

            for(int i =0 ;i < rowCountSieveDataTable; i++) {
                View rowView = tableSieveData.getChildAt(i);
                float sieveSize, cumPercentagePass;
                if (rowView instanceof TableRow) {
                    TableRow tableRow = (TableRow) rowView;
                    int columnCount = tableRow.getChildCount();
                    for (int j = 1; j < columnCount; j++) {
                        View columnView = tableRow.getChildAt(j);
                        if (columnView instanceof EditText) {
                            EditText editTextSieveSize = (EditText) columnView;
                            switch (j) {
                                case 1:
                                    if (editTextSieveSize.getText().toString().equals("")) {
                                        editTextSieveSize.setError("Enter sieve size");
                                        return;
                                    }

                                    sieveSize = Float.parseFloat(editTextSieveSize.getText().toString().trim());
                                    sieveList.add(i, sieveSize);
                                    break;
                                case 2:
                                    if(editTextSieveSize.getText().toString().equals("")){
                                        editTextSieveSize.setError("Enter Cum. Pass Percent");
                                        return;
                                    }
                                    cumPercentagePass = Float.parseFloat(editTextSieveSize.getText().toString().trim());
                                    percentPassList.add(i, cumPercentagePass);
                                    break;
                            }
                        }
                    }
                }
            }
            b.dismiss();
        });
        buttonSetSizeCancel.setOnClickListener(view1 -> b.dismiss());
    }

    private void dialogShowModels() {
        rowIndexSieveData = 1;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.dialog_show_input_data, null);
        dialogBuilder.setView(dialogView);

        Cursor cursorModelList = sqLiteDatabaseMesoData.rawQuery("SELECT DISTINCT modelName FROM mixRatioTable ORDER BY modelName", null);
        if(cursorModelList.getCount()==0){
            Toast.makeText(this, "No data saved yet. Go to Input button and create a model...", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> showDataAdapter;
        ListView listViewModelList;
        ArrayList<String> savedDataItems;
        savedDataItems = new ArrayList<>();
        listViewModelList = dialogView.findViewById(R.id.list_view_show_data);
        listViewModelList.setAdapter(null);

        cursorModelList.moveToFirst();
        do {
            savedDataItems.add(cursorModelList.getString(0));
        } while(cursorModelList.moveToNext());
        cursorModelList.close();

        if(showFileListFlag){
            oldFileList = getApplicationContext().fileList();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, oldFileList);
            listViewModelList.setAdapter(adapter);
        }else{
            listViewModelList.setAdapter(null);
            showDataAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,savedDataItems);
            listViewModelList.setAdapter(showDataAdapter);
        }
        final AlertDialog b = dialogBuilder.create();
        b.show();

        listViewModelList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                modelNameToShowData = listViewModelList.getItemAtPosition(position).toString();
                if(showFileListFlag){
                    fileNameToExport = modelNameToShowData;
                    sendFileAsEmail();
                    listViewModelList.setAdapter(null);
                    showFileListFlag = false;
                    b.dismiss();
                }else{
                    dialogInputData();
                    listViewModelList.setAdapter(null);
                    showFileListFlag = false;
                    b.dismiss();
                }
            }
        });

        Button buttonCancelDialog = dialogView.findViewById(R.id.button_cancel_show_dat);
        buttonCancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewModelList.setAdapter(null);
                b.dismiss();
            }
        });
    }

    private void dialogShowOldFiles() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.dialog_show_input_data, null);
        dialogBuilder.setView(dialogView);
        ListView listViewModeList;
        listViewModeList = dialogView.findViewById(R.id.list_view_show_data);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getApplicationContext().fileList());
        listViewModeList.setAdapter(adapter);
        final AlertDialog b = dialogBuilder.create();
        b.show();
        listViewModeList.setOnItemClickListener((parent, view, position, id) -> {
            modelNameToShowData = listViewModeList.getItemAtPosition(position).toString();
            fileNameToExport = modelNameToShowData;
            sendFileAsEmail();
            b.dismiss();
        });

        Button buttonCancelDialog = dialogView.findViewById(R.id.button_cancel_show_dat);
        buttonCancelDialog.setOnClickListener(v -> b.dismiss());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void numberOfSieveDataSets() {
        final Dialog d = new Dialog(MainActivity.this);
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.number_picker_dialog);
        Button b1 = d.findViewById(R.id.button1);
        final EditText editTextNoOfSieveData = d.findViewById(R.id.edit_text_number_of_sieve_data);

        b1.setOnClickListener(view -> {

            String dataNumber = editTextNoOfSieveData.getText().toString().trim();

            if(dataNumber.isEmpty() ){
                editTextNoOfSieveData.setError("Number is required");
                editTextNoOfSieveData.requestFocus();
                return;
            }
            numberOfSieveDatasets =Integer.parseInt(dataNumber);
            dialogInputData();
            d.dismiss();

        });

        d.show();
    }


    private static class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }

    public void placeAggregates(){
        super.onStart();

        textViewGeneratedCount.setText("0");
        textViewVoidsGeneratedCount.setText("0");
        textViewPlacedCount.setText("0");
        textViewVoidsPlacedCount.setText("0");

        AggregateGenerationTask gt = new AggregateGenerationTask();
        gt.execute();
        AggregatePlacingTask pt = new AggregatePlacingTask();
        pt.execute();
        if(includeVoidsFlag){
            VoidsGenerationTask vgt = new VoidsGenerationTask();
            vgt.execute();
            VoidsPlacingTask vpt = new VoidsPlacingTask();
            vpt.execute();
        }
    }

    private class AggregateGenerationTask extends AsyncTask<String, Integer, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startTime = System.currentTimeMillis();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            long seconds = (endTime - startTime) / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            String time = hours % 24 + ":" + minutes % 60 + ":" + seconds % 60;
            textViewTimeElapsedPlacing.setVisibility(View.VISIBLE);
            textViewTimeElapsedPlacing.setText(time);

            //int progressPercent = aggregateRadiusList.size();
            //textViewProgress.setText(""+ progressPercent);
            textViewGeneratedCount.setText(String.valueOf(numberOfParticlesGenerated));
            endTime = System.currentTimeMillis();
        }

        @Override
        protected String doInBackground(String... strings) {
            if(sieveList.size()!=0 && percentPassList.size()!=0)
            {
                numSieves = sieveList.size();
                numSegments = numSieves -1;
                maxAggSize = Collections.max(sieveList);
                minAggSize = Collections.min(sieveList);
                maxPercentPass = Collections.max(percentPassList);
                minPercentPass = Collections.min(percentPassList);

                if(specimenLength != 0 && specimenWidth !=0 && specimenDepth !=0){
                    leftX = 0;
                    rightX=specimenLength;
                    topY=specimenDepth;
                    bottomY=0;
                    frontZ=0;
                    backZ= specimenWidth;
                    specimenVolume = (rightX - leftX) * (topY - bottomY) * (backZ - frontZ);
                    coordIterationsBlock = Integer.parseInt(spinnerSelectIterationsBlock.getSelectedItem().toString());
                    aggregateRadiusList.clear();

                    float aggVolRemainder = 0;

                    aggVolumeGeneratedInSegmentList = new ArrayList<>();
                    numberOfAggregatesGeneratedInSegment = new ArrayList<>();

                    for (int i = 0; i < numSegments; i++) {

                        float maxAggSizeInSegment = sieveList.get(i);
                        float minAggSizeInSegment = sieveList.get(i + 1);
                        float maxPercentPassInSegment = percentPassList.get(i);
                        float minPercentPassInSegment = percentPassList.get(i + 1);
                        float aggVolInSegment = (maxPercentPassInSegment - minPercentPassInSegment) * aggVolFraction * specimenVolume / (maxPercentPass - minPercentPass);

                        aggVolInSegment = aggVolInSegment + aggVolRemainder;
                        aggVolRemainder = 0;
                        float particleVolume = 0;
                        int particlesGenerated = 0;
                        float aggVolumeGeneratedInSegment = 0.0f;
                        while (aggVolInSegment >= particleVolume && aggVolInSegment !=0) {
                            float particleRadius = (minAggSizeInSegment + random.nextFloat() * (maxAggSizeInSegment - minAggSizeInSegment)) / 2;
                            particleVolume = (float) (4 * Math.PI * Math.pow(particleRadius, 3) / 3);
                            aggVolInSegment = aggVolInSegment - particleVolume;
                            aggregateRadiusList.add(particleRadius);
                            particlesGenerated ++;
                            aggVolumeGeneratedInSegment = aggVolumeGeneratedInSegment + particleVolume;
                            numberOfParticlesGenerated = particlesGenerated;
                            Log.d("particleRadius_G", "  "  + particleRadius + "\n");
                            publishProgress(i);
                            publishProgress(aggregateRadiusList.size());
                        }
                        Log.d("particlesInSegment", "  "  + particlesGenerated + "\n");
                        if (aggVolInSegment > 0) {
                            aggVolRemainder = aggVolInSegment;
                        }
                        Log.d("Particle# :", " " + aggregateRadiusList.size());
                        numberOfAggregatesGeneratedInSegment.add(particlesGenerated);
                        aggVolumeGeneratedInSegmentList.add(aggVolumeGeneratedInSegment);
                    }

                    if(buttonCubeSizeOther.getText().equals("50")) {
                        buttonCubeSizeOther.setVisibility(View.GONE);
                        buttonSelectCubeSize150.setVisibility(View.GONE);
                    }else if(buttonCubeSizeOther.getText().equals("150")) {
                        buttonCubeSizeOther.setVisibility(View.GONE);
                        buttonSelectCubeSize50.setVisibility(View.GONE);
                    } else if(buttonCubeSizeOther.getText().equals("Other")){
                        buttonSelectCubeSize50.setVisibility(View.GONE);
                        buttonSelectCubeSize150.setVisibility(View.GONE);
                    }
                    basicFileName = aggregateRadiusList.size() +"_spheres_"+java.text.DateFormat.getDateTimeInstance().format(new Date());

                    ContentValues contentValuesSaveCubeData = new ContentValues();
                    ContentValues contentValuesSaveMixData = new ContentValues();
                    ContentValues contentValuesSaveSieveData = new ContentValues();
                    ContentValues contentValuesVoidsData = new ContentValues();

                    try {
                        contentValuesSaveCubeData.put("modelName", basicFileName);
                        contentValuesSaveCubeData.put("specimenLength", specimenLength);
                        contentValuesSaveCubeData.put("specimenWidth", specimenWidth);
                        contentValuesSaveCubeData.put("specimenDepth", specimenDepth);

                        contentValuesSaveMixData.put("modelName", basicFileName);
                        contentValuesSaveMixData.put("mixRatioCement", ratioList.get(0));
                        contentValuesSaveMixData.put("mixRatioSand", ratioList.get(1));
                        contentValuesSaveMixData.put("mixRatioAggregate", ratioList.get(2));

                        if(includeVoidsFlag){
                            contentValuesVoidsData.put("modelName", basicFileName);
                            contentValuesVoidsData.put("voidsPercentage", voidsPercentage);
                            contentValuesVoidsData.put("voidsMaxSize", voidsMaxSize);
                            contentValuesVoidsData.put("voidsMinSize", voidsMinSize);

                        }

                        long cubeDataInsertResult = sqLiteDatabaseMesoData.insert("cubeSizeTable", null, contentValuesSaveCubeData);
                        long mixDataInsertResult = sqLiteDatabaseMesoData.insert("mixRatioTable", null, contentValuesSaveMixData);
                        long voidsDataInsertResult = sqLiteDatabaseMesoData.insert("voidsTable", null, contentValuesVoidsData);

                        long sieveDataInsertResult=0;
                        for(int i = 0; i<=sieveList.size()-1; i++){

                            contentValuesSaveSieveData.put("modelName", basicFileName);
                            contentValuesSaveSieveData.put("sieveSize",sieveList.get(i));
                            contentValuesSaveSieveData.put("cumPercentPass",percentPassList.get(i));
                            sieveDataInsertResult = sqLiteDatabaseMesoData.insert("sieveDataTable", null, contentValuesSaveSieveData);
                        }

                        if(cubeDataInsertResult>0 && mixDataInsertResult >0 && sieveDataInsertResult >0 && voidsDataInsertResult > 0){
                            Toast.makeText(getApplicationContext(), "Data saved...", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(), "Error saving data... Check the values...", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    /*try {
                        contentValuesSaveCubeData.put("modelName", basicFileName);
                        contentValuesSaveCubeData.put("specimenLength", specimenLength);
                        contentValuesSaveCubeData.put("specimenWidth", specimenWidth);
                        contentValuesSaveCubeData.put("specimenDepth", specimenDepth);

                        contentValuesSaveMixData.put("modelName", basicFileName);
                        contentValuesSaveMixData.put("mixRatioCement", ratioList.get(0));
                        contentValuesSaveMixData.put("mixRatioSand", ratioList.get(1));
                        contentValuesSaveMixData.put("mixRatioAggregate", ratioList.get(2));

                        long cubeDataInsertResult = sqLiteDatabaseMesoData.insert("cubeSizeTable", null, contentValuesSaveCubeData);
                        long mixDataInsertResult = sqLiteDatabaseMesoData.insert("mixRatioTable", null, contentValuesSaveMixData);
                        long sieveDataInsertResult=0;
                        for(int i = 0; i<=sieveList.size()-1; i++){

                            contentValuesSaveSieveData.put("modelName", basicFileName);
                            contentValuesSaveSieveData.put("sieveSize",sieveList.get(i));
                            contentValuesSaveSieveData.put("cumPercentPass",percentPassList.get(i));
                            sieveDataInsertResult = sqLiteDatabaseMesoData.insert("sieveDataTable", null, contentValuesSaveSieveData);
                        }

                        if(cubeDataInsertResult>0 && mixDataInsertResult >0 && sieveDataInsertResult >0){
                            Toast.makeText(getApplicationContext(), "Data saved...", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(), "Error saving data... Check the values...", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/

                }else {
                    dialogOtherSize();
                }
            }else {
                if (sieveList.size() != 0) {
                    percentPassList.size();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    numberOfSieveDataSets();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            textViewGeneratedCount.setText(String.valueOf(aggregateRadiusList.size()));

        }
    }

    private class AggregatePlacingTask extends AsyncTask<String, Integer, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarPlacing.setVisibility(View.VISIBLE);
            startTime = System.currentTimeMillis();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            int progressPercent = 100 * aggregateList.size()/ aggregateRadiusList.size();

            progressBarPlacing.setProgress(progressPercent);
            textViewProgress.setText(MessageFormat.format("{0}", progressPercent));
            textViewPlacedCount.setText(String.valueOf(aggregateList.size()));
            endTime = System.currentTimeMillis();

            long seconds = (endTime - startTime) / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            String time = hours % 24 + ":" + minutes % 60 + ":" + seconds % 60;
            textViewTimeElapsedPlacing.setVisibility(View.VISIBLE);
            textViewTimeElapsedPlacing.setText(time);

            long remSeconds = seconds * aggregateRadiusList.size()/aggregateList.size();
            long remMinutes = remSeconds / 60;
            long remHours = remMinutes / 60;
            String remTime = remHours % 24 + ":" + remMinutes % 60 + ":" + remSeconds % 60;
            textViewTimeRemainingPlacing.setVisibility(View.VISIBLE);
            textViewTimeRemainingPlacing.setText(remTime);
        }

        @Override
        protected String doInBackground(String... strings) {

            if(sieveList.size()!=0 && percentPassList.size()!=0)
            {
                int coordIterations = 0;
                aggregateList.clear();
                FileOutputStream fileOut_Apdl, fileOut_Acad, fileOut_Data;
                OutputStreamWriter outputWriter_apdl = null, outputWriter_acad = null, outputWriter_data = null;
                try {
                    fileOut_Apdl = openFileOutput(basicFileName+"_apdl.lgw", MODE_PRIVATE);
                    apdlFileNameToExport = basicFileName+"_apdl.lgw";
                    fileOut_Acad = openFileOutput(basicFileName+"_cad.txt", MODE_PRIVATE);
                    cadFileNameToExport = basicFileName+"_cad.txt";
                    fileOut_Data = openFileOutput(basicFileName+"_data.txt", MODE_PRIVATE);
                    rawFileNameToExport = basicFileName+"_data.txt";

                    outputWriter_apdl = new OutputStreamWriter(fileOut_Apdl);
                    outputWriter_acad = new OutputStreamWriter(fileOut_Acad);
                    outputWriter_data = new OutputStreamWriter(fileOut_Data);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                radiusCorrection = Float.parseFloat(editTextRadiusCorrection.getText().toString());

                for (int i = 0; i <= aggregateRadiusList.size()-1; i++) {
                    float particleRadius = aggregateRadiusList.get(i);
                    boolean canBePlaced = false;
                    while (coordIterations < coordIterationsBlock && !canBePlaced) {
                        if(coordIterations == coordIterationsBlock -1){
                            //                                if(particleRadius>=minAggSize/2){
                            particleRadius = particleRadius * radiusCorrection;
                            //                                }
                        }
                        float xCoord = leftX + random.nextFloat() * (rightX - leftX);
                        float yCoord = topY + random.nextFloat() * (bottomY - topY);
                        float zCoord = backZ + random.nextFloat() * (frontZ - backZ);
                        if (xCoord >= leftX + particleRadius && xCoord <= rightX - particleRadius && yCoord >= bottomY + particleRadius && yCoord <= topY - particleRadius
                                && zCoord >= frontZ + particleRadius && zCoord <= backZ - particleRadius ) {
                            Aggregate aggregate = new Aggregate();
                            aggregate.xCoord = xCoord;
                            aggregate.yCoord = yCoord;
                            aggregate.zCoord = zCoord;
                            aggregate.radius = particleRadius;
                            if (aggregateList.size() == 0 ) {
                                aggregateList.add(aggregate);
                                canBePlaced = true;
                                coordIterations = 0;

                                try {
                                    assert outputWriter_apdl != null;

                                    outputWriter_apdl.write("/BATCH"+"\n"+"! /COM,ANSYS RELEASE 2020 R2           BUILD 20.2      UP20200601       21:21:49"+"\n" +"!*"+"\n" +"! /REPLOT,RESIZE"+"\n"+"/PREP7"+ "\n");
                                    outputWriter_apdl.write("BLC4,0,0,"+specimenLength+","+ specimenWidth+","+specimenDepth+"\n");
                                    outputWriter_apdl.write("SPH4,"+xCoord + "," + yCoord + "," + particleRadius + "\n");
                                    outputWriter_apdl.write("FLST,3,1,6,ORDE,1\n");
                                    int particleNo = i+1;
                                    outputWriter_apdl.write("FITEM,3,"+ particleNo +"\n");
                                    outputWriter_apdl.write("VGEN,,P51X,,,,,"+ zCoord + ",,,1\n");
                                    ////////////////////////////////////////////////////////////////////////////
                                    outputWriter_apdl.write("VSBV,1,"+ particleNo +"\n");

                                    ////////////////////////////////////////////////////////////////////////////

                                    outputWriter_acad.write("SPHERE"+ "\n");
                                    outputWriter_acad.write(xCoord + "," + yCoord + "," + zCoord + "\n" + particleRadius + "\n\n");

                                    outputWriter_data.write(aggregateRadiusList.size()+"_Particles" + "\n");
                                    outputWriter_data.write(xCoord + "," + yCoord + "," + zCoord + "," + particleRadius + "\n");

                                    Log.d("sphereData", i + " -" +  xCoord + "," + yCoord + "," + zCoord + "\n" + particleRadius + "\n\n");
                                    Log.d("sphereData", i + " - " + "\n\n");

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {

                                for (int j = 0; j <= aggregateList.size() - 1; j++) {
                                    float centralDistance = (float) Math.sqrt(Math.pow((xCoord - aggregateList.get(j).xCoord), 2) + Math.pow((yCoord - aggregateList.get(j).yCoord), 2) + Math.pow((zCoord - aggregateList.get(j).zCoord), 2));
                                    float sumRadii = particleRadius + aggregateList.get(j).radius;
                                    float particleGap = centralDistance - sumRadii;
                                    if (centralDistance >= sumRadii && particleGap >= 1.0f) {
                                        canBePlaced = true;
                                    } else {
                                        canBePlaced = false;
                                        break;
                                    }
                                }
                                if (canBePlaced) {
                                    if(particleRadius >= minAggSize/2){
                                        aggregateList.add(aggregate);

                                        publishProgress(aggregateList.size());
                                        publishProgress(i);
                                        try {
                                            assert outputWriter_apdl != null;

                                                outputWriter_apdl.write("SPH4,"+xCoord + "," + yCoord + "," + particleRadius + "\n");
                                                outputWriter_apdl.write("FLST,3,1,6,ORDE,1\n");
                                                int particleNo = i+1;
                                                outputWriter_apdl.write("FITEM,3,"+particleNo+"\n");
                                                outputWriter_apdl.write("VGEN,,P51X,,,,,"+zCoord + ",,,1\n");
                                                outputWriter_apdl.write("VSBV,1,"+ particleNo +"\n");

                                                if(i!= aggregateRadiusList.size()-1){
                                                    outputWriter_acad.write(xCoord + "," + yCoord + "," + zCoord + "\n" + particleRadius + "\n\n");
                                                }else{
                                                    outputWriter_acad.write(xCoord + "," + yCoord + "," + zCoord + "\n" + particleRadius + "\n");
                                                }
                                                outputWriter_data.write(xCoord + "," + yCoord + "," + zCoord +"," + particleRadius + "\n");

                                                Log.d("sphereData", i + " - " +  xCoord + "," + yCoord + "," + zCoord + "\n" + particleRadius + "\n\n");
                                                Log.d("sphereData", i + " - " + "\n\n");


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        coordIterations = 0;
                                    }
                                }
                            }
                        } else {
                            coordIterations++;
                            if(coordIterations >=coordIterationsBlock ) {
                                coordIterations = coordIterationsBlock - 1;
                            }
                        }
                    }
                }

                try {
                    assert outputWriter_apdl != null;
                    outputWriter_apdl.write("! LGWRITE,'test','lgw','E:\\Shaji\\ansys - meso\\Models\\16-05-2021\\',COMMENT  " +"\n");
                    outputWriter_apdl.close();
                    outputWriter_acad.write("z\n_e\nUNION\nall\n\nCOPYBASE\n0,0,0\nall\n\nBOX\n0,0,0\n"+specimenLength+","+specimenWidth+","+specimenDepth+"\nSUBTRACT\nlast\n\nall\nr\nl\n\nPASTECLIP\n0,0,0\nSAVE\n");
                    outputWriter_acad.close();
                    outputWriter_data.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                dialogOtherSize();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBarPlacing.setVisibility(View.GONE);

            numberOfAggregatesPlacedInSegment = new ArrayList<>();
            aggVolumePlacedInSegmentList = new ArrayList<>();
            for (int i = 0; i < numSegments; i++) {
                int range = 0;
                float maxAggSizeInSegment = sieveList.get(i);
                float minAggSizeInSegment = sieveList.get(i + 1);
                float aggVolumeInSegment = 0.0f;

                for (int j = 0; j < aggregateList.size(); j++) {
                    if ( aggregateList.get(j).radius<=maxAggSizeInSegment/2 & aggregateList.get(j).radius>=minAggSizeInSegment/2){
                        aggVolumeInSegment = aggVolumeInSegment + (float) (4 * Math.PI * Math.pow(aggregateList.get(j).radius, 3) / 3);
                        ++range;
                    }
                }
                numberOfAggregatesPlacedInSegment.add(range);
                aggVolumePlacedInSegmentList.add(aggVolumeInSegment);
            }
            showReportPlacedVolume();
        }
    }

    private  class VoidsGenerationTask extends AsyncTask<String, Integer, String>{

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... strings) {
            float volumeOfVoids = specimenVolume * aggVolFraction * voidsPercentage / 100;

            coordIterationsBlock = Integer.parseInt(spinnerSelectIterationsBlock.getSelectedItem().toString());

            Random randomVoidsGeneration = new Random();

            voidsRadiusList.clear();

            float voidParticleVolume = 0;
            while (volumeOfVoids >= voidParticleVolume) {
                float voidParticleRadius = (voidsMinSize + randomVoidsGeneration.nextFloat() * (voidsMaxSize - voidsMinSize)) / 2;
                voidParticleVolume = (float) (4 * Math.PI * Math.pow(voidParticleRadius, 3) / 3);
                volumeOfVoids = volumeOfVoids - voidParticleVolume;
                voidsRadiusList.add(voidParticleRadius);
                Log.d("voidParticleRadius", "-"+ voidParticleRadius);
            }

            Log.d("voidParticle# :", " " + voidsRadiusList.size());
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values){

            super.onProgressUpdate(values);
            textViewVoidsGeneratedCount.setText(String.valueOf(voidsRadiusList.size()));
        }
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            textViewVoidsGeneratedCount.setText(String.valueOf(voidsRadiusList.size()));
        }
    }

    private class VoidsPlacingTask extends AsyncTask<String, Integer, String>{

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            startTime = System.currentTimeMillis();;
        }
        @Override
        protected String doInBackground(String... strings) {
            int coordIterations = 0;
            voidsList.clear();

            FileOutputStream fileOut_Voids_Apdl, fileOut_Voids_Acad, fileOut_Voids_Data;
            OutputStreamWriter outputWriter_voids_apdl = null, outputWriter_voids_acad = null, outputWriter_voids_data = null;
            try {
                fileOut_Voids_Apdl = openFileOutput(basicFileName+"_voids_apdl.lgw", MODE_PRIVATE);
                apdlFileNameToExport = basicFileName+"_voids_apdl.lgw";
                fileOut_Voids_Acad = openFileOutput(basicFileName+"_voids_cad.txt", MODE_PRIVATE);
                cadFileNameToExport = basicFileName+"_voids_cad.txt";
                fileOut_Voids_Data = openFileOutput(basicFileName+"_voids_data.txt", MODE_PRIVATE);
                rawFileNameToExport = basicFileName+"_voids_data.txt";

                outputWriter_voids_apdl = new OutputStreamWriter(fileOut_Voids_Apdl);
                outputWriter_voids_acad = new OutputStreamWriter(fileOut_Voids_Acad);
                outputWriter_voids_data = new OutputStreamWriter(fileOut_Voids_Data);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Random randomVoidsPlacing = new Random();
            for (int i = 1; i <= voidsRadiusList.size() - 1; i++) {
                float voidParticleRadius = voidsRadiusList.get(i);
                boolean canBePlacedVoid = false;
                while (coordIterations < coordIterationsBlock && !canBePlacedVoid) {
                    if(coordIterations == coordIterationsBlock -1){
                        if(voidParticleRadius>=voidsMinSize/2){
                            voidParticleRadius = voidParticleRadius * radiusCorrection;
                        }
                    }
                    float xCoord = leftX + randomVoidsPlacing.nextFloat() * (rightX - leftX);
                    float yCoord = topY + randomVoidsPlacing.nextFloat() * (bottomY - topY);
                    float zCoord = backZ + randomVoidsPlacing.nextFloat() * (frontZ - backZ);
                    if (xCoord >= leftX + voidParticleRadius && xCoord <= rightX - voidParticleRadius && yCoord >= bottomY + voidParticleRadius && yCoord <= topY - voidParticleRadius
                            && zCoord >= frontZ + voidParticleRadius && zCoord <= backZ - voidParticleRadius) {

                        Aggregate vAggregate = new Aggregate();
                        vAggregate.xCoord = xCoord;
                        vAggregate.yCoord = yCoord;
                        vAggregate.zCoord = zCoord;
                        vAggregate.radius = voidParticleRadius;

                        for (int j = 0; j <= aggregateList.size() - 1; j++) {
                            float centralDistance = (float) Math.sqrt(Math.pow((xCoord - aggregateList.get(j).xCoord), 2) + Math.pow((yCoord - aggregateList.get(j).yCoord), 2) + Math.pow((zCoord - aggregateList.get(j).zCoord), 2));
                            float sumRadii = voidParticleRadius + aggregateList.get(j).radius;
                            if (centralDistance >= sumRadii ) {
                                canBePlacedVoid = true;
                            } else {
                                canBePlacedVoid = false;
                                break;
                            }
                        }

                        if(canBePlacedVoid){
                            if(voidsList.size()==0){
                                voidsList.add(vAggregate);
                            }else{
                                for (int j = 0; j <= voidsList.size() - 1; j++) {
                                    float centralDistance = (float) Math.sqrt(Math.pow((xCoord - voidsList.get(j).xCoord), 2) + Math.pow((yCoord - voidsList.get(j).yCoord), 2) + Math.pow((zCoord - voidsList.get(j).zCoord), 2));
                                    float sumRadii = voidParticleRadius + voidsList.get(j).radius;
                                    if (centralDistance >= sumRadii ) {
                                        canBePlacedVoid = true;
                                    } else {
                                        canBePlacedVoid = false;
                                        break;
                                    }
                                }
                            }
                        }

                        if (canBePlacedVoid) {
                            voidsList.add(vAggregate);

                            try {
                                FileOutputStream fileOut_apdl = openFileOutput("void_spheres_apdl.txt", MODE_APPEND);
                                outputWriter_voids_apdl.write("SPH4,"+xCoord + "," + yCoord + "," + voidParticleRadius + "\n");
                                outputWriter_voids_apdl.write("FLST,3,1,6,ORDE,1\n");
                                outputWriter_voids_apdl.write("FITEM,3,"+i+"\n");
                                outputWriter_voids_apdl.write("VGEN,,P51X,,,,,"+zCoord + ",,,1\n");

                                FileOutputStream fileOut = openFileOutput("void_sphere_list.txt", MODE_APPEND);
                                OutputStreamWriter outputWriter = new OutputStreamWriter(fileOut);
                                outputWriter.write(xCoord + "," + yCoord + "," + zCoord + "\n" + voidParticleRadius + "\n\n");
                                Log.d("vSphereData", i + " -" +  xCoord + "," + yCoord + "," + zCoord + "\n" + voidParticleRadius + "\n\n");
                                Log.d("vSphereData", i + " - " + "\n\n");

                                if(i!= aggregateRadiusList.size()-1){
                                    outputWriter_voids_acad.write(xCoord + "," + yCoord + "," + zCoord + "\n" + voidParticleRadius + "\n\n");
                                }else{
                                    outputWriter_voids_acad.write(xCoord + "," + yCoord + "," + zCoord + "\n" + voidParticleRadius + "\n");
                                }


                                outputWriter_voids_data.write(xCoord + "," + yCoord + "," + zCoord +"," + voidParticleRadius + "\n");

                                Log.d("sphereData", i + " - " +  xCoord + "," + yCoord + "," + zCoord + "\n" + voidParticleRadius + "\n\n");
                                Log.d("sphereData", i + " - " + "\n\n");


                                outputWriter_voids_apdl.close();
                                outputWriter.close();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            coordIterations = 0;
                        }

                    } else {
                        coordIterations++;
                        if(coordIterations >=coordIterationsBlock ) {
                            coordIterations = coordIterationsBlock - 1;
                        }
                    }
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values){

            super.onProgressUpdate(values);
            textViewVoidsPlacedCount.setText(String.valueOf(voidsList.size()));

            endTime = System.currentTimeMillis();

            long seconds = (endTime - startTime) / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            String time = hours % 24 + ":" + minutes % 60 + ":" + seconds % 60;
            textViewTimeElapsedPlacing.setVisibility(View.VISIBLE);
            textViewTimeElapsedPlacing.setText(time);
        }

        @Override
        protected void onPostExecute(String s){
            textViewVoidsPlacedCount.setText(String.valueOf(voidsList.size()));
        }
    }

    private void showReportPlaced()    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Number of Particles Placed in Each Range");

        final List<String> numberOfParticles = new ArrayList<>();

        for (int i = 0; i < numberOfAggregatesPlacedInSegment.size(); i++) {
            String listDataRow = sieveList.get(i) + " - " + sieveList.get(i+1) +" : ";
            numberOfParticles.add(listDataRow +  numberOfAggregatesPlacedInSegment.get(i));
        }
        numberOfParticles.add("Total : "+ findArrayListSum(numberOfAggregatesPlacedInSegment));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, numberOfParticles);
        builder.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(MainActivity.this,"You have selected " + numberOfParticles.get(which),Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void showReportPlacedVolume()    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Number of Particles Placed in Each Range");

        final List<String> volumeOfParticles = new ArrayList<>();

        for (int i = 0; i < aggVolumePlacedInSegmentList.size(); i++) {
            String listDataRow = sieveList.get(i) + " - " + sieveList.get(i+1) +" : ";
            volumeOfParticles.add(listDataRow +  aggVolumePlacedInSegmentList.get(i));
        }
        volumeOfParticles.add("Total : "+ findArrayListVolumeSum(aggVolumePlacedInSegmentList));
        volumeOfParticles.add("VAR : "+ findArrayListVolumeSum(aggVolumePlacedInSegmentList)/(specimenVolume));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, volumeOfParticles);
        builder.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(MainActivity.this,"You have selected " + numberOfParticles.get(which),Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showReportGenerated()    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Number of Particles Generated in Each Range");

        final List<String> numberOfParticlesGenerated = new ArrayList<>();

        for (int i = 0; i < numberOfAggregatesGeneratedInSegment.size(); i++) {
            String listDataRow = sieveList.get(i) + " - " + sieveList.get(i+1) +" : ";
            numberOfParticlesGenerated.add(listDataRow +  numberOfAggregatesGeneratedInSegment.get(i));
        }
        numberOfParticlesGenerated.add("Total : "+ findArrayListSum(numberOfAggregatesGeneratedInSegment));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, numberOfParticlesGenerated);
        builder.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(MainActivity.this,"You have selected " + numberOfParticles.get(which),Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showReportGeneratedVolume()    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Number of Particles Generated in Each Range");

        final List<String> volumeOfParticlesGenerated = new ArrayList<>();

        for (int i = 0; i < aggVolumePlacedInSegmentList.size(); i++) {
            String listDataRow = sieveList.get(i) + " - " + sieveList.get(i+1) +" : ";
            volumeOfParticlesGenerated.add(listDataRow +  aggVolumePlacedInSegmentList.get(i));
        }
        volumeOfParticlesGenerated.add("Total : "+ findArrayListVolumeSum(aggVolumePlacedInSegmentList));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, volumeOfParticlesGenerated);
        builder.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(MainActivity.this,"You have selected " + numberOfParticles.get(which),Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public double findArrayListSum(ArrayList<Integer> arrayList){
        double sum = 0;
        for(int d : arrayList)
            sum += d;
        return sum;
    }

    public double findArrayListVolumeSum(ArrayList<Float> arrayList){
        double sum = 0;
        for(Float d : arrayList)
            sum += d;
        return sum;
    }
}