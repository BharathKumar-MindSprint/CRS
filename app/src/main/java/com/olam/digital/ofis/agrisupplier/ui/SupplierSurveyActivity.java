package com.olam.digital.ofis.agrisupplier.ui;

import static com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment.ActionPlanProofFragment.getFileWithDate;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.BaseListActivity;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.agrisupplier.adapter.SupplierSurveyAdapter;
import com.olam.digital.ofis.agrisupplier.dto.SupplierSurveyDto;
import com.olam.digital.ofis.agrisupplier.entity.SupplierSubmittedModules;
import com.olam.digital.ofis.agrisupplier.listener.SupplierDialogBoxClickListener;
import com.olam.digital.ofis.gps.GPSListener;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.dto.TrackDetailsDTO;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.FileUtils;
import com.olam.digital.ofis.mvvmmodule.ImageOrientation;
import com.olam.digital.ofis.mvvmmodule.PDFUtils;
import com.olam.digital.ofis.mvvmmodule.PathUtil;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmComplaintFileTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmLookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRangeTextBoxModuleQuestionsTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSectionsItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSurveyAnswerTable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class SupplierSurveyActivity extends BaseListActivity {

    RecyclerView rvSupplierQuestions;
    private OFISRoomDB ofisRoomDB;
    private SupplierSurveyDto supplierSurveyDto;
    private String supplierId, supplierName, supplierDisplayId;
    private Context context;
    private Resources resources;

    private static final int CAMERA_REQUEST = 2277;
    private static final int GALLERY_PICK = 2233;
    private static final int GALLERY_PICK_DOCX = 4455;

    private static int GALLERY_QUESTION_ID = 0;
    private static View VIEW = null;

    private static final int MY_GALLERYPERMISSION_CODE = 100;
    private static final int MY_CAMERA_PERMISSION_CODE = 500;

    private static final int MY_GALLERY_PERMISSION_CODE_TIRAMISU = 222;

    private static final String[] ACCEPTED_MIME_TYPES_IMG = {"image/jpeg", "image/png"};

    private static final String[] ACCEPTED_MIME_TYPES_DOC = {"application/pdf",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel",
            "text/plain", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/csv"};

    private static final String[] ACCEPTED_MIME_TYPES_ALL_FORMAT = {"image/jpeg", "image/png", "application/pdf",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel",
            "text/plain", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/csv"};

    private final int FILE_SIZE = 5242880;
    private final String errorSizeMessage = "5MB";
    private String mCurrentPhotoPath;
    private LinearLayout saveButton, cancelButton;
    private List<DmSurveyAnswerTable> surveyAnswers = new ArrayList<>();
    private DataLayer dl;
    private Location mCurrentCapturedLocation = null;
    GPSListener gpsListener = null;
    private GPSTracker gpsTracker = null;
    private static SQLiteDatabase db;
    private LinkedHashMap<Integer, View> answerViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_survey);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        resources = getResources();
        dl = new DataLayer(resources, context);
        db = Utilities.getWriteableDb(context);

        TextView tvModuleTitle = findViewById(R.id.tvModuleTitle);
        TextView tvModuleDescription = findViewById(R.id.tvModuleDescription);
        rvSupplierQuestions = findViewById(R.id.rvSupplierQuestions);
        saveButton = findViewById(R.id.llSurveySave);
        cancelButton = findViewById(R.id.llCancel);

        supplierSurveyDto = getIntent().getParcelableExtra(Utilities.SUPPLIER);
        supplierId = getIntent().getStringExtra("SupplierId");
        supplierName = getIntent().getStringExtra("SupplierName");
        supplierDisplayId = getIntent().getStringExtra("SupplierDisplayId");
        ofisRoomDB = OFISRoomDB.getDbInstance(context);

        tvModuleTitle.setText(Utilities.getContentByLanguage(supplierSurveyDto, true));
        tvModuleDescription.setText(Utilities.getContentByLanguage(supplierSurveyDto, false));

        gpsListener = new GPSListener() {
            @Override
            public void onGPSClick(Location location) {
                if (location != null && !GPSUtils.isMockLocation(location)) {
                    mCurrentCapturedLocation = location;
                }
            }
        };
        gpsTracker = new GPSTracker(this, gpsListener);
        gpsTracker.startLocationUpdateInitially();

        setSurvey();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateViews(int questionId, View view) {
        answerViews.put(questionId, view);
    }


    private void setSurvey() {
        answerViews = new LinkedHashMap<>();
        List<DmSectionsItem> sections = ofisRoomDB.dmSectionsItemDao().getDmSectionsByModuleId(supplierSurveyDto.getId());
        rvSupplierQuestions = findViewById(R.id.rvSupplierQuestions);
        rvSupplierQuestions.setLayoutManager(new LinearLayoutManager(this));
        SupplierSurveyAdapter supplierSurveyAdapter = new SupplierSurveyAdapter(context, sections, resources, supplierId);
        rvSupplierQuestions.setAdapter(supplierSurveyAdapter);

        supplierSurveyAdapter.setOnDialogClickListener(new SupplierDialogBoxClickListener(){
            @Override
            public void onDialogItemClick(View view, int item, int questionId, String proofFormat, CharSequence[] options) {
                collectCaptureProof(options, item, proofFormat, view, questionId);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSurveyAnswers(true);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSurveyAnswers(false);
            }
        });
    }

    private void collectCaptureProof(CharSequence[] finalOptions, int item, String proofFormat, View view, int questionId) {
        GALLERY_QUESTION_ID = questionId;
        VIEW = view;

        if (finalOptions[item].equals(getResources().getString(R.string.take_photo))) {
            if (isStoragePermissionCameraGranted()) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
                    try {
                        File photoFile = createImageFile();
                        mCurrentPhotoPath = photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, this.getPackageName() + ".fileprovider", new File(mCurrentPhotoPath)));
                        startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        else if (finalOptions[item].equals( getResources().getString(R.string.dm_choose_from_gallery))) {
            if (isStoragePermissionGalleryGranted()) {
                if (Objects.equals(proofFormat, "Document (.doc, .docx, .xls, .xlsx, .pdf, .csv)")) {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, ACCEPTED_MIME_TYPES_DOC);
                    galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    galleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    galleryIntent.setType("*/*");
                    startActivityForResult(Intent.createChooser(galleryIntent, "Choose gallery"), GALLERY_PICK_DOCX);
                } else if (Objects.equals(proofFormat, "Image (.png, .jpg, .jpeg)")) {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, ACCEPTED_MIME_TYPES_IMG);
                    galleryIntent.setType("*/*");
                    startActivityForResult(Intent.createChooser(galleryIntent, "Choose gallery"), GALLERY_PICK);
                } else if (Objects.equals(proofFormat, "Both")) {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, ACCEPTED_MIME_TYPES_ALL_FORMAT);
                    galleryIntent.setType("*/*");
                    startActivityForResult(Intent.createChooser(galleryIntent, "Choose gallery"), GALLERY_PICK);
                }
            }
        }
    }

    public boolean isStoragePermissionGalleryGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                return  true;
            }else {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                android.Manifest.permission.READ_MEDIA_IMAGES}, MY_GALLERY_PERMISSION_CODE_TIRAMISU);
                return false;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_GALLERYPERMISSION_CODE);
                return false;
            }
        }
        else {
            return true;
        }
    }

    public boolean isStoragePermissionCameraGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        MY_CAMERA_PERMISSION_CODE);
                return false;
            }
        }
        else {
            return true;
        }
    }

    private File createImageFile() throws IOException {
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = String.format("JPEG_%s_", ts);
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(filename, ".jpg", storageDir);
    }

    public void showSettingsDialog(String msg) {
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(context);
        dialog.setTitle(R.string.permission_permanent_denial_title);
        dialog.setMessage(msg).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.getPackageName())));
            }
        }).setNegativeButton(R.string.app_update_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Toast.makeText(context, R.string.permission_permanent_denial_msg, Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_GALLERY_PERMISSION_CODE_TIRAMISU) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                        == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                        == PackageManager.PERMISSION_GRANTED)) {
                }
                else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE && grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && (!shouldShowRequestPermissionRationale(permissions[0]))) {
                    showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));
                }else {
                    Toast.makeText(this, R.string.permission_denial_msg_android_13_above, Toast.LENGTH_LONG).show();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, getResources().getString(R.string.dm_please_give_camera_permission), Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == MY_GALLERYPERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, getResources().getString(R.string.dm_please_give_gallery_permission), Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap resizedBitmapNew = null;

        if (resultCode == RESULT_OK) {
            int questionId = GALLERY_QUESTION_ID;
            View view = VIEW;
            RelativeLayout rlEvidenceLayout = view.findViewById(R.id.rlEvidenceLayout);
            TextView evidenceName = view.findViewById(R.id.tv_evidence_name);
            ImageView deleteFile = view.findViewById(R.id.iv_evidence_delete);
            TextView tvEvidenceUpload = view.findViewById(R.id.tvEvidenceUpload);
            tvEvidenceUpload.setVisibility(View.GONE);
            rlEvidenceLayout.setVisibility(View.VISIBLE);

            DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
            String fileSeparator = ".", formatNew = "yyyyMMddHHmmss";
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String currentTime = format.format(new Date());

            dmComplaintFileTable.setIsFileSynced(0);
            dmComplaintFileTable.setFarmerId(supplierId);
            dmComplaintFileTable.setPlotId(0);
            dmComplaintFileTable.setQuestionId(String.valueOf(questionId));

            File[] externalStorageDirs = context.getExternalFilesDirs(null);
            File primaryExternalStorageDir = externalStorageDirs[0];
            File destinationFolder = new File(primaryExternalStorageDir, "OFISDM_PROOF");
            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs();
            }

            StringBuilder OFISFileName = new StringBuilder().append("Proof_").append(supplierSurveyDto.getId()).append("_").append(supplierDisplayId).append("_").append(currentTime).append(".");

            if (requestCode == CAMERA_REQUEST) {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                options.inSampleSize = 2;
                options.inJustDecodeBounds = false;
                options.inTempStorage = new byte[16 * 1024];

                Bitmap bmp = BitmapFactory.decodeFile(mCurrentPhotoPath, options);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmp, 960, 730, false);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Uri uriImage = Uri.fromFile(new File(mCurrentPhotoPath));
                String filename = mCurrentPhotoPath.substring(mCurrentPhotoPath.lastIndexOf("/") + 1);
                OFISFileName.append(Bitmap.CompressFormat.JPEG);

                try {
                    resizedBitmapNew = ImageOrientation.modifyOrientation(this, resizedBitmap, uriImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                resizedBitmapNew.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();

                saveCapturedEvidence(imageBytes, OFISFileName, dmComplaintFileTable, getFileWithDate(filename, fileSeparator, formatNew), evidenceName, destinationFolder, deleteFile, rlEvidenceLayout, tvEvidenceUpload, questionId);
            }

            if (requestCode == GALLERY_PICK && data != null) {
                Uri uri = data.getData();
                ContentResolver cr = this.getContentResolver();
                String mime = cr.getType(uri);

                if (mime.equalsIgnoreCase("application/vnd.ms-excel")
                        || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        || mime.equalsIgnoreCase("application/msword") || mime.equalsIgnoreCase("text/plain")
                        || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                        || mime.equalsIgnoreCase("text/csv")) {

                    String filename = Constants.getFileName(uri, context);
                    String fileExtension = Constants.getFileExtension(filename);
                    OFISFileName.append(fileExtension);

                    byte[] fileByteArray;
                    try {
                        fileByteArray = FileUtils.convertFileToByteArray(this, uri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    saveCapturedEvidence(fileByteArray, OFISFileName, dmComplaintFileTable, getFileWithDate(filename, fileSeparator, formatNew), evidenceName, destinationFolder, deleteFile, rlEvidenceLayout, tvEvidenceUpload, questionId);
                }
                else if (mime.equalsIgnoreCase("application/pdf")) {
                    String filename = Constants.getFileName(uri, context);
                    Uri uri1 = Uri.fromFile(Constants.compressPdf(uri, context, filename));
                    String fileExtension = Constants.getFileExtension(filename);
                    OFISFileName.append(fileExtension);

                    byte[] pdfByteArray2;
                    try {
                        pdfByteArray2 = PDFUtils.convertPDFToByteArray(uri1.getPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    saveCapturedEvidence(pdfByteArray2, OFISFileName, dmComplaintFileTable, getFileWithDate(filename, fileSeparator, formatNew), evidenceName, destinationFolder, deleteFile, rlEvidenceLayout, tvEvidenceUpload, questionId);
                }
                else if (mime.equalsIgnoreCase("image/jpeg") || mime.equalsIgnoreCase("image/png")) {
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    options.inSampleSize = 2;
                    options.inJustDecodeBounds = false;
                    options.inTempStorage = new byte[16 * 1024];
                    String filePathimage = null;

                    try {
                        filePathimage = PathUtil.getPath(this, uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Bitmap bmp = BitmapFactory.decodeFile(filePathimage, options);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    String filename = filePathimage.substring(filePathimage.lastIndexOf("/") + 1);
                    String fileExtension = Constants.getFileExtension(filename);
                    OFISFileName.append(fileExtension);

                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmp, 750, 650, false);
                    try {
                        resizedBitmapNew = ImageOrientation.modifyOrientation(this, resizedBitmap, uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fileExtension.equalsIgnoreCase(Bitmap.CompressFormat.PNG.name())) {
                        resizedBitmapNew.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    } else {
                        resizedBitmapNew.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    }

                    byte[] imageBytes = baos.toByteArray();

                    saveCapturedEvidence(imageBytes, OFISFileName, dmComplaintFileTable, filename, evidenceName, destinationFolder, deleteFile, rlEvidenceLayout, tvEvidenceUpload, questionId);

                    if (PathUtil.isDownloadsDocument(uri)) {
                        File file = new File(filePathimage);
                        if (file.exists()) {
                            if (file.delete()) {
                                Log.d("IsDownloadDocument","File deleted successfully");
                            } else {
                                Log.d("IsDownloadDocument","Failed to delete file");
                            }
                        }
                    }

                }
            }

            if (requestCode == GALLERY_PICK_DOCX && data != null) {
                Uri uri = data.getData();
                ContentResolver cr = this.getContentResolver();
                String mime = cr.getType(uri);

                if (mime.equalsIgnoreCase("application/vnd.ms-excel")
                        || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        || mime.equalsIgnoreCase("application/msword") || mime.equalsIgnoreCase("text/plain")
                        || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                        || mime.equalsIgnoreCase("text/csv")) {
                    String filename = Constants.getFileName(uri, context);
                    String fileExtension = Constants.getFileExtension(filename);
                    OFISFileName.append(fileExtension);

                    byte[] fileByteArray;
                    try {
                        fileByteArray = FileUtils.convertFileToByteArray(this, uri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    saveCapturedEvidence(fileByteArray, OFISFileName, dmComplaintFileTable, filename, evidenceName, destinationFolder, deleteFile, rlEvidenceLayout, tvEvidenceUpload, questionId);
                }
                else if (mime.equalsIgnoreCase("application/pdf")) {
                    String filename = Constants.getFileName(uri, context);
                    Uri uri1 = Uri.fromFile(Constants.compressPdf(uri, context, filename));
                    String fileExtension = Constants.getFileExtension(filename);
                    OFISFileName.append(fileExtension);

                    byte[] pdfByteArray2;
                    try {
                        pdfByteArray2 = PDFUtils.convertPDFToByteArray(uri1.getPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    saveCapturedEvidence(pdfByteArray2, OFISFileName, dmComplaintFileTable, filename, evidenceName, destinationFolder, deleteFile, rlEvidenceLayout, tvEvidenceUpload, questionId);
                }
                else {
                    Toast.makeText(context, "Required Format:(.doc, .docx, .xls, .xlsx, .pdf, .csv)", Toast.LENGTH_SHORT).show();
                    evidenceName.setText("");
                    rlEvidenceLayout.setVisibility(View.GONE);
                    tvEvidenceUpload.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    private void saveCapturedEvidence(byte[] pdfByteArray2, StringBuilder OFISFileName, DmComplaintFileTable dmComplaintFileTable, String filename, TextView evidenceName, File destinationFolder, ImageView deleteFile, RelativeLayout rlEvidenceLayout, TextView tvEvidenceUpload, int questionId) {
        if (validateFileSize(pdfByteArray2.length)) {
            return;
        }

        dmComplaintFileTable.setFile_name(filename);
        dmComplaintFileTable.setOfisFileName(OFISFileName.toString());

        evidenceName.setText(filename);
        File destinationFile = new File(destinationFolder, OFISFileName.toString());
        storeFileDataAtDestinationPath(destinationFile, pdfByteArray2);
        String destinationFilePath = destinationFile.getAbsolutePath();
        dmComplaintFileTable.setFilePath(destinationFilePath);

        ofisRoomDB.dmComplaintFileTableDao().insert(dmComplaintFileTable);

        deleteFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder((Activity) context);
                builder1.setMessage(getResources().getString(R.string.dm_do_you_want_to_remove_this_report));
                builder1.setTitle(getResources().getString(R.string.Alert_txt));
                builder1.setCancelable(false);
                builder1.setPositiveButton(getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                evidenceName.setText("");
                                rlEvidenceLayout.setVisibility(View.GONE);
                                tvEvidenceUpload.setVisibility(View.VISIBLE);
                                ofisRoomDB.dmComplaintFileTableDao().deleteByQuestionId(questionId, supplierId);
                            }
                        }
                );

                builder1.setNegativeButton(getResources().getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });
    }

    private static void storeFileDataAtDestinationPath(File destinationFile, byte[] imageBytes) {
        try {
            FileOutputStream outputStream = new FileOutputStream(destinationFile);
            outputStream.write(imageBytes);
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validateFileSize(int size) {
        if (size > FILE_SIZE) {
            Toast.makeText(context, getString(R.string.file_exceeds) + errorSizeMessage, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void saveSurveyAnswers(boolean isSurveyCompleted) {
        surveyAnswers.clear();
        for (Map.Entry<Integer, View> entry : answerViews.entrySet()) {
            View questionView = entry.getValue();
            if (!validateAnswers(questionView, isSurveyCompleted)) {
                return;
            }
        }

        ofisRoomDB.dmSurveyAnswerTableDao().deleteAllDataForRecurrence(String.valueOf(supplierSurveyDto.getId()), supplierId, 0);
        ofisRoomDB.supplierSubmittedModuleDao().deleteSubmittedModule(String.valueOf(supplierSurveyDto.getId()), supplierId.substring(2), "false");
        ofisRoomDB.dmSurveyAnswerTableDao().insertAll(surveyAnswers);

        if (isSurveyCompleted) {
            if (!saveNetworkQueueEntry()) {
                return;
            }
            ofisRoomDB.supplierSubmittedModuleDao().insert(new SupplierSubmittedModules(formatToUTCDate(), supplierId.substring(2), String.valueOf(supplierSurveyDto.getId()), "false", "true"));
        }
        else {
            dl.deleteSupplierSurveyNetworkQueueItem(supplierId, "Supplier Survey", supplierSurveyDto.getId());
            ofisRoomDB.supplierSubmittedModuleDao().insert(new SupplierSubmittedModules(formatToUTCDate(), supplierId.substring(2), String.valueOf(supplierSurveyDto.getId()), "false", "false"));
        }

        finish();
    }

    private boolean saveNetworkQueueEntry() {
        if (mCurrentCapturedLocation == null || !gpsTracker.isGpsEnabled() || GPSUtils.isMockLocation(mCurrentCapturedLocation)) {
            gpsTracker.getDeviceLocation();
            return false;
        }

        boolean updateWaiting = dl.networkEntryTypeExistsByFarmerModuleId("Supplier Survey", supplierId, String.valueOf(supplierSurveyDto.getId()), 0);
        if(!updateWaiting) {
            collectGPSPoint(mCurrentCapturedLocation);
            gpsTracker.stopLocationUpdates();
        }

        String location = mCurrentCapturedLocation.getLatitude() + "," + mCurrentCapturedLocation.getLongitude();

        if (updateWaiting) {
            ContentValues nqiValues = new ContentValues();
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, formatToUTCDate());
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT, location);
            String[] whereArgsFarm = new String[]{supplierId, "Supplier Survey", "0", String.valueOf(supplierSurveyDto.getId())};
            db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, nqiValues, OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLOT_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_MODULE_ID + " = ? ", whereArgsFarm);
        }else {
            ContentValues networkQueueValues = new ContentValues();
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "Supplier Survey");
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, Utilities.getContentByLanguage(supplierSurveyDto, true));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, resources.getString(R.string.supplier_survey));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, supplierId);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, formatToUTCDate());
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, formatToUTCDate());
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_MODULE_ID, supplierSurveyDto.getId());
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, supplierName);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLOT_ID, 0);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT, location);
            dl.insertNetworkQueueEntry(networkQueueValues);
        }
        return true;
    }

    @NonNull
    private String formatToUTCDate() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedUtcDateTime = sdf.format(calendar.getTime());
        return formattedUtcDateTime;
    }

    private void collectGPSPoint(Location farmLocation) {
        TrackDetailsDTO trackDetailsDTO = new TrackDetailsDTO();
        trackDetailsDTO.setModule("Suppler Survey");
        trackDetailsDTO.setLatitude(String.valueOf(farmLocation.getLatitude()));
        trackDetailsDTO.setLongitude(String.valueOf(farmLocation.getLongitude()));
        trackDetailsDTO.setAccuracy(String.valueOf(farmLocation.getAccuracy()));
        trackDetailsDTO.setRecordDate(new Timestamp(System.currentTimeMillis()).toString());
        trackDetailsDTO.setFarmerId(supplierId.substring(2));
        trackDetailsDTO.setFarmerName(supplierName);
        trackDetailsDTO.setSurveyName(supplierSurveyDto.getName());

        ContentValues geoContentValues = new GPSUtils().getGeoContentValues(trackDetailsDTO);
        new GpsTrackerLogic().insertGeoDataDetails(context, geoContentValues);
    }

    private boolean validateAnswers(View questionView, boolean isSurveyCompleted) {
        DmQuestionsModule question = (DmQuestionsModule) questionView.getTag();
        if (question.displayType.equalsIgnoreCase("text box")) {
            return validateTextBoxAnswer(questionView, question, isSurveyCompleted);
        }
        else if (question.displayType.equalsIgnoreCase("dropdown")) {
            return validateDropdownAnswer(questionView, question, isSurveyCompleted);
        }

        return true;
    }

    private boolean validateTextBoxAnswer(View questionView, DmQuestionsModule question, boolean isSurveyCompleted) {
        EditText et = questionView.findViewById(R.id.etAnswer);
        TextView tvErrorMessage = questionView.findViewById(R.id.tvErrorMessage);
        TextView btnEvidenceUpload = questionView.findViewById(R.id.tvEvidenceUpload);
        String answer = et.getText().toString().trim();

        if (isSurveyCompleted && (question.mandate || question.isCriticalQuest) && answer.isEmpty()) {
            et.setBackgroundResource(R.drawable.answer_text_error);
            tvErrorMessage.setVisibility(View.VISIBLE);
            tvErrorMessage.setFocusable(true);
            return false;
        }

        DmRangeTextBoxModuleQuestionsTable dmRangeTextBoxModuleQuestionsTable = new DmRangeTextBoxModuleQuestionsTable();

        if (!answer.isEmpty()) {
            dmRangeTextBoxModuleQuestionsTable = ofisRoomDB.dmRangeTextBoxModuleQuestionsTableDao().getDmRangeTextBoxModuleQuestionsTableByAnswer(question.id, Double.parseDouble(answer));
            if (isSurveyCompleted && dmRangeTextBoxModuleQuestionsTable.isCaptureProof) {
                if (isEvidenceUploaded(question, btnEvidenceUpload)) {
                    return false;
                }
            }
        }

        DmSurveyAnswerTable dmSurveyAnswerTable = new DmSurveyAnswerTable();
        dmSurveyAnswerTable.questionID = String.valueOf(question.id);
        dmSurveyAnswerTable.answer = answer;
        dmSurveyAnswerTable.answerID = String.valueOf(dmRangeTextBoxModuleQuestionsTable.id);
        dmSurveyAnswerTable.moduleId = String.valueOf(supplierSurveyDto.getId());
        dmSurveyAnswerTable.question = question.question;
        dmSurveyAnswerTable.complinceTypeId = String.valueOf(dmRangeTextBoxModuleQuestionsTable.complianceId);
        dmSurveyAnswerTable.isCaptureProof = dmRangeTextBoxModuleQuestionsTable.isCaptureProof;
        dmSurveyAnswerTable.displayType = "textbox";
        dmSurveyAnswerTable.farmerId = supplierId;
        dmSurveyAnswerTable.isSynced = false;
        dmSurveyAnswerTable.plotId = 0;

        if (dmRangeTextBoxModuleQuestionsTable.id != null && !answer.isEmpty() && dmRangeTextBoxModuleQuestionsTable.complianceId != 0) {
            surveyAnswers.add(dmSurveyAnswerTable);
        }
        return true;
    }

    private boolean validateDropdownAnswer(View questionView, DmQuestionsModule question, boolean isSurveyCompleted) {
        Spinner spinner = questionView.findViewById(R.id.spinnerAnswer);
        TextView tvErrorMessage = questionView.findViewById(R.id.tvErrorMessage);
        TextView btnEvidenceUpload = questionView.findViewById(R.id.tvEvidenceUpload);

        DmLookupValuesItem dmLookupValuesItem = (DmLookupValuesItem) spinner.getTag();

        if (dmLookupValuesItem == null) {
            dmLookupValuesItem = new DmLookupValuesItem();
        }

        if (isSurveyCompleted && (question.mandate || question.isCriticalQuest) && spinner.getSelectedItem().equals(resources.getString(R.string.ModuleAnswerHint))) {
            spinner.setBackgroundResource(R.drawable.spinner_bg_error);
            tvErrorMessage.setFocusable(true);
            tvErrorMessage.setVisibility(View.VISIBLE);
            return false;
        }

        if (isSurveyCompleted && dmLookupValuesItem.isCaptureProof) {
            if (isEvidenceUploaded(question, btnEvidenceUpload)) {
                return false;
            }
        }

        DmSurveyAnswerTable dmSurveyAnswerTable = new DmSurveyAnswerTable();
        dmSurveyAnswerTable.questionID = String.valueOf(question.id);
        dmSurveyAnswerTable.answerID = String.valueOf(dmLookupValuesItem.id);
        dmSurveyAnswerTable.answer = dmLookupValuesItem.value;
        dmSurveyAnswerTable.moduleId = String.valueOf(supplierSurveyDto.getId());
        dmSurveyAnswerTable.question = question.question;
        dmSurveyAnswerTable.complinceTypeId = String.valueOf(dmLookupValuesItem.complianceId);
        dmSurveyAnswerTable.farmerId = supplierId;
        dmSurveyAnswerTable.isCaptureProof = dmLookupValuesItem.isCaptureProof;
        dmSurveyAnswerTable.isSynced = false;
        dmSurveyAnswerTable.plotId = 0;
        dmSurveyAnswerTable.displayType = "dropdown";

        if (dmLookupValuesItem.id != 0 && dmLookupValuesItem.value != null && dmLookupValuesItem.complianceId != 0) {
            surveyAnswers.add(dmSurveyAnswerTable);
        }

        return true;
    }

    private boolean isEvidenceUploaded(DmQuestionsModule question, TextView btnEvidenceUpload) {
        DmComplaintFileTable dmComplaintFileTable = ofisRoomDB.dmComplaintFileTableDao().getFileByQuestionId(question.id, supplierId);
        if (dmComplaintFileTable == null) {
            Toast.makeText(context, resources.getString(R.string.evidence_is_missing), Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }



}